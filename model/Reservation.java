package model;

public class Reservation {

    private int resvType;
    private String resvKey;

    public int getResvType() {
        return resvType;
    }

    public String getResvKey() {
        return resvKey;
    }

    public Reservation(String reserveKey){
        this.resvKey = resvKey;
    }
}