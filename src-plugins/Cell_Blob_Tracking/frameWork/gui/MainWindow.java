package frameWork.gui;

import ij.ImageListener;
import ij.ImagePlus;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class MainWindow  <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageWindow<T,IT> implements ImageListener {

	private int currentFrameNumber;
	private int currentSliceNumber;
	private int currentChannelNumber;
	private ImagePlus imp; 
	
	protected MainWindow(ImagePlus imagePlus,  RandomAccessibleInterval<IT> img, Model<T, IT> mod, ViewModel<T, IT> vm) {
		super(mod, img, imagePlus.getWindow().getTitle(), vm);
		imp=imagePlus;
		// TODO Auto-generated constructor stub
		currentFrameNumber= imp.getFrame()-1;
		currentSliceNumber= imp.getSlice()-1;
		currentChannelNumber=imp.getChannel()-1;
		ImagePlus.addImageListener(this);
	}

	@Override
	public void rePaint(long[] position, boolean rePaintImage) {
		
			
		if(position.length>3)currentFrameNumber= (int)position[3];
		if(position.length>2)currentSliceNumber= (int)position[2];
		if(position.length>4)currentChannelNumber=(int)position[4];
		
		if(currentFrameNumber != imp.getFrame()-1
				|| currentSliceNumber!= imp.getSlice()-1
				|| currentChannelNumber!= imp.getChannel()-1) 
					imp.setPosition(currentChannelNumber+1, currentSliceNumber+1, currentFrameNumber+1);
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		imp.setOverlay(ov);
		imp.updateAndDraw();

	}
	

	@Override
	public void imageClosed(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void imageOpened(ImagePlus arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized void imageUpdated(ImagePlus arg0) {
		
		
		if(!arg0.equals(imp)) return;
		
		
		if(currentFrameNumber != imp.getFrame()-1){
			currentFrameNumber= imp.getFrame()-1;
			viewModel.setPosition(3, currentFrameNumber);
		
			return;
		}
		
		if(currentSliceNumber!= imp.getSlice()-1){
			currentSliceNumber= imp.getSlice()-1;
			viewModel.setPosition(2, currentSliceNumber);
		
			return;
		}
		
		if(currentChannelNumber!= imp.getChannel()-1){
			currentChannelNumber= imp.getChannel()-1;
			viewModel.setPosition(4, currentChannelNumber);
			
			return;
		}
			
	
		return;
		
	}



}
