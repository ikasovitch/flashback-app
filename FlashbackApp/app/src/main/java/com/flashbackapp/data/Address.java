package com.flashbackapp.data;

public class Address {
    public String streetName;
    public int houseNumber;
    public String cityName;
    public float longitude;
    public float latitude;

    public Address () {
        // don't delete this.
    }

    public Address(String address, float longitude, float latitude) {
        String[] parts = address.split(",");
        if (parts.length != 3) {
             throw new IllegalArgumentException("Invalid address format");
        }
        this.cityName = parts[0];
        this.streetName = parts[1];
        try {
            this.houseNumber = Integer.parseInt(parts[2]);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException(String.format("Invalid house number: %s", parts[2]));
        }

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String combineAddressParts() {
        return String.format("%s, %s, %s", this.cityName, this.streetName, this.houseNumber);
    }
}
