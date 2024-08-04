package me.nullpoint.socket.enums;


import me.nullpoint.api.utils.render.EnumChatFormatting;

/**
 * @author DreamDev
 * @since 4/5/2024
 */
public enum Rank {
    EMPTY("Empty"),
    USER("User"),
    CONTRIBUTOR("Contributor"),
    BETA("Beta"),
    TESTER("Tester"),
    MEDIA("Media"),
    PARTNER("Partner"),
    MODERATOR("Moderator"),
    DEV("Dev"),
    ADMIN("Admin"),
    FEMBOY("Femboy"),
    GAY("Gay"),
    OWNER("Owner");

    private final String name;

    Rank(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public EnumChatFormatting color() {
        return color(this);
    }

    public EnumChatFormatting color(String rank) {
        return color(Rank.valueOf(rank.toUpperCase()));
    }

    public EnumChatFormatting color(Rank rank) {
        if (rank == null) {
            rank = Rank.EMPTY;
        }
        EnumChatFormatting formatting = EnumChatFormatting.RESET;

        switch (rank) {
            case OWNER:
                formatting =EnumChatFormatting.RED;
                //jiu zhe me xie
                break;
            case ADMIN:
                formatting =EnumChatFormatting.GOLD;
                break;
            case DEV:
                formatting =EnumChatFormatting.LIGHT_PURPLE;
                break;
            case MODERATOR:
                formatting =EnumChatFormatting.YELLOW;
                break;
            case PARTNER:
                formatting =EnumChatFormatting.GREEN;
                break;
            case MEDIA:
                formatting =EnumChatFormatting.DARK_PURPLE;
                break;
            case CONTRIBUTOR:
                formatting =EnumChatFormatting.AQUA;
                break;
            case BETA:
                formatting =EnumChatFormatting.BLUE;
                break;
            case TESTER:
                formatting =EnumChatFormatting.DARK_BLUE;
                break;
            case USER:
                formatting =EnumChatFormatting.GRAY;
                break;
            case FEMBOY:
                formatting =EnumChatFormatting.YELLOW;
                break;
            case GAY:
                formatting =EnumChatFormatting.BLACK;
                break;
            case EMPTY:
                formatting = EnumChatFormatting.WHITE;
                break;
        }

        return formatting;
    }
}
