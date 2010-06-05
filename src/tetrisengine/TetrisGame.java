/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tetrisengine;

import agent.TetrisAgent;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.logging.*;

/**
 *
 * @author szityu
 */
public class TetrisGame {
    public final static int PADDING = 3;
    public final static int NTETROMINOES = 7;
    public final static int NROTS = 4;
    public final static int T_WALL  = 8;    
    public final static int T_EMPTY = 0;
    public final static Color[] tilecolor = 
    {
        Color.WHITE,
        Color.BLUE,
        Color.CYAN,
        Color.GRAY,
        Color.GREEN,
        Color.MAGENTA,
        Color.RED,
        Color.ORANGE,
        Color.BLACK
    };
    
    public static int[][][][] tiles = new int[NTETROMINOES][NROTS][4][4];
    public static int[][][]   tilebottoms = new int[NTETROMINOES][NROTS][4];

///*  landing height
// *  eroded pieces
// *  row transitions
// *  column transitions
// *  board wells
// *  holes depth  - num cells above holes
// * 
// */
//    
//    public static final int LANDING_HEIGHT  = 2;
//    public static final int ERODED_PIECES   = 3;
//    public static final int ROW_TRANS       = 4;
//    public static final int COL_TRANS       = 5;
//    public static final int WELLS           = 6;
//    public static final int HOLE_DEPTH      = 7;
//    public static final int INCOMPLETE_LINES= 8;
//    public static final int I_FILLS         = 9;
//    public static final int I_DESIRABLE     = 10;
    
    public int width, height; 
//    public int NBASISFUNCTIONS;
//    public int OFS_HEIGHTS;
//    public int OFS_DELTAS;
//    public int OFS_MISC;

    public AfterState state;
    //public int board[][];
    public int workboard[][];
    public int wb[][];
    //public int skyline[];
//    public int[] phi;
//    public double[] w;
    public Random rnd;
    public int bestrot=0;
    public int bestpos=0;
    BufferedReader in;
    

        
    //public int lastLandingHeight=0;
    //public int lastResult=0;
    
    public TetrisGame()
    {
        this(10, 20);
    }
    
    public TetrisGame(int width, int height)
    {
	int i, j, x, y;

        this.width = width;
        this.height = height;
//        phi = new int[NBASISFUNCTIONS];
//        w = new double[NBASISFUNCTIONS];

        state = new AfterState(width,height);
//        board = new int[width + 2*PADDING][height + 2*PADDING];
        workboard = new int[width + 2*PADDING][height + 2*PADDING];
        wb = new int[width + 2*PADDING][height + 2*PADDING];
//        skyline = new int[width + 2*PADDING];
        
        clearBoard();
        rnd = new Random();

        int [][] tile;
        
        for (i=0; i<NTETROMINOES; i++)
        {
            for (j=0; j<NROTS; j++)
            {
                tile = generateTile(i,j);
                for (x=0; x<4; x++)
                {
                    int last = -100;
                    for (y=0; y<4; y++)
                    {
                        tiles[i][j][x][y] = tile[x][y];
                        if (tile[x][y] != 0)
                            last = y;
                    }
                    tilebottoms[i][j][x] = last;
                }
                //tiles[i][j].clone(generateTile(i,j));
            }
        }
        
        
        
    }
    
           
    
    public void clearBoard()
    {
	int i, j;
        state.clear();

	for(i=0; i<width + 2*PADDING; i++)
		for(j=PADDING; j<height + 2*PADDING; j++)
			workboard[i][j] = state.board[i][j];
        
        
    }
    
    public static final int[] scoretable = {0,1,2,3,4};
    
