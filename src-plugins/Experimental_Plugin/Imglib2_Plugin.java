
import java.awt.Color;
import java.awt.event.*;
import java.util.Properties;


import net.imglib2.type.NativeType;
import net.imglib2.type.Type;

import ij.measure.Calibration;
import ij.plugin.ContrastEnhancer;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.plugin.frame.PlugInFrame;



import ij.gui.*;
import ij.process.*;


import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.ImgFactory;

import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;






public class Imglib2_Plugin < T extends NativeType<T> & RealType<T>> implements PlugIn, MouseListener, ImageListener{
	private ImagePlus impZ;
	private ImagePlus impX;
	private ImagePlus impY;
	
	private ImagePlus impXT;
	private ImagePlus impYT;
	
	ImagePlus mainImage;
	ImageCanvas canvasZ;
	ImageCanvas canvasX;
	ImageCanvas canvasY;
	
	Img original;
	int SliceNumber=1;
	boolean buisy=false;
	double xyToZ=3.5;

	

    /** Ask for parameters and then execute.*/
    public void run(String arg) {
    	
   // 	ImagePlusAdapter.wrap(IJ.getImage());
    	
        // 1 - Obtain the currently active image:
        ImagePlus imp = IJ.getImage();
        mainImage=imp;
      
        if (null == imp) return;
 
        exec(imp); 
    }
    
    public void exec(ImagePlus imp) {
        // 0 - Check validity of parameters
      

    	
       
       Img<T> img = ImagePlusAdapter.wrap(imp);
       
       original=img;
       if (img==null) return;

       
    	   
       
       
       
       
       
   
       RandomAccessibleInterval<T> imgz = projection(img,2);
       RandomAccessibleInterval<T> imgx = Views.zeroMin(Views.rotate(projection(img,0), 0,1) );
       RandomAccessibleInterval<T> imgxt= Views.zeroMin(Views.rotate(projection(imgx,0),0,1) );
       RandomAccessibleInterval<T> imgy = projection(img,1);
       RandomAccessibleInterval<T> imgyt= projection(imgy,1);
       IntervalView<T> imgmain=Views.hyperSlice(img, 3, 0);
       
       IntervalView<T> rot = Views.rotate(imgz, 2, 0);
       rot=Views.zeroMin(rot);
       
       
    		   
       ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
       
       long[] dims = new long[imgx.numDimensions()];
   		for(int i=0;i<imgx.numDimensions();i++)
   			dims[i]=imgx.dimension(i);
    	        
       dims[0]=(long)((double)dims[0]*this.xyToZ);
       Img <T> imgxs= imgFactory.create(dims, imgx.randomAccess().get().copy());      
       resize(imgx,imgxs);  
       
       
       
       
       for(int i=0;i<imgy.numDimensions();i++)
  			dims[i]=imgy.dimension(i);
       
       dims[1]=(long)((double)dims[1]*this.xyToZ);
       Img <T> imgys= imgFactory.create(dims, imgy.randomAccess().get().copy());      
       resize(imgy,imgys);  
       
       imgx=imgxs;
       imgy=imgys;
      
       
       
       impZ=ImageJFunctions.show(imgz,"z");
       impX=ImageJFunctions.show(imgx,"x");
       impY=ImageJFunctions.show(imgy,"y");
       impXT=ImageJFunctions.show(imgxt,"xt");
       impYT=ImageJFunctions.show(imgyt,"yt");
       //mainImage=ImageJFunctions.showUnsignedShort(img,"stack");     
    //   ImageJFunctions.showUnsignedShort(projection(rot,1),"x and z"); 
       
    
       canvasX=impX.getCanvas();
       canvasY=impY.getCanvas();
       canvasZ=impZ.getCanvas();
           
     
      
       impX.addImageListener(this);
       impY.addImageListener(this);
       impZ.addImageListener(this);
       
     
        return;
    }
    
