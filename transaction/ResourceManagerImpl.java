package transaction;

import model.*;
import lockmgr.*;
import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.io.*;

/** 
 * Resource Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the RM
 */

public class ResourceManagerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements ResourceManager {
    
        protected String myRMIName = null; // Used to distinguish this RM from other RMs
        public String getMyRMIName()
                throws RemoteException {
            return myRMIName;
        }

        protected TransactionManager tm = null;

        public static void main(String args[]) {
        	System.setSecurityManager(new RMISecurityManager());

        	String rmiName = System.getProperty("rmiName");
        	if (rmiName == null || rmiName.equals("")) {
        	    System.err.println("No RMI name given");
        	    System.exit(1);
        	}

        	String rmiPort = System.getProperty("rmiPort");
        	if (rmiPort == null) {
        	    rmiPort = "";
        	} else if (!rmiPort.equals("")) {
        	    rmiPort = "//:" + rmiPort + "/";
        	}

        	try {
        	    ResourceManagerImpl obj = new ResourceManagerImpl(rmiName);
        	    Naming.rebind(rmiPort + rmiName, obj);
        	    System.out.println(rmiName + " bound");
        	} 
        	catch (Exception e) {
        	    System.err.println(rmiName + " not bound:" + e);
        	    System.exit(1);
        	}
        }

        protected boolean dieRMAfterEnlist;
        protected boolean dieRMBeforePrepare;
        protected boolean dieRMAfterPrepare;
        protected boolean dieRMBeforeCommit;
        protected boolean dieRMBeforeAbort;
        
        private HashMap<Integer, Transaction> transactions;
        private LockManager lockmgr;   
        private String transFilePath;    
        public ResourceManagerImpl(String rmiName) throws RemoteException {
        	myRMIName = rmiName;
            transactions = new HashMap<Integer, Transaction>();
            lockmgr = new LockManager();
            Transaction.dbType = myRMIName;
            Transaction.recover();

            dieRMAfterEnlist = false;
            dieRMBeforePrepare = false;
            dieRMAfterPrepare = false;
            dieRMBeforeCommit = false;
            dieRMBeforeAbort = false;

        	while (!reconnect()) {
        	    // would be better to sleep a while
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
        	}
            transFilePath =  "data/trans." + myRMIName;
            loadTransactions();
            printTransactions();
        }

        public boolean reconnect()
    	throws RemoteException {
        	String rmiPort = System.getProperty("rmiPort");
        	if (rmiPort == null) {
        	    rmiPort = "";
        	} else if (!rmiPort.equals("")) {
        	    rmiPort = "//:" + rmiPort + "/";
        	}

        	try {
        	    tm = (TransactionManager)Naming.lookup(rmiPort + TransactionManager.RMIName);
        	    System.out.println(myRMIName + " bound to TM");
        	} 
        	catch (Exception e) {
        	    System.err.println(myRMIName + " cannot bind to TM:" + e);
        	    return false;
        	}

        	return true;
        }


        void xidCheck(int xid) throws  InvalidTransactionException, RemoteException,
            TransactionAbortedException {
            if(!transactions.containsKey(xid)) {
                if(tm.xidCheck(xid)) {
                    System.out.println("Add new xid in transaction " + this.myRMIName + " :" + xid);
                    transactions.put(xid, new Transaction(xid, this.lockmgr));
                    tm.enlist(xid, this);
                    if (dieRMAfterEnlist)
                        dieNow();
                }
                else {
                    throw new InvalidTransactionException(xid, "xid is invalid in TM");
                }
            }
        }

