package me.kalbskinder.patientZero.utils;

import me.kalbskinder.patientZero.PatientZero;

public class Prefixes {
    private static PatientZero plugin;

    // Get the plugin instance
    public static void register(PatientZero main) {
        plugin = main;
    }

    // Returns the plugins default prefix
    public static String getPrefix() {
        return "[<gradient:#ff8112:#ffdf12>ptz<reset>] ";
    }

    // Returns the prefix saved in the config
    public static String getCustomPrefix() {
        return plugin.getConfig().getString("messages.prefix", "");
    }
}