package frameWork.gui;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;



public class MaxProjectionY < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT> 
implements MouseListener, MouseMotionListener {
		
		public MaxProjectionY(Model<IT> mod, ViewModel<IT> vm){
			super(mod, 
					//ImglibTools.scaleByFactor(
							null,
					//1,mod.xyToZ)
					"max-Y-projection",vm,null);
			this.scaleY=model.xyToZ;
			imp.getCanvas().addMouseListener(this);
			imp.getCanvas().addMouseMotionListener(this);
		}
		
		@Override
		public void rePaint(long[] position, boolean rePaintImage){
			this.clearOverlay();
			
			this.addYOverlayes((int)position[3]);
			this.addYLineOverlay(position[2]);
			int frameNumber= (int)position[3];
			toDraw=model.getFrame(frameNumber, viewModel.getCurrentChannelNumber()).getYProjections();
			reDraw( position ,rePaintImage);
			//super.rePaint(position, rePaintImage);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			
			int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			
			System.out.println("x:"+ x +"  y:"+y);
			if(e.getButton()==MouseEvent.BUTTON2) viewModel.setPosition(2,(int)y);
			else{
				if(e.getButton()==MouseEvent.BUTTON1) viewModel.mouseAtPosition(positionFromEvent(e), e);
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
			if(e.getButton()==MouseEvent.BUTTON1) viewModel.mouseAtPosition(positionFromEvent(e), e);
			
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			
			viewModel.mouseAtPosition(positionFromEvent(e), e);
			
			
		}

		@Override
		public void mouseMoved(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}
		
		private long[] positionFromEvent(MouseEvent e){
			int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			
			
				long[] pos= viewModel.getPosition();
				pos[0]=x;
				pos[1]=-1; 
				pos[2]=y;
			return pos;
		}

	
}
