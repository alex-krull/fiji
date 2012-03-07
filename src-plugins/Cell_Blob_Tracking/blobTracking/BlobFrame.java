package blobTracking;


import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
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
import frameWork.TrackingFrame;

public class BlobFrame <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends TrackingFrame<Blob, IT>{
	
	private final MovieFrame<IT> movieFrame;
	private double backProb=0.1;
	private IterableInterval<IT> iterableFrame;
	public BlobFrame(int frameNum, MovieFrame<IT> mv){
		super(frameNum);
		movieFrame=mv;
	}

	@Override
	public synchronized void optimizeFrame(boolean cheap) {
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();	
		for(Blob b:trackables)
			b.expectedValues= imgFactory.create(movieFrame.getFrameView(), new FloatType());
		
				
			for(int i=0;i<100;i++){	
					double ti= doEStep();
					double change;
					
					change=this.doMstep(ti);
					System.out.println("change:" +change);			
					if(change<0.1) break;
			}
			
		
				
	//	for(Blob b:trackables)
	//		b.expectedValues= null;
		

	}
	
	private double doEStep(){
		double totalInten=0;
		long[] mins=  {movieFrame.getFrameView().min(0),movieFrame.getFrameView().min(1)};		
		long[] maxs=  {movieFrame.getFrameView().max(0),movieFrame.getFrameView().max(1)};
		
		
		for(Blob b:trackables){
			
	
			b.inten=0;
			System.out.println(" mins[0]:"+ mins[0]);
			mins[0]=Math.max(mins[0],(long) (b.xPos-b.sigma*3));
			mins[1]=Math.max(mins[1],(long) (b.yPos-b.sigma*3));
			maxs[0]=Math.min(maxs[0],(long) (b.xPos+b.sigma*3));
			maxs[1]=Math.min(maxs[1],(long) (b.yPos+b.sigma*3));
			System.out.println(" mins[0] after:"+ mins[0]);
		}
		
		 iterableFrame=new IterableRandomAccessibleInterval<IT>(Views.interval(movieFrame.getFrameView(),mins,maxs ));

		for(Blob b:trackables)	
			b.calcDenominator(iterableFrame);
		
	//	IterableInterval<IT> iterableFrame= new IterableRandomAccessibleInterval<IT>(movieFrame.getFrameView());
		Cursor<IT> cursor =iterableFrame.cursor();
		
		
		double pX=0;
		
		
		while ( cursor.hasNext() )	{
	    	cursor.fwd();
	    	pX=this.backProb/ImglibTools.getNumOfPixels(iterableFrame); // init with probability for background

	    	int x=cursor.getIntPosition(0);
	    	int y=cursor.getIntPosition(1);
	    //	int z=cursor.getIntPosition(2);
	    	int z=0;
	    	float value= cursor.get().getRealFloat();
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
/*	
	private double doMStepCheap(double totalInten){
	
		
		
		double change=0;
		double totalBlobsInten=0;
		for(Blob b:trackables){   
	//		System.out.println(b.localLogLikelihood());
	//		System.out.println(b.toString());
			
			double newX=0;
			double newY=0;
			double newSig=0;
			double newZ=0;
			double inten=0;
			long[] mins=  {(long)Math.max(b.expectedValues.min(0), b.xPos-b.sigma*3 ),(long)
					Math.max(b.expectedValues.min(1), b.yPos-b.sigma*3 )};
			
			long[] maxs=  {(long)Math.min(b.expectedValues.max(0), b.xPos+b.sigma*3 ),(long)
					Math.min(b.expectedValues.max(1), b.yPos+b.sigma*3 )};
			
			mins[0]=this.movieFrame.getFrameView().min(0);
			mins[1]=this.movieFrame.getFrameView().min(1);
			
			maxs[0]=this.movieFrame.getFrameView().max(0);
			maxs[1]=this.movieFrame.getFrameView().max(1);
			
			b.expectedValuesRoi=new IterableRandomAccessibleInterval<FloatType>(Views.interval(b.expectedValues,mins,maxs ));
			
    		Cursor<FloatType> cursor= b.expectedValuesRoi.cursor(); 
	    	while ( cursor.hasNext() )	{    		
		    	cursor.fwd();
				int x=cursor.getIntPosition(0);
		    	int y=cursor.getIntPosition(1);
		    //	int z=cursor.getIntPosition(2);
		    	int z=0;
		    	
		    	double value=cursor.get().get();
		    	inten+=value;
		    	newX+=value*x;
		    	newY+=value*y;
		    	newZ+=value*z;	
		    	newSig+=value*((x-b.xPos)*(x-b.xPos) + (y-b.yPos)*(y-b.yPos));
		
		    	
		    	
	    	}
	    	
	    	newX=newX/inten;
	    	newY=newY/inten;
	    	newSig=Math.sqrt(newSig/(2*inten));
	    			
	//    	}
   		
	    	change+=Math.abs((newX-b.xPos)/b.sigma);
	    	change+=Math.abs((newY-b.yPos)/b.sigma);
	    	change+=Math.abs((newSig-b.sigma)/b.sigma)*10;
	    	change+=Math.abs(((inten/totalInten)-b.pK)/b.pK);
  	
	    	b.xPos=newX;
	    	b.yPos=newY;
	    	b.sigma=Math.min(2.0,newSig);
	//    	b.zPos=newZ/inten;
	    	b.pK=inten/totalInten;
	    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~pk: " +b.pK);
	    	totalBlobsInten+=inten;
	    
    	}
		
		this.backProb=1-(totalBlobsInten/totalInten);
		return change;
	}
	*/
	
	
	private double doMstep(double totalInten){
		
		
		
		
		double change=0;
		double totalBlobsInten=0;
		for(Blob b:trackables){   
	//		System.out.println(b.localLogLikelihood());
	//		System.out.println(b.toString());
			
			double newX=0;
			double newY=0;
			double newSig=0;
			double newZ=0;
			
			long[] mins=  {(long)Math.max(iterableFrame.min(0), b.xPos-b.sigma*3 ),(long)
					Math.max(iterableFrame.min(1), b.yPos-b.sigma*3 )};
			
			System.out.println(" mins[0] mstep:"+ mins[0]);
			
			long[] maxs=  {(long)Math.min(iterableFrame.max(0), b.xPos+b.sigma*3 ),(long)
					Math.min(iterableFrame.max(1), b.yPos+b.sigma*3 )};
			
			//mins[0]=b.expectedValues.min(0);
			//mins[1]=b.expectedValues.min(1);
			
			//maxs[0]=b.expectedValues.max(0);
			//maxs[1]=b.expectedValues.max(1);
			
			b.expectedValuesRoi=new IterableRandomAccessibleInterval<FloatType>(Views.interval(b.expectedValues,mins,maxs ));
		//	inten=totalInten*b.pK;
			
    		Cursor<FloatType> cursor= b.expectedValuesRoi.cursor(); 
	   /* 	while ( cursor.hasNext() )	{    		
		    	cursor.fwd();
				int x=cursor.getIntPosition(0);
		    	int y=cursor.getIntPosition(1);
		    //	int z=cursor.getIntPosition(2);
		    //	int z=0;
		    	
		    	double value=cursor.get().get();
		    	inten+=value;
		    	newX+=value*x;
		    	newY+=value*y;
		   // 	newZ+=value*z;	
		    	newSig+=value*((x-b.xPos)*(x-b.xPos) + (y-b.yPos)*(y-b.yPos));
		
		    	
		    	
	    	}
	   */
	    	
	   // 	newX=newX/inten;
	   // 	newY=newY/inten;
	   // 	newSig=Math.sqrt(newSig/(2*inten));
	    	
	
	    //	NonLinearConjugateGradientOptimizer optimizer= new NonLinearConjugateGradientOptimizer(ConjugateGradientFormula.FLETCHER_REEVES);
	    	
	    	// 	if(b.denominator>0.99) {
	    	b.counter=0;
    		PowellOptimizer optimizer = new PowellOptimizer(100,100);
    		optimizer.setConvergenceChecker(new SimpleScalarValueChecker() );
	  //  	SimplexOptimizer optimizer = new SimplexOptimizer();
	    		    	
    		double []startPoint={b.xPos,b.yPos,b.sigma*b.sigma};
    	//	double []startPoint={newX,newY,newSig};
    		
    			
    		
    	//	optimizer.setSimplex(new  NelderMeadSimplex(3));
    		double []output = optimizer.optimize(100, b, GoalType.MAXIMIZE, startPoint).getPoint();
    		
    		
    		newX=output[0];
    		newY=output[1];
    		newSig=Math.min(2,Math.sqrt(output[2]) );
    		
	//    	}
   		
	    	change=Math.abs((newX-b.xPos)/b.sigma);
	    	change=Math.max(Math.abs((newY-b.yPos)/b.sigma), change );
	    	change=Math.max(Math.abs((newSig-b.sigma)/b.sigma)*10, change);
	    	change=Math.max(Math.abs(((b.inten/totalInten)-b.pK)/b.pK), change);
	/*    	
	    	System.out.println("xdiff:"+ (newX-b.xPos));
	    	System.out.println("ydiff:"+ (newY-b.yPos));
	    	System.out.println("sdiff:"+ (newSig-b.sigma));
	    	
	    	System.out.println("xold:"+ (b.xPos)+ "new:"+ newX);
	    	System.out.println("yold:"+ (b.yPos)+ "new:"+ newY);
	    	System.out.println("sold:"+ (b.sigma)+ "new:"+ newSig);
	*/    	
	    	b.xPos=newX;
	    	b.yPos=newY;
	    	b.sigma=Math.min(2.0,newSig);
	//    	b.zPos=newZ/inten;
	    	b.pK=b.inten/totalInten;
	    	totalBlobsInten+=b.inten;
	    	
	    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~pk: " +b.pK);
	    	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~totalInten: " +totalInten);
	     	System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~inten: " +b.inten);
    	}
		
		this.backProb=1-(totalBlobsInten/totalInten);
		return change;
	}
	
	
@Override
public Blob copy(Blob toCopy){
	Blob result=new Blob(toCopy.sequenceId, toCopy.frameId, toCopy.xPos, toCopy.yPos, toCopy.zPos, toCopy.sigma, toCopy.channel);
	return result;
}

}

