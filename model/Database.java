package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class Database implements Serializable {
    static final String FLIGHT_PREFIX= "Flight.";
    static final String CAR_PREFIX = "Car.";
    static final String ROOM_PREFIX = "Room.";
    static final String CUSTOMER_PREFIX = "Customer.";
    static final String RESERVATION_PREFIX = "Reservation.";

    // public enum DatabaseResource { FLIGHT, CAR, ROOM, RESERVATION };

    private HashMap<String, Object> dbEntries;

    public static String FLIGHT_KEY(String key) {
        return FLIGHT_PREFIX + key;
    }
    public static String CAR_KEY(String key) {
        return CAR_PREFIX + key;
    }
    public static String ROOM_KEY(String key) {
        return ROOM_PREFIX + key;
    }
    public static String CUSTOMER_KEY(String key) {
        return CUSTOMER_PREFIX + key;
    }
    public static String RESERVATION_KEY(String key) {
        return RESERVATION_PREFIX + key;
    }

    public Database() {
        dbEntries = new HashMap<String, Object>();
    }

    public Database(Database db){
        dbEntries = new HashMap<String, Object>();
        if (db == null)
            return;
        for (String key : db.dbEntries.keySet())
            dbEntries.put(key, dbEntries.get(key));
    }

    public void put(String key, Object value) {
        dbEntries.put(key, value);
    }

    public Object get(String key) {
        return dbEntries.get(key);
    }

    public void delete(String key) {
        dbEntries.remove(key);
    }

    public boolean containsKey(String key) {
        return dbEntries.containsKey(key);
    }

    public String toString() {
        String rst = "";
        for (String key : dbEntries.keySet()) {
            rst += key;
            rst += ",";
        }
        return rst;
    }
}