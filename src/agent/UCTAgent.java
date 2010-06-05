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
public class UCTAgent extends VIAgent {

    Random rnd = new Random();
    UCTtree tree;
    int gamenumber = 0;

    public UCTAgent(int width, int height, UCTtree tree, FeatureExtractor fex)
    {
        super(width,height, fex);
        name = "UCT agent";
        this.tree = tree;
    }

    @Override
    public void NewGame(int piece, int[][] state)
    {
        super.NewGame(piece, state);
        gamenumber ++;
    }

    @Override
    public void getObservation(double reward, int piece, int[][] state)
    {
        super.getObservation(reward, piece, state);
    }


    @Override
    public TetrisAction act()
    {
        XP xp = analyzeActions(piece);
        boolean echo = (gamenumber%10 == 0);
        int aind = tree.selectAction(agentstate, piece, xp, echo);
        if (aind != -1)
            xp.a = xp.actions.get(aind);
        xp.aind = aind;
        xpList.add(xp);


        aList.add(xp.a);
        return xp.a;
    }

    @Override
    public void GameOver()
    {
        XP xp = new XP(phi,agentstate,0);
        xpList.add(xp);
    }



}
