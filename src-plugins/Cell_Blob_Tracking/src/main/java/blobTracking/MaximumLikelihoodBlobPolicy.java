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

	private static double  BORDERSIZE=20;
	protected long numOfPixelsUsed;



	@Override
	public String getTypeName() {
		return "M.L.GaussianTracking";
	}

	private IterableRandomAccessibleInterval<IT> makeIterableFrame(RandomAccessibleInterval <IT> movieFrame,  List <Blob> trackables){
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
			
		
	//	IterableInterval<IT> iterableFrame= new IterableRandomAccessibleInterval<IT>(movieFrame.getFrameView());
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
	   // 	
	 /*   	
	    	boolean isIn=false;
	    	for(Blob b:trackables){
	    		isIn=(b.getDistanceTo(x, y, 0)>(b.sigma*3+3)*(b.sigma*3+3));
	    		if(isIn) break;
	    	}
	    	if(!isIn) continue;
	  */  	
	    	
	    	
	    //	double dist= Math.sqrt((cx-x)*(cx-x) +(cy-y)*(cy-y) );
	    //	double sn=1;
	   // 	double overlap=dist-(-iterableFrame.min(0)+iterableFrame.max(0))/2.0-1;
	   // 	if(overlap>0) sn=Math.max(0, sn-(overlap*2));
	    		
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
			
		//	double akku=0;
		//	for(int i=0; i<previous.getPoint().length;i++){
		//		akku+=Math.abs(previous.getPoint()[i]-current.getPoint()[i]);
		//	}
			
		//	System.out.println("                akku:"+akku+ " iteration:"+ iteration);
			if (iteration>10  &&firstV<=current.getValue()){
				Model.getInstance().depositMsg("iterations:"+iteration);
				return true;
			}
			
	//		if(akku<0.00001) return true;
			
	//		return akku<0.01;
			return false;
		}



		
		
	}
	
	private void doMstepForBlob(Blob b, double totalInten, IterableRandomAccessibleInterval<IT> iterableFrame){
	//	System.out.println("1");
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
		//	mins[2]= b.expectedValues.min(2);
			mins[2]= (long)	Math.max(b.expectedValues.min(2), (b.zPos-b.sigmaZ*3)/Model.getInstance().getXyToZ()-1 );
			
			maxs = new long[3];
		//	maxs[2]= b.expectedValues.max(2);
			maxs[2]= (long)	Math.min(b.expectedValues.max(2), (b.zPos+b.sigmaZ*3)/Model.getInstance().getXyToZ() +1);
		}else{
			mins = new long[2];
			maxs = new long[2];
		}
		
	//	System.out.println("2");
		
		mins[0]=(long)Math.max(b.expectedValues.min(0), b.xPos-b.sigma*3-BORDERSIZE );
		mins[1]= (long)	Math.max(b.expectedValues.min(1), b.yPos-b.sigma*3-BORDERSIZE );
		
		
		maxs[0]=(long)Math.min(b.expectedValues.max(0), b.xPos+b.sigma*3+BORDERSIZE );
		maxs[1]= (long)	Math.min(b.expectedValues.max(1), b.yPos+b.sigma*3 +BORDERSIZE);
		
		
		
		
		
		
		
		
		
//		mins[0]=(long)b.expectedValues.min(0) ;
//		mins[1]=(long)b.expectedValues.min(1);
		
		
//		maxs[0]=(long)b.expectedValues.max(0);
//		maxs[1]= (long)	b.expectedValues.max(1);
		
	//	System.out.println("3");
		
		
	//	long[] mins=  {(long)Math.max(iterableFrame.min(0), b.xPos-b.sigma*3-3 ),(long)
	//			Math.max(iterableFrame.min(1), b.yPos-b.sigma*3-3 ), b.expectedValues.min(2)};
		
		
	//	long[] maxs=  {(long)Math.min(iterableFrame.max(0), b.xPos+b.sigma*3+3 ),(long)
	//			Math.min(iterableFrame.max(1), b.yPos+b.sigma*3 +3), b.expectedValues.max(2)};
			
	//	try{
	/*	
		System.out.println("maxs.length: "+ maxs.length);
		System.out.println("mins.length: "+ mins.length);
		System.out.println("numDimensions: "+ b.expectedValues.numDimensions());
	
		for(int i=0; i<maxs.length;i++){
			System.out.println("maxs["+i+"]: "+ maxs[i]);
			System.out.println("mins["+i+"]: "+ mins[i]);
			System.out.println("b.max("+i+"): "+ b.expectedValues.max(i));
			System.out.println("b.min("+i+"): "+ b.expectedValues.min(i));
			
		}
		System.out.println("b.xPos: "+ b.xPos);
		System.out.println("b.yPos: "+ b.yPos);
	*/	
		b.expectedValuesRoi=new IterableRandomAccessibleInterval<FloatType>(Views.interval(b.expectedValues,mins,maxs ));
