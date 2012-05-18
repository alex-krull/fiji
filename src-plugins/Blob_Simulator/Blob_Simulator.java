import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.io.FileWriter;
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
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

import tools.ErlangDist;
import tools.HighQualityRandom;
import tools.ImglibTools;



public class Blob_Simulator implements PlugIn{

	
	private final SortedMap<Integer,ErlangDist> erlangDists;
	
	
	public Blob_Simulator(){
		erlangDists= new TreeMap<Integer,ErlangDist>();
	}
	
	@Override
	public void run(String arg0) {
		doErlangExperiment(1,300);
		doErlangExperiment(2,300);
		doErlangExperiment(3,300);
		doErlangExperiment(4,300);
		
		
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
	
		double xPos=gd.getNextNumber();
		double yPos=gd.getNextNumber();
		double sig=gd.getNextNumber();
		double blobFlux=gd.getNextNumber();
		double backFlux=gd.getNextNumber();

	
		Img <UnsignedShortType> image= makeImg( xSize,  ySize,  frames, 
				 xPos,  yPos,  sig,
				 blobFlux,  backFlux, false, 300, new Random(1));
		
		
		ImagePlus impTemp= ImageJFunctions.wrap(image,"no EMCCD");
		ImagePlus impTemp2 = impTemp.duplicate();
		impTemp2.show();
			
		applyEMCCD(image, 300,new Random(1));
			
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
			double blobFlux, double backFlux, boolean emccd, double gain, Random r){
		ImgFactory<UnsignedShortType> imgFactory = new ArrayImgFactory<UnsignedShortType>();
		long[] dims = {xSize,ySize,frames};
		Img <UnsignedShortType> image= imgFactory.create(dims, new UnsignedShortType());
		fillImage(image,xPos,yPos,sig,blobFlux,backFlux,r);
		if(emccd) applyEMCCD(image, gain,r);
		return image;
	}
	
	public void applyEMCCD(Img <UnsignedShortType> img, double gain, Random rand){
		Cursor<UnsignedShortType> it=img.cursor();
	
		int c=0;
		while(it.hasNext()){
			it.fwd();
			int value= it.get().get();
			int emccdValue;
			
			if(value>0){
				GammaDistributionImpl gamma = new GammaDistributionImpl(value,gain);
			gamma.reseedRandomGenerator(rand.nextLong());
			try {
				emccdValue=(int)Math.ceil(gamma.sample());
			
			
	//		ErlangDist e= erlangDists.get(value);
	//		if(e==null ){
	//			e=new ErlangDist(value, gain, 0.001, false);
	//			if(erlangDists.size()<200)erlangDists.put(value, e);
				
//			}
	//		if(e!=null){
	//			emccdValue=e.drawOutput(rand.nextDouble());
	//		}else{
			
	//			emccdValue=emccd.draw(value, rand);
	//		}
			
			
			it.get().set((int)Math.min(Math.pow(2, 16)-1, emccdValue));
			
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			}else it.get().set(0);
			
			if(c%1000==0)System.out.println("pixel:"+ c+ "size:"+erlangDists.size());
			c++;
		}
	}
	
	public void doErlangExperiment(int inp, double g){
		GammaDistributionImpl e= new GammaDistributionImpl(inp,g);
		try {
			FileWriter fw= new FileWriter(new File("erlangDist"+inp+".txt") );
		
		Random rand= new HighQualityRandom(1);
		e.reseedRandomGenerator(rand.nextLong());
		
		
		int[] hist= new int[100000];
		for(int i=0;i<hist.length;i++){
			hist[i]=0;
		}
			
		for(int i=0;i<100000;i++){
			int sample = (int)Math.ceil(e.sample());
			hist[sample]++;
		}
		
		
		for(int i=0;i<hist.length;i++){
		
	//		String s= String.valueOf(i)+ "\t"+OtherTools.getErlangProp(inp, i, g)
	//				+ "\t"+OtherTools.getErlangProbAlternative(inp, i, g)+"\n";
			String s= String.valueOf(i)+ "\t"+ hist[i]+ "\n";
			fw.write(s);
			fw.flush();
			System.out.println(s);
		}
		
		
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	
	
	public void fillImage (Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux, Random r) {
		
		Cursor<UnsignedShortType> it=image.cursor();
		int nop=(int) image.dimension(0)*(int)image.dimension(1);
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );
		//Random r= new Random(1);
		
		
		while(it.hasNext()){
			it.fwd();
			double mean=ImglibTools.gaussPixelIntegral(it.getIntPosition(0), it.getIntPosition(1), posX, posY, sig)*normBlobFlux +pixelBackFlux;
			
			
			int sample=0;
			//mean=Math.max(0.0000000000001, mean);
			
			
			
			
			if(0>=mean) sample=0;
			
			
			else{
				
				PoissonDistributionImpl poissonDist= new PoissonDistributionImpl(mean);				
				poissonDist.reseedRandomGenerator(r.nextLong());
				
				try {
					
					sample=poissonDist.sample();
				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			
		//	System.out.println("xPos: "+ posX+ "yPos: "+ posY+
		//			" x:"+ it.getIntPosition(0)+ " y:"+ it.getIntPosition(1)+ " mean:"+mean+ " sample:"+ sample);
			
			it.get().set(sample);
		}
	}

}
