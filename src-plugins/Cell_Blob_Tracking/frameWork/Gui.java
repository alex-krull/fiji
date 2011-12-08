package frameWork;

import ij.IJ;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.plugin.ContrastEnhancer;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

import tools.ImglibTools;

import net.imglib2.Cursor;
import net.imglib2.IterableInterval;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.ImagePlusAdapter;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.IntervalView;
import net.imglib2.view.Views;

public abstract class Gui<T extends Trackable, IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>  > implements MouseMotionListener,MouseListener, ImageListener {

	protected RandomAccessibleInterval<IT> image;
	protected ImagePlus impZ;
	protected ImagePlus impX;
	protected ImagePlus impY;
	
	protected ImagePlus impXT;
	protected ImagePlus impYT;
	
	protected boolean isVolume=false;
	protected boolean isTimeSequence=false;
	protected boolean isMultiChannel=false;
	
	protected ImagePlus mainImage;
	protected ImageCanvas canvasZ;
	protected ImageCanvas canvasX;
	protected ImageCanvas canvasY;
	
	protected RandomAccessibleInterval<IT> zProjections;
	protected RandomAccessibleInterval<IT> xProjections;
	protected RandomAccessibleInterval<IT> yProjections;
	protected RandomAccessibleInterval<IT> xtProjections;
	protected RandomAccessibleInterval<IT> ytProjections;
	
	protected int currentFrameNumber=0;
	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected boolean buisy=false;
	protected double xyToZ=3.5;
	protected double mouseX=0;
	protected double mouseY=0;
	protected double mouseZ=0;
	
	protected Controler<T,IT> controler;

	
 
	protected Gui(ImagePlus imp, RandomAccessibleInterval<IT> img, Controler<T,IT> contr){
	
		controler=contr;
        // 0 - Check validity of parameters
      

        mainImage= imp;
      
        if (null == mainImage) return;
    	
       
       image = img;
       
       System.out.println("channels:" +mainImage.getNChannels()+ "  frames:"+mainImage.getNFrames()+ "  slices:"+mainImage.getNSlices());
       this.isVolume=mainImage.getNSlices()>1;
       this.isTimeSequence=mainImage.getNFrames()>1;
       this.isMultiChannel=mainImage.getNChannels()>1;
       
       if(isVolume&&!isTimeSequence){
    	   isVolume=false;
    	   isTimeSequence=true;
    	   System.out.println("SWITCHING DIMENSIONS");
       }
       
       
       
       if(isMultiChannel){
    	   image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,image.numDimensions()-1),2) );
       if(image.numDimensions()==5)  	   
    	   image = Views.zeroMin(Views.invertAxis(Views.rotate(image,2,3),2 ));
       
       }
  	     
       
       
 		System.out.println("dimensions:" + image.numDimensions());
       
         
       if(isVolume){
    	   zProjections=ImglibTools.projection(image,2);
    	   xProjections=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(image,0),0,1) ),0  ) ); 
           yProjections=ImglibTools.projection(image,1);
           xProjections=ImglibTools.scaleByFactor(xProjections,0,this.xyToZ);
           yProjections=ImglibTools.scaleByFactor(yProjections,1,this.xyToZ);
                  
           xtProjections=Views.zeroMin( Views.invertAxis(  Views.rotate( ImglibTools.projection(xProjections,0) ,0,1),0 ) )  ;
           ytProjections=ImglibTools.projection(yProjections,1);
       }
       else{ 
    	   zProjections=null;
    	   xProjections=null; 
    	   yProjections=null;
    	   
    	   xtProjections=Views.zeroMin( Views.invertAxis(  Views.rotate( ImglibTools.projection(image,0) ,0,1),0 ) )  ;
    	   ytProjections=ImglibTools.projection(image,1);
       }
       
       
       
       
       
   
       this.upDateImages(0, 0, 0,true);
       
       
       	
       //mainImage=ImageJFunctions.showUnsignedShort(img,"stack");     
    //   ImageJFunctions.showUnsignedShort(projection(rot,1),"x and z"); 
       
    
   //    canvasX=impX.getCanvas();
   //    canvasY=impY.getCanvas();
   //    canvasZ=impZ.getCanvas();
           
     
      
    //   impX.addImageListener(this);
    //   impY.addImageListener(this);
    //   impZ.addImageListener(this);
       mainImage.addImageListener(this);
       if(impX!=null) impX.getCanvas().addMouseMotionListener(this);
       if(impY!=null) impY.getCanvas().addMouseMotionListener(this);
       if(impZ!=null) impZ.getCanvas().addMouseMotionListener(this);
       if(impXT!=null) impXT.getCanvas().addMouseMotionListener(this);
       if(impYT!=null) impYT.getCanvas().addMouseMotionListener(this);
       
       if(impXT!=null) impXT.getCanvas().addMouseListener(this);
       if(impYT!=null) impYT.getCanvas().addMouseListener(this);
       if(impX!=null) impX.getCanvas().addMouseListener(this);
       if(impY!=null) impY.getCanvas().addMouseListener(this);
    
       
	//   gui screen = new gui("Example 1");
    //   screen.setSize(500,100);
    //   screen.setVisible(true);
    //   screen.add(impZ.getCanvas());
       
     
     
       
        return;
    }
    
	
