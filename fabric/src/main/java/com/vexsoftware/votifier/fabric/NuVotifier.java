package com.vexsoftware.votifier.fabric;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.vexsoftware.votifier.VoteHandler;
import com.vexsoftware.votifier.fabric.config.ConfigLoader;
import com.vexsoftware.votifier.fabric.event.VotifierEvent;
import com.vexsoftware.votifier.fabric.forwarding.FabricPluginMessagingForwardingSink;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.net.VotifierServerBootstrap;
import com.vexsoftware.votifier.net.VotifierSession;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAIO;
import com.vexsoftware.votifier.net.protocol.v1crypto.RSAKeygen;
import com.vexsoftware.votifier.platform.LoggingAdapter;
import com.vexsoftware.votifier.platform.VotifierPlugin;
import com.vexsoftware.votifier.platform.scheduler.VotifierScheduler;
import com.vexsoftware.votifier.support.forwarding.ForwardedVoteListener;
import com.vexsoftware.votifier.support.forwarding.ForwardingVoteSink;
import com.vexsoftware.votifier.util.ArgsToVote;
import com.vexsoftware.votifier.util.KeyCreator;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.security.Key;
import java.security.KeyPair;
import java.util.HashMap;
import java.util.Map;

public class NuVotifier implements ModInitializer, VoteHandler, VotifierPlugin, ForwardedVoteListener {

    public static final Logger LOGGER = LoggerFactory.getLogger("NuVotifier");

    /**
     * The server bootstrap.
     */
    private VotifierServerBootstrap bootstrap;
    private SLF4JLogger loggerAdapter;


    private KeyPair keyPair;
    private boolean debug;
    private Map<String, Key> tokens = new HashMap<>();

    private ForwardingVoteSink forwardingMethod;

    private FabricScheduler scheduler;

    private File configDir;

