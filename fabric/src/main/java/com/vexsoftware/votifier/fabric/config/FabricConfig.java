package com.vexsoftware.votifier.fabric.config;

import com.vexsoftware.votifier.util.TokenUtil;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;
import org.spongepowered.configurate.objectmapping.meta.Comment;
import org.spongepowered.configurate.objectmapping.meta.Setting;

import java.util.Collections;
import java.util.Map;

@ConfigSerializable
public class FabricConfig {

    @Comment("The IP to listen to. Use 0.0.0.0 if you wish to listen to all interfaces on your server. (All IP addresses)\n" +
            "This defaults to the IP you have configured your server to listen on, or 0.0.0.0 if you have not configured this.")
    @Setting
    public String host = "0.0.0.0";

    @Comment("Port to listen for new votes on")
    @Setting
    public int port = 8192;

    @Comment("Whether or not to print debug messages. In a production system, this should be set to false.\n" +
            "This is useful when initially setting up NuVotifier to ensure votes are being delivered.")
    @Setting
    public boolean debug = true;

    @Comment("Setting this option to true will disable handling of Protocol v1 packets. While the old protocol is not secure, this\n" +
            "option is currently not recommended as most voting sites only support the old protocol at present. However, if you are\n" +
            "using NuVotifier's proxy forwarding mechanism, enabling this option will increase your server's security.")
    @Setting("disable-v1-protocol")
    public boolean disableV1Protocol = false;

    @Comment("All tokens, labeled by the serviceName of each server list.\n" +
            "Default token for all server lists, if another isn't supplied.")
    @Setting
    public Map<String, String> tokens = Collections.singletonMap("default", TokenUtil.newToken());

    @Comment("Configuration section for all vote forwarding to NuVotifier")
    @Setting
    public Forwarding forwarding = new Forwarding();

    @ConfigSerializable
    public static class Forwarding {

        @Comment("Sets whether to set up a remote method for fowarding. Supported methods:\n" +
                "- none - Does not set up a forwarding method.\n" +
                "- pluginMessaging - Sets up plugin messaging")
        @Setting
        public String method = "none";

        @Setting
        public PluginMessaging pluginMessaging = new PluginMessaging();

        @ConfigSerializable
        public static class PluginMessaging {

            @Setting
            public String channel = "nuvotifier:votes";
        }
    }
}