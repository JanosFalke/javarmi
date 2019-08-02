package Operations;

import Compute.Task;
import java.math.BigInteger;
/**
 *
 * @author jfalke
 */

//Implementer de l'interface Task<Boolean> (ici) pour executer methode sur une ComputeEngine
public class Nombre implements Task<Boolean> {
    private final BigInteger nombre; //Nombre à traiter
    
    //Constructeur du nombre (en entrée un BigInteger)
    public Nombre(BigInteger i){
        this.nombre = i; 
    }
    
    public BigInteger getValue(){
        return this.nombre;
    }

    //Source: 'StackOverflow' https://stackoverflow.com/questions/4407839/how-can-i-find-the-square-root-of-a-java-biginteger
    public static BigInteger sqrt(BigInteger x) {
        BigInteger div = BigInteger.ZERO.setBit(x.bitLength()/2);
        BigInteger div2 = div;
        for(;;) {
            BigInteger y = div.add(x.divide(div)).shiftRight(1);
            if (y.equals(div) || y.equals(div2))
                return y;
            div2 = div;
            div = y;
        }
    }
   

    
    //Methode de Task à executer par la ComputeEngine (ici Verification si le nombre est un nombre premier)
    //Inspiré par le TP et https://randcode.wordpress.com/2012/09/19/generating-prime-numbers-with-java-rmi/
    @Override
    public Boolean execute(boolean verbose) {
        /* nombre.compareTo(x)
            --> -1 : nombre inférieur à x
            --> 0 : nombre égal à x
            --> 1 : nombre supérieur à x
        */

        if(verbose){
            System.out.println("Verification du nombre: "+nombre);
        }
        
        //Si le nombre == 1 OU 0 --> pas de nombre premier
        if (nombre.equals(BigInteger.ONE) || nombre.equals(BigInteger.ZERO)){
            return false;
            
        //Si le nombre est plus petit que 4 --> nombre premier
        } else if (nombre.compareTo(new BigInteger("4")) == -1){
            return true;
            
        //Si nombre % 2 est 0 --> pas de nombre premier (multiples de 2)
        } else if (nombre.mod(new BigInteger("2")).equals(BigInteger.ZERO)){
            return false;
            
        //Si nombre est plus petit que 9 --> nombre premier
        } else if (nombre.compareTo(new BigInteger("9")) == -1){
            return true;
            
        //Si le nombre % 3 est 0 --> pas de nombre premier (multiples de 3)
        } else if (nombre.mod(new BigInteger("3")).equals(BigInteger.ZERO)){
            return false;
            

        //Les autres possibilités restantes
        } else {
            BigInteger r = sqrt(nombre); //racine BigInteger donc arrondi vers le bas (8.87 -> 8)
            BigInteger f = new BigInteger("5"); //Commencer à 5 car deja teste le reste plus haut
 
            //Comparer la valeur temporaire de f à la racine carré du nombre
            while (f.compareTo(r) <= 0){
                
                //Premiere fois --> multiple de 5 --> si oui donc pas premier
                //Après 11,17,23,29,35,41,47,53,59,65,71,...
                if (nombre.mod(f).equals(BigInteger.ZERO)){
                    return false;
                }
                
                //Premiere fois --> multiple de 7 --> si oui donc pas premier
                //Après 13,19,25,31,37,43,49,55,61,67,73,...
                if (nombre.mod(f.add(new BigInteger("2"))).equals(BigInteger.ZERO)){
                    return false;
                }
                
                //On ajoute 6 à f (etat inital de 5)
                f = f.add(new BigInteger("6")); //-> 6 (car multiple de 2,3,5,7 deja traité) --> 11,17,23,29,35,...
                //-> pour avoir les cas pas traités (nombres finissant par 1,3,5,7,9) 
                // => impair --> possibilité d'etre nombre premier)  
            }
        }
        return true;
    }  
}
