package blobTracking;


import java.awt.Color;
import java.awt.Font;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.EllipseRoi;
import ij.gui.TextRoi;

import frameWork.Trackable;
import org.apache.commons.math.special.Erf;

public class Blob extends Trackable {
	public double xPos;
	public double yPos;
	public double zPos;
	public double sigma;
	public double sigmaZ;
	
	public double denominator=0;

	public double conditionalProb(int x, int y, int z){
		
		return 0;
	}
	
	public void addShapeZ(Overlay ov, boolean selected){
		Font f=new Font(null,Font.PLAIN,8);
		
		if(selected){
			Roi roiS=new EllipseRoi(xPos + sigma * 2, yPos, xPos - sigma * 2,
					yPos, 1);
			Color c= new Color(255, 0, 0, 100);
			roiS.setStrokeColor(c);
			roiS.setStrokeWidth(5);
			ov.add(roiS);
		}
		
		Roi roi = new EllipseRoi(xPos + sigma * 2, yPos, xPos - sigma * 2,
				yPos, 1);
		
		roi.setStrokeColor(Color.RED);
		roi.setStrokeWidth(1);
		ov.add(roi);
		
		
		roi = new TextRoi((int)xPos,(int)yPos,Integer.toString(this.sequenceId),f);
		ov.add(roi);
	}
	
	public void addShapeY(Overlay ov, boolean selected){
		if(selected){
			Roi roiS=new EllipseRoi(xPos + sigma * 2, zPos, xPos - sigma * 2,
					zPos, sigmaZ / sigma);
			Color c= new Color(255, 0, 0, 100);
			roiS.setStrokeColor(c);
			roiS.setStrokeWidth(5);
			ov.add(roiS);
		}
		
		Roi roi = new EllipseRoi(xPos + sigma * 2, zPos, xPos - sigma * 2,
				zPos, sigmaZ / sigma);
		roi.setStrokeColor(Color.RED);
		roi.setStrokeWidth(1);
		ov.add(roi);
	}

	public void addShapeX(Overlay ov, boolean selected){
		if(selected){
			Roi roiS=new EllipseRoi(zPos + sigmaZ * 2, yPos, zPos - sigmaZ * 2,
					yPos, sigma / sigmaZ);
			Color c= new Color(255, 0, 0, 100);
			roiS.setStrokeColor(c);
			roiS.setStrokeWidth(5);
			ov.add(roiS);
		}
		
		Roi roi = new EllipseRoi(zPos + sigmaZ * 2, yPos, zPos - sigmaZ * 2,
				yPos, sigma / sigmaZ);
		roi.setStrokeColor(Color.RED);
		roi.setStrokeWidth(1);
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
		double xn= x;
		double yn= y;
		double zn= z;
		if(x<0) xn=this.xPos;
		if(y<0) yn=this.yPos;
		if(z<0) zn=this.zPos;
		return (xn-xPos)*(xn-xPos)+(yn-yPos)*(yn-yPos)+(zn-zPos)*(zn-zPos);
	}


}
