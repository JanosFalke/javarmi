package Compute;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
/**
 *
 * @author jfalke
 */

//Classe qui va faire les calculs (implemente l'interface Compute --> methode executeTask)
public class ComputeEngine extends UnicastRemoteObject implements Compute{
    boolean verbose; //Detail de traitement du serveur / des calculs
    
    public ComputeEngine(boolean verbose) throws RemoteException{
        super();
        this.verbose = verbose; //Detail de traitement du serveur / des calculs
    }
    
    @Override
    public Object executeTask(Task t) throws RemoteException {
        return t.execute(this.verbose); //appeler la methode execute du Task passé en parametre (ici Prime)
    }
    
}