//		}catch(Exception e){
	//	System.out.println("4");
			
//			IJ.error("stop");
//		}


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
		

  //  	SimplexOptimizer optimizer = new SimplexOptimizer();
	    double [][] bounds= new double [startPoint.length][2];
	    bounds[0][0]=iterableFrame.realMin(0);bounds[1][0]=iterableFrame.realMax(0);
	    bounds[0][1]=iterableFrame.realMin(1);bounds[1][1]=iterableFrame.realMax(1);
	    
	    double []lowerB={iterableFrame.realMin(0),iterableFrame.realMin(1)};
	    double []upperB={iterableFrame.realMax(0),iterableFrame.realMax(1)};
	    
//	    BOBYQAOptimizer optimizer= new BOBYQAOptimizer(6);
   // 	CMAESOptimizer optimizer= new CMAESOptimizer(0,null,bounds);
  //  	NonLinearConjugateGradientOptimizer optimizer
   // 	= new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.POLAK_RIBIERE);
  //  	optimizer.setInitialStep(0.001);
    	
    
	    //  	SimplexOptimizer optimizer = new SimplexOptimizer();
	//	optimizer.setSimplex(new   MultiDirectionalSimplex(startPoint.length));
	//    optimizer.setSimplex(new   NelderMeadSimplex(startPoint.length));
	    
		double []output=null;
	//	try{
	//	BOBYQAOptimizer optimizer= new BOBYQAOptimizer(startPoint.length);
		
    //	optimizer.setConvergenceChecker(new myConvChecker());
    	
		output= optimizer.optimize(10000000, b, GoalType.MAXIMIZE, startPoint).getPoint();
			
	//	}catch(Exception e){};
		
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
	//		t.run();
	//	change=Math.max(change, this.doMstepForBlob(b, totalInten));	
	//		totalBlobsInten+=b.inten;
    	}
		
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
	    	//change=Math.max(session.getChangeFactorPK()*Math.abs(b.newInten-b.inten), change);
	 //   	System.out.println("intenchange:"+Math.abs(b.newInten-b.inten));
	    	
	    	b.xPos=b.newX;
	    	b.yPos=b.newY;
	    	b.zPos=b.newZ;
	    	b.sigma=b.newSig;
	    	b.inten=b.newInten;
	    	b.pK=b.newPK;
	    	b.denom=b.calcDenominator(iFrame, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ); // important for EMCCD
	  //  	if(totalInten<b.inten){
	//    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~pk: " +b.pK);
	//    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~totalInten: " +totalInten);
	 //    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~inten: " +b.inten);
	  //  	}
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
				
			//	backProb=1.0;	
			//	for(Blob b:trackables){
					//b.expectedValues= imgFactory.create(movieFrame, new FloatType());
			//		backProb-=b.pK;
			//	}
				
	//				long eTime0= System.nanoTime();
					ti = doEStep(trackables,backProb,iFrame,  constBackGround);
			//		ImageJFunctions.show (trackables.get(0).expectedValues, "ev");					
			//		IJ.error("stop");
		//			long eTime1= System.nanoTime();
					double change=0;
			
	//				long mTime0= System.nanoTime();
					change=this.doMstep(ti, trackables,backProb, iFrame, session);
					//System.out.println("value:"+ trackables.get(0).localLogLikelihood());
	//				long mTime1= System.nanoTime();
					
		//			mTime +=mTime1-mTime0;
		//			eTime +=eTime1-eTime0;
					
		//			System.out.println("change:" +change);		
		//			Model.getInstance().depositMsg("change: "+change);
		//			Model.getInstance().makeChangesPublic();
				
					if(change<qualityT ||!Model.getInstance().isCurrentlyTracking()) break;
					
					
			//		for(Blob b:trackables){
					//	b.iterations=Math.max(b.iterations, i);
			//			backProb-=b.pK;
			//		}
					
			}
	//		long time1= System.nanoTime();
	//		long time= (time1-time0)/1000000;
	//		eTime/=1000000;
	//		mTime/=1000000;
			
	//		System.out.println("totalTime:" +time+ "  fraction E:"+ ((double)eTime/(double)time)+ "  fraction M:"+ ((double)mTime/(double)time) );

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
				
				
				
				
			//	return;
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
				//ImglibTools.differenceOfGaussians(threedFloat, maxSigma*1.1, minSigma*0.9);
				for(int i=0;i<trackables.size();i++){
					Blob tb= trackables.get(i);
					tb.zPos=Model.getInstance().getXyToZ()*
							//(double) ImglibTools.findBrightestPixelInColumn(movieFrame.getFrameView(),(int) tb.xPos,(int) tb.yPos);

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
