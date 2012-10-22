import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;
import java.util.SortedMap;
import java.util.TreeMap;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.numeric.integer.UnsignedShortType;

import org.apache.commons.math.MathException;
import org.apache.commons.math.distribution.GammaDistributionImpl;
import org.apache.commons.math.distribution.PoissonDistributionImpl;
import org.apache.commons.math.linear.Array2DRowRealMatrix;
import org.apache.commons.math.linear.OpenMapRealMatrix;
import org.apache.commons.math.linear.RealMatrix;
import org.apache.commons.math.special.Erf;

import tools.ErlangDist;
import tools.HighQualityRandom;
import tools.ImglibTools;
import tools.OtherTools;

import cern.jet.math.Bessel;
import cern.jet.random.engine.MersenneTwister; 
import cern.jet.random.Poisson; 
import cern.jet.random.Gamma; 


public class Blob_Simulator implements PlugIn{

	private MersenneTwister mT= new MersenneTwister(42);
	private final SortedMap<Integer,ErlangDist> erlangDists;
	
	
	public Blob_Simulator(){
		erlangDists= new TreeMap<Integer,ErlangDist>();
	}
	
	@Override
	public void run(String arg0) {
	double GAIN=50;
	//	doErlangExperiment(1,300);
	//	doErlangExperiment(2,300);
	//	doErlangExperiment(3,300);
	//	doErlangExperiment(4,300);
		
		
		
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
				 blobFlux,  backFlux, false, GAIN, new Random(1));
		
		
		ImagePlus impTemp= ImageJFunctions.wrap(image,"no EMCCD");
		ImagePlus impTemp2 = impTemp.duplicate();
		impTemp2.show();
			
