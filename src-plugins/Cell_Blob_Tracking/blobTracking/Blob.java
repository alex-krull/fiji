package blobTracking;


import java.awt.Color;
import java.awt.Font;

import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.EllipseRoi;
import ij.gui.TextRoi;

import frameWork.Trackable;
import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;


import org.apache.commons.math.analysis.MultivariateRealFunction;




import tools.ImglibTools;

public class Blob extends Trackable implements MultivariateRealFunction {
	public double xPos;
	public double yPos;
	public double zPos;
	public double sigma;
	public double sigmaZ;
	public double pK=0.1;
	public double pKAkku;
	
	
	public double denominator=0;
	public Img<FloatType> expectedValues=null;
	public IterableRandomAccessibleInterval <FloatType> expectedValuesRoi;

	public double calcDenominator(Interval img){
		denominator=ImglibTools.gaussIntegral((double)img.min(0)-0.5,(double)img.min(1)-0.5,(double)img.max(0)+0.5,(double) img.max(1)+0.5,xPos,yPos,sigma );
//		System.out.println("denominator :" +denominator );
		return denominator;
	}
	
	public double pXunderK(int x, int y, int z){
		
		
		return ImglibTools.gaussPixelIntegral(x, y, xPos, yPos, sigma)/denominator;
		
	}
	
	public double pXandK(int x, int y, int z){
		return pXunderK(x,y,z)*pK;
	}
	
	public double localLogLikelihood(){
		this.calcDenominator(expectedValuesRoi);
		double result=0;
		Cursor<FloatType> cursor= expectedValuesRoi.cursor();	
    	while ( cursor.hasNext() )	{
    		cursor.fwd();
    		double a=pXunderK(cursor.getIntPosition(0), cursor.getIntPosition(1),0 );
    		double b=(cursor.get().get());
    		
    		if(a<0.0000001) continue;
    		result+=Math.log(a)*b;
    	}
		return result;
	}
	
	
	public void addShapeZ(Overlay ov, boolean selected, Color c){
		Font f=new Font(null,Font.PLAIN,8);
		
		if(selected){
			Roi roiS=new EllipseRoi(0.5+xPos + sigma * 2,0.5+ yPos,0.5+ xPos - sigma * 2,
					0.5+yPos, 1);
			
			roiS.setStrokeColor(new Color(255,255,255,100));
			roiS.setStrokeWidth(5);
			ov.add(roiS);
		}
		
		Roi roi = new EllipseRoi(0.5+xPos + sigma * 2,0.5+ yPos,0.5+ xPos - sigma * 2,
				0.5+yPos, 1);
		
		roi.setStrokeColor(c);
		roi.setStrokeWidth(1);
		ov.add(roi);
		
		
		roi = new TextRoi((int)xPos,(int)yPos,Integer.toString(this.sequenceId),f);
		ov.add(roi);
	}
	
	public void addShapeY(Overlay ov, boolean selected, Color c){
		if(selected){
			Roi roiS=new EllipseRoi(xPos , zPos-2*sigmaZ, xPos ,
					zPos+2*sigmaZ, sigma / sigmaZ);		
			roiS.setStrokeColor(new Color(255,255,255,100));
			roiS.setStrokeWidth(5);
			ov.add(roiS);
		}
		
		Roi roi=new EllipseRoi(xPos , zPos-2*sigmaZ, xPos ,
				zPos+2*sigmaZ, sigma / sigmaZ);
		roi.setStrokeColor(c);
		roi.setStrokeWidth(1);
		ov.add(roi);
	}

	public void addShapeX(Overlay ov, boolean selected, Color c){
		if(selected){
			Roi roiS=new EllipseRoi(zPos + sigmaZ * 2, yPos, zPos - sigmaZ * 2,
					yPos, sigma / sigmaZ);
			
			roiS.setStrokeColor(new Color(255,255,255,100));
			roiS.setStrokeWidth(5);
			ov.add(roiS);
		}
		
		Roi roi = new EllipseRoi(zPos + sigmaZ * 2, yPos, zPos - sigmaZ * 2,
				yPos, sigma / sigmaZ);
		roi.setStrokeColor(c);
		roi.setStrokeWidth(1);
		ov.add(roi);
	}
	
	public Blob(int seqId, int FrameId, double x, double y, double z, double sig, int chan) {
		super(seqId, FrameId, chan);
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
	
	public String toString(){
		String result=
				"x:"+xPos+
				" y:"+yPos+
				" z:"+zPos+
				" pK:"+pK+"\n";
		return result;
	}

	@Override
	public synchronized double value(double[] position) {
		//if(position[2]<0) return Double.MIN_VALUE;
		double xOld=xPos;
		double yOld=yPos;
		double sigmaOld=sigma;
		
		xPos=position[0];
		yPos=position[1];
		sigma=Math.abs(position[2]);
		double value=this.localLogLikelihood();
		
		xPos=xOld;
		yPos=yOld;
		sigma=sigmaOld;
		
		return value;
		
	}
	


}
