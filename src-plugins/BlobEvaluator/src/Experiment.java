import java.util.ArrayList;
import java.util.List;

import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;
import blobTracking.Blob;
import blobTracking.CompatiblePolicy;
import blobTracking.EMCCDBlobPolicy;
import blobTracking.MaximumLikelihoodBlobPolicy;
import frameWork.Controller;
import frameWork.Model;
import frameWork.Sequence;
import frameWork.Session;


public class Experiment {
	
	public Model <UnsignedShortType> model;
	public Controller <UnsignedShortType> cont;
	public Sequence <Blob> seq;
	
	public Experiment(Img<UnsignedShortType> data, double qualityThreshold, double initX, double initY, 
			double sigma, boolean autoSigma, double minSigma, double maxSigma, boolean multiScale,
			String typeName, String sessionLabel){
		
		
		model= 
				new Model<UnsignedShortType>("eval.tif", "/home/alex/Desktop/", data,
						(int) data.dimension(2), 1, 1, false);
				System.out.println("creating Controller...");
					
		
		cont= new Controller<UnsignedShortType>(model,null);
		
		cont.addPolicy(new MaximumLikelihoodBlobPolicy<UnsignedShortType>());
		cont.addPolicy("Blob", new CompatiblePolicy<UnsignedShortType>());
		cont.addPolicy(new EMCCDBlobPolicy<UnsignedShortType>());
		

		cont.addSession(typeName, sessionLabel, 0, null);
		
	
		((Session<Blob,UnsignedShortType>)cont.getCurrentSession()).addTrackable(new Blob(1, 0, initX, initY, 0, sigma, 0, autoSigma, 2, maxSigma));
		cont.getCurrentSession().setQualityThreshold(qualityThreshold);
		List<Integer> sidl= new ArrayList<Integer>();
		sidl.add(1);
		cont.setSelectionList(sidl);
		
	//	cont.toggleTracking(0,true, model.getNumberOfFrames());
		cont.getCurrentSessionController().startTrackingSingleThread(0, multiScale, true, model.getNumberOfFrames()-1);
		seq=(Sequence<Blob>) cont.getSessions().get(0).getSequence(1);
	}
	
	
}
