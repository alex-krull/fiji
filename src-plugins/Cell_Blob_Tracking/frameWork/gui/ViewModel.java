package frameWork.gui;

import java.awt.Color;
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
import frameWork.Sequence;
import frameWork.Session;
import frameWork.Trackable;

public class ViewModel < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > implements Observer{
	

	protected RandomAccessibleInterval<IT> image;	
	
			
	protected int currentFrameNumber=0;
	protected int currentSliceNumber=0;
	protected int currentChannelNumber=0;
	protected int currentTrackingChannel=0;
	protected double mouseX=0;
	protected double mouseY=0;
	protected double mouseZ=0;
	
	protected boolean drawOverlays=true;
	protected HotKeyListener hotKeyListener;
	
	
	protected Model<IT> model;
	protected Controller<IT> controller;
	public List<Session<? extends Trackable,IT>> sessionsToBeDisplayed;
	
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
		
	hotKeyListener=new HotKeyListener(this);
	   controller=contr;
		
	   model=mod;
	   sessionsToBeDisplayed=model.getTCsAssociatedWithChannel(this.currentChannelNumber);   
            
       image = model.getImage();
       
       System.out.println("channels:" +model.getNumberOfChannels()+ "  frames:"+model.getNumberOfFrames()+ "  slices:"+model.getNumberOfSlices());
       
       
    
  	     
       
       
 		System.out.println("dimensions:" + image.numDimensions());
   
       
 	  views= new ArrayList<ViewWindow<IT>>();
            return;
    }

public void toggleDrawOverlays(){
	drawOverlays=!drawOverlays;
	System.out.println("drawOverlays:"+drawOverlays);
	this.setPosition(-1,-1);
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
		this.currentSliceNumber= Math.min(Math.max(pos,0), model.getNumberOfSlices()-1);
		
	}
	if(dim==3){
		if(currentFrameNumber==pos) return;
		this.currentFrameNumber= Math.min(Math.max(pos,0), model.getNumberOfFrames()-1);
	}
	if(dim==4){
		if(currentChannelNumber==pos) return;
		this.currentChannelNumber= Math.min(Math.max(pos,0), model.getNumberOfChannels()-1);
		sessionsToBeDisplayed.clear();
		sessionsToBeDisplayed=model.getTCsAssociatedWithChannel(currentChannelNumber);
	}
	
	System.out.println("]]]]]]]]]]]]]]]]]]new FrameNumber:"+currentFrameNumber);
	upDateImages(currentFrameNumber, currentSliceNumber, currentChannelNumber, true );

	//long time1= System.nanoTime();
	
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


	controller.click(pos, currentTrackingChannel, me);
}

protected void upDateImages(int frame, int slice, int channel, boolean init){
	
	long[] pos= {0,0,slice, frame, channel};
	System.out.println("]]]]]]]]]]]]]]]]]]new FrameNumber update:"+pos[3]);
	for(ViewWindow<IT> vw:views){	
		vw.upDate(pos, init);
	}
 
				
	}



@Override
public void update(Observable arg0, Object arg1) {
	if(arg1!=null)
		currentFrameNumber=(Integer)arg1;
	upDateImages(currentFrameNumber, this.currentSliceNumber, this.currentChannelNumber, arg1!=null );
}

//public int getSelectedSequenceId(){
//	return this.controller.getSelectedSeqId();
//}

public boolean isSelected(int sId, int cId){
	return controller.isSeletced(sId, cId);
}

public int getCurrentChannelNumber(){
	return currentChannelNumber;
}

public void addViewWindow( ViewWindow<IT> vw, double initialZoom){
	views.add(vw);
	vw.addKeyListener(hotKeyListener);
	
	this.upDateImages(0, 0, 0,true);
	vw.setZoom(initialZoom);
}

public List<Session<? extends Trackable,IT>> getSessionsToBeDisplayed(){
	return this.sessionsToBeDisplayed;
}

public boolean getDrawOverLays(){
	return drawOverlays;
}

public void toggleTracking(){
	controller.toggleTracking(currentFrameNumber);
}

public void deleteSequence(){
	controller.deleteSequence();
}

public void trimSequence(){
	controller.trimSequence(currentFrameNumber);
}

public void splitSequence(){
	controller.splitSequence(currentFrameNumber);
}

public boolean isTracking(){
return controller.isTracking();
}

public void saveAll(){
	controller.saveAll();
}

public void setColor(Color c){
	controller.setColor(c);
}

public  List <Sequence<? extends Trackable>> getVisibleSequences(){
	List <Sequence<? extends Trackable>> results = new ArrayList <Sequence<? extends Trackable>>();
	for(Session<? extends Trackable ,IT> tc: sessionsToBeDisplayed){
		results.addAll(tc.getSeqsCollection());
	}
	return results;
}
 
public void setSelectionList(List <Integer> selectedIds){
	controller.setSelectionList(selectedIds);
}

public void mergeSequenences(){
	controller.mergeSequenences();
}

public Controller<IT> getController(){
	return controller;
}

public void reFreashSessionToBeDisplayed(){
	sessionsToBeDisplayed=model.getTCsAssociatedWithChannel(this.currentChannelNumber);  
}

}
