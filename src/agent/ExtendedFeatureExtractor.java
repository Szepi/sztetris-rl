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
public class ExtendedFeatureExtractor extends FeatureExtractor {

    public static int D = 2;
    public static int DW = 2*D+1;

    public static final int MAX_HEIGHT      = 0;
    public static final int NUM_HOLES       = 1;
    public static final int LANDING_HEIGHT  = 2;
    public static final int ROW_TRANS       = 3;
    public static final int COL_TRANS       = 4;
    public static final int WELLS           = 5;
    public static final int HOLE_DEPTH      = 6;
    public static final int INCOMPLETE_LINES= 7;
    public static final int ERODED_PIECES   = 8;
    public static final int CONST           = 9;
    public int OFS_HEIGHTS;
    public int OFS_DELTAS;
    public int OFS_MISC;
    public int OFS_HPAIRS;
    public int N_MISC;
    public int N_HPAIRS;
    int wb[][];

    double[] phi;

    public ExtendedFeatureExtractor(TetrisGame game)
    {
        super(game);
        N_MISC = 10;
        OFS_HEIGHTS = 0;
        OFS_DELTAS = OFS_HEIGHTS + game.width;
        OFS_MISC = OFS_DELTAS + (game.width-1);
        OFS_HPAIRS = OFS_MISC + N_MISC;
        N_HPAIRS = (game.width-1)*2*DW;

        wb = new int[game.width + 2*game.PADDING][game.height + 2*game.PADDING];
        nFeatures = game.width + (game.width-1) + N_MISC +N_HPAIRS;
        phi = new double[nFeatures];
        name = "XT";
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
        phi[OFS_MISC + MAX_HEIGHT] = game.height-maxh;
        phi[OFS_MISC + NUM_HOLES] = nholes;
        phi[OFS_MISC + LANDING_HEIGHT] = game.height-game.state.lastLandingHeight;
//        phi[OFS_MISC + ERODED_PIECES] = (game.lastResult==-1) ? 0 : game.lastResult ;
        phi[OFS_MISC + CONST] = 1;
        for(j=0; j<=game.height; j++)
            for (i=0; i<game.width; i++)
                if ((board[i+game.PADDING][j-1+game.PADDING]==0) ^ (board[i+game.PADDING][j+game.PADDING]==0))
                    coltrans++;
        phi[OFS_MISC + COL_TRANS] = coltrans;
        for(j=0; j<game.height; j++)
            for (i=0; i<=game.width; i++)
                if ((board[i-1+game.PADDING][j+game.PADDING]==0) ^ (board[i+game.PADDING][j+game.PADDING]==0))
                    rowtrans++;
        phi[OFS_MISC + ROW_TRANS] = rowtrans;
        maxwd = 0;
        for (i=0; i<game.width; i++)
        {
            lh = (i==0) ? (game.height) : (heights[i-1]);
            rh = (i==game.width-1) ? (game.height) : (heights[i+1]);
            int h = heights[i];
            if ((h<lh) && (h<rh))
            {
                int wd = Math.min(lh-h, rh-h);
                welldepth += wd*(wd+1)/2;
                if (wd<maxwd)
                {
                    maxwd = wd;
                }
            }
        }
        phi[OFS_MISC + WELLS] = welldepth;
        for (i=0; i<game.width; i++)
        {
            boolean hadhole = false;
            for (j=game.height-1; j>=0; j--)
            {
                if (board[i+game.PADDING][j+game.PADDING]==0)
                    hadhole = true;
                if ((board[i+game.PADDING][j+game.PADDING]!=0) && (hadhole))
                    holedepth++;
            }
        }
        phi[OFS_MISC + HOLE_DEPTH] = holedepth;

	for(i=game.PADDING; i<game.width+game.PADDING; i++)
		for(j=game.PADDING; j<game.height+game.PADDING; j++)
			wb[i][j] = board[i][j];
        for (i=0; i<game.width; i++)
        {
            boolean started = false;
            for (j=0; j<game.height; j++)
            {
                if (wb[i+game.PADDING][j+game.PADDING]!=0)
                    started = true;
                if (started)
                    wb[i+game.PADDING][j+game.PADDING]=1;
            }
        }
        phi[OFS_MISC + INCOMPLETE_LINES] = eraseLines(wb, game.width, game.height);

 


        // h-pairs...
        for(i=0; i<game.width-1; i++)
        {
            int diff = heights[i]-heights[i+1];
            diff = Math.min(Math.max(diff, -D),D) + D;
            //System.out.println(OFS_HPAIRS+DW*i+diff);
            phi[OFS_HPAIRS+DW*i+diff] = heights[i+1];
        }
        for(i=1; i<game.width; i++)
        {
            int diff = heights[i-1]-heights[i];
            diff = Math.min(Math.max(diff, -D),D) + D;
            //System.out.println(OFS_HPAIRS+N_HPAIRS/2+DW*i+diff);
            phi[OFS_HPAIRS+N_HPAIRS/2+ DW*(i-1) + diff] = heights[i-1];
        }
        Random rnd = new Random();
        for(i=0; i<nFeatures; i++)
            phi[i] +=rnd.nextDouble()*0.001;
        return phi;
    }
    public int eraseLines(int[][] b, int width, int height)
    {
        int x, y, y2;
        int nErased = 0;
        boolean isfull;
        int debugy = 0;

        for (y=height-1; y>=0; y--)
        {
            debugy ++;
            isfull = true;
            for (x=0; x<width; x++)
            {
                if (b[x+TetrisGame.PADDING][y+TetrisGame.PADDING] == 0)
                {
                    isfull = false;
                    break;
                }
            }
            if (isfull)
            {
                for(y2=y; y2>=0; y2--)
                {
                    for (x=0; x<width; x++)
                        b[x+TetrisGame.PADDING][y2+TetrisGame.PADDING] = b[x+TetrisGame.PADDING][y2-1+TetrisGame.PADDING];
                }
                y++; //we should check that line again
                nErased++;
            }
        }
        return nErased;
    }

}
