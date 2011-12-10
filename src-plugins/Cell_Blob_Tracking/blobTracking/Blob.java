package blobTracking;


import java.awt.Color;
import java.awt.Font;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.EllipseRoi;
import ij.gui.TextRoi;

import frameWork.Trackable;

public class Blob extends Trackable {
	public double xPos;
	public double yPos;
	public double zPos;
	public double sigma;
	public double sigmaZ;

	public void addShapeZ(Overlay ov){
		Font f=new Font(null,Font.PLAIN,8);
		Roi roi = new EllipseRoi(xPos + sigma * 2, yPos, xPos - sigma * 2,
				yPos, 1);
		roi.setStrokeColor(Color.RED);
		ov.add(roi);
		roi = new TextRoi((int)xPos,(int)yPos,Integer.toString(this.sequenceId),f);
		ov.add(roi);
	}
	
	public void addShapeY(Overlay ov){
		Roi roi = new EllipseRoi(xPos + sigma * 2, zPos, xPos - sigma * 2,
				zPos, sigmaZ / sigma);
		roi.setStrokeColor(Color.RED);
		ov.add(roi);
	}

	public void addShapeX(Overlay ov){
		Roi roi = new EllipseRoi(zPos + sigmaZ * 2, yPos, zPos - sigmaZ * 2,
				yPos, sigma / sigmaZ);
		roi.setStrokeColor(Color.RED);
		ov.add(roi);
	}
	
	public Blob(int seqId, int FrameId, double x, double y, double z, double sig) {
		super(seqId, FrameId);
		xPos = x;
		yPos = y;
		zPos = z;
		sigma = sig;
		sigmaZ = sigma * 2;
	}

	@Override
	public double getDistanceTo(double x, double y, double z) {
		// TODO Auto-generated method stub
		if(x<0) x=this.xPos;
		if(y<0) y=this.yPos;
		if(z<0) z=this.zPos;
		return (x-xPos)*(x-xPos)+(y-yPos)*(y-yPos)+(z-zPos)*(z-zPos);
	}


}
