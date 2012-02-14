package frameWork.gui;

import frameWork.Controller;
import frameWork.Model;
import frameWork.Trackable;
import frameWork.TrackingChannel;

import ij.ImagePlus;



import java.awt.event.MouseEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.Callable;

import net.imglib2.RandomAccessibleInterval;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

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
	protected Controller<IT> controller;
	private List<TrackingChannel<? extends Trackable,IT>> tCsToBeDisplayed;
	
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
	   //tCsToBeDisplayed= new ArrayList<TrackingChannel<? extends Trackable,IT>>();
	   tCsToBeDisplayed=model.getTCsAssociatedWithChannel(0);

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
 	  addViewWindow(new MainWindow<IT>(mainImage, model, this));
      this.upDateImages(0, 0, 0,true);
       
       
    
     
       
        return;
    }
    

public void setPosition(int dim, int pos){
	System.out.println("dim:" + dim + " pos: "+pos);
	
	if(dim==2){
		if(currentSliceNumber==pos) return;
		this.currentSliceNumber= pos;	
	}
	if(dim==3){
		if(currentFrameNumber==pos) return;
		this.currentFrameNumber= pos;
	}
	if(dim==4){
		if(currentChannelNumber==pos) return;
		this.currentChannelNumber= pos;
		tCsToBeDisplayed.clear();
		tCsToBeDisplayed=model.getTCsAssociatedWithChannel(currentChannelNumber);
	}
	
	upDateImages(currentFrameNumber, currentSliceNumber, currentChannelNumber, true );

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
	
		vw.rePaint(pos, init);
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

public void addViewWindow( ViewWindow<IT> vw){
	views.add(vw);
	this.upDateImages(0, 0, 0,true);
}

public List<TrackingChannel<? extends Trackable,IT>> getTCsToBeDisplayed(){
	return this.tCsToBeDisplayed;
}

}
