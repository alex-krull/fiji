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
	
	
		
		

			model.makeChangesPublic();
			
	
	return;
}





}


