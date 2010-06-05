/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;
import javax.management.RuntimeErrorException;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author szityu
 */
public class UCTtree  {
    
    public Hashtable<String,UCTtreeNode> tree;
    
    public static final int MAXNTRACES = 20000;
    int ntraces;
    Trace[] traceList;
    Random rnd = new Random();
    
    public UCTtree()
    {
        tree = new Hashtable(10000000);
        ntraces = 0;
        traceList = new Trace[MAXNTRACES];
        for (int i=0; i<MAXNTRACES; i++)
            traceList[i] = new Trace();
    }
    
//    public static long getHashCode(int[][] b)
//    {
//
//        return(Hash.hash(b));
//
//    }
    
//    public void addState(int[][] b, Vector<TetrisAction> possibilities)
//    {
//        addState(getHashCode(b),possibilities);
//    }
    
    public boolean addState(int[][] b, int piece, Vector<TetrisAction> possibilities)
    {
        int i;
        boolean added = false;

        String sh = Hash.stringhash(b, piece);
        if (!tree.containsKey(sh))
        {
            UCTtreeNode val = new UCTtreeNode(possibilities);
            tree.put(sh, val);
            added = true;
        }
        else
        {
            if (tree.get(sh).nactions != possibilities.size())
            {
                throw new RuntimeErrorException(new Error(),"Collision");
            }
        }
        return added;
    }
    
//    public UCTtreeNode getNode(int[][] b)
//    {
//        long hc = getHashCode(b);
//        return getNode(hc);
//    }

    public UCTtreeNode getNode(int[][] b, int piece)
    {
        UCTtreeNode res = (UCTtreeNode) tree.get(Hash.stringhash(b, piece));
        return res;
    }
    
    public void clearTraces()
    {
        ntraces = 0;
    }
    
    public void addTrace(int[][] b, int piece, int aind)
    {
        if (ntraces >= MAXNTRACES)
            return;
        traceList[ntraces].set(b, piece, aind);
        ntraces++;
    }
    
    public void update(double value, int timeStamp)
    {
        int i;
        UCTtreeNode node;
        Trace tr;
        
        for (i=0; i<ntraces; i++)
        {
            tr = traceList[i];
            node = getNode(tr.b, tr.piece);
            node.nvisits++;
            if (tr.aind !=-1)
            {
                //System.out.println("node:"+tr.hc);
                node.values[tr.aind]+= value;
                node.nactionvisits[tr.aind]++;
            }
            node.timeStamp = timeStamp;
            
            tree.put(Hash.stringhash(tr.b, tr.piece), node);
        }

    }
        
    public static final int MINVISITS = 10;
    public static final double C0 = 10.0;
    public static final double MAXVAL = 20.0;
    
     
    public int selectAction(int[][] b, int piece, XP xp, boolean echo)
    {
        int k;
        double v, maxv;
        int maxind=0;
        UCTtreeNode node = getNode(b, piece);
        
        maxv = 0.0;
        if (xp.actions.size()==0)
            return -1;
        if (node==null || node.nactions==0)
        {
            //System.out.print("rnd ");
            int i = rnd.nextInt(xp.actions.size());
            return i;
        }
        if (node.nactions != xp.actions.size())
        {
            throw new RuntimeErrorException(new Error(),"Gebasz van");
        }
        if (node.nvisits < MINVISITS)
        {
            //return node.actions.get(rnd.nextInt(node.nactions));
            return rnd.nextInt(node.nactions);
        }
        for (k=0; k<node.nactions; k++)
        {
            if (node.nactionvisits[k]==0)
            {
                v = MAXVAL;
                if (echo)
                {
                    System.out.printf("%2d (%d): \t %5.2f ", k, node.nactionvisits[k], v);
                    //v = 0.0;
                }
            }
            else
            {
                v = (node.values[k])/(node.nactionvisits[k]) +
                        C0*Math.sqrt(Math.log(node.nactions)/node.nactionvisits[k]);
                if (echo)
                {
                    System.out.printf("\n%2d (%d): \t %5.2f = %5.2f + %5.2f", k, node.nactionvisits[k], v,
                            ((double)node.values[k])/(node.nactionvisits[k]), C0*Math.sqrt(Math.log(node.nactions)/node.nactionvisits[k]));
                    //v = ((double)node.values[k])/(node.nactionvisits[k]);
                }
            }
            if (maxv<v)
            {
                maxv = v;
                maxind = k;
            }
        }
        TetrisAction bestaction =  node.actions.get(maxind);
        if (echo)
        {
            System.out.printf("\nsel:%d (%f) [p:%d,r:%d]\n\n", maxind, maxv, bestaction.pos, bestaction.rot);
        }
        return maxind;
        
    }
    
    @Override
    public String toString()
    {
        String s = getClass().getName() + ": " + tree.size() + "\n";
        UCTtreeNode node;
        
//        Enumeration e = tree.keys();
//        while( e.hasMoreElements() )
//        {
//            hc = (Integer) e.nextElement();
//            node = (UCTtreeNode) tree.get(hc);
//            s += String.format("%X/%d: %3d", hc, node.nactions, node.nvisits);
//            for (int i=0; i<node.nactions; i++)
//            {
//                s+= String.format(" %d [%f]", node.nactionvisits[i],
//                        node.values[i]);
//            }
//            s+= "\n";
//
//        }
        return s;
    }
}

class Trace {
    public int[][] b;
    public int piece;
    public int aind;
    
    public void set(int[][] b1, int piece, int aind)
    {
        this.piece = piece;
        //b = TetrisGame.copyBoardwitAlloc(b1);
        b = b1;
        this.aind = aind;
    }
}

