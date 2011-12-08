package tools;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
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
			System.out.println("f:"+factors[dim]+ "   s: "+ (source.max(dim)+1-source.min(dim))+ "  :d"+ (dst.max(dim)+1-dst.min(dim) ));
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
	

	public static <T extends RealType<T> & NativeType<T> & NumericType<T> > RandomAccessibleInterval<T> projection( RandomAccessibleInterval<T> source, int d){
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


}
