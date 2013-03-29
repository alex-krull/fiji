/*******************************************************************************
 * This software implements the tracking method descibed in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2013 Alexandar Krull
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
package tools;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.algorithm.gauss.Gauss;
import net.imglib2.display.RealFloatConverter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;

import org.apache.commons.math.special.Erf;

public class ImglibTools {
	
	
	public static final int MAXPROJECTION = 0;
	public static final int SUMPROJECTION = 1;
	public static final double sq=Math.sqrt(2);
	
	public static <T extends  NumericType<T> & RealType<T> & NativeType<T>  > Img<T> scaleByFactor(RandomAccessibleInterval<T> img, int d, double factor){
		ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	       long[] dims = new long[img.numDimensions()];
	  		for(int i=0;i<img.numDimensions();i++)
	  			dims[i]=img.dimension(i);
	  		
	  		
	   	        
	       dims[d]=(long) Math.max(1,(dims[d]*factor) );
	       Img <T> result= imgFactory.create(dims, img.randomAccess().get().copy());      
	       resize(img,result);
	       return result;
	}
	
	public static <T extends  NumericType<T> & RealType<T> & NativeType<T>  > Img<T> resizeTo(RandomAccessibleInterval<T> source, long x, long y){
		ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
		long[] dims = new long[2];
		dims[0]=x;
		dims[1]=y;
		Img <T> result= imgFactory.create(dims, source.randomAccess().get().copy());
		resize(source,result);
	    return result;
	}


	public static <T extends  NumericType<T> & RealType<T> & NativeType<T>  > void resize(RandomAccessibleInterval<T> source, IterableInterval<T> dst){
		int dimensions=dst.numDimensions();
		if(source.numDimensions()!=dimensions) return;
		
		Cursor< T > d = dst.localizingCursor();
		RandomAccess< T > s = source.randomAccess();
		double[] factors= new double[dimensions];
		long[] offsets= new long[dimensions];
		for(int dim=0;dim<dimensions;dim++){
			
			factors[dim]=(double)(source.max(dim)+1-source.min(dim)) / (double)(dst.max(dim)+1-dst.min(dim));
			offsets[dim]=source.min(dim)-dst.min(dim);
	}
		
		while ( d.hasNext() )
		{
			d.fwd();
			for(int dim=0;dim<dimensions;dim++){
				int position= (int) ((d.getIntPosition(dim))*factors[dim]);
				
				s.setPosition(position,dim );
			}
			
			d.get().set( s.get() );
		}	
		
	}
	
static class ProjectionJob <T extends RealType<T> & NativeType<T> & NumericType<T> >
implements Callable<Integer>{
	
int d;
RandomAccessibleInterval<T> source;
IterableInterval<T> destination;
ProjectionJob(RandomAccessibleInterval<T> img, RandomAccessibleInterval<T> dst,int dim) {
		d=dim;
		source=img;
		destination=new IterableRandomAccessibleInterval<T>(dst);
	}
	
	@Override
	public Integer call() throws Exception {
		projectionWithDst(source, destination,d);
		return 0;
	}
	
}
public static <T extends Type<T>> RandomAccessibleInterval<T> split( RandomAccessibleInterval<T> source, int partIndex, int numOfParts, int d){
	long[] mins= new long[source.numDimensions()];
	long[] maxs= new long[source.numDimensions()];
	for(int i=0;i<source.numDimensions();i++){
		mins[i]=source.min(i);
		maxs[i]=source.max(i);
	}
	
	long chunckSize=(1+source.max(d)-source.min(d))/numOfParts;
	long chunckPosition=chunckSize*partIndex;
	
	mins[d]=chunckPosition;	
	if(numOfParts<partIndex+1) maxs[d]=chunckPosition+chunckSize-1;
	else maxs[d]=source.max(d);
	
//	IterableInterval<T> v=Views.interval(source, mins, maxs);
//	return source;
	return Views.interval(source, mins, maxs);

}

public static <T extends RealType<T> & NativeType<T> & NumericType<T> > 
RandomAccessibleInterval<T>
projection( RandomAccessibleInterval<T> source, int d, int noj){
	int numOfJobs=4;
	int splitAlongDim=source.numDimensions()-1;
	if(d==source.numDimensions()-1)splitAlongDim=source.numDimensions()-2;
	
	
	int cores = Runtime.getRuntime().availableProcessors();
	ExecutorService pool= Executors.newFixedThreadPool(2);
	
	
	IntervalView<T> imgv = Views.hyperSlice(source, d, 0);
    ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	Img <T> img= imgFactory.create(imgv, imgv.randomAccess().get().copy());  
	for(int i=0;i<numOfJobs;i++){
		 RandomAccessibleInterval<T> sView=split(source, i, numOfJobs, splitAlongDim);
		 RandomAccessibleInterval<T> dView;
		 if(d==source.numDimensions()-1) dView=split(img, i, numOfJobs, splitAlongDim);
		 else dView=split(img, i, numOfJobs, splitAlongDim-1);
		 ProjectionJob<T> job=new ProjectionJob<T>(sView, dView, d);
//		 try{job.call();}
	//	 catch(Exception e){};
		 
		 pool.submit(job);
	}
	pool.shutdown();
	try{pool.awaitTermination(100, TimeUnit.HOURS);}catch(Exception e){};
	return img;
}
/*
public static <T extends RealType<T> & NativeType<T> & NumericType<T> > RandomAccessibleInterval<T>
fillUpDimensions( RandomAccessibleInterval<T> source){
	ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	Img <T> img= imgFactory.create()
			
}*/

