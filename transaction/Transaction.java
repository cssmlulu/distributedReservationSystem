package transaction;

import model.*;
import lockmgr.DeadlockException;
import lockmgr.LockManager;
import transaction.TransactionAbortedException;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Transaction {

    //Each DBfile has 2 copies on the disk.
    //And the pointer point to the latest modified copy
    static String DBImage0 = "data/DBImage0.";
    static String DBImage1 = "data/DBImage1.";
    public static String DBPointer = "data/DBPointer.";
    public static String UpdateList = "data/UpdateList.";
    static LockManager lockmgr;
    public static String dbType;

    //the latest modified copy also stored in memory
    public static Database activeDB;
    public static String activeFile;
    public static String shadowFile;

    private int id;
    public int getID() {
        return id;
    }

    public static String getPath(String path) {
        return path + dbType;
    }

    public static void recover() {
        File dbFile = new File(getPath(DBPointer));
        if (dbFile.exists()) {
            loadFromFile();
        }
        else {            
            new File("data").mkdir();
            activeDB = new Database();
            activeFile = DBImage0;
            shadowFile = DBImage1;
        }
    }
    //Each transction has its own update list
    // which stores the latest value of the entry
    private HashMap<String, Object> updates;

    public Transaction(int xid, LockManager lockmgr) {
        this.id = xid;
        Transaction.lockmgr = lockmgr;
        updates = new HashMap<String, Object>();
        status = Participant_Status.Initiated;
    }

    public static void loadFromFile() {
        try {
            ObjectInputStream fin = new ObjectInputStream(new FileInputStream(getPath(DBPointer)));
            activeFile = (String)fin.readObject();
            shadowFile = (String)fin.readObject();
            activeDB = (Database) (new ObjectInputStream(new FileInputStream(getPath(activeFile)))).readObject();
            fin.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void storeDB() {
        try {
            ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(getPath(shadowFile)));
            fout.writeObject(activeDB);
            fout.close();

            String tmpFile = activeFile;
            activeFile = shadowFile;
            shadowFile = tmpFile;

            fout = new ObjectOutputStream(new FileOutputStream(getPath(DBPointer)));
            fout.writeObject(activeFile);
            fout.writeObject(shadowFile);
            fout.close();

            fout = new ObjectOutputStream(new FileOutputStream(getPath(shadowFile)));
            fout.writeObject(activeDB);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void storeUpdateList() {
        try {
                String updateListPath = getPath(UpdateList) + "." + this.id;
                ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(updateListPath));
                fout.writeObject(updates);
                fout.close();
            }  catch (IOException e) {
                e.printStackTrace();
            }
    }

    enum Participant_Status {
        Initiated, Prepared, Aborted, Committed
    }
    private Participant_Status status;

    public boolean prepare() {
        if(status == Participant_Status.Initiated) {
            storeUpdateList();
            status = Participant_Status.Prepared;
            return true;
        }
        else {
            status = Participant_Status.Aborted;
            return false;
        }
    }

    public void abort() {
        System.out.println("Before abort: " + activeDB.toString());
        lockmgr.unlockAll(this.id);
        updates.clear();
        recover();
        System.out.println("After abort: " + activeDB.toString());
        status = Participant_Status.Aborted;
    }

    public boolean commit() {
        if(status != Participant_Status.Prepared)
            return false;

        System.out.println("Before commit: " + activeDB.toString());
        for (String key : updates.keySet()) {
            activeDB.put(key, updates.get(key));
        }   

        storeDB();

        lockmgr.unlockAll(this.id);
        updates.clear();
        System.out.println("After commit: " + activeDB.toString());
        status = Participant_Status.Committed;
        return true;
    }

    public Object readObj(String key) {
        Object obj = null;
        if (updates.containsKey(key))
            obj = updates.get(key);
        else if (activeDB.containsKey(key)) 
            obj = activeDB.get(key);
        return obj;
    }

    public boolean addResource(String key, String id, int size, int price) throws TransactionAbortedException {
        Resource resource = (Resource)readObj(key);
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            if(resource != null) { //update existing DB entries
                resource.update(price, size);
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
        if(resource == null)
            return false;
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            int newAvail = resource.getAvail() - subNum;
            if (newAvail < 0) {
                throw new TransactionAbortedException(this.id, "insufficient resources");
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
        if(resource == null)
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

    public boolean newCustomer(String key) throws TransactionAbortedException {
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            updates.put(key, new Customer(key));
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"add new customer failed");
        }
        return true;    
    }

    public boolean deleteCustomer(String key) throws TransactionAbortedException {
        Customer customer = (Customer)readObj(key);
        if(customer == null) 
            return false;
        try {
            lockmgr.lock(this.id, key, LockManager.WRITE);
            customer.delete();
            updates.put(key, customer);
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
            if(resource == null) {
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
            if(resource == null) {
                return -1;
            }
        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"query avail failed");
        }
        return resource.getAvail();
    }

    public boolean reserve(String custKey, int resvType, String resvKey) throws TransactionAbortedException {
        try {
            lockmgr.lock(this.id, custKey, LockManager.WRITE);
            lockmgr.lock(this.id, resvKey, LockManager.WRITE);
            
            if(readObj(resvKey) == null) //resource doesn't exist
                return false;

            ArrayList<Reservation> reservations = (ArrayList<Reservation>)readObj(custKey);
            if(reservations == null) 
                reservations = new ArrayList<Reservation>();

            Resource resource = (Resource)readObj(resvKey);
            int newAvail = resource.getAvail() - 1;
            if(newAvail < 0) //check available num
                return false;
            resource.setAvail(newAvail);

            reservations.add(new Reservation(resvType, resvKey));
            updates.put(custKey, reservations);
            updates.put(resvKey, resource);
            System.out.println(custKey + " reserve " + resvKey);

        } catch (DeadlockException e) {
            lockmgr.unlockAll(this.id);
            throw new TransactionAbortedException(this.id,"reservation failed");
        }
        return true;
    }
}