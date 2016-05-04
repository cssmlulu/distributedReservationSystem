package model;

import java.io.Serializable;

public class Resource implements Serializable{
    private String id; //flight number or location
    private int price;
    private int size;
    private int avail;
    private boolean isdeleted;

    public Resource(String id, int price, int size) {
        this.id = id;
        this.price = price;
        this.size = size;
        this.avail = size;
        this.isdeleted = false;
    }

    public Resource(Resource rs){
        this.id = rs.id;
        this.price = rs.price;
        this.size = rs.size;
        this.avail = rs.avail;
        this.isdeleted = rs.isdeleted;
    }

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

    public boolean isDelete() {
        return isdeleted;
    }

    public void delete() {
        isdeleted = true;
    }

    //positive value to add, negative value to minus
    public void update(int newPrice, int sizeChange) {
        this.price = newPrice;
        this.size += sizeChange;
        this.avail += sizeChange;
    }
}