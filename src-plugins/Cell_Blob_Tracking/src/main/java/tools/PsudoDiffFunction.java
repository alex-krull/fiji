package tools;

import org.apache.commons.math.analysis.DifferentiableMultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.exception.MathUserException;

public class PsudoDiffFunction implements DifferentiableMultivariateRealFunction{
	
	MultivariateRealFunction function;
	double delta;
	
	public PsudoDiffFunction(MultivariateRealFunction func, double delt){
		function = func;
		delta=delt;
		
	}
	
	@Override
	public double value(double[] pos) {
		return function.value(pos);
	}

	public class Gradient implements MultivariateVectorialFunction{

		 
		
		@Override
		public double[] value(double[] arg0) throws MathUserException,
				IllegalArgumentException {
			
			double[] result= new double[arg0.length];
			
			for(int i=0;i<arg0.length;i++){
				result[i]=partialDerivative(i).value(arg0);
			}
			return result;
		}
		
	}
	
	@Override
	public MultivariateVectorialFunction gradient() {
		return new Gradient();
	}
	
	

	public class PartDeriv implements MultivariateRealFunction{
		private final int dimension;
		
		PartDeriv(int dim){
			dimension=dim;
		}
		
		@Override
		public double value(double[] arg0) {
			double[] posA=new double[arg0.length];
			double[] posB=new double[arg0.length];
			
			for(int i=0;i< arg0.length;i++){
				posA[i]=arg0[i];
				posB[i]=arg0[i];
			}
			
			posA[dimension]+=delta;
			posB[dimension]-=delta;
			
			double va=function.value(posA);
			double vb=function.value(posB);
			
			double result=(va-vb)/(2.0*delta);
	/*		
			System.out.println("result:"+result);
			System.out.println("va:"+va);
			System.out.println("vb:"+vb);
			System.out.println("posA[dimension]:"+posA[dimension]);
			System.out.println("posB[dimension]:"+posB[dimension]);
	*/		
			return -result;
		}
		
	}
	
	
	
	@Override
	public MultivariateRealFunction partialDerivative(int arg0) {
		return new PartDeriv(arg0);
	}

}
