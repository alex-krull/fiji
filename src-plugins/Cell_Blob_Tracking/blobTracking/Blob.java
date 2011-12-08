package blobTracking;

import ij.gui.Roi;
import ij.gui.EllipseRoi;
import frameWork.Frame;
import frameWork.Trackable;

public class Blob extends Trackable{
	public double xPos;
	public double yPos;
	public double zPos;
	public double sigma;
	
	
	public Blob(double x, double y ,double z, double sig){
		xPos=x;
		yPos=y;
		zPos=z;
		sigma=sig;
	}
	
	public Roi getShape() {
		
		
		Roi roi= new EllipseRoi(xPos+sigma*2,yPos,xPos-sigma*2,yPos,1 ); 
		
		return roi;
	}

		
	
}
