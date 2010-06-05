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
import agent.KwikLinearLearner;
import agent.TetrisAgent;
import agent.VIAgent;
import agent.XP;
import java.io.File;
import java.io.FileNotFoundException;
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
import tetrisengine.TetrisGame;

/**
 *
 * @author szityu
 */
public class VItrainer implements Trainer {
    Random rnd;
    int POPULATIONSIZE = 50;
    double [] scorelist;
    double [] truescorelist;
    double [] sortedscores;
    
    
    int generation;
    int it;
    boolean fileexists;
    
    TetrisGame game;
    FeatureExtractor fex;
    VIAgent agent;
    Vector<XP> xpList;

    JTextArea textarea;
    public int nFeatures;
    PrintStream fileForMatlab, fileForHuman;
    public int initPeriod;
    public int learnMethod;
    public String initFname;

    public static final int LM_NOTHING  = 0;
    public static final int LM_VI       = 1;
    public static final int LM_MCPE     = 2;
    public static final int LM_LSPE     = 3;

    public VItrainer(int width, FeatureExtractor fex, int initPeriod, String initFname, int learnMethod)
    {
        int i;
        rnd = new Random();
        game = new TetrisGame(width, 20);
        //fex = new BertsekasIoffeFeatureExtractor(game);
        //fex = new ExtendedFeatureExtractor(game);
        //agent = new FeatureUserAgent(game.width,game.height,fex);
        agent = new VIAgent(game.width,game.height,fex);
        nFeatures = fex.nFeatures;
        xpList = new Vector<XP>();

        generation = 0;
        it = 0;

        String fname = String.format("tetrisVI_formatlab.txt", game.width);
        //fileexists = (new File(fname)).exists();
        fileexists = false;
        if (!fileexists)
        {
            try 
            {            
                fileForMatlab = new PrintStream(fname);
            } 
            catch (FileNotFoundException ex) 
            {
                Logger.getLogger(VItrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fname = String.format("tetris%d_forhuman.txt", game.width);
            try 
            {            
                fileForHuman = new PrintStream(fname);
            } 
            catch (FileNotFoundException ex) 
            {
                Logger.getLogger(VItrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fileForHuman.printf((Locale)null, 
                    "Training data for %d*%d table, %d basis functions \n---------------------------\n\n",
                    game.width, game.height, nFeatures);
            fileForMatlab.printf((Locale)null, 
                    "%d %d %d\n",
                    game.width, game.height, nFeatures);
        }
        this.initFname = initFname;
        this.initPeriod = initPeriod;
        this.learnMethod = learnMethod;
    }
    
    @Override
    protected void finalize()
    {
        if (!fileexists)
        {
            fileForMatlab.close();
            fileForHuman.close();
        }
    }
    
    public VItrainer(int width, FeatureExtractor fex,  int initPeriod, String initFname, int learnMethod, JTextArea textarea)
    {
        this(width, fex, initPeriod, initFname, learnMethod);
        this.textarea = textarea;
        scorelist = new double[POPULATIONSIZE];
        truescorelist = new double[POPULATIONSIZE];
        sortedscores = new double[POPULATIONSIZE];
    }
    
    
    public void playGeneration()
    {
        String str;

        if (generation>initPeriod)
        {
            if ((learnMethod == LM_MCPE) && generation==initPeriod+1)
                doMCPolicyEval();
            if ((learnMethod == LM_LSPE) && generation==initPeriod+1)
                doLSPolicyEval();
//            doConcAVI();
        }
        else
        {
            agent.loadWeights(initFname);
            System.out.print("w: " + Arrays.toString(agent.w));

        }
        if (fileexists)
            return;
        for (it=0; it<POPULATIONSIZE; it++)
        {
            scorelist[it] = game.playOneGame(agent);
            double value = scorelist[it];
            for(int i=0; i<agent.xpList.size(); i++)
            {
                XP xp = agent.xpList.get(i);

                //if (!agent.kwik.isKnown(xp.phi))
                {
                    if (xp.aind != -1)
                        value -= agent.rList.get(i);
                        //value -= xp.ra.get(xp.aind);
                    //System.out.print(value + " ");
                    xp.mcvalue = value;
                    xpList.add(xp);
                    agent.kwik.addSample(xp.phi);
                    //System.out.println(Arrays.toString(xp.phi));
                }
            }
            //xpList.addAll(agent.xpList);
            truescorelist[it] = scorelist[it];
            sortedscores[it] = scorelist[it];
            str = "gen."+generation+"  #"+it+"   "+(int)(truescorelist[it]) 
                    + "  (approx: " + (int)(scorelist[it]) + ")";
            System.out.println(str);
        }
        

        double avg = 0;
        for (it=0; it<POPULATIONSIZE; it++)
        {
            avg += sortedscores[it];
        }
        avg /= POPULATIONSIZE;
        System.out.println("--------------------------------------");
        str = "gen.#"+generation 
                + "\t  avgScore:"+(int)(avg) ;

        if (textarea != null)
        {
            textarea.append("w"+game.width+"  "+str+"\n");
            JScrollPane scrollpane = (JScrollPane) textarea.getParent().getParent();
            JScrollBar scrollbar = scrollpane.getVerticalScrollBar();        
            textarea.validate();
            scrollbar.validate();
            scrollbar.setValue(scrollbar.getMaximum());
            scrollbar.validate();
                    
        }
        System.out.println("w"+game.width+"  "+str);
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
        
        
        generation++;
    }

    public void doAVI()
    {
        double c = 2.0;

        Matrix weightnew;
        int nSamples = xpList.size();

        System.out.println("--- value iteration started");
        Matrix weight = new Matrix(agent.w,nFeatures);
        double [] w = Arrays.copyOf(agent.w, nFeatures);
//        Matrix weight = new Matrix(nFeatures,1);
//        double[] w = new double[nFeatures];

        Matrix rhs = new Matrix(nSamples,1);
        Matrix H = new Matrix(nSamples,nFeatures);
        int[] known = new int[nSamples];
        for (int x=0; x<nSamples; x++)
        {
            known[x] = (agent.kwik.isKnown(xpList.get(x).phi))?1:0;
            for (int y=0; y<nFeatures; y++)
            {
                H.set(x, y, xpList.get(x).phi[y]);
            }
        }
        //System.out.println("--- Matrix generation ended");


        int MAXIT = 1000;
        double EPSILON = 1e-1;
        double Delta=0.0;
        int it;
        int nKnown=0;
        int nUnknown = 0;
        for (it=0; it<MAXIT; it++)
        {
            Delta=0.0;
            for (int x=0; x<nSamples; x++)
            {
                if (known[x]==1)
                {
                    rhs.set(x, 0, Math.min(xpList.get(x).getVal(w),agent.VMAX*10000));
                    if (it==0) nKnown++;
                }
                else
                {
                    rhs.set(x, 0, Math.min(xpList.get(x).getVal(w)+agent.VMAX,agent.VMAX*10000));
                    if (it==0) nUnknown++;
                }
            }
            //System.out.println(MatrixToString(H));
            //System.out.println("rhs: " + MatrixToString(rhs));

            weightnew = (H.solve(rhs)).times(0.9);
            Delta = weightnew.minus(weight).normInf();
            weight = weightnew;
            w = weight.getColumnPackedCopy();
            //System.out.print(MatrixToString(weight));
            if (Delta<EPSILON)
                break;
//            if (it%20==0)
//            {
//                System.out.println(String.format("...it.%d, accuracy: %f", it, Delta));
//            }
        }
        System.out.println(String.format("--- value iteration ended in %d iterations, accuracy: %f",it, Delta));
        System.out.println(String.format("nSamples: %d, known: %d, unknown: %d", nSamples, nKnown, nUnknown));
        System.out.print("w: " + MatrixToString(weight));
        System.out.println("rhs: " + MatrixToString(rhs));
        System.out.println("known: " + Arrays.toString(known));
        agent.setWeights(w);
    }

    public void doConcAVI()
    {
        Matrix weightnew;
        int nSamples = xpList.size();

        System.out.println("--- Concentrated value iteration started");
        Matrix weight = new Matrix(agent.w,nFeatures);
        double [] w = Arrays.copyOf(agent.w, nFeatures);

        Matrix rhs = new Matrix(nSamples,1);
        Matrix H = new Matrix(nSamples,nFeatures);
        int[] known = new int[nSamples];
        for (int x=0; x<nSamples; x++)
        {
            known[x] = (agent.kwik.isKnown(xpList.get(x).phi))?1:0;
            for (int y=0; y<nFeatures; y++)
            {
                H.set(x, y, xpList.get(x).phi[y]);
            }
        }
        //System.out.println("--- Matrix generation ended");


        double PERCENTILE = 0.55;
        int MAXIT = 1000;
        double EPSILON = 1e-1;
        double Delta=0.0;
        int it;
        int nKnown=0;
        int nUnknown = 0;
        for (it=0; it<MAXIT; it++)
        {
            Delta=0.0;
            for (int x=0; x<nSamples; x++)
            {
                if (known[x]==1)
                {
                    rhs.set(x, 0, Math.min(xpList.get(x).getVal(w),agent.VMAX*10000));
                    if (it==0) nKnown++;
                }
                else
                {
                    rhs.set(x, 0, Math.min(agent.VMAX+xpList.get(x).getVal(w), agent.VMAX*10000));
                    if (it==0) nUnknown++;
                }
            }
            //System.out.println(MatrixToString(H));
            //System.out.println("rhs: " + MatrixToString(rhs));
            double[] sortedRhs = rhs.getColumnPackedCopy();
            Arrays.sort(sortedRhs);
            double threshold = sortedRhs[(int)(nSamples*PERCENTILE)];
            int nRedSamples = 0;
            for (int i=0; i<nSamples; i++)
            {
                if (sortedRhs[i]>= threshold)
                    nRedSamples++;
            }
            Matrix redH = new Matrix(nRedSamples,nFeatures);
            Matrix redRhs = new Matrix(nRedSamples,1);
            int ind = 0;
            for (int x=0; x<nSamples; x++)
            {
                if (rhs.get(x, 0) < threshold)
                    continue;
                redRhs.set(ind, 0, rhs.get(x, 0));
                for (int y=0; y<nFeatures; y++)
                {
                    redH.set(ind, y, H.get(ind,y));
                }
                ind++;
            }

            weightnew = (redH.solve(redRhs)).times(0.9);
            Delta = weightnew.minus(weight).normInf();
            weight = weightnew;
            w = weight.getColumnPackedCopy();
            //System.out.print(MatrixToString(weight));
            if (Delta<EPSILON)
                break;
//            if (it%20==0)
//            {
//                System.out.println(String.format("...it.%d, accuracy: %f", it, Delta));
//            }
        }
        System.out.println(String.format("--- value iteration ended in %d iterations, accuracy: %f",it, Delta));
        System.out.println(String.format("nSamples: %d, known: %d, unknown: %d", nSamples, nKnown, nUnknown));
        System.out.print("w: " + MatrixToString(weight));
        System.out.println("rhs: " + MatrixToString(rhs));
        System.out.println("known: " + Arrays.toString(known));
        agent.setWeights(w);
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
                s=s.append(String.format("%.2f  ", array[i][j]));
            }
            s = s.append("\n");
        }
        return s.toString();
    }

    public void doMCPolicyEval() {
        Matrix weightnew;
        int nSamples = xpList.size();

        System.out.println("---MC policy evaluation started");
        Matrix weight = new Matrix(agent.w,nFeatures);
        double [] w = Arrays.copyOf(agent.w, nFeatures);
//        Matrix weight = new Matrix(nFeatures,1);
//        double[] w = new double[nFeatures];

        Matrix rhs = new Matrix(nSamples,1);
        Matrix H = new Matrix(nSamples,nFeatures);
        int[] known = new int[nSamples];
        for (int x=0; x<nSamples; x++)
        {
            for (int y=0; y<nFeatures; y++)
            {
                H.set(x, y, xpList.get(x).phi[y]);
            }
        }
        //System.out.println("--- Matrix generation ended");


        double EPSILON = 1e-1;
        double Delta=0.0;
        int it;
        int nKnown=0;
        int nUnknown = 0;
        {
            Delta=0.0;
            for (int x=0; x<nSamples; x++)
            {
                //rhs.set(x, 0, xpList.get(x).getVal(w));
                rhs.set(x, 0, xpList.get(x).mcvalue);
                //System.out.println(xpList.get(x).mcvalue);
            }
            //System.out.println(MatrixToString(H));
            System.out.println("rhs: " + MatrixToString(rhs));

            weightnew = (H.solve(rhs));
            Delta = weightnew.minus(weight).normInf();
            weight = weightnew;
            w = weight.getColumnPackedCopy();
            //System.out.print(MatrixToString(weight));
        }
        System.out.println(String.format("--- MCPE ended"));
        System.out.println(String.format("nSamples: %d, known: %d, unknown: %d", nSamples, nKnown, nUnknown));
        System.out.print("w: " + MatrixToString(weight));
        System.out.println("rhs: " + MatrixToString(rhs));
        agent.setWeights(w);
    }

    public void doLSPolicyEval()
    {
        Matrix weightnew;
        int nSamples = xpList.size();
        int MAXIT = 2000;

        System.out.println("---LS policy evaluation started");
//        Matrix weight = new Matrix(agent.w,nFeatures);
//        double [] w = Arrays.copyOf(agent.w, nFeatures);
        Matrix weight = new Matrix(nFeatures+1,1);
        double[] w = new double[nFeatures+1];

        Matrix rhs = new Matrix(nSamples,1);
        Matrix H = new Matrix(nSamples,nFeatures);
        int[] known = new int[nSamples];
        for (int x=0; x<nSamples; x++)
        {
            for (int y=0; y<nFeatures; y++)
            {
                H.set(x, y, xpList.get(x).phi[y]);
            }
        }
        //System.out.println("--- Matrix generation ended");


        double EPSILON = 1e-1;
        double Delta=0.0;
        int it;
        int nKnown=0;
        int nUnknown = 0;
        for (it=0; it<MAXIT; it++)
        {
            Delta=0.0;
            for (int x=0; x<nSamples; x++)
            {
                rhs.set(x, 0, xpList.get(x).getPolicyVal(w));
                //rhs.set(x, 0, xpList.get(x).mcvalue);
                //System.out.println(xpList.get(x).mcvalue);
            }
            //System.out.println(MatrixToString(H));
            System.out.println("rhs: " + MatrixToString(rhs));

            weightnew = (H.solve(rhs));
            Delta = weightnew.minus(weight).normInf();
            weight = weightnew;
            w = weight.getColumnPackedCopy();
            //System.out.print(MatrixToString(weight));
        }
        System.out.println(String.format("--- LSPE ended in %d iterations, accuracy: %f",it, Delta));
        System.out.println(String.format("nSamples: %d, known: %d, unknown: %d", nSamples, nKnown, nUnknown));
        System.out.print("w: " + MatrixToString(weight));
        System.out.println("rhs: " + MatrixToString(rhs));
        agent.setWeights(w);
    }

}
