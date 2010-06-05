/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Random;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author szityu
 */
public class HandCodedForSZAgent extends TetrisAgent {

    Random rnd = new Random();
    int piece;
    double[] phi;
    public FeatureExtractor fex;
    public int nFeatures;

    int[][] workboard;

    public HandCodedForSZAgent(int width, int height)
    {
        super(width,height);
        name = "Hand-coded SZ agent";
        fex = new BertsekasIoffeFeatureExtractor(new TetrisGame(width, height));
        nFeatures = fex.nFeatures;
        phi = new double[nFeatures];
        workboard = new int[width + 2*TetrisGame.PADDING][height + 2*TetrisGame.PADDING];

    }



    @Override
    public void NewGame(int piece, int[][] state)
    {
        super.NewGame(piece, state);
        phi = fex.getFeatures(state,0);
        this.piece = piece;
    }

    @Override
    public void getObservation(double reward, int piece, int[][] state)
    {
        super.getObservation(reward, piece, state);
        phi = fex.getFeatures(state,0);
        this.piece = piece;
    }


    static TetrisAction toPos1 = new TetrisAction(0,0);
    static TetrisAction toPos2 = new TetrisAction(2,0);
    static TetrisAction toPos3 = new TetrisAction(4,0);
    static TetrisAction toPos4 = new TetrisAction(6,0);
    static TetrisAction toPos5 = new TetrisAction(8,0);

    int typeOfMid = 0;
    int DANGERLEVEL = 16;

    @Override
    public TetrisAction act()
    {
        TetrisAction a = toPos3;

        int OFS = BertsekasIoffeFeatureExtractor.OFS_HEIGHTS;
        int[] h = {
            (int)Math.max(phi[OFS+0], phi[OFS+1]),
            (int)Math.max(phi[OFS+2], phi[OFS+3]),
            (int)Math.max(phi[OFS+4], phi[OFS+5]),
            (int)Math.max(phi[OFS+6], phi[OFS+7]),
            (int)Math.max(phi[OFS+8], phi[OFS+9])
        };
        int minh;

        if (typeOfMid == 0)
            typeOfMid = piece;
        if (piece == 4)
        {
//            a = toPos1;
            minh = Math.min(h[0], h[1]);
            if (typeOfMid == piece)
            {
                minh = Math.min(h[2], minh);
                if (minh == h[0])
                    a = toPos1;
                else if (minh == h[1])
                    a = toPos2;
                else if (minh == h[2])
                    a = toPos3;
            }
            else
            {
                if (minh>DANGERLEVEL)
                {
                    typeOfMid = piece;
                    a = toPos3;
                }
                else if (minh == h[0])
                    a = toPos1;
                else if (minh == h[1])
                    a = toPos2;
            }
        }
        if (piece == 5)
        {
//            a = toPos5;
            minh = Math.min(h[3], h[4]);
            if (typeOfMid == piece)
            {
                minh = Math.min(h[2], minh);
                if (minh == h[4])
                    a = toPos5;
                else if (minh == h[3])
                    a = toPos4;
                else if (minh == h[2])
                    a = toPos3;
            }
            else
            {
                if (minh>DANGERLEVEL)
                {
                    typeOfMid = piece;
                    a = toPos3;
                }
                else if (minh == h[4])
                    a = toPos5;
                else if (minh == h[3])
                    a = toPos4;
            }
        }

        //System.out.println(a.pos + ", " + a.rot);

        // fill xpList
        XP xp = new XP(phi,agentstate, piece);
        AfterState s = new AfterState(agentstate,width, height);
        AfterState nexts;
        nexts = s.putTile(piece, a.rot, a.pos, true);
        double res = nexts.lastResult;
        if (res>=0)
        {
            phi = fex.getFeatures(nexts.board, res); //compute phi
            xp.addNext(res, a, phi);
            //legalActions.add(new TetrisAction(pos,rot));
        }
        xp.a = a;
        xp.aind = 0;
        xp.r = res;
        xpList.add(xp);


        return a;
    }

    @Override
    public void GameOver()
    {

    }


    public void UpdateSkyline()
    {
        int i, j;
        for(i=0; i<skyline.length; i++)
        {
            for(j=0; j<agentstate[i].length; j++)
            {
                if (agentstate[i][j] != 0)
                    break;
            }
            skyline[i] = j;
        }
    }


}
