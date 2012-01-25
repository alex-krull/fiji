package blobTracking;


import java.util.List;

import ij.gui.Line;
import ij.gui.Overlay;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import frameWork.Sequence;
import frameWork.Trackable;

public class BlobSequence extends Sequence<Blob>{

	public BlobSequence(int ident, String lab) {
		super(ident, lab);		
	}
	
	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY){	
			
		Blob b=null;
		Blob lastB=null;
		for(int i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get(i);
			if(lastB!=null&&b!=null)  ov.add(new Line((i-1)*scaleX,lastB.yPos*scaleY,i*scaleX,b.yPos*scaleY));
		}
	
	}
	
	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY){	
	
		Blob b=null;
		Blob lastB=null;
		for(int i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get(i);
			if(lastB!=null&&b!=null)  ov.add(new Line(lastB.xPos*scaleX,(i-1)*scaleY,b.xPos*scaleX,i*scaleY));
		}
	
	}
	
	

}
