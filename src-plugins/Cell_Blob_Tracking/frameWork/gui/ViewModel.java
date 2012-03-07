package frameWork.gui;

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
import frameWork.Controller;
import frameWork.Model;
import frameWork.Trackable;
import frameWork.TrackingChannel;

public class ViewModel < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > implements Observer{
	

	protected RandomAccessibleInterval<IT> image;
	
	
	
	
	
			
	protected int currentFrameNumber=0;
	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected int currentTrackingChannel=0;
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
 
	/**
	 * Creates a new ViewModel
	 *
	 * The ViewModel from a Model and a Controller
	 *
	 * @param mod the Model to be used.
	 * @param contr the Controller to be used.
	 **/
	public ViewModel(Model<IT> mod, Controller<IT> contr){
		
	
	   controller=contr;
		
	   model=mod;
   	   tCsToBeDisplayed=model.getTCsAssociatedWithChannel(0);     
            
       image = model.getImage();
       
       System.out.println("channels:" +model.getNumberOfChannels()+ "  frames:"+model.getNumberOfFrames()+ "  slices:"+model.getNumberOfSlices());
       
       
    
  	     
       
       
 		System.out.println("dimensions:" + image.numDimensions());
   
       
 	  views= new ArrayList<ViewWindow<IT>>();
            return;
    }



	/**
	 * set the ViewModel to a different position
	 *
	 * All ViewWindows will be set to the position automatically as well.
	 *
	 * @param dim the dimension that should be changed
	 * @param pos the new value for the chosen dimension.
	 **/
public void setPosition(int dim, int pos){
	//long time0= System.nanoTime();
	
	
	
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

	//long time1= System.nanoTime();
	//System.out.println("]]]]]]]]]]]]]]]]]]Time taken to set position:"+((time1-time0)/1000));
}

/**
 * Gives the current position in form of a long[]
 *
 * @return the position
 **/
public long[] getPosition(){
	long[] result= {0,0,currentSliceNumber, currentFrameNumber, currentChannelNumber};
	return result;
}

public void mouseAtPosition(long [] pos, MouseEvent me){
	if(me.isControlDown()){
		
		controller.StartTracking( currentFrameNumber, currentTrackingChannel);
	}
	
	if(me.isShiftDown()){
		
		controller.StopTracking(currentTrackingChannel);
	}

	controller.click(pos, currentTrackingChannel, me);
}

protected void upDateImages(int frame, int slice, int channel, boolean init){

	long[] pos= {0,0,slice, frame, channel};
	for(ViewWindow<IT> vw:views){	
		vw.upDate(pos, init);
		
	}
 
				
	}



@Override
public void update(Observable arg0, Object arg1) {
	if(arg1!=null)
		currentFrameNumber=(Integer)arg1;
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
