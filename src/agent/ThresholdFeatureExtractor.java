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
public class ThresholdFeatureExtractor extends FeatureExtractor {

    public static int OFS_HEIGHTS;
    public static int OFS_DELTAS;
    public static int OFS_MISC;
    public static int HBINS;
    public static int DBINS;
    public static int DELTACAP;
    public static int NHEIGHTS;
    public static int NDELTAS;

    int wb[][];

    double[] phi;

    public ThresholdFeatureExtractor(TetrisGame game)
    {
        super(game);
        DELTACAP = 3;
        DBINS = DELTACAP*2+1;
        NDELTAS = DBINS*(game.width-1);
        HBINS = game.height;
        NHEIGHTS = HBINS*game.width;
        nFeatures = 2 + NHEIGHTS + NDELTAS;
        OFS_MISC = 0;
        OFS_HEIGHTS = OFS_MISC +2;
        OFS_DELTAS = OFS_HEIGHTS + NHEIGHTS;
        wb = new int[game.width + 2*game.PADDING][game.height + 2*game.PADDING];
        phi = new double[nFeatures];
        name = "TH";
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
            for (int z=0; z<HBINS; z++)
            {
                if (Math.abs(heights[i]-z)<=2)
                {
                    phi[OFS_HEIGHTS+i*HBINS+ z] = 1;
                }
            }
            //phi[OFS_HEIGHTS+i*HBINS] = heights[i];
            if (i>0)
            {
                int d = Math.abs(heights[i]-heights[i-1]);
                d = Math.max(Math.min(d, DELTACAP), -DELTACAP);
                d += DELTACAP;
                phi[OFS_DELTAS + (i-1)*DBINS + d] = 1;

                //phi[OFS_DELTAS + (i-1)*DBINS] = Math.abs(heights[i]-heights[i-1]);
            }
            if (heights[i]>maxh)
                maxh = heights[i];
        }
        phi[OFS_MISC + 0] = game.height-maxh;
        phi[OFS_MISC + 1] = nholes;

        return phi;
    }

}
