package frameWork.gui.controlWindow;

import ij.gui.GenericDialog;

import javax.swing.JDialog;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;

import frameWork.Model;
import frameWork.gui.ViewModel;
import frameWork.Controller;

public class NewSessionDialog < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > {

	private String methodChoice;


	private Integer channelChoice;


	private String userSessionName;

	public NewSessionDialog(Controller<IT> controller, Model <IT> model){
		JDialog.setDefaultLookAndFeelDecorated(true);


		String[] trackingMethods = controller.getPossibleSessionTypes();
		int channelNumber = model.getNumberOfChannels();
		String[] channelList = new  String[channelNumber];
		Integer i;
		for (i=1; i <= channelNumber; i++ ){
			channelList[i-1]= i +"";
		}

		GenericDialog gd = new GenericDialog("New Session");
		gd.addStringField("Enter new session name: ", "");
		gd.addChoice("Pick tracking method", trackingMethods, null);
		gd.addChoice("Pick channel to track", channelList, null);
		gd.showDialog();
		if(gd.wasCanceled())
			return;
		userSessionName = gd.getNextString();




		 methodChoice = gd.getNextChoice();
		 channelChoice =Integer.valueOf(gd.getNextChoice());



		
		//viewModel.getController().addSession(methodChoice, userSessionName, channelChoice-1, viewModel);
	}
	public String getUserSessionName() {
		return userSessionName;
	}
	public String getMethodChoice() {
		return methodChoice;
	}

	public Integer getChannelChoice() {
		return channelChoice;
	}

}


