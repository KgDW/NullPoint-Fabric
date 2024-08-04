package me.nullpoint.socket.network.user;

import me.nullpoint.socket.enums.Rank;

import java.util.Date;

/**
 * @author DreamDev
 * @since 4/10/2024
 */
public class User {
    private int userId;
    private String username;
    private String password;
    private String token;
    private static Rank rank;
    private Date expireDate;
    private double balance;
    private String avatarData;

    public User(int userId, String username, String password, String token, Rank rank, Date expireDate, double balance, String avatarData) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.token = token;
        this.rank = rank;
        this.expireDate = expireDate;
        this.balance = balance;
        this.avatarData = avatarData;
    }

    public User(int userId, String username, String password, String token, Rank rank, Date expireDate, double balance) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.token = token;
        this.rank = rank;
        this.expireDate = expireDate;
        this.balance = balance;
        this.avatarData = "";
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public Rank getRank() {
        return rank;
    }

    public void setRank(Rank rank) {
        this.rank = rank;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public String getAvatarData() {
        return avatarData;
    }

    public void setAvatarData(String avatarData) {
        this.avatarData = avatarData;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
