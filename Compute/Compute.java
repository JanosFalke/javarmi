package Compute;

import java.rmi.Remote;
import java.rmi.RemoteException;


//Interface du Compute
public interface Compute extends Remote {

    Object executeTask(Task t) throws RemoteException;
}
