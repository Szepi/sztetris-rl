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
 *
 * the same as a feature user agent, but learns a perceptron in the background
 */
public class PercLearnerAgent extends FeatureUserAgent {

//    public KwikLinearLearner kwik;
//    XP lastxp;
    public PerceptronLearner pl;
    
    Random rnd = new Random();

    public PercLearnerAgent(int width, int height, FeatureExtractor fex)
    {
        super(width,height,fex);
        name = "perceptron learner agent";
        pl = new PerceptronLearner(fex);

    }

    @Override
    public void NewGame(int piece, int[][] state)
    {
        super.NewGame(piece, state);
//        xpList.clear();
    }

    @Override
    public void getObservation(double reward, int piece, int[][] state)
    {
        super.getObservation(reward, piece, state);
    }

    @Override
    public TetrisAction act()
    {
        TetrisAction a = putTileGreedy(piece);
        pl.addSample(new AfterState(agentstate, width,height), piece, a);
        return a;
    }

    @Override
    public void GameOver()
    {
//        XP xp = new XP(phi,agentstate,0);
//        xpList.add(xp);

    }


//    @Override
//    public TetrisAction putTileGreedy(int piece)
//    {
//        int rot, pos;
//        double bestvalue, value;
//        int res;
//        int i;
//        TetrisAction bestAction = new TetrisAction(0,0);
//        int bestind = -1;
//        double bestr = 0;
//        //legalActions = new Vector<TetrisAction>();
//
//        //type = rnd.nextInt(7);
//
//        //UpdateSkyline();
//        AfterState s = new AfterState(agentstate,width, height);
//        AfterState nexts;
//        bestvalue = -1e10;
//        for (rot=0; rot<2; rot++)
//        {
//            for (pos=0; pos<width; pos++)
//            {
//                //AfterState.copyBoard(agentstate, workboard);
////                System.out.println("   "+ type + ", " +rot + ", " + pos);
//                nexts = s.putTile(piece, rot, pos, false);
//                res = nexts.lastResult;
////                System.out.println("   "+ type + ", " +rot + ", " + pos + "   OK");
//                if (res>=0)
//                {
//                    value = 0;
//                    phi = fex.getFeatures(nexts.board, res); //compute phi
//                    for (i=0; i<nFeatures; i++)
//                    {
//                        value += w[i]*phi[i];
//                    }
//                    //legalActions.add(new TetrisAction(pos,rot));
//                }
//                else
//                    value = -1e10;
//                if (value>bestvalue)
//                {
//                    bestvalue = value;
//                    bestAction = new TetrisAction(pos,rot);
//                    bestr = res;
//                }
//            }
//        }
//        return bestAction;
//    }

}
