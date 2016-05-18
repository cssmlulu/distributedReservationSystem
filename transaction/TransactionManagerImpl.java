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

    public static int xidCounter=1;
    
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

    enum Coordinator_Status {
        Initiated, Prepared, Aborted, Committed
    }

    protected boolean dieTMBeforeCommit;
    protected boolean dieTMAfterCommit;
    
    private HashMap<Integer, Set<ResourceManager>> enlistList;
    private HashMap<Integer, Coordinator_Status> transactionStatus;
    public boolean xidCheck(int xid)
            throws RemoteException,
            InvalidTransactionException {
        if (enlistList.containsKey(xid)){
            return true;
        }
        return false;
    }
    public TransactionManagerImpl() throws RemoteException {
        enlistList = new HashMap<Integer, Set<ResourceManager>>();
        transactionStatus = new HashMap<Integer, Coordinator_Status>();
        dieTMBeforeCommit = false;
        dieTMAfterCommit = false;
    }



    public int start()
            throws RemoteException {
        enlistList.put(xidCounter, new HashSet<ResourceManager>());
        transactionStatus.put(xidCounter, Coordinator_Status.Initiated);
        return xidCounter++;
    }

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException {
        System.out.println("TM start commit: " + xid);
        if(!xidCheck(xid) || (transactionStatus.get(xid) != Coordinator_Status.Initiated))
            throw new TransactionAbortedException(xid, "invalid xid when commit in TM");

        Set<ResourceManager> commits = enlistList.get(xid);

        for (ResourceManager rm : commits) {
            try {
                System.out.println("TM send prepare " + xid + " to " + rm.getMyRMIName());
                if (!rm.recvPrepare(xid)) {
                    //TODO: wrtie abort to log
                    abort(xid);
                    transactionStatus.put(xid, Coordinator_Status.Aborted);
                    return false;
                }
            } catch (Exception e) {
                commits.remove(rm);
                enlistList.put(xid,commits);
                abort(xid);
                throw new TransactionAbortedException(xid, "invalid RM when prepare");
            }
        }
        transactionStatus.put(xid, Coordinator_Status.Prepared);
        

        if (dieTMBeforeCommit)
            dieNow();

        for (ResourceManager rm : commits) {
            try {
                System.out.println("TM send commit " + xid + " to " + rm.getMyRMIName());
                if (!rm.recvCommit(xid))
                    return false;
            } catch (Exception e) {
                commits.remove(rm);
                enlistList.put(xid,commits);
                abort(xid);
                throw new TransactionAbortedException(xid, "invalid RM when commit");
            }
        }
        transactionStatus.put(xid, Coordinator_Status.Committed);

        if (dieTMAfterCommit)
            dieNow();

        return true;
    }

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException {
        Set<ResourceManager> aborts= enlistList.get(xid);
        for (ResourceManager rm : aborts) {
            try {
                rm.recvAbort(xid);
            } catch (Exception e) {
                aborts.remove(rm);
                enlistList.put(xid,aborts);
                System.out.println("TM abort has invalid RM. Removed in enlistList.");
            }
        }
        enlistList.remove(xid);
        transactionStatus.put(xid, Coordinator_Status.Aborted);
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

    public boolean dieNow() 
           throws RemoteException {
       System.exit(1);
       return true; // We won't ever get here since we exited above;
                 // but we still need it to please the compiler.
    }

    public void dieTMBeforeCommit()
            throws RemoteException {
        dieTMBeforeCommit = true;
    }

    public void dieTMAfterCommit()
            throws RemoteException {
        dieTMAfterCommit = true;
    }

}
