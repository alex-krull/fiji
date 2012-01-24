package frameWork.gui;

import ij.gui.Overlay;

import java.util.List;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Trackable;

public class MaxProjectionZ <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> {
	
	public MaxProjectionZ(Model<T,IT> mod, RandomAccessibleInterval<IT> img){
		super(mod, ImglibTools.projection(img,2), "max-Z-projection",3);
	}
	
	public void rePaint(long[] position){	
		this.clearOverlay();
		addZOverlayes((int)position[3]);
		super.rePaint(position);
	}

}
