package net.kalbskinder.infection.utils;

import net.kalbskinder.infection.Infection;

public class Prefixes {
    private static Infection plugin;

    // Get the plugin instance
    public static void register(Infection main) {
        plugin = main;
    }

    // Returns the plugins default prefix
    public static String getPrefix() {
        return "[<gradient:#ff8112:#ffdf12>Infection<reset>] ";
    }

    // Returns the prefix saved in the config
    public static String getCustomPrefix() {
        return plugin.getConfig().getString("messages.prefix", "");
    }
}