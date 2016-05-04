package model;

public class Reservation {

    private int resvType;
    private String resvKey;
    private boolean isdeleted = false;

    public int getResvType() {
        return resvType;
    }

    public String getResvKey() {
        return resvKey;
    }

    public Reservation(String reserveKey){
        this.resvKey = resvKey;
    }

    public void delete {
        this.isdeleted = true;
    }
}