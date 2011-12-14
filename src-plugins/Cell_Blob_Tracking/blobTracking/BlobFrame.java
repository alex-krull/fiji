package blobTracking;


import java.util.ArrayList;

import tools.ImglibTools;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;
import frameWork.Frame;

public class BlobFrame <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends Frame<Blob, IT>{
	private double backProb=0.9;
	public BlobFrame(int frameNum, RandomAccessibleInterval<IT> view){
		super(frameNum, view);
	}

	@Override
	public void optimizeFrame() {
		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();	
		for(Blob b:trackables)
			b.expectedValues= imgFactory.create(frameView, new FloatType());
		
			for(int i=0;i<100;i++){	
				double ti= doEStep();
				this.doMstep(ti);
			}
				
		for(Blob b:trackables)
			b.expectedValues= null;
		

	}
	
	private double doEStep(){
		double totalInten=0;
		for(Blob b:trackables)
			b.calcDenominator(frameView);
		
		IterableInterval<IT> iterableFrame= new IterableRandomAccessibleInterval<IT>(frameView);
		Cursor<IT> cursor =iterableFrame.cursor();
		
		
		double pX=0;
		while ( cursor.hasNext() )	{
	    	cursor.fwd();
	    	pX=this.backProb/(double)ImglibTools.getNumOfPixels(frameView); // init with probability for background

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
	    		ra.get().set((float)( value*b.pXandK(x, y, z)/pX ) );
	    	
	    		
	    	}
	    	
	    		
		}
		return totalInten;
	}
	
	private void doMstep(double totalInten){
		
		double totalBlobsInten=0;
		for(Blob b:trackables){   
			double newX=0;
			double newY=0;
			double newZ=0;
			double inten=0;
			
    		Cursor<FloatType> cursor= b.expectedValues.cursor();
    
	    	
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
		    //	System.out.println("x:"+x+ " value:"+value);
		    	
		    	
	    	}
	    	
	    	b.xPos=newX/inten;
	    	b.yPos=newY/inten;
	//    	b.zPos=newZ/inten;
	    	b.pK=inten/totalInten;
	    	totalBlobsInten+=inten;
	    	System.out.println(b.toString());
    	}
		this.backProb=1-(totalBlobsInten/totalInten);
		
	}

	
	@Override
	public Frame<Blob,IT> createFrame(int frameNum, RandomAccessibleInterval<IT> view) {		
		return new BlobFrame<IT>(frameNum, view);
	}

}
