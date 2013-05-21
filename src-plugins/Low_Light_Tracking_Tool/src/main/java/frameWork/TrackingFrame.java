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
package frameWork;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;


public abstract class TrackingFrame<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>>{
	protected List <T> trackablesInFrame;
	protected int frameNumber;
	protected Policy<T,IT> policy;
	private MovieFrame<IT> movieFrame;


	public MovieFrame<IT> getMovieFrame() {
		return movieFrame;
	}



	public void setMovieFrame(MovieFrame<IT> movieFrame) {
		this.movieFrame = movieFrame;
	}



	protected TrackingFrame(int frameNum, Policy <T,IT> pol, MovieFrame<IT> mf ){
		movieFrame=mf;
		frameNumber=frameNum;
		trackablesInFrame= new ArrayList<T>();
		policy=pol;
	}



	public void addTrackable(T trackable){
		removeTrackable(trackable.sequenceId);
		trackablesInFrame.add(trackable);
	}

	public void removeTrackable(int id){
		for(T t:trackablesInFrame){
			if(t.sequenceId==id){
				trackablesInFrame.remove(t);
				break;
			}
		}

	}

	public int selectAt(int x, int y, int z){
		double bestResponse=Double.MAX_VALUE;
		int winner = -1;
		for(T t: trackablesInFrame){
			double currentResponse=t.getDistanceTo(x, y, z);
			if(winner==-1 || bestResponse> currentResponse){
				bestResponse=currentResponse;
				winner=t.sequenceId;
			}
			System.out.println("seqID:"+t.sequenceId + "  dist:"+currentResponse);

		}
		return winner;
	}

	public List<T>getTrackables(){
		return trackablesInFrame;
	}

	public List<T> cloneTrackablesForFrame (int newFrame){
		List<T> results = new ArrayList<T>();
		for(T trackable:trackablesInFrame){
			T newTrackable= policy.copy(trackable);	
			newTrackable.frameId=newFrame;
			results.add(newTrackable);
		}
		return results;
	}


}
