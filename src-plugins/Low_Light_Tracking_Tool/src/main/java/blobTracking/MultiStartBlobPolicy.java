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

import frameWork.MovieFrame;
import frameWork.Session;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

public class MultiStartBlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends BlobPolicy<IT> {

	private BlobPolicy<IT> internalPolicy;
	private int numberOfRestarts;
	
	public MultiStartBlobPolicy(BlobPolicy<IT> ip, int nor){
		internalPolicy= ip;
		numberOfRestarts=nor;
	}
	
	@Override
	public String getTypeName() {
		return "MultiStartBlobPolicy";
	}

	@Override
	public void optimizeFrame(boolean alternateMethod, List<Blob> trackables,
			MovieFrame<IT> movieFrame, double qualityT,
			Session<Blob, IT> session) {
		
		Blob refB= this.copy(trackables.get(0));
		Blob b= trackables.get(0);
		
		double centX= (double)(movieFrame.getFrameView().dimension(0)-1)/2;
		double centY= (double)(movieFrame.getFrameView().dimension(1)-1)/2;
		
		
		
		double bestX=0;
		double bestY=0;
		double bestPk=0;
		double bestScore=Double.MAX_VALUE;
		
		for(int x=-numberOfRestarts/2; x<numberOfRestarts/2;x++){
			for(int y=-numberOfRestarts/2; y<numberOfRestarts/2;y++){
				b.pK=refB.pK;
				b.xPos=centX+(double)x*2;
				b.yPos=centY+(double)y*2;
				this.internalPolicy.optimizeFrame(alternateMethod, trackables, movieFrame, qualityT, session);
				double score=(b.xPos-centX)*(b.xPos-centX) + (b.yPos-centY)*(b.yPos-centY);
				if(score<bestScore){
					bestX=b.xPos;
					bestY=b.yPos;
					bestPk=b.pK;
				}
			}
		}
		
		b.xPos=bestX;
		b.yPos=bestY;
		b.pK=bestPk;
		
				
	}
	

}
