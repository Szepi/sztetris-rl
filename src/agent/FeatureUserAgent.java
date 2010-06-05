/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public class FeatureUserAgent extends TetrisAgent {

    Random rnd = new Random();
    int piece;
    public double[] phi;
    public FeatureExtractor fex;
    public int nFeatures;
    public double[] w;

    int[][] workboard;

    public FeatureUserAgent(int width, int height, FeatureExtractor fex)
    {
        super(width,height);
        name = "Feature user agent";
        this.fex = fex;
        nFeatures = fex.nFeatures;
        phi = new double[nFeatures];
        w = new double[nFeatures];
        workboard = new int[width + 2*TetrisGame.PADDING][height + 2*TetrisGame.PADDING];

        for (int i=0; i<nFeatures; i++)
        {
            w[i] = rnd.nextDouble();
        }
    }

    public void setWeights(double[] w2)
    {
        for (int i=0; i<nFeatures; i++)
        {
            w[i] = w2[i];
        }
    }

    public void loadWeights(String fname)
    {
    	int i, j;
        BufferedReader in=null;

        boolean fileexists = (new File(fname)).exists();
        String[] slist;
        if (fileexists)
        {
            try {
                String s, lasts;
                int k = 0;

                try {
                    in = new BufferedReader(new FileReader(fname));
                } catch (FileNotFoundException ex) {
                    Logger.getLogger(TetrisGame.class.getName()).log(Level.SEVERE, null, ex);
                }
                s = in.readLine();
                do
                {
                    k++;
                    lasts = s;
                    s = in.readLine();
                } while (s!=null);
                lasts = lasts.replaceAll("  ", " ");
                lasts = lasts.replaceAll("  ", " ");
                lasts = lasts.replaceAll("  ", " ");
                lasts = lasts.replaceAll("  ", " ");
//                System.out.println(lasts);
                slist = lasts.split(" ");
//                for (i=0; i<slist.length; i++)
//                    System.out.print(slist[i]+"; ");
//                System.out.println();
                for (i=0; i<nFeatures; i++)
                    w[i] = Double.parseDouble(slist[i+3]);
                System.out.println(" Initialized.");
            } catch (IOException ex) {
                //Logger.getLogger(TetrisGame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else
        {
            System.out.println("File not found. Using random values.");
            for (i=0; i<nFeatures; i++)
                w[i] = rnd.nextGaussian();
        }
    }

    @Override
    public void NewGame(int piece, int[][] state)
    {
        super.NewGame(piece, state);
        phi = fex.getFeaturesNormed(state,0,1);
        this.piece = piece;
    }

    @Override
    public void getObservation(double reward, int piece, int[][] state)
    {
        super.getObservation(reward, piece, state);
//        phi = fex.getFeatures(state, reward);
        phi = fex.getFeaturesNormed(state, reward,1);
        this.piece = piece;
    }


    @Override
    public TetrisAction act()
    {
        TetrisAction a = putTileGreedy(piece);
        //System.out.println(a.pos + ", " + a.rot);
        return a;
    }

    @Override
    public void GameOver()
    {

    }

    public TetrisAction putTileGreedy(int piece)
    {
        int rot, pos;
        double bestvalue, value;
        int res;
        int i;
        TetrisAction bestAction = new TetrisAction(0,0);

        //type = rnd.nextInt(7);

        //UpdateSkyline();
        AfterState s = new AfterState(agentstate,width, height);
        AfterState nexts;
        bestvalue = -1e10;
        for (rot=0; rot<4; rot++)
        {
            for (pos=0; pos<width; pos++)
            {
//                AfterState.copyBoard(agentstate, workboard);
//                System.out.println("   "+ type + ", " +rot + ", " + pos);
                nexts = s.putTile(piece, rot, pos, false);
                res = nexts.lastResult;
//                res = putTile(workboard, piece, rot, pos, false);
//                System.out.println("   "+ type + ", " +rot + ", " + pos + "   OK");
                if (res>=0)
                {
                    value = 0;
                    phi = fex.getFeaturesNormed(nexts.board,res,1); //compute phi
                    for (i=0; i<nFeatures; i++)
                    {
                        value += w[i]*phi[i];
                    }
                }
                else
                    value = -1e10;
                if (value>bestvalue)
                {
                    bestvalue = value;
                    bestAction = new TetrisAction(pos,rot);
                }
            }
        }
        return bestAction;
    }

//   public int putTile(int[][] b, int type, int rot, int pos, boolean bUpdateSkyline)
//    {
//        int[][] tile = TetrisGame.tiles[type][rot];
//        int ofs = 10000;
//        int x, y;
//        int result;
//
//        for(x=0; x<4; x++)
//        {
//            ofs = Math.min(ofs, skyline[x+pos+TetrisGame.PADDING]-TetrisGame.tilebottoms[type][rot][x]-1);
//        }
//        if (ofs<TetrisGame.PADDING)   // does not fit in there
//            return -1;
//        for(x=0; x<4; x++)
//            for (y=0; y<4; y++)
//                if (tile[x][y] != 0)
//                    b[x+pos+TetrisGame.PADDING][y+ofs] = type+1;
//        result = eraseLines(b);
//
//        return result;
//    }
//
//    public void UpdateSkyline()
//    {
//        int i, j;
//        for(i=0; i<skyline.length; i++)
//        {
//            for(j=0; j<agentstate[i].length; j++)
//            {
//                if (agentstate[i][j] != 0)
//                    break;
//            }
//            skyline[i] = j;
//        }
//    }
//
//    public int eraseLines(int[][] b)
//    {
//        int x, y, y2;
//        int nErased = 0;
//        boolean isfull;
//        int debugy = 0;
//
//        for (y=height-1; y>=0; y--)
//        {
//            debugy ++;
//            isfull = true;
//            for (x=0; x<width; x++)
//            {
//                if (b[x+TetrisGame.PADDING][y+TetrisGame.PADDING] == 0)
//                {
//                    isfull = false;
//                    break;
//                }
//            }
//            if (isfull)
//            {
//                for(y2=y; y2>=0; y2--)
//                {
//                    for (x=0; x<width; x++)
//                        b[x+TetrisGame.PADDING][y2+TetrisGame.PADDING] = b[x+TetrisGame.PADDING][y2-1+TetrisGame.PADDING];
//                }
//                y++; //we should check that line again
//                nErased++;
//            }
//        }
//        return nErased;
//    }

        public XP analyzeActions(int piece)
    {
        int rot, pos;
        int res;
        int i;
        //legalActions = new Vector<TetrisAction>();

        //type = rnd.nextInt(7);

        XP xp = new XP(phi,agentstate, piece);

        AfterState s = new AfterState(agentstate,width, height);
        AfterState nexts;
//        UpdateSkyline();
        for (rot=0; rot<2; rot++)
        {
            for (pos=0; pos<width; pos++)
            {
                AfterState.copyBoard(agentstate, workboard);
//                System.out.println("   "+ type + ", " +rot + ", " + pos);
                nexts = s.putTile(piece, rot, pos, false);
                res = nexts.lastResult;
//                res = putTile(workboard, piece, rot, pos, false);
//                System.out.println("   "+ type + ", " +rot + ", " + pos + "   OK");
                if (res>=0)
                {
                    xp.addNext(res, new TetrisAction(pos,rot), phi);
                    //legalActions.add(new TetrisAction(pos,rot));
                }
            }
        }
        return xp;
    }

}
