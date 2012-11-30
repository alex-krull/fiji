package blobTracking;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;

import org.apache.commons.math.distribution.PoissonDistributionImpl;

import cern.jet.math.Bessel;

import sun.tools.tree.ThisExpression;
import tools.ErlangDist;
import tools.ImglibTools;
import tools.OtherTools;
import frameWork.Model;
import frameWork.MovieFrame;
import frameWork.Session;


public class EMCCDBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends MaximumLikelihoodBlobPolicy<IT>


{
	protected static double  GAIN=30;
//	private static double  PAG=12.17;
//	private static double  PAG=11.3;
	protected static double  PAG=1;
	private static Img<FloatType> fluxValues;
	

	public EMCCDBlobPolicy(){
	
	}
	
	@Override
	public String getTypeName() {	
		return "EMCCD-GaussianML";
	}

	@Override
	public void optimizeFrame(boolean alternateMethod, List<Blob> trackables,
			MovieFrame<IT> movieFrame, double qualityT,
			Session<Blob, IT> session) {
		
		for(Blob b: trackables){
			b.denom=b.calcDenominator(movieFrame.getFrameView(), b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
			
		}
		
	//	super.optimizeFrame(false, trackables, movieFrame, 0.1, session);
	
		
		
		BlobSession<IT> bSession=(BlobSession<IT>) session;
	    
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();
		Img<FloatType> expectedValues=imgFactory.create(movieFrame.getFrameView(), new FloatType());
		fluxValues=imgFactory.create(movieFrame.getFrameView(), new FloatType());
		
	//	if(!alternateMethod){
//			super.optimizeFrame(false, trackables, movieFrame, qualityT, session);
	//		return;
	//	}
		
		int maxIterations=30;
		
		
		double totalInten=0;
		
		IterableRandomAccessibleInterval<IT> itFrame=new IterableRandomAccessibleInterval<IT>( movieFrame.getFrameView());
		Cursor<IT> c = itFrame.cursor();
		while(c.hasNext()){
			c.fwd();
			totalInten+=c.get().getRealDouble();
		}
		totalInten/=GAIN;
		
		super.numOfPixelsUsed=ImglibTools.getNumOfPixels(movieFrame.getFrameView());
		
		
		
		List<Blob> refBlobs=new ArrayList<Blob>();
		double energyOld=1;
		double energy=1;
		double change=0;
		for(int i=0;i<maxIterations;i++){
			
			refBlobs.clear();
			for(Blob b: trackables){
		//		b.pK=(b.pK*totalInten-2.5)/totalInten;
		//		b.calcDenominator(movieFrame.getFrameView(), b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
				Blob cb= this.copy(b);
				refBlobs.add(cb);
				
			}
			
			Model.getInstance().depositMsg("E-step");
			Model.getInstance().makeChangesPublic();
			
			totalInten= doEstep(expectedValues, movieFrame.getFrameView(), trackables, totalInten);
			
			Model.getInstance().depositMsg("M-step");
			Model.getInstance().makeChangesPublic();
		
			
			doMstep(alternateMethod, trackables, expectedValues, qualityT, session);
			energyOld=energy;
			energy=this.getLogLikelihood(totalInten, trackables, itFrame);
	//		System.out.println(energy);
	//		MaximumLikelihoodBlobPolicy<IT> bp= new MaximumLikelihoodBlobPolicy<IT>();	
		//	totalInten= bp.doOptimizationSingleScale(trackables, movieFrame.getFrameView(), qualityT, 0, 1000,(BlobSession<IT>) session);
			
		//	bp.doOptimizationSingleScale(trackables, movieFrame.getFrameView(),qualityT,
		//			Model.getInstance().getIntensityOffset(),1000, (BlobSession<IT>)session);
			
			change=0;
			int index=0;
	/*		for(Blob b: trackables){
				
				Blob bOld=refBlobs.get(index);
				double changePos=Math.sqrt(
						(bOld.xPos-b.xPos)*(bOld.xPos-b.xPos)+
						(bOld.yPos-b.yPos)*(bOld.yPos-b.yPos)+
						(bOld.zPos-b.zPos)*(bOld.zPos-b.zPos)
						);
				
				change=Math.max(change, changePos);
		    	change=Math.max(bSession.getChangeFactorSigma()*Math.abs((bOld.sigma*bOld.sigma-b.sigma*b.sigma)), change);
		    	change=Math.max(bSession.getChangeFactorPK()*Math.abs(bOld.pK-b.pK), change);		
				index++;
				b.iterations=i;
				
			}
		*/
			change=Math.abs(energyOld-energy);
			
			if(change<qualityT)
				break;
		}
	//	ImageJFunctions.show (fluxValues, "ev");
		
	} 
	
	private double doEstep(Img<FloatType> expectedValues, RandomAccessibleInterval<IT> image,
			List<Blob> blobs, double totalInten){
	
		double newTotalInt=0;
		Cursor<FloatType> cursor =expectedValues.cursor();
		Cursor<FloatType> cursorFlux =fluxValues.cursor();
		RandomAccess<IT> ra= image.randomAccess();
		int offSet=Model.getInstance().getIntensityOffset();
		PoissonDistributionImpl poissonDist=null;

		
		while(cursor.hasNext()){
			
			
			cursor.fwd();
			cursorFlux.fwd();
			
			
			
			ra.setPosition(cursor);
			int value=(int) (((double)Math.max(0, (ra.get()).getRealDouble()-offSet))*PAG);
	/*		if(value==0){
				cursor.get().set((0));
				continue;
			}
	*/		
			
			cursor.get().set((float)value);
			
			
	/*		
				int x= ra.getIntPosition(0);
				int y= ra.getIntPosition(1);
				int z= 0;
				if(expectedValues.numDimensions()>2) z= ra.getIntPosition(2);
				double flux=calcFlux(totalInten, blobs, x,  y, z);
				double pZero=0;
				if(flux>0){
					poissonDist= new PoissonDistributionImpl(flux);	
					pZero=OtherTools.getErlangProp(0, value, GAIN)*poissonDist.probability(0);
				}
									
				else{
					poissonDist=null;
					pZero=OtherTools.getErlangProp(0, value, GAIN);
				}
					
		*/	
			
			
			
		/*	
			double aErlang=0;
			double akku=0;
			double akkuDenom=0;
			for(int i=0;i<20;i++){
				
				
				
				Double currentP=OtherTools.getErlangProp(i, value, GAIN);
				aErlang+=currentP;
				if(poissonDist!=null)currentP*=poissonDist.probability(i);
				else{
					if(i>0)currentP=0.0;
				}
			
				akku+=currentP*i;
				akkuDenom+=currentP;
				
				if(i==0) pZero=currentP;
				
				
		//		if(i>flux*3+2) break;
			}
			double expected=akku/akkuDenom;
			cursor.get().set((float)(expected));
			cursorFlux.get().set((float)flux);
			newTotalInt+=expected;
	*/
				
	/*
			double invGain=1.0/GAIN;
			double temp=Math.sqrt(invGain*flux*(double)value);
			double missingTerm=0;
			if(pZero!=0) missingTerm=pZero/Math.exp(-invGain*value-flux)*invGain*flux;
		
			//double tempA=temp*OtherTools.bessi0(2*temp);
			//double tempB=(OtherTools.bessi1(2*temp)+missingTerm);
			double tempA=temp*Bessel.i0(2*temp);
			double tempB=(double)Bessel.i1(2*temp)+missingTerm;
							
			double besselExpected=0;
			
			if(tempB!=0) besselExpected=tempA/tempB;
			if(Double.isNaN(besselExpected) || Double.isInfinite(besselExpected)) 
				besselExpected=(temp*Bessel.i0e(2*temp)) / (Bessel.i1e(2*temp)+missingTerm*Math.exp(-2*temp));
			
	//		System.out.println("iterative:"+ (akku/akkuDenom) + " bessel:"+ besselExpected);
			
			
			
			
			
			if(!Double.isNaN(besselExpected)){
				cursor.get().set((float)(besselExpected));		
				newTotalInt+=besselExpected;
			}else {
				newTotalInt+=value/GAIN;
				System.out.println("NAN!!!!!!!!");	
				System.out.println("value:"+ value+ " flux:" +flux+" temp:"+ temp+
						"x:"+ x+" y:"+y+ 
						" missingTerm:"+missingTerm+
						" pZero:"+pZero+
						" Math.exp(-invGain*value-flux):"+ Math.exp(-invGain*value-flux)+
						" OtherTools.bessi0(2*temp):"+Bessel.i0(2*temp)+
						" OtherTools.bessi1(2*temp):"+Bessel.i1(2*temp));	
				
				cursor.get().set((0));
				calcFlux(totalInten, blobs, x,  y, z);
			}
		*/
			
		}
		
		
    //	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~totalInt EMCCD: " +totalInt);
    //	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~totalInten: " +totalInten);
    //	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~fluxAkku: " +fluxAkku);
    //	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~valueAkku: " +valueAkku);
		return newTotalInt;
	}
	
	protected double calcFlux(double totalInten, List<Blob> blobs, int x, int y, int z){
		double numOfPixels=super.numOfPixelsUsed;
		
		double backProb=1;
		for(Blob b: blobs){
			backProb-=b.pK;
		}
		
		double akku=0;
		
		
		
		for(Blob b: blobs){		
			akku+=b.pXandK(x, y, z, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ, b.denom);
		}
		if(Double.isNaN(akku)||Double.isNaN(totalInten) || Double.isInfinite(akku*totalInten)){
			System.out.println("akku:"+akku+" totalInten:"+totalInten+ " x:"+x+ " y:"+y);
			return 0;
		}
		return akku*totalInten+((totalInten*backProb)/numOfPixels);
		//return 1;
	}
	
	private double doMstep(boolean alternateMethod, List<Blob> trackables,
			Img<FloatType> expectedValues, double qualityT,
			Session<Blob, IT> session){
		MaximumLikelihoodBlobPolicy<FloatType> bp= new MaximumLikelihoodBlobPolicy<FloatType>();
		
		return bp.doOptimizationSingleScale(trackables, expectedValues, qualityT, 0, 1000,(BlobSession<IT>) session);
		
	}
	
	public class ErlangSet{
		private final TreeMap<Integer,ErlangDist> dists;
		private final double gain;
		
		public ErlangSet( double g){
			dists=new TreeMap<Integer,ErlangDist>();
			gain = g;
		}
		
		public synchronized double getErlangProb( int input, int output){		
			ErlangDist e= dists.get(input);
			if(e==null){
				
				e=new ErlangDist(input, gain, 0.01);
				synchronized (this){
				dists.put(input, e);
				}
			}		
			return e.getProb(output);
			
		}
		
		public synchronized double draw(int input, double rv){
			ErlangDist e= dists.get(input);
			if(e==null){
				e=new ErlangDist(input, gain, 0.01);
				synchronized (this){
				dists.put(input, e);
				}
			}
			return e.drawOutput(rv);
			
		}
		
		
		
	}
/*	
	@Override
	public boolean isHidden(){
		return false;
	}
*/


	public double getLogLikelihood(double totalFlux,  List<Blob> tempTrackables, IterableRandomAccessibleInterval<IT> tempImage){
		
		double inverseGain=1.0/GAIN;
		
		Cursor<IT> cursor= tempImage.cursor();	
		double akku=0;
		int i=0;
		double akkuCheck=0;
    	while ( cursor.hasNext() )	{
    		cursor.fwd();
    		i++;
    		int x= cursor.getIntPosition(0);
    		int y= cursor.getIntPosition(1);
    //		System.out.println("i:"+i+ " x:"+x+" y:"+y+"\n");
    		numOfPixelsUsed=(tempImage.dimension(0)*tempImage.dimension(1));
    		double flux=calcFlux(totalFlux, tempTrackables, x, y, 0);
    		int value= (int)cursor.get().getRealDouble();
    		if(value==0){
    			akku+=-flux;
    		}else{
    			
    			akku+=Math.log(Math.sqrt(inverseGain*flux/(double)value));
    			akku+=(-inverseGain*value-flux);
    			akku+=Math.log(Bessel.i1(2.0*Math.sqrt(inverseGain*flux*value)));
    		}
    		akkuCheck+=flux;
    		
    	}
    	
    //	System.out.println("akkuCheck:"+akkuCheck+ " totalFlux:"+totalFlux);
		return akku;
	}
}