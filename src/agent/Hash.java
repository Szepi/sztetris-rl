/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package agent;

import java.util.Random;
import tetrisengine.TetrisGame;

/**
 *
 * @author szityu
 */
public class Hash {

    static final int N=600; // upper bound on total number of cells in a board
    static long[] hashnums;
    static Random rnd;

    static {
        System.out.println("Hash init");
        rnd = new Random();
        hashnums = new long[N];
        for (int i=0; i<N; i++)
        {
            hashnums[i] = rnd.nextLong();
        }
    }

    public static String stringhash(int[][] b, int piece)
    {
        StringBuilder s = new StringBuilder();
        s.append(piece+'0');
        for (int i=0; i<b.length; i++)
            for (int j=0; j<b[i].length; j++)
            {
                s.append(b[i][j]==TetrisGame.T_EMPTY?'0':'1');
            }
        return s.toString();
    }

//    public static long hash(int[][] b)
//    {
//        long h = 0;
//        int ind = 0;
//        for (int i=0; i<b.length; i++)
//            for (int j=0; j<b[i].length; j++)
//            {
//                if (b[i][j] != TetrisGame.T_EMPTY)
//                {
//                    h ^= hashnums[ind];
//                }
//                ind++;
//            }
//        return h;
//    }
}