        private void storeTransactions() {
            try {
                    // String transFilePath = "data/trans." + myRMIName;
                    ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(transFilePath));
                    Set keySet = new HashSet(transactions.keySet());
                    fout.writeObject(keySet);
                    fout.close();
                }  catch (IOException e) {
                    e.printStackTrace();
                }
        }

        private void loadTransactions() {
            // String transFilePath = "data/trans." + myRMIName;
            File transFile = new File(transFilePath);
            if(!transFile.exists())
                return;
            try {
                ObjectInputStream fin = new ObjectInputStream(new FileInputStream(transFilePath));
                Set<Integer> transKeys = (Set<Integer>)fin.readObject();
                fin.close();
                for(Integer xid: transKeys) {
                    Transaction trans = new Transaction(xid, lockmgr);
                    trans.loadUpdateList();
                    transactions.put(xid,trans);
                    tm.enlist(xid, this);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        // TRANSACTION INTERFACE

        public boolean commit(int xid)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            if (dieRMBeforeCommit)
                dieNow();
            if (!transactions.containsKey(xid)) {
                System.out.println(getMyRMIName() + " died before.");
                throw new TransactionAbortedException(xid, getMyRMIName() + " died before.");
            }
            else {
                System.out.println("Commit: Current xid: "+xid);
            }
            transactions.get(xid).commit();
            transactions.remove(xid);
            storeTransactions();
            return true;
        }

        public void abort(int xid)
                throws RemoteException,
                InvalidTransactionException {
            if (dieRMBeforeAbort)
                dieNow();
            transactions.get(xid).abort();
            transactions.remove(xid);
            storeTransactions();
            return;
        }


        // ADMINISTRATIVE INTERFACE
        public boolean addResource(int xid, String dbKey, String id, int size, int price)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).addResource(dbKey,id,size,price);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "addResource error");
            }
        }

        public boolean deleteResource(int xid, String dbKey) 
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).deleteResource(dbKey);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "deleteResource error");
            }
        }

        public boolean subResource(int xid, String dbKey, int subNum)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).subResource(dbKey, subNum);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "subResource error");
            }
        }


        public boolean newCustomer(int xid, String dbCustName)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).newCustomer(dbCustName);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "newCustomer error");
            }
            
        }

        public boolean deleteCustomer(int xid, String dbCustName)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).deleteCustomer(dbCustName);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "deleteCustomer error");
            }
        }

        // QUERY INTERFACE
        public int queryAvail(int xid, String dbKey)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).queryAvail(dbKey);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "queryAvail error");
            }            
        }

        public int queryPrice(int xid, String dbKey)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                return transactions.get(xid).queryPrice(dbKey);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "queryPrice error");
            }            
            
        }

        public int queryCustomerBill(int xid, String dbCustName)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            ArrayList<Reservation> reservations = (ArrayList<Reservation>)Transaction.activeDB.get(dbCustName);
            if (reservations == null)
                return 0;
            int sum = 0;
            for (Reservation r : reservations){
                Resource rsc = (Resource) Transaction.activeDB.get(r.getResvKey());
                sum += rsc.getPrice();
            }
            return sum;
        }

        // RESERVATION INTERFACE
        public boolean reserve(int xid, String dbCustName, int resvType, String dbResvKey)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            xidCheck(xid);
            try {
                Transaction trans = transactions.get(xid);
                return trans.reserve(dbCustName, resvType, dbResvKey);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "reserve error");
            } 
        }

        public boolean dieNow() 
        throws RemoteException {
            System.exit(1);
            return true; // We won't ever get here since we exited above;
                         // but we still need it to please the compiler.
        }

        public void dieRMAfterEnlist()
                throws RemoteException {
            dieRMAfterEnlist = true;
        }
        public void dieRMBeforePrepare()
                throws RemoteException {
            dieRMBeforePrepare = true;
        }
        public void dieRMAfterPrepare()
                throws RemoteException {
            dieRMAfterPrepare = true;
        }
        public void dieRMBeforeCommit()
                throws RemoteException {
            dieRMBeforeCommit = true;
        }
        public void dieRMBeforeAbort()
                throws RemoteException {
            dieRMBeforeAbort = true;
        }

        public boolean recvPrepare(int xid)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            if(dieRMBeforePrepare)
                dieNow();
            boolean isPrepared = transactions.get(xid).prepare();
            storeTransactions();
            if(dieRMAfterPrepare)
                dieNow();


            if(isPrepared) {
                //TOOD: write prepared to log
                return true;
            }
            else {
                //TODO: write abort to log
                transactions.get(xid).abort();
                return false;
            }
        }

        public boolean recvCommit(int xid)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            //TODO: write commit to log
            return commit(xid);
        }

        public void recvAbort(int xid)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            //TODO: write abort to log
            abort(xid);
        }

        //print current transactions in this RM
        public void printTransactions() throws RemoteException {
            System.out.print("Current transactions in " + getMyRMIName() + " :");
            for (Integer xid : transactions.keySet()) {
                System.out.print(" " + xid + ",");
            }
            System.out.print("\n");
        }
}

