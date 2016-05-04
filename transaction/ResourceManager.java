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

    public boolean reconnect() 
	throws RemoteException;

    public boolean dieNow() 
	throws RemoteException;

    //API for WC
    // TRANSACTION INTERFACE
    public int start()
            throws RemoteException;

    public boolean commit(int xid)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public void abort(int xid)
            throws RemoteException,
            InvalidTransactionException;
            
    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean deleteFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean addRooms(int xid, String location, int numRooms, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean deleteRooms(int xid, String location, int numRooms)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean addCars(int xid, String location, int numCars, int price)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean deleteCars(int xid, String location, int numCars)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean newCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean deleteCustomer(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryFlightPrice(int xid, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryRooms(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryRoomsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryCars(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryCarsPrice(int xid, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public int queryCustomerBill(int xid, String custName)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean reserveCar(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    public boolean reserveRoom(int xid, String custName, String location)
            throws RemoteException,
            TransactionAbortedException,
            InvalidTransactionException;

    /** The RMI names a ResourceManager binds to. */
    public static final String RMINameFlights = "RMFlights";
    public static final String RMINameRooms = "RMRooms";
    public static final String RMINameCars = "RMCars";
    public static final String RMINameCustomers = "RMCustomers";
}
