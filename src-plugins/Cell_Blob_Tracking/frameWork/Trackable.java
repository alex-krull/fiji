package frameWork;

import ij.gui.Overlay;
import ij.gui.Roi;



public abstract class Trackable {
	public int sequenceId;
	protected int frameId;
	
	protected Trackable(int seqId, int fId){
		sequenceId=seqId;
		frameId=fId;
	
	}
	
public abstract void addShapeZ(Overlay ov, boolean selected);
public abstract void addShapeX(Overlay ov, boolean selected);
public abstract void addShapeY(Overlay ov, boolean selected);
public abstract double getDistanceTo(double x, double y, double z);
}
