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
        return "§f[§x§f§2§8§8§0§6p§x§f§6§a§a§2§3t§x§f§b§c§d§4§1z§r] ";
    }

    // Returns the prefix saved in the config
    public static String getCustomPrefix() {
        return plugin.getConfig().getString("messages.prefix", "");
    }
}
