package transaction;

import model.*;
import lockmgr.DeadlockException;
import lockmgr.LockManager;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;


public class Transaction {
    public static Database activeDB ;
    public static Database shadowDB;

    public static String DBImage1 = "data/DBImage1";
    public static String DBImage2 = "data/DBImage2";

    public static int counter;

    static {
        activeDB = new Database();
        shadowDB = new Database();
        counter = 1;
    }

    private int id;
    private LockManager lockmgr;

    public Transaction(LockManager lockmgr) {
        this.id = counter++;
        this.lockmgr = lockmgr;
    }

}