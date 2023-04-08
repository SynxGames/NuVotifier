package com.vexsoftware.votifier.fabric.config;

import com.vexsoftware.votifier.fabric.NuVotifier;
import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurationNode;
import org.spongepowered.configurate.ConfigurationOptions;
import org.spongepowered.configurate.loader.ConfigurationLoader;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;

import java.io.File;
import java.io.IOException;

public class ConfigLoader {

    private static FabricConfig fabricConfig;

    public static void loadConfig(NuVotifier pl) {
        if (!pl.getConfigDir().exists()) {
            if (!pl.getConfigDir().mkdirs()) {
                throw new RuntimeException("Unable to create the plugin data folder " + pl.getConfigDir());
            }
        }
        try {
            File config = new File(pl.getConfigDir(), "config.yml");
            if (!config.exists() && !config.createNewFile()) {
                throw new IOException("Unable to create the config file at " + config);
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = YamlConfigurationLoader.builder().file(config).build();
            ConfigurationNode configNode = loader.load(ConfigurationOptions.defaults().shouldCopyDefaults(true));
            fabricConfig = configNode.get(FabricConfig.class, new FabricConfig());
            loader.save(configNode);
        } catch (Exception e) {
            pl.getPluginLogger().error("Could not load config.", e);
        }
    }

    public static FabricConfig getFabricConfig() {
        return fabricConfig;
    }
}
