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
                lockmgr.unlockAll(this.id);
                return false;
            }
            resource.setAvail(newAvail);
            updates.put(key, resource);
        } catch (DeadlockException e) {
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
            throw new TransactionAbortedException(this.id,"delete resource failed");
        }
        return true;    
    }

    public boolean newCustomer(String key, String id) throws TransactionAbortedException {
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            updates.put(key, new Customer(id));
        } catch (DeadlockException e) {
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
            throw new TransactionAbortedException(this.id,"delete customer failed");
        }
        return true;   
    }
}