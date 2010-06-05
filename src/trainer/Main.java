/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trainer;

import agent.BertsekasIoffeFeatureExtractor;
import agent.ExtendedFeatureExtractor;
import agent.FeatureExtractor;
import agent.FeatureUserAgent;
import agent.HandCodedForSZAgent;
import agent.Options;
import agent.QlearningAgent;
import agent.ScherrerThieryFeatureExtractor;
import agent.TetrisAgent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import tetrisengine.TetrisGame;

/**
 *
 * @author istvanszita
 */
public class Main {

    public static double[] lambdalist;

    static{
        int N = 10;
        lambdalist = new double[N+1];
        double l;
        lambdalist[0] = 1.0;
        for (int i=1; i<=N; i++)
        {
            lambdalist[i] = 1.0 - 1.0/i;
        }

    }

    public static void createSamples()
    {
        Trainer trainer;
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;

        fex = new BertsekasIoffeFeatureExtractor(game);
//        fex = new ExtendedFeatureExtractor(game);
        Evaluator ev = new Evaluator(fex, 0, 100);
        ev.isLogging = true;
        ev.evaluate();
        //ev.saveXPlist("xplist.dat");
        ev.createLSTDmatrices("lstddata_BI.dat");
    }

    public static void lstdSolve()
    {
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;

        fex = new BertsekasIoffeFeatureExtractor(game);
//        fex = new ExtendedFeatureExtractor(game);
        Evaluator ev = new Evaluator(fex, 0, 100);
        ev.loadLSTDmatrices("lstddata_BI.dat");
        for (int i =0; i<lambdalist.length; i++)
        {
            double lambda = lambdalist[i];
            String fname = String.format((Locale)null,"BI_lambda%.4f.txt",lambda);
            ev.doLSPolicyEval(fname,lambda);
        }
    }


    public static void perceptronSolve()
    {
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;
        String fname;

        fex = new ExtendedFeatureExtractor(game);
        Evaluator ev = new Evaluator(fex, 0, 500);
        TetrisAgent ag1 = new FeatureUserAgent(10,20,fex);
        ((FeatureUserAgent)ag1).loadWeights(ev.inname);
        fname = String.format((Locale)null,"XT_perceptron.txt");
        ev.perceptronLearn(ag1,fname);


//        fex = new ExtendedFeatureExtractor(game);
//        Evaluator ev = new Evaluator(fex, 0, 1000);
//        TetrisAgent ag2 = new HandCodedForSZAgent(10,20);
//        fname = String.format((Locale)null,"HC_XT_perceptron.txt");
//        ev.perceptronLearn(ag2,fname);
    }

    public static void evalLSTD()
    {
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;

        fex = new ExtendedFeatureExtractor(game);
        for (int i =0; i<lambdalist.length; i++)
        {
            double lambda = lambdalist[i];
            String fname = String.format((Locale)null,"XT_lambda%.4f.txt",lambda);
            Evaluator ev = new Evaluator(fex, fname, 1000);
            ev.evaluate();
        }
    }

    public static void evalPerceptron()
    {
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;
        String fname;
        Evaluator ev;

        fex = new ExtendedFeatureExtractor(game);
        fname = String.format((Locale)null,"XT_perceptron.txt");
        ev = new Evaluator(fex, fname, 1000);
        ev.evaluate();
//
//        fex = new BertsekasIoffeFeatureExtractor(game);
//        fname = String.format((Locale)null,"BI_perceptron.txt");
//        ev = new Evaluator(fex, fname, 1000);
//        ev.evaluate();
    }

    public static void onlinece()
    {
        //Trainer trainer;
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex = new BertsekasIoffeFeatureExtractor(game);

        Options options = new Options();
        options.put("fex", fex);
        options.put("alpha", 1e-1);
        options.put("beta",  10e-2);
        options.put("epsilon", 0.1);
        options.put("gamma", 0.99);

        Trainer trainer = new directUpdateTrainer(10,0,options);
        int numgenerations = 300;
        for (int it=0; it<numgenerations; it++)
            trainer.playGeneration();
    }