		applyEMCCD(image, GAIN, new HighQualityRandom(1));
			
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
		if(emccd) applyEMCCD(image, gain, r);
		return image;
	}
	
	public void applyEMCCD(Img <UnsignedShortType> img, double gain, Random rand){
		Cursor<UnsignedShortType> it=img.cursor();
		Gamma g= new Gamma( 1.0,1.0, mT);
	//	rand= new  HighQualityRandom(System.currentTimeMillis());
	//	Random rand= new SecureRandom();
		boolean erl=false;
		int satCount=0;
		int i=0;
		while(it.hasNext()){
		it.fwd();
			int value= it.get().get();
			
			
	if(erl){
			ErlangDist e= erlangDists.get(value);
			if(e==null){
				e=new ErlangDist(value, gain, 0.00001);
				erlangDists.put(value, e);
			}
			if(value!=0)it.get().set(e.drawOutput(rand.nextDouble()));
			else it.get().set(0);
	}else{
			int sample=0;		
			if(value>0){
				
				sample= (int)g.nextDouble((double) value,1.0/gain)+1;
	/*			
				try {
					GammaDistributionImpl erl= new GammaDistributionImpl( value, gain);
					erl.reseedRandomGenerator(rand.nextLong());
					sample=((int) erl.sample())+1;

				} catch (MathException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				*/
			}
			
		if(sample>=(int)Math.pow(2, 16)-1){
			satCount++;
			System.out.println("SATCOUNT:"+satCount);
		}
		
  		sample= Math.min(sample, (int)Math.pow(2, 16)-1);
			it.get().set(sample);
	}
		
		if(i%1000==0)	System.out.println("pixel:"+ i);
			i++;
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
	
	public Array2DRowRealMatrix getFischerMatrixEMCCD(Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux,double gain) {

		Array2DRowRealMatrix fisher = new Array2DRowRealMatrix(4, 4);	//the result
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				fisher.setEntry(i, j, 0);
		
		RandomAccess <UnsignedShortType> ra =image.randomAccess();
		double sq= Math.sqrt(2);
		double temp=flux/(Math.sqrt(2*Math.PI)*sig);
		double nop=(int) image.dimension(0)*(int)image.dimension(1);
		
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );

			
		double[][] dxSum= new double [(int)image.dimension(0)][(int)image.dimension(1)];	//auxiliary variables
		double[][] dySum= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] diSum= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dbSum= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		
		double[][] dxxSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dxySumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dxiSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dxbSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		
		double[][] dyySumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dyiSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dybSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		
		double[][] diiSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		double[][] dibSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		
		double[][] dbbSumSq= new double [(int)image.dimension(0)][(int)image.dimension(1)];
		
			
			for(int y=0;y<image.dimension(1);y++)
					for(int x=0;x<image.dimension(0);x++){
			
						double mean		// the expected photon count E_i
						=ImglibTools.gaussPixelIntegral((int)x, (int)y, posX, posY, sig)*normBlobFlux +pixelBackFlux;
													
						dxSum[x][y]=0;	
						dySum[x][y]=0;
						diSum[x][y]=0;
						dbSum[x][y]=0;
						
						 dxxSumSq[x][y]=0;
						 dxySumSq[x][y]=0;
						 dxiSumSq[x][y]=0;
						 dxbSumSq[x][y]=0;
							
						 dyySumSq[x][y]=0;
						 dyiSumSq[x][y]=0;
						 dybSumSq[x][y]=0;
						
						 diiSumSq[x][y]=0;
						 dibSumSq[x][y]=0;
						
						 dbbSumSq[x][y]=0;
						
						
						double dx=temp* (Math.exp( -Math.pow((x-posX-0.5),2)/(2*sig*sig) )		//derivative of E_i in direction x-position
								- Math.exp( -Math.pow((x-posX+0.5),2)/(2*sig*sig) )
								)*(0.5*Erf.erf( (y-posY+0.5)/(sq*sig) )-0.5*Erf.erf( (y-posY-0.5)/(sq*sig) ));
						
						double dy=temp* (Math.exp( -Math.pow((y-posY-0.5),2)/(2*sig*sig) )	//derivative of E_i in direction y-position
								- Math.exp( -Math.pow((y-posY+0.5),2)/(2*sig*sig) )
								)*(0.5*Erf.erf( (x-posX+0.5)/(sq*sig) )-0.5*Erf.erf( (x-posX-0.5)/(sq*sig) ));
						double di=ImglibTools.gaussPixelIntegral((int)x, (int)y, posX, posY, sig);	//derivative of E_i in direction blob intensity
						double db=1;	//derivative of E_i in direction background intensity
						
						
						double qA;     			//the function q
						double qdeA;			//the derivative of log(q) in the direction of the mean E_i
						
						
				for (double s = 0; s < Math.pow(2, 16); s++) {				
					
					double sqTemp = Math.sqrt(mean * s / gain);

					if (s == 0) {						//implementation of dirac delta
						qA = Math.exp(-mean);
						qdeA = -1;

					} else {
						qA = Math.sqrt(mean / (gain * s))
								* Math.exp(-s / gain - mean)
								* Bessel.i1(2 * sqTemp);
						qdeA = Math.sqrt(s / (gain * mean))
								* (Bessel.i0e(2 * sqTemp) / Bessel.i1e(2 * sqTemp)) - 1;

					}
				
					dxSum[x][y] += qA * dx * qdeA;
					dySum[x][y] += qA * dy * qdeA;
					diSum[x][y] += qA * di * qdeA;
					dbSum[x][y] += qA * db * qdeA;

					dxxSumSq[x][y] += qA * dx * qdeA * dx * qdeA;
					dxySumSq[x][y] += qA * dx * qdeA * dy * qdeA;
					dxiSumSq[x][y] += qA * dx * qdeA * di * qdeA;
					dxbSumSq[x][y] += qA * dx * qdeA * db * qdeA;

					dyySumSq[x][y] += qA * dy * qdeA * dy * qdeA;
					dyiSumSq[x][y] += qA * dy * qdeA * di * qdeA;
					dybSumSq[x][y] += qA * dy * qdeA * db * qdeA;

					diiSumSq[x][y] += qA * di * qdeA * di * qdeA;
					dibSumSq[x][y] += qA * di * qdeA * db * qdeA;

					dbbSumSq[x][y] += qA * db * qdeA * db * qdeA;

				}

			}
			
		
			for(int y=0;y<image.dimension(1);y++)				
				for(int x=0;x<image.dimension(0);x++){
					
										
					for(int y_=0;y_<image.dimension(1);y_++)				//loop in loop could be done better
						for(int x_=0;x_<image.dimension(0);x_++){
							
							if(x!=x_||y!=y_){								// k != k'
							fisher.setEntry(0, 0, fisher.getEntry(0, 0)+(dxSum[x][y]*dxSum[x_][y_]));	
							fisher.setEntry(0, 1, fisher.getEntry(0, 1)+(dxSum[x][y]*dySum[x_][y_]) );
							fisher.setEntry(0, 2, fisher.getEntry(0, 2)+(dxSum[x][y]*diSum[x_][y_]) );
							fisher.setEntry(0, 3, fisher.getEntry(0, 3)+(dxSum[x][y]*dxSum[x_][y_]) );
							
							fisher.setEntry(1, 0, fisher.getEntry(1, 0)+(dySum[x][y]*dxSum[x_][y_]));		
							fisher.setEntry(1, 1, fisher.getEntry(1, 1)+(dySum[x][y]*dySum[x_][y_]));	
							fisher.setEntry(1, 2, fisher.getEntry(1, 2)+(dySum[x][y]*diSum[x_][y_]));	
							fisher.setEntry(1, 3, fisher.getEntry(1, 3)+(dySum[x][y]*dbSum[x_][y_]));		
							
							fisher.setEntry(2, 0, fisher.getEntry(2, 0)+(diSum[x][y]*dxSum[x_][y_]));	
							fisher.setEntry(2, 1, fisher.getEntry(2, 1)+(diSum[x][y]*dySum[x_][y_]));	
							fisher.setEntry(2, 2, fisher.getEntry(2, 2)+(diSum[x][y]*diSum[x_][y_]));	
							fisher.setEntry(2, 3, fisher.getEntry(2, 3)+(diSum[x][y]*dbSum[x_][y_]));	
							
							fisher.setEntry(3, 0, fisher.getEntry(3, 0)+(dbSum[x][y]*dxSum[x_][y_]));	
							fisher.setEntry(3, 1, fisher.getEntry(3, 1)+(dbSum[x][y]*dySum[x_][y_]));		
							fisher.setEntry(3, 2, fisher.getEntry(3, 2)+(dbSum[x][y]*diSum[x_][y_]));	
							fisher.setEntry(3, 3, fisher.getEntry(3, 3)+(dbSum[x][y]*dbSum[x_][y_]));
							
							}else{			//add last term
								fisher.setEntry(0, 0, fisher.getEntry(0, 0)+(dxxSumSq[x][y]));	
								fisher.setEntry(0, 1, fisher.getEntry(0, 1)+(dxySumSq[x][y]));
								fisher.setEntry(0, 2, fisher.getEntry(0, 2)+(dxiSumSq[x][y]));
								fisher.setEntry(0, 3, fisher.getEntry(0, 3)+(dxbSumSq[x][y]));
								
								fisher.setEntry(1, 0, fisher.getEntry(1, 0)+(dxySumSq[x][y]));		
								fisher.setEntry(1, 1, fisher.getEntry(1, 1)+(dyySumSq[x][y]));	
								fisher.setEntry(1, 2, fisher.getEntry(1, 2)+(dyiSumSq[x][y]));	
								fisher.setEntry(1, 3, fisher.getEntry(1, 3)+(dybSumSq[x][y]));		
								
								fisher.setEntry(2, 0, fisher.getEntry(2, 0)+(dxiSumSq[x][y]));	
								fisher.setEntry(2, 1, fisher.getEntry(2, 1)+(dyiSumSq[x][y]));	
								fisher.setEntry(2, 2, fisher.getEntry(2, 2)+(diiSumSq[x][y]));	
								fisher.setEntry(2, 3, fisher.getEntry(2, 3)+(dibSumSq[x][y]));	
								
								fisher.setEntry(3, 0, fisher.getEntry(3, 0)+(dxbSumSq[x][y]));
								fisher.setEntry(3, 1, fisher.getEntry(3, 1)+(dybSumSq[x][y]));	
								fisher.setEntry(3, 2, fisher.getEntry(3, 2)+(dibSumSq[x][y]));
								fisher.setEntry(3, 3, fisher.getEntry(3, 3)+(dbbSumSq[x][y]));	
							}
							
						}
					
						
			
			}
			
			
	
		
	return fisher;
	
	}
	
	
	public Array2DRowRealMatrix getFischerMatrix(Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux) {
		Array2DRowRealMatrix fischer = new Array2DRowRealMatrix(4, 4);
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				fischer.setEntry(i, j, 0);
		
		
		int nop=(int) image.dimension(0)*(int)image.dimension(1);
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );
		//Random r= new Random(1);
		
		
		double temp=flux/(Math.sqrt(2*Math.PI)*sig);
		
		double sq= Math.sqrt(2);
		for(double x=0;x<image.dimension(0);x++)
			for(double y=0;y<image.dimension(1);y++){
			
						
			
			double mean=ImglibTools.gaussPixelIntegral((int)x, (int)y, posX, posY, sig)*normBlobFlux +pixelBackFlux;

			
			
			double dx=temp* (Math.exp( -Math.pow((x-posX-0.5),2)/(2*sig*sig) )
					- Math.exp( -Math.pow((x-posX+0.5),2)/(2*sig*sig) )
					)*(0.5*Erf.erf( (y-posY+0.5)/(sq*sig) )-0.5*Erf.erf( (y-posY-0.5)/(sq*sig) ));
			
			double dy=temp* (Math.exp( -Math.pow((y-posY-0.5),2)/(2*sig*sig) )
					- Math.exp( -Math.pow((y-posY+0.5),2)/(2*sig*sig) )
					)*(0.5*Erf.erf( (x-posX+0.5)/(sq*sig) )-0.5*Erf.erf( (x-posX-0.5)/(sq*sig) ));
			double di=ImglibTools.gaussPixelIntegral((int)x, (int)y, posX, posY, sig);
			double db=1;
			
	//		mean=Math.max(0.0000000000001, mean);
			if(mean<0.00000001) continue;
			
			fischer.setEntry(0, 0, fischer.getEntry(0, 0)+(dx*dx/mean));	
			fischer.setEntry(0, 1, fischer.getEntry(0, 1)+(dx*dy/mean));
			fischer.setEntry(0, 2, fischer.getEntry(0, 2)+(dx*di/mean));
			fischer.setEntry(0, 3, fischer.getEntry(0, 3)+(dx*db/mean));
			
			fischer.setEntry(1, 0, fischer.getEntry(1, 0)+(dy*dx/mean));	
			fischer.setEntry(1, 1, fischer.getEntry(1, 1)+(dy*dy/mean));
			fischer.setEntry(1, 2, fischer.getEntry(1, 2)+(dy*di/mean));
			fischer.setEntry(1, 3, fischer.getEntry(1, 3)+(dy*db/mean));
			
			fischer.setEntry(2, 0, fischer.getEntry(2, 0)+(di*dx/mean));	
			fischer.setEntry(2, 1, fischer.getEntry(2, 1)+(di*dy/mean));
			fischer.setEntry(2, 2, fischer.getEntry(2, 2)+(di*di/mean));
			fischer.setEntry(2, 3, fischer.getEntry(2, 3)+(di*db/mean));
			
			fischer.setEntry(3, 0, fischer.getEntry(3, 0)+(db*dx/mean));	
			fischer.setEntry(3, 1, fischer.getEntry(3, 1)+(db*dy/mean));
			fischer.setEntry(3, 2, fischer.getEntry(3, 2)+(db*di/mean));
			fischer.setEntry(3, 3, fischer.getEntry(3, 3)+(db*db/mean));
			
			
			
			
	
			
		}
		
		return fischer;
	
	}
	
	public void fillImage (Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux, Random r) {
		
		
		Cursor<UnsignedShortType> it=image.cursor();
		int nop=(int) image.dimension(0)*(int)image.dimension(1);
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );
		//Random r= new Random(1);
		Poisson p= new Poisson(2, mT) ;
		
		while(it.hasNext()){
			it.fwd();
			double mean=ImglibTools.gaussPixelIntegral(it.getIntPosition(0), it.getIntPosition(1), posX, posY, sig)*normBlobFlux +pixelBackFlux;
			
			
			int sample=0;
//			mean=Math.max(0.0000000000001, mean);		
			
//			sample=PoissonDraw( mean,  r);
			
			
			sample= p.nextInt(mean);
			sample= Math.min(sample, (int)Math.pow(2, 16)-1);
			
			it.get().set(sample);
		}
	}

	public int PoissonDraw(double mean, Random r){
		PoissonDistributionImpl poissonDist= new PoissonDistributionImpl(mean);				
		poissonDist.reseedRandomGenerator(r.nextLong());
		
		int sample=0;
		if(0>=mean) sample=0;	
		else{
			try {
				
				sample=poissonDist.sample();
			} catch (MathException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		sample= Math.min(sample, (int)Math.pow(2, 16)-1);
		return sample;
	}
	
	public  Array2DRowRealMatrix fischerFromSimulation(Img <UnsignedShortType> image , double posX, double posY, double sig, double flux, double backFlux, double gain){
		Array2DRowRealMatrix fisher = new Array2DRowRealMatrix(4, 4);
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				fisher.setEntry(i, j, 0);
		
		RandomAccess <UnsignedShortType> ra =image.randomAccess();
		double sq= Math.sqrt(2);
		double temp=flux/(Math.sqrt(2*Math.PI)*sig);
		int nop=(int) image.dimension(0)*(int)image.dimension(1);
		double pixelBackFlux=backFlux/nop;
		double normBlobFlux		=flux/ImglibTools.gaussIntegral(image.min(0)-0.5,image.min(1)-0.5,image.max(0)+0.5,image.max(1)+0.5,posX,posY,sig );

		
		
		for(int z=0;z<image.dimension(2);z++){
			
			
			double dx=0;
			double dy=0;
			double di=0;
			double db=0;
			for(int y=0;y<image.dimension(1);y++)
					for(int x=0;x<image.dimension(0);x++){
						
						double mean=ImglibTools.gaussPixelIntegral((int)x, (int)y, posX, posY, sig)*normBlobFlux +pixelBackFlux;
						
						double q;
						double qde;
						double qdede;
						int []pos={(int)x,(int)y,(int)z};
						ra.setPosition(pos);
						double s=ra.get().get();
						double sqTemp=Math.sqrt(mean*s/gain);
					//	if(mean<1e-5)continue;
						
						if(s==0){
							q= Math.exp(-mean);
							qde=-1;
							
						
						}else{
							
							qde=Math.sqrt(s/(gain*mean))* (Bessel.i0e(2*sqTemp)/Bessel.i1e(2*sqTemp)) -1;
						
						}
						//qde*=mean;
						
						
						dx+=qde*temp* (Math.exp( -Math.pow((x-posX-0.5),2)/(2*sig*sig) )
								- Math.exp( -Math.pow((x-posX+0.5),2)/(2*sig*sig) )
								)*(0.5*Erf.erf( (y-posY+0.5)/(sq*sig) )-0.5*Erf.erf( (y-posY-0.5)/(sq*sig) ));
						
						dy+=qde*temp* (Math.exp( -Math.pow((y-posY-0.5),2)/(2*sig*sig) )
								- Math.exp( -Math.pow((y-posY+0.5),2)/(2*sig*sig) )
								)*(0.5*Erf.erf( (x-posX+0.5)/(sq*sig) )-0.5*Erf.erf( (x-posX-0.5)/(sq*sig) ));
						di+=qde*ImglibTools.gaussPixelIntegral((int)x, (int)y, posX, posY, sig);
						db+=qde*1;
						
		//				System.out.println("s:"+s+ " dx"+dx);
					}
			
			fisher.setEntry(0, 0, fisher.getEntry(0, 0)+(dx*dx));	
			fisher.setEntry(0, 1, fisher.getEntry(0, 1)+(dx*dy));
			fisher.setEntry(0, 2, fisher.getEntry(0, 2)+(dx*di));
			fisher.setEntry(0, 3, fisher.getEntry(0, 3)+(dx*db));
			
			fisher.setEntry(1, 0, fisher.getEntry(1, 0)+(dy*dx));	
			fisher.setEntry(1, 1, fisher.getEntry(1, 1)+(dy*dy));
			fisher.setEntry(1, 2, fisher.getEntry(1, 2)+(dy*di));
			fisher.setEntry(1, 3, fisher.getEntry(1, 3)+(dy*db));
			
			fisher.setEntry(2, 0, fisher.getEntry(2, 0)+(di*dx));	
			fisher.setEntry(2, 1, fisher.getEntry(2, 1)+(di*dy));
			fisher.setEntry(2, 2, fisher.getEntry(2, 2)+(di*di));
			fisher.setEntry(2, 3, fisher.getEntry(2, 3)+(di*db));
			
			fisher.setEntry(3, 0, fisher.getEntry(3, 0)+(db*dx));	
			fisher.setEntry(3, 1, fisher.getEntry(3, 1)+(db*dy));
			fisher.setEntry(3, 2, fisher.getEntry(3, 2)+(db*di));
			fisher.setEntry(3, 3, fisher.getEntry(3, 3)+(db*db));
			
			
			
		}
		for(int i=0;i<4;i++)
			for(int j=0;j<4;j++)
				fisher.setEntry(i, j,fisher.getEntry(i,j) /(double)image.dimension(2) );
		
	return fisher;
	}
	
}
