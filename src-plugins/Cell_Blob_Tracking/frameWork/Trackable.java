package frameWork;

import ij.gui.Overlay;
import ij.gui.Roi;



public abstract class Trackable {
	protected int sequenceId;
	protected int frameId;
	
	protected Trackable(int seqId, int fId){
		sequenceId=seqId;
		frameId=fId;
	
	}
	
public abstract void addShapeZ(Overlay ov);
public abstract void addShapeX(Overlay ov);
public abstract void addShapeY(Overlay ov);
}