    public Overlay cellOverlay(int segments, double posistionX, double posistionY, double length, double width){
    	Overlay ov=new Overlay();    	
    	double x=0;
    	double y=0;
    	double lx=0;
    	double ly=0;
    	float[] xs= new float[segments*2+2];
    	float[] ys= new float[segments*2+2];
    	
    	for(int i=0; i<=segments;i++){
    		double bl=( (double)i / (double) segments)*Math.PI;
    		lx=x;
    		ly=y;
    		x=Math.cos(bl+Math.PI)*width + posistionX;
    		y=Math.sin(bl+Math.PI)*width + posistionY - length;
    		Line l=new Line(lx,ly,x,y);
    		l.setStrokeColor(Color.blue);
    //		if(lx!=0 && ly!=0) ov.add(l);
    		xs[i]=(float)x; 
    		ys[i]=(float)y; 
    		
    		
    	}
    	
    	for(int i=0; i<=segments;i++){
    		double bl=( (double)i / (double) segments)*Math.PI;
    		lx=x;
    		ly=y;
    		x=Math.cos(bl)*width + posistionX;
    		y=Math.sin(bl)*width + posistionY+ length;
    	//	if(lx!=0 && ly!=0) ov.add(new Line(lx,ly,x,y)); 
    		xs[i+segments+1]=(float)x; 
    		ys[i+segments+1]=(float)y; 
    	}
    	lx=x;
		ly=y;
		y=y-length*2;
		
	//	ov.add(new Line(lx,ly,x,y));
		PolygonRoi r=new PolygonRoi(xs,ys,segments*2+2,Roi.POLYGON);
	//	r.fitSpline();
		ov.add(r);
    	    	
    	return ov;
    }
    
   
    public void makeHist( Img<UnsignedShortType> source )
	{
    	
    /*	
    	System.out.println("num of Dimensions: "+ source.numDimensions());
    	
        Cursor<UnsignedShortType> cursor = source.cursor();
        
				
			
			 	float[] x = new float[65536];
			 	float[] y = new float[65536];
			 	for(int i=0;i<65536;i++){
			 		x[i]=i;
			 		y[i]=0;
			 	}
		        
		        
		        int maxX=0;
		        int maxY=0;	
		        int minX=65536;
		        int minY=100000000;	
		        int pixels=0;
		        while ( cursor.hasNext() )
				{
		        	pixels++;
					cursor.fwd();
					int v=cursor.get().get();
					y[v]++;
					maxX=Math.max(v, maxX);
					maxY=Math.max((int)y[v], maxY);
					
					minX=Math.min(v, minX);
					minY=Math.min((int)y[v], minY);
				
				}
		        
		        
		       
		     	        
		        		        
		        int akku=0;
		        minX=-1;
		        maxX=-1;
		        for(int i=0;i<65536;i++){
			 		akku+=y[i];
			 		if((double)akku/(double)pixels >= 0.00001 && minX<0) minX=i;
			 		if((double)akku/(double)pixels >= 0.99999 && maxX<0){
			 			maxX=i;
			 			break;
			 		}
			 	}
		        
		        GenericDialog gd = new GenericDialog("Histogramm");
		        gd.addNumericField("min intensity:", minX, 0);
		        gd.addNumericField("max intensity:", maxX, 0);
		        gd.showDialog();
		        if (gd.wasCanceled()) return;
		 
		        // 3 - Retrieve parameters from the dialog
		        minX = (int)gd.getNextNumber();
		        maxX = (int)gd.getNextNumber();
		        
		        

		        PlotWindow.noGridLines = false; // draw grid lines
		        Plot plot = new Plot("Example Plot","X Axis","Y Axis",x,y);
		        plot.setLimits(minX, maxX, minY, maxY);
		        plot.setLineWidth(2);
		        plot.show();
		        
		        
		*/        
		     //   ImageJFunctions.showUnsignedShort(projection(source,0)); 
		        
//		        RandomAccessibleInterval<UnsignedShortType> imgv= Views.hyperSlice(ImagePlusAdapter.wrapShort(IJ.getImage()), 0,0);
//		        ImageJFunctions.showUnsignedShort(imgv);
    	
		  
    			
		 
		        
		        
		
	}
    public static <T extends RealType<T> & NativeType<T>> void resize(RandomAccessibleInterval<T> source, IterableInterval<T> dst){
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
    
    public static <T extends RealType<T> & NativeType<T>> RandomAccessibleInterval<T> projection( RandomAccessibleInterval<T> source, int d){
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
    
   
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX();
		int y = e.getY();
		int offscreenX = canvasX.offScreenX(x);
		int offscreenY = canvasX.offScreenY(y);
		System.out.println("mousePressed off screen: "+offscreenX+","+offscreenY);
		System.out.println("mousePressed: "+x+","+y);
	
	
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
	
			
	}

	@Override
	public void mousePressed(MouseEvent e) {
		System.out.println("pressed ! !");
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		System.out.println("released ! !");
	}

	@Override
	public void imageClosed(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void imageUpdated(ImagePlus ip) {
		if(buisy)return;
		buisy=true;
		int newSliceNumber=SliceNumber;
		
		// TODO Auto-generated method stub
		if(SliceNumber !=impZ.getSlice()) newSliceNumber= impZ.getSlice();
		if(SliceNumber !=impY.getSlice()) newSliceNumber= impY.getSlice();
		if(SliceNumber !=impX.getSlice()) newSliceNumber= impX.getSlice();
		System.out.println("SN:" + SliceNumber+ " NSL:"+newSliceNumber);
		
		if(newSliceNumber!=SliceNumber)
		{
			int zSliceNumber=mainImage.getCurrentSlice() % 13;
			Calibration cal=mainImage.getCalibration();
			Properties prop=mainImage.getProperties();
			
			SliceNumber=newSliceNumber;
			this.impX.setSlice(SliceNumber);
			this.impY.setSlice(SliceNumber);
			this.impZ.setSlice(SliceNumber);
			this.mainImage.setPosition(1, zSliceNumber, SliceNumber);
	//		this.mainImage.setSlice(zSliceNumber);
		//	ImagePlus temp=ImageJFunctions.wrapUnsignedShort(Views.hyperSlice(original, 3, SliceNumber-1), "") ;			
		//	mainImage.getWindow().setImage( temp);
		//	mainImage=temp;
			
			this.impZ.updateAndDraw();			
			this.impY.updateAndDraw();
			this.impX.updateAndDraw();
			System.out.println("slice:"+zSliceNumber);
	//		this.mainImage.setCalibration(cal);
			
			System.out.println("new slice:"+mainImage.getCurrentSlice());
			
	//		ContrastEnhancer ce= new ContrastEnhancer();
		//	ce.stretchHistogram(mainImage.getProcessor(), 0.5); 
			
			this.mainImage.updateAndDraw();
			
			Overlay ov= cellOverlay(20, 40, 40.0, 10.0,SliceNumber);
		       
		       
		       
		       impX.setOverlay(ov);
		       impY.setOverlay(ov);   
		       impZ.setOverlay(ov);
			
		}
		buisy=false;
		
	}
    
    
}
