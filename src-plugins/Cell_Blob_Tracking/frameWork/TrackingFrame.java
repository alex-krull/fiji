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
	double bestResponse=1;
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
