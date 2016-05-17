package transaction;

import java.rmi.*;

/** 
 * Interface for the Resource Manager of the Distributed Travel
 * Reservation System.
 * <p>
 * Unlike WorkflowController.java, you are supposed to make changes
 * to this file.
 */

public interface ResourceManager extends Remote {
    public String getMyRMIName()
            throws RemoteException;
            
    public boolean reconnect() 
	throws RemoteException;

    public boolean dieNow() 
	throws RemoteException;

    public void dieRMAfterEnlist()
            throws RemoteException;
    public void dieRMBeforePrepare()
            throws RemoteException;
    public void dieRMAfterPrepare()
            throws RemoteException;
    public void dieRMBeforeCommit()
            throws RemoteException;
    public void dieRMBeforeAbort()
            throws RemoteException;

    //API for WC
    // TRANSACTION INTERFACE
    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException;
            
    // ADMINISTRATIVE INTERFACE
    public boolean addResource(int xid, String dbKey, String id, int size, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean subResource(int xid, String dbKey, int subNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean deleteResource(int xid, String dbKey) 
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean newCustomer(int xid, String dbCustName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean deleteCustomer(int xid, String dbCustName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    // QUERY INTERFACE
    public int queryAvail(int xid, String dbKey)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryPrice(int xid, String dbKey)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;


    public int queryCustomerBill(int xid, String dbCustName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    // RESERVATION INTERFACE
    public boolean reserve(int xid, String dbCustName, int resvType, String dbResvKey)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    /** The RMI names a ResourceManager binds to. */
    public static final String RMINameFlights = "RMFlights";
    public static final String RMINameRooms = "RMRooms";
    public static final String RMINameCars = "RMCars";
    public static final String RMINameCustomers = "RMCustomers";
}
