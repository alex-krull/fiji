package frameWork;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;


public abstract class TrackingFrame<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>>{
protected List <T> trackables;
protected int frameNumber;

protected TrackingFrame(int frameNum){
	frameNumber=frameNum;
	trackables= new ArrayList<T>();
}

public abstract void optimizeFrame(boolean cheap);

public void addTrackable(T trackable){
	for(T t:trackables){
		if(t.sequenceId==trackable.sequenceId){
			trackables.remove(t);
			break;
		}
	}
	trackables.add(trackable);
}

public int selectAt(int x, int y, int z){
	double bestResponse=1;
	int winner = -1;
	for(T t: trackables){
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
	return trackables;
}

public List<T> cloneTrackablesForFrame (int newFrame){
	List<T> results = new ArrayList<T>();
	for(T trackable:trackables){
		T newTrackable= copy(trackable);		
		newTrackable.frameId=newFrame;
		results.add(newTrackable);
	}
	return results;
}

public abstract T copy(T toCopy);

}
