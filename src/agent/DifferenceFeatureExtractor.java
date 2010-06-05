/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Random;
import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public class DifferenceFeatureExtractor extends FeatureExtractor {

    public static int D = 2;
    public static int DW = 2*D+1;
    
    public static int OFS_MISC;
    public static int OFS_PAIRS;
    public static int OFS_TRIPLETS;
    public static int N_MISC;
    public static int N_PAIRS;
    public static int N_TRIPLETS;

    int wb[][];

    double[] phi;

    public DifferenceFeatureExtractor(TetrisGame game)
    {
        super(game);
        N_MISC = 2;
        N_PAIRS = DW*(game.width-1);
        N_TRIPLETS = DW*DW*(game.width-2);
        nFeatures = N_MISC + N_PAIRS;// + N_TRIPLETS;
        wb = new int[game.width + 2*game.PADDING][game.height + 2*game.PADDING];
        phi = new double[nFeatures];
        OFS_MISC = 0;
        OFS_PAIRS = OFS_MISC + N_MISC;
        OFS_TRIPLETS = OFS_PAIRS + N_PAIRS;
        name = "DI";
    }

    @Override
    public double[] getFeatures(int[][] board, double reward)
    {
        int[] heights = new int[game.width];
        int maxh = 0;
        int nholes = 0;
        int coltrans = 0;
        int rowtrans = 0;
        int welldepth = 0;
        int maxwd = 0;
        int holedepth = 0;
        int rh, lh;

        int i, j;
        for(i=0; i<game.width; i++)
        {
            for(j=0; j<game.height; j++)
            {
                if (board[i+game.PADDING][j+game.PADDING] != 0)
                    break;
            }
            heights[i] = game.height-j;
            for(; j<game.height; j++)
            {
                if (board[i+game.PADDING][j+game.PADDING] == 0)
                    nholes++;
            }
        }

        phi = new double[nFeatures];

        phi[OFS_MISC + 0] = game.height-maxh;
        phi[OFS_MISC + 1] = nholes;
        // pairs...
        for(i=0; i<game.width-1; i++)
        {
            int diff = heights[i]-heights[i+1];
            diff = Math.min(Math.max(diff, -D),D) + D;
            phi[OFS_PAIRS+DW*i + diff] = 1;
        }
        // triplets
        for(i=0; i<game.width-2; i++)
        {
            int diff1 = heights[i]-heights[i+1];
            diff1 = Math.min(Math.max(diff1, -D),D) + D;
            int diff2 = heights[i]-heights[i+1];
            diff2 = Math.min(Math.max(diff2, -D),D) + D;
            //phi[OFS_TRIPLETS+DW*DW*i + DW*diff1 + diff2] = 1;
        }
        Random rnd = new Random();
        for(i=0; i<nFeatures; i++)
            phi[i] +=rnd.nextDouble()*0.01;
        return phi;
    }

}
