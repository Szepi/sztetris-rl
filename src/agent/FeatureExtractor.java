/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public abstract class FeatureExtractor {

    public TetrisGame game;
    public int nFeatures;
    public String name;

    public FeatureExtractor(TetrisGame game)
    {
        this.game = game;
    }

    public abstract double[] getFeatures(int[][] board, double reward);

    ///!!!!!!!!!! divisor not used
    public double[] getFeaturesNormed(int[][] board, double reward, double divisor)
    {
        double[] phi = getFeatures(board, reward);
        double norm2 = 0;
        for (int i=0; i<phi.length; i++)
            norm2 += phi[i]*phi[i];
        norm2 = Math.sqrt(norm2);
        for (int i=0; i<phi.length; i++)
            phi[i] /= divisor;
        return phi;
    }
    
}
