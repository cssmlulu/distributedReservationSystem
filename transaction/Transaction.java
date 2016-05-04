package transaction;

import model.*;
import lockmgr.DeadlockException;
import lockmgr.LockManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Transaction {

    //Each DBfile has 2 copies on the disk.
    //And the pointer point to the latest modified copy
    static String DBImage0 = "data/DBImage0";
    static String DBImage1 = "data/DBImage1";
    public static String DBPointer = DBImage1;
    static LockManager lockmgr;

    static String switchDB() {
        if(DBPointer == DBImage0) 
            return DBImage1;
        else
            return DBImage0;
    }

    //the latest modified copy also stored in memory
    public static Database activeDB;
    // public static Database shadowDB;

    public static int counter;

    static {
        activeDB = new Database();
        // shadowDB = new Database();
        counter = 1;
    }

    private int id;
    public int getID() {
        return id;
    }
    //Each transction has its own update list
    // which stores the latest value of the entry
    private HashMap<String, Object> updates;

    public Transaction(LockManager lockmgr) {
        this.id = counter++;
        this.lockmgr = lockmgr;
    }

    public void abort() {
        lockmgr.unlockAll(this.id);
    }

    public boolean commit() {
        for (String key : updates.keySet()) {
            activeDB.put(key, updates.get(key));
        }

        DBPointer = switchDB();
        ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(DBPointer));
        fout.writeObject(activeDB);
        fout.close();

        lockmgr.unlockAll(this.id);
        return true;
    }

    public Object readObj(String key) {
        Object obj = null;
        if (updates.containKey(key))
            obj = updates.get(key);
        else if (activeDB.containKey(key)) 
            obj = activeDB.get(key);
        return obj;
    }

    public boolean addResource(String key, String id, int size, int price) throws TransactionAbortedException {
        Resource resource = (Resource)readObj(key);
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            if(resource) { //update existing DB entries
                resource.update(newPrice=price, sizeChange=size);
            }
            else { // add a new DB entry
                resource = new Resource(id, price, size); //add new DB entry
            }
            updates.put(key, resource);
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"add resource failed");
        }
        return true;
    }

    public boolean subResource(String key, int subNum) throws TransactionAbortedException {
        Resource resource = (Resource)readObj(key);
        if(!resource)
            return false;
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            int newAvail = resource.getAvail() - subNum;
            if (newAvail < 0) {
                throw new TransactionAbortedException(this.id, "insufficient resources")
            }
            resource.setAvail(newAvail);
            updates.put(key, resource);
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"substract resource failed");
        }
        return true;
    }
    
    //TODO: Should fail if a customer has a reservation on this key
    public boolean deleteResource(String key) throws TransactionAbortedException {
        Resource resource = (Resource)readObj(key);
        if(!resource)
            return false;  
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            resource.delete(); //set isdeleted to be true
            updates.put(key, resource);
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"delete resource failed");
        }
        return true;    
    }

    public boolean newCustomer(String key, String id) throws TransactionAbortedException {
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            updates.put(key, new Customer(id));
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"add new customer failed");
        }
        return true;    
    }

    public boolean deleteCustomer(String key) throws TransactionAbortedException {
        Customer customer = (Customer)readObj(key);
        if(!customer) 
            return false;
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            customer.delete();
            updates.put(key, resource);
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"delete customer failed");
        }
        return true;   
    }

    public int queryPrice(String key) throws TransactionAbortedException {
        Resource resource = (Resource)readObj(key);
        try {
            lockmgr.lock(this.id, key, LockManager.READ);
            if(!resource) {
                return -1;
            }
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"query price failed");
        }
        return resource.getPrice();
    }

    public int queryAvail(String key) throws TransactionAbortedException {
        Resource resource = (Resource)readObj(key);
        try {
            lockmgr.lock(this.id, key, LockManager.READ);
            if(!resource) {
                return -1;
            }
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"query price failed");
        }
        return resource.getAvail();
    }

    public boolean reserve(String custKey, int resvType, String resvKey) throws TransactionAbortedException {
        try {
            lockmgr.lock(this.id, resvKey, LockManager.WRITE);
            lockmgr.lock(this.id, resvKey, LockManager.WRITE);
            
            if(!readObj(resvKey)) //resource doesn't exist
                return false;

            ArrayList<Reservation> reservations = (ArrayList<Reservation>)readObj(custKey);
            if(!reservations) 
                reservations = new ArrayList<Reservation>();

            Resource resource = (Resource)readObj(resvKey);
            int newAvail = resource.getAvail() - 1;
            if(newAvail < 0) //check available num
                return false;
            resource.setAvail(newAvail);

            reservations.add(new Reservation(resvType, resvKey));
            updates.put(custKey, reservations);
            updates.put(resvKey, resource);

        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"reservation failed");
        }
        return true;
    }
}