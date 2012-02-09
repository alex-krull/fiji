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
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;

public class ViewModel < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > implements Observer{
	

	protected RandomAccessibleInterval<IT> image;
	
	
	
	
	
	protected ImagePlus mainImage;
		
		
	protected int currentFrameNumber=0;
	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected double mouseX=0;
	protected double mouseY=0;
	protected double mouseZ=0;
	
	
	protected Model<IT> model;
	protected Controller controller;
	
	protected List <ViewWindow<IT>> views;

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
 
	public ViewModel(ImagePlus imp,  Model<IT> mod, Controller<IT> contr){
		
		controller=contr;
		
	   model=mod;
       // 0 - Check validity of parameters
      

       mainImage= imp;
       
     
       
       if (null == mainImage) return;
    	
       
       image = model.getImage();
       
       System.out.println("channels:" +mainImage.getNChannels()+ "  frames:"+mainImage.getNFrames()+ "  slices:"+mainImage.getNSlices());
       
       
    
  	     
       
       
 		System.out.println("dimensions:" + image.numDimensions());
       
   /*     
       if(model.isVolume()){
 	   
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
       
    */   
       
       views= new ArrayList<ViewWindow<IT>>();
        
       views.add(new MaxProjectionX<IT>(model, this));
       views.add(new MaxProjectionY<IT>(model, this));
       views.add(new KymographY<IT>(model, null,this));
       views.add(new MaxProjectionZ<IT>(model, this));
       views.add(new KymographX<IT>(model, null,this));
       
       views.add(new MainWindow<IT>(mainImage, model, this));
       this.upDateImages(0, 0, 0,true);
       
       
    
     
       
        return;
    }
    

public void setPosition(int dim, int pos){
	
	if(dim==2)this.currentSliceNumber= pos;
	if(dim==3)this.currentFrameNumber= pos;
	if(dim==4)this.currentChannelNumber= pos;
	
	upDateImages(currentFrameNumber, this.currentSliceNumber, this.currentChannelNumber, true );

}

public long[] getPosition(){
	long[] result= {0,0,currentSliceNumber, currentFrameNumber, currentChannelNumber};
	return result;

}

public void mouseAtPosition(long [] pos, MouseEvent me){
	
	
	controller.click(pos, me);
}

protected void upDateImages(int frame, int slice, int channel, boolean init){

	long[] pos= {0,0,slice, frame, channel};
	for(ViewWindow<IT> vw:views){
	
		((ImageWindow<IT>)vw).rePaint(pos, init);
	}
 
				
	}


@Override
public void update(Observable arg0, Object arg1) {
	upDateImages(currentFrameNumber, this.currentSliceNumber, this.currentChannelNumber, false );
}

public int getSelectedSequenceId(){
	return this.controller.selectedSequenceId;
}

public int getCurrentChannelNumber(){
	return currentChannelNumber;
}

}
