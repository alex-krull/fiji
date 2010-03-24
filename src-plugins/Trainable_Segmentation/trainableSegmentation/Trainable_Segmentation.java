package trainableSegmentation;
/* This is a small Plugin that should perform better in segmentation than thresholding
 * The idea is to train a random forest classifier on given manual labels
 * and then classify the whole image 
 * I try to keep parameters hidden from the user to make usage of the plugin
 * intuitive and easy. I decided that it is better to need more manual annotations
 * for training and do feature selection instead of having the user manually tune 
 * all filters.
 * 
 * ToDos:
 * - work with color features
 * - work on whole Stack 
 * - delete annotations with a shortkey
 * - change training image
 * - do probability output (accessible?) and define threshold
 * - put thread solution to wiki http://pacific.mpi-cbg.de/wiki/index.php/Developing_Fiji#Writing_plugins
 * 
 * - clean up gui (buttons, window size, funny zoom)
 * - give more feedback when classifier is trained or applied
 * 
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * Author: Verena Kaynig (verena.kaynig@inf.ethz.ch)
 */


import ij.IJ;
import ij.ImageStack;
import ij.plugin.PlugIn;
import ij.plugin.RGBStackMerge;

import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.gui.ImageWindow;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.ImagePlus;
import ij.WindowManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;


import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;
import hr.irb.fastRandomForest.FastRandomForest;

public class Trainable_Segmentation implements PlugIn {


	private static final int MAX_NUM_CLASSES = 5;

	private List<Roi> [] examples = new ArrayList[MAX_NUM_CLASSES]; 
	private ImagePlus trainingImage;
	private ImagePlus displayImage;
	private ImagePlus classifiedImage;
	private ImagePlus overlayImage;
	private FeatureStack featureStack;
	private CustomWindow win;

	private int traceCounter[] = new int[MAX_NUM_CLASSES];
	private boolean showColorOverlay;
	private Instances wholeImageData;
	private Instances loadedTrainingData;
	private FastRandomForest rf;
	
	private static boolean updateWholeData = true;
	
	final JButton addExampleButton;
	final JButton trainButton;
	final JButton overlayButton;
	final JButton resultButton;
	final JButton applyButton;
	final JButton loadDataButton;
	final JButton saveDataButton;

	final JButton addClassButton;

	final Color[] colors = new Color[]{Color.red, Color.green, Color.blue,
			Color.orange, Color.pink};

	String[] classLabels = new String[]{"background", "foreground", "class-3", "class-4", "class-5"};

	private static int numOfClasses = 2;
	private java.awt.List exampleList[] = new java.awt.List[MAX_NUM_CLASSES];
	private JRadioButton [] classButton = new JRadioButton[MAX_NUM_CLASSES];
	//Group the radio buttons.
	ButtonGroup classButtonGroup = new ButtonGroup();


	public Trainable_Segmentation() 
	{
		addExampleButton = new JButton("+");
		trainButton = new JButton("Train classifier");
		overlayButton = new JButton("Toggle overlay");
		overlayButton.setEnabled(false);
		resultButton = new JButton("Create result");
		resultButton.setEnabled(false);
		applyButton = new JButton ("Apply classifier");
		applyButton.setEnabled(false);
		loadDataButton = new JButton ("Load data");
		saveDataButton = new JButton ("Save data");

		addClassButton = new JButton ("Add class");


		for(int i = 0; i < numOfClasses ; i++)
		{
			examples[i] = new ArrayList<Roi>();
			exampleList[i] = new java.awt.List(5);
			exampleList[i].setForeground(colors[i]);
		}

		showColorOverlay = false;

		rf = new FastRandomForest();
		//FIXME: should depend on image size?? Or labels??
		rf.setNumTrees(200);
		//this is the default that Breiman suggests
		//rf.setNumFeatures((int) Math.round(Math.sqrt(featureStack.getSize())));
		//but this seems to work better
		rf.setNumFeatures(6);


		rf.setSeed(123);
	}

