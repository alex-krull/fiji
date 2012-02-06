package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import net.imglib2.view.Views;
import tools.ImglibTools;
import frameWork.Model;
import frameWork.Trackable;


public class MaxProjectionY <T extends Trackable , IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends StackWindow<T,IT> 
implements MouseListener, MouseMotionListener {
		
		public MaxProjectionY(Model<T,IT> mod, RandomAccessibleInterval<IT> img,  ViewModel<T,IT> vm){
			super(mod, 
					//ImglibTools.scaleByFactor(
							ImglibTools.projection(img,1),
					//1,mod.xyToZ)
					"max-Y-projection",3,vm);
			this.scaleY=model.xyToZ;
			imp.getCanvas().addMouseListener(this);
			imp.getCanvas().addMouseMotionListener(this);
		}
		
		public void rePaint(long[] position, boolean rePaintImage){
			this.clearOverlay();
			
			this.addYOverlayes((int)position[3]);
			this.addYLineOverlay((double)position[2]);
			super.rePaint(position, rePaintImage);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			
			int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			
			System.out.println("x:"+ x +"  y:"+y);
			if(e.getButton()==MouseEvent.BUTTON2) viewModel.setPosition(2,(int)((double)y));
			else{
				long[] pos= viewModel.getPosition();
				pos[0]=x;
				pos[1]=-1; 
				pos[2]=y;
				viewModel.mouseAtPosition(pos, e);
			}
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
			int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			
			if(e.getButton()==MouseEvent.BUTTON1){
				long[] pos= viewModel.getPosition();
				pos[0]=x;
				pos[1]=-1; 
				pos[2]=y;
				viewModel.mouseAtPosition(pos, e);
			}
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			int x=(int)((double)imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)((double)imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			if(e.getButton()==MouseEvent.BUTTON1){
				long[] pos= viewModel.getPosition();
				pos[0]=x;
				pos[1]=-1; 
				pos[2]=y;
				viewModel.mouseAtPosition(pos, e);
			}
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

	
}
