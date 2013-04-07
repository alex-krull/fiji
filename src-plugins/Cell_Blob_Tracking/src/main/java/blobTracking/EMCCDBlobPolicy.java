/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexandar Krull
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Contributors:
 * 	Alexander Krull (Alexander.Krull@tu-dresden.de)
 *     Damien Ramunno-Johnson (GUI)
 *******************************************************************************/
package blobTracking;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import net.imglib2.Cursor;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.img.Img;
import net.imglib2.img.ImgFactory;
import net.imglib2.img.array.ArrayImgFactory;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.IntegerType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;

import org.apache.commons.math.distribution.PoissonDistributionImpl;

import cern.jet.math.Bessel;

import sun.tools.tree.ThisExpression;
import tools.ErlangDist;
import tools.ImglibTools;
import tools.OtherTools;
import frameWork.Model;
import frameWork.MovieFrame;
import frameWork.Session;


public class EMCCDBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends MaximumLikelihoodBlobPolicy<IT>


{
	protected static double  GAIN=300;

	protected static double  PAG=1;



	public EMCCDBlobPolicy(){

	}

	@Override
	public String getTypeName() {	
		return "EMCCD-GaussianML";
	}

	@Override
	public void optimizeFrame(boolean alternateMethod, List<Blob> trackables,
			MovieFrame<IT> movieFrame, double qualityT,
			Session<Blob, IT> session) {

		IterableRandomAccessibleInterval<IT> iFrame= makeIterableFrame( movieFrame.getFrameView(),  trackables);

		for(Blob b: trackables){

			b.denom=b.calcDenominator(iFrame, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);

		}





		BlobSession<IT> bSession=(BlobSession<IT>) session;

		ImgFactory<FloatType> imgFactory = new ArrayImgFactory<FloatType>();
		Img<FloatType> expectedValues=imgFactory.create(movieFrame.getFrameView(), new FloatType());


		int maxIterations=30;


		double totalInten=0;

		Cursor<IT> c = iFrame.cursor();
		while(c.hasNext()){
			c.fwd();
			totalInten+=c.get().getRealDouble();
		}
		totalInten/=GAIN;


		super.numOfPixelsUsed=ImglibTools.getNumOfPixels(iFrame);



		List<Blob> refBlobs=new ArrayList<Blob>();
		double energyOld=1;
		double energy=1;
		double change=0;
		for(int i=0;i<maxIterations;i++){

			iFrame= makeIterableFrame( movieFrame.getFrameView(),  trackables);

			refBlobs.clear();
			for(Blob b: trackables){
				Blob cb= this.copy(b);
				refBlobs.add(cb);

			}

			Model.getInstance().depositMsg("E-step");
			Model.getInstance().makeChangesPublic();

			totalInten= doEstep(expectedValues, iFrame, trackables, totalInten);

			Model.getInstance().depositMsg("M-step");
			Model.getInstance().makeChangesPublic();


			doMstep(alternateMethod, trackables, expectedValues, qualityT, session);
			energyOld=energy;
			energy=this.getLogLikelihood(totalInten, trackables, iFrame);

			change=0;
			int index=0;

			change=Math.abs(energyOld-energy);

			if(change<qualityT)
				break;


		}


	} 

