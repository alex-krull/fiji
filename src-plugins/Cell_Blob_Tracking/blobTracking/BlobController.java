package blobTracking;

import java.awt.event.MouseEvent;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.ChannelController;
import frameWork.Model;
import frameWork.TrackingChannel;


public class BlobController < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT>  > extends ChannelController<Blob, IT> {
	

public BlobController(Model<IT> model, TrackingChannel<Blob,IT> tc){
	super(model,tc);
	
	
	for(int i=0;i<model.getNumberOfFrames();i++){

	if(i%10>5) continue;
	//model.addTrackable(new Blob(0,i,20 +Math.sin(i/15.0f)*25,20+ Math.sin(i/15.0f)*25,15,4, 0), );
    //model.addTrackable(new Blob(1,i,70 +Math.sin(i/15.0f)*25,20+ Math.sin(i/45.0f)*25,15,4,0),0);
    //model.addTrackable(new Blob(2,i,20 +Math.cos(i/15.0f)*25,70+ Math.sin(i/35.0f)*25,15,4, 0));
    
 	   
    }
	
	
	
	
	
	
}


@Override
public void click(long[] pos, MouseEvent e){
	System.out.println("click!!!!");
	
	if(e.getID()==MouseEvent.MOUSE_CLICKED||e.getID()==MouseEvent.MOUSE_PRESSED)
		selectedSequenceId=model.selectAt((int)pos[0],(int) pos[1],(int) pos[2],(int) pos[3],(int) pos[4]);
	 
		selectedTrackable=(Blob)model.getTrackable(selectedSequenceId, (int) pos[3], (int)pos[4] );
	

	
	if(e.getID()==MouseEvent.MOUSE_DRAGGED){
		if(selectedTrackable==null) return; 
		if(pos[0]>=0)selectedTrackable.xPos=pos[0];
		if(pos[1]>=0)selectedTrackable.yPos=pos[1];
		if(pos[2]>=0)selectedTrackable.zPos=pos[2]*model.xyToZ;
		
	}
	
	if(e.getClickCount()>1){
		
			 model.getTrackingChannel((int)pos[4]).optimizeFrame((int)pos[3], e.getButton()==MouseEvent.BUTTON1);
		

			model.makeChangesPublic();
			
				
			
		
	}
	
	model.makeChangesPublic();
	
	return;
}


}


