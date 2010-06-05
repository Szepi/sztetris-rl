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
public class BertsekasIoffeFeatureExtractor extends FeatureExtractor {

    public static int OFS_HEIGHTS;
    public static int OFS_DELTAS;
    public static int OFS_MISC;
    int wb[][];

    double[] phi;

    public BertsekasIoffeFeatureExtractor(TetrisGame game)
    {
        super(game);
        nFeatures = (game.width + (game.width-1) + 3)*1;
        OFS_HEIGHTS = 0;
        OFS_DELTAS = OFS_HEIGHTS + game.width;
        OFS_MISC = OFS_DELTAS + (game.width-1);
        wb = new int[game.width + 2*game.PADDING][game.height + 2*game.PADDING];
        phi = new double[nFeatures];
        name = "BI";
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
            phi[OFS_HEIGHTS+i] = heights[i];
            if (i>0)
            {
                phi[OFS_DELTAS + i-1] = Math.abs(heights[i]-heights[i-1]);
            }
            if (heights[i]>maxh)
                maxh = heights[i];
        }
        phi[OFS_MISC + 0] = (game.height-maxh);
        phi[OFS_MISC + 1] = nholes;
        phi[OFS_MISC + 2] = 1;

        return phi;
    }

}
