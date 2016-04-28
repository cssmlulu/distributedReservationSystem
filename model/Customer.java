package model;

import java.io.Serializable;
import java.util.ArrayList;

public class Customer implements Serializable{

    private String name;
    private ArrayList<Reservation> reservations;

    public Customer(String name) {
        this.name = name;
        reservations = new ArrayList<Reservation>();
    }
}
