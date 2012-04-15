package blobTracking;

import ij.IJ;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.SimpleScalarValueChecker;
import org.apache.commons.math.optimization.direct.PowellOptimizer;

import tools.ImglibTools;
import frameWork.Model;
import frameWork.MovieFrame;

public class MaximumLikelihoodBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends BlobPolicy<IT>{

	@Override
	public String getTypeName() {
		return "Blob";
	}

	private IterableRandomAccessibleInterval<IT> makeIterableFrame(RandomAccessibleInterval <IT> movieFrame,  List <Blob> trackables){
		boolean isVolume= movieFrame.numDimensions()>2;
		long[] mins=  {Long.MAX_VALUE,Long.MAX_VALUE,0};	
		int max2=1;
		if(isVolume)max2=(int) movieFrame.max(2);
		long[] maxs=  {Long.MIN_VALUE,Long.MIN_VALUE,max2};
			
		for(Blob b:trackables){
			
	
			b.inten=0;
			
			mins[0]=Math.min(mins[0],(long) (b.xPos-b.sigma*3-3));
			mins[1]=Math.min(mins[1],(long) (b.yPos-b.sigma*3-3));
			maxs[0]=Math.max(maxs[0],(long) (b.xPos+b.sigma*3+3));
			maxs[1]=Math.max(maxs[1],(long) (b.yPos+b.sigma*3+3));
			
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
	
		for(Blob b:trackables)	
			b.denom=b.calcDenominator(iterableFrame, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
		
	//	IterableInterval<IT> iterableFrame= new IterableRandomAccessibleInterval<IT>(movieFrame.getFrameView());
		Cursor<IT> cursor =iterableFrame.cursor();
		
		
		double pX=0;
		
	
		while ( cursor.hasNext() )	{
	    	cursor.fwd();
	    	pX=backProb/ImglibTools.getNumOfPixels(iterableFrame); // init with probability for background

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
	    		
	    	float value= Math.max(0,cursor.get().getRealFloat()- constBackground);//*(float)sn; 
	    	totalInten+=value;
	    	
	    	for(Blob b:trackables){
	    		pX+=b.pXandK(x, y, z, 
	    				b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ,
	    				b.denom);
	    	}
	    	
	    	for(Blob b:trackables){
	    		RandomAccess<FloatType> ra= b.expectedValues.randomAccess();
	    		ra.setPosition(cursor);
	    		double currentInten=value*
	    				b.pXandK(x, y, z,
	    				b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ,
	    				b.denom)/pX;
	    		
	    		
	
	    		ra.get().set((float)(currentInten  ) );
	    		b.inten+=currentInten;    		
	    	}
	    	
	    	
	    		
		}
		return totalInten;
	}

	
	
	private double doMstepForBlob(Blob b, double totalInten, IterableRandomAccessibleInterval<IT> iterableFrame){
		double change=0;
		double newX=0;
		double newY=0;
		double newSig=0;
		double newZ=0;
		
		long[] mins=null;
		long[] maxs=null;
		if(b.expectedValues.numDimensions()>2){
			mins = new long[3];		
			mins[2]= b.expectedValues.min(2);
			
			maxs = new long[3];
			maxs[2]= b.expectedValues.max(2);
		}else{
			mins = new long[2];
			maxs = new long[2];
		}
		
		mins[0]=(long)Math.max(iterableFrame.min(0), b.xPos-b.sigma*3-3 );
		mins[1]= (long)	Math.max(iterableFrame.min(1), b.yPos-b.sigma*3-3 );
		
		maxs[0]=(long)Math.min(iterableFrame.max(0), b.xPos+b.sigma*3+3 );
		maxs[1]= (long)	Math.min(iterableFrame.max(1), b.yPos+b.sigma*3 +3);
		
		
	//	long[] mins=  {(long)Math.max(iterableFrame.min(0), b.xPos-b.sigma*3-3 ),(long)
	//			Math.max(iterableFrame.min(1), b.yPos-b.sigma*3-3 ), b.expectedValues.min(2)};
		
		
	//	long[] maxs=  {(long)Math.min(iterableFrame.max(0), b.xPos+b.sigma*3+3 ),(long)
	//			Math.min(iterableFrame.max(1), b.yPos+b.sigma*3 +3), b.expectedValues.max(2)};
			
		b.expectedValuesRoi=new IterableRandomAccessibleInterval<FloatType>(Views.interval(b.expectedValues,mins,maxs ));



    	b.counter=0;
		PowellOptimizer optimizer = new PowellOptimizer(1,1);
		optimizer.setConvergenceChecker(new SimpleScalarValueChecker() );
  //  	SimplexOptimizer optimizer = new SimplexOptimizer();
    		    	
		boolean findSigma=b.autoSigma;
		double []startPoint=null;
	    if(findSigma&& Model.getInstance().isVolume()){
	    	startPoint=new double [4];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=b.yPos;
	    	startPoint[2]=b.zPos;
	    	startPoint[3]=	b.sigma*b.sigma;
	    }
	    if(!findSigma&& Model.getInstance().isVolume()){
	    	startPoint=new double [3];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=b.yPos;
	    	startPoint[2]=b.zPos;
	    }
	    if(findSigma&& !Model.getInstance().isVolume()){
	    	startPoint=new double [3];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=b.yPos;
	    	startPoint[2]=b.sigma*b.sigma;
	    }
	    if(!findSigma&& !Model.getInstance().isVolume()){
	    	startPoint=new double [2];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=b.yPos;
	    }
		
			
		
	//	optimizer.setSimplex(new  NelderMeadSimplex(3));
		double []output = optimizer.optimize(100000, b, GoalType.MAXIMIZE, startPoint).getPoint();
		
		
		newX=output[0];
		newY=output[1];
		if(Model.getInstance().isVolume())newZ=output[2];
		if(findSigma&& !Model.getInstance().isVolume()) newSig=Math.max(b.minSigma,Math.min(b.maxSigma,Math.pow(output[2],0.5 )));
		if(findSigma&& Model.getInstance().isVolume()) newSig=Math.max(b.minSigma,Math.min(b.maxSigma,Math.pow(output[3],0.5 )));
		
	
    	change=Math.max(Math.abs((newX-b.xPos)),change);
    	change=Math.max(Math.abs((newY-b.yPos)), change);
    	if(Model.getInstance().isVolume()) change=Math.max(Math.abs((newZ-b.zPos)), change);
    	if(findSigma) change=Math.max(Math.abs((newSig*newSig-b.sigma*b.sigma)), change);
    	change=Math.max(Math.abs(((b.inten/totalInten)-b.pK)/b.pK), change);
 	
    	
    Model.getInstance().rwLock.writeLock().lock();
    	b.xPos=newX;
    	b.yPos=newY;
    	b.zPos=newZ;
    	if(findSigma) b.sigma=newSig;
//    	b.zPos=newZ/inten;
    	b.pK=b.inten/totalInten;
    Model.getInstance().rwLock.writeLock().unlock();	
    	
    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~pk: " +b.pK);
    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~totalInten: " +totalInten);
     	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~inten: " +b.inten);
		
		return change;
	}
	
	
	private class MstepThread extends Thread{
		public double localChange=0;
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
			localChange=doMstepForBlob(blob, totalInten, iterableFrame);
		}
	}
	
	private double doMstep(double totalInten,  List <Blob> trackables, Double backProb, IterableRandomAccessibleInterval<IT> iFrame){
		
		
		double change=0;
		double totalBlobsInten=0;
		List<MstepThread> threads=new ArrayList<MstepThread>();
		for(Blob b:trackables){   
			MstepThread t= new MstepThread(b,totalInten, iFrame);
			threads.add(t);
		t.start();
	//		t.run();
	//	change=Math.max(change, this.doMstepForBlob(b, totalInten));	
	//		totalBlobsInten+=b.inten;
    	}
		
		for(MstepThread t:threads){
			try{
			t.join();
			}catch(Exception e){}
			change=Math.max(change,t.localChange);
			
		}
		
		backProb=1.0;
		for(Blob b:trackables){  
			backProb-=b.pK;
		}
		
		
		return change;
	}

	private void doOptimizationSingleScale( List<Blob> trackables,
			RandomAccessibleInterval <IT> movieFrame,  double qualityT, int constBackGround,
			int maxIterations){
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();	
		Double backProb=1.0;
		
		for(Blob b:trackables){
			
			b.expectedValues= imgFactory.create(movieFrame, new FloatType());
			backProb-=b.pK;
		}
	
		
		
			long time0= System.nanoTime();	
			long eTime=0;	
			long mTime=0;
			for(int i=0;i<maxIterations;i++){
				
				IterableRandomAccessibleInterval<IT> iFrame= makeIterableFrame( movieFrame,  trackables);
					
					
				
					long eTime0= System.nanoTime();
					double ti= doEStep(trackables,backProb,iFrame,  constBackGround);
			//		ImageJFunctions.show (trackables.get(0).expectedValues, "ev");
			//		IJ.error("stop");
					long eTime1= System.nanoTime();
					double change=0;
			
					long mTime0= System.nanoTime();
					change=this.doMstep(ti, trackables,backProb, iFrame);
					//System.out.println("value:"+ trackables.get(0).localLogLikelihood());
					long mTime1= System.nanoTime();
					
					mTime +=mTime1-mTime0;
					eTime +=eTime1-eTime0;
					
					System.out.println("change:" +change);			
					if(change<qualityT) break;
			}
			long time1= System.nanoTime();
			long time= (time1-time0)/1000000;
			eTime/=1000000;
			mTime/=1000000;
			
			System.out.println("totalTime:" +time+ "  fraction E:"+ ((double)eTime/(double)time)+ "  fraction M:"+ ((double)mTime/(double)time) );

	}
	
	@Override
	public void optimizeFrame(boolean cheap, List<Blob> trackables,
			MovieFrame<IT> movieFrame,  double qualityT) {
			ImgFactory <IT>factory= new ArrayImgFactory<IT>();
			Img<IT>src= factory.create(movieFrame.getFrameView(), movieFrame.getFrameView().randomAccess().get());
			ImglibTools.resize(movieFrame.getFrameView(), src);
			
			double gaussianStd =0.5;
			double sf =0.9;
			double steps=4;
			List <Img<IT>> pyramid= ImglibTools.generatePyramid(src, (int)steps, gaussianStd, sf);	
			
			
	//		List<Blob> tempBlobs= new ArrayList<Blob>();
	//		for(Blob b:trackables){
	//			tempBlobs.add(this.copy(b));
	//		}
			for(double iter=0;iter<steps;iter++){
				for(int i=0;i<trackables.size();i++){
					Blob ob= trackables.get(i);
					ob.sigma=Math.sqrt(ob.sigma*ob.sigma +gaussianStd*gaussianStd);
					ob.sigma*=sf;
					ob.xPos=ob.xPos*sf;
					ob.yPos=ob.yPos*sf;
				}
			}
			
			
			double scaleIndex=steps;
			for(Img<IT> currentScale: pyramid){
		//		IJ.error("starting optimization");
				doOptimizationSingleScale(trackables, currentScale, 0.1, movieFrame.getConstBackground(), 25);
	//			IJ.error("done with optimization");
				for(int i=0;i<trackables.size();i++){
					
					Blob tb= trackables.get(i);
					tb.sigma/=sf;
					tb.sigma=Math.sqrt(tb.sigma*tb.sigma -(gaussianStd)*(gaussianStd));		
					
					tb.xPos=tb.xPos/sf;
					tb.yPos=tb.yPos/sf;
				}
				
				scaleIndex--;
			}
	
			doOptimizationSingleScale(trackables, movieFrame.getFrameView(), 0.01, movieFrame.getConstBackground(),100);
	}

	
	
	
}
