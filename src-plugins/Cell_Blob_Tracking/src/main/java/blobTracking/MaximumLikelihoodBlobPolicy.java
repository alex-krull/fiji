/*******************************************************************************
 * This software implements the tracking method descibed in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexandar Krull
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Contributors:
 * 	Alexander Krull (Alexander.Krull@tu-dresden.de)
 *     Damien Ramunno-Johnson (GUI)
 *******************************************************************************/
package blobTracking;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;

import org.apache.commons.math.optimization.ConvergenceChecker;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.RealPointValuePair;
import org.apache.commons.math.optimization.direct.BOBYQAOptimizer;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.direct.CMAESOptimizer;

import tools.ImglibTools;
import frameWork.Model;
import frameWork.MovieFrame;
import frameWork.Session;

public class MaximumLikelihoodBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends BlobPolicy<IT>{

	private static double  BORDERSIZE=3;
	protected long numOfPixelsUsed;



	@Override
	public String getTypeName() {
		return "M.L.GaussianTracking";
	}

	protected IterableRandomAccessibleInterval<IT> makeIterableFrame(RandomAccessibleInterval <IT> movieFrame,  List <Blob> trackables){
		boolean isVolume= movieFrame.numDimensions()>2;
		long[] mins=  {Long.MAX_VALUE,Long.MAX_VALUE,0};	
		int max2=1;
		if(isVolume)max2=(int) movieFrame.max(2);
		long[] maxs=  {Long.MIN_VALUE,Long.MIN_VALUE,max2};

		for(Blob b:trackables){




			mins[0]=Math.min(mins[0],(long) (b.xPos-b.sigma*3-BORDERSIZE));
			mins[1]=Math.min(mins[1],(long) (b.yPos-b.sigma*3-BORDERSIZE));
			maxs[0]=Math.max(maxs[0],(long) (b.xPos+b.sigma*3+BORDERSIZE));
			maxs[1]=Math.max(maxs[1],(long) (b.yPos+b.sigma*3+BORDERSIZE));

		}

		long frameMinX=movieFrame.min(0);
		long frameMinY=movieFrame.min(1);
		long frameMaxX=movieFrame.max(0);
		long frameMaxY=movieFrame.max(1);


		mins[0]=Math.max(mins[0],frameMinX);
		mins[1]=Math.max(mins[1],frameMinY);
		maxs[0]=Math.min(maxs[0],frameMaxX);
		maxs[1]=Math.min(maxs[1],frameMaxY);

		mins[0]=Math.min(mins[0],maxs[0]);
		mins[1]=Math.min(mins[1],maxs[1]);
		maxs[0]=Math.max(maxs[0],mins[0]);
		maxs[1]=Math.max(maxs[1],mins[1]);


		if(isVolume){
			System.out.println(" mins[0] after:"+ mins[0]+" max[0] after:"+ maxs[0]);
			System.out.println(" mins[1] after:"+ mins[1]+" max[0] after:"+ maxs[1]);
			System.out.println(" mins[2] after:"+ mins[2]+" max[2] after:"+ maxs[2]);
			System.out.println(" movieFrame min[0] :"+movieFrame.min(0)+" movieFrame[0] max:"+ movieFrame.max(0));
			System.out.println(" movieFrame min[1] :"+movieFrame.min(1)+" movieFrame[1] max:"+ movieFrame.max(1));
			System.out.println(" movieFrame min[2] :"+movieFrame.min(2)+" movieFrame[2] max:"+ movieFrame.max(2));
			return new IterableRandomAccessibleInterval<IT>(Views.interval(movieFrame,mins,maxs ));
		}else{
			long[] mins2D={mins[0],mins[1]};
			long[] maxs2D={maxs[0],maxs[1]};
			return new IterableRandomAccessibleInterval<IT>(Views.interval(movieFrame,mins2D,maxs2D ));
		}



	}

