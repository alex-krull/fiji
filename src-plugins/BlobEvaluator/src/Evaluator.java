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
		for(double i=780;i<=1000;i+=10){
			Img<UnsignedShortType> img=bs.makeImg(10, 10, 1000,5, 5, 1, i, 0, true, 300,r);
			Experiment e=new Experiment(img, 0.1, 5, 5, 1, false, 0, 2, false, "M.L.GaussianTracking", "epxA");			

			
			String s= i+ "\t"+e.toString();
			fw.write(s);
			fw.flush();
			
		
			
			
			Experiment e2=new Experiment(img, 0.1, 5, 5, 1, false, 0, 2, false, "EMCCD-GaussianML", "epxB");			
	
			
			 s= i+ "\t"+e2.toString();
			fw2.write(s);
			fw2.flush();
				
		}
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		


	}
	
	

}
