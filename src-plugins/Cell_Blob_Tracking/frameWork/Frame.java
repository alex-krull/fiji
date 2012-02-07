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
protected RandomAccessibleInterval<IT> frameView;
protected int frameNumber;
private RandomAccessibleInterval<IT> zProjection = null;
private RandomAccessibleInterval<IT> xProjection = null;
private RandomAccessibleInterval<IT> yProjection = null;

protected Frame(int frameNum, RandomAccessibleInterval<IT> view){
	frameView=view;
	frameNumber=frameNum;
	trackables= new ArrayList<T>();
		
}

public synchronized RandomAccessibleInterval<IT> getXProjections(){
	if(xProjection==null) xProjection=Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(frameView,0),0,1) ),0  ) ); 
	return xProjection;
}

public synchronized RandomAccessibleInterval<IT> getYProjections(){
	if(yProjection==null) yProjection=ImglibTools.projection(frameView,1);
	return yProjection;
}

public synchronized RandomAccessibleInterval<IT> getZProjections(){
	if(zProjection==null) zProjection=ImglibTools.projection(frameView,2);
	return zProjection;
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
