package frameWork;

import java.util.ArrayList;
import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;

public abstract class Frame<T extends Trackable, IT extends Type<IT>> {
private List <T> trackables;
protected RandomAccessibleInterval<IT> frameView;
protected int frameNumber;

protected Frame(int frameNum, RandomAccessibleInterval<IT> view){
	frameView=view;
	frameNumber=frameNum;
	trackables= new ArrayList<T>();
	
}

public abstract void optimizeFrame();
public abstract Frame<T,IT> createFrame(int frameNum, RandomAccessibleInterval<IT> view);
public void addTrackable(T trackable){
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
		
	}
	return winner;
}

public List<T>getTrackables(){
	return trackables;
}

}
