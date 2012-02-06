package frameWork;

import java.awt.Color;

import ij.gui.Overlay;
import ij.gui.Roi;



public abstract class Trackable {
	public int sequenceId;
	public int channel;
	protected int frameId;
	
	
	protected Trackable(int seqId, int fId, int  chan){
		sequenceId=seqId;
		frameId=fId;
		channel=chan;
		
		
	}
	
public abstract void addShapeZ(Overlay ov, boolean selected, Color c);
public abstract void addShapeX(Overlay ov, boolean selected, Color c);
public abstract void addShapeY(Overlay ov, boolean selected, Color c);
public abstract double getDistanceTo(double x, double y, double z);
}
