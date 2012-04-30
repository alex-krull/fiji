package blobTracking;


import ij.gui.EllipseRoi;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.TextRoi;

import java.awt.Color;
import java.awt.Font;
import java.text.DecimalFormat;

import net.imglib2.Cursor;
import net.imglib2.Interval;
import net.imglib2.img.Img;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;

import org.apache.commons.math.analysis.MultivariateRealFunction;

import tools.ImglibTools;
import frameWork.Model;
import frameWork.Trackable;

/**
 * @author alex
 *
 */
/**
 * @author alex
 *
 */
public class Blob extends Trackable implements MultivariateRealFunction {
	public double xPos;
	public double yPos;
	public double zPos;
	public double sigma;
	public double sigmaZ;
	public double pK=0.1;
	public double pKAkku; 
	public double inten=0;
	public int counter=0;
	public double maxSigma=2;
	public double minSigma=0.5;
	public double maxSigmaZ=2;
	public double minSigmaZ=0.5;
	public boolean autoSigma=false;
	public boolean autoSigmaZ=false;
	public double denom=0;
	public boolean coupled=false;
	public double newX=0;
	public double newY=0;
	public double newSig=0;
	public double newZ=0;
	public double newPK=0;
	
	
	public Img<FloatType> expectedValues=null;
	public IterableRandomAccessibleInterval <FloatType> expectedValuesRoi;

	/**
	 * Creates a new Blob
	 * 
	 * @param seqId the id of the Sequqnce the blob belongs to
	 * @param FrameId the frame number the blob is in
	 * @param x the x-position of the blob
	 * @param y the y-position of the blob
	 * @param z the z-position of the blob
	 * @param sig the standard deviation of the blob
	 * @param chan the channel the blob belongs to
	 */
	public Blob(int seqId, int FrameId, double x, double y, double z, double sig, int chan, 
			boolean autoS, double sigZ) {
		super(seqId, FrameId, chan);
		xPos = x;
		yPos = y;
		zPos = z;
		sigma = sig;
		sigmaZ = sigZ;
		this.autoSigma=autoS;
	}
	
	public double calcDenominator(Interval img, double px,double py,double pz,double ps,double psz){
		//denominator=ImglibTools.gaussIntegral(img.min(0)-0.5,img.min(1)-0.5,img.max(0)+0.5,img.max(1)+0.5,xPos,yPos,sigma );
		double akku=0;
		for(double i=0;i<this.expectedValues.dimension(2);i++){
			akku+=ImglibTools.gaussIntegral2dIn3d(img.min(0)-0.5,img.min(1)-0.5,img.max(0)+0.5,img.max(1)+0.5, i*Model.getInstance().getXyToZ(),
					px, py, pz, ps, psz);
		
		}
						
		return akku;
	}
	
	public double pXunderK(int x, int y, int z,
			double px, double py, double pz, double ps, double psz,
			double denominator, double xyToZ){
		//return ImglibTools.gaussPixelIntegral(x, y, xPos, yPos, sigma)/denominator;
		return ImglibTools.gaussPixelIntegral2dIn3d(x, y, z* xyToZ
				, px, py, pz, ps, psz)/denominator;
	}
	
	
	public double pXandK(int x, int y, int z,
			double px, double py, double pz, double ps, double psz,
			double denominator){
		return pXunderK(x,y,z, px, py, pz, ps, psz, denominator, Model.getInstance().getXyToZ())*pK;
		
	}
	
	public double localLogLikelihood(double px, double py, double pz, double ps, double psz){
		boolean isVolume= expectedValuesRoi.numDimensions()>2;
		double denominator= calcDenominator(expectedValuesRoi, px,py,pz,ps,psz);
	//	if(this.denominator<0.00000001) return 0;
		double result=0;
		Cursor<FloatType> cursor= expectedValuesRoi.cursor();	
		double xyToZ= Model.getInstance().getXyToZ();
    	while ( cursor.hasNext() )	{
    		
    		cursor.fwd();
    		double b=(cursor.get().get());
    		int xPos=cursor.getIntPosition(0);int yPos=cursor.getIntPosition(1);int zPos=0;
    		if(isVolume) zPos=cursor.getIntPosition(2);
    		
    		double tx=xPos;
    		double ty=yPos;
    		double dist= (tx-px)*(tx-px) + (ty-py)*(ty-py);
    //		System.out.println("dist:"+dist+ " (ps*3+1)*(ps*3+1):"+(ps*3+1)*(ps*3+1) + " ps:"+ps);
    //		if(dist>(ps*3+2)*(ps*3+2)) continue;
    		
    		double a=pXunderK(xPos, yPos, zPos,
    				px,py,pz,ps,psz,
    				denominator, xyToZ);  		
    		
    		if(a<0.00000001) continue;
    		result+=Math.log(a)*b;
    		
    	}
		return result;
	}
	
	
	@Override
	public void addShapeZ(Overlay ov, boolean selected, Color c, boolean drawNumbers){
		Font f=new Font(null,Font.PLAIN,8);
			
		Roi roi = new EllipseRoi(0.5+xPos + sigma * 2,0.5+ yPos,0.5+ xPos - sigma * 2,
				0.5+yPos, 1);
		
		roi.setStrokeColor(c);
		roi.setStrokeWidth(1);
		if(selected) roi.setStrokeWidth(4);
		ov.add(roi);
		
	if(drawNumbers){	
		TextRoi troi = new TextRoi((int)xPos,(int)yPos,Integer.toString(this.sequenceId));
		troi.setStrokeColor(c);
		troi.setStrokeWidth(1);
		ov.add(troi);
	}
	
	}
	
	
	
	
	
