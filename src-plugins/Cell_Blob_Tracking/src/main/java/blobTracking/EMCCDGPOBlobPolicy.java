package blobTracking;

import java.util.List;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.direct.NelderMeadSimplex;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.direct.SimplexOptimizer;
import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.optimization.RealPointValuePair;

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
	
	private class MyConvChecker implements ConvergenceChecker<RealPointValuePair>{
		public boolean converged(int iteration ,RealPointValuePair a, RealPointValuePair b){
			System.out.println("its:"+iteration);
			return false;
		}
	}

	private IterableRandomAccessibleInterval<IT> tempImage=null;
	private List<Blob> tempTrackables;
	private int count;
	
	
	@Override
	public double value(double[] arg0) {
		double totalFlux=Math.abs(arg0[arg0.length-1]);
		
		for(int i=2;i<arg0.length;i+=3){
			totalFlux+=Math.abs(arg0[i]);
		}
		
		//Blob b= tempTrackables.get(0);
		int count=0;
		for(Blob b:tempTrackables){
		b.xPos=arg0[0+count]; b.yPos=arg0[1+count];			
		b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
	//	totalFlux= arg0[2+count]*b.denom+arg0[3+count]*(double)(tempImage.dimension(0)*tempImage.dimension(1));
		b.pK=(Math.abs(arg0[2+count])*b.denom)/totalFlux;
//	System.out.println("b.denom:"+b.denom+ " xpos:"+b.xPos+ " ypos:"+ b.yPos+ " b/p:"+ arg0[3+count]*10+ " int:"+b.inten+ " tf:"+ totalFlux);
		count+=3;
		}
		
		if(totalFlux<0) System.out.println("totalFlux<0");
		double energy= getLogLikelihood(totalFlux, tempTrackables, tempImage);
//		System.out.println("e:"+ energy);
		return -energy;
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
			
			int count=0;
			
			int nop=calcNumberOfPixels(tempImage);
			
			
			
			
			
			Cursor<IT> cursor= tempImage.cursor();	
			double akku=0;
			int i=0;
			double akkuCheck=0;
			
			
			
			
			
			double totalFlux=0; 
	    	while ( cursor.hasNext() )	{
	    		cursor.fwd();
	    		totalFlux+= (int)cursor.get().getRealDouble();
	    	}
	    	totalFlux=(totalFlux/GAIN)*PAG;
	    	double backFlux=totalFlux;
	    	
	    	
	    		
	
			
	    	
	    	double[] startPoint=new double [3*trackables.size()+1];
	    	for(Blob b:trackables){
				
			b.numberOfPixels=nop;
	    	if(b.inten==0)
	    		b.inten=totalFlux*b.pK;
	    	
	    	
	//    	b.backInten = totalFlux*(1-b.pK);
	    	backFlux-=b.inten;
	    	
	    	startPoint[0+count]=b.xPos;
	    	startPoint[1+count]=b.yPos;
	    	startPoint[2+count]= b.inten;	//blobFlux
	    	
	    	
	    	count+=3;
	    	System.out.println("        b.inten:"+b.inten + " b.pk:"+ b.pK +" TF:"+totalFlux+" b.backInt/p:"+ b.backInten/(double)nop);
	    	}
	    	
	    	startPoint[startPoint.length-1]=backFlux/(double)nop;//b.backInten/(double)b.numberOfPixels;				//BackgroundFlux per pixel
	    	
	    	
	    	double []output=null;

	    	PowellOptimizer optimizer = new PowellOptimizer(1e-3, 1e-3);
	    
	    	
		//  SimplexOptimizer optimizer = new SimplexOptimizer();
	 // 	optimizer.setSimplex(new   NelderMeadSimplex(startPoint.length));
	 // 	optimizer.setConvergenceChecker(new MyConvChecker());
	  	
	    	
	    	
	  //  	output= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, startPoint).getPoint();
	   // 	System.out.println("2ndTry:");
	    	
	  // 	startPoint= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, startPoint).getPoint();
	    	
	    	
	    	for(int j=0;j<1;j++)
	    	startPoint= optimizer.optimize(10000000, this, GoalType.MINIMIZE , startPoint).getPoint();
	 
	    	totalFlux=Math.abs(startPoint[startPoint.length-1]);	//add background
			
			for(int j=2;j<startPoint.length;j+=3){		//add other intensities
				totalFlux+=Math.abs(startPoint[j]);
			}
			
	    	
	    	count=0;
			for(Blob b:trackables){
			System.out.println("count:"+count);
	    	b.xPos=startPoint[0+count]; b.yPos=startPoint[1+count];		    
	    	b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
			b.pK=(Math.abs(startPoint[2+count])*b.denom)/totalFlux;	
			b.inten=Math.abs(totalFlux*b.pK/b.denom);
	    	b.backInten = 	Math.abs(startPoint[startPoint.length-1])*(double)nop;    	
	    	count+=3;
	    	
//	    	System.out.println("b.inten:"+b.inten + " b.pk:"+ b.pK + " b.backInt/p:"+ startPoint[startPoint.length-1]+ "TF:"+ totalFlux);
			}
//			System.out.println("Done:");
			
	}

}