public static <T extends RealType<T> & NativeType<T> & NumericType<T> > RandomAccessibleInterval<T>
projection( RandomAccessibleInterval<T> source, int d){
	
	IntervalView<T> imgv = Views.hyperSlice(source, d, 0);
    ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	Img <T> img= imgFactory.create(imgv, imgv.randomAccess().get().copy());  
	projectionWithDst(source,img,d);
	return img;
}

public static <T extends RealType<T> & NativeType<T> & NumericType<T> >
void projectionWithDst( RandomAccessibleInterval<T> source,IterableInterval<T> img , int d){


	IntervalView<T> imgv = Views.hyperSlice(source, d, 0);


    Cursor<T> cursor = img.cursor();
    while ( cursor.hasNext() )	{
    	cursor.fwd();
    	int dimensions=imgv.numDimensions();
    	
    	RandomAccess<T> ra= source.randomAccess();
    	for(int i=0;i<dimensions;i++){       	
    		if(i>=d)
    			ra.setPosition( cursor.getIntPosition(i),i+1);
    		else if(i<d) ra.setPosition( cursor.getIntPosition(i),i);
    	}
    	int minD=(int) source.min(d);
    	int maxD=(int) source.max(d);
    	ra.setPosition(minD,d );
    	
    	
    	T akku= ra.get().copy();
 
    	while(minD<=ra.getIntPosition(d)
   			&& (maxD>ra.getIntPosition(d)) ){		        		         		
    		if(akku.compareTo(ra.get())<0 ){
    			akku.set(ra.get());
    		}

    		ra.fwd(d);
    		
    	}
    	cursor.get().set(akku);
    	
	}

}

public static long getNumOfPixels(Interval img){
	long akku=1;
	for(int i=0;i<img.numDimensions();i++)
		akku*=(img.max(i)-img.min(i)+1);
	//System.out.println("                            number of pixels:" + akku);
	return akku;
}

	/*
	public static <T extends RealType<T> & NativeType<T> & NumericType<T> > RandomAccessibleInterval<T>
	projection( RandomAccessibleInterval<T> source, int d){
		int cores = Runtime.getRuntime().availableProcessors();
		ExecutorService pool= Executors.newFixedThreadPool(cores);
		
		System.out.println("start.");
		IntervalView<T> imgv = Views.hyperSlice(source, d, 0);
	    ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	    
	    Img <T> img= imgFactory.create(imgv, imgv.randomAccess().get().copy());      
	    System.out.println("starting...");
	    
	    Cursor<T> cursor = img.cursor();
	    while ( cursor.hasNext() )	{
	    	cursor.fwd();
	    	int dimensions=imgv.numDimensions();
	    	
	    	RandomAccess<T> ra= source.randomAccess();
	    	for(int i=0;i<dimensions;i++){       	
	    		if(i>=d)
	    			ra.setPosition( cursor.getIntPosition(i),i+1);
	    		else if(i<d) ra.setPosition( cursor.getIntPosition(i),i);
	    	}
	    	int minD=(int) source.min(d);
	    	int maxD=(int) source.max(d);
	    	ra.setPosition(minD,d );
	    	
	    	
	    	T akku= ra.get().copy();
	 
	    	while(minD<=ra.getIntPosition(d)
	   			&& (maxD>ra.getIntPosition(d)) ){		        		         		
	    		if(akku.compareTo(ra.get())<0 ){
	    			akku.set(ra.get());
	    		}

	    		ra.fwd(d);
	    		
	    	}
	    	cursor.get().set(akku);
	    	
		}
	    System.out.println("stop");
	    return img;
	}
*/

