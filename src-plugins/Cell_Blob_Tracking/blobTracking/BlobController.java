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
	
}


@Override
public void click(long[] pos, MouseEvent e){
	System.out.println("click!!!!");
	
	if(e.getID()==MouseEvent.MOUSE_PRESSED){
		
		if(e.getClickCount()==1){
			
		selectedSequenceId=model.selectAt((int)pos[0],(int) pos[1],(int) pos[2],(int) pos[3],(int) pos[4]);	 
		System.out.println("        new selected Sequence ID:"+selectedSequenceId);
		if(!e.isControlDown()){
			selectedIdList.clear();			
		}
		if(this.selectedIdList.contains(selectedSequenceId)){
			selectedIdList.remove(new Integer( selectedSequenceId));	
		}else {
			selectedIdList.add(new Integer( selectedSequenceId));
		}
		
		
		}
	}
	
	if(e.getID()==MouseEvent.MOUSE_CLICKED){
		if(e.isShiftDown() && e.getClickCount()==1){
			trackingChannel.addTrackable(new Blob(model.getNextSequqnceId(), (int)pos[3], pos[0], pos[1], pos[2], 1, trackingChannel.getId()));
		}
		
		if(e.getClickCount()>1) trackingChannel.optimizeFrame((int)pos[3], false);
		
	}

	
	if(e.getID()==MouseEvent.MOUSE_DRAGGED){
		selectedTrackable=trackingChannel.getTrackable(selectedSequenceId, (int)pos[3]);
		if(selectedTrackable==null){
			System.out.println("selectedTrackable==null");
			return; 
		}
		if(pos[0]>=0)selectedTrackable.xPos=pos[0];
		if(pos[1]>=0)selectedTrackable.yPos=pos[1];
		if(pos[2]>=0)selectedTrackable.zPos=pos[2]*model.xyToZ;
		
	}
	
			model.makeChangesPublic();
			
	
	return;
}





}


