/*******************************************************************************
 * This software implements the tracking method described in the following paper: 
 * "A divide and conquer strategy for the maximum likelihood localization of ultra low intensity objects"
 *  By Alexander Krull et Al, 2013. (Enter final journal)
 *
 * Copyright (c) 2012, 2013 Alexandar Krull
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Contributors:
 * 	Alexander Krull (Alexander.Krull@tu-dresden.de)
 *  Damien Ramunno-Johnson (GUI: damienrj@gmail.com)
 *******************************************************************************/
package frameWork.gui.controlWindow;

import ij.IJ;
import ij.gui.GenericDialog;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Sequence;
import frameWork.Session;
import frameWork.Trackable;
import frameWork.gui.HotKeyListener;
import frameWork.gui.MainWindow;
import frameWork.gui.MaxProjectionZ;
import frameWork.gui.ViewModel;
import frameWork.gui.ViewWindow;





public class ControlWindow < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<IT> {

	public ControlWindow(Model <IT> mod, String title, ViewModel <IT> vm) {
		super(mod, title, vm);
	//	go();
		startThread();
	}





	JTextField currentSession;
	JTextField currentMethod;
	ArrayList<Object> sessionList = new ArrayList();
	ArrayList<Object> tableData = new ArrayList();
	JFrame frame;
	JPanel rightPanel;
	JPanel centerPanel;
	JPanel leftPanel;
	JPanel rightButtonPanel;
	JPanel spinnerPanel;
	JMenuItem editSession;
	JButton deleteSession;

	JPanel bottomPanel;
	JPanel pathPanel;
	JList selectSessionList;
	JButton start;
	JTextArea text;
	TableSort trackerTable;
	File currentFolder;
	String currentFolderString;
	JTextField workingFolder;
	volatile JSpinner frameSpinner;
	volatile JSpinner zSpinner;
	volatile JSpinner cSpinner;
	JMenu windowMenu;
	JScrollPane tableScroll;
	Choice changeSession;
	JPanel checkPanel;
	JScrollPane checkScroll;

	List<JCheckBox> cBoxes;
	List<ViewWindow<IT>> windowList;
	private JButton merge;
	private JButton delete;
	private JButton split;
	private JButton trim;
	private JButton saveAll;
	private JMenuItem editBlob;
	private JMenuItem startMenu;
	private JMenuItem deleteSeqMenu;
	private JMenuItem splitSeqMenu;
	private JMenuItem trimSeqMenu;
	private JMenuItem mergeMenu;
	private JMenuItem saveAllMenu;
	private JMenuItem newSessionMenu;
	private JMenuItem exportImagesMenu;
	private JMenuItem toggleOverlay;
	private JMenu viewMenu;
	private JMenuItem contrastMenu;
	private JButton altTracking;
	private JMenuItem altMenu;
	private JMenuItem altOptionMenu;
	private JCheckBoxMenuItem toggleNumbers;
	private JCheckBox autoSaveButton;
	private JButton optimizeFrame;
	private JMenuItem resetWindows;
	private JMenuItem optimizeFrameMenu;
	private JButton loadAllButton;
	private JLabel frameLabel;
	private JButton newSession;
	private JMenuItem onlineHelp;
	private JMenuItem aboutSoftware;


