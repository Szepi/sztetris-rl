/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Random;
import tetrisengine.TetrisAction;

/**
 *
 * @author istvanszita
 */
public class RandomAgent extends TetrisAgent {

    Random rnd = new Random();

    public RandomAgent(int width, int height)
    {
        super(width,height);
        name = "Random agent";
    }

    @Override
    public void NewGame(int piece, int[][] state)
    {
        super.NewGame(piece, state);
    }

    @Override
    public void getObservation(double reward, int piece, int[][] state)
    {
        super.getObservation(reward, piece, state);
    }

    @Override
    public TetrisAction act()
    {
        int rot = rnd.nextInt(4);
        int pos = rnd.nextInt(width-4)+2;
        return new TetrisAction(pos,rot);
    }

    @Override
    public void GameOver()
    {

    }

}
