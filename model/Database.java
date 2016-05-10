package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Database implements Serializable {
    static final String FLIGHT_PREFIX= "Flight.";
    static final String CAR_PREFIX = "Car.";
    static final String HOTEL_PREFIX = "Hotel.";
    static final String CUSTOMER_PREFIX = "Customer.";
    static final String RESERVATION_PREFIX = "Reservation.";

    // public enum DatabaseResource { FLIGHT, CAR, HOTEL, RESERVATION };

    private HashMap<String, Object> db;
    public static int dbCnt = 1;

    public static String FLIGHT_KEY(String key) {
        return FLIGHT_PREFIX + key;
    }
    public static String CAR_KEY(String key) {
        return CAR_PREFIX + key;
    }
    public static String HOTEL_KEY(String key) {
        return HOTEL_PREFIX + key;
    }
    public static String CUSTOMER_KEY(String key) {
        return CUSTOMER_PREFIX + key;
    }
    public static String RESERVATION_KEY(String key) {
        return RESERVATION_PREFIX + key;
    }

    public Database() {
        db = new HashMap<String, Object>();
    }

    public void put(String key, Object value) {
        db.put(key, value);
    }

    public Object get(String key) {
        return db.get(key);
    }

    public void delete(String key) {
        db.remove(key);
    }

    public boolean containsKey(String key) {
        return db.containsKey(key);
    }

    public String toString() {
        String rst = "";
        for (String key : db.keySet()) {
            rst += key;
            rst += ",";
        }
        return rst;
    }
}