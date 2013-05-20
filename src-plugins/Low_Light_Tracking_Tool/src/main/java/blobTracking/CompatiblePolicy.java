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

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public class CompatiblePolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> >
extends MaximumLikelihoodBlobPolicy<IT> {

	@Override
	public Blob loadTrackableFromString(String s, int sessionId) {



		String[] values=s.split("\t");




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
