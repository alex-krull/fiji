package blobTracking;




import ij.gui.Line;
import ij.gui.Overlay;
import frameWork.Sequence;


public class BlobSequence extends Sequence<Blob>{

	public BlobSequence(int ident, String lab) {
		super(ident, lab);		
	}
	
	@Override
	public void getKymoOverlayX(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected){	
			
		Blob b=null;
		Blob lastB=null;
		for(double i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get((int)i);
			if(lastB!=null&&b!=null){
				Line l=new Line((i-1+0.5)*scaleX-transX,(lastB.yPos+0.5)*scaleY-transY,(i+0.5)*scaleX-transX,(b.yPos+0.5)*scaleY-transY);
				l.setStrokeColor(this.getColor());
				l.setStrokeWidth(0.1);
				if(selected) l.setStrokeWidth(0.5);
				ov.add(l);
			
			}
		}
	
	}
	
	@Override
	public void getKymoOverlayY(Overlay ov, double scaleX, double scaleY, double transX, double transY, boolean selected){	
	
		Blob b=null;
		Blob lastB=null;
		for(double i=trackables.firstKey();i<=trackables.lastKey();i++){
			lastB=b;
			b=trackables.get((int)i);		
			if(lastB!=null&&b!=null){
				Line l =new Line((lastB.xPos+0.5)*scaleX-transX,(i-1+0.5)*scaleY-transY,(b.xPos+0.5)*scaleX-transX,(i+0.5)*scaleY-transY);
				l.setStrokeColor(this.getColor());
				l.setStrokeWidth(0.1);
				if(selected) l.setStrokeWidth(0.5);
				ov.add(l);
				
			}
		}
	
	}

	@Override
	public String getTypeName() {
		return "Blob";
	}
	
	
	
	

}