private synchronized void upDateImages(int frame, int slice, int channel, boolean init){
		
		
		System.out.println("=================f:"+frame + " s:" + slice+ " c:" + channel+ " i:"+init);
		
		RandomAccessibleInterval<IT> imgx=zProjections;
		RandomAccessibleInterval<IT> imgy=xProjections;
		RandomAccessibleInterval<IT> imgz=yProjections;
	    RandomAccessibleInterval<IT> imgxt= xtProjections;      
	    RandomAccessibleInterval<IT> imgyt= ytProjections;
	       
	       if(isVolume){
	       imgz=Views.hyperSlice(zProjections,2,frame);
	       imgx=Views.hyperSlice(xProjections,2,frame);
	       imgy=Views.hyperSlice(yProjections,2,frame);
	       }
	       
	       if(isMultiChannel){
	    	   if(isVolume){
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
	       
	      
	       
	       if(isVolume){	     
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
			if(isVolume){
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
	    
	    
	    
	    
	    
	    
	    
		
		System.out.println("new slice:"+mainImage.getCurrentSlice());
		
		
		
		this.mainImage.updateAndDraw();
		
		addOverlays(frame,slice,channel);
		

	  
				
	}

private synchronized void updatePosition(int x,int y, int slice ,int frame, int channel){

	if(buisy) return;
	buisy=true;
	System.out.println("oframe:"+ currentFrameNumber+ "   oslice:"+ currentSliceNumber+ "  ochannel:"+currentChannelNumber );
	
	currentSliceNumber=slice;
	currentFrameNumber=frame;
	currentChannelNumber=channel;
	if (mainImage.getNFrames()<=1 && mainImage.getNSlices()>1) mainImage.setPosition(channel+1, frame+1, slice+1); // switch dimensions
	else mainImage.setPosition(channel+1, slice+1, frame+1);
	this.upDateImages(currentFrameNumber, currentSliceNumber, currentChannelNumber, false);
	System.out.println("nframe:"+ currentFrameNumber+ "   nslice:"+ currentSliceNumber+ "  nchannel:"+currentChannelNumber );
	//mainImage.setSlice(slice+1);
	
	buisy=false;

}
	
	private synchronized void doStuff(){
		int newFrameNumber= mainImage.getFrame()-1;
		int newSliceNumber= mainImage.getSlice()-1;
		int newChannelNumber=mainImage.getChannel()-1;
		
		if (mainImage.getNFrames()<=1 && mainImage.getNSlices()>1){
			newFrameNumber= mainImage.getSlice()-1;	// switch dimensions
			newSliceNumber= mainImage.getFrame()-1;
		}
		
		if(currentFrameNumber==newFrameNumber
				&& currentSliceNumber==newSliceNumber
				&& currentChannelNumber==newChannelNumber){
			return;    // sliders were not moved
			
		}else{
						// sliders were moved
		
		System.out.println("______________________________");
		System.out.println("frame:"+ newFrameNumber+ "   slice:"+ newSliceNumber+ "  channel:"+newChannelNumber );
		System.out.println("cframe:"+ currentFrameNumber+ "   cslice:"+ currentSliceNumber+ "  cchannel:"+currentChannelNumber );
		
			
		updatePosition(0,0, newSliceNumber ,newFrameNumber, newChannelNumber);
			
		
		}
	}
	
	private void addOverlays(int frame, int slice, int channel){
		
		 Overlay ovLineYT=new Overlay();
		   int newSliceNumber=frame;
		   int zSliceNumber= slice;
		   
		   ovLineYT.add(new Line(0,newSliceNumber+1-0.5,this.image.dimension(0) ,newSliceNumber+1-0.5));
		   ovLineYT.setStrokeColor(Color.yellow);
		   this.impYT.setOverlay(ovLineYT);
		   
		   Overlay ovLineXT=new Overlay();
		   ovLineXT.add(new Line(newSliceNumber+1-0.5,0, newSliceNumber+1-0.5, this.image.dimension(1) ));
		   ovLineXT.setStrokeColor(Color.yellow);
		   this.impXT.setOverlay(ovLineXT);
		   if(isVolume){	   
			   Overlay ovLineY=new Overlay();
			   ovLineY.add(new Line(0,(zSliceNumber+1-0.5)*this.xyToZ,this.zProjections.dimension(0) ,(zSliceNumber+1-0.5)*this.xyToZ));
			   ovLineY.setStrokeColor(Color.green);
			   this.impY.setOverlay(ovLineY);
		   
			   Overlay ovLineX=new Overlay();
			   ovLineX.add(new Line((zSliceNumber+1-0.5)*this.xyToZ,0, (zSliceNumber+1-0.5)*this.xyToZ, this.zProjections.dimension(1) ));
			   ovLineX.setStrokeColor(Color.green);
			   this.impX.setOverlay(ovLineX);
		   }
		   
		   
		   
		   List<T> trackables= controler.getTrackablesForFrame(frame);
		   Overlay circles=new Overlay();
		   for(Trackable t : trackables){
			   circles.add(t.getShape());
		   }
		   if(impZ!=null) impZ.setOverlay(circles);
	}


@Override
public void mouseDragged(MouseEvent arg0) {
	// TODO Auto-generated method stub
	if(!arg0.isControlDown()) return;
	if(impX!=null && impX.getCanvas().equals(arg0.getSource())){
		int x=impX.getCanvas().offScreenX(arg0.getX());
		int y=impX.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		this.updatePosition(0, 0, (int)(x/this.xyToZ), currentFrameNumber, currentChannelNumber);
		
	}
	
	if(impY!=null && impY.getCanvas().equals(arg0.getSource())){
		int x=impY.getCanvas().offScreenX(arg0.getX());
		int y=impY.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		this.updatePosition(0, 0, (int)(y/this.xyToZ), currentFrameNumber, currentChannelNumber);
	}
	
	if(impXT!=null && impXT.getCanvas().equals(arg0.getSource())){
		int x=impXT.getCanvas().offScreenX(arg0.getX());
		int y=impXT.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);
		
	}
	
	if(impYT!=null && impYT.getCanvas().equals(arg0.getSource())){
		int x=impYT.getCanvas().offScreenX(arg0.getX());
		int y=impYT.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		
		this.updatePosition(0, 0, currentSliceNumber, y, currentChannelNumber);
//		upDateImages(y, 1, 0, false);
	}
	arg0.consume();
	
	
}

@Override
public void mouseMoved(MouseEvent arg0) {

	
	// TODO Auto-generated method stub
	
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
public void imageUpdated(ImagePlus arg0) {
	// TODO Auto-generated method stub
	doStuff();
	
}





//MouseListener Methods
@Override
public void mouseClicked(MouseEvent arg0) {
	
	
	if(impX!=null && impX.getCanvas().equals(arg0.getSource())){
		int x=impX.getCanvas().offScreenX(arg0.getX());
		int y=impX.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		this.updatePosition(0, 0, (int)(x/this.xyToZ), currentFrameNumber, currentChannelNumber);
		
	}
	
	if(impY!=null && impY.getCanvas().equals(arg0.getSource())){
		int x=impY.getCanvas().offScreenX(arg0.getX());
		int y=impY.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		this.updatePosition(0, 0, (int)(y/this.xyToZ), currentFrameNumber, currentChannelNumber);
	}
	
	if(impXT!=null && impXT.getCanvas().equals(arg0.getSource())){
		int x=impXT.getCanvas().offScreenX(arg0.getX());
		int y=impXT.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		this.updatePosition(0, 0, currentSliceNumber, x, currentChannelNumber);
		
	}
	
	if(impYT!=null && impYT.getCanvas().equals(arg0.getSource())){
		int x=impYT.getCanvas().offScreenX(arg0.getX());
		int y=impYT.getCanvas().offScreenY(arg0.getY());
		System.out.println("x:"+ x +"  y:"+y);
		
		this.updatePosition(0, 0, currentSliceNumber, y, currentChannelNumber);
//		upDateImages(y, 1, 0, false);
	}
	
}

@Override
public void mouseEntered(MouseEvent arg0) {

	// TODO Auto-generated method stub
	
}

@Override
public void mouseExited(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mousePressed(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}

@Override
public void mouseReleased(MouseEvent arg0) {
	// TODO Auto-generated method stub
	
}


}
