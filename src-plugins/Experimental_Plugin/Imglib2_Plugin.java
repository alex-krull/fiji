
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
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;






public class Imglib2_Plugin < T extends  NumericType<T> & NativeType<T> & RealType<T>  > implements PlugIn, MouseListener, ImageListener{
	
	
	private ImagePlus impZ;
	private ImagePlus impX;
	private ImagePlus impY;
	
	private ImagePlus impXT;
	private ImagePlus impYT;
	
	ImagePlus mainImage;
	ImageCanvas canvasZ;
	ImageCanvas canvasX;
	ImageCanvas canvasY;
	
	RandomAccessibleInterval<T> zProjections;
	RandomAccessibleInterval<T> xProjections;
	RandomAccessibleInterval<T> yProjections;
	RandomAccessibleInterval<T> xtProjections;
	RandomAccessibleInterval<T> ytProjections;
	RandomAccessibleInterval<T> image;
	int SliceNumber=1;
	int sliceNumberZ=1;
	boolean buisy=false;
	double xyToZ=3.5;

	

    /** Ask for parameters and then execute.*/
	public Img<T> scaleByFactor(RandomAccessibleInterval<T> img, int d, double factor){
		ImgFactory<T> imgFactory = new ArrayImgFactory<T>();
	       long[] dims = new long[img.numDimensions()];
	  		for(int i=0;i<img.numDimensions();i++)
	  			dims[i]=img.dimension(i);
	  		
	  		
	   	        
	       dims[d]=(long) ((double)dims[d]*factor);
	       Img <T> result= imgFactory.create(dims, img.randomAccess().get().copy());      
	       resize(img,result);  
	       return result;
	}
	
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
      

    	
    	
       
       image = ImagePlusAdapter.wrap(imp);
       
       
       
