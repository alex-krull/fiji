package tools;

import org.apache.commons.math.special.Erf;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

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
import net.imglib2.view.IntervalView;
import net.imglib2.view.IterableRandomAccessibleInterval;
import net.imglib2.view.Views;

public class ImglibTools {
	public static <T extends  NumericType<T> & RealType<T> & NativeType<T>  > Img<T> scaleByFactor(RandomAccessibleInterval<T> img, int d, double factor){
		ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	       long[] dims = new long[img.numDimensions()];
	  		for(int i=0;i<img.numDimensions();i++)
	  			dims[i]=img.dimension(i);
	  		
	  		
	   	        
	       dims[d]=(long) ((double)dims[d]*factor);
	       Img <T> result= imgFactory.create(dims, img.randomAccess().get().copy());      
	       resize(img,result);  
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
	//		System.out.println("f:"+factors[dim]+ "   s: "+ (source.max(dim)+1-source.min(dim))+ "  :d"+ (dst.max(dim)+1-dst.min(dim) ));
		}
		
		while ( d.hasNext() )
		{
			d.fwd();
			for(int dim=0;dim<dimensions;dim++){
				int position= (int) ((double)(d.getIntPosition(dim))*factors[dim]);
				
				s.setPosition((int) position,dim );
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
	
	//System.out.println("start.");
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
public static <T extends RealType<T> & NativeType<T> & NumericType<T> > RandomAccessibleInterval<T>
projection( RandomAccessibleInterval<T> source, int d){
	
	//System.out.println("start.");
	IntervalView<T> imgv = Views.hyperSlice(source, d, 0);
    ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	Img <T> img= imgFactory.create(imgv, imgv.randomAccess().get().copy());  
	projectionWithDst(source,img,d);
	return img;
}

public static <T extends RealType<T> & NativeType<T> & NumericType<T> >
void projectionWithDst( RandomAccessibleInterval<T> source,IterableInterval<T> img , int d){

//	System.out.println("start.");
	IntervalView<T> imgv = Views.hyperSlice(source, d, 0);

 //   System.out.println("starting...");
    
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
  //  System.out.println("stop");
    
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

static double gaussPixelIntegral(int x , int y, int z, double cx, double cy, double cz,double sig, double sigZ){
    double sq=Math.sqrt(2);
    double calcX=0.5*Erf.erf( ((double)x-cx+0.5)/(sq*sig) )-0.5*Erf.erf( ((double)x-cx-0.5)/(sq*sig) );
    double calcY=0.5*Erf.erf( ((double)y-cy+0.5)/(sq*sig) )-0.5*Erf.erf( ((double)y-cy-0.5)/(sq*sig) );
    double calcZ=0.5*Erf.erf( ((double)z-cz+0.5)/(sq*sigZ) )-0.5*Erf.erf( ((double)z-cz-0.5)/(sq*sigZ) );

    return calcX*calcY*calcZ;
}

}
