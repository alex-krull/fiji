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
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;

import org.apache.commons.math.distribution.PoissonDistributionImpl;

import tools.ErlangDist;
import tools.ImglibTools;
import frameWork.Model;
import frameWork.MovieFrame;
import frameWork.Session;

public class EMCCDBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends MaximumLikelihoodBlobPolicy<IT>
{
	private ErlangSet eSet=null;

	public EMCCDBlobPolicy(){
		eSet=new ErlangSet(300);
	}
	
	@Override
	public String getTypeName() {	
		return "EMCCD-GaussianML";
	}

	@Override
	public void optimizeFrame(boolean alternateMethod, List<Blob> trackables,
			MovieFrame<IT> movieFrame, double qualityT,
			Session<Blob, IT> session) {
		
		
		BlobSession<IT> bSession=(BlobSession<IT>) session;
	    
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();
		Img<FloatType> expectedValues=imgFactory.create(movieFrame.getFrameView(), new FloatType());
		
	//	if(!alternateMethod){
//			super.optimizeFrame(false, trackables, movieFrame, qualityT, session);
	//		return;
	//	}
		
		int maxIterations=100;
		double totalInten=1000;
		super.numOfPixelsUsed=ImglibTools.getNumOfPixels(movieFrame.getFrameView());
		
		for(Blob b: trackables){
			b.denom=b.calcDenominator(movieFrame.getFrameView(), b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
		}
		List<Blob> refBlobs=new ArrayList<Blob>();
		
		double change=0;
		for(int i=0;i<maxIterations;i++){
			
			refBlobs.clear();
			for(Blob b: trackables){
				refBlobs.add(this.copy(b));
			}
			
					
			doEstep(expectedValues, movieFrame.getFrameView(), trackables, totalInten);
			totalInten=	doMstep(alternateMethod, trackables, expectedValues, qualityT, session);
		
			
			change=0;
			int index=0;
			for(Blob b: trackables){
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
			}
			
			System.out.println("                REAL CHANGE: "+change);
			if(change<qualityT) break;
		}
		
	} 
	
	private void doEstep(Img<FloatType> expectedValues, RandomAccessibleInterval<IT> image,
			List<Blob> blobs, double totalInten){
		System.out.println("entering");
		Cursor<FloatType> cursor =expectedValues.cursor();
		RandomAccess<IT> ra= image.randomAccess();
		int offSet=Model.getInstance().getIntensityOffset();
		int j=0;
		PoissonDistributionImpl poissonDist=null;
		
		while(cursor.hasNext()){
			cursor.fwd();
			
			j++;
			ra.setPosition(cursor);
			int value= Math.max(0, ((IntegerType)ra.get()).getInteger()-offSet);
			
			
			
			
			
				int x= ra.getIntPosition(0);
				int y= ra.getIntPosition(1);
				int z= 0;
				if(expectedValues.numDimensions()>2) z= ra.getIntPosition(2);
				double flux=calcFlux(totalInten, blobs, x,  y, z);
				if(flux>0.0000000000001)
					poissonDist= new PoissonDistributionImpl(flux);					
				else 
					poissonDist=null;
			
			
			double akkuDenom=0;
			double akku=0;
			double currentP=0;
			double aErlang=0;
			
			
			for(int i=0;i<10;i++){
				
				
				
				currentP=eSet.getErlangProb(i, value);
				aErlang+=currentP;
				if(poissonDist!=null)currentP*=poissonDist.probability(i);
				else{
					if(i>0)currentP=0;
				}
			
				akku+=currentP*i;
				akkuDenom+=currentP;
				
				
				
				
				if(aErlang>0.99) break;
			}
			
		
			if(akkuDenom>0) cursor.get().set((float)(akku/akkuDenom));
			else  cursor.get().set((0));
		
		}
	
	}
	
	private double calcFlux(double totalInten, List<Blob> blobs, int x, int y, int z){
		double numOfPixels=super.numOfPixelsUsed;
		double backProb=1;
		for(Blob b: blobs){
			backProb-=b.pK;
		}
		
		double akku=backProb/numOfPixels;
		double xyToZ=Model.getInstance().getXyToZ();
		for(Blob b: blobs){
			akku+=b.pXunderK(x, y, z, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ, b.denom, xyToZ)*b.pK;
		}
		return akku*totalInten;
	}
	
	private double doMstep(boolean alternateMethod, List<Blob> trackables,
			Img<FloatType> expectedValues, double qualityT,
			Session<Blob, IT> session){
		MaximumLikelihoodBlobPolicy<FloatType> bp= new MaximumLikelihoodBlobPolicy<FloatType>();
		return bp.doOptimizationSingleScale(trackables, expectedValues, qualityT, 0, 1,(BlobSession<IT>) session);
		
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
}
