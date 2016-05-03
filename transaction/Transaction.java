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
    public static String DBImage0 = "data/DBImage0";
    public static String DBImage1 = "data/DBImage1";
    public static String DBPointer = DBImage1;
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
        shadowDB = new Database();
        counter = 1;
    }

    private int id;
    private LockManager lockmgr;
    //Each transction has its own update list
    // which stores the latest value of the entry
    private HashMap<String, Object> updates;

    public Transaction(LockManager lockmgr) {
        this.id = counter++;
        this.lockmgr = lockmgr;
    }

    public Object readObj(String key) {
        Object obj = null;
        if (updates.containKey(key))
            obj = updates.get(key);
        else if (activeDB.isContain(key)) 
            obj = activeDB.get(key);
        return obj;
    }

    public boolean addResourse(String key, String id, int size, int price) throws TransactionAbortedException {
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            Resourse resourse = readObj(key);
            if(resourse) { //update existing DB entries
                resourse.update(newPrice=price, sizeChange=size);
            }
            else {
                resourse = new Resourse(id, price, size); //add new DB entry
            }
            updates.put(key, resourse);
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"add resourse failed");
        }
        return true;
    }

    }
}