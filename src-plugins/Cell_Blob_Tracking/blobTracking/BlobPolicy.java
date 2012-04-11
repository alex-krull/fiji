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

public abstract class BlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends Policy<Blob, IT>{

	@Override
	public ChannelController<Blob, IT> produceControllerAndChannel(
			Properties sessionProps, Model<IT> model) {
		Integer cid= Integer.valueOf(sessionProps.getProperty("channelId"));
		if(cid==null)cid=0;
		Integer sid= Integer.valueOf(sessionProps.getProperty("sessionId"));
		if(sid==null)sid=model.getNextTCId();
		Session<Blob, IT> btc=  new BlobSession<IT>(sid, this, model.getMovieChannel(cid));
		
		btc.setProperties(sessionProps);
		model.addTrackingChannel(btc, btc.getId());
		return new ChannelController<Blob,IT>(model,btc, this);
	}

	
	@Override
	public Sequence<Blob> produceSequence(int ident, String lab, Session<Blob, IT> session) {
		return new Sequence<Blob>( ident,  lab, this, session);
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
			l.setStrokeWidth(1);
			if(selected) l.setStrokeWidth(4);
			ov.add(l);
			
			
			
		
		}
		if(!(trackables.keySet().contains((int)i+1)) && !(trackables.keySet().contains((int)i-1) ) ){
			
			
			Line lStart1 =new Line((i+0.5)*scaleX-transX-1,(b.yPos+0.5)*scaleY-transY-1,
					(i+0.5)*scaleX-transX+1,(b.yPos+0.5)*scaleY-transY+1);
			lStart1.setStrokeColor(color);
			lStart1.setStrokeWidth(1);
			if(selected) lStart1.setStrokeWidth(4);
			ov.add(lStart1);
			
			Line lStart2 =new Line((i+0.5)*scaleX-transX+1,(b.yPos+0.5)*scaleY-transY-1,
					(i+0.5)*scaleX-transX-1,(b.yPos+0.5)*scaleY-transY+1);
			lStart2.setStrokeColor(color);
			lStart2.setStrokeWidth(1);
			if(selected) lStart2.setStrokeWidth(4);
			ov.add(lStart2);
		
		
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
				l.setStrokeWidth(1);
				if(selected) l.setStrokeWidth(4);
				ov.add(l);
				
			}
			
			if(!(trackables.keySet().contains((int)i+1)) && !(trackables.keySet().contains((int)i-1) ) ){
				
				
					
					Line lStart1 =new Line((b.xPos+0.5)*scaleX-transX-1,(i+0.5)*scaleY-transY-1,
							(b.xPos+0.5)*scaleX-transX+1,(i+0.5)*scaleY-transY+1);
					lStart1.setStrokeColor(color);
					lStart1.setStrokeWidth(1);
					if(selected) lStart1.setStrokeWidth(4);
					ov.add(lStart1);
					
					Line lStart2 =new Line((b.xPos+0.5)*scaleX-transX+1,(i+0.5)*scaleY-transY-1,
							(b.xPos+0.5)*scaleX-transX-1,(i+0.5)*scaleY-transY+1);
					lStart2.setStrokeColor(color);
					lStart2.setStrokeWidth(1);
					if(selected) lStart2.setStrokeWidth(4);
					ov.add(lStart2);
				
				
			}
		}
		
	
		
		
		

	}
	
	@Override
	public TrackingFrame<Blob,IT> produceFrame(int frameNum, MovieChannel<IT> mChannel) {
		return new BlobFrame<IT>(frameNum, mChannel.getMovieFrame(frameNum));
	}
	
	@Override
	public Blob loadTrackableFromString(String s, int sessionId) {
	//	System.out.println(s);
	
		String[] values=s.split("\t");
	//	System.out.println(values.length);
		
	//	System.out.println(values[1]);
	//	System.out.println(values[2]);
	//	System.out.println(values[0]);
		int sId= Integer.valueOf(values[0]);
		int fNum= Integer.valueOf(values[1]);
		double x= Double.valueOf(values[2]);
		double y= Double.valueOf(values[3]);
		double z= Double.valueOf(values[4]);
		double sigma= Double.valueOf(values[5]);
		double sigmaZ= Double.valueOf(values[6]);
		
		Blob nB=new Blob(sId, fNum, x, y, z, sigma, sessionId);
		
		return nB;
	}
	
	@Override
	public int click(long[] pos, MouseEvent e, Model<IT> model, List<Integer>  selectedIdList, Session<Blob,IT> trackingChannel, int selectedSequenceId){
		
		
		Blob selectedTrackable;
		
		if(e.getID()==MouseEvent.MOUSE_PRESSED){
			
			if(e.getClickCount()==1){
				
			selectedSequenceId=trackingChannel.selectAt((int)pos[0],(int) pos[1],(int) pos[2],(int) pos[3],(int) pos[4]);	 
		//	System.out.println("        new selected Sequence ID:"+selectedSequenceId);
			if(!e.isControlDown()){
				selectedIdList.clear();			
			}
			if(selectedIdList.contains(selectedSequenceId)){
				selectedIdList.remove(new Integer( selectedSequenceId));	
			}else {
				
				selectedIdList.add(new Integer( selectedSequenceId));
			}
			
			
			}
			model.makeStructuralChange();
		}
		
		if(e.getID()==MouseEvent.MOUSE_CLICKED){
			synchronized (model){
			if(e.isShiftDown() && e.getClickCount()==1){
				Blob nB=new Blob(model.getNextSequqnceId(), (int)pos[3], pos[0], pos[1], pos[2], 1, trackingChannel.getId());				
				BlobSession<IT> bs= (BlobSession<IT>)trackingChannel;
				nB.sigma=bs.getDefaultSigma();
				nB.sigmaZ=bs.getDefaultSigmaZ();
				nB.minSigma=bs.getDefaultMinSigma();
				nB.maxSigma=bs.getDefaultMaxSigma();
				nB.minSigmaZ=bs.getDefaultMinSigmaZ();
				nB.maxSigmaZ=bs.getDefaultMaxSigmaZ();
				nB.autoSigma=bs.isAutoSigma();
				nB.autoSigmaZ=bs.isAutoSigmaZ();
				
				selectedIdList.clear();
				selectedIdList.add(nB.sequenceId);
				trackingChannel.addTrackable(nB);
			}
			
			if(e.getClickCount()>1) trackingChannel.optimizeFrame((int)pos[3], false, selectedIdList);
			model.makeStructuralChange();
			}
		}

		
		if(e.getID()==MouseEvent.MOUSE_DRAGGED){
			selectedTrackable=trackingChannel.getTrackable(selectedSequenceId, (int)pos[3]);
			if(selectedTrackable==null){
	//			System.out.println("selectedTrackable==null");
				return selectedSequenceId; 
			}
			if(pos[0]>=0)selectedTrackable.xPos=pos[0];
			if(pos[1]>=0)selectedTrackable.yPos=pos[1];
			if(pos[2]>=0)selectedTrackable.zPos=pos[2]*model.getXyToZ();
			
		}
		
				model.makeChangesPublic();
		return selectedSequenceId;
	}
	
	@Override
	public Blob copy(Blob toCopy){
		if(toCopy==null) return null;
		Blob result=new Blob(toCopy.sequenceId, toCopy.frameId, toCopy.xPos, toCopy.yPos, toCopy.zPos, toCopy.sigma, toCopy.channel);
		result.pK=toCopy.pK;
		return result;
	}


}
