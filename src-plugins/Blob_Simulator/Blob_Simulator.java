import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.util.Random;

import net.imglib2.Cursor;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.PoissonDistributionImpl;

import tools.ImglibTools;


public class Blob_Simulator implements PlugIn{

	@Override
	public void run(String arg0) {
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

		ImgFactory<UnsignedShortType> imgFactory = new ArrayImgFactory<UnsignedShortType>();
		long[] dims = {xSize,ySize,frames};
		Img <UnsignedShortType> image= imgFactory.create(dims, new UnsignedShortType());
		
			fillImage(image,xPos,yPos,sig,blobFlux,backFlux);
		
		//ImagePlus imp2= new ImagePlus();
		
		//ImageJFunctions.showUnsignedByte(image);
		ImagePlus imp= ImageJFunctions.wrap(image,"string");
		ImagePlus imp2 = imp.duplicate();
		imp2.show();
		//imp.show();
		
	
		

	}
	
	public void fillImage (Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux) {
		
		Cursor<UnsignedShortType> it=image.cursor();
		int nop=(int) image.dimension(0)*(int)image.dimension(1);
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );
		while(it.hasNext()){
			it.fwd();
			double mean=ImglibTools.gaussPixelIntegral(it.getIntPosition(0), it.getIntPosition(1), posX, posY, sig)*normBlobFlux +pixelBackFlux;
			
			
			int sample=0;
			mean=Math.max(0, mean);
			
			Random r= new Random();
			if(mean<0.00000000000001) sample=0;
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
			
			
			
			it.get().set(sample);
		}
	}

}
