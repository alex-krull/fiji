package blobTracking;

import ij.gui.Line;
import ij.gui.Overlay;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Properties;
import java.util.SortedMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.ChannelController;
import frameWork.Model;
import frameWork.MovieChannel;
import frameWork.Policy;
import frameWork.Sequence;
import frameWork.Session;
import frameWork.TrackingFrame;

public class BlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends Policy<Blob, IT>{


	@Override
	public String getTypeName() {
		return "Blob";
	}


	@Override
	public ChannelController<Blob, IT> produceControllerAndChannel(
			Properties sessionProps, Model<IT> model) {
		int cid= Integer.valueOf(sessionProps.getProperty("channelId"));
		int sid= Integer.valueOf(sessionProps.getProperty("sessionId"));
		Session<Blob, IT> btc=  new Session<Blob, IT>(sid, this, model.getMovieChannel(cid));
		model.addTrackingChannel(btc, btc.getId());
		return new ChannelController<Blob,IT>(model,btc, this);
	}

	
	@Override
	public Sequence<Blob> produceSequence(int ident, String lab) {
		return new Sequence<Blob>( ident,  lab, this);
	}
	
	
	@Override
	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected,
			SortedMap <Integer,Blob> trackables, Color color){
		
	Blob b=null;
	Blob lastB=null;
	for(double i=trackables.firstKey();i<=trackables.lastKey();i++){
		lastB=b;
		b=trackables.get((int)i);
		if(lastB!=null&&b!=null){
			Line l=new Line((i-1+0.5)*scaleX-transX,(lastB.yPos+0.5)*scaleY-transY,(i+0.5)*scaleX-transX,(b.yPos+0.5)*scaleY-transY);
			l.setStrokeColor(color);
			l.setStrokeWidth(0.1);
			if(selected) l.setStrokeWidth(0.5);
			ov.add(l);
		
		}
	}
	}
	
	@Override
	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected,
			SortedMap <Integer,Blob> trackables, Color color){

		Blob b=null;
		Blob lastB=null;
		for(double i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get((int)i);		
			if(lastB!=null&&b!=null){
				Line l =new Line((lastB.xPos+0.5)*scaleX-transX,(i-1+0.5)*scaleY-transY,(b.xPos+0.5)*scaleX-transX,(i+0.5)*scaleY-transY);
				l.setStrokeColor(color);
				l.setStrokeWidth(0.1);
				if(selected) l.setStrokeWidth(0.5);
				ov.add(l);
				
			}
		}
	}
	
	@Override
	public TrackingFrame<Blob,IT> produceFrame(int frameNum, MovieChannel<IT> mChannel) {		
		return new BlobFrame<IT>(frameNum, mChannel.getMovieFrame(frameNum));
	}
	
	@Override
	public Blob loadTrackableFromString(String s, int sessionId) {
		System.out.println(s);
	
		String[] values=s.split("\t");
		System.out.println(values.length);
		
		System.out.println(values[1]);
		System.out.println(values[2]);
		System.out.println(values[0]);
		int sId= Integer.valueOf(values[0]);
		int fNum= Integer.valueOf(values[1]);
		double x= Double.valueOf(values[2]);
		double y= Double.valueOf(values[3]);
		double z= Double.valueOf(values[4]);
		double sigma= Double.valueOf(values[5]);
		double sigmaZ= Double.valueOf(values[6]);
		
		return new Blob(sId, fNum, x, y, z, sigma, sessionId);
	}
	
	@Override
	public void click(long[] pos, MouseEvent e, Model<IT> model, List<Integer>  selectedIdList, Session<Blob,IT> trackingChannel){
		System.out.println("click!!!!");
		int selectedSequenceId=-1;
		Blob selectedTrackable;
		
		if(e.getID()==MouseEvent.MOUSE_PRESSED){
			
			if(e.getClickCount()==1){
				
			selectedSequenceId=model.selectAt((int)pos[0],(int) pos[1],(int) pos[2],(int) pos[3],(int) pos[4]);	 
			System.out.println("        new selected Sequence ID:"+selectedSequenceId);
			if(!e.isControlDown()){
				selectedIdList.clear();			
			}
			if(selectedIdList.contains(selectedSequenceId)){
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
	
	public Blob copy(Blob toCopy){
		Blob result=new Blob(toCopy.sequenceId, toCopy.frameId, toCopy.xPos, toCopy.yPos, toCopy.zPos, toCopy.sigma, toCopy.channel);
		result.pK=toCopy.pK;
		return result;
	}


}
