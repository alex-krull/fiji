package blobTracking;




import ij.gui.Line;
import ij.gui.Overlay;
import frameWork.Sequence;


public class BlobSequence extends Sequence<Blob>{

	public BlobSequence(int ident, String lab) {
		super(ident, lab);		
	}
	
	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY){	
			
		Blob b=null;
		Blob lastB=null;
		for(double i=(double)trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get((int)i);
			if(lastB!=null&&b!=null)  ov.add(new Line((i-1+0.5)*scaleX-transX,(lastB.yPos+0.5)*scaleY-transY,(i+0.5)*scaleX-transX,(b.yPos+0.5)*scaleY-transY));
		}
	
	}
	
	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY){	
	
		Blob b=null;
		Blob lastB=null;
		for(double i=(double)trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get((int)i);
			if(lastB!=null&&b!=null)  ov.add(new Line((lastB.xPos+0.5)*scaleX,(i-1+0.5)*scaleY,(b.xPos+0.5)*scaleX,(i+0.5)*scaleY));
		}
	
	}
	
	

}