	private double doEStep( List <Blob> trackables,  Double backProb, IterableRandomAccessibleInterval<IT> iterableFrame,
			int constBackground){
		boolean isVolume=iterableFrame.numDimensions()>2;
		double totalInten=0;


		for(Blob b:trackables){
			b.newInten=0;
			b.denom=b.calcDenominator(iterableFrame, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
		}
		Cursor<IT> cursor =iterableFrame.cursor();


		double pX=0;


		numOfPixelsUsed=ImglibTools.getNumOfPixels(iterableFrame);
		while ( cursor.hasNext() )	{
			cursor.fwd();
			pX=backProb/(double)numOfPixelsUsed; // init with probability for background

			int x=cursor.getIntPosition(0);
			int y=cursor.getIntPosition(1);
			int z=0;
			if(isVolume) z=cursor.getIntPosition(2);
			float value= Math.max(0,cursor.get().getRealFloat()- constBackground);
			totalInten+=value;

			for(Blob b:trackables){
				pX+=b.pXandK(x, y, z, 
						b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ,
						b.denom);
			}

			for(Blob b:trackables){
				RandomAccess<FloatType> ra= b.expectedValues.randomAccess();
				ra.setPosition(cursor);
				double currentInten=0;
				if(value>0 && pX>0){			// avoid NAN
					currentInten=value*
							b.pXandK(x, y, z,
									b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ,
									b.denom)/pX;
				}



				ra.get().set((float)(currentInten  ) );
				b.newInten+=currentInten;  



				if(Double.isNaN(b.newInten)){
					System.out.println("problem!!!!"); 
					b.pXandK(x, y, z,
							b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ,
							b.denom);
				}




			}



		}
		return totalInten;
	}

	private class myConvChecker implements ConvergenceChecker<RealPointValuePair>{

		private double firstV;

		@Override
		public boolean converged(int iteration, RealPointValuePair previous, RealPointValuePair current) {
			if(iteration==1) firstV=previous.getValue();

			if (iteration>10  &&firstV<=current.getValue()){
				Model.getInstance().depositMsg("iterations:"+iteration);
				return true;
			}

			return false;
		}





	}

	private void doMstepForBlob(Blob b, double totalInten, IterableRandomAccessibleInterval<IT> iterableFrame){
		b.newPK=b.pK;
		b.newSig=b.sigma;
		b.newX=b.xPos;
		b.newY=b.yPos;
		b.newZ=b.zPos;

		boolean isVolume=iterableFrame.numDimensions()>2;

		long[] mins=null;
		long[] maxs=null;



		if(b.xPos<b.expectedValues.min(0)) b.xPos=b.expectedValues.min(0);
		if(b.yPos<b.expectedValues.min(1)) b.yPos=b.expectedValues.min(1);
		if(b.xPos>b.expectedValues.max(0)) b.xPos=b.expectedValues.max(0);
		if(b.yPos>b.expectedValues.max(1)) b.yPos=b.expectedValues.max(1);

		if(isVolume){
			mins = new long[3];		
			mins[2]= (long)	Math.max(b.expectedValues.min(2), (b.zPos-b.sigmaZ*3)/Model.getInstance().getXyToZ()-1 );

			maxs = new long[3];
			maxs[2]= (long)	Math.min(b.expectedValues.max(2), (b.zPos+b.sigmaZ*3)/Model.getInstance().getXyToZ() +1);
		}else{
			mins = new long[2];
			maxs = new long[2];
		}

		mins[0]=(long)Math.max(b.expectedValues.min(0), b.xPos-b.sigma*3-BORDERSIZE );
		mins[1]= (long)	Math.max(b.expectedValues.min(1), b.yPos-b.sigma*3-BORDERSIZE );


		maxs[0]=(long)Math.min(b.expectedValues.max(0), b.xPos+b.sigma*3+BORDERSIZE );
		maxs[1]= (long)	Math.min(b.expectedValues.max(1), b.yPos+b.sigma*3 +BORDERSIZE);









		b.expectedValuesRoi=new IterableRandomAccessibleInterval<FloatType>(Views.interval(b.expectedValues,mins,maxs ));







		double likeBefore=b.localLogLikelihood(b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);

		double result=0;
		Cursor<FloatType> cursor= b.expectedValuesRoi.cursor();	
		double xyToZ= Model.getInstance().getXyToZ();
		double xg=0;;
		double yg=0;
		double zg=0;
		double akkug=0;
		while ( cursor.hasNext() )	{

			cursor.fwd();
			double e=(cursor.get().get());
			double xPos=cursor.getIntPosition(0);double yPos=cursor.getIntPosition(1);double zPos=0;
			if(isVolume) zPos=cursor.getIntPosition(2);

			xg+=e*xPos;
			yg+=e*yPos;
			zg+=e*zPos;
			akkug+=e;


		}
		xg/=akkug; yg/=akkug; zg/=akkug;


		double likeAfter=b.localLogLikelihood(xg, yg, zg, b.sigma, b.sigmaZ);



		if(likeBefore<likeAfter){
			b.xPos=xg;
			b.yPos=yg;
			b.zPos=zg;

		}




		b.counter=0;

		boolean findSigma=b.autoSigma;
		double []startPoint=null;
		if(findSigma&&isVolume){
			startPoint=new double [4];
			startPoint[0]=b.xPos;
			startPoint[1]=b.yPos;
			startPoint[2]=b.zPos;
			startPoint[3]=	b.sigma*b.sigma;
		}
		if(!findSigma&&isVolume){
			startPoint=new double [3];
			startPoint[0]=b.xPos;
			startPoint[1]=b.yPos;
			startPoint[2]=b.zPos;
		}
		if(findSigma&& !isVolume){
			startPoint=new double [3];
			startPoint[0]=b.xPos;
			startPoint[1]=b.yPos;
			startPoint[2]=b.sigma*b.sigma;
		}
		if(!findSigma&& !isVolume){
			startPoint=new double [2];
			startPoint[0]=b.xPos;
			startPoint[1]=b.yPos;
		}


		PowellOptimizer optimizer = new PowellOptimizer(1, 1);		


		double [][] bounds= new double [startPoint.length][2];
		bounds[0][0]=iterableFrame.realMin(0);bounds[1][0]=iterableFrame.realMax(0);
		bounds[0][1]=iterableFrame.realMin(1);bounds[1][1]=iterableFrame.realMax(1);

		double []lowerB={iterableFrame.realMin(0),iterableFrame.realMin(1)};
		double []upperB={iterableFrame.realMax(0),iterableFrame.realMax(1)};

		double []output=null;
		output= optimizer.optimize(10000000, b, GoalType.MAXIMIZE, startPoint).getPoint();

		b.newX=output[0];
		b.newY=output[1];
		if(isVolume)b.newZ=output[2];
		if(findSigma&& !isVolume) b.newSig=Math.max(b.minSigma,Math.min(b.maxSigma,Math.pow(output[2],0.5 )));
		if(findSigma&& isVolume) b.newSig=Math.max(b.minSigma,Math.min(b.maxSigma,Math.pow(output[3],0.5 )));

		b.newPK=(b.newInten/totalInten);










		return;
	}


	private class MstepThread extends Thread{

		public Blob blob;
		public double totalInten;		
		public IterableRandomAccessibleInterval<IT> iterableFrame;

		public MstepThread(Blob b, double ti, IterableRandomAccessibleInterval<IT> iFrame){
			blob=b;
			totalInten=ti;
			iterableFrame=iFrame;

		}
		@Override
		public void run(){
			try{
				doMstepForBlob(blob, totalInten, iterableFrame);
			}catch(Exception e){
				e.printStackTrace(Model.errorWriter);
				Model.errorWriter.flush();
			}
		}
	}

	private double doMstep(double totalInten,  List <Blob> trackables, Double backProb, 
			IterableRandomAccessibleInterval<IT> iFrame, BlobSession<?> session){


		double change=0;
		List<MstepThread> threads=new ArrayList<MstepThread>();
		for(Blob b:trackables){   
			MstepThread t= new MstepThread(b,totalInten, iFrame);
			threads.add(t);
			t.setPriority(Thread.MIN_PRIORITY);
			t.start();

		}


//			t.run();
	//	change=Math.max(change, this.doMstepForBlob(b, totalInten));	
	//		totalBlobsInten+=b.inten;

		for(MstepThread t:threads){
			try{
				t.join();
			}catch(Exception e){}


		}



		double couplePK=0;
		double coupleCount=0;
		for(Blob b:trackables){
			if(b.coupled){
				coupleCount++;
				couplePK+=b.newPK;
			}
		}

		for(Blob b:trackables){
			if(b.coupled){
				b.newPK=couplePK/coupleCount;
			}
		}
		backProb=1.0;
		for(Blob b:trackables){  
			backProb-=b.newPK;

			double changePos=Math.sqrt(
					(b.newX-b.xPos)*(b.newX-b.xPos)+
					(b.newY-b.yPos)*(b.newY-b.yPos)+
					(b.newZ-b.zPos)*(b.newZ-b.zPos)
					);
			change=Math.max(Math.abs( changePos),change);
			change=Math.max(session.getChangeFactorSigma()*Math.abs((b.newSig*b.newSig-b.sigma*b.sigma)), change);
			change=Math.max(session.getChangeFactorPK()*Math.abs(b.newPK-b.pK), change);
			b.xPos=b.newX;
			b.yPos=b.newY;
			b.zPos=b.newZ;
			b.sigma=b.newSig;
			b.inten=b.newInten;
			b.pK=b.newPK;
			b.denom=b.calcDenominator(iFrame, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ); // important for EMCCD
		}

		for(Blob b:trackables){
			b.backInten=totalInten*backProb;
			b.totalInt=totalInten;
		}


		return change;

	}

	public int calcNumberOfPixels(Interval img){
		int akku=1;

		for(int i=0;i<img.numDimensions();i++ ){
			akku*=(((int)img.max(i)-(int)img.min(i))+1);
		}

		return akku;
	}

	protected double doOptimizationSingleScale( List<Blob> trackables,
			RandomAccessibleInterval <IT> movieFrame,  double qualityT, int constBackGround,
			int maxIterations, BlobSession<?> session){
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();	
		double backProb=1.0;


		for(Blob b:trackables){
			b.expectedValues= imgFactory.create(movieFrame, new FloatType());
			backProb-=b.pK;
		}



		long time0= System.nanoTime();	
		long eTime=0;	
		long mTime=0;
		double ti=0;
		for(int i=0;i<maxIterations;i++){
			backProb=1.0;


			IterableRandomAccessibleInterval<IT> iFrame= makeIterableFrame( movieFrame,  trackables);
			int nop=calcNumberOfPixels(iFrame);
			for(Blob b:trackables){
				backProb-=b.pK;	
				b.numberOfPixels=nop;
			}

			ti = doEStep(trackables,backProb,iFrame,  constBackGround);
			double change=0;

			change=this.doMstep(ti, trackables,backProb, iFrame, session);
			if(change<qualityT ||!Model.getInstance().isCurrentlyTracking()) break;


		}
		return ti;
	}

	@Override
	public void optimizeFrame(boolean multiscale, List<Blob> trackables,
			MovieFrame<IT> movieFrame,  double qualityT, Session<Blob,IT> session) {

		BlobSession<IT> blobS= (BlobSession<IT>) session;

		if(multiscale){
			ImgFactory <FloatType>floatFactory= new ArrayImgFactory<FloatType>();
			Img<FloatType>srcFloat=floatFactory.create(movieFrame.getZProjections(), new FloatType());
			ImglibTools.convert(movieFrame.getZProjections(), srcFloat);

			double maxSigma=0;
			double minSigma=Double.MAX_VALUE;
			double maxSigmaZ=0;
			for(Blob b:trackables){
				maxSigmaZ=Math.max(maxSigmaZ, b.sigmaZ);
				maxSigma=Math.max(maxSigma, b.sigma);
				minSigma=Math.min(minSigma, b.sigma);
			}



			srcFloat=ImglibTools.differenceOfGaussians(srcFloat, maxSigma*1.2, minSigma*0.8);
			//    ImageJFunctions.show(srcFloat);


			double gaussianStd =blobS.getMscaleSigma();
			double sf =blobS.getDownscaleFactor();
			double steps=blobS.getMscaleIterations();
			double minDimension=Math.min(srcFloat.dimension(0),srcFloat.dimension(1));
			while(minDimension*Math.pow(sf,steps)<10){
				steps--;
			}

			List <Img<FloatType>> pyramid= ImglibTools.generatePyramid(srcFloat, (int)steps, gaussianStd, sf);	


			List<Blob> tempBlobs= new ArrayList<Blob>();
			for(Blob b:trackables){
				Blob tb= this.copy(b);
				tb.autoSigma=false;
				tempBlobs.add(tb);
			}

			for(double iter=0;iter<steps;iter++){
				for(int i=0;i<trackables.size();i++){

					Blob tb= tempBlobs.get(i);
					tb.sigma=Math.sqrt(tb.sigma*tb.sigma +gaussianStd*gaussianStd);
					tb.sigma*=sf;
					tb.maxSigma=Math.sqrt(tb.maxSigma*tb.maxSigma +gaussianStd*gaussianStd);
					tb.maxSigma*=sf;
					tb.minSigma=Math.sqrt(tb.minSigma*tb.minSigma +gaussianStd*gaussianStd);
					tb.minSigma*=sf;
					tb.xPos=tb.xPos*sf;
					tb.yPos=tb.yPos*sf;
				}
			}



			MaximumLikelihoodBlobPolicy<FloatType> bp= new MaximumLikelihoodBlobPolicy<FloatType>();
			for(Img<FloatType> currentScale: pyramid){
				for(Blob tb:tempBlobs){

					tb.xPos=Math.min(Math.max(0,tb.xPos),currentScale.max(0));
					tb.yPos=Math.min(Math.max(0,tb.yPos),currentScale.max(1));
				}

				bp.doOptimizationSingleScale(tempBlobs, currentScale, 0.01, 0, 100, (BlobSession<?>)session);
				//			IJ.error("done with optimization");
				for(int i=0;i<tempBlobs.size();i++){
					Blob tb= tempBlobs.get(i);


					tb.sigma/=sf;
					tb.sigma=Math.sqrt(tb.sigma*tb.sigma -(gaussianStd)*(gaussianStd));		
					tb.maxSigma/=sf;
					tb.maxSigma=Math.sqrt(tb.maxSigma*tb.maxSigma -(gaussianStd)*(gaussianStd));		
					tb.minSigma/=sf;
					tb.minSigma=Math.sqrt(tb.minSigma*tb.minSigma -(gaussianStd)*(gaussianStd));		

					tb.xPos=tb.xPos/sf;
					tb.yPos=tb.yPos/sf;



				}
			}


			for(double iter=0;iter<steps;iter++){
				for(int i=0;i<trackables.size();i++){
					Blob ob= trackables.get(i);
					Blob tb= tempBlobs.get(i);				
					ob.xPos=tb.xPos;
					ob.yPos=tb.yPos;
				}
			}





			if(Model.getInstance().isVolume()){
				Img<FloatType>threedFloat=floatFactory.create(movieFrame.getFrameView(), new FloatType());
				ImglibTools.convert(movieFrame.getFrameView(), threedFloat);
				double sig[]= {maxSigma*2,maxSigma*2,maxSigma*2};
				threedFloat=Gauss.inNumericType(sig, threedFloat);
				for(int i=0;i<trackables.size();i++){
					Blob tb= trackables.get(i);
					tb.zPos=Model.getInstance().getXyToZ()*
							ImglibTools.findBrightestPixelInColumn(threedFloat,(int) tb.xPos,(int) tb.yPos);
				}
			}
		}
		doOptimizationSingleScale(trackables, movieFrame.getFrameView(),qualityT,
				Model.getInstance().getIntensityOffset(),1000, (BlobSession<IT>)session);
	}



	@Override
	public String getLabelForAlternateTracking(){
		return "Multiscale";
	}


}
