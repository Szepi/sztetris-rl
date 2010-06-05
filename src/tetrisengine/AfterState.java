/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tetrisengine;

import java.io.Serializable;

/**
 *
 * @author szityu
 */
public class AfterState implements Serializable {
    public final static int PADDING = 3;
    //public final static int NTETROMINOES = 7;
    //public final static int NROTS = 4;
    public final static int T_WALL  = 8;
    public final static int T_EMPTY = 0;

    public int width, height;
    public int board[][];
    public int skyline[];
    public int lastLandingHeight=0;
    public int lastResult=0;
    public int lastrot =0;
    public int lastpos =0;
    public TetrisAction lastAction = new TetrisAction(0,0);

    public AfterState(int width, int height)
    {
        this.width = width;
        this.height = height;
        board = new int[width + 2*PADDING][height + 2*PADDING];
        skyline = new int[width + 2*PADDING];
    }

    public AfterState(AfterState s)
    {
        this(s.width, s.height);
        copyBoard(s.board, board);
        for (int i=0; i<skyline.length; i++)
        {
            skyline[i] = s.skyline[i];
        }

    }

    public AfterState(int [][] b, int width, int height)
    {
        this(width, height);
        copyBoard(b, board);
        UpdateSkyline();
        
    }

    public void clear()
    {
    	int i, j;
	for(i=0; i<board.length; i++)
		for(j=0; j<board[i].length; j++)
			board[i][j] = T_WALL;
	for(i=0; i<width; i++)
		for(j=0; j<height+PADDING; j++)
			board[i+PADDING][j] = T_EMPTY;
        UpdateSkyline();
    }


    public void UpdateSkyline()
    {
        int i, j;
        for(i=0; i<skyline.length; i++)
        {
            for(j=0; j<board[i].length; j++)
            {
                if (board[i][j] != 0)
                    break;
            }
            skyline[i] = j;
        }
    }

    public static void copyBoard(int[][] fromBoard, int[][] toBoard)
    {
        int i,j;
    	for(i=0; i<fromBoard.length; i++)
        	for(j=0; j<fromBoard[i].length; j++)
            	toBoard[i][j] = fromBoard[i][j];
    }

    public static int[][] copyBoardwithAlloc(int[][] fromBoard)
    {
        int[][] toBoard = new int[fromBoard.length][fromBoard[0].length];
        copyBoard(fromBoard, toBoard);
        return toBoard;

    }

   public AfterState putTile(int type, int rot, int pos, boolean bUpdateSkyline)
    {
        int[][] tile = TetrisGame.tiles[type][rot];
        int ofs = 10000;
        int x, y;
        int result;

        AfterState s = new AfterState(this);

        for(x=0; x<4; x++)
        {
            ofs = Math.min(ofs, skyline[x+pos+PADDING]-TetrisGame.tilebottoms[type][rot][x]-1);
        }
        if (ofs<PADDING)   // does not fit in there
        {
            s.lastResult = -1;
            return s;
        }
        for(x=0; x<4; x++)
            for (y=0; y<4; y++)
                if (tile[x][y] != 0)
                    s.board[x+pos+PADDING][y+ofs] = type+1;
        s.lastLandingHeight = ofs;
//        System.out.println("before erase");
        s.lastResult = s.eraseLines();
        s.lastpos = pos;
        s.lastrot = rot;
        s.lastAction = new TetrisAction(pos, rot);
        if (bUpdateSkyline)
            s.UpdateSkyline();
        return s;
    }

    public int eraseLines()
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
                if (board[x+PADDING][y+PADDING] == 0)
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
                        board[x+PADDING][y2+PADDING] = board[x+PADDING][y2-1+PADDING];
                }
                y++; //we should check that line again
                nErased++;
            }
        }
        return nErased;
    }

    // used for pretty printing
    // by default, all tiles have different colors (different numbers in the board)
    // after decolorizing, the new tiles will stand out better.
    public void decolorize()
    {
        for (int y=0; y<height; y++)
        {
            for (int x=0; x<width; x++)
            {
                if (board[x+PADDING][y+PADDING] != T_EMPTY)
                {
                    board[x+PADDING][y+PADDING] = 9;
                }
            }
        }

    }

    private static final char[] chars = " 0123456#x".toCharArray();
    @Override
    public String toString()
    {
        StringBuilder s = new StringBuilder();

        for (int y=0; y<height+1; y++)
        {
            for (int x=-1; x<width+1; x++)
            {
                s.append(chars[board[x+PADDING][y+PADDING]]);
            }
            s.append("\n");
        }
        return s.toString();
    }
}
