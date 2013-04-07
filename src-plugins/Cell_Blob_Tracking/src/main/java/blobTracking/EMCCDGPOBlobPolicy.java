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

import java.util.List;

import org.apache.commons.math.analysis.MultivariateRealFunction;
import org.apache.commons.math.optimization.GoalType;
import org.apache.commons.math.optimization.direct.MultiDirectionalSimplex;
import org.apache.commons.math.optimization.direct.PowellOptimizer;
import org.apache.commons.math.optimization.direct.SimplexOptimizer;

import cern.jet.math.Bessel;

import frameWork.Model;
import frameWork.MovieFrame;
import frameWork.Session;

import net.imglib2.Cursor;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.type.numeric.real.FloatType;
import net.imglib2.view.IterableRandomAccessibleInterval;

public class EMCCDGPOBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends EMCCDBlobPolicy<IT>
implements MultivariateRealFunction
{

	private IterableRandomAccessibleInterval<IT> tempImage=null;
	private List<Blob> tempTrackables;
	private int count;


	@Override
	public double value(double[] arg0) {
		double totalFlux=arg0[arg0.length-1];

		for(int i=2;i<arg0.length;i+=3){
			totalFlux+=arg0[i];
		}

		int count=0;
		for(Blob b:tempTrackables){
			b.xPos=arg0[0+count]; b.yPos=arg0[1+count];			
			b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
			totalFlux= arg0[2+count]*b.denom+arg0[3+count]*(double)(tempImage.dimension(0)*tempImage.dimension(1));
			b.pK=(arg0[2+count]*b.denom)/totalFlux;

			count+=3;
		}

		double energy= getLogLikelihood(totalFlux, tempTrackables, tempImage);

		return energy;
	}

	@Override
	public String getTypeName() {
		// TODO Auto-generated method stub
		return "EMCCD-GPO";
	}

	@Override
	public void optimizeFrame(boolean alternateMethod, List<Blob> trackables,
			MovieFrame<IT> movieFrame, double qualityT,
			Session<Blob, IT> session) {


		count=0;
		tempImage=new IterableRandomAccessibleInterval<IT>( movieFrame.getFrameView());
		tempTrackables=trackables;

		int count=0;

		int nop=calcNumberOfPixels(tempImage);





		Cursor<IT> cursor= tempImage.cursor();	
		double akku=0;
		int i=0;
		double akkuCheck=0;


		PowellOptimizer optimizer = new PowellOptimizer(1.5e-15, 1.5e-15);


		double totalFlux=0; 
		while ( cursor.hasNext() )	{
			cursor.fwd();
			totalFlux+= (int)cursor.get().getRealDouble();
		}
		totalFlux=(totalFlux/GAIN)*PAG;
		double backFlux=totalFlux;






		double[] startPoint=new double [3*trackables.size()+1];
		for(Blob b:trackables){

			b.numberOfPixels=nop;
			if(b.inten==0)
				b.inten=totalFlux*b.pK;


			backFlux-=b.inten;

			startPoint[0+count]=b.xPos;
			startPoint[1+count]=b.yPos;
			startPoint[2+count]= b.inten;	//blobFlux


			count+=3;
		}

		startPoint[startPoint.length-1]=0.839;//backFlux/(double)nop;//b.backInten/(double)b.numberOfPixels;				//BackgroundFlux per pixel


		double []output=null;


		startPoint= optimizer.optimize(10000000, this, GoalType.MAXIMIZE, startPoint).getPoint();
		System.out.println("2ndTry:");
		totalFlux=startPoint[startPoint.length-1];	//add background

		for(int j=2;j<startPoint.length;j+=3){		//add other intensities
			totalFlux+=startPoint[j];
		}


		count=0;
		for(Blob b:trackables){
			System.out.println("count:"+count);
			b.xPos=startPoint[0+count]; b.yPos=startPoint[1+count];		    
			b.calcDenominator(tempImage, b.xPos, b.yPos, b.zPos, b.sigma, b.sigmaZ);
			b.pK=(startPoint[2+count]*b.denom)/totalFlux;	
			b.inten=totalFlux*b.pK/b.denom;
			b.backInten = 	startPoint[startPoint.length-1]*(double)nop;    	
			count+=3;
		}


	}

}
