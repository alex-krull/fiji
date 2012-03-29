package blobTracking;



import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.MovieFrame;
import frameWork.TrackingFrame;

public class BlobFrame <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends TrackingFrame<Blob, IT>{
	

	public BlobFrame(int frameNum, MovieFrame<IT> mv){
		super(frameNum, new BlobPolicy<IT>());
		movieFrame=mv;
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
	
	
	
	
	
	
	


}

