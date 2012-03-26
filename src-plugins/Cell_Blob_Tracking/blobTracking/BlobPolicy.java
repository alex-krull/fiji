package blobTracking;

import java.util.Properties;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.ChannelController;
import frameWork.Model;
import frameWork.Policy;

public class BlobPolicy<IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends Policy<Blob, IT>{


	@Override
	public String getTypeName() {
		return "Blob";
	}


	@Override
	public ChannelController<Blob, IT> produceControllerAndChannel(
			Properties sessionProps, Model<IT> model) {
		int cid= Integer.valueOf(sessionProps.getProperty("channelId"));
		int sid= Integer.valueOf(sessionProps.getProperty("sessionId"));
		BlobTrackingChannel<IT> btc=  new BlobTrackingChannel<IT>(model.getMovieChannel(cid), sid);
		model.addTrackingChannel(btc, btc.getId());
		
		return new BlobController<IT>(model,btc);
	}

}
