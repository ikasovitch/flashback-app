package com.flashbackapp.data;

import java.io.Serializable;

public class AddressObject implements Serializable {
    private String address;
    private boolean active;

    public AddressObject(String address) {
        this.address = address;
    }

    public String getAddress() {
        return this.address;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.address;
    }

}
