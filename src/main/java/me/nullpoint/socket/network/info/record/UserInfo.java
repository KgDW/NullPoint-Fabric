package me.nullpoint.socket.network.info.record;

import me.nullpoint.socket.enums.Rank;

import java.util.Date;

/**
 * @author DreamDev
 * @since 4/3/2024
 */
public class  UserInfo {
    private final String token;
    private final int userId;
    private final String username;
    private final Rank rank;
    private final Date expiryDate;
    private final double balance;

    public UserInfo(String token,int userId,String username,Rank rank,Date expiryDate,double balance){
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.rank = rank;
        this.expiryDate = expiryDate;
        this.balance=balance;
    }

    public double getBalance() {
        return balance;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

    public Rank getRank() {
        return rank;
    }

    public String getUsername() {
        return username;
    }

    public int getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }
}//(String token, int userId, String username, Rank rank, Date expiryDate, double balance)
