package frameWork.gui;

import frameWork.Controller;
import frameWork.Model;
import frameWork.Trackable;
import ij.ImageListener;
import ij.ImagePlus;
import ij.gui.ImageCanvas;
import ij.gui.Line;
import ij.gui.Overlay;
import ij.plugin.ContrastEnhancer;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;

public class ViewModel <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >{
	

	protected RandomAccessibleInterval<IT> image;
	
	
	protected boolean isVolume=false;
	protected boolean isTimeSequence=false;
	protected boolean isMultiChannel=false;
	
	protected ImagePlus mainImage;
		
	protected RandomAccessibleInterval<IT> zProjections;
	protected RandomAccessibleInterval<IT> xProjections;
	protected RandomAccessibleInterval<IT> yProjections;
	protected RandomAccessibleInterval<IT> xtProjections;
	protected RandomAccessibleInterval<IT> ytProjections; 
	
	protected int currentFrameNumber=0;
	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected double mouseX=0;
	protected double mouseY=0;
	protected double mouseZ=0;
	protected int selectedSequenceId;
	
	protected Model<T,IT> model;
	protected Controller<T> controller;
	
	protected List <ViewWindow<T,IT>> views;

	class ProjectionJob	implements Callable<RandomAccessibleInterval<IT> >{
		RandomAccessibleInterval<IT> image;
		int dim;
		
		ProjectionJob(RandomAccessibleInterval<IT> img, int d){
			image= img;
			dim=d;
		}
		@Override
		public RandomAccessibleInterval<IT> call() throws Exception {
			
			return ImglibTools.projection(image, dim);
		}
		
	}
 
	public ViewModel(ImagePlus imp,  Model<T,IT> mod, Controller<T> contr){
		
		controller=contr;
		
	   model=mod;
       // 0 - Check validity of parameters
      

       mainImage= imp;
       
     
       
       if (null == mainImage) return;
    	
       
       image = model.getImage();
       
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
 	   
    	   zProjections=ImglibTools.projection(image,2,20);
    	   xProjections=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(image,0,20),0,1) ),0  ) ); 
           yProjections=ImglibTools.projection(image,1,20);
    	   
    	   
           xProjections=ImglibTools.scaleByFactor(xProjections,0,model.xyToZ);
           yProjections=ImglibTools.scaleByFactor(yProjections,1,model.xyToZ);
                  
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
       
       
       
       views= new ArrayList<ViewWindow<T,IT>>();
        
       views.add(new MaxProjectionX<T,IT>(model, image,this));
       views.add(new MaxProjectionY<T,IT>(model, image,this));
       views.add(new KymographY<T,IT>(model, ytProjections,this));
       views.add(new MaxProjectionZ<T,IT>(model, image,this));
       views.add(new KymographX<T,IT>(model, xtProjections,this));
       
       views.add(new MaxProjectionX<T,IT>(model, image,this));
       views.add(new MaxProjectionY<T,IT>(model, image,this));
       views.add(new KymographY<T,IT>(model, ytProjections,this));
       views.add(new MaxProjectionZ<T,IT>(model, image,this));
       views.add(new KymographX<T,IT>(model, xtProjections,this));
       
       views.add(new MaxProjectionX<T,IT>(model, image,this));
       views.add(new MaxProjectionY<T,IT>(model, image,this));
       views.add(new KymographY<T,IT>(model, ytProjections,this));
       views.add(new MaxProjectionZ<T,IT>(model, image,this));
       views.add(new KymographX<T,IT>(model, xtProjections,this));
       
       views.add(new MainWindow<T,IT>(mainImage, image, model, this));
       this.upDateImages(0, 0, 0,true);
       
       
    
     
       
        return;
    }
    
public void setPosition(int dim, int pos){
	if(dim==2)this.currentSliceNumber= pos;
	if(dim==3)this.currentFrameNumber= pos;
	System.out.println("should be new frameA: "+ currentFrameNumber);
	upDateImages(currentFrameNumber, this.currentSliceNumber, this.currentChannelNumber, false );
}
	
protected void upDateImages(int frame, int slice, int channel, boolean init){
	System.out.println("should be new frameB: "+ frame);
	
	long[] pos= {0,0,slice, frame, channel};
	for(ViewWindow<T,IT> vw:views){
		System.out.println("should be new frameC: "+ frame);
		System.out.println("should be new frameD: "+ pos[3]);
		((ImageWindow<T,IT>)vw).rePaint(pos);
	}
 
				
	}



}
