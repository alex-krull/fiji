package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;
import frameWork.Model;
import frameWork.Trackable;


public class MaxProjectionY <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> implements MouseListener {
		
		public MaxProjectionY(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
			super(mod, 
					ImglibTools.scaleByFactor(
							ImglibTools.projection(img,1),
					1,mod.xyToZ)
					, "max-Y-projection",3,vm);
			this.imp.getCanvas().addMouseListener(this);
		}
		
		public void rePaint(long[] position){
			this.clearOverlay();
			
			this.addYOverlayes((int)position[3]);
			this.addYLineOverlay(((double)position[2]+0.5)*model.xyToZ);
			super.rePaint(position);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			int x=imp.getCanvas().offScreenX(e.getX());
			int y=imp.getCanvas().offScreenY(e.getY());
			System.out.println("x:"+ x +"  y:"+y);
			viewModel.setPosition(2,(int)((double)y/model.xyToZ));
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

	
}