    public static void qlearning()
    {
        boolean fileExists = true;
        int fileIndex = -1;
        String fname = "";
        PrintStream file = null;

        while (fileExists)
        {
            fileIndex ++;
            fname = String.format("run%03d.txt", fileIndex);
            fileExists = (new File(fname)).exists();

        }
        System.out.println(fname);
        try {
            file = new PrintStream(fname);
        }  catch (FileNotFoundException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Trainer trainer;
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;
        fex = new BertsekasIoffeFeatureExtractor(game);

        Options options = new Options();
        options.put("fex", fex);
        options.put("alpha", 1e-2);
        options.put("beta",  1e-2);
        options.put("epsilon", 0.1);
        options.put("gamma", 1.0);
        options.put("lambda", 0.7);
        options.put("replacing_traces", 1);
        options.put("trace_bound", 5.0);
        //options.put("Qinit", 100.0);
        file.print(options);
        
        QlearningAgent agent = new QlearningAgent(10,20,options);
        file.println(agent.theta.getRowDimension());
        QlearningAgent testagent;
        int numgames = 1000000;
        for (int it=0; it<numgames; it++)
        {
            if (it==10000)
                options.put("epsilon", 0.0);

            // print weights to file
            if (it%1000 == 0)
            {
                file.printf("%d \t %f %f \t",it, agent.theta.norm2(), agent.w.norm2());
                file.print(Evaluator.MatrixToString(agent.theta) + "\t");
                file.print(Evaluator.MatrixToString(agent.w) + "\n");
            }

            /// test performance after every 1000 games
            if (it%1000 == 0)
            {
                System.out.printf("********************************************* test: it#%d... \t ", it);
                testagent = agent.copy();
                testagent.options.put("learningOn", 0);
                int numtestgames = 100; // number of test games
                double score = 0;
                for (int i=0; i<numtestgames; i++)
                    score += game.playOneGame(agent);
                score /= numtestgames;
                System.out.println(score);

            }


            agent.xpList.clear();
            int res;
            double totalRew = 0.0;

            int piece = game.newPiece();
            game.clearBoard();
            agent.NewGame(piece, game.state.board);
            while (true)
            {
                TetrisAction a = agent.act();
                game.state = game.state.putTile(piece, a.rot, a.pos, true);
                res = game.state.lastResult;
                if (res ==-1)
                {
                    agent.GameOver();
                    break;
                }
                else
                {
                    totalRew += res;
                    piece = game.newPiece();
                    agent.getObservation(res, piece, game.state.board);
                }

            }

            if (it%100 == 99)
            {
                System.out.printf("%s:  %4d/%4d \t  %d \t %f \t %f \n", "QL", it+1,numgames, (int) totalRew,
                   agent.theta.norm2(),agent.w.norm2());
            }
        }
        file.close();
    }

    public static void qlearning_eval(int fileIndex, int gap, int ngames)
    {
        String fname = String.format("run%03d.txt", fileIndex);
        /// TODO....

    }

    public static void ce_forworkshop()
    {
        TetrisGame game = new TetrisGame(10, 20);
        FeatureExtractor fex;
        //fex = new BertsekasIoffeFeatureExtractor(game);
        int i;
        int width=10;
        Trainer trainer;

//        TetrisGame game = new TetrisGame(width, 20);
//        FeatureExtractor fex = new BertsekasIoffeFeatureExtractor(game);

        for (int run = 0; run<10; run++)
        {
//            fex = new ThresholdFeatureExtractor(game);
//            trainer = new CEtrainer(width,fex,run,textarea);
//            for(i=0; i<50; i++)
//                trainer.playGeneration();

            fex = new BertsekasIoffeFeatureExtractor(game);
            trainer = new CEtrainer(width,fex,run);
            for(i=0; i<50; i++)
                trainer.playGeneration();

            fex = new ScherrerThieryFeatureExtractor(game);
            trainer = new CEtrainer(width,fex,run);
            for(i=0; i<50; i++)
                trainer.playGeneration();

            fex = new ExtendedFeatureExtractor(game);
            trainer = new CEtrainer(width,fex,run);
            for(i=0; i<50; i++)
                trainer.playGeneration();

//            fex = new BertsekasIoffeFeatureExtractor(game);
//            trainer = new VItrainer(width, fex, 3, "CE_BI_formatlab.txt", VItrainer.LM_LSPE,textarea);
//            for(i=0; i<10; i++)
//                trainer.playGeneration();
        }

//        fex = new BertsekasIoffeFeatureExtractor(game);
//        Evaluator ev = new Evaluator(fex, 0, 10000);
////        ev.evaluate();
//
//        game = new TetrisGame(10,20);
//        game.state = game.state.putTile(4, 0, 0, true);
//        System.out.println(game.state);
    }
    
    public static void main(String[] args) {
        TetrisGame game;

//        ce_forworkshop();

//          createSamples();
//          lstdSolve();
//          evalLSTD();
//        perceptronSolve();
//        evalPerceptron();
        
//        qlearning();
//        onlinece();

        double avg = 0.0;
        //Trainer trainer;
        for (int i=0; i<5; i++)
        {
            game = new TetrisGame(10, 20);
            FeatureExtractor fex;

//            fex = new ExtendedFeatureExtractor(game);
//            fex = new ScherrerThieryFeatureExtractor(game);
            fex = new BertsekasIoffeFeatureExtractor(game);
            Evaluator ev = new Evaluator(fex, i, 100);
            ev.isLogging = false;
            avg += ev.evaluate();
        }
        avg /=5;
        System.out.println("\nTotal avg: " + avg);

//
//        AfterState state = game.state;
//        state = state.putTile(1, 0, 0, true);
//        state = state.putTile(1, 0, 0, true);
//        state = state.putTile(1, 0, 0, true);
//        state = state.putTile(1, 0, 2, true);
//        state = state.putTile(1, 0, 2, true);
//        state = state.putTile(1, 0, 2, true);
//        state = state.putTile(1, 0, 4, true);
//        state = state.putTile(0, 0, 8, true);
//        state = state.putTile(0, 0, 8, true);
//        state = state.putTile(0, 0, 6, true);
//        state = state.putTile(0, 0, 6, true);
////        System.out.println(state);
//        state.decolorize();
//
//        AfterState[] slist = new AfterState[4];
//        slist[0] = state.putTile(0, 0, 8, true);
//        slist[1] = state.putTile(0, 0, 4, true);
//        slist[2] = state.putTile(0, 0, 2, true);
//        slist[3] = state.putTile(0, 0, 1, true);
//        for(int i=0; i<4; i++)
//        {
//            ev.setStartState(slist[i], i);
//            ev.evaluate();
//            System.out.println(slist[i]);
//        }
    }

}
