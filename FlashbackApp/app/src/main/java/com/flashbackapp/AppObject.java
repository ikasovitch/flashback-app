package com.flashbackapp;

import java.io.Serializable;

public class AppObject implements Serializable {
    private String app_name;
    private String app_address;
    private boolean active;

    public AppObject(String name, String address) {
        this.app_name = name;
        this.app_address = address;
    }

    public String getAddress() {
        return this.app_address;
    }
    public String getName() {
        return this.app_name;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.app_name +" ("+ this.app_address+")";
    }
}
