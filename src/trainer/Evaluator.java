/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trainer;

import Jama.Matrix;
import agent.BertsekasIoffeFeatureExtractor;
import agent.DifferenceFeatureExtractor;
import agent.ExtendedFeatureExtractor;
import agent.FeatureExtractor;
import agent.FeatureUserAgent;
import agent.PercLearnerAgent;
import agent.PerceptronLearner;
import agent.ScherrerThieryFeatureExtractor;
import agent.TetrisAgent;
import agent.VIAgent;
import agent.XP;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author szityu
 */
public class Evaluator {
    Random rnd;
    
    int MAXGAMES = 10000;
    
    double [] scorelist;
    
    boolean fileexists;
    
    TetrisGame game;
    FeatureExtractor fex;
    TetrisAgent agent;
    JTextArea textarea;
    public int nFeatures;
    PrintStream evalFile;
    String outputname, inname;
    int numgames;
    double avg;
    AfterState startState = null;
    int statenum;

    public boolean isLogging = false;
//    Vector<XP> xpList = new Vector<XP>();
    Vector<double[]> Phi = new Vector<double[]>();
    Vector<double[]> PhiNext = new Vector<double[]>();
    Vector<Double> rew = new Vector<Double>();
    Vector<Boolean> isTerminal = new Vector<Boolean>();

    public Evaluator(FeatureExtractor fex, String inname, int numgames)
    {
        fileexists = (new File(inname)).exists();
        this.fex = fex;
        if (!fileexists)
        {
            throw new RuntimeException("Evaluator: " + inname + " does not exist");
        }
        agent = new VIAgent(10,20,fex);
        ((VIAgent)agent).loadWeights(inname);
        String outputname = inname.replace(".txt", "");
        this.inname = inname;
        init(agent, outputname, numgames);

    }

    public Evaluator(FeatureExtractor fex, int run, int numgames)
    {
        inname = String.format("CE_%s_run%02d_formatlab.txt", fex.name, run);
        fileexists = (new File(inname)).exists();

        this.fex = fex;

        if (!fileexists)
        {
            throw new RuntimeException("Evaluator: " + inname + " does not exist");
        }
        agent = new VIAgent(10,20,fex);
        ((VIAgent)agent).loadWeights(inname);
        String outputname = String.format("CE_%s_run%02d", fex.name, run);
        init(agent, outputname, numgames);

    }

    public Evaluator(TetrisAgent agent, String outputname, int numgames)
    {
        this.agent = agent;
        init(agent,outputname, numgames);
    }

    public void init(TetrisAgent agent, String outputname, int numgames)
    {
        this.outputname = outputname;
        this.numgames = numgames;
        scorelist = new double[numgames];
        game = new TetrisGame(10,20);
    }


    public double evaluate()
    {
        avg = 0;
        for (int it=0; it<numgames; it++)
        {
            if (startState==null)
                scorelist[it] = game.playOneGame(agent);
            else
                scorelist[it] = game.playOneGame(agent, startState);
            if (isLogging)
            {
                TetrisAgent ag = agent;
                double value = scorelist[it];
                for(int i=0; i<ag.xpList.size(); i++)
                {
                    XP xp = ag.xpList.get(i);

                    if (xp.r>=0 )
                        value -= xp.r;
                    xp.mcvalue = value;
                    Phi.add(xp.phi);
                    if (!xp.isTerminal)
                        PhiNext.add(xp.phinext.get(xp.aind));
                    else
                    	PhiNext.add(new double[xp.phi.length]);
                    rew.add(xp.r);
                    isTerminal.add(xp.isTerminal);
                }
            }
            System.out.printf("%s:  %4d/%4d \t  %d\n", outputname, it+1,numgames, (int) scorelist[it]);
            avg += scorelist[it];
        }
        avg /= numgames;
        System.out.printf("Average: %f", avg);
        printResults();
        return avg;
    }