	public void go(){
		cBoxes= new ArrayList<JCheckBox>();
		frame = new JFrame("Control Panel");
		
		
		
		
		frame.setMinimumSize(new Dimension(650, 480));
		frame.setSize(650,480);
		
		frame.addWindowListener(new ControlWindowListener());



		rightPanel = new JPanel();
		leftPanel = new JPanel(new GridLayout(16, 0));

		centerPanel = new JPanel();
		bottomPanel = new JPanel();
		pathPanel = new JPanel();
		pathPanel.setLayout(new BoxLayout(pathPanel, BoxLayout.X_AXIS));
		// Some constants
		Dimension labelDim = new Dimension(60, 28);
		Dimension labelDim2 = new Dimension(60, 22);


		//Bottom Area
		bottomPanel.setBackground(Color.LIGHT_GRAY);

		text = new JTextArea(3,5);

		text.setLineWrap(true);
		text.setText("Ready to start tracking!");
		text.setEditable(false);
		text.append("\n");

		JScrollPane feedbackPanel = new JScrollPane(text);
		feedbackPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		feedbackPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		feedbackPanel.setPreferredSize(new Dimension(100,300));



		//Creating Save and Reload buttons
		saveAll = new JButton("Save");
		saveAll.addActionListener(new SaveAllListener());
		loadAllButton = new JButton("Reload");
		loadAllButton.addActionListener(new LoadAllListener());




		//Create Spinners and control for Frame #, ect. at the top

		//Frame Spinner
		SpinnerModel model1 = new SpinnerNumberModel(1, 1, model.getNumberOfFrames(), 1);
		frameSpinner = new JSpinner(model1);

		frameLabel = new JLabel("Frame #");
		frameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		frameSpinner.setPreferredSize(labelDim2);
		frameSpinner.setMaximumSize(labelDim);

		//Z Spinner
		SpinnerModel zSpinnerModel = new SpinnerNumberModel(1, 1, model.getNumberOfSlices(), 1);
		zSpinner = new JSpinner(zSpinnerModel);


		zSpinner.setPreferredSize(labelDim2);
		zSpinner.setMaximumSize(labelDim);

		JLabel zLabel = new JLabel("Z #");
		zLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);

		//Channel Spinner
		SpinnerModel cSpinnerModel = new SpinnerNumberModel(1, 1, model.getNumberOfChannels(), 1);
		cSpinner = new JSpinner(cSpinnerModel);
		cSpinner.setPreferredSize(labelDim2);
		cSpinner.setMaximumSize(labelDim);

		JLabel cLabel = new JLabel("Channel #");
		cLabel.setAlignmentX(JLabel.RIGHT);

