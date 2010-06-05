/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Vector;
import tetrisengine.TetrisAction;


/**
 *
 * @author szityu
 */
public class UCTtreeNode {
    int MAXNACTIONS = 40;
    int nvisits;
    double[] values;
    int nactions;
    int[] nactionvisits;
    int timeStamp;
    Vector<TetrisAction> actions;
    
    public UCTtreeNode(Vector<TetrisAction> possibilities)
    {
        nvisits = 0;
        nactions = possibilities.size();
        values = new double[MAXNACTIONS];
        nactionvisits = new int [MAXNACTIONS];
        actions =  (Vector<TetrisAction>) possibilities.clone();
    }
    
    // addVisit
    
    // selectAction
}