public static double gaussPixelIntegral(int x , int y, double cx, double cy, double sig){
  //  double sq=Math.sqrt(2);
	double xd=x;
	double yd=y;
    double calcX=0.5*Erf.erf( (xd-cx+0.5)/(sq*sig) )-0.5*Erf.erf( (xd-cx-0.5)/(sq*sig) );
    double calcY=0.5*Erf.erf( (yd-cy+0.5)/(sq*sig) )-0.5*Erf.erf( (yd-cy-0.5)/(sq*sig) );

    return calcX*calcY;
}



public static double gaussIntegral(double x1 , double y1, double x2, double y2,  double cx, double cy, double sig){
//    double sq=Math.sqrt(2.0);

    double calcX=0.5*Erf.erf( (x2-cx)/(sq*sig) )-0.5*Erf.erf( (x1-cx)/(sq*sig) );
    double calcY=0.5*Erf.erf( (y2-cy)/(sq*sig) )-0.5*Erf.erf( (y1-cy)/(sq*sig) );


    return calcX*calcY;
}

public static double gaussIntegral2dIn3d(double x1 , double y1,  double x2, double y2,  double z ,double cx, double cy, double cz, double sig, double sigZ){
	    return ImglibTools.gaussIntegral(x1, y1, x2, y2, cx, cy, sig)*Math.exp(-(cz-z)*(cz-z)/(sigZ*sigZ*2));
}

public static double gaussPixelIntegral2dIn3d(int x , int y, double z, double cx, double cy, double cz,double sig, double sigZ){
	return ImglibTools.gaussPixelIntegral(x, y, cx, cy, sig)*Math.exp(-(cz-z)*(cz-z)/(sigZ*sigZ*2));
}

public static <T extends RealType<T> & NativeType<T> & NumericType<T>> RandomAccessibleInterval<T> 
scaleAndShift(RandomAccessibleInterval<T> src, int transX, int transY, double scaleX, double scaleY, int xSize, int ySize){
	
	long minX=(long)((transX)/scaleX) -4;
	long minY=(long)((transY)/scaleY) -4;
	
	
	minX=Math.max(minX, 0);
	minY=Math.max(minY, 0);
	
	long maxX=minX+8+(long)((xSize)/scaleX) ;
	long maxY=minY+8+(long)((ySize)/scaleY) ;
	
	if(maxX>=src.max(0)){
		maxX=src.max(0);
		minX=Math.max( (long)(maxX-xSize/(scaleX)-8) , 0);
	}
	
	if(maxY>=src.max(1)){
		maxY=src.max(1);
		minY=Math.max( (long)(maxY-ySize/(scaleY)-8) , 0);
	}
	

	
	long[] minsP= {minX, minY};
	long[] maxsP= {maxX,maxY};
	
			
	RandomAccessibleInterval<T> temp=  Views.zeroMin(Views.interval(src, minsP, maxsP));
	
	temp=ImglibTools.scaleByFactor(temp, 0, scaleX);
	temp=ImglibTools.scaleByFactor(temp, 1, scaleY);
	
	minsP[0]=(long)(-scaleX*minsP[0]);
	minsP[1]=(long)(-scaleY*minsP[1]);
	
				
	
	minX=transX+minsP[0];
	minY=transY+minsP[1];
	
	minX=Math.max(minX, 0);
	minY=Math.max(minY, 0);
	
	maxX=minX+xSize-1 ;
	maxY=minY+ySize-1 ;

	if(maxX>temp.max(0)){
	
		maxX=temp.max(0);
		minX=Math.max( maxX-xSize+1 , 0);
	}
	
	if(maxY>=temp.max(1)){
		maxY=temp.max(1);
		minY=Math.max( maxY-ySize+1 , 0);
	}
	
	
	
	long[] mins= {minX,minY};
	
	
	long[] maxs= {maxX,maxY};
		
						
	
		
	temp= Views.zeroMin( Views.interval(temp, mins, maxs) );
	
	return temp;
}

