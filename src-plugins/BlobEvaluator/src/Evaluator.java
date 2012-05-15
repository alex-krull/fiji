import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;



public class Evaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
	/*	
		File fi= new File("/home/alex/Desktop/bessel.txt");
		try {
			FileWriter fwr= new FileWriter(fi);
			for(double i=0;i<10;i+=0.01){
				fwr.write(i+ "\t"+ OtherTools.bessi0(i)+ "\t"+OtherTools.bessi1(i)+ "\n");
				fwr.flush();
			}
			
		return;	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	*/	
		
		
		System.out.println("creating Model...");
		Blob_Simulator bs= new Blob_Simulator();
		
		
		try {
			File f= new File("/home/alex/Desktop/noEMCCD.txt");
			FileWriter fw= new FileWriter(f);
			
			File f2= new File("/home/alex/Desktop/EMCCD.txt");
			FileWriter fw2= new FileWriter(f2);
		Random r= new SecureRandom();
		for(double i=10;i<=500;i+=10){
			Img<UnsignedShortType> img=bs.makeImg(10, 10, 500,5, 5, 1, i, 0, true, 300,r);
			Experiment e=new Experiment(img, 0.1, 5, 5, 1, false, 0, 2, false, "M.L.GaussianTracking", "epxA");			
			double meanX=e.getMeanX();
			double meanY=e.getMeanY();
			double stdX=e.getStdX(meanX);
			double stdY=e.getStdY(meanY);
			
			String s= i+ "\t"+meanX+"\t"+ meanY+"\t"+ stdX+"\t"+ stdY+ "\n";
			fw.write(s);
			fw.flush();
			
		
			
			
			Experiment e2=new Experiment(img, 0.1, 5, 5, 1, false, 0, 2, false, "EMCCD-GaussianML", "epxB");			
			double meanX2=e2.getMeanX();
			double meanY2=e2.getMeanY();
			double stdX2=e2.getStdX(meanX2);
			double stdY2=e2.getStdY(meanY2);
			
			String s2= i+ "\t"+meanX2+"\t"+ meanY2+"\t"+ stdX2+"\t"+ stdY2+ "\n";
			fw2.write(s2);
			fw2.flush();
				
		}
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		


	}
	
	

}