		//Create Spinner Panel, and place spinners
		spinnerPanel = new JPanel();
		spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));
		spinnerPanel.add(Box.createHorizontalGlue());
		//Frame Spinner
		spinnerPanel.add(frameLabel);
		spinnerPanel.add(frameSpinner);
		spinnerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
		//Z Spinner
		spinnerPanel.add(zLabel);
		spinnerPanel.add(zSpinner);
		spinnerPanel.add(Box.createRigidArea(new Dimension(30, 0)));
		//Channel Spinner
		spinnerPanel.add(cLabel);
		spinnerPanel.add(cSpinner);
		spinnerPanel.add(Box.createRigidArea(new Dimension(20, 0)));

		//Autosave button
		autoSaveButton = new JCheckBox("Auto Save", viewModel.getController().isAutoSave());
		autoSaveButton.addItemListener(new AutoSaveListener());
		autoSaveButton.setBorderPainted(false);
		autoSaveButton.setBorder(BorderFactory.createRaisedBevelBorder());
		spinnerPanel.add(autoSaveButton);
		spinnerPanel.add(Box.createHorizontalGlue());



		//Create Right Panel and controls

		rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

		//Current method box label
		JLabel label4 = new JLabel("Current Method");
		label4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(label4);


		//Current Method Box 
		String currentM = "";
		currentMethod = new JTextField();
		currentMethod.setText(currentM);
		currentMethod.setHorizontalAlignment(JTextField.CENTER);
		currentMethod.setMaximumSize(new Dimension(1000, 30));
		currentMethod.setEditable(false);
		rightPanel.add(currentMethod);


		//Session Label
		JLabel changeLabel = new JLabel("Current Session");
		changeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		//Box to select session
		changeSession = new Choice();
		changeSession.setMaximumSize(new Dimension(1000, 80));
		changeSession.addItemListener(new ChangeSessionListener());

		rightPanel.add(changeLabel);
		rightPanel.add(changeSession);

		//Section creates Visible Session Box
		JLabel label5 = new JLabel("Visible Sessions");
		label5.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(label5);

		String[] vis = {"No Sessions"};
		selectSessionList = new JList(vis);
		selectSessionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		//Create CheckBox Panel

		checkPanel = new JPanel();
		checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));
		checkScroll = new JScrollPane(checkPanel);
		checkScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		checkScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		checkScroll.setMinimumSize(new Dimension(50, 200));
		rightPanel.add(checkScroll);

		//Create sub-panel for tracking buttons
		rightButtonPanel = new JPanel(new GridLayout(5,0));

		start = new JButton("Start Tracking");
		start.setAlignmentX(JButton.CENTER_ALIGNMENT);
		start.addActionListener(new StartListener());

		newSession = new JButton("New Session");
		newSession.addActionListener(new NewSessionListener());
		newSession.setAlignmentX(JButton.CENTER_ALIGNMENT);

		deleteSession = new JButton("Delete Session");
		deleteSession.addActionListener(new DeleteSessionListener());
		deleteSession.setAlignmentX(JButton.CENTER_ALIGNMENT);

		optimizeFrame = new JButton("Optimize");
		optimizeFrame.addActionListener(new OptimizeFrameListener());

		rightButtonPanel.add(newSession);
		rightButtonPanel.add(deleteSession);

		altTracking = new JButton("M Tracking");
		altTracking.addActionListener(new AltTrackingListener());
		altTracking.setForeground(Color.blue);
		altTracking.setVisible(false);

		rightButtonPanel.add(optimizeFrame);
		rightButtonPanel.add(altTracking);
		rightButtonPanel.add(start);
		rightButtonPanel.setMaximumSize(new Dimension(1000, 300));


		rightPanel.add(rightButtonPanel);
		rightPanel.revalidate();
		rightPanel.repaint();


		//This controls the center panel
		JButton changeWorking = new JButton("Change Working Directory");



		workingFolder = new JTextField();
		workingFolder.setPreferredSize(new Dimension(300, 20));
		workingFolder.setEditable(false);
		workingFolder.addMouseListener(new changeWorkspaceListener());


		pathPanel.add(workingFolder);

		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));

		//Add the Table
		trackerTable = new TableSort(viewModel, model);
		trackerTable.setOpaque(true);
		trackerTable.setPreferredSize(new Dimension(300, 300));

		//Creating buttons for table

		merge = new JButton("Merge");
		merge.addActionListener(new MergeListener());

		delete = new JButton("Delete");
		delete.addActionListener(new DeleteListener());

		split = new JButton("Split");
		split.addActionListener(new SplitListener());

		trim = new JButton("Trim");
		trim.addActionListener(new TrimListener());


		//Creates Panel for merge, split, trim, and delete buttons
		JPanel traceButtonPanel = new JPanel();


		traceButtonPanel.add(merge);
		traceButtonPanel.add(split);
		traceButtonPanel.add(trim);
		traceButtonPanel.add(delete);

		pathPanel.add(saveAll);
		pathPanel.add(loadAllButton);

		traceButtonPanel.setLayout(new GridLayout(0, 4));
		traceButtonPanel.setMaximumSize(new Dimension(1000, 80));

		spinnerPanel.setMaximumSize(new Dimension(1000, 80));
		pathPanel.setMaximumSize(new Dimension(1000, 80));

		//Put Panels into center panel
		centerPanel.add(spinnerPanel);
		centerPanel.add(pathPanel);
		centerPanel.add(traceButtonPanel);
		centerPanel.add(trackerTable);
		centerPanel.add(feedbackPanel);



		// The is the menu bar area
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		windowMenu = new JMenu("Windows");
		JMenu helpMenu = new JMenu("Help");
		JMenu editMenu = new JMenu("Edit");


		//Menu items

		editSession = new JMenuItem("Session Options");
		editSession.addActionListener(new EditMenuListener());
		editSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.CTRL_MASK));

		editBlob = new JMenuItem("Edit Object");
		editBlob.addActionListener(new BlobMenuListener());
		editBlob.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));

		saveAllMenu = new JMenuItem("Save");
		saveAllMenu.addActionListener(new SaveAllListener());
		saveAllMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK));

		JMenuItem saveTo = new JMenuItem("Save to");
		saveTo.addActionListener(new SaveToListener());

		JMenuItem loadFrom = new JMenuItem("Load from");
		loadFrom.addActionListener(new LoadFromListener());

		JMenuItem loadAll = new JMenuItem("Load");
		loadAll.addActionListener(new LoadAllListener());
		loadAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));

		newSessionMenu = new JMenuItem("New Session");
		newSessionMenu.addActionListener(new NewSessionListener());
		newSessionMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, ActionEvent.CTRL_MASK));

		exportImagesMenu = new JMenuItem("Export images");
		exportImagesMenu.addActionListener(new ExportImagesListener());
		//	exportImagesMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, ActionEvent.CTRL_MASK));

		startMenu = new JMenuItem("Start Tracking");
		startMenu.addActionListener(new StartListener());
		startMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.CTRL_MASK));

		altMenu = new JMenuItem("");
		altMenu.addActionListener(new AltTrackingListener());
		altMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));

		altOptionMenu = new JMenuItem("");
		altOptionMenu.addActionListener(new AltOptionListener());
		altOptionMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.CTRL_MASK));

		deleteSeqMenu = new JMenuItem("Delete Sequence");
		deleteSeqMenu.addActionListener(new DeleteListener());
		deleteSeqMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.CTRL_MASK));

		splitSeqMenu = new JMenuItem("Split Sequence");
		splitSeqMenu.addActionListener(new SplitListener());
		splitSeqMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));

		trimSeqMenu = new JMenuItem("Trim Sequence");
		trimSeqMenu.addActionListener(new TrimListener());
		trimSeqMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.CTRL_MASK));

		mergeMenu = new JMenuItem("Merge Sequence");
		mergeMenu.addActionListener(new MergeListener());
		mergeMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));

		toggleOverlay = new JMenuItem("Toggle Overlay");
		toggleOverlay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.CTRL_MASK));
		toggleOverlay.addActionListener(new ToggleListener());

		optimizeFrameMenu = new JMenuItem("Optimize");
		optimizeFrameMenu.addActionListener(new OptimizeFrameListener());
		optimizeFrameMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, ActionEvent.CTRL_MASK));

		//Put things in file Menu
		fileMenu.setBackground(Color.lightGray);
		fileMenu.add(newSessionMenu);
		fileMenu.add(saveAllMenu);
		fileMenu.add(exportImagesMenu);
		fileMenu.add(saveTo);
		fileMenu.add(loadAll);
		fileMenu.add(loadFrom);


		//Put things in Edit Menu
		editMenu.setBackground(Color.lightGray);
		editMenu.add(deleteSeqMenu);
		editMenu.add(this.splitSeqMenu);
		editMenu.add(trimSeqMenu);
		editMenu.add(mergeMenu);
		editMenu.addSeparator();
		editMenu.add(editBlob);
		editMenu.add(editSession);
		editMenu.add(altOptionMenu);
		editMenu.addSeparator();
		editMenu.add(startMenu);
		editMenu.add(optimizeFrameMenu);
		editMenu.add(altMenu);

		//Window Menu and help Menu Color Set.  Window menu contents controlled elsewhere. Is dynamic
		windowMenu.setBackground(Color.lightGray);
		helpMenu.setBackground(Color.lightGray);



		//View Menu Items
		contrastMenu = new JMenuItem("Adjust Brightness/Contrast");
		contrastMenu.addActionListener(new ContrastListener());
		contrastMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.CTRL_MASK));

		toggleNumbers = new JCheckBoxMenuItem("Show Numbers");
		toggleNumbers.addActionListener(new ToggleNumberListener());
		toggleNumbers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.CTRL_MASK));

		resetWindows = new JMenuItem("Arrange Windows");
		resetWindows.addActionListener(new ResetWindowsListener());
		resetWindows.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));



		//Add Items to View Menu
		viewMenu = new JMenu("View");
		viewMenu.setBackground(Color.lightGray);
		viewMenu.add(contrastMenu);
		viewMenu.add(toggleOverlay);
		viewMenu.add(toggleNumbers);
		viewMenu.addSeparator();
		viewMenu.add(resetWindows);

		//Add Menu Items
		onlineHelp = new JMenuItem("Online Manual");
		onlineHelp.addActionListener(new OnlineHelpListener());

		aboutSoftware = new JMenuItem("About Software");
		aboutSoftware.addActionListener(new AboutSoftwareListener());
		//Add Items to Help Menu
		helpMenu.add(aboutSoftware);
		helpMenu.add(onlineHelp);


		//Place menus into menu bar
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		menuBar.setBackground(Color.lightGray);
		menuBar.setForeground(Color.BLACK);

		//Puts menu bar into frame
		frame.setJMenuBar(menuBar);


		//Make Buttons grayed out from the start
		editSession.setEnabled(false);
		this.changeSession.setEnabled(false);
		this.deleteSession.setEnabled(false);
		this.start.setEnabled(false);

		merge.setEnabled(false);
		delete.setEnabled(false);
		split.setEnabled(false);
		trim.setEnabled(false);
		editBlob.setEnabled(false);
		this.saveAll.setEnabled(false);


		// This puts everything in the frame


		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();


		
		
		frame.getContentPane().add(BorderLayout.EAST, rightPanel);
		frame.getContentPane().add(BorderLayout.CENTER, centerPanel);
		frame.getContentPane().add(BorderLayout.WEST, leftPanel);


		
		
		
		

		//Adds listeners to spinners
		frameSpinner.addChangeListener(new FrameSpinnerListener());
		zSpinner.addChangeListener(new ZSpinnerListener());
		cSpinner.addChangeListener(new CSpinnerListener());


		initFramePosition();
	}



	public void initFramePosition(){
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screen = toolkit.getScreenSize();
		frame.setLocation(frame.getLocation());
		frame.setMinimumSize(new Dimension(650, 380));
		frame.setSize(650,380);
		frame.setVisible(true);	
		frame.setLocation(screen.width-650, 0);
	}


	public JFormattedTextField getTextField(JSpinner spinner) {
		JComponent editor = spinner.getEditor();
		if (editor instanceof JSpinner.DefaultEditor) {
			return ((JSpinner.DefaultEditor)editor).getTextField();
		} else {
			System.err.println("Unexpected editor type: "
					+ spinner.getEditor().getClass()
					+ " isn't a descendant of DefaultEditor");
			return null;
		}
	}



	public void changeSessionDialog() {

		JDialog.setDefaultLookAndFeelDecorated(true);


		Object[] selectionValues = new Object[sessionList.size()];
		sessionList.toArray(selectionValues);
		String initialSelection = "Cell Tracking";
		Object selection = JOptionPane.showInputDialog(null, "Pick session to change to",
				"Tracking Method", JOptionPane.PLAIN_MESSAGE, null, selectionValues, initialSelection);
		currentSession.setText((String) selection);


	} 



	public class ChangeSessionListener implements ItemListener {


		@Override
		public void itemStateChanged(ItemEvent e) {

			int index= changeSession.getSelectedIndex();
			if(index<0){

				return;
			}
			List<Session<? extends Trackable, IT>> tempSessionList = viewModel.getController().getSessions();
			Session<? extends Trackable, IT> session = tempSessionList.get(index);

			int tempChannelNumber = session.getChannelNumnber();

			viewModel.getController().setCurrentSession(session.getId(),viewModel);
			viewModel.setPosition(4, tempChannelNumber);

		}
	}


	public class DeleteSessionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {
			viewModel.getController().deleteSession(viewModel);

		}

	}

	public class NewSessionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {

			viewModel.getController().newSession(viewModel);
		}

	}

	public class ExportImagesListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {

			viewModel.exportImages();

		}

	}

	public class FrameSpinnerListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			int number=( (SpinnerNumberModel)frameSpinner.getModel()).getNumber().intValue()-1;

			viewModel.setPosition(3,number);	
		}


	}

	public class ZSpinnerListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent a) {

			viewModel.setPosition(2,((SpinnerNumberModel)zSpinner.getModel()).getNumber().intValue()-1);

		}

	}

	public class CSpinnerListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent a) {

			viewModel.setPosition(4,((SpinnerNumberModel)cSpinner.getModel()).getNumber().intValue()-1);

		}



	}
	//This Section makes custom refresh method for better performance and syncing 
	@Override
	public synchronized void reFresh(long[] position, boolean rePaintImage) {


		//This section puts frame number in console area
		toggleNumbers.setState(viewModel.isDrawNumbers());

	//	frame.setVisible(false);
		appendText(model.getMsg());
		
	//	frame.setVisible(true);	

		int tempSessionSize = viewModel.getController().getSessions().size();

		if(tempSessionSize > 0){
			currentMethod.setText(viewModel.getController().getCurrentSession().getTypeName());
			if(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking()==null){
				altTracking.setVisible(false);
				altOptionMenu.setVisible(false);
			}else{
				altTracking.setText(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking());
				altMenu.setText("Start " + viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking());
				altOptionMenu.setText("Session " + viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking() + " Options");
				

			}

			if (viewModel.isTracking()){
				start.setText("Stop Tracking");
				start.setForeground(Color.red);
				startMenu.setText("Stop Tracking");
				altMenu.setVisible(false);
				altOptionMenu.setVisible(false);

				altTracking.setVisible(false);
				optimizeFrame.setVisible(false);
			} else {
				start.setText("Start Tracking");
				startMenu.setText("Start Tracking");
				start.setForeground(Color.blue);
				altMenu.setVisible(true);
				altOptionMenu.setVisible(true);
				optimizeFrame.setVisible(true);
				if(!(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking()==null)){
					altTracking.setVisible(true);
				}
			}
			
		}else currentMethod.setText("");
		trackerTable.removeListener();


		workingFolder.setText(viewModel.getController().getWorkspace());

		zSpinner.removeChangeListener(zSpinner.getChangeListeners()[0]);
		zSpinner.setValue((int )(position[2]+1));
		zSpinner.addChangeListener(new ZSpinnerListener());

		frameSpinner.removeChangeListener(frameSpinner.getChangeListeners()[0]);
		frameSpinner.setValue((int )(position[3]+1));
		frameSpinner.addChangeListener(new FrameSpinnerListener());

		cSpinner.removeChangeListener(cSpinner.getChangeListeners()[0]);
		cSpinner.setValue((int )(position[4]+1));
		cSpinner.addChangeListener(new CSpinnerListener());


		if(model.isStruckturalChange()){


			//This will get visible

			List<Sequence<? extends Trackable>> test = viewModel.getVisibleSequences();

			Object[][]trace = new Object[test.size()][8];
			int i = 0;
			for(Sequence<? extends Trackable> seq : test){

				trace[i][0]=seq.getId();
				trace[i][1]=seq.getColor();
				trace[i][2]=seq.getLabel();
				trace[i][3]=seq.getSession().getChannelNumnber()+1;
				trace[i][4]=seq.getSession().getLabel();
				trace[i][5]=seq.getTypeName();
				trace[i][6]=seq.getFirstFrame()+1;
				trace[i][7]=seq.getLastFrame()+1;
				i++;


			}

			trackerTable.updateData(trace);


			List<Session<? extends Trackable, IT>> tempSessionList = viewModel.getController().getSessions();


			int index=0;
			int count=0;

			if(!(viewModel.isTracking())){
				for(Session<? extends Trackable, IT> session : tempSessionList){

					if(session.getId()==viewModel.getController().getCurrentSessionId())
						index=count;

					count++;

				}

				//Takes care of the session list.

				String[] sessionNamesList = new String[tempSessionList.size()];

				cBoxes.clear();
				checkPanel.removeAll();
				changeSession.removeAll();


				i=0;
				for(Session<? extends Trackable, IT> session : tempSessionList){
					sessionNamesList[i]=session.getLabel();

					int tempChannel = session.getChannelNumnber() + 1;

					changeSession.addItem(session.getLabel() + " Ch " + tempChannel);


					JCheckBox temps = new JCheckBox(sessionNamesList[i] + " Ch " + tempChannel);
					checkPanel.add(temps);
					cBoxes.add(temps);


					// check boxes if visible
					JCheckBox currentCBox= cBoxes.get(i);
					currentCBox.setSelected(viewModel.isSessionVisible(session.getId()));
					currentCBox.addActionListener(new VisBoxListener(session));

					i++;
				}
				checkPanel.revalidate();
				checkPanel.repaint();

				//Change Session drop menu

				if(index>0 && changeSession.getItemCount()>0 )
					changeSession.select(index);


				selectSessionList.setListData(sessionNamesList);
			}
			trackerTable.addListener();
		}

		//Make Window list

		windowList = viewModel.getViewWindows();


		windowMenu.removeAll();
		for(ViewWindow<IT> aViewWindow : windowList){

			if(!aViewWindow.showInWindowList()) continue;
			JCheckBoxMenuItem tempMenu = new JCheckBoxMenuItem(aViewWindow.getCaption());
			tempMenu.setState(aViewWindow.isOpen());
			tempMenu.addActionListener(new KymographListener(aViewWindow));

			windowMenu.add(tempMenu);
		}


		if (viewModel.getController().getSessions().size() <= 0){
			editSession.setEnabled(false);
			this.changeSession.setEnabled(false);
			this.deleteSession.setEnabled(false);
			this.start.setEnabled(false);

			merge.setEnabled(false);
			delete.setEnabled(false);
			split.setEnabled(false);
			trim.setEnabled(false);
			editBlob.setEnabled(false);
			this.saveAll.setEnabled(false);
			altMenu.setEnabled(false);
			altMenu.setVisible(false);
			altOptionMenu.setEnabled(false);
			altOptionMenu.setVisible(false);


			startMenu.setEnabled(false);
			altTracking.setVisible(false);
			this.optimizeFrame.setVisible(false);

		}else
		{editSession.setEnabled(true);
		this.changeSession.setEnabled(true);
		this.deleteSession.setEnabled(true);
		this.start.setEnabled(true);
		altMenu.setEnabled(true);
		altOptionMenu.setEnabled(true);
		startMenu.setEnabled(true);
		saveAll.setEnabled(true);

		if(viewModel.getController().getSelectionList().size()==0){
			merge.setEnabled(false);
			delete.setEnabled(false);
			split.setEnabled(false);
			trim.setEnabled(false);
			editBlob.setEnabled(false);

		}else{
			merge.setEnabled(true);
			delete.setEnabled(true);
			split.setEnabled(true);
			trim.setEnabled(true);
			this.saveAll.setEnabled(true);
			editBlob.setEnabled(true);
		}

		}

		// Todo do: this more cleanly
		if(viewModel.getController().getCurrentSession()==null||viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking()==null){
			altMenu.setEnabled(false);
			altOptionMenu.setEnabled(false);
		}
			
	
	}



	//Addes key listeners to all panels
	@Override
	public void addKeyListener(HotKeyListener keyListener) {
		frame.addKeyListener(keyListener);
		trackerTable.addKeyListener(keyListener);
		centerPanel.addKeyListener(keyListener);
		rightPanel.addKeyListener(keyListener);
		bottomPanel.addKeyListener(keyListener);
		leftPanel.addKeyListener(keyListener);


	}


	public class SplitListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Split Stuff
			viewModel.splitSequence();

		}

	}

	public class TrimListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Trims trace
			viewModel.trimSequence();
		}

	}

	public class DeleteListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// Deletes trace
			viewModel.deleteSequence();

		}

	}

	public class StartListener implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// Start Tracking
			viewModel.toggleTracking(false);


		}

	}

	public class changeWorkspaceListener implements MouseListener{


		@Override
		public void mouseClicked(MouseEvent arg0) {
			// TODO Auto-generated method stub

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
			JFileChooser workspace = new JFileChooser();
			workspace.setCurrentDirectory(new java.io.File(viewModel.getController().getWorkspace()));
			workspace.setDialogTitle("Pick Workspace");
			workspace.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			workspace.setAcceptAllFileFilterUsed(false);
			workspace.showDialog(workspace, "Okay");
			File workspaceName = workspace.getSelectedFile();

			viewModel.getController().setWorkspace(workspaceName.toString());
		}

	}
	public class SaveToListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			//Saves all traces
			JFileChooser saveLocation = new JFileChooser();
			saveLocation.setCurrentDirectory(new java.io.File(viewModel.getController().getWorkspace()));
			saveLocation.setDialogTitle("Pick Location to Save Files");
			saveLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			saveLocation.setAcceptAllFileFilterUsed(false);
			saveLocation.showDialog(saveLocation, "Okay");
			viewModel.getController().setWorkspace(saveLocation.getSelectedFile().toString());
			viewModel.saveAll();

		}

	}

	public class SaveAllListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.saveAll();
		}

	}
	public class LoadAllListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.getController().load(viewModel);
		}

	}


	public class LoadFromListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Saves all traces
			JFileChooser loadLocation = new JFileChooser();
			loadLocation.setCurrentDirectory(new java.io.File(viewModel.getController().getWorkspace()));
			loadLocation.setDialogTitle("Pick Location to Load Files From");
			loadLocation.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			loadLocation.setAcceptAllFileFilterUsed(false);
			loadLocation.showDialog(loadLocation, "Okay");
			viewModel.saveAll();

		}

	}
	//Edit Menu Listeners

	public class EditMenuListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.getController().getCurrentSession().showPropertiesDialog();

		}

	}

	public class BlobMenuListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			viewModel.getController().showOjectOptions(viewModel.getCurrentFrameNumber());
		}

	}

	@Override
	public void initWindow() {

	}





	@Override
	public void close() {


	}





	@Override
	public boolean isOpen() {
		return true;
	}





	public class KymographListener implements ActionListener{
		ViewWindow<IT> viewWindow;
		public KymographListener(ViewWindow<IT> aViewWindow){
			viewWindow = aViewWindow;
		}
		@Override
		public void actionPerformed(ActionEvent arg0) {
			if(viewWindow.isOpen()){
				viewWindow.close();
			}else{
				viewWindow.open();
			}
		}

	}
	public class ToggleListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.toggleDrawOverlays();	
		}

	}

	public class AltTrackingListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.toggleTracking(true);
		}

	}

	public class ContrastListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			IJ.doCommand("Brightness/Contrast...");
		}

	}

	public class MergeListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.getController().mergeSequenences();
		}

	}


	public class AltOptionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.getController().getCurrentSession().showAlternatePropertiesDialog();

		}

	}

	public class AboutSoftwareListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
	
			JOptionPane.showMessageDialog(frame,
					"The current version of the software is 1.00"
							+ "\n This software implements the tracking method described in the following paper:" +
								  "\n \"A divide and conquer strategy for the maximum likelihood\n localization of ultra low intensity objects\""
								 + " \nAlexander Krull et Al, 2013. (Enter final journal)"
								  + "\nSoftware is under the GNU General Public License",
							"About Software",
							JOptionPane.PLAIN_MESSAGE);

			
			
		}

	}

	public class OnlineHelpListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {

			try {
				//Set your page url in this string. For eg, I m using URL for Google Search engine
				String url = "http://fiji.sc/Low_Light_Tracking_Tool";
				java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
			}
			catch (java.io.IOException e) {
				System.out.println(e.getMessage());
			}

		}

	}


	public class VisBoxListener implements ActionListener{
		Session<? extends Trackable,IT> session;
		VisBoxListener(Session<? extends Trackable,IT> ses){
			session=ses;
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.toggleSessionTobeDisplayed(session);			

		}

	}

	public class ToggleNumberListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.toggleDrawNumbers();
			viewModel.update(null, null);
		}

	}

	public class AutoSaveListener implements ItemListener{

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			viewModel.getController().toggleAutosave();

		}

	}

	@Override
	public boolean showInWindowList(){
		return false;
	}



	public class OptimizeFrameListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.getController().optimizeFrame(viewModel.getCurrentFrameNumber());

		}

	}

	@Override
	public void open() {

	}

	public void appendText(String arg){

		int diff=text.getText().length()-1000;
		if(diff>0) text.setText(text.getText().substring(text.getText().length()-1000));

		if(arg.length()>0){
			text.append(arg);
			text.select(text.getText().length(), text.getText().length());
		}


	}

	public class ResetWindowsListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.resetWindowsPositions();

		}

	}

	private class ControlWindowListener implements WindowListener{

		@Override
		public void windowActivated(WindowEvent arg0) {

		}

		@Override
		public void windowClosed(WindowEvent arg0) {

		}

		@Override
		public void windowClosing(WindowEvent arg0) {

			viewModel.getController().shutdown(viewModel.getViewWindows());

		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {

		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {

		}

		@Override
		public void windowIconified(WindowEvent arg0) {

		}

		@Override
		public void windowOpened(WindowEvent arg0) {

		}

	}

	@Override
	public void setWindowPosition(MainWindow<IT> mainWindow, MaxProjectionZ<IT> maxZWindow) {




	}


}
