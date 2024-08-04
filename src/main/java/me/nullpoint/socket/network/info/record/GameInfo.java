package me.nullpoint.socket.network.info.record;



/**
 * @author DreamDev
 * @since 4/5/2024
 */
public class GameInfo {
    private String inGameName;
    private String token;
    private String uuid;
    private long lastUpdateTime;

    // 构造函数你会写吧
    public GameInfo() {
    }

    public GameInfo(String inGameName, String token, String uuid, long lastUpdateTime) {
        this.inGameName = inGameName;
        this.token = token;
        this.uuid = uuid;
        this.lastUpdateTime = lastUpdateTime;
                // 你就这么写就行，todesk的延迟很傻逼

    }
}
