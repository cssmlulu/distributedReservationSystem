package transaction;

import model.*;
import lockmgr.*;
import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;

/** 
 * Resource Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the RM
 */

public class ResourceManagerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements ResourceManager {
    
        protected String myRMIName = null; // Used to distinguish this RM from other RMs
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
        
        HashMap<Integer, Transaction> transactions;
        LockManager lockmgr;       
        public ResourceManagerImpl(String rmiName) throws RemoteException {
        	myRMIName = rmiName;
            transactions = new HashMap<Integer, Transaction>();
            lockmgr = new LockManager();

        	while (!reconnect()) {
        	    // would be better to sleep a while
                try {
                    Thread.sleep(1000);
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
        	} 
        }

        void newIdCheck(int xid) throws  RemoteException {
            if(!transactions.containsKey(xid)) {
                System.out.println("Add new xid in transaction " + this.myRMIName + " :" + xid);
                transactions.put(xid, new Transaction(xid, this.lockmgr, this.myRMIName));
                tm.enlist(xid, this);
            }
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

        public boolean dieNow() 
    	throws RemoteException {
        	System.exit(1);
        	return true; // We won't ever get here since we exited above;
        	             // but we still need it to please the compiler.
        }


        // TRANSACTION INTERFACE

        public boolean commit(int xid)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            transactions.get(xid).commit();
            transactions.remove(xid);
            return true;
        }

        public void abort(int xid)
                throws RemoteException,
                InvalidTransactionException {
            transactions.get(xid).abort();
            transactions.remove(xid);
            return;
        }


        // ADMINISTRATIVE INTERFACE
        public boolean addResource(int xid, String dbKey, String id, int size, int price)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
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
            newIdCheck(xid);
            try {
                Transaction trans = transactions.get(xid);
                return trans.reserve(dbCustName, resvType, dbResvKey);
            } catch (TransactionAbortedException e) {
                tm.abort(xid);
                throw new TransactionAbortedException(xid, "reserve error");
            } 
        }
}

