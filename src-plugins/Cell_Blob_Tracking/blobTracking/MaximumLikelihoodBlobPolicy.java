package blobTracking;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
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
import frameWork.MovieFrame;

public class MaximumLikelihoodBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends BlobPolicy<IT>{

	@Override
	public String getTypeName() {
		return "Blob";
	}

	private IterableRandomAccessibleInterval<IT> makeIterableFrame(MovieFrame<IT> movieFrame,  List <Blob> trackables){
		long[] mins=  {Long.MAX_VALUE,Long.MAX_VALUE};		
		long[] maxs=  {Long.MIN_VALUE,Long.MIN_VALUE};
			
		for(Blob b:trackables){
			
	
			b.inten=0;
			System.out.println(" mins[0]:"+ mins[0]);
			mins[0]=Math.min(mins[0],(long) (b.xPos-b.sigma*3-3));
			mins[1]=Math.min(mins[1],(long) (b.yPos-b.sigma*3-3));
			maxs[0]=Math.max(maxs[0],(long) (b.xPos+b.sigma*3+3));
			maxs[1]=Math.max(maxs[1],(long) (b.yPos+b.sigma*3+3));
			System.out.println(" mins[0] after:"+ mins[0]);
		}
		
		long frameMinX=movieFrame.getFrameView().min(0);
		long frameMinY=movieFrame.getFrameView().min(1);
		long frameMaxX=movieFrame.getFrameView().max(0);
		long frameMaxY=movieFrame.getFrameView().max(1);
		
		mins[0]=Math.max(mins[0],frameMinX);
		mins[1]=Math.max(mins[1],frameMinY);
		maxs[0]=Math.min(maxs[0],frameMaxX);
		maxs[1]=Math.min(maxs[1],frameMaxY);
		
		IterableRandomAccessibleInterval<IT> iterableFrame=new IterableRandomAccessibleInterval<IT>(Views.interval(movieFrame.getFrameView(),mins,maxs ));

		
		return iterableFrame;
	}
	
	private double doEStep( List <Blob> trackables, MovieFrame<IT> movieFrame, Double backProb, IterableRandomAccessibleInterval<IT> iterableFrame){
		double totalInten=0;
	
		for(Blob b:trackables)	
			b.calcDenominator(iterableFrame);
		
	//	IterableInterval<IT> iterableFrame= new IterableRandomAccessibleInterval<IT>(movieFrame.getFrameView());
		Cursor<IT> cursor =iterableFrame.cursor();
		
		
		double pX=0;
		
		
		while ( cursor.hasNext() )	{
	    	cursor.fwd();
	    	pX=backProb/ImglibTools.getNumOfPixels(iterableFrame); // init with probability for background

	    	int x=cursor.getIntPosition(0);
	    	int y=cursor.getIntPosition(1);
	    //	int z=cursor.getIntPosition(2);
	    	int z=0;
	 /*   	
	    	boolean isIn=false;
	    	for(Blob b:trackables){
	    		isIn=(b.getDistanceTo(x, y, 0)>(b.sigma*3+3)*(b.sigma*3+3));
	    		if(isIn) break;
	    	}
	    	if(!isIn) continue;
	  */  	
	    	
	    	float value= Math.max(0,cursor.get().getRealFloat()- movieFrame.getConstBackground()); 
	    	totalInten+=value;
	    	
	    	for(Blob b:trackables){    		    		
	    		pX+=b.pXandK(x, y, z);
	    		
	    	}
	    	
	    	for(Blob b:trackables){    		    		
	    		RandomAccess<FloatType> ra= b.expectedValues.randomAccess();
	    		ra.setPosition(cursor);
	    		double currentInten=value*b.pXandK(x, y, z)/pX;
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
		
		long[] mins=  {(long)Math.max(iterableFrame.min(0), b.xPos-b.sigma*3 ),(long)
				Math.max(iterableFrame.min(1), b.yPos-b.sigma*3 )};
		
		System.out.println(" mins[0] mstep:"+ mins[0]);
		
		long[] maxs=  {(long)Math.min(iterableFrame.max(0), b.xPos+b.sigma*3 ),(long)
				Math.min(iterableFrame.max(1), b.yPos+b.sigma*3 )};
			
		b.expectedValuesRoi=new IterableRandomAccessibleInterval<FloatType>(Views.interval(b.expectedValues,mins,maxs ));

		Cursor<FloatType> cursor= b.expectedValuesRoi.cursor(); 
  

    	b.counter=0;
		PowellOptimizer optimizer = new PowellOptimizer(1,1);
		optimizer.setConvergenceChecker(new SimpleScalarValueChecker() );
  //  	SimplexOptimizer optimizer = new SimplexOptimizer();
    		    	
		boolean findSigma=false;
		double []startPoint;
	    if(findSigma){
	    	startPoint=new double [3];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=	b.yPos;
	    	startPoint[2]=	b.sigma*b.sigma;
	    }else{
	    	startPoint=new double [2];
	    	startPoint[0]=b.xPos;
	    	startPoint[1]=	b.yPos;
	    }
		
		
			
		
	//	optimizer.setSimplex(new  NelderMeadSimplex(3));
		double []output = optimizer.optimize(100000, b, GoalType.MAXIMIZE, startPoint).getPoint();
		
		
		newX=output[0];
		newY=output[1];
		if(findSigma) newSig=Math.max(0.5,Math.min(2,Math.sqrt(output[2]) ));
		
	
    	change=Math.max(Math.abs((newX-b.xPos)),change);
    	change=Math.max(Math.abs((newY-b.yPos)), change );
    	if(findSigma) change=Math.max(Math.abs((newSig*newSig-b.sigma*b.sigma)), change);
    	change=Math.max(Math.abs(((b.inten/totalInten)-b.pK)/b.pK), change);
 	
    	b.xPos=newX;
    	b.yPos=newY;
    	if(findSigma) b.sigma=newSig;
//    	b.zPos=newZ/inten;
    	b.pK=b.inten/totalInten;
    	
    	
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
			totalBlobsInten+=t.localChange;
		}
		
		backProb=1-(totalBlobsInten/totalInten);
		return change;
	}

	@Override
	public void optimizeFrame(boolean cheap, List<Blob> trackables,
			MovieFrame<IT> movieFrame, BlobSession<IT> bs) {
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();	
		Double backProb=1.0;
		
		for(Blob b:trackables){
			b.expectedValues= imgFactory.create(movieFrame.getFrameView(), new FloatType());
			backProb-=b.pK;
		}
	
		
			long time0= System.nanoTime();	
			long eTime=0;	
			long mTime=0;
			for(int i=0;i<100;i++){	
				
				IterableRandomAccessibleInterval<IT> iFrame= makeIterableFrame( movieFrame,  trackables);
				
					long eTime0= System.nanoTime();
					double ti= doEStep(trackables,movieFrame,backProb,iFrame);
					long eTime1= System.nanoTime();
					double change;
					
					long mTime0= System.nanoTime();
					change=this.doMstep(ti, trackables,backProb, iFrame);
					long mTime1= System.nanoTime();
					
					mTime +=mTime1-mTime0;
					eTime +=eTime1-eTime0;
					
					System.out.println("change:" +change);			
					if(change<0.01) break;
			}
			long time1= System.nanoTime();
			long time= (time1-time0)/1000000;
			eTime/=1000000;
			mTime/=1000000;
			
			System.out.println("totalTime:" +time+ "  fraction E:"+ ((double)eTime/(double)time)+ "  fraction M:"+ ((double)mTime/(double)time) );
			
	}

	
	
	
}
