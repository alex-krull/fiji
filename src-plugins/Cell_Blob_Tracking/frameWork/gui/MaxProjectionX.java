package frameWork.gui;

import ij.gui.Overlay;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;

import tools.ImglibTools;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import frameWork.Model;
import frameWork.Trackable;



	public class MaxProjectionX <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> implements MouseListener {
		
		public MaxProjectionX(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
			super(mod, 
					ImglibTools.scaleByFactor(
							Views.zeroMin( Views.invertAxis( Views.zeroMin( Views.rotate( ImglibTools.projection(img,0),0,1) ),0  ) ) , 
					0,mod.xyToZ)
					, "max-X-projection",3, vm);
			imp.getCanvas().addMouseListener(this);
		}
		
		public void rePaint(long[] position){
			this.clearOverlay();
			
			this.addXOverlayes((int)position[3]);
			this.addXLineOverlay(((double)position[2]+0.5)*model.xyToZ);
			super.rePaint(position);
			}

		@Override
		public void mouseClicked(MouseEvent arg0) {
			int x=imp.getCanvas().offScreenX(arg0.getX());
			int y=imp.getCanvas().offScreenY(arg0.getY());
			System.out.println("x:"+ x +"  y:"+y);
			viewModel.setPosition(2,(int)((double)x/model.xyToZ));
			
		}

		@Override
		public void mouseEntered(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent arg0) {
			// TODO Auto-generated method stub
			
		}

	}
	