	private double doEstep(Img<FloatType> expectedValues, IterableRandomAccessibleInterval<IT> image,
			List<Blob> blobs, double totalInten){

		double newTotalInt=0;
		Cursor<IT> cursor =image.cursor();

		RandomAccess<FloatType> ra= expectedValues.randomAccess();
		int offSet=Model.getInstance().getIntensityOffset();
		PoissonDistributionImpl poissonDist=null;


		while(cursor.hasNext()){






			ra.setPosition(cursor);
			int value=(int) (((double)Math.max(0, (cursor.get()).getRealDouble()-offSet))*PAG);



			int x= ra.getIntPosition(0);
			int y= ra.getIntPosition(1);
			int z= 0;
			if(expectedValues.numDimensions()>2) z= ra.getIntPosition(2);
			double flux=calcFlux(totalInten, blobs, x,  y, z);
			double pZero=0;
			if(flux>0){
				poissonDist= new PoissonDistributionImpl(flux);	
				pZero=OtherTools.getErlangProp(0, value, GAIN)*poissonDist.probability(0);
			}

			else{
				poissonDist=null;
				pZero=OtherTools.getErlangProp(0, value, GAIN);
			}








			double invGain=1.0/GAIN;
			double temp=Math.sqrt(invGain*flux*(double)value);
			double missingTerm=0;
			if(pZero!=0) missingTerm=pZero/Math.exp(-invGain*value-flux)*invGain*flux;

			//double tempA=temp*OtherTools.bessi0(2*temp);
			//double tempB=(OtherTools.bessi1(2*temp)+missingTerm);
			double tempA=temp*Bessel.i0(2*temp);
			double tempB=(double)Bessel.i1(2*temp)+missingTerm;

			double besselExpected=0;

			if(tempB!=0) besselExpected=tempA/tempB;
			if(Double.isNaN(besselExpected) || Double.isInfinite(besselExpected)) 
				besselExpected=(temp*Bessel.i0e(2*temp)) / (Bessel.i1e(2*temp)+missingTerm*Math.exp(-2*temp));




			if(!Double.isNaN(besselExpected)){
				ra.get().set((float)(besselExpected));		
				newTotalInt+=besselExpected;
			}else {
				newTotalInt+=value/GAIN;
				System.out.println("NAN!!!!!!!!");	
				System.out.println("value:"+ value+ " flux:" +flux+" temp:"+ temp+
						"x:"+ x+" y:"+y+ 
						" missingTerm:"+missingTerm+
						" pZero:"+pZero+
						" Math.exp(-invGain*value-flux):"+ Math.exp(-invGain*value-flux)+
						" OtherTools.bessi0(2*temp):"+Bessel.i0(2*temp)+
						" OtherTools.bessi1(2*temp):"+Bessel.i1(2*temp));	

				ra.get().set((0));
				calcFlux(totalInten, blobs, x,  y, z);
			}


		}


		return newTotalInt;
	}

	protected double calcFlux(double totalInten, List<Blob> blobs, int x, int y, int z){
		double numOfPixels=super.numOfPixelsUsed;


		double backProb=1;
		for(Blob b: blobs){
			backProb-=b.pK;
		}

		double akku=0;



		for(Blob b: blobs){		
			akku+=b.pXandK(x, y, z, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ, b.denom);

			if(Double.isNaN(akku)||Double.isNaN(totalInten) || Double.isInfinite(akku*totalInten)){
				return 0;
			}

		}

		return akku*totalInten+((totalInten*backProb)/numOfPixels);
		//return 1;
	}

	private double doMstep(boolean alternateMethod, List<Blob> trackables,
			Img<FloatType> expectedValues, double qualityT,
			Session<Blob, IT> session){
		MaximumLikelihoodBlobPolicy<FloatType> bp= new MaximumLikelihoodBlobPolicy<FloatType>();

		return bp.doOptimizationSingleScale(trackables, expectedValues, qualityT, 0, 1000,(BlobSession<IT>) session);

	}

	public class ErlangSet{
		private final TreeMap<Integer,ErlangDist> dists;
		private final double gain;

		public ErlangSet( double g){
			dists=new TreeMap<Integer,ErlangDist>();
			gain = g;
		}

		public synchronized double getErlangProb( int input, int output){		
			ErlangDist e= dists.get(input);
			if(e==null){

				e=new ErlangDist(input, gain, 0.01);
				synchronized (this){
					dists.put(input, e);
				}
			}		
			return e.getProb(output);

		}

		public synchronized double draw(int input, double rv){
			ErlangDist e= dists.get(input);
			if(e==null){
				e=new ErlangDist(input, gain, 0.01);
				synchronized (this){
					dists.put(input, e);
				}
			}
			return e.drawOutput(rv);

		}



	}



	public double getLogLikelihood(double totalFlux,  List<Blob> tempTrackables, IterableRandomAccessibleInterval<IT> tempImage){

		double inverseGain=1.0/GAIN;

		Cursor<IT> cursor= tempImage.cursor();	
		double akku=0;
		int i=0;
		double akkuCheck=0;



		while ( cursor.hasNext() )	{
			cursor.fwd();
			i++;
			int x= cursor.getIntPosition(0);
			int y= cursor.getIntPosition(1);
			numOfPixelsUsed=(tempImage.dimension(0)*tempImage.dimension(1));
			double flux=calcFlux(totalFlux, tempTrackables, x, y, 0);
			int value= (int)cursor.get().getRealDouble();
			if(value==0){
				akku+=-flux;
			}else{

				akku+=Math.log(Math.sqrt(inverseGain*flux/(double)value));
				akku+=(-inverseGain*value-flux);
				akku+=Math.log(Bessel.i1(2.0*Math.sqrt(inverseGain*flux*value)));
			}
			akkuCheck+=flux;

		}

		return akku;
	}
}