package model;

import java.io.Serializable;

public class Resource implements Serializable{
    private String id;
    private int price;
    private int size;
    private int avail;

    public String getId() {
        return id;
    }

    public int getPrice() {
        return price;
    }

    public int getSize() {
        return size;
    }

    public int getAvail() {
        return avail;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setAvail(int avail) {
        this.avail = avail;
    }

    public Resource(String id, int price, int size) {
        this.id = id;
        this.price = price;
        this.size = size;
        this.avail = size;
    }

    public Resource(Resource rs){
        this.id = rs.id;
        this.price = rs.price;
        this.size = rs.size;
        this.avail = rs.avail;
    }

}