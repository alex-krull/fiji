package blobTracking;

import java.util.List;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.direct.MultiDirectionalSimplex;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.direct.SimplexOptimizer;

import cern.jet.math.Bessel;

import frameWork.Model;
import frameWork.MovieFrame;
import frameWork.Session;

import net.imglib2.Cursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;

public class EMCCDGPOBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends EMCCDBlobPolicy<IT>
implements MultivariateRealFunction
{

	private IterableRandomAccessibleInterval<IT> tempImage=null;
	private List<Blob> tempTrackables;
	private int count;
	
	@Override
	public double value(double[] arg0) {
		Blob b= tempTrackables.get(0);
		b.xPos=arg0[0]; b.yPos=arg0[1];			
		b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
		double totalFlux= arg0[2]*b.denom+arg0[3]*(double)(tempImage.dimension(0)*tempImage.dimension(1));
		b.pK=(arg0[2]*b.denom)/totalFlux;
		
//		System.out.println("b.denom:"+b.denom+ " xpos:"+b.xPos+ " ypos:"+ b.yPos);
		double energy= getLogLikelihood(totalFlux, tempTrackables, tempImage);
    	
		return energy;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "EMCCD-GPO";
	}

	@Override
	public void optimizeFrame(boolean alternateMethod, List<Blob> trackables,
			MovieFrame<IT> movieFrame, double qualityT,
			Session<Blob, IT> session) {
	//	System.out.println("starting to optimize\n");
			count=0;
			tempImage=new IterableRandomAccessibleInterval<IT>( movieFrame.getFrameView());
			tempTrackables=trackables;
			Blob b= trackables.get(0);
			
	    	double[] startPoint=new double [4];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=b.yPos;
	    	startPoint[2]=100;				//blobFlux
	    	startPoint[3]=5;				//BackgroundFlux per pixel
	    
	    	double []output=null;

	    	PowellOptimizer optimizer = new PowellOptimizer(1e-15, 1e-15);	
	//	     	SimplexOptimizer optimizer = new SimplexOptimizer();
	//  		optimizer.setSimplex(new   MultiDirectionalSimplex(startPoint.length));
	    	
	    	output= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, startPoint).getPoint();
	    	
	    	for(int i=0;i<10;i++)
	    	output= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, output).getPoint();
	 
	    	
	    	b.xPos=output[0];
	    	b.yPos=output[1];
	    	tempImage=null;
	}

}
