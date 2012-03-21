package blobTracking;

import org.apache.commons.math.analysis.MultivariateVectorialFunction;
import org.apache.commons.math.exception.MathUserException;

public class BlobGradientFunction implements MultivariateVectorialFunction{

	Blob blob;
	
	public BlobGradientFunction(Blob b){
		blob=b;
	}
	
	@Override
	public double[] value(double[] dim) throws MathUserException{
		double [] result=new double[3];
		result[0]=(new BlobPartialDerivative(blob,0)).value(dim);
		result[1]=(new BlobPartialDerivative(blob,1)).value(dim);
		result[2]=(new BlobPartialDerivative(blob,2)).value(dim);
		
		return result;
	}
	
}
