/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trainer;

import agent.BertsekasIoffeFeatureExtractor;
import agent.DifferenceFeatureExtractor;
import agent.ExtendedFeatureExtractor;
import agent.FeatureExtractor;
import agent.FeatureUserAgent;
import agent.ScherrerThieryFeatureExtractor;
import agent.TetrisAgent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Locale;
import java.util.Random;
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
public class CEtrainer implements Trainer {
    Random rnd;
    
    int POPULATIONSIZE = 1000;
    double ALPHA = 0.6;
    double RHO = 0.01;
    
    double [][] wlist;
    double [] scorelist;
    double [] truescorelist;
    double [] sortedscores;
    double [] M, Mnew;
    double [] S, Snew;
    double smean;
    
    int generation;
    int it;
    boolean fileexists;
    
    TetrisGame game;
    FeatureExtractor fex;
    FeatureUserAgent agent;
    JTextArea textarea;
    public int nFeatures;
    PrintStream fileForMatlab, fileForHuman;
    
    public CEtrainer(int width, FeatureExtractor fex, int run)
    {
        int i;
        rnd = new Random();
        game = new TetrisGame(width, 20);
        this.fex = fex;
        //fex = new BertsekasIoffeFeatureExtractor(game);
        agent = new FeatureUserAgent(game.width,game.height,fex);
        nFeatures = fex.nFeatures;
        wlist = new double[POPULATIONSIZE][nFeatures];
        scorelist = new double[POPULATIONSIZE];
        truescorelist = new double[POPULATIONSIZE];
        sortedscores = new double[POPULATIONSIZE];
        M = new double[nFeatures];
        S = new double[nFeatures];
        Mnew = new double[nFeatures];
        Snew = new double[nFeatures];
        //game.LoadInitValues();
        for (i = 0; i < nFeatures; i++) {
            //M[i] = 0.0;
            S[i] = 100.0;
        }
        //M[2*game.width] = -50.0;
        

        generation = 0;
        it = 0;

        String fname = String.format("CE_%s_run%02d_formatlab.txt", fex.name, run);
        System.out.println(fname);
        fileexists = (new File(fname)).exists();
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
            fname = String.format("CE_%s_run%02d_forhuman.txt", fex.name, run);
            try
            {
                fileForHuman = new PrintStream(fname);
            }
            catch (FileNotFoundException ex)
            {
                Logger.getLogger(CEtrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fileForHuman.printf((Locale)null,
                    "Training data for %d*%d table, %d basis functions \n---------------------------\n\n",
                    game.width, game.height, nFeatures);
            fileForMatlab.printf((Locale)null,
                    "%d %d %d\n",
                    game.width, game.height, nFeatures);
        }
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
    
    public CEtrainer(int width,  FeatureExtractor fex, int run, JTextArea textarea)
    {
        this(width, fex, run);
        this.textarea = textarea;
    }
    
        // quicksort a[left] to a[right]
    public void quicksort(double[] a, int left, int right) {
        if (right <= left) return;
        int i = partition(a, left, right);
        quicksort(a, left, i-1);
        quicksort(a, i+1, right);
    }

    // partition a[left] to a[right], assumes left < right
    @SuppressWarnings("empty-statement")
    private static int partition(double[] a, int left, int right) {
        int i = left - 1;
        int j = right;
        while (true) {
            while (a[++i] > a[right])      // find item on left to swap
                ;                               // a[right] acts as sentinel
            while (a[right] > a[--j])      // find item on right to swap
                if (j == left) break;           // don't go out-of-bounds
            if (i >= j) break;                  // check if pointers cross
            exch(a, i, j);                      // swap two elements into place
        }
        exch(a, i, right);                      // swap with partition element
        return i;
    }

 
    // exchange a[i] and a[j]
    private static void exch(double[] a, int i, int j) {
//        exchanges++;
        double swap = a[i];
        a[i] = a[j];
        a[j] = swap;
    }
    
    public void playGeneration()
    {
        int i, j;
        String str;
        double[] lambda = new double[1];
        
        if (fileexists)
            return;
        for (it=0; it<POPULATIONSIZE; it++)
        {
            for (i=0; i<nFeatures; i++)
            {
                wlist[it][i] = Math.sqrt(S[i])*rnd.nextGaussian() + M[i];
            }
            agent.setWeights(wlist[it]);
            // add a small random value to make keys unique
            //scorelist[it] = game.playNSteps(wlist[it], 1000) + 0.001*rnd.nextDouble();
//            scorelist[it] = game.playOneGameCapped(wlist[it], 10000, lambda) + 0.001*rnd.nextDouble();
            scorelist[it] = game.playOneGame(agent);
            truescorelist[it] = scorelist[it];
            sortedscores[it] = scorelist[it];
            str = "gen."+generation+"  #"+it+"   "+(int)(truescorelist[it]) 
                    + "  (approx: " + (int)(scorelist[it]) + ")";
            System.out.println(str);
        }
        
        //sort, get gamma, update M,S
        quicksort(sortedscores, 0, POPULATIONSIZE-1);
        
        int NELITE = (int) (POPULATIONSIZE * RHO);
        double GAMMA = sortedscores[NELITE-1];
        //double noise = Math.max(0,50*(1-generation/50.0));
        double noise = 0;
        smean = 0;
        for (i=0; i<nFeatures; i++)
        {
            Mnew[i] = 0;
            for (it=0; it<POPULATIONSIZE; it++)
                if (scorelist[it]>=GAMMA)
                    Mnew[i] += wlist[it][i];
            Mnew[i] /= NELITE;

            Snew[i] = 0;
            for (it=0; it<POPULATIONSIZE; it++)
                if (scorelist[it]>=GAMMA)
                    Snew[i] += (wlist[it][i]-Mnew[i])*(wlist[it][i]-Mnew[i]);
            Snew[i] /= NELITE;
            Snew[i] += noise;
            
            M[i] = (1-ALPHA)*M[i] + ALPHA*Mnew[i];
            S[i] = (1-ALPHA)*S[i] + ALPHA*Snew[i];
            smean += S[i];
        }
        smean /= nFeatures;

        double avg = 0;
        for (it=0; it<POPULATIONSIZE; it++)
        {
            avg += sortedscores[it];
        }
        avg /= POPULATIONSIZE;
        System.out.println("--------------------------------------");
        str = "gen.#"+generation+" \tmeanS:"+ String.format((Locale)null,"%8.4f ", smean) 
                + "\t  avgScore:"+(int)(avg) ;
        System.out.println(Arrays.toString(Mnew));
        System.out.println(Arrays.toString(Snew));

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
        fileForHuman.println(str);
        str = String.format((Locale)null,"%d %8.4f %d ", generation, smean, (int)(sortedscores[0]));
        fileForMatlab.print(str);
        
        str = "";
        for (i=0; i<nFeatures; i++)
            str = str+ String.format((Locale)null, "%8.4f ", M[i]);
        System.out.println(str);
        fileForMatlab.print(str);
        str = "";
        for (i=0; i<nFeatures; i++)
            str = str+ String.format((Locale)null,"%8.4f ", S[i]);
        System.out.println(str);
        fileForMatlab.print(str);
        System.out.println("--------------------------------------");
        fileForMatlab.println();
        
        
        generation++;
    }
}
