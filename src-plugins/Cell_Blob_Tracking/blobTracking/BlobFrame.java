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

import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.direct.*;

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
		
			for(int i=0;i<10;i++){	
				double ti= doEStep();
			
				
				double change=this.doMstep(ti);
				System.out.println("change:" +change);			
		//		if(change<0.001) break;
			}
				
	//	for(Blob b:trackables)
	//		b.expectedValues= null;
		

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
		    	newSig+=value*((x-b.xPos)*(x-b.xPos) + (y-b.yPos)*(y-b.yPos));
		
		    	
		    	
	    	}
	    	newX=newX/inten;
	    	newY=newY/inten;
	    	newSig=Math.sqrt(newSig/(2*inten));
	
    		
    	//	PowellOptimizer optimizer = new PowellOptimizer(100,100);
	    	SimplexOptimizer optimizer = new SimplexOptimizer();
    		double []startPoint={b.xPos,b.yPos,b.sigma};
    		optimizer.setSimplex(new  NelderMeadSimplex(3));
    		double []output = optimizer.optimize(10000, b, GoalType.MAXIMIZE, startPoint).getPoint();
    		
    		newX=output[0];
    		newY=output[1];
    		newSig=output[2];
   		
	    	change+=Math.abs(newX-b.xPos);
	    	change+=Math.abs(newY-b.yPos);
	    	change+=Math.abs(newSig-b.sigma);
	    	
	    	b.xPos=newX;
	    	b.yPos=newY;
	    	b.sigma=newSig;
	//    	b.zPos=newZ/inten;
	    	b.pK=inten/totalInten;
	    	totalBlobsInten+=inten;
	    
    	}
		this.backProb=1-(totalBlobsInten/totalInten);
		return change;
	}

	
	@Override
	public Frame<Blob,IT> createFrame(int frameNum, RandomAccessibleInterval<IT> view) {		
		return new BlobFrame<IT>(frameNum, view);
	}

}
