package blobTracking;

import ij.gui.Line;
import ij.gui.Overlay;

import java.awt.Color;
import java.util.Properties;
import java.util.SortedMap;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.ChannelController;
import frameWork.Model;
import frameWork.Policy;
import frameWork.Sequence;

public class BlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends Policy<Blob, BlobFrame<IT>, IT>{


	@Override
	public String getTypeName() {
		return "Blob";
	}


	@Override
	public ChannelController<Blob, IT> produceControllerAndChannel(
			Properties sessionProps, Model<IT> model) {
		int cid= Integer.valueOf(sessionProps.getProperty("channelId"));
		int sid= Integer.valueOf(sessionProps.getProperty("sessionId"));
		SingleChannelSession<IT> btc=  new SingleChannelSession<IT>(model.getMovieChannel(cid), sid,this);
		model.addTrackingChannel(btc, btc.getId());
		return new BlobController<IT>(model,btc);
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


}
