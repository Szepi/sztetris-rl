/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Random;
import java.util.Vector;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public class VIAgent extends FeatureUserAgent {

    public Vector<TetrisAction> aList = new Vector<TetrisAction>(); //obsolete
    public Vector<Double> rList = new Vector<Double>(); //obsolete
    public KwikLinearLearner kwik;
    public double VMAX = 1000.0;
    public double ALPHA = 0.001;
    //Vector<TetrisAction> legalActions;
    XP lastxp;
    
    Random rnd = new Random();

    public VIAgent(int width, int height, FeatureExtractor fex)
    {
        super(width,height,fex);
        name = "Value iteration agent";
        kwik = new KwikLinearLearner(fex.nFeatures, ALPHA);

    }

    @Override
    public void NewGame(int piece, int[][] state)
    {
        super.NewGame(piece, state);
        xpList.clear();
        aList.clear();
        rList.clear();
        tr2 = 0;
    }

    @Override
    public void getObservation(double reward, int piece, int[][] state)
    {
        super.getObservation(reward, piece, state);
    }

    double tr2= 0;
    @Override
    public TetrisAction act()
    {
        TetrisAction a = putTileGreedy(piece);
        tr2 += lastxp.r;
//        if (!kwik.isKnown(phi) && lastxp.actions.size()>0)
//        {
//            //System.out.println(lastxp.a.size());
//            int i = rnd.nextInt(lastxp.actions.size());
//            a = lastxp.actions.get(i);
//        }
        aList.add(a);
        //System.out.println(a.pos + ", " + a.rot);
        return a;
    }

    @Override
    public void GameOver()
    {
        XP xp = new XP(phi,agentstate,0);
        xpList.add(xp);

//        double tr = 0;
//        for (int i=0; i<rList.size(); i++)
//            tr+=rList.get(i);
//        System.out.println("\ntr = "+ tr + " " + tr2);
    }


    @Override
    public TetrisAction putTileGreedy(int piece)
    {
        int rot, pos;
        double bestvalue, value;
        int res;
        int i;
        TetrisAction bestAction = new TetrisAction(0,0);
        int bestind = -1;
        double bestr = 0;
        //legalActions = new Vector<TetrisAction>();

        //type = rnd.nextInt(7);

        XP xp = new XP(phi,agentstate, piece);

        //UpdateSkyline();
        AfterState s = new AfterState(agentstate,width, height);
        AfterState nexts;
        bestvalue = -1e10;
        for (rot=0; rot<4; rot++)
        {
            for (pos=0; pos<width; pos++)
            {
                //AfterState.copyBoard(agentstate, workboard);
//                System.out.println("   "+ type + ", " +rot + ", " + pos);
                nexts = s.putTile(piece, rot, pos, false);
                res = nexts.lastResult;
//                System.out.println("   "+ type + ", " +rot + ", " + pos + "   OK");
                if (res>=0)
                {
                    value = 0;
                    phi = fex.getFeatures(nexts.board, res); //compute phi
                    for (i=0; i<nFeatures; i++)
                    {
                        value += w[i]*phi[i];
                    }
                    xp.addNext(res, new TetrisAction(pos,rot), phi);
                    //legalActions.add(new TetrisAction(pos,rot));
                }
                else
                    value = -1e10;
                if (value>bestvalue)
                {
                    bestvalue = value;
                    bestAction = new TetrisAction(pos,rot);
                    bestind = xp.actions.size()-1;
                    bestr = res;
                }
            }
        }
        xp.a = bestAction;
        xp.aind = bestind;
        lastxp = xp;
        if (bestind != -1)
        {
            rList.add(xp.ra.get(bestind));
            xp.r = bestr;
        }
        else
            rList.add(0.0);
        xpList.add(xp);
        return bestAction;
    }

}
