/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trainer;

import Jama.Matrix;
import agent.BertsekasIoffeFeatureExtractor;
import agent.DifferenceFeatureExtractor;
import agent.FeatureExtractor;
import agent.FeatureUserAgent;
import agent.KwikLinearLearner;
import agent.TetrisAgent;
import agent.UCTAgent;
import agent.UCTtree;
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
public class UCTtrainer implements Trainer {
    Random rnd;
    int POPULATIONSIZE = 1000;
    double [] scorelist;
    double [] truescorelist;
    double [] sortedscores;
    
    
    int generation;
    int it;
    boolean fileexists;
    
    TetrisGame game;
    FeatureExtractor fex;
    UCTAgent agent;
    //Vector<XP> xpList;
    UCTtree tree;

    JTextArea textarea;
    //public int nFeatures;
    PrintStream fileForMatlab, fileForHuman;
    
    public UCTtrainer(int width)
    {
        int i;
        rnd = new Random();
        game = new TetrisGame(width, 20);
        fex = new BertsekasIoffeFeatureExtractor(game);
        //fex = new DifferenceFeatureExtractor(game);
        tree = new UCTtree();
        agent = new UCTAgent(game.width,game.height,tree, fex);
        //nFeatures = fex.nFeatures;
        //xpList = new Vector<XP>();

        generation = 0;
        it = 0;

        String fname = String.format("tetris%d_formatlab.txt", game.width);
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
                Logger.getLogger(UCTtrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
            fname = String.format("tetris%d_forhuman.txt", game.width);
            try 
            {            
                fileForHuman = new PrintStream(fname);
            } 
            catch (FileNotFoundException ex) 
            {
                Logger.getLogger(UCTtrainer.class.getName()).log(Level.SEVERE, null, ex);
            }
//            fileForHuman.printf((Locale)null,
//                    "Training data for %d*%d table, %d basis functions \n---------------------------\n\n",
//                    game.width, game.height, nFeatures);
//            fileForMatlab.printf((Locale)null,
//                    "%d %d %d\n",
//                    game.width, game.height, nFeatures);
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
    
    public UCTtrainer(int width, JTextArea textarea)
    {
        this(width);
        this.textarea = textarea;
        scorelist = new double[POPULATIONSIZE];
        truescorelist = new double[POPULATIONSIZE];
        sortedscores = new double[POPULATIONSIZE];
    }
    
    
    public void playGeneration()
    {
        int i, j;
        String str;
        boolean newstate = false, addstates = true;

        if (fileexists)
            return;
        for (it=0; it<POPULATIONSIZE; it++)
        {
            scorelist[it] = game.playOneGame(agent);
            tree.clearTraces();
            for(i=0; i<agent.xpList.size(); i++)
            {
                XP xp = agent.xpList.get(i);
                addstates = true;
                //if (!agent.kwik.isKnown(xp.phi))
                if (addstates)
                {
                    newstate = tree.addState(xp.b, xp.piece, xp.actions);
                    if (newstate)
                        addstates = false;
                    tree.addTrace(xp.b, xp.piece, xp.aind);
                    //agent.kwik.addSample(xp.phi);
                }
            }
            tree.update(scorelist[it], 0);
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



}