	@Override
	public void addShapeY(Overlay ov, boolean selected, Color c){
		
		
		Roi roi;
		if(sigma / sigmaZ<1)
			roi=new EllipseRoi(
				0.5+ xPos ,0.5*Model.getInstance().getXyToZ()+ zPos-2*sigmaZ,
				0.5+ xPos ,	0.5*Model.getInstance().getXyToZ()+zPos+2*sigmaZ
				, sigma / sigmaZ);
		
		else roi=new EllipseRoi(
				0.5+ xPos -2*sigma,0.5*Model.getInstance().getXyToZ()+ zPos,
				0.5+ xPos +2*sigma,	0.5*Model.getInstance().getXyToZ()+zPos
				, sigmaZ / sigma);
		
		
		roi.setStrokeColor(c);
		roi.setStrokeWidth(1);
		if(selected) roi.setStrokeWidth(4);
		ov.add(roi);
	}

	
	@Override
	public void addShapeX(Overlay ov, boolean selected, Color c){
		
		
		Roi roi;
		if(sigma / sigmaZ<1) roi= new EllipseRoi(
				0.5*Model.getInstance().getXyToZ()+zPos + sigmaZ * 2,0.5+ yPos,
				0.5*Model.getInstance().getXyToZ()+ zPos - sigmaZ * 2,
				0.5+yPos, sigma / sigmaZ);
		else  roi= new EllipseRoi(
				0.5*Model.getInstance().getXyToZ()+zPos ,0.5+ yPos+ sigma * 2,
				0.5*Model.getInstance().getXyToZ()+ zPos ,
				0.5+yPos- sigma * 2, sigmaZ / sigma);
			
		roi.setStrokeColor(c);
		roi.setStrokeWidth(1);
		if(selected) roi.setStrokeWidth(4);
		ov.add(roi);
	}
	
	
	

	@Override
	public double getDistanceTo(double x, double y, double z) {
		

		double xn= x;
		double yn= y;
		double zn= z*Model.getInstance().getXyToZ();
		if(x<0) xn=this.xPos;
		if(y<0) yn=this.yPos;
		if(z<0) zn=this.zPos;
		
		System.out.println("x:"+xn+ "  xPos:"+ xPos+"  "+ (xn-xPos)*(xn-xPos));
		System.out.println("y:"+yn+ "  yPos:"+ yPos+"  "+ (yn-yPos)*(yn-yPos));
		System.out.println("z:"+zn+ "  zPos:"+ zPos+"  "+ (zn-zPos)*(zn-zPos)
				);
		
		return (xn-xPos)*(xn-xPos)+
				(yn-yPos)*(yn-yPos)+
				(zn-zPos)*(zn-zPos);
	}
	
	@Override
	public String toString(){
		String result=
				"_____________________"+
				"frameID:"+this.frameId+
				"x:"+xPos+
				" y:"+yPos+
				" z:"+zPos+
				" pK:"+pK+"\n";
		
		return result;
	}

	@Override
	public double value(double[] position) {
		boolean isVolume=this.expectedValuesRoi.numDimensions()>2;
		counter++;
		//if(position[2]<0.5) return Double.MIN_VALUE;
	
		
		double px=position[0];
		double py=position[1];
		double pz=0;
		double ps=this.sigma;
		double psz=this.sigmaZ;
		if(!isVolume&& this.autoSigma) ps=Math.max(0.8,Math.min(3.0,Math.pow(position[2],0.5) ) );
		if(isVolume&& this.autoSigma){
			 pz=position[2];
			 ps=Math.max(0.8,Math.min(3.0,Math.pow(position[3],0.5) ) );
		}
		if(isVolume&& !this.autoSigma)
			pz=position[2];
		double value=this.localLogLikelihood(px,py,pz,ps,psz);
		
		return value;
		
	}

	
	@Override
	public String toSaveString() {
		DecimalFormat df = new DecimalFormat("#0.00000");

		String result;
		result= this.frameId + "\t"
				+ this.sequenceId + "\t" 
				+ df.format(this.xPos)+ "\t"
				+ df.format( this.yPos)+ "\t"
				+  df.format(this.zPos)+ "\t"
				+  df.format(this.sigma)+ "\t"
				+  df.format(this.sigmaZ)+"\t"
				+ df.format(this.maxSigma)+"\t"
				+ String.valueOf(!this.autoSigma);
		return result;
	}

}