    public void printResults()
    {
        String fname = outputname;
        if (startState != null)
            fname += "_s"+statenum;
        fname += "_eval.txt";
        try
        {
            evalFile = new PrintStream(fname);
            evalFile.println(numgames);
            evalFile.println(avg);
            for (int it=0; it<numgames; it++)
            {
                evalFile.println(scorelist[it]);
            }
            evalFile.close();

        }
        catch (FileNotFoundException ex)
        {
            Logger.getLogger(Evaluator.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void setStartState(AfterState s, int statenum)
    {
        startState = s;
        this.statenum = statenum;
    }

//	public void loadXPlist(String filePath)
//	{
//		xpList.clear();
//		//String filePath = "xpList.dat";
//		FileInputStream fis=null;
//		ObjectInputStream inStream;
//		try {
//			fis = new FileInputStream( filePath );
//		} catch (FileNotFoundException e) {
//			return;
//		}
//	    try {
//			inStream = new ObjectInputStream( fis );
//			
//			int size = inStream.readInt();			
//			for (int i=0; i<size; i++)
//			{
//				XP xp = (XP)inStream.readObject();
//				xpList.add(xp);
//			}
//			
//			inStream.close();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//	}
//
//    public void saveXPlist(String filePath)
//	{
//		int n = 0;
//		//String filePath = "xplist.dat";
//		FileOutputStream fos=null;
//		ObjectOutputStream outStream;
//		try {
//			fos = new FileOutputStream( filePath );
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		}
//	    try {
//			outStream = new ObjectOutputStream( fos );
//
//			outStream.writeInt(xpList.size());
//			for (XP xp : xpList)
//			{
//				outStream.writeObject(xp);
//				n++;
//				if (n%1000 == 0) //needed to prevent memory overflow
//					outStream.reset();
//			}
//
//			outStream.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}


//    double[][] Phi;
//    double[][] PhiNext;
//    double[] rew;
//    boolean[] isTerminal;

    public void createLSTDmatrices(String filePath)
    {
        int N = Phi.size();
        int nFeatures = Phi.get(0).length;

        //System.out.println();
        
		int n = 0;
		//String filePath = "xplist.dat";
		FileOutputStream fos=null;
		ObjectOutputStream outStream;
		try {
			fos = new FileOutputStream( filePath );
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	    try {
			outStream = new ObjectOutputStream( fos );
            outStream.writeObject(Phi);
            outStream.writeObject(PhiNext);
            outStream.writeObject(rew);
            outStream.writeObject(isTerminal);
		} catch (IOException e) {
			e.printStackTrace();
		}

    }
    
	public void loadLSTDmatrices(String filePath)
	{
		//String filePath = "xpList.dat";
		FileInputStream fis=null;
		ObjectInputStream inStream;
		try {
			fis = new FileInputStream( filePath );
		} catch (FileNotFoundException e) {
			return;
		}
	    try {
			inStream = new ObjectInputStream( fis );

			Phi = (Vector<double[]>)inStream.readObject();
            PhiNext = (Vector<double[]>)inStream.readObject();
            rew = (Vector<Double>)inStream.readObject();
            isTerminal = (Vector<Boolean>)inStream.readObject();
            inStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

    public void doLSPolicyEval(String fname, double lambda)
    {
        Matrix weightnew;
        int nSamples = Phi.size();
        int nFeatures = Phi.get(0).length;
        int MAXIT = 200;

        fileexists = (new File(fname)).exists();
        if (fileexists)
        	return;
        System.out.println("---LS policy evaluation started ");
//        Matrix weight = new Matrix(agent.w,nFeatures);
//        double [] w = Arrays.copyOf(agent.w, nFeatures);
        Matrix weight = new Matrix(nFeatures,1);

        Matrix rhs = new Matrix(nSamples,1);
        Matrix H = new Matrix(nSamples,nFeatures);
        Matrix Hnext = new Matrix(nSamples,nFeatures);
        Matrix reward = new Matrix(nSamples,1);
        double[] z = new double[nFeatures];
        double rcum = 0;
        for (int x=nSamples-1; x>=0; x--)
        {
//            if (x<10)
//            System.out.println(Arrays.toString(PhiNext[x]));
            if (isTerminal.get(x))
            {
                z = new double[nFeatures];
                rcum = 0;
            }
            else
                rcum = rcum*lambda + rew.get(x);

            for (int y=0; y<nFeatures; y++)
            {
                H.set(x, y, Phi.get(x)[y]);
                z[y] = z[y]*lambda +(1-lambda)*PhiNext.get(x)[y];
                Hnext.set(x, y, z[y]);
            }
            reward.set(x, 0, rcum);

        }
//        for (int i=0; i<nSamples; i++)
//            System.out.print((int)rew[i]);
//        System.out.print("\nr: " + MatrixToString(reward));
        //System.out.println("--- Matrix generation ended");


        double EPSILON = 1e-2;
        double Delta=0.0;
        int it;
        for (it=0; it<MAXIT; it++)
        {
//            rhs = reward;
            rhs= reward.plus(Hnext.times(weight));
            Delta=0.0;
//            for (int x=0; x<nSamples; x++)
//            {
//                rhs.set(x, 0, Hnext.times(weight));
//            }
            //System.out.println(MatrixToString(H));
            System.out.println(""+ it +" rhs: " + MatrixToString(rhs.getMatrix(0, 100, 0, 0)));

            weightnew = (H.solve(rhs));
            Delta = weightnew.minus(weight).normInf();
            weight = weightnew;
            if (Delta<EPSILON)
                break;
            //System.out.print(MatrixToString(weight));
            //System.out.println(Arrays.toString(w));
        }
        System.out.println(String.format("--- LSTD(%4f) ended in %d iterations, accuracy: %f",lambda, it, Delta));
        System.out.println(String.format("nSamples: %d", nSamples));
        System.out.println("w: " + MatrixToString(weight));
        //System.out.println("rhs: " + MatrixToString(rhs));
        //agent.setWeights(w);

        PrintStream fileForMatlab = null;
        //fileexists = false;
        if (!fileexists)
        {
            try
            {
                fileForMatlab = new PrintStream(fname);
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(CEtrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fileForMatlab.printf((Locale)null,
                    "%d %d %d\n",
                    10, 20, nFeatures);

            String str = String.format((Locale)null,"%d %8.4f %d ", 0, 0.0, 0);
            fileForMatlab.print(str);
            str = "";
            for (int i=0; i<nFeatures; i++)
                str = str+ String.format((Locale)null, "%8.4f ", weight.get(i, 0));
            System.out.println(str);
            fileForMatlab.print(str);
            fileForMatlab.close();
        }

    }

    public void perceptronLearn(TetrisAgent teacher, String fname)
    {
        PerceptronLearner pl = new PerceptronLearner(fex);
        agent = new PercLearnerAgent(10,20,fex);
        nFeatures = fex.nFeatures;
        //((PercLearnerAgent)agent).loadWeights(inname);
        for (int it=0; it<numgames; it++)
        {
   //         game.playOneGame((PercLearnerteacher)teacher);
            teacher.xpList.clear();
            int res;
            double totalRew = 0.0;

            int piece = game.newPiece();
            game.clearBoard();
            teacher.NewGame(piece, game.state.board);
            while (true)
            {
                TetrisAction a = teacher.act();
                game.state = game.state.putTile(piece, a.rot, a.pos, true);
                res = game.state.lastResult;
                if (res ==-1)
                {
                    teacher.GameOver();
                    break;
                }
                else
                {
                    pl.addSample(new AfterState(teacher.agentstate, game.width,game.height), piece, a);
                    totalRew += res;
                    piece = game.newPiece();
                    teacher.getObservation(res, piece, game.state.board);
                }

            }
            System.out.printf("%s:  %4d/%4d \t  %d\n", outputname, it+1,numgames, (int) totalRew);
        }

        fileexists = (new File(fname)).exists();
        PrintStream fileForMatlab = null;
        fileexists = false;
        if (!fileexists)
        {
            try
            {
                fileForMatlab = new PrintStream(fname);
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(CEtrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fileForMatlab.printf((Locale)null,
                    "%d %d %d\n",
                    10, 20, nFeatures);

            String str = String.format((Locale)null,"%d %8.4f %d ", 0, 0.0, 0);
            fileForMatlab.print(str);
            str = "";
            for (int i=0; i<nFeatures; i++)
                str = str+ String.format((Locale)null, "%8.4f ", pl.weights.get(i, 0));
            System.out.println(str);
            fileForMatlab.print(str);
            fileForMatlab.close();
        }

//        //fileForHuman.println(str);
//        str = String.format((Locale)null,"%d %d ", generation,  (int)(sortedscores[0]));
//        //fileForMatlab.print(str);

//        str = "";
//        for (i=0; i<nFeatures; i++)
//            str = str+ String.format((Locale)null, "%8.4f ", M[i]);
//        System.out.println(str);
//        fileForMatlab.print(str);
//        str = "";
//        for (i=0; i<nFeatures; i++)
//            str = str+ String.format((Locale)null,"%8.4f ", S[i]);
//        System.out.println(str);
//        fileForMatlab.print(str);
//        System.out.println("--------------------------------------");
//        fileForMatlab.println();

    }

    static public String MatrixToString(Matrix A)
    {
        StringBuffer s = new StringBuffer("");
        if (A.getColumnDimension()==1)
            A=A.transpose();
        int m = A.getRowDimension();
        int n = A.getColumnDimension();
        double[][] array = A.getArray();
        for (int i=0; i<m; i++)
        {
            for (int j=0; j<n; j++)
            {
                s=s.append(String.format("%.4f ", array[i][j]));
            }
            if (i<m-1)
                s = s.append("\n");
        }
        return s.toString();
    }
    
}