    int [][] generateTile(int type, int rot)
    {
        int [][] t;
        switch (type)
        {
	        case 0: // S
	            t = new int[][]{{0, 1, 1, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
	            break;                
	        case 1: // Z
	            t = new int[][]{{1, 1, 0, 0}, {0, 1, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
	            break;                
            case 2: // L
                t = new int[][]{{1, 1, 1, 0}, {1, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
                break;                
            case 3: // J
                t = new int[][]{{1, 1, 1, 0}, {0, 0, 1, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
                break;                
            case 4: // T
                t = new int[][]{{1, 1, 1, 0}, {0, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
                break;                
            case 5: // I
                t = new int[][]{{1, 1, 1, 1}, {0, 0, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
                break;
            case 6: // O
                t = new int[][]{{1, 1, 0, 0}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
                break; 
            default: // non-existent piece
                t = new int[][]{{1, 1, 1, 1}, {1, 1, 0, 0}, {0, 0, 0, 0}, {0, 0, 0, 0}}; 
                break; 
        }
        
        
        int[][] t2 = new int[4][4];
        int x, y;
        switch (rot)
        {
            case 0:
                for (x=0; x<4; x++)
                    for (y=0; y<4; y++)
                        t2[x][y] = t[x][y];
                break;
            case 1:
                // 1110      0000     
                // 0100      1000       
                // 0000  ->  1100          
                // 0000      1000          
                for (x=0; x<4; x++)
                    for (y=0; y<4; y++)
                        t2[x][y] = t[y][3-x];
                break;
            case 2:
                for (x=0; x<4; x++)
                    for (y=0; y<4; y++)
                        t2[x][y] = t[3-x][3-y];
                break;
            case 3:
                for (x=0; x<4; x++)
                    for (y=0; y<4; y++)
                        t2[x][y] = t[3-y][x];
                break;                
        }
                    
        int emptyrow = 0;
        int emptycol = 0;
        
        // determine number of empty columns
        outerloop1:
        for (x=0; x<4; x++)
        {
            for (y=0; y<4; y++)   
            {
                if (t2[x][y] != 0)
                    break outerloop1;
            }
            emptycol++;
        }
        
        // determine number of empty columns
        outerloop2:
        for (y=0; y<4; y++)
        {
            for (x=0; x<4; x++)   
            {
                if (t2[x][y] != 0)
                    break outerloop2;
            }
            emptyrow++;
        }
        
        int[][] t3 = new int[4][4];
        
        for (x=emptycol; x<4; x++)
        {
            for (y=emptyrow; y<4; y++)
                t3[x-emptycol][y-emptyrow] = t2[x][y];
        }
        return t3;
    }


    
    
    
    public void copyWorkBoard()
    {
        int i,j;
    	for(i=PADDING; i<width+PADDING; i++)
        	for(j=PADDING; j<height+PADDING; j++)
            	workboard[i][j] = state.board[i][j];
    }

       
//    public int putTileGreedy(int type)
//    {
//        int rot, pos;
//        double bestvalue, value;
//        int res;
//        int i;
//        
//        
//        bestvalue = -1e10;
//        for (rot=0; rot<4; rot++)
//        {
//            for (pos=0; pos<width; pos++)
//            {
//                copyWorkBoard();
////                System.out.println("   "+ type + ", " +rot + ", " + pos);
//                res = putTile(workboard, type, rot, pos);
////                if (((type==0) && (pos==0) && (rot!=0))
////                        || ((type!=0) && (pos==0)))
////                    res = -1;
////                System.out.println("   "+ type + ", " +rot + ", " + pos + "   OK");
//                if (res>=0)
//                {
//                    value = 0;
//                    getBasisFunctionValues(workboard); //compute phi
//                    for (i=0; i<NBASISFUNCTIONS; i++)
//                    {
//                        value += w[i]*phi[i];
//                    }
//                }
//                else
//                    value = -1e10;
//                if (value>bestvalue)
//                {
//                    bestvalue = value;
//                    bestrot = rot;
//                    bestpos = pos;
//                }
//            }
//        }
//        getBasisFunctionValues(board); //compute phi
//        boolean hasdeepwell = true;
//        for (i=1; i<width; i++)
//            if (phi[OFS_HEIGHTS+i]<=phi[OFS_HEIGHTS+0]+4)
//                hasdeepwell = false;
//        if ((hasdeepwell) && (type==0))
//        {
//            bestrot = 0;
//            bestpos = 0;
//        }
////        rot = rnd.nextInt(4);
////        pos = rnd.nextInt(width);
//        if (bestvalue>-1e10)
//        {
//            res = putTile(board, type, bestrot, bestpos);
//            UpdateSkyline();
//            return res;
//        }
//        else
//            return -1;
//    }


    public int newPiece()
    {
        //return rnd.nextInt(7);
        return rnd.nextInt(2);
    }

    public double playOneGame(TetrisAgent agent)
    {
        clearBoard();
        return playOneGame(agent,state);
    }

    public double playOneGame(TetrisAgent agent, AfterState fromState)
    {

        int res;
        double totalRew = 0.0;

        int piece = newPiece();
        //clearBoard();
        state = fromState;
        agent.NewGame(piece, state.board);
        while (true)
        {
            TetrisAction a = agent.act();
            state = state.putTile(piece, a.rot, a.pos, true);
            res = state.lastResult;
            //System.out.println(String.format("a: p:%d,r:%d   res:%d",a.pos,a.rot, res));
            if (res ==-1)
            {
                agent.GameOver();
                break;
            }
            else
            {
                totalRew += res;
                piece = newPiece();
                agent.getObservation(res, piece, state.board);
            }

        }
        //System.out.println("totalrew = " + totalRew);
        return totalRew;
    }

    
}
 