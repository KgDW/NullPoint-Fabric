package me.nullpoint.socket.network.user;

import me.nullpoint.socket.network.info.record.OnlineUserInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author DreamDev
 * @since 4/10/2024
 */

public class UserManager {
    private static User user;
    private static List<OnlineUserInfo> onlineUsers = new ArrayList<>();

    public static User getUser() {
        return user;
    }

    public static void setUser(User user) {
        user = user;
    }

    public static boolean add(OnlineUserInfo onlineUserInfo) {
        return onlineUsers.add(onlineUserInfo);
    }

    public static boolean remove(OnlineUserInfo onlineUserInfo) {
        return onlineUsers.remove(onlineUserInfo);
    }

    public static List<OnlineUserInfo> getOnlineUsers() {
        return onlineUsers;
    }

    public static void setOnlineUsers(List<OnlineUserInfo> onlineUsers) {
        onlineUsers = onlineUsers;
    }

    public static List<String> getUsernames() {
        return onlineUsers.stream()
                .map(OnlineUserInfo::getUsername)
                .collect(Collectors.toList());
    }
}
