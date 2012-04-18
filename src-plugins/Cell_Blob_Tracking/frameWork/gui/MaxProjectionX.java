package frameWork.gui;

import java.awt.event.MouseEvent;

import net.imglib2.RandomAccessibleInterval;
import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;




	public class MaxProjectionX < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ImageView<IT>{
		
		public MaxProjectionX(Model<IT> mod, ViewModel<IT> vm){
			super(mod, 
				     null,		
					"max-X-projection",vm, null);
			this.scaleX=model.getXyToZ();
	
			startThread();
		
		}
		
		@Override
		public void reFresh(long[] position, boolean rePaintImage){
			this.clearOverlay();
			
			this.addXOverlayes((int)position[3]);
			this.addXLineOverlay(position[2]);
			if(viewModel.mouseIsInWindow)  this.addYShortLineOverlay(position[1], position[2],10);
			
			int frameNumber= (int)position[3];
			RandomAccessibleInterval<IT> toDraw=model.getFrame(frameNumber, viewModel.getCurrentChannelNumber()).getXProjections();
			reDraw( position ,rePaintImage, toDraw);
			}
		
		@Override
		public long[] positionFromEvent(MouseEvent e){
			int x=(int)(imp.getCanvas().offScreenX(e.getX())/this.scaleX);
			int y=(int)(imp.getCanvas().offScreenY(e.getY())/this.scaleY);
			System.out.println("x:"+ x +"  y:"+y);
			
			
				long[] pos= viewModel.getPosition();
				pos[0]=-1;
				pos[1]=y; 
				pos[2]=x;
			return pos;
		}
		
		


	}
	

