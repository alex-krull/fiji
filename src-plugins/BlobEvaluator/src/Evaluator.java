import net.imglib2.img.Img;
import net.imglib2.type.numeric.integer.UnsignedShortType;


public class Evaluator {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		System.out.println("creating Model...");
		Blob_Simulator bs= new Blob_Simulator();
		Img<UnsignedShortType> img=bs.makeImg(10, 10, 100,5, 5, 1, 30, 0, false, 300);
		
		new Experiment(img, 0.001, 5, 5, 1, false, 0, 2, false, "M.L.GaussianTracking", "epxA");
		


	}
	
	

}
