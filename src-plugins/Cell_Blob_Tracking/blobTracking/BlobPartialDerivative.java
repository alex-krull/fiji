package blobTracking;

import org.apache.commons.math.analysis.MultivariateRealFunction;

public class BlobPartialDerivative implements MultivariateRealFunction{

	public Blob blob;
	public int dim;
	
	public BlobPartialDerivative(Blob b, int d){
		blob=b;
		dim=d;
	}
	
	@Override
	public double value(double[] position) {
		double[] pos1={position[0],position[1],position[2]};
		double[] pos2={position[0],position[1],position[2]};
		pos1[dim]-=0.0001;
		pos2[dim]+=0.0001;
	//	return 1;
		System.out.println((blob.value(pos1)-blob.value(pos2))/0.0002);
		return -(blob.value(pos1)-blob.value(pos2))/0.0002;
	}

}
