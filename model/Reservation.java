package model;
import java.io.Serializable;

public class Reservation implements Serializable{
    public static final int RESVTYPE_FLIGHT = 1;
    public static final int RESVTYPE_CAR = 2;
    public static final int RESVTYPE_HOTEL = 3;

    private int resvType;
    private String resvKey;
    private boolean isdeleted = false;

    public int getResvType() {
        return resvType;
    }

    public String getResvKey() {
        return resvKey;
    }

    public Reservation(int resvType, String reserveKey){
        this.resvType = resvType;
        this.resvKey = resvKey;
    }

    public void delete() {
        this.isdeleted = true;
    }
}