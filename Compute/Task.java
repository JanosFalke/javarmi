package Compute;

import java.io.Serializable;

/**
 *
 * @author jfalke
 */

//L'interface des Task<T> (T remplacable par tous les types) qui va être serialisable
public interface Task<T> extends Serializable{
    T execute(boolean verbose); //Besoin de passer un boolean pour afficher ou pas les details des traitements
}
