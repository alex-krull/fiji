package frameWork.gui;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;
import frameWork.Model;
import frameWork.Trackable;


public class MaxProjectionY <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> {
		
		public MaxProjectionY(Model<T,IT> mod, RandomAccessibleInterval<IT> img){
			super(mod, 
					ImglibTools.scaleByFactor(
							ImglibTools.projection(img,1),
					1,mod.xyToZ)
					, "max-Y-projection",3);
		}
		
		public void rePaint(long[] position){
			this.clearOverlay();
			
			this.addYOverlayes((int)position[3]);
			this.addYLineOverlay(((double)position[2]+0.5)*model.xyToZ);
			super.rePaint(position);
		}

	
}
