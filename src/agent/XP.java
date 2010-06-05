/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Vector;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public class XP implements Serializable {
    public double[] phi;
    //public long hc;
    transient public int[][] b;
    public int piece;
    public TetrisAction a;
    public int aind;
    public double r;
    public double mcvalue; //used by MC policy eval

    public Vector<Double> ra;
    public Vector<TetrisAction> actions;
    public Vector<double[]> phinext;
    public boolean isTerminal = true;


    public XP(double[] phi, int[][] b1, int piece)
    {
        this.phi = Arrays.copyOf(phi, phi.length);
//        this.hc = hc;
        this.piece = piece;
        ra = new Vector<Double>();
        actions = new Vector<TetrisAction>();
        phinext = new Vector<double[]>();
        a =new TetrisAction(0,0);
        aind = -1;
        b = AfterState.copyBoardwithAlloc(b1);
    }

    public void addNext(double r1, TetrisAction a1, double[] phinext1)
    {
        ra.add(r1);
        actions.add(a1);
        phinext.add(Arrays.copyOf(phinext1, phinext1.length));
        isTerminal = false;
    }

    public double getVal(double[] w)
    {
        double val = 0.0;
        double maxval = -1e10;

        double[] phinext1;

        if (ra.size()==0)
            return val;
        for (int i=0; i<ra.size(); i++)
        {
            val = ra.get(i);
            //val = 1;
            phinext1 = phinext.get(i);
            for (int j=0; j<w.length; j++)
            {
                val += phinext1[j]*w[j];
            }
            if (val>maxval)
            {
                maxval = val;
            }
        }
        return maxval;
    }

    public double getPolicyVal(double[] w)
    {
        double val = 0.0;
        double[] phinext1;

        if (aind == -1)
            return val;
        val = ra.get(aind);
        phinext1 = phinext.get(aind);
        for (int j=0; j<phinext1.length; j++)
        {
            val += phinext1[j]*w[j];
        }
        return val;
    }

}
