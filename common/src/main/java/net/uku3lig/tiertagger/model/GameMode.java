package net.uku3lig.tiertagger.model;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.uku3lig.tiertagger.TierCache;

import java.util.List;
import java.util.Optional;

public record GameMode(String id, String title) {

    public static final GameMode NONE =
            new GameMode("none", "None");

    /**
     * PRIME TIERS MODE LIST (EDIT THIS TO MATCH YOUR WEBSITE)
     */
    public static List<GameMode> fetchFromPrimeTiers() {
        return List.of(
                new GameMode("nodebuff", "No Debuff"),
                new GameMode("uhc", "UHC"),
                new GameMode("sword", "Sword"),
                new GameMode("axe", "Axe"),
                new GameMode("crystal", "Crystal"),
                new GameMode("bow", "Bow"),
                new GameMode("smp", "SMP")
        );
    }

    public boolean isNone() {
        return this.id.equals(NONE.id);
    }

    private Pair iconAndColor() {
        return switch (this.id) {
            case "axe" -> Pair.of('A', ChatFormatting.GREEN);
            case "uhc" -> Pair.of('U', ChatFormatting.RED);
            case "sword" -> Pair.of('S', ChatFormatting.AQUA);
            case "nodebuff" -> Pair.of('N', ChatFormatting.BLUE);
            case "crystal" -> Pair.of('C', ChatFormatting.LIGHT_PURPLE);
            case "bow" -> Pair.of('B', ChatFormatting.GOLD);
            case "smp" -> Pair.of('M', ChatFormatting.YELLOW);
            default -> Pair.of('•', ChatFormatting.WHITE);
        };
    }

    public Component asStyled(boolean withDefaultDot) {
        var pair = iconAndColor();

        ChatFormatting color = pair.right();

        if (color == ChatFormatting.WHITE && !withDefaultDot) {
            return Component.literal(title);
        }

        return Component.literal(pair.left() + " ")
                .append(Component.literal(title).withStyle(s -> s.withColor(color)));
    }
}
