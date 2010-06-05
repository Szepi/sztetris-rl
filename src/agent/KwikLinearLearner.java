package agent;

import java.util.ArrayList;
import java.util.List;

import Jama.Matrix;

public class KwikLinearLearner  {

	protected Matrix Q;
	protected Matrix w;
	protected double alpha0;
    int N;

    public KwikLinearLearner(int N){
        this.N = N;
		setup(.1);
	}
	
	
	public KwikLinearLearner(int N, double alph){
        this.N = N;
		setup(alph);
	}
	
	protected void setup(double alph){
		Q = Matrix.identity(N,N);
		w = new Matrix(N, 1);
		alpha0 = alph;
	}
	
	
	public boolean isKnown(double[] phi) {
		Matrix xt = new Matrix(phi,N);
		double alpha = Q.times(xt).norm2();
		if(alpha < alpha0)
			return true;
		return false;
	}
	
	public void addSample(double[] phi) {
		
		double outcome = 1.0;
		
		Matrix xt = new Matrix(phi,N);
		Matrix Qx = Q.times(xt);
		Matrix one = new Matrix(1,1);
		one.set(0, 0, 1.0);
		Q = Q.minus(Qx.times(Qx.transpose()).times((one.plus(xt.transpose().times(Qx))).inverse().get(0, 0)));  
		w.plusEquals(xt.times(outcome));  
		
	}
	
}
