package Serveur;

import Compute.ComputeEngine;
import Compute.Compute;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 *
 * @author jfalke
 */
public class Serveur {

    public static int DEFAULT_PORT = 1099;
    public static String serverURL;   //Nom du serveur
    public static boolean verbose = true; //détail du traitement

    public static void main(String[] args) throws RemoteException, InterruptedException, UnknownHostException, MalformedURLException {
        serverURL = InetAddress.getLocalHost().getHostAddress();
        //Boucler sur les arguments passés en parametres
        for (int i = 0; i < args.length; i++) {

            //Extraire le premier character de l'argument et verifier s'il s'agit d'une option
            if (args[i].charAt(0) == '-') {
                char option = args[i].charAt(1); //extraite l'option
                switch (option) {
                    //Address du serveur
                    case 'a':
                        serverURL = args[i + 1]; //recuperer l'addresse si pas par defaut
                        break;
                    //Port du serveur
                    case 'p':
                        DEFAULT_PORT = Integer.parseInt(args[i + 1]); //recuperer le port si pas defaut
                        break;
                    //Details du traitement (verbose)
                    case 'v':
                        verbose = Boolean.parseBoolean(args[i + 1]); //recuperer le verbose si true ou false
                        break;
                }
            }
        }

        Serveur s = new Serveur(); //Création du serveur
        s.createConnection(); //Création de la connection
    }

    //Creation du serveur et son ComputeEngine dédiée
    public void createConnection() throws RemoteException, UnknownHostException, MalformedURLException {
        if (System.getSecurityManager() == null){
            System.setSecurityManager(new RMISecurityManager());
        }
        //Création du registre (un par serveur donc incrementer avec le nombre de port)
        Registry registry = LocateRegistry.createRegistry((DEFAULT_PORT));

        //Donner l'url au serveur avec son port dedié 
        String url = "rmi://" + serverURL + ":" + DEFAULT_PORT + "/Compute";

        //Création de la ComputeEngine du serveur qui va faire les operations / calculs du serveur
        Compute engine = new ComputeEngine(verbose);

        try {
            Naming.bind(url, engine); //Lier l'url à la ComputeEngine

        } catch (AlreadyBoundException ae) {
            Naming.rebind(url, engine); //Relier l'url à la ComputeEngine (si existant)
        }
        System.out.println("Listening on port: " + (DEFAULT_PORT) + " to server: " + url);

    }
}
