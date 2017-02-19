package com.example.akundu.arnabsmessenger;

/**
 * Created by akundu on 26-Dec-16.
 */

public class Friend {
    String name;
    String address;
    String u_id;
    String online;

    public Friend() {
    }

   /* public Friend(String name, String address, String u_id) {
        this.name = name;
        this.address = address;
        this.u_id = u_id;
    }*/


    public Friend(String name, String address, String u_id, String online) {

        this.name = name;
        this.address = address;
        this.u_id = u_id;
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getU_id() {
        return u_id;
    }

    public String getOnline() {
        return online;
    }

}
