package frameWork;

import java.util.List;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.Type;

public abstract class Frame<T extends Trackable, IT extends Type<IT>> {
protected List <T> trackables;
protected RandomAccessibleInterval<IT> frameView;

public abstract void optimizeFrame();
public abstract Frame<T,IT> createFrame(int FrameNumber, RandomAccessibleInterval<IT> view);
public void addTrackable(T trackable){
	trackables.add(trackable);
}

}