public static <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>> List<Img <IT>> generatePyramid(Img<IT> src, int steps, double std, double factor){
	System.out.println("pyramid!");
	List <Img <IT>> result = new LinkedList<Img <IT>>();
//	result.add(src);
	double sigma[]= new double[src.numDimensions()];
	for(int i=0;i<src.numDimensions();i++){
		if(i<2)sigma[i]=std;
		else sigma[i]=0;
	}
	
	Img <IT> lastImage= src;
	Img <IT> currentImage= null;
	for(int i= 0;i<steps;i++){
		currentImage=Gauss.inNumericType(sigma, lastImage);
		for(int d=0;d<currentImage.numDimensions();d++)
			if(d<2) currentImage= scaleByFactor(currentImage, d, factor);
			
		result.add(0,currentImage);
		
		lastImage=currentImage;
	//	ImageJFunctions.show(Gauss.inNumericType(sigma, currentImage));
		
	}
	
	//IJ.error("returning");
	
	
	
	return result;
}

public static  Img <FloatType> differenceOfGaussians(Img<FloatType> src, double stdA, double stdB){
	ImgFactory <FloatType> factory= new ArrayImgFactory<FloatType>();
	Img <FloatType> result = factory.create(src, new FloatType());
	
	
	double sigmaA[]= new double[src.numDimensions()];
	for(int i=0;i<src.numDimensions();i++){
		if(i<2)sigmaA[i]=stdA;
		else sigmaA[i]=0;
	}
	
	
	Img <FloatType> imgA=Gauss.inNumericType(sigmaA, src);
	
	double sigmaB[]= new double[src.numDimensions()];
	for(int i=0;i<src.numDimensions();i++){
		if(i<2)sigmaB[i]=stdB;
		else sigmaB[i]=0;
	}
	Img <FloatType> imgB=Gauss.inNumericType(sigmaB, src);

	
	Cursor<FloatType> it= imgA.cursor();
	RandomAccess<FloatType> ra= imgB.randomAccess();
	RandomAccess<FloatType> raResult= result.randomAccess();
	while(it.hasNext()){
		it.fwd();
		raResult.setPosition(it);
		ra.setPosition(it);
		double a=it.get().getPowerDouble();
		double b=ra.get().getPowerDouble();
		double res= Math.max(0, b-a);
	//	System.out.println("a:"+a+ "  b:"+b + "  res:"+res);
		raResult.get().set(new FloatType((float)res));
	}
	
	return result;
}

public static <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>> void convert(RandomAccessibleInterval<IT> in, Img<FloatType> out ){
	RealFloatConverter<IT> converter= new RealFloatConverter<IT>();
	Cursor<FloatType> it= out.cursor();
	RandomAccess<IT> ra= in.randomAccess();
	while(it.hasNext()){
		it.fwd();
		ra.setPosition(it);
		converter.convert(ra.get(), it.get());
	}
	
}

public static <IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>> int findBrightestPixelInColumn(RandomAccessibleInterval<IT> in, int x, int y){
	RandomAccess<IT> ra= in.randomAccess();
	double bestValue=0;
	int winner=0;
	for(int i=(int)in.min(2);i<=in.max(2);i++){
		int[] pos= {x, y, i};
		ra.setPosition(pos);
		if(ra.get().getRealDouble()>=bestValue){
			bestValue=ra.get().getRealDouble();
			winner=i;
		}
	}
	
	return winner;
	
}

}

