package Diviseur;

import Compute.Compute;
import Operations.Nombre;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.RMISecurityManager;
import java.rmi.registry.Registry;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


/**
 *
 * @author jfalke
 */
public class Diviseur implements Runnable {

    public static int DEFAULT_PORT = 1099;
    public int id;                  //Identifiant du diviseur/thread
    public static int nbDiviseurs = 1;         //Nombre de diviseurs/threads totale
    public String URL;
    public Connection connection;   //Connexion à la base de données
    public static BigInteger ENTIERS_BEGIN = new BigInteger("1"); //premier des entiers a traiter
    public static BigInteger ENTIERS_END = new BigInteger("100"); //dernier des entiers a traiter
    public static Boolean verbose = true; //affichage detailee ou pas

    //Constructeur pour un Thread de Diviseur
    public Diviseur(int id, Connection connection, String adress) {
        this.id = id; //Identifiant du diviseur/thread
        this.connection = connection; //Connexion à la base de données
        this.URL = adress;
    }

    @Override
    public void run() {
        try {
            //Mettre en place un RMISecurityManager --> obligé d'avoir un java.policy pour les permissions
            if (System.getSecurityManager() == null){
                System.setSecurityManager(new RMISecurityManager());
            }
            
            //Recuperer le bon registre/port du serveur/ComputeEngine dédiée
            LocateRegistry.getRegistry(this.URL.split(":")[1]);
            
            //Recuperer le lien du thread avec son identifant pour pouvoir
            //se connecter à celui du ComputeEngine equivalent
            String url = "rmi://" + this.URL + "/Compute";
            System.out.println("Diviseur/Thread "+this.id+" connecte sur "+this.URL+" (Port: "+(this.URL.split(":")[1])+")");
           
            
            Compute comp = (Compute) Naming.lookup(url); //Se connecter au ComputeEngine/Serveur correspondant
                        
            Statement statement = connection.createStatement(); //créer 

            //Traitement des nombres premiers du thread correspondant
            System.out.println("Debut d'insertion (Diviseur/Thread " + this.id + ")");
            int count = 0; //Nombre totale des nombre premiers inserees par le thread
            int countTraite = 0; //Nombre totale des nombres traitees par le thread
            
            //Methode de 'BigInteger for' découvert et appliqué avec de l'aide de: https://stackoverflow.com/questions/3024186/java-how-for-loop-work-in-the-case-of-biginteger
            //On commence à l'id du diviseur/thread et on incremente avec le nombre de diviseur/threads totale
            for (BigInteger bi = ENTIERS_BEGIN.add(BigInteger.valueOf(this.id)); 
                    bi.compareTo(ENTIERS_END) <= 0; 
                    bi = bi.add(BigInteger.valueOf(nbDiviseurs))) {
               
                Nombre nombre = new Nombre(bi); //Création du nombre premier
                if(verbose)
                    System.out.println("\t(Diviseur/Thread " + this.id + ") -Traite le nombre: " + nombre.getValue());

                
                //ComputeEngine s'occupe du calcul et renvoie le resultat au diviseur
                //Prime implemente Task, donc on pourra l'envoyer à la Compute(Engine) qui fera le calcul pour le Diviseur
                Boolean isPrime = (Boolean) comp.executeTask(nombre);

                
                //Insértion du résultat (si c'est un nombre premier) dans la base de données 
                if(isPrime == true){
                    statement.executeUpdate("INSERT INTO diviseurs (nombre_premier)" + "VALUES(" + nombre.getValue() + ")");
                    if(verbose)
                        System.out.println("\t(Diviseur/Thread " + this.id + ") -Insert nombre_premier: " + nombre.getValue() + " dans la table 'diviseurs'");
                    count++;
                }
                countTraite++;
            }
            System.out.println("Diviseur/Thread "+this.id+" > Nombres premiers insere: " +count+ " || Nombres traites: "+countTraite);
            System.out.println("Fin d'insertion (Diviseur/Thread " + this.id + ")");

        } catch (RemoteException | MalformedURLException | NotBoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    
    
    //////////////////Methodes statiques//////////////////////
    
    //Methode pour creer une base de données avec le nom passé en parametre (ou celui par defaut)
    public static Connection creerBaseDeDonnees(String login, String motDePasse, String nomBaseDeDonnees) throws SQLException{
        //creation de la connexion vers postgres pour creer une base de données
        Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/", login, motDePasse);
         
        try {
            Statement statement;
            statement = connection.createStatement();
            
            System.out.println("Creation de la base de donnees "+nomBaseDeDonnees);
            statement.executeUpdate("DROP DATABASE IF EXISTS \""+nomBaseDeDonnees+"\""); //supprimer la base si elle existe
            statement.executeUpdate("CREATE DATABASE \""+nomBaseDeDonnees+"\" WITH OWNER = postgres"); //creer la base de données
            connection.close(); //fermer la connection
        } catch (SQLException e) {
            e.printStackTrace();
        }
        
        //Connexion à la base de données créée 
        System.out.println("Connexion a la base de donnees "+nomBaseDeDonnees);
        connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/"+nomBaseDeDonnees, login, motDePasse);

        
        return connection; //retourner la connexion
    }
    
    
    //Methode pour se connecter à une base de données postgres
    public static Connection connecterBaseDeDonneesPostgres(String login, String motDePasse, String nomBaseDeDonnees){
        Connection connection = null; //Initialiser la connexion à null
         
        try {
            Class.forName("org.postgresql.Driver"); //Recuperation du driver
        } catch (ClassNotFoundException e) {
            System.out.println("Impossible de trouver le driver");
            e.printStackTrace();
            return null;
        }
        try {
            //Creer la base de données avec les données saisies
            connection = Diviseur.creerBaseDeDonnees(login, motDePasse, nomBaseDeDonnees);
            
            //Si on ne veut pas supprimer et recréer la base de données il faut executer cette ligne et mettre celle au dessus en commentaire
            //connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1:5432/"+nomBaseDeDonnees, login, motDePasse);
        } catch (SQLException e) { 
            System.out.println("\t-> Connexion failed");
            e.printStackTrace();
            return null;
        }
        if (connection != null) {
            System.out.println("\t-> Connexion reussie !");
        } else {
            System.out.println("\t-> Connexion failed");
        }
         
        return connection; //retourner la connexion (null ou reussi)
    }

    
    //Methode pour la creation de la table diviseur dans la base de données postgres
    public static void creerTableDiviseur(Connection connection) {
        //Création de la table diviseurs dans la base de données
        try {
            Statement statement;
            statement = connection.createStatement();
            System.out.println("Creation de la table 'diviseurs' (nombre_premier BIGINT)");
            statement.executeUpdate("DROP TABLE IF EXISTS diviseurs");
            statement.executeUpdate("CREATE TABLE diviseurs ( nombre_premier BIGINT );");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    //Methode de la création des diviseurs / threads
    public static ArrayList<Diviseur> createDiviseur(Connection connection, ArrayList<String> adresses) {
        ArrayList<Diviseur> diviseurs = new ArrayList<>();
        for (int i = 0; i < nbDiviseurs; i++) {
            Diviseur d = new Diviseur(i, connection, adresses.get(i));
            diviseurs.add(d);
        }

        return diviseurs;
    }

    
    
    
    public static void main(String[] args) throws InterruptedException, Exception {
       
        //Informations de connexion par défaut
        String login = "postgres"; //utilisateur de la base de données
        String motDePasse = "pgAdmin"; //mot de Passe de la base de données
        String nomBaseDeDonnees = "TEAArchi"; //nom de la base de données --> va etre creee
        
        ArrayList<String> adresses = new ArrayList<>(); 
        
        //Boucler sur les arguments passés en parametres
        for (int i = 0; i < args.length; i++) {
            
            //Extraire le premier character de l'argument et verifier s'il s'agit d'une option
            if(args[i].charAt(0) == '-'){
                char option = args[i].charAt(1); //extraite l'option
                switch(option){
                    //Login de postgres
                    case 'l':
                        login = args[i+1]; //Mettre le login à jour (-l)
                        break;
                    //Mot de passe de postgres
                    case 'p':
                        motDePasse = args[i+1]; //Mettre le mot de passe à jour (-m)
                        break;
                    //Nom de la base de données
                    case 'n': 
                        nomBaseDeDonnees = args[i+1]; //Mettre le nom de la bd à jour (-n)
                        break;
                    //Nombre de diviseurs (si on precise pas d'adresses)
                    case 't':
                        nbDiviseurs = Integer.parseInt(args[i+1]); //Mettre le nombre de diviseurs

                        //Par defaut on ajoute des adresses si c'est vide
                        if(adresses.isEmpty()){
                            for (int j = 0; j < nbDiviseurs; j++) {
                                adresses.add(InetAddress.getLocalHost().getHostAddress()+":"+(DEFAULT_PORT+j));
                            }
                        }
                        break;
                    //Début des entiers
                    case 'b':
                        ENTIERS_BEGIN = new BigInteger(args[i+1]); //Mettre le debut des entiers a traiter à jour (-b)
                        break;
                    //Fin des entiers
                    case 'e':
                        ENTIERS_END = new BigInteger(args[i+1]); //Mettre la fin des entiers a traiter à jour (-e)
                        break;
                    //verbose -> details
                    case 'v':
                        verbose = Boolean.valueOf(args[i+1]); //Mettre l'affichage de detail à jour (-v)
                        break;
                    //Addresses et ports des diviseurs
                    case 'a':
                        //Supprimer la valeur par defaut:
                        adresses.clear();
                        nbDiviseurs = 0; //remettre à 0
                        
                        //pour un intervalle d'adresse
                        if(args[i+1].charAt(0) == '{'){
                            String[] tempAdresses = args[i+1].split(";");  
                            //On va ajouter tous les adresses 
                            for (int j = 0; j < tempAdresses.length; j++) {
                                if(j == 0){
                                    adresses.add(tempAdresses[j].substring(1));
                                } else if(j == tempAdresses.length-1){
                                    adresses.add(tempAdresses[j].substring(0, tempAdresses[j].length()-1)); 
                                } else {
                                    adresses.add(tempAdresses[j]);
                                }
                                nbDiviseurs++;
                            }
                        //Si on a qu'une seule adresse
                        } else {
                            adresses.add(args[i+1]);
                            nbDiviseurs++;
                        }  
                        break;
                }
            }
        }
        
        //par defaut s'il ny a pas d'adresses ou nombre de diviseurs precise
        if(adresses.isEmpty()){
            adresses.add(InetAddress.getLocalHost().getHostAddress()+":"+DEFAULT_PORT); //par defaut sans arguments
        }
        
        //Création de la connexion
        Connection connection = Diviseur.connecterBaseDeDonneesPostgres(login, motDePasse, nomBaseDeDonnees);
        
        //Verifier l'existance de la connexion
        if(connection == null){
            throw new Exception("ERREUR: La connexion ne pouvait pas etre etabli!");
        }
        
        //Creation de la table 'diviseurs' dans Postgres 
        Diviseur.creerTableDiviseur(connection); 
        
        ArrayList<Diviseur> diviseurs = Diviseur.createDiviseur(connection, adresses); //Creations des Diviseurs (Threads)
        
        if(ENTIERS_BEGIN.compareTo(ENTIERS_END) == 1){
            throw new Exception("ERREUR: Impossible de calculer les valeurs entre "+ENTIERS_BEGIN+" et "+ENTIERS_END+" (debut plus grand que la fin)!!!");
        }
        
        
        System.out.println("\n********** Debut du traitement des nombres demandes (de "+ENTIERS_BEGIN+" a "+ENTIERS_END+")**********");
        
        long startTime = System.currentTimeMillis(); 

        ArrayList<Thread> threads = new ArrayList<>();
        
        //Ajouter les diviseurs 
        for (Diviseur d : diviseurs) {
            threads.add(new Thread(d));
        }
        
        //Lancer les threads (diviseurs)
        for (Thread t : threads) {
            t.start();
        }
        
        //Attendre la fin des threads (diviseurs)
        for (Thread t : threads) {
            t.join();
        }

        
        long endTime = System.currentTimeMillis();
        long duration = (endTime - startTime);
        
        System.out.println("\n********** Fin du traitement des nombres demandes **********");
    
        System.out.println("Temps d'execution: "+(float)duration/1000+" secs");
    }
}
