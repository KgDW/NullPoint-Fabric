package me.nullpoint.socket.network.info;

import me.nullpoint.socket.network.info.record.GameInfo;
import me.nullpoint.socket.network.info.record.UserInfo;
import me.nullpoint.socket.enums.ClientType;


/**
 * @author DreamDev
 * @since 4/3/2024
 */
public class ClientInfo {
    private ClientType client;
    private UserInfo user;
    private GameInfo game;

    public ClientInfo()
    {
        this.client = ClientType.EMPTY;
        this.user = null;
        this.game = null;
    }

    public ClientInfo(ClientType client, UserInfo user, GameInfo game)
    {
        this.client = client;
        this.user = user;
        this.game = game;
    }

    public ClientType getClient() {
        return client;
    }

    public UserInfo getUser() {
        return user;
    }

    public GameInfo getGame() {
        return game;
    }

    public void setClient(ClientType client) {
        this.client = client;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public void setGame(GameInfo game) {
        this.game = game;
    }
}
