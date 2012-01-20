package frameWork.gui;

import ij.gui.Overlay;

import java.util.List;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import frameWork.Model;
import frameWork.Trackable;



	public class MaxProjectionX <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> {
		
		public MaxProjectionX(Model<T,IT> mod, RandomAccessibleInterval<IT> img){
			super(mod, 
					ImglibTools.scaleByFactor(
							Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(img,0),0,1) ),0  ) ) , 
					0,mod.xyToZ)
					, "max-X-projection",3);
		}
		
		public void rePaint(long[] position){
			super.rePaint(position);
			this.addXOverlayes((int)position[3]);
			this.addXLineOverlay(((double)position[2]+0.5)*model.xyToZ);
			}

	}
	

