package me.kalbskinder.patientZero.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class CenterTag {
    private static final int CENTER_PX = 160; // Middle of the chat

    private static final MiniMessage mm = MiniMessage.miniMessage();

    public static Component deserializeCentered(String input) {
        if (input == null || !input.startsWith("<center>")) {
            return mm.deserialize(input);
        }

        // Strip <center>
        String raw = input.replaceFirst("<center>", "");
        Component component = mm.deserialize(raw);

        int pixelLength = getPixelLength(component);
        int spaces = Math.max(0, (CENTER_PX - pixelLength / 2) / 4);

        return Component.text(" ".repeat(spaces)).append(component);
    }

    private static int getPixelLength(Component component) {
        String plain = MiniMessage.miniMessage().serialize(component).replaceAll("<[^>]+>", ""); // Ignore minimessage tags
        int length = 0;

        for (char c : plain.toCharArray()) {
            int charWidth = switch (c) {
                case 'i', 'l', '.', ',', ':' -> 2;
                case 't', 'f' -> 4;
                case 'w', 'm', 'W', 'M' -> 7;
                case ' ' -> 4;
                default -> 5;
            };
            length += charWidth + 1;
        }

        return length;
    }
}