       if(mainImage.getNChannels()>1){
    	   image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,image.numDimensions()-1),2) );
       if(image.numDimensions()==5)  	   
    	   image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,3),2 ));
       
       }
  	     
       
       System.out.println("channels:" +mainImage.getNChannels()+ "  frames:"+mainImage.getNFrames()+ "  slices:"+mainImage.getNSlices());
 		System.out.println("dimensions:" + image.numDimensions());
       
         
       if(mainImage.getNSlices()>1){
    	   zProjections=projection(image,2);
    	   xProjections=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( projection(image,0),0,1) ),0  ) ); 
           yProjections=projection(image,1);
           xProjections=scaleByFactor(xProjections,0,this.xyToZ);
           yProjections=scaleByFactor(yProjections,1,this.xyToZ);
           
           xtProjections=Views.zeroMin( Views.invertAxis(  Views.rotate( projection(xProjections,0) ,0,1),0 ) )  ;
           ytProjections=projection(yProjections,1);
       }
       else{ 
    	   zProjections=null;
    	   xProjections=null; 
    	   yProjections=null;
    	   
    	   xtProjections=Views.zeroMin( Views.invertAxis(  Views.rotate( projection(image,0) ,0,1),0 ) )  ;
    	   ytProjections=projection(image,1);
       }
       
       
       
       
       
   
       this.upDateImages(0, 0,true);
       
       
       	
       //mainImage=ImageJFunctions.showUnsignedShort(img,"stack");     
    //   ImageJFunctions.showUnsignedShort(projection(rot,1),"x and z"); 
       
    
   //    canvasX=impX.getCanvas();
   //    canvasY=impY.getCanvas();
   //    canvasZ=impZ.getCanvas();
           
     
      
    //   impX.addImageListener(this);
    //   impY.addImageListener(this);
    //   impZ.addImageListener(this);
       mainImage.addImageListener(this);
       
    
       
	//   gui screen = new gui("Example 1");
    //   screen.setSize(500,100);
    //   screen.setVisible(true);
    //   screen.add(impZ.getCanvas());
       
     
     
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
		r.setStrokeColor(Color.yellow);
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
    
   
	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		int x = e.getX();
		int y = e.getY();
	//	int offscreenX = canvasX.offScreenX(x);
	//	int offscreenY = canvasX.offScreenY(y);
	//	System.out.println("mousePressed off screen: "+offscreenX+","+offscreenY);
	//	System.out.println("mousePressed: "+x+","+y);
	
	
		
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

	private synchronized void upDateImages(int frame, int channel, boolean init){
		
		
		
		
		RandomAccessibleInterval<T> imgx=zProjections;
		RandomAccessibleInterval<T> imgy=xProjections;
		RandomAccessibleInterval<T> imgz=yProjections;
	       RandomAccessibleInterval<T> imgxt= xtProjections;      
	       RandomAccessibleInterval<T> imgyt= ytProjections;
	       
	       if(mainImage.getNSlices()>1){
	       imgz=Views.hyperSlice(zProjections,2,frame);
	       imgx=Views.hyperSlice(xProjections,2,frame);
	       imgy=Views.hyperSlice(yProjections,2,frame);
	       }
	       
	       if(mainImage.getNChannels()>1){
	    	   if(mainImage.getNSlices()>1){
	    		   imgz=Views.hyperSlice( imgz,2,channel);
		    	   imgx=Views.hyperSlice( imgx,2,channel);
		    	   imgy=Views.hyperSlice( imgy,2,channel);   			
	   		   }	
	    	   
	    	   imgxt=Views.hyperSlice(imgxt,2,channel);
	    	   imgyt=Views.hyperSlice(imgyt,2,channel);
	       }
	    
	    	   
	       ContrastEnhancer ce= new ContrastEnhancer();
	       
	       
	    if(init){  
	       
	       int maxZx=500;      
	       int maxZy=500;  
	       
	       int mainX=mainImage.getWindow().getX();      
	       int mainY=mainImage.getWindow().getY();  
	       
	      
	       
	       if(mainImage.getNSlices()>1){	     
	    	   impZ=ImageJFunctions.show(imgz,"z"); impZ.getWindow().setLocation(maxZx, maxZy);    
	    	   impX=ImageJFunctions.show(imgx,"x"); impX.getWindow().setLocation(mainX -impX.getWindow().getWidth(), mainY);
	    	   impY=ImageJFunctions.show(imgy,"y"); impY.getWindow().setLocation(mainX, mainY -impY.getWindow().getHeight());
	    	   
	    	   ce.stretchHistogram(impZ.getProcessor(), 0.5); 	 
	    	   impZ.updateAndDraw();
	    	   ce.stretchHistogram(impX.getProcessor(), 0.5); 	
	    	   impX.updateAndDraw();
	    	   ce.stretchHistogram(impY.getProcessor(), 0.5); 	
	    	   impY.updateAndDraw();
	    	  
	    	   
	       }else{
	    	   impZ=null;
	    	   impX=null;
	    	   impY=null;
	       }
	       
	       impXT=ImageJFunctions.show(imgxt,"xt"); impXT.getWindow().setLocation(maxZx -impXT.getWindow().getWidth(), maxZy);
	       impYT=ImageJFunctions.show(imgyt,"yt"); impYT.getWindow().setLocation(maxZx, maxZy -impYT.getWindow().getHeight());
	       
	       ce.stretchHistogram(impXT.getProcessor(), 0.5);   
    	   impXT.updateAndDraw();
    	   ce.stretchHistogram(impYT.getProcessor(), 0.5); 
    	   impYT.updateAndDraw();
	       
	    }else{
	    				
			
	    	ImagePlus impl=null;
			if(mainImage.getNSlices()>1){
			impl=ImageJFunctions.wrap( imgz , " ");
	    	ce.stretchHistogram(impl.getProcessor(), 0.5); 			
			this.impZ.setProcessor(impl.getProcessor());
			impZ.updateAndDraw();
					
			impl=ImageJFunctions.wrap( imgx , " ");					
	    	ce.stretchHistogram(impl.getProcessor(), 0.5); 			
			this.impX.setProcessor(impl.getProcessor());
			impX.updateAndDraw();
			
			impl=ImageJFunctions.wrap( imgy , " ");					
	    	ce.stretchHistogram(impl.getProcessor(), 0.5); 			
			this.impY.setProcessor(impl.getProcessor());
			impY.updateAndDraw();	
			}
			
			impl=ImageJFunctions.wrap( imgxt , " ");					
	    	ce.stretchHistogram(impl.getProcessor(), 0.5); 			
			this.impXT.setProcessor(impl.getProcessor());
			impXT.updateAndDraw();
			
			impl=ImageJFunctions.wrap( imgyt , " ");					
	    	ce.stretchHistogram(impl.getProcessor(), 0.5); 			
			this.impYT.setProcessor(impl.getProcessor());
			impYT.updateAndDraw();	
			
	    }
	    
	    
	    
		
				
	}
	
	
	private synchronized void doStuff(){
		if(buisy)return;
		buisy=true;
		int newSliceNumber=SliceNumber;
		
		// TODO Auto-generated method stub
	//	if(SliceNumber !=impZ.getSlice()) newSliceNumber= impZ.getSlice();
	//	if(SliceNumber !=impY.getSlice()) newSliceNumber= impY.getSlice();
	//	if(SliceNumber !=impX.getSlice()) newSliceNumber= impX.getSlice();
		
		newSliceNumber= mainImage.getFrame()-1;
		int zSliceNumber=mainImage.getSlice()-1;
		int cNumber=mainImage.getChannel()-1;
		
		System.out.println("frame:"+ newSliceNumber+ "   slice:"+ zSliceNumber+ "  channel:"+cNumber );
		
		if(newSliceNumber!=SliceNumber|| zSliceNumber!=sliceNumberZ)
		{
			
		
			this.upDateImages(newSliceNumber, cNumber, false);
			
			System.out.println("new slice:"+mainImage.getCurrentSlice());
			
			
			
			this.mainImage.updateAndDraw();
			
	//		Overlay ov= cellOverlay(20, 40, 40.0, 10.0,SliceNumber);
		       
		       
		       
	//	       impX.setOverlay(ov);
	//	       impY.setOverlay(ov);   
	//	       impZ.setOverlay(ov);
		       
		   Overlay ovLineYT=new Overlay();
		   
		   ovLineYT.add(new Line(0,newSliceNumber-0.5,this.image.dimension(0) ,newSliceNumber-0.5));
		   ovLineYT.setStrokeColor(Color.yellow);
		   this.impYT.setOverlay(ovLineYT);
		   
		   Overlay ovLineXT=new Overlay();
		   ovLineXT.add(new Line(newSliceNumber-0.5,0, newSliceNumber-0.5, this.image.dimension(1) ));
		   ovLineXT.setStrokeColor(Color.yellow);
		   this.impXT.setOverlay(ovLineXT);
		   if(mainImage.getNSlices()>1){	   
			   Overlay ovLineY=new Overlay();
			   ovLineY.add(new Line(0,(zSliceNumber+1-0.5)*this.xyToZ,this.zProjections.dimension(0) ,(zSliceNumber+1-0.5)*this.xyToZ));
			   ovLineY.setStrokeColor(Color.green);
			   this.impY.setOverlay(ovLineY);
		   
			   Overlay ovLineX=new Overlay();
			   ovLineX.add(new Line((zSliceNumber+1-0.5)*this.xyToZ,0, (zSliceNumber+1-0.5)*this.xyToZ, this.zProjections.dimension(1) ));
			   ovLineX.setStrokeColor(Color.green);
			   this.impX.setOverlay(ovLineX);
		   }
		}
		buisy=false;
	}
	
	@Override
	public synchronized void imageUpdated(ImagePlus ip) {
		doStuff();
		
	}
    
    
}
