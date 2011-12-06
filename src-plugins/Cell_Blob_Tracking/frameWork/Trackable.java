package frameWork;

import ij.gui.Roi;



public abstract class Trackable {
	public int sequenceId;
	public int frameId;
	
public abstract Roi getShape();


}
