/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Vector;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public abstract class TetrisAgent {
    public String name;
    public int width, height;
    public int[][] agentstate;
    public int skyline[];

    public Vector<XP> xpList = new Vector<XP>();

    public TetrisAgent(int width, int height)
    {
        this.height = height;
        this.width = width;
        agentstate = new int[width + 2*TetrisGame.PADDING][height + 2*TetrisGame.PADDING];
        skyline = new int[width + 2*TetrisGame.PADDING];
    }

    public void NewGame(int piece, int[][] state)
    {
        AfterState.copyBoard(state, agentstate);
    }

    public void getObservation(double reward, int piece, int[][] state)
    {
        AfterState.copyBoard(state, agentstate);
    }

    public abstract TetrisAction act();

    public abstract void GameOver();


}
