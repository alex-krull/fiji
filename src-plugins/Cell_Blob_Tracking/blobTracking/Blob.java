package blobTracking;

import ij.gui.Roi;
import frameWork.Frame;
import frameWork.Trackable;

public class Blob extends Trackable{
	public double xPos;
	public double yPos;
	public double zPos;
	public double sigma;
	
	
	public Blob(int x, int y ,int z, double sig){
		xPos=x;
		yPos=y;
		zPos=z;
		sigma=sig;
	}
	
	public Roi getShape() {
		// TODO Auto-generated method stub
		return null;
	}

		
	
}
