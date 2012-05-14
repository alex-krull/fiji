import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

import tools.ErlangDist;
import tools.ImglibTools;
import tools.OtherTools;



public class Blob_Simulator implements PlugIn{

	private SortedMap<Integer,ErlangDist> erlangDists;
	
	@Override
	public void run(String arg0) {
	//	doErlangExperiment(1,300);
	//	doErlangExperiment(2,300);
	//	doErlangExperiment(3,300);
	//	doErlangExperiment(4,300);
		
		erlangDists= new TreeMap<Integer,ErlangDist>();
		
		GenericDialog gd = new GenericDialog("blob simulator");
		gd.addNumericField("x-Size", 100, 0);
		gd.addNumericField("y-Size", 100, 0);
		gd.addNumericField("frames", 100, 0);
		
		gd.addNumericField("x-position", 50, 0);
		gd.addNumericField("y-position", 50, 0);
		gd.addNumericField("sigma", 2, 0);
		gd.addNumericField("flux of blob", 100, 0);
		gd.addNumericField("flux of background", 100, 0);
		
		gd.showDialog();
		
		
		if (gd.wasCanceled()) {
		IJ.error("PlugIn canceled!");
		return;
		}

		
		int xSize= (int)gd.getNextNumber();
		int ySize= (int)gd.getNextNumber();
		int frames= (int)gd.getNextNumber();
	
		double xPos=(int)gd.getNextNumber();
		double yPos=(int)gd.getNextNumber();
		double sig=(int)gd.getNextNumber();
		double blobFlux=gd.getNextNumber();
		double backFlux=gd.getNextNumber();

	
		Img <UnsignedShortType> image= makeImg( xSize,  ySize,  frames, 
				 xPos,  yPos,  sig,
				 blobFlux,  backFlux, false, 300);
		
		
		ImagePlus impTemp= ImageJFunctions.wrap(image,"no EMCCD");
		ImagePlus impTemp2 = impTemp.duplicate();
		impTemp2.show();
			
		applyEMCCD(image, 300);
			
		//ImagePlus imp2= new ImagePlus();
		
		//ImageJFunctions.showUnsignedByte(image);
		ImagePlus imp= ImageJFunctions.wrap(image,"EMCCD");
		ImagePlus imp2 = imp.duplicate();
		imp2.show();
		//imp.show();
		
		
		
		
		
	//	doErlangExperiment(2,300);
//		doErlangExperiment(3,300);
//		doErlangExperiment(4,300);
	}
	public Img <UnsignedShortType> makeImg(int xSize, int ySize, int frames, 
			double xPos, double yPos, double sig,
			double blobFlux, double backFlux, boolean emccd, double gain){
		ImgFactory<UnsignedShortType> imgFactory = new ArrayImgFactory<UnsignedShortType>();
		long[] dims = {xSize,ySize,frames};
		Img <UnsignedShortType> image= imgFactory.create(dims, new UnsignedShortType());
		fillImage(image,xPos,yPos,sig,blobFlux,backFlux);
		if(emccd) applyEMCCD(image, gain);
		return image;
	}
	
	public void applyEMCCD(Img <UnsignedShortType> img, double gain){
		Cursor<UnsignedShortType> it=img.cursor();
		Random rand= new Random(1);
		
		while(it.hasNext()){
			it.fwd();
			int value= it.get().get();
			ErlangDist e= erlangDists.get(value);
			if(e==null){
				e=new ErlangDist(value, gain, 0.001);
				erlangDists.put(value, e);
			}
			it.get().set(e.drawOutput(rand.nextDouble()));
			
		}
	}
	
	public void doErlangExperiment(int inp, double g){
		ErlangDist e= new ErlangDist(inp, g, 0.0001);
		try {
			FileWriter fw= new FileWriter(new File("erlangDist"+inp+".txt") );
		
		Random rand= new Random(1);
		
		int[] hist= new int[e.getLastPossibleOutput()+1];
		for(int i=0;i<hist.length;i++){
			hist[i]=0;
		}
			
	//	for(int i=0;i<100000;i++){
	//		double rv= rand.nextDouble();
	//		hist[e.drawOutput(rv)]++;
	//	}
		
		
	//	for(int i=0;i<hist.length;i++){
			for(int i=0;i<20000;i++){	
			String s= String.valueOf(i)+ "\t"+OtherTools.getErlangProp(inp, i, g)
					+ "\t"+OtherTools.getErlangProbAlternative(inp, i, g)+"\n";
		//	String s= String.valueOf(i)+ "\t"+ hist[i]+ "\n";
			fw.write(s);
			fw.flush();
			System.out.println(s);
		}
		
		
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	
	public void fillImage (Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux) {
		
		Cursor<UnsignedShortType> it=image.cursor();
		int nop=(int) image.dimension(0)*(int)image.dimension(1);
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );
		Random r= new Random(1);
		
		
		while(it.hasNext()){
			it.fwd();
			double mean=ImglibTools.gaussPixelIntegral(it.getIntPosition(0), it.getIntPosition(1), posX, posY, sig)*normBlobFlux +pixelBackFlux;
			
			
			int sample=0;
			mean=Math.max(0, mean);
			
			
			PoissonDistributionImpl poissonDist= new PoissonDistributionImpl(mean);				
			poissonDist.reseedRandomGenerator(r.nextLong());
			
			if(0>=mean) sample=0;
			
			
			else{
				
				
				
				try {
					
					sample=poissonDist.sample();
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
			
			it.get().set(sample);
		}
	}

}
