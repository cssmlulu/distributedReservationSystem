package transaction;

import model.*;
import lockmgr.*;
import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;


/** 
 * Transaction Manager for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the TM
 */

public class TransactionManagerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements TransactionManager {

    public static int idCounter=1;
    
    public static void main(String args[]) {
	System.setSecurityManager(new RMISecurityManager());

	String rmiPort = System.getProperty("rmiPort");
	if (rmiPort == null) {
	    rmiPort = "";
	} else if (!rmiPort.equals("")) {
	    rmiPort = "//:" + rmiPort + "/";
	}

	try {
	    TransactionManagerImpl obj = new TransactionManagerImpl();
	    Naming.rebind(rmiPort + TransactionManager.RMIName, obj);
	    System.out.println("TM bound");
	} 
	catch (Exception e) {
	    System.err.println("TM not bound:" + e);
	    System.exit(1);
	}
    }
    
    HashMap<Integer, Set<ResourceManager>> enlistList;
    public TransactionManagerImpl() throws RemoteException {
        enlistList = new HashMap<Integer, Set<ResourceManager>>();
    }

    public boolean dieNow() 
	       throws RemoteException {
	   System.exit(1);
	   return true; // We won't ever get here since we exited above;
	             // but we still need it to please the compiler.
    }

    public int start()
            throws RemoteException {
        enlistList.put(idCounter, new HashSet<ResourceManager>());
        return idCounter++;
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        Set<ResourceManager> commits = enlistList.get(xid);
        for (ResourceManager rm : commits) {
            if (!rm.commit(xid))
                return false;
        }
        return true;
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {
        Set<ResourceManager> aborts= enlistList.get(xid);
        for (ResourceManager rm : aborts) {
            rm.abort(xid);
        }
    }

    public boolean enlist(int xid, ResourceManager rm)
            throws RemoteException {
        if (enlistList.containsKey(xid)) {
            enlistList.get(xid).add(rm);
        }
        else {
            Set<ResourceManager> rmSet = new HashSet<ResourceManager>();
            rmSet.add(rm);
            enlistList.put(xid, rmSet);
        }
        return true;
    }
}
