package agent;

import tetrisengine.AfterState;
import tetrisengine.TetrisAction;
import Jama.Matrix;

public class PerceptronLearner {

	private static final int MIN_ROT = 0;
	private static final int MAX_ROT = 1;
	private static final int MIN_POS = 0;
	private static final int MAX_POS = 9;

	FeatureExtractor featureExtractor;
	protected double stepSize=1.0;
    int N = 0;
	public Matrix weights = null;

    public PerceptronLearner(FeatureExtractor fe){
    	featureExtractor = fe;
        this.N = fe.nFeatures;
		setup(1.0);
	}
		
    public PerceptronLearner(FeatureExtractor fe, double stepSize1) {
    	featureExtractor = fe;
        this.N = fe.nFeatures;
		setup(stepSize1);
	}

    protected void setup(double alph) {
		weights = new Matrix(N, 1);
		stepSize = alph;
	}

	private Matrix nextStateFeatures( AfterState currentState, int currentTileType, int rot, int pos ) {
		AfterState newState = currentState.putTile(currentTileType, rot, pos, true);
		if (newState.lastResult!=-1) // not an invalid action
			return new Matrix( featureExtractor.getFeatures( newState.board
				  				, newState.lastResult )
				  			, N );
		
		return null;
	}
	
	private double getValue( Matrix features )
	{
		return weights.transpose().times(features).get(0,0);
	}
	
	/** Returns the best action according to the current set of weights
	 * 
	 * @param currentState		The state at the moment
	 * @param currentTileType	The tile to be placed
	 * @return	The action that looks the best given the weights
	 */
	public TetrisAction getBestAction(AfterState currentState, int currentTileType ) {
		double bestValue = Double.NEGATIVE_INFINITY;
		int bestRot = 0, bestPos = 0;
		Matrix features = null;
		for (int rot=MIN_ROT; rot<=MAX_ROT; ++rot) {
			for (int pos=MIN_POS; pos<=MAX_POS; ++pos) {
				// compute features
				features = nextStateFeatures( currentState, currentTileType, rot, pos );
				if (features==null) // invalid next state
					continue;
				double value = getValue( features );
				if (value>bestValue) {
					bestValue = value;
					bestRot = rot; bestPos = pos;
				}
			}
		}
		if (bestValue == Double.NEGATIVE_INFINITY)
			throw new RuntimeException("Did not find any admissible action, how can this be?");
		return new TetrisAction(bestPos,bestRot);
	}
 	
	/** Trains the weights using the perceptron training rule
	 * 
	 * @param currentState			The current state in the game
	 * @param currentTileType		The tile to be placed
	 * @param expertActionToCome	The action that expert would take in this state
	 * @return	Returns true if the current set of weights correctly predicted the expert action
	 */
	public boolean addSample(AfterState currentState, int currentTileType, TetrisAction expertActionToCome ) {		
		double bestValue = Double.NEGATIVE_INFINITY;
		int bestRot = 0, bestPos = 0;
		Matrix features, bestFeatures = null;
		for (int rot=MIN_ROT; rot<=MAX_ROT; ++rot) {
			for (int pos=MIN_POS; pos<=MAX_POS; ++pos) {
				// compute features
				features = nextStateFeatures( currentState, currentTileType, rot, pos );
				if (features==null) // invalid next state
					continue;
				double value = getValue( features );
				if (value>bestValue) {
					bestValue = value;
					bestRot = rot; bestPos = pos;
					bestFeatures = features;
				}
			}
		}
		if (bestFeatures==null)
            return true;
//			throw new RuntimeException("Did not find any admissible action, how can this be?");
		
		if (bestRot!=expertActionToCome.rot || bestPos!=expertActionToCome.pos) {
			// update the weights
			features = nextStateFeatures( currentState, currentTileType
										, expertActionToCome.rot, expertActionToCome.pos );
			features.minusEquals(bestFeatures);
			features.times(stepSize);
			weights.plusEquals( features );
			return false;
		}
		return true;
	}

}