    @Override
    public void onInitialize() {

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            this.scheduler = new FabricScheduler();
            this.loggerAdapter = new SLF4JLogger(LOGGER);

            if (!loadAndBind()) {
                gracefulExit();
            }

        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> {

            dispatcher.register(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("nvreload")
                            .requires(source -> Permissions.check(source, "nuvotifier.reload"))
                            .executes(context -> {

                                CommandSourceStack source = context.getSource();
                                source.sendSystemMessage(Component.literal("Reloading NuVotifier...").withStyle(ChatFormatting.GRAY));
                                if (this.reload()) {
                                    source.sendSystemMessage(Component.literal("Reloaded NuVotifier!").withStyle(ChatFormatting.GREEN));
                                    return Command.SINGLE_SUCCESS;
                                } else {
                                    source.sendSystemMessage(Component.literal("Failed to reload NuVotifier!").withStyle(ChatFormatting.RED));
                                    return 0;
                                }
                            })
            );

            dispatcher.register(
                    LiteralArgumentBuilder.<CommandSourceStack>literal("testvote")
                            .requires(source -> Permissions.check(source, "nuvotifier.testvote"))
                            .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("args", StringArgumentType.greedyString())
                                    .executes(context -> {

                                        CommandSourceStack source = context.getSource();

                                        Vote vote;
                                        try {
                                            vote = ArgsToVote.parse(StringArgumentType.getString(context, "args").split(" "));
                                        } catch (Exception e) {
                                            source.sendSystemMessage(Component.literal("Error while parsing arguments to create test vote: ").withStyle(ChatFormatting.RED));
                                            source.sendSystemMessage(Component.literal("Usage hint: /testvote [username] [serviceName=?] [username=?] [address=?] [localTimestamp=?] [timestamp=?]").withStyle(ChatFormatting.GRAY));
                                            return Command.SINGLE_SUCCESS;
                                        }

                                        onVoteReceived(vote, VotifierSession.ProtocolVersion.TEST, "localhost.test");
                                        source.sendSystemMessage(Component.literal("Test vote executed: " + vote).withStyle(ChatFormatting.GREEN));
                                        return Command.SINGLE_SUCCESS;
                                    })
                            )
            );

        });

        VotifierEvent.EVENT.register(vote -> {

        });

    }

    private boolean loadAndBind() {

        this.configDir = new File("config" + File.separator + "nuvotifier");

        // Load configuration.
        ConfigLoader.loadConfig(this);

        /*
         * Create RSA directory and keys if it does not exist; otherwise, read
         * keys.
         */
        File rsaDirectory = new File(configDir, "rsa");
        try {
            if (!rsaDirectory.exists()) {
                if (!rsaDirectory.mkdir()) {
                    throw new RuntimeException("Unable to create the RSA key folder " + rsaDirectory);
                }
                keyPair = RSAKeygen.generate(2048);
                RSAIO.save(rsaDirectory, keyPair);
            } else {
                keyPair = RSAIO.load(rsaDirectory);
            }
        } catch (Exception ex) {
            LOGGER.error("Error creating or reading RSA tokens", ex);
            return false;
        }

        debug = ConfigLoader.getFabricConfig().debug;

        // Load Votifier tokens.
        ConfigLoader.getFabricConfig().tokens.forEach((s, s2) -> {
            tokens.put(s, KeyCreator.createKeyFrom(s2));
            LOGGER.info("Loaded token for website: " + s);
        });

        // Initialize the receiver.
        final String host = ConfigLoader.getFabricConfig().host;
        final int port = ConfigLoader.getFabricConfig().port;

        if (!debug)
            LOGGER.info("QUIET mode enabled!");

        if (port >= 0) {
            final boolean disablev1 = ConfigLoader.getFabricConfig().disableV1Protocol;
            if (disablev1) {
                LOGGER.info("------------------------------------------------------------------------------");
                LOGGER.info("Votifier protocol v1 parsing has been disabled. Most voting websites do not");
                LOGGER.info("currently support the modern Votifier protocol in NuVotifier.");
                LOGGER.info("------------------------------------------------------------------------------");
            }

            this.bootstrap = new VotifierServerBootstrap(host, port, this, disablev1);
            this.bootstrap.start(err -> {
            });
        } else {
            LOGGER.info("------------------------------------------------------------------------------");
            LOGGER.info("Your Votifier port is less than 0, so we assume you do NOT want to start the");
            LOGGER.info("votifier port server! Votifier will not listen for votes over any port, and");
            LOGGER.info("will only listen for pluginMessaging forwarded votes!");
            LOGGER.info("------------------------------------------------------------------------------");
        }

        if (ConfigLoader.getFabricConfig().forwarding != null) {
            String method = ConfigLoader.getFabricConfig().forwarding.method.toLowerCase(); //Default to lower case for case-insensitive searches
            if ("none".equals(method)) {
                LOGGER.info("Method none selected for vote forwarding: Votes will not be received from a forwarder.");
            } else if ("pluginmessaging".equals(method)) {
                String channel = ConfigLoader.getFabricConfig().forwarding.pluginMessaging.channel;
                try {
                    forwardingMethod = new FabricPluginMessagingForwardingSink(this, channel, this);
                    LOGGER.info("Receiving votes over PluginMessaging channel '" + channel + "'.");
                } catch (RuntimeException e) {
                    LOGGER.error("NuVotifier could not set up PluginMessaging for vote forwarding!", e);
                }
            } else {
                LOGGER.error("No vote forwarding method '" + method + "' known. Defaulting to noop implementation.");
            }
        }
        return true;
    }

    private void halt() {
        // Shut down the network handlers.
        if (bootstrap != null) {
            bootstrap.shutdown();
            bootstrap = null;
        }

        if (forwardingMethod != null) {
            forwardingMethod.halt();
            forwardingMethod = null;
        }
    }

    public boolean reload() {
        try {
            halt();
        } catch (Exception ex) {
            LOGGER.error("On halt, an exception was thrown. This may be fine!", ex);
        }

        if (loadAndBind()) {
            LOGGER.info("Reload was successful.");
            return true;
        } else {
            try {
                halt();
                LOGGER.error("On reload, there was a problem with the configuration. Votifier currently does nothing!");
            } catch (Exception ex) {
                LOGGER.error("On reload, there was a problem loading, and we could not re-halt the server. Votifier is in an unstable state!", ex);
            }
            return false;
        }
    }

    private void gracefulExit() {
        LOGGER.error("Votifier did not initialize properly!");
    }

    @Override
    public LoggingAdapter getPluginLogger() {
        return loggerAdapter;
    }

    @Override
    public VotifierScheduler getScheduler() {
        return scheduler;
    }

    public boolean isDebug() {
        return debug;
    }

    @Override
    public Map<String, Key> getTokens() {
        return tokens;
    }

    @Override
    public KeyPair getProtocolV1Key() {
        return keyPair;
    }

    @Override
    public void onVoteReceived(final Vote vote, VotifierSession.ProtocolVersion protocolVersion, String remoteAddress) {
        if (debug) {
            if (protocolVersion == VotifierSession.ProtocolVersion.ONE) {
                LOGGER.info("Got a protocol v1 vote record from " + remoteAddress + " -> " + vote);
            } else {
                LOGGER.info("Got a protocol v2 vote record from " + remoteAddress + " -> " + vote);
            }
        }
        this.fireVoteEvent(vote);
    }

    @Override
    public void onError(Throwable throwable, boolean alreadyHandledVote, String remoteAddress) {
        if (debug) {
            if (alreadyHandledVote) {
                LOGGER.error("Vote processed, however an exception " +
                        "occurred with a vote from " + remoteAddress, throwable);
            } else {
                LOGGER.error("Unable to process vote from " + remoteAddress, throwable);
            }
        } else if (!alreadyHandledVote) {
            LOGGER.error("Unable to process vote from " + remoteAddress);
        }
    }

    @Override
    public void onForward(final Vote v) {
        if (debug) {
            LOGGER.info("Got a forwarded vote -> " + v);
        }
        fireVoteEvent(v);
    }

    private void fireVoteEvent(final Vote vote) {
        VotifierEvent.EVENT.invoker().interact(vote);
    }

    public File getConfigDir() {
        return configDir;
    }

}
