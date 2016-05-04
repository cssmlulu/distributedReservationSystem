package transaction;

import lockmgr.*;
import java.rmi.*;

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
        
        
        public ResourceManagerImpl(String rmiName) throws RemoteException {
        	myRMIName = rmiName;

        	while (!reconnect()) {
        	    // would be better to sleep a while
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

        HashMap<Integer, Transaction> transactions;
        LockManager lockmgr;
        // TRANSACTION INTERFACE
        public int start()
                throws RemoteException {
            Transaction trans = new Transaction(lockmgr);
            transactions.put(trans.getID(), trans);
            return trans.getID();
        }

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
            try {
                transactions.get(xid).abort();
            } catch (TransactionAbortedException e) {
                e.printStackTrace();
            }
            transactions.remove(xid);
            return;
        }


        // ADMINISTRATIVE INTERFACE
        public boolean addFlight(int xid, String flightNum, int numSeats, int price)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).addResource(Database.FLIGHT_KEY(flightNum), flightNum, numSeats, price);
        }

        public boolean deleteFlight(int xid, String flightNum) 
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).deleteResource(Database.FLIGHT_KEY(flightNum));
        }

        public boolean addRooms(int xid, String location, int numRooms, int price)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).addResource(Database.HOTEL_KEY(location), location, numRooms, price);
        }

        public boolean deleteRooms(int xid, String location, int numRooms)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).subResource(Database.HOTEL_KEY(location), numRooms);
        }


        public boolean addCars(int xid, String location, int numCars, int price)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).addResource(Database.Car_KEY(location), location, numCars, price);
        }

        public boolean deleteCars(int xid, String location, int numCars)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).subResource(Database.CAR_KEY(location), numCars);
        }

        public boolean newCustomer(int xid, String custName)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).newCustomer(Database.CUSTOMER_KEY(custName), custName);
        }

        public boolean deleteCustomer(int xid, String custName)
                throws RemoteException,
                TransactionAbortedException,
                InvalidTransactionException {
            return transactions.get(xid).deleteCustomer(Database.CUSTOMER_KEY(custName));
        }
}

