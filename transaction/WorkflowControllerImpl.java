package transaction;

import model.*;
import java.rmi.*;
import java.util.*;

/** 
 * Workflow Controller for the Distributed Travel Reservation System.
 * 
 * Description: toy implementation of the WC.  In the real
 * implementation, the WC should forward calls to either RM or TM,
 * instead of doing the things itself.
 */

public class WorkflowControllerImpl
    extends java.rmi.server.UnicastRemoteObject
    implements WorkflowController {
    
    protected ResourceManager rmFlights = null;
    protected ResourceManager rmRooms = null;
    protected ResourceManager rmCars = null;
    protected ResourceManager rmCustomers = null;
    protected TransactionManager tm = null;

    public static void main(String args[]) {
    	System.setSecurityManager(new RMISecurityManager());

    	String rmiPort = System.getProperty("rmiPort");
    	if (rmiPort == null) {
    	    rmiPort = "";
    	} else if (!rmiPort.equals("")) {
    	    rmiPort = "//:" + rmiPort + "/";
    	}

    	try {
    	    WorkflowControllerImpl obj = new WorkflowControllerImpl();
    	    Naming.rebind(rmiPort + WorkflowController.RMIName, obj);
    	    System.out.println("WC bound");
    	}
    	catch (Exception e) {
    	    System.err.println("WC not bound:" + e);
    	    System.exit(1);
    	}
    }
    
    
    public WorkflowControllerImpl() throws RemoteException {
        idSet = new HashSet<Integer>();
    	while (!reconnect()) {
    	    // would be better to sleep a while
            try {
                Thread.sleep(1000);
            } catch(InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
    	} 
    }

    protected Set<Integer> idSet = null;
    void checkExist(int xid) throws InvalidTransactionException {
        if (!idSet.contains(xid)){
            throw  new InvalidTransactionException(xid, "transaction does not exits");
        }
    }

    // TRANSACTION INTERFACE
    public int start() throws RemoteException {
	   int xid = tm.start();
       idSet.add(xid);
       return xid;
    }

    public boolean commit(int xid)
	throws RemoteException, 
	       TransactionAbortedException, 
	       InvalidTransactionException {
        checkExist(xid);
        System.out.println("WC send commit to tm: " + xid);
	    return tm.commit(xid);
    }

    public void abort(int xid)
	throws RemoteException, 
               InvalidTransactionException {
        checkExist(xid);
	    tm.abort(xid);
    }


    // ADMINISTRATIVE INTERFACE
    public boolean addFlight(int xid, String flightNum, int numSeats, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmFlights.addResource(xid, Database.FLIGHT_KEY(flightNum), flightNum, numSeats, price);
    }

    public boolean deleteFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
    	return rmFlights.deleteResource(xid, Database.FLIGHT_KEY(flightNum));
    }
		
    public boolean addRooms(int xid, String location, int numRooms, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmRooms.addResource(xid, Database.ROOM_KEY(location), location, numRooms, price);
    }

    public boolean deleteRooms(int xid, String location, int numRooms) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmRooms.subResource(xid, Database.ROOM_KEY(location), numRooms);
    }

    public boolean addCars(int xid, String location, int numCars, int price) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCars.addResource(xid, Database.CAR_KEY(location), location, numCars, price);
    }

    public boolean deleteCars(int xid, String location, int numCars) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCars.subResource(xid, Database.CAR_KEY(location), numCars);
    }

    public boolean newCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCustomers.newCustomer(xid, Database.CUSTOMER_KEY(custName));
    }

    public boolean deleteCustomer(int xid, String custName) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCustomers.deleteCustomer(xid, Database.CUSTOMER_KEY(custName));   
    }


    // QUERY INTERFACE
    public int queryFlight(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
	    return rmFlights.queryAvail(xid, Database.FLIGHT_KEY(flightNum));
    }

    public int queryFlightPrice(int xid, String flightNum)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmFlights.queryPrice(xid, Database.FLIGHT_KEY(flightNum));
    }

    public int queryRooms(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmRooms.queryAvail(xid, Database.ROOM_KEY(location));
    }

    public int queryRoomsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmRooms.queryPrice(xid, Database.ROOM_KEY(location));
    }

    public int queryCars(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCars.queryAvail(xid, Database.CAR_KEY(location));
    }

    public int queryCarsPrice(int xid, String location)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCars.queryPrice(xid, Database.CAR_KEY(location));
    }

    public int queryCustomerBill(int xid, String custName)
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        int sum = rmRooms.queryCustomerBill(xid, Database.CUSTOMER_KEY(custName)) + rmCars.queryCustomerBill(xid, Database.CUSTOMER_KEY(custName)) + rmFlights.queryCustomerBill(xid, Database.CUSTOMER_KEY(custName));
        return sum;
    }


    // RESERVATION INTERFACE
    public boolean reserveFlight(int xid, String custName, String flightNum) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmFlights.reserve(xid, Database.CUSTOMER_KEY(custName), Reservation.RESVTYPE_FLIGHT, Database.FLIGHT_KEY(flightNum));
    }
 
    public boolean reserveCar(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmCars.reserve(xid, Database.CUSTOMER_KEY(custName), Reservation.RESVTYPE_CAR, Database.CAR_KEY(location));
    }

    public boolean reserveRoom(int xid, String custName, String location) 
	throws RemoteException, 
	       TransactionAbortedException,
	       InvalidTransactionException {
        checkExist(xid);
        return rmRooms.reserve(xid, Database.CUSTOMER_KEY(custName), Reservation.RESVTYPE_ROOM, Database.ROOM_KEY(location));
    }

    public boolean reserveItinerary(int xid, String custName, List flightNumList, String location, boolean needCar, boolean needRoom)
    throws RemoteException,
           TransactionAbortedException,
           InvalidTransactionException {
        checkExist(xid);
        for (String flightNum : (List<String>)flightNumList) {
            if(queryFlight(xid, flightNum) < 1)
                return false;
        }
        if (needCar & (queryCars(xid, location) < 1))
            return false;
        if (needRoom & (queryRooms(xid, location) < 1))
            return false;
        
        boolean result = true;
        for (String flightNum : (List<String>)flightNumList)
            result &= reserveFlight(xid, custName, flightNum);
        if (needCar)
            result &= reserveCar(xid, custName, location);
        if (needRoom)
            result &= reserveRoom(xid, custName, location);
        return result;
    }

    // TECHNICAL/TESTING INTERFACE
    public boolean reconnect()
	throws RemoteException {
	String rmiPort = System.getProperty("rmiPort");
	if (rmiPort == null) {
	    rmiPort = "";
	} else if (!rmiPort.equals("")) {
	    rmiPort = "//:" + rmiPort + "/";
	}

	try {
	    rmFlights =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameFlights);
	    System.out.println("WC bound to RMFlights");
	    rmRooms =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameRooms);
	    System.out.println("WC bound to RMRooms");
	    rmCars =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameCars);
	    System.out.println("WC bound to RMCars");
	    rmCustomers =
		(ResourceManager)Naming.lookup(rmiPort +
					       ResourceManager.RMINameCustomers);
	    System.out.println("WC bound to RMCustomers");
	    tm =
		(TransactionManager)Naming.lookup(rmiPort +
						  TransactionManager.RMIName);
	    System.out.println("WC bound to TM");
	} 
	catch (Exception e) {
	    System.err.println("WC cannot bind to some component:" + e);
	    return false;
	}

	try {
	    if (rmFlights.reconnect() && rmRooms.reconnect() &&
		rmCars.reconnect() && rmCustomers.reconnect()) {
		return true;
	    }
	} catch (Exception e) {
	    System.err.println("Some RM cannot reconnect:" + e);
	    return false;
	}

	return false;
    }

    public boolean dieNow(String who)
	throws RemoteException {
	if (who.equals(TransactionManager.RMIName) ||
	    who.equals("ALL")) {
	    try {
		tm.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameFlights) ||
	    who.equals("ALL")) {
	    try {
		rmFlights.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameRooms) ||
	    who.equals("ALL")) {
	    try {
		rmRooms.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameCars) ||
	    who.equals("ALL")) {
	    try {
		rmCars.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(ResourceManager.RMINameCustomers) ||
	    who.equals("ALL")) {
	    try {
		rmCustomers.dieNow();
	    } catch (RemoteException e) {}
	}
	if (who.equals(WorkflowController.RMIName) ||
	    who.equals("ALL")) {
	    System.exit(1);
	}
	return true;
    }
    public boolean dieRMAfterEnlist(String who)
            throws RemoteException {
        if (who.equals(ResourceManager.RMINameFlights)) {
            try {
                rmFlights.dieRMAfterEnlist();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameRooms)) {
            try {
                rmRooms.dieRMAfterEnlist();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCars)) {
            try {
                rmCars.dieRMAfterEnlist();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCustomers)) {
            try {
                rmCustomers.dieRMAfterEnlist();
            } catch (RemoteException e) {}
        }
        return true;
    }
    public boolean dieRMBeforePrepare(String who)
            throws RemoteException {
        if (who.equals(ResourceManager.RMINameFlights)) {
            try {
                rmFlights.dieRMBeforePrepare();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameRooms)) {
            try {
                rmRooms.dieRMBeforePrepare();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCars)) {
            try {
                rmCars.dieRMBeforePrepare();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCustomers)) {
            try {
                rmCustomers.dieRMBeforePrepare();
            } catch (RemoteException e) {}
        }
        return true;
    }
    public boolean dieRMAfterPrepare(String who)
            throws RemoteException {
        if (who.equals(ResourceManager.RMINameFlights)) {
            try {
                rmFlights.dieRMAfterPrepare();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameRooms)) {
            try {
                rmRooms.dieRMAfterPrepare();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCars)) {
            try {
                rmCars.dieRMAfterPrepare();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCustomers)) {
            try {
                rmCustomers.dieRMAfterPrepare();
            } catch (RemoteException e) {}
        }
        return true;
    }
    public boolean dieTMBeforeCommit()
            throws RemoteException {
        tm.dieTMBeforeCommit();
        return true;
    }
    public boolean dieTMAfterCommit()
            throws RemoteException {
        tm.dieTMAfterCommit();
        return true;
    }
    public boolean dieRMBeforeCommit(String who)
            throws RemoteException {
        if (who.equals(ResourceManager.RMINameFlights)) {
            try {
                rmFlights.dieRMBeforeCommit();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameRooms)) {
            try {
                rmRooms.dieRMBeforeCommit();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCars)) {
            try {
                rmCars.dieRMBeforeCommit();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCustomers)) {
            try {
                rmCustomers.dieRMBeforeCommit();
            } catch (RemoteException e) {}
        }
        return true;
    }
    public boolean dieRMBeforeAbort(String who)
            throws RemoteException {
        if (who.equals(ResourceManager.RMINameFlights)) {
            try {
                rmFlights.dieRMBeforeAbort();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameRooms)) {
            try {
                rmRooms.dieRMBeforeAbort();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCars)) {
            try {
                rmCars.dieRMBeforeAbort();
            } catch (RemoteException e) {}
        }
        if (who.equals(ResourceManager.RMINameCustomers)) {
            try {
                rmCustomers.dieRMBeforeAbort();
            } catch (RemoteException e) {}
        }
        return true;
    }
}
