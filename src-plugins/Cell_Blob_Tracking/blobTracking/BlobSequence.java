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
	
	public void getKymoOverlayX(Overlay ov){	
			
		Blob b=null;
		Blob lastB=null;
		for(int i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get(i);
			if(lastB!=null&&b!=null)  ov.add(new Line(i-1,lastB.yPos,i,b.yPos));
		}
	
	}
	
	public void getKymoOverlayY(Overlay ov){	
	
		Blob b=null;
		Blob lastB=null;
		for(int i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get(i);
			if(lastB!=null&&b!=null)  ov.add(new Line(lastB.xPos,i-1,b.xPos,i));
		}
	
	}
	
	

}
