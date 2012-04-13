package frameWork;


import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;

public class MovieFrame <IT extends NumericType<IT> & NativeType<IT> & RealType<IT>> {
	protected int frameNumber;
	
	private RandomAccessibleInterval<IT> zProjection = null;
	private RandomAccessibleInterval<IT> xProjection = null;
	private RandomAccessibleInterval<IT> yProjection = null;
	protected RandomAccessibleInterval<IT> frameView;
	protected int constBackground;
	protected boolean isVolume;
	protected double xyToZ;
	
	public boolean isVolume(){
		return this.isVolume;
	}
	
	public int getNumberOfPlanes(){
		if(frameView.numDimensions()<3) return 1;
		return (int)frameView.dimension(2);
	}
	
	public int getConstBackground() {
		return constBackground;
	}

	public void setConstBackground(int constBackground) {
		this.constBackground = constBackground;
	}

	public RandomAccessibleInterval<IT> getFrameView() {
		return frameView;
	}

	public void setFrameView(RandomAccessibleInterval<IT> frameView) {
		this.frameView = frameView;
	}

	public MovieFrame(int frameNum, RandomAccessibleInterval<IT> view, int cBackGround, double zRatio){
		xyToZ=zRatio;
		constBackground=cBackGround;
		frameView=view;
		frameNumber=frameNum;
		isVolume=(frameView.numDimensions()>2);
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
	
	
}
