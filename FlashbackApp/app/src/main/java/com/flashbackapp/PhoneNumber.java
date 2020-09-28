package com.flashbackapp;


import java.io.Serializable;

class PhoneNumber implements Serializable {
    private String name;
    private String number;
    private boolean is_primary;
    private boolean active;

    public PhoneNumber(String name, String phone_number, boolean is_primary) {
        this.name= name;
        this.number = phone_number;
        this.is_primary= is_primary;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoneNumber() {
        return this.number;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        if (this.is_primary) {
            return "Primary: " + this.name +" ("+ this.number+")";
        }
        return this.name +" ("+ this.number+")";
    }

}