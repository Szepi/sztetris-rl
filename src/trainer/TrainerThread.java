/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package trainer;

import agent.BertsekasIoffeFeatureExtractor;
import agent.ExtendedFeatureExtractor;
import agent.FeatureExtractor;
import agent.ScherrerThieryFeatureExtractor;
import agent.ThresholdFeatureExtractor;
import javax.swing.JTextArea;
import tetrisengine.TetrisGame;

/**
 *
 * @author szityu
 */
public class TrainerThread extends Thread {
    
//    CEtrainer trainer;
    Trainer trainer;
    JTextArea textarea;
    
    public TrainerThread()
    {
        super();
        //this.cetrainer = cetrainer;
        
        textarea = null;
        //cetrainer = new CEtrainer();
    }
    
    public TrainerThread(JTextArea textarea)
    {
        super();
        //this.cetrainer = cetrainer;
        
        this.textarea = textarea;
//        cetrainer = new CEtrainer(textarea);
    }

    @Override
    public void run()
    {
        int i;
        int width=10;
        
        TetrisGame game = new TetrisGame(width, 20);
        FeatureExtractor fex;

        for (int run = 0; run<10; run++)
        {
//            fex = new ThresholdFeatureExtractor(game);
//            trainer = new CEtrainer(width,fex,run,textarea);
//            for(i=0; i<50; i++)
//                trainer.playGeneration();

//            fex = new BertsekasIoffeFeatureExtractor(game);
//            trainer = new CEtrainer(width,fex,run,textarea);
//            fex = new BertsekasIoffeFeatureExtractor(game);
//            for(i=0; i<50; i++)
//                trainer.playGeneration();

//            fex = new ScherrerThieryFeatureExtractor(game);
//            trainer = new CEtrainer(width,fex,run,textarea);
//            for(i=0; i<50; i++)
//                trainer.playGeneration();
//
            fex = new ExtendedFeatureExtractor(game);
            trainer = new CEtrainer(width,fex,run,textarea);
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

}
