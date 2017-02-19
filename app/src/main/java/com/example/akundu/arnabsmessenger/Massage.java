package com.example.akundu.arnabsmessenger;

/**
 * Created by akundu on 26-Dec-16.
 */

public class Massage {
    String sender;
    String msg;
    String date;

    public Massage() {
    }

    public Massage(String sender, String msg, String date) {
        this.sender = sender;
        this.msg = msg;
        this.date = date;
    }

    public String getSender() {
        return sender;
    }

    public String getMsg() {
        return msg;
    }

    public String getDate() {
        return date;
    }
}
