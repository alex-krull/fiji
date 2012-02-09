package frameWork;

import java.util.ArrayList;
import java.util.List;

import tools.ImglibTools;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.Type;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;

public abstract class Frame<T extends Trackable, IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
protected List <T> trackables;
protected int frameNumber;

protected Frame(int frameNum){
	frameNumber=frameNum;
	trackables= new ArrayList<T>();
}

public abstract void optimizeFrame();

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
		System.out.println("seqID:"+t.sequenceId + "  dist:"+currentResponse);
		
	}
	return winner;
}

public List<T>getTrackables(){
	return trackables;
}

}
