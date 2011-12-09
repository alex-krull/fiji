package frameWork;

import ij.gui.Overlay;
import ij.gui.Roi;



public abstract class Trackable {
	public int sequenceId=1;
	public int frameId=1;
	
public abstract void addShapeZ(Overlay ov);
public abstract void addShapeX(Overlay ov);
public abstract void addShapeY(Overlay ov);
}
