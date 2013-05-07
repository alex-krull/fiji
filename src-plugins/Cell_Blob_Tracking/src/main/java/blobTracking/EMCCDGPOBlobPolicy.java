package blobTracking;

import java.util.List;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.direct.NelderMeadSimplex;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.direct.SimplexOptimizer;
import org.apache.commons.math.optimization.direct.CMAESOptimizer;
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
	private double totalFlux;
	private int count;
	
	
	@Override
	public double value(double[] arg0) {
		
		double assumedTotalFlux=arg0[arg0.length-1]*(double)(tempImage.dimension(0)*tempImage.dimension(1));
		//Blob b= tempTrackables.get(0);
		
		if(arg0[arg0.length-1]<0) return 1.0/0;
		
		int i=0;
		for(Blob b:tempTrackables){
			if(arg0[i+2]<0) return 1.0/0;
			b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
			assumedTotalFlux+=arg0[i+2]*b.denom;
			i+=3;
		}			
		
	
		int count=0;
		for(Blob b:tempTrackables){
		
		b.xPos=arg0[0+count]; b.yPos=arg0[1+count];			
		
	
	//	totalFlux= arg0[2+count]*b.denom+arg0[3+count]*(double)(tempImage.dimension(0)*tempImage.dimension(1));
		b.pK=Math.abs((arg0[2+count]*b.denom)/assumedTotalFlux);
//	System.out.println("b.denom:"+b.denom+ " xpos:"+b.xPos+ " ypos:"+ b.yPos+ " b/p:"+ arg0[3+count]*10+ " int:"+b.inten+ " tf:"+ assumedTotalFlux+ "\n");
		count+=3;
		if(b.pK<0) System.out.println("totalFlux<0");
		}
		
		
		double energy= getLogLikelihood(assumedTotalFlux, tempTrackables, tempImage);
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
			
			
			
			
			
			totalFlux=0; 
	    	while ( cursor.hasNext() )	{
	    		cursor.fwd();
	    		totalFlux+= (int)cursor.get().getRealDouble();
	    	}
	    	totalFlux=(totalFlux/GAIN)*PAG;
	    	double backFlux=totalFlux;
	    	
	    	
	    		
	
			
	    	
	    	double[] startPoint=new double [3*trackables.size()+1];
	    	for(Blob b:trackables){
	    		b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
			//	b.pK=(Math.abs(startPoint[2+count])*b.denom)/totalFlux;	
			b.numberOfPixels=nop;
	    	//if(b.inten==0)
	    	b.inten=totalFlux*b.pK/b.denom;
	    	
	    	
	//    	b.backInten = totalFlux*(1-b.pK);
	    	backFlux-=totalFlux*b.pK;
	    	
	    	startPoint[0+count]=b.xPos;
	    	startPoint[1+count]=b.yPos;
	    	startPoint[2+count]= b.inten;	//blobFlux
	    	
	    	
	    	count+=3;
	    	System.out.println("        b.inten:"+b.inten + " b.pk:"+ b.pK +" TF:"+totalFlux+" backFlux/p:"+ backFlux/(double)nop+ " bf:"+ backFlux+ "\n");
	    	}
	    	
	    	startPoint[startPoint.length-1]=backFlux/(double)nop;//b.backInten/(double)b.numberOfPixels;				//BackgroundFlux per pixel
	    	System.out.println("recalc:"+ (double)(tempImage.dimension(0)*tempImage.dimension(1))*startPoint[startPoint.length-1]+"\n");
	    	
	    	
	    	double []output=null;

	    	PowellOptimizer optimizer = new PowellOptimizer(1e-3, 1e-3);
	    
	//    	CMAESOptimizer optimizer= new CMAESOptimizer((int)(4+Math.floor(3*Math.log(startPoint.length))));
	    	
	//    	SimplexOptimizer optimizer = new SimplexOptimizer();
	 // optimizer.setSimplex(new   NelderMeadSimplex(startPoint.length));
	 // 	optimizer.setConvergenceChecker(new MyConvChecker());
	  	
	    	
	    	
	  //  	output= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, startPoint).getPoint();
	   // 	System.out.println("2ndTry:");
	    	
	  // 	startPoint= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, startPoint).getPoint();
	    	System.out.println("tf: "+ totalFlux+"\n");
	   	System.out.println("energy: "+  value(startPoint)+"\n");
	 //   			getLogLikelihood(totalFlux, trackables, tempImage) );
	//	if(true) return;
	    
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
