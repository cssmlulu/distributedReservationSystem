package transaction;

import java.rmi.*;

/** 
 * Interface for the Transaction Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface TransactionManager extends Remote {
    public boolean xidCheck(int xid)
            throws RemoteException,
            InvalidTransactionException,
            TransactionAbortedException;

    public boolean dieNow()
	       throws RemoteException;

    public void dieTMBeforeCommit()
        throws RemoteException;

    public void dieTMAfterCommit()
            throws RemoteException;

    /** The RMI name a TransactionManager binds to. */
    public static final String RMIName = "TM";

    //API for WC
    public int start()
            throws RemoteException;

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException;

    //API for RM
    public boolean enlist(int xid, ResourceManager rm)
            throws RemoteException;
}
