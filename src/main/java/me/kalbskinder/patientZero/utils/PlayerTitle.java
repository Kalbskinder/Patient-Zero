package me.kalbskinder.patientZero.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

import java.time.Duration;

public class PlayerTitle {

    /**
     * Display a title to a player
     *
     * @param player The player to whom the title is displayed
     * @param title The title
     * @param subtitle The subtitle
     */
    public static void displayPlayerTitle(Player player, String title, String subtitle, float fadeIn, float stay, float fadeOut) {
        Component mainTitle = Component.text(title);
        Component sub = Component.text(subtitle);

        Title.Times times = Title.Times.times(
                Duration.ofMillis((long)(fadeIn * 1000)),
                Duration.ofMillis((long)(stay * 1000)),
                Duration.ofMillis((long)(fadeOut * 1000))
        );

        Title titleObject = Title.title(mainTitle, sub, times);

        player.showTitle(titleObject); // Show the title
    }
}
