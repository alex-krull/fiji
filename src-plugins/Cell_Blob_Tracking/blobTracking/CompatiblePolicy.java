package blobTracking;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public class CompatiblePolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >
extends MaximumLikelihoodBlobPolicy<IT> {
	
	@Override
	public Blob loadTrackableFromString(String s, int sessionId) {

	//	System.out.println(s);
	
		String[] values=s.split("\t");
	//	System.out.println(values.length);
		
	//	System.out.println(values[1]);
	//	System.out.println(values[2]);
	//	System.out.println(values[0]);
		
		
		
		int sId= Integer.valueOf(values[0]);
		int fNum= Integer.valueOf(values[1]); 
		double x= Double.valueOf(values[2]);
		double y= Double.valueOf(values[3]);
		double z= Double.valueOf(values[4]);
		double sigma= Double.valueOf(values[5]);
		double sigmaZ= Double.valueOf(values[6]);
		
		Blob nB=new Blob(sId, fNum, x, y, z, sigma, sessionId, false, sigma*3, sigma+1);
		
		return nB;
	}
	
	
	@Override
	public boolean isHidden(){
		return true;
	}
}