	final ExecutorService exec = Executors.newFixedThreadPool(1);

	
	/**
	 * Listeners
	 */
	private ActionListener listener = new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			exec.submit(new Runnable() {
				public void run() {

					if(e.getSource() == addExampleButton)
					{
						IJ.log("+ pressed");
						for(int i = 0; i < numOfClasses; i++)
						{
							if(classButton[i].isSelected())
							{
								addExamples(i);
								break;
							}
						}
					}
					else if(e.getSource() == trainButton){
						trainClassifier();
					}
					else if(e.getSource() == overlayButton){
						toggleOverlay();
					}
					else if(e.getSource() == resultButton){
						showClassificationImage();
					}
					else if(e.getSource() == applyButton){
						applyClassifierToTestData();
					}
					else if(e.getSource() == loadDataButton){
						loadTrainingData();
					}
					else if(e.getSource() == saveDataButton){
						saveTrainingData();
					}
					else if(e.getSource() == addClassButton){
						addNewClass();
					}
					else{ 
						for(int i = 0; i < numOfClasses; i++)
							if(e.getSource() == exampleList[i])
							{
								deleteSelected(e);
								break;
							}
					}

				}
			});


		}
	};

	private ItemListener itemListener = new ItemListener() {
		public void itemStateChanged(final ItemEvent e) {
			exec.submit(new Runnable() {
				public void run() {
					for(int i = 0; i < numOfClasses; i++)
					{
						if(e.getSource() == exampleList[i])
							listSelected(e, i);
					}
				}
			});
		}
	};

	/**
	 * Custom window to define the trainable segmentation GUI
	 * 
	 */
	private class CustomWindow extends ImageWindow 
	{
		/** layout for annotation panel */
		GridBagLayout boxAnnotation = new GridBagLayout();
		/** constraints for annotation panel */
		GridBagConstraints annotationsConstraints = new GridBagConstraints();
		/** Panel with class radio buttons and lists */
		Panel annotationsPanel = new Panel();
		
		Panel imageAndLists = new Panel();
		
		Panel buttons = new Panel();
		
		JPanel trainingJPanel=new JPanel(new GridBagLayout());
		
		Panel all = new Panel();
		
		CustomWindow(ImagePlus imp) 
		{
			super(imp);

			Panel piw = new Panel();
			piw.setLayout(super.getLayout());
			setTitle("Trainable Segmentation");
			for (Component c : getComponents()) {
				piw.add(c);
			}

			
			
			annotationsConstraints.anchor = GridBagConstraints.NORTHWEST;
			annotationsConstraints.fill = GridBagConstraints.NONE;
			annotationsConstraints.gridwidth = 1;
			annotationsConstraints.gridheight = 1;
			annotationsConstraints.gridx = 0;
			annotationsConstraints.gridy = 0;

			annotationsPanel.setLayout(boxAnnotation);
			for(int i = 0; i < numOfClasses ; i++)
			{
				exampleList[i].addActionListener(listener);
				classButton[i] = new JRadioButton(classLabels[i]);
				classButtonGroup.add(classButton[i]);

				boxAnnotation.setConstraints(classButton[i], annotationsConstraints);
				annotationsPanel.add(classButton[i]);
				annotationsConstraints.gridy++;

				boxAnnotation.setConstraints(exampleList[i], annotationsConstraints);
				annotationsPanel.add(exampleList[i]);
				annotationsConstraints.gridy++;
			}

			// Select first class
			classButton[1].setSelected(true);

			BoxLayout boxImgList = new BoxLayout(imageAndLists, BoxLayout.X_AXIS);
			
			imageAndLists.setLayout(boxImgList);
			imageAndLists.add(piw);
			imageAndLists.add(annotationsPanel);

			
			BoxLayout buttonLayout = new BoxLayout(buttons, BoxLayout.Y_AXIS);
			buttons.setLayout(buttonLayout);
			//buttons.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

			// Add listeners
			addExampleButton.addActionListener(listener);
			trainButton.addActionListener(listener);
			overlayButton.addActionListener(listener);
			resultButton.addActionListener(listener);
			applyButton.addActionListener(listener);
			loadDataButton.addActionListener(listener);
			saveDataButton.addActionListener(listener);
			addClassButton.addActionListener(listener);

			// Buttons panel (left side of the GUI)
			buttons.add(addExampleButton);
			buttons.add(trainButton);
			buttons.add(overlayButton);
			buttons.add(resultButton);
			buttons.add(applyButton);
			buttons.add(loadDataButton);
			buttons.add(saveDataButton);
			buttons.add(addClassButton);

			for (Component comp : new Component[]{addExampleButton, trainButton, overlayButton, 
					resultButton, applyButton, loadDataButton, saveDataButton, addClassButton}) {
				comp.setMaximumSize(new Dimension(230, 50));
				comp.setPreferredSize(new Dimension(130, 30));
			}

			
			BoxLayout box = new BoxLayout(all, BoxLayout.X_AXIS);
			all.setLayout(box);
			all.add(buttons);
			all.add(imageAndLists);
			removeAll();
			add(all);

			pack();
			pack();

			// Propagate all listeners
			for (Panel p : new Panel[]{all, buttons, piw}) {
				for (KeyListener kl : getKeyListeners()) {
					p.addKeyListener(kl);
				}
			}

			addWindowListener(new WindowAdapter() {
				public void windowClosing(WindowEvent e) {
					IJ.log("closing window");
					// cleanup
					exec.shutdownNow();
					addExampleButton.removeActionListener(listener);
					trainButton.removeActionListener(listener);
					overlayButton.removeActionListener(listener);
					resultButton.removeActionListener(listener);
					applyButton.removeActionListener(listener);
					loadDataButton.removeActionListener(listener);
					saveDataButton.removeActionListener(listener);
					addClassButton.removeActionListener(listener);
				}
			});
		}

		/* 		public void changeDisplayImage(ImagePlus imp){
  			super.getImagePlus().setProcessor(imp.getProcessor());
  			super.getImagePlus().setTitle(imp.getTitle());
  		}
		 */ 	
		
		public void repaintAll()
		{
			this.annotationsPanel.repaint();
			this.imageAndLists.repaint();
			this.buttons.repaint();
			this.all.repaint();
		}
		
		/**
		 * Add new segmentation class (new label and new list on the right side)
		 */
		public void addClass()
		{
			examples[numOfClasses] = new ArrayList<Roi>();
			exampleList[numOfClasses] = new java.awt.List(5);
			exampleList[numOfClasses].setForeground(colors[numOfClasses]);
			
			exampleList[numOfClasses].addActionListener(listener);
			classButton[numOfClasses] = new JRadioButton(classLabels[numOfClasses]);
			classButtonGroup.add(classButton[numOfClasses]);

			boxAnnotation.setConstraints(classButton[numOfClasses], annotationsConstraints);
			annotationsPanel.add(classButton[numOfClasses]);
			annotationsConstraints.gridy++;

			boxAnnotation.setConstraints(exampleList[numOfClasses], annotationsConstraints);
			annotationsPanel.add(exampleList[numOfClasses]);
			annotationsConstraints.gridy++;
			
			// increase number of available classes
			numOfClasses ++;
			
			IJ.log("new number of classes = " + numOfClasses);
			
			repaintAll();
		}
	}

	/**
	 * Plugin run method
	 */
	public void run(String arg) {
		//		trainingImage = IJ.openImage("testImages/i00000-1.tif");
		//get current image
		if (null == WindowManager.getCurrentImage()) {
			trainingImage = IJ.openImage();
			if (null == trainingImage) return; // user canceled open dialog
		}
		else {
			trainingImage = new ImagePlus("training Image",WindowManager.getCurrentImage().getProcessor().duplicate());
		}

		if (Math.max(trainingImage.getWidth(), trainingImage.getHeight()) > 1024)
			if (!IJ.showMessageWithCancel("Warning", "At least one dimension of the image \n" +
					"is larger than 1024 pixels. \n" +
					"Feature stack creation and classifier training \n" +
					"might take some time depending on your computer.\n" +
			"Proceed?"))
				return;


		trainingImage.setProcessor("training image", trainingImage.getProcessor().duplicate().convertToByte(true));
		createFeatureStack(trainingImage);
		

		displayImage = new ImagePlus();
		displayImage.setProcessor("training image", trainingImage.getProcessor().duplicate().convertToRGB());

		ij.gui.Toolbar.getInstance().setTool(ij.gui.Toolbar.FREELINE);

		//Build GUI
		win = new CustomWindow(displayImage);

		//trainingImage.getWindow().setVisible(false);
	}

	/**
	 * Enable / disable buttons
	 * @param s enabling flag
	 */
	private void setButtonsEnabled(Boolean s)
	{
		addExampleButton.setEnabled(s);
		trainButton.setEnabled(s);
		overlayButton.setEnabled(s);
		resultButton.setEnabled(s);
		applyButton.setEnabled(s);
		loadDataButton.setEnabled(s);
		saveDataButton.setEnabled(s);
		addClassButton.setEnabled(s);
	}

	/**
	 * Add examples defined by the user to the corresponding list
	 * @param i list index
	 */
	private void addExamples(int i)
	{
		//IJ.log("add examples in list "+ i + " (numOfClasses = " + numOfClasses + ")");
		//get selected pixels
		Roi r = displayImage.getRoi();
		if (null == r){
			//IJ.log("no ROI");
			return;
		}

		//IJ.log("Before killRoi r = " + r + " examples[i].size + " + examples[i].size());

		displayImage.killRoi();
		examples[i].add(r);
		//IJ.log("added ROI " + r + " to list " + i);
		exampleList[i].add("trace " + traceCounter[i]); 
		traceCounter[i]++;
		drawExamples();
	}


	private void drawExamples()
	{
		if (!showColorOverlay)
			displayImage.setProcessor("Playground", trainingImage.getProcessor().convertToRGB());
		else
			displayImage.setProcessor("Playground", overlayImage.getProcessor().convertToRGB());


		for(int i = 0; i < numOfClasses; i++)
		{
			displayImage.setColor(colors[i]);
			for (Roi r : examples[i]){
				r.drawPixels(displayImage.getProcessor());
				//IJ.log("painted ROI: " + r + " in color "+ colors[i]);
			}
		}
		displayImage.updateAndDraw();
	}

	public void createFeatureStack(ImagePlus img){
		IJ.showStatus("creating feature stack");
		featureStack = new FeatureStack(img);
		featureStack.addDefaultFeatures();
	}


	public void writeDataToARFF(Instances data, String filename){
		try{
			BufferedWriter out = new BufferedWriter(
					new OutputStreamWriter(
							new FileOutputStream( filename) ) );
			try{	
				out.write(data.toString());
				out.close();
			}
			catch(IOException e){IJ.showMessage("IOException");}
		}
		catch(FileNotFoundException e){IJ.showMessage("File not found!");}

	}

	/**
	 * Read ARFF file
	 * @param filename ARFF file name
	 * @return set of instances read from the file
	 */
	public Instances readDataFromARFF(String filename){
		try{
			BufferedReader reader = new BufferedReader(
					new FileReader(filename));
			try{
				Instances data = new Instances(reader);
				// setting class attribute
				data.setClassIndex(data.numAttributes() - 1);
				reader.close();
				return data;
			}
			catch(IOException e){IJ.showMessage("IOException");}
		}
		catch(FileNotFoundException e){IJ.showMessage("File not found!");}
		return null;
	}

	/**
	 * Create training instances out of the user markings
	 * @return set of instances
	 */
	public Instances createTrainingInstances()
	{
		ArrayList<Attribute> attributes = new ArrayList<Attribute>();
		for (int i=1; i<=featureStack.getSize(); i++){
			String attString = featureStack.getSliceLabel(i) + " numeric";
			attributes.add(new Attribute(attString));
		}
		
		final ArrayList<String> classes = new ArrayList<String>();

		int numOfInstances = 0;
		for(int i = 0; i < numOfClasses ; i ++)
		{
			// Do not add empty lists
			if(examples[i].size() > 0)
				classes.add(classLabels[i]);
			numOfInstances += examples[i].size();
		}

		attributes.add(new Attribute("class", classes));

		Instances trainingData =  new Instances("segment", attributes, numOfInstances);

		for(int l = 0; l < numOfClasses; l++)
		{
			for(int j=0; j<examples[l].size(); j++)
			{
				Roi r = examples[l].get(j);
				//need to take care of shapeRois that are represented as multiple polygons
				Roi[] rois;
				if (r instanceof ij.gui.ShapeRoi){
					//IJ.log("shape roi detected");
					rois = ((ShapeRoi) r).getRois();
				}
				else{
					rois = new Roi[1];
					rois[0] = r;
				}

				for(int k=0; k<rois.length; k++){
					int[] x = rois[k].getPolygon().xpoints;
					int[] y = rois[k].getPolygon().ypoints;
					int n = rois[k].getPolygon().npoints;

					for (int i=0; i<n; i++){
						double[] values = new double[featureStack.getSize()+1];
						for (int z=1; z<=featureStack.getSize(); z++){
							values[z-1] = featureStack.getProcessor(z).getPixelValue(x[i], y[i]);
						}
						values[featureStack.getSize()] = (double) l;
						trainingData.add(new DenseInstance(1.0, values));
					}
				}
			}
		}

		return trainingData;
	}

	/**
	 * Train classifier with the current instances
	 */
	public void trainClassifier(){
		if (examples[0].size()==0 & loadedTrainingData==null){
			IJ.showMessage("Cannot train without positive examples!");
			return;
		}
		if (examples[1].size()==0 & loadedTrainingData==null){
			IJ.showMessage("Cannot train without negative examples!");
			return;
		}

		setButtonsEnabled(false);

		IJ.showStatus("training classifier");
		Instances data = null;
		if (0 == examples[0].size() | 0 == examples[1].size())
			IJ.log("Training from loaded data only");
		else {
			long start = System.currentTimeMillis();
			data = createTrainingInstances();
			long end = System.currentTimeMillis();
			IJ.log("Creating training data took: " + (end-start) + "ms");
			data.setClassIndex(data.numAttributes() - 1);
		}

		if (loadedTrainingData != null & data != null){
			IJ.log("merging data");
			for (int i=0; i < loadedTrainingData.numInstances(); i++){
				data.add(loadedTrainingData.instance(i));
			}
			IJ.log("Finished");
		}
		else if (data == null){
			data = loadedTrainingData;
			IJ.log("Taking loaded data as only data...");
		}

		IJ.showStatus("Ttraining classifier...");
		IJ.log("Training classifier...");
		if (null == data){
			IJ.log("WTF");
		}
		
		// Train the classifier on the current data
		try{
			rf.buildClassifier(data);
		}
		catch(Exception e){
			IJ.showMessage(e.getMessage());
		}
		
		//
		if(updateWholeData)
			updateTestSet();

		IJ.log("Classifying whole image...");

		classifiedImage = applyClassifier(wholeImageData, trainingImage.getWidth(), trainingImage.getHeight());

		overlayButton.setEnabled(true);
		resultButton.setEnabled(true);
		applyButton.setEnabled(true);
		showColorOverlay = false;
		toggleOverlay();

		setButtonsEnabled(true);
	}
	
	/**
	 * Update whole data set with current number of classes and features
	 */
	private void updateTestSet() 
	{
		IJ.showStatus("Reading whole image data...");
		
		long start = System.currentTimeMillis();
		ArrayList<String> classNames = new ArrayList<String>();
		for(int i = 0; i < numOfClasses; i++)
			if(examples[i].size() > 0)
				classNames.add(classLabels[i]);
		
		wholeImageData = featureStack.createInstances(classNames);
		long end = System.currentTimeMillis();
		IJ.log("Creating whole image data took: " + (end-start) + "ms");
		wholeImageData.setClassIndex(wholeImageData.numAttributes() - 1);
		
		updateWholeData = false;
	}

	/**
	 * Apply current classifier to set of instances
	 * @param data set of instances
	 * @param w image width
	 * @param h image height
	 * @return result image
	 */
	public ImagePlus applyClassifier(Instances data, int w, int h)
	{
		IJ.showStatus("Classifying image...");
		double[] classificationResult = new double[data.numInstances()];
		for (int i=0; i<data.numInstances(); i++){
			try{
				classificationResult[i] = rf.classifyInstance(data.instance(i));
			}catch(Exception e){
				IJ.showMessage("Could not apply Classifier!");
				e.printStackTrace();
				return null;
			}
		}

		IJ.showStatus("Displaying result...");
		ImageProcessor classifiedImageProcessor = new FloatProcessor(w, h, classificationResult);
		classifiedImageProcessor.convertToByte(true);
		ImagePlus classImg = new ImagePlus("classification result", classifiedImageProcessor);
		return classImg;
	}

	/**
	 * Toggle between overlay and original image with markings
	 */
	void toggleOverlay(){
		showColorOverlay = !showColorOverlay;
		IJ.log("toggel overlay to: " + showColorOverlay);
		if (showColorOverlay){
			//do this every time cause most likely classification changed
			int width = trainingImage.getWidth();
			int height = trainingImage.getHeight();

			ImageProcessor white = new ByteProcessor(width, height);
			white.setMinAndMax(255, 255);

			ImageStack redStack = new ImageStack(width, height);
			redStack.addSlice("red", trainingImage.getProcessor().duplicate());
			ImageStack greenStack = new ImageStack(width, height);
			greenStack.addSlice("green", classifiedImage.getProcessor().duplicate());
			ImageStack blueStack = new ImageStack(width, height);
			blueStack.addSlice("blue", white.duplicate());

			RGBStackMerge merger = new RGBStackMerge();
			ImageStack overlayStack = merger.mergeStacks(trainingImage.getWidth(), trainingImage.getHeight(), 
					1, redStack, greenStack, blueStack, true);

			overlayImage = new ImagePlus("overlay image", overlayStack);
		}

		drawExamples();
	}

	/**
	 * Select a list and deselect the others
	 * @param e item event (originated by a list)
	 * @param i list index
	 */
	void listSelected(final ItemEvent e, final int i)
	{
		drawExamples();
		displayImage.setColor(Color.YELLOW);

		for(int j = 0; j < numOfClasses; j++)
		{
			if (j == i) 
				examples[i].get(exampleList[i].getSelectedIndex()).drawPixels(displayImage.getProcessor());
			else
				exampleList[j].deselect(exampleList[j].getSelectedIndex());
		}

		displayImage.updateAndDraw();
	}

	/**
	 * Delete one of the ROIs
	 * 
	 * @param e action event
	 */
	void deleteSelected(final ActionEvent e){

		for(int i = 0; i < numOfClasses; i++)
		{

			if (e.getSource() == exampleList[i]) {
				//delete item from ROI
				int index = exampleList[i].getSelectedIndex();
				examples[i].remove(index);
				//delete item from list
				exampleList[i].remove(index);
			}
		}

		if (!showColorOverlay)
			drawExamples();
		else{
			//FIXME I have no clue why drawExamples 
			//does not do the trick if overlay is displayed
			toggleOverlay();
			toggleOverlay();
		}
	}

	void showClassificationImage(){
		ImagePlus resultImage = new ImagePlus("classification result", classifiedImage.getProcessor().convertToByte(true).duplicate());
		resultImage.show();
	}

	/**
	 * Apply classifier to test data
	 */
	public void applyClassifierToTestData(){
		ImagePlus testImage = IJ.openImage();
		if (null == testImage) return; // user canceled open dialog

		setButtonsEnabled(false);

		if (testImage.getImageStackSize() == 1){
			applyClassifierToTestImage(testImage).show();
			testImage.show();
		}
		else{
			ImageStack testImageStack = testImage.getStack();
			ImageStack testStackClassified = new ImageStack(testImageStack.getWidth(), testImageStack.getHeight());
			IJ.log("Size: " + testImageStack.getSize() + " " + testImageStack.getWidth() + " " + testImageStack.getHeight());
			for (int i=1; i<=testImageStack.getSize(); i++){
				IJ.log("Classifying image " + i + "...");
				ImagePlus currentSlice = new ImagePlus(testImageStack.getSliceLabel(i),testImageStack.getProcessor(i).duplicate());
				//applyClassifierToTestImage(currentSlice).show();
				testStackClassified.addSlice(currentSlice.getTitle(), applyClassifierToTestImage(currentSlice).getProcessor().duplicate());
			}
			testImage.show();
			ImagePlus showStack = new ImagePlus("Classified Stack", testStackClassified);
			showStack.show();
		}
		setButtonsEnabled(true);
	}

	/**
	 * Apply current classifier to image
	 * @param testImage test image
	 * @return result image
	 */
	public ImagePlus applyClassifierToTestImage(ImagePlus testImage)
	{
		testImage.setProcessor(testImage.getProcessor().convertToByte(true));

		IJ.showStatus("Creating features for test image...");
		final FeatureStack testImageFeatures = new FeatureStack(testImage);
		testImageFeatures.addDefaultFeatures();

		// Set proper class names (skip empty list ones)
		ArrayList<String> classNames = new ArrayList<String>();
		for(int i = 0; i < numOfClasses; i++)
			if(examples[i].size() > 0)
				classNames.add(classLabels[i]);
		
		final Instances testData = testImageFeatures.createInstances(classNames);
		testData.setClassIndex(testData.numAttributes() - 1);

		final ImagePlus testClassImage = applyClassifier(testData, testImage.getWidth(), testImage.getHeight());
		testClassImage.setTitle("classified_" + testImage.getTitle());
		testClassImage.setProcessor(testClassImage.getProcessor().convertToByte(true).duplicate());

		return testClassImage;
	}

	/**
	 * Load previously saved model
	 */
	public void loadTrainingData(){
		OpenDialog od = new OpenDialog("choose data file","");
		if (od.getFileName()==null)
			return;
		IJ.log("load data from " + od.getDirectory() + od.getFileName());
		loadedTrainingData = readDataFromARFF(od.getDirectory() + od.getFileName());
		IJ.log("loaded data: " + loadedTrainingData.numInstances());
	}
	
	/**
	 * Save training model into a file
	 */
	public void saveTrainingData()
	{
		boolean examplesEmpty = true;
		for(int i = 0; i < numOfClasses; i ++)
			if(examples[i].size() > 0)
			{
				examplesEmpty = false;
				break;
			}
		if (examplesEmpty & loadedTrainingData == null){
			IJ.showMessage("There is no data to save");
			return;
		}

		Instances data = createTrainingInstances();
		data.setClassIndex(data.numAttributes() - 1);
		if (null != loadedTrainingData & null != data){
			IJ.log("merging data");
			for (int i=0; i < loadedTrainingData.numInstances(); i++){
				// IJ.log("" + i)
				data.add(loadedTrainingData.instance(i));
			}
			IJ.log("finished");
		}
		else if (null == data)
			data = loadedTrainingData;

		SaveDialog sd = new SaveDialog("choose save file", "data",".arff");
		if (sd.getFileName()==null)
			return;
		IJ.log("writing training data: " + data.numInstances());
		writeDataToARFF(data, sd.getDirectory() + sd.getFileName());
		IJ.log("wrote training data " + sd.getDirectory() + " " + sd.getFileName());
	}
	
	/**
	 * Add new class in the panel (up to MAX_NUM_CLASSES)
	 */
	private void addNewClass() 
	{
		if(numOfClasses == MAX_NUM_CLASSES)
		{
			IJ.showMessage("Trainable Segmentation", "Sorry, maximum number of classes has been reached");
			return;
		}

		IJ.log("Adding new class...");
		
		// Add new class label and list
		win.addClass();
		
		// Repaint window
		SwingUtilities.invokeLater(
				new Runnable() {
					public void run() {
						win.invalidate();
						win.validate();
						win.repaint();
					}
				});
		
		// Force whole data to be updated
		updateWholeData = true;
		
	}

}
