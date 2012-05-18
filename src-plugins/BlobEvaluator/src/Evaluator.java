import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import tools.HighQualityRandom;



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
		
		
		
		try {
			File f= new File("/home/alex/Desktop/noEMCCD.txt");
			FileWriter fw= new FileWriter(f);
			
			File f2= new File("/home/alex/Desktop/EMCCD.txt");
			FileWriter fw2= new FileWriter(f2);
			
			File f3= new File("/home/alex/Desktop/EMCCDcorrection.txt");
			FileWriter fw3= new FileWriter(f3);
			
	
		
		//Random r= new SecureRandom();
		double i=600;
		double bc=0;
		
		Blob_Simulator bs= new Blob_Simulator();
		while(i<=1000){
			
			Random r= new HighQualityRandom((long)i);
	//		bc=Math.pow(10, i/4);
			System.out.println("                  next Experiment:"+bc);
			Img<UnsignedShortType> img=bs.makeImg(13, 13, 1000,6, 6, 0.64788, i,0, false, 300,r);
			
			
			
			Experiment e=new Experiment(img, 0.01, 5, 5, 0.64788, false, 0, 2, false, "M.L.GaussianTracking", "epxA");			

			
			String s= i+ "\t"+e.toString();
			fw.write(s);
			fw.flush();
			
			bs.applyEMCCD(img, 300,r);
			
		//	img=bs.makeImg(13, 13, 1000,6.5, 6.5, 0.64788, i,0, true, 300,r);
			
			Experiment e2=new Experiment(img, 0.01, 5, 5, 0.64788, false, 0, 2, false, "M.L.GaussianTracking", "epxA");			

			
			s= i+ "\t"+e2.toString();
			fw2.write(s);
			fw2.flush();
			
			
	//		Experiment e3=new Experiment(img, 0.01, 5, 5, 0.64788, false, 0, 2, false, "EMCCD-GaussianML", "epxB");			
	
			
	//		 s= i+ "\t"+e3.toString();
	//		fw3.write(s);
	//		fw3.flush();
				
			i+=100;
		}
		
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		


	}
	
	

}
