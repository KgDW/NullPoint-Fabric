package me.nullpoint.socket.enums;



/**
 * @author DreamDev
 * @since 4/5/2024
 */
public enum ClientType {
    EMPTY("Empty"),
    Kura("Kura"),
    Rebirth("Rebirth"),
    ArtDay("ArtDay"),
    FoxSense("FoxSense"),
    Dominic("Dominic"),
    Artist("Artist"),
    EDFDP("Edfdp"),
    Salt("Salt"),
    Forever("Forever"),
    NullPoint("NullPoint"),
    Ark("Ark"),
    Eve("Eve"),
    NEVER("Never");
    private final String name;

    ClientType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
