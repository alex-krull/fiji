package frameWork.gui.controlWindow;

import ij.IJ;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.JRadioButton;
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
import frameWork.gui.ViewModel;
import frameWork.gui.ViewWindow;





public class ControlWindow < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<IT> {

	public ControlWindow(Model <IT> mod, String title, ViewModel <IT> vm) {
		super(mod, title, vm);
		go();
		startThread();
	}


	/**
	 * @param args
	 */


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
	private JMenuItem toggleOverlay;
	private JMenu viewMenu;
	private JMenuItem contrastMenu;
	private JButton altTracking;
	private JMenuItem altMenu;
	private JMenuItem altOptionMenu;
	private JCheckBoxMenuItem toggleNumbers;
	private JCheckBox autoSaveButton;

	/*
	public static void main(String[] args) {
		ControlWindow2 window = new ControlWindow2();
		window.go();
	}
	 */



	public void go(){
		cBoxes= new ArrayList<JCheckBox>();
		frame = new JFrame("Control Window");
		frame.addWindowListener(new ControlWindowListener());

		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


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
		//text.setMinimumSize(new Dimension(40,200));

		text.setLineWrap(true);
		text.setText("Ready to start tracking!");
		text.append("\n");

		JScrollPane feedbackPanel = new JScrollPane(text);
		feedbackPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		feedbackPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		feedbackPanel.setPreferredSize(new Dimension(100,300));


		//bottomPanel.add(scroller);









		// The controls on the 


		//This controls the left Panel
		merge = new JButton("Merge");
		merge.addActionListener(new MergeListener());


		delete = new JButton("Delete");
		delete.addActionListener(new DeleteListener());

		//JButton jump = new JButton("Go to  ");
		split = new JButton("Split");
		split.addActionListener(new SplitListener());
		//split.setAlignmentX(JButton.CENTER_ALIGNMENT);
		trim = new JButton("Trim");
		trim.addActionListener(new TrimListener());

		//JButton saveTo = new JButton("Save To");
		//saveTo.addActionListener(new SaveToListener());

		//JButton loadTo = new JButton("Reload From");
		//loadTo.addActionListener(new LoadFromListener());

		saveAll = new JButton("Save");
		saveAll.addActionListener(new SaveAllListener());
		JButton loadAllButton = new JButton("Reload");
		loadAllButton.addActionListener(new LoadAllListener());








		rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		//rightPanel.setLayout(new GridLayout(16, 0));
		SpinnerModel model1 = new SpinnerNumberModel(1, 1, model.getNumberOfFrames(), 1);
		frameSpinner = new JSpinner(model1);



		JLabel frameLabel = new JLabel("Frame #");
		frameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);




		frameSpinner.setPreferredSize(labelDim2);
		frameSpinner.setMaximumSize(labelDim);

		SpinnerModel zSpinnerModel = new SpinnerNumberModel(1, 1, model.getNumberOfSlices(), 1);
		zSpinner = new JSpinner(zSpinnerModel);


		zSpinner.setPreferredSize(labelDim2);
		zSpinner.setMaximumSize(labelDim);
		JLabel zLabel = new JLabel("Z #");
		zLabel.setAlignmentX(JLabel.RIGHT_ALIGNMENT);


		SpinnerModel cSpinnerModel = new SpinnerNumberModel(1, 1, model.getNumberOfChannels(), 1);
		cSpinner = new JSpinner(cSpinnerModel);


		cSpinner.setPreferredSize(labelDim2);
		cSpinner.setMaximumSize(labelDim);
		JLabel cLabel = new JLabel("Channel #");
		cLabel.setAlignmentX(JLabel.RIGHT);

		spinnerPanel = new JPanel();
		spinnerPanel.setLayout(new BoxLayout(spinnerPanel, BoxLayout.X_AXIS));

		spinnerPanel.add(Box.createHorizontalGlue());
		spinnerPanel.add(frameLabel);
		spinnerPanel.add(frameSpinner);
		spinnerPanel.add(Box.createRigidArea(new Dimension(30, 0)));

		spinnerPanel.add(zLabel);
		spinnerPanel.add(zSpinner);
		spinnerPanel.add(Box.createRigidArea(new Dimension(30, 0)));

		spinnerPanel.add(cLabel);
		spinnerPanel.add(cSpinner);
		spinnerPanel.add(Box.createRigidArea(new Dimension(20, 0)));
		
		autoSaveButton = new JCheckBox("Auto Save", viewModel.getController().isAutoSave());
		autoSaveButton.addItemListener(new AutoSaveListener());
		
		autoSaveButton.setBorderPainted(false);
		autoSaveButton.setBorder(BorderFactory.createRaisedBevelBorder());
		
		spinnerPanel.add(autoSaveButton);
		
		spinnerPanel.add(Box.createHorizontalGlue());
		//Create the tracking method pair.

		JLabel label4 = new JLabel("Current Method");
		label4.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(label4);

		String currentM = "";




		currentMethod = new JTextField();
		currentMethod.setText(currentM);
		currentMethod.setHorizontalAlignment(JTextField.CENTER);
		currentMethod.setMaximumSize(new Dimension(1000, 30));
		currentMethod.setEditable(false);



		rightPanel.add(currentMethod);



		changeSession = new Choice();
		changeSession.setMaximumSize(new Dimension(1000, 80));
		changeSession.addItemListener(new ChangeSessionListener());
		//	changeSession.addActionListener(new ChangeSessionListener());

		JLabel changeLabel = new JLabel("Current Session");
		changeLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(changeLabel);
		rightPanel.add(changeSession);

		/*JLabel labelWhich = new JLabel("Pick Session ");
		labelWhich.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(labelWhich);

		String[] whichSession = {"Session1", "Session2", "Session3"}; //get month names
        SpinnerListModel trackMethodsModel = null;

        SpinnerListModel whichSessionModel = new SpinnerListModel(whichSession);

        JSpinner sessionSpinner = new JSpinner(whichSessionModel);
        sessionSpinner.setPreferredSize(new Dimension(90, 25));
        sessionSpinner.setMaximumSize(new Dimension(90, 25));



        sessionSpinner.setAlignmentX(JSpinner.CENTER_ALIGNMENT);

        JFormattedTextField ftf = getTextField(sessionSpinner);
        ftf.setHorizontalAlignment(JTextField.CENTER);


        rightPanel.add(sessionSpinner);*/



		JLabel label5 = new JLabel("Visible Sessions");
		label5.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(label5);

		String[] vis = {"No Sessions"};



		selectSessionList = new JList(vis);
		//visList.setVisibleRowCount(3);
		//visList.setBorder(BorderFactory.createLineBorder(Color.black));
		selectSessionList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane visScroller = new JScrollPane(selectSessionList);
		visScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		visScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		//visScroller.setMaximumSize(new Dimension(1000, 100));

		//rightPanel.add(visScroller);

		checkPanel = new JPanel();
		checkPanel.setLayout(new BoxLayout(checkPanel, BoxLayout.Y_AXIS));

		//checkPanel.setPreferredSize(new Dimension(100, 100));





		checkScroll = new JScrollPane(checkPanel);
		checkScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		checkScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		checkScroll.setMinimumSize(new Dimension(50, 200));
		rightPanel.add(checkScroll);

		rightButtonPanel = new JPanel(new GridLayout(4,0));

		start = new JButton("Start Tracking");
		start.setAlignmentX(JButton.CENTER_ALIGNMENT);
		start.addActionListener(new StartListener());



		JButton newSession = new JButton("New Session");
		newSession.addActionListener(new NewSessionListener());
		newSession.setAlignmentX(JButton.CENTER_ALIGNMENT);


		deleteSession = new JButton("Delete Session");
		deleteSession.addActionListener(new DeleteSessionListener());
		deleteSession.setAlignmentX(JButton.CENTER_ALIGNMENT);


		/*JButton changeSession = new JButton("Change Session");
		//deleteSession.addActionListener(new NewSessionListener());
		changeSession.setAlignmentX(JButton.CENTER_ALIGNMENT);
		changeSession.addActionListener(new ChangeSessionListener());*/



		rightButtonPanel.add(newSession);
		rightButtonPanel.add(deleteSession);
		//rightButtonPanel.add(new JLabel(""));
		
		altTracking = new JButton("M Tracking");
		altTracking.addActionListener(new AltTrackingListener());
		altTracking.setForeground(Color.blue);
		altTracking.setVisible(false);
		
		rightButtonPanel.add(altTracking);
		rightButtonPanel.add(start);
		rightButtonPanel.setMaximumSize(new Dimension(1000, 300));
				
		
		
		
		
		
		
		//rightButtonPanel.add(changeSession);

		rightPanel.add(rightButtonPanel);
		







		//This controls the center panel
		JButton changeWorking = new JButton("Change Working Directory");
		//changeWorking.addActionListener(new changeWorkspaceListener());


		workingFolder = new JTextField();
		workingFolder.setPreferredSize(new Dimension(300, 20));
		workingFolder.setEditable(false);
		workingFolder.addMouseListener(new changeWorkspaceListener());






		pathPanel.add(workingFolder);
		//urlPanel.add(changeWorking);
		//urlPanel.setBackground(Color.blue);

		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));




		trackerTable = new TableSort(viewModel, model);
		trackerTable.setOpaque(true);
		//trackerTable.setPreferredSize(new Dimension(300, 100));
		/*
		tableScroll = new JScrollPane(trackerTable);
		tableScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		tableScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);*/


		//urlPanel.setBackground(Color.blue);
		//spinnerPanel.setBackground(Color.DARK_GRAY);

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


		//Edit items

		editSession = new JMenuItem("Edit Session Defaults");
		editSession.addActionListener(new EditMenuListener());
		editSession.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, ActionEvent.SHIFT_MASK));

		editBlob = new JMenuItem("Edit Object");
		editBlob.addActionListener(new BlobMenuListener());
		editBlob.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.SHIFT_MASK));

		saveAllMenu = new JMenuItem("Save");
		saveAllMenu.addActionListener(new SaveAllListener());
		saveAllMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.SHIFT_MASK));

		JMenuItem saveTo = new JMenuItem("Save to");
		saveTo.addActionListener(new SaveToListener());

		JMenuItem loadFrom = new JMenuItem("Load from");
		loadFrom.addActionListener(new LoadFromListener());

		JMenuItem loadAll = new JMenuItem("Load");
		loadAll.addActionListener(new LoadAllListener());
		loadAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.SHIFT_MASK));

		newSessionMenu = new JMenuItem("New Session");
		newSessionMenu.addActionListener(new NewSessionListener());
		newSessionMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.SHIFT_MASK));


		startMenu = new JMenuItem("Start Tracking");
		startMenu.addActionListener(new StartListener());
		startMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, ActionEvent.SHIFT_MASK));

		altMenu = new JMenuItem("");
		altMenu.addActionListener(new AltTrackingListener());
		altMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.SHIFT_MASK));
		
		altOptionMenu = new JMenuItem("");
		altOptionMenu.addActionListener(new AltOptionListener());
		altOptionMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, ActionEvent.SHIFT_MASK));
		
		deleteSeqMenu = new JMenuItem("Delete Sequence");
		deleteSeqMenu.addActionListener(new DeleteListener());
		deleteSeqMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, ActionEvent.SHIFT_MASK));

		splitSeqMenu = new JMenuItem("Split Sequence");
		splitSeqMenu.addActionListener(new SplitListener());
		splitSeqMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.SHIFT_MASK));

		trimSeqMenu = new JMenuItem("Trim Sequence");
		trimSeqMenu.addActionListener(new TrimListener());
		trimSeqMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, ActionEvent.SHIFT_MASK));

		mergeMenu = new JMenuItem("Merge Sequence");
		mergeMenu.addActionListener(new MergeListener());
		mergeMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.SHIFT_MASK));

		toggleOverlay = new JMenuItem("Toggle Overlay");
		toggleOverlay.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, ActionEvent.SHIFT_MASK));
		toggleOverlay.addActionListener(new ToggleListener());

		fileMenu.setBackground(Color.lightGray);
		fileMenu.add(newSessionMenu);
		fileMenu.add(saveAllMenu);
		fileMenu.add(saveTo);
		fileMenu.add(loadAll);
		fileMenu.add(loadFrom);

		editMenu.setBackground(Color.lightGray);


		editMenu.add(deleteSeqMenu);
		editMenu.add(this.splitSeqMenu);
		editMenu.add(trimSeqMenu);
		editMenu.add(mergeMenu);
		editMenu.addSeparator();

		editMenu.add(editSession);
		editMenu.add(editBlob);
		editMenu.add(altOptionMenu);
		editMenu.addSeparator();
		editMenu.add(startMenu);
		editMenu.add(altMenu);
		windowMenu.setBackground(Color.lightGray);
		helpMenu.setBackground(Color.lightGray);




		contrastMenu = new JMenuItem("Adjust Brightness/Contrast");
		contrastMenu.addActionListener(new ContrastListener());
		contrastMenu.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, ActionEvent.SHIFT_MASK));
		
		toggleNumbers = new JCheckBoxMenuItem("Toggle Numbers");
		toggleNumbers.addActionListener(new ToggleNumberListener());
		toggleNumbers.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, ActionEvent.SHIFT_MASK));


		viewMenu = new JMenu("View Menu");
		viewMenu.setBackground(Color.lightGray);
		viewMenu.add(contrastMenu);
		viewMenu.add(toggleOverlay);
		viewMenu.add(toggleNumbers);
		viewMenu.addSeparator();


		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(viewMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		menuBar.setBackground(Color.lightGray);
		menuBar.setForeground(Color.BLACK);


		frame.setJMenuBar(menuBar);

		// This makes the Dialog for tracking method

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
		//frame.getContentPane().add(BorderLayout.SOUTH, feedbackPanel);
		frame.getContentPane().add(BorderLayout.EAST, rightPanel);
		frame.getContentPane().add(BorderLayout.CENTER, centerPanel);
		frame.getContentPane().add(BorderLayout.WEST, leftPanel);
		frame.setSize(650,350);
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(650, 350));

		//frame.setFocusable(true);
		//HotKeyListener keyListener = new HotKeyListener(viewModel);
		//frame.addKeyListener(keyListener);
		//trackerTable.addKeyListener(keyListener);
		//trackerTable.setFocusable(true);

		frameSpinner.addChangeListener(new FrameSpinnerListener());
		zSpinner.addChangeListener(new ZSpinnerListener());
		cSpinner.addChangeListener(new CSpinnerListener());

		
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


		//Object[] selectionValues = { "Session 1", "Session 2", "Session 3" };

		Object[] selectionValues = new Object[sessionList.size()];
		sessionList.toArray(selectionValues);
		String initialSelection = "Cell Tracking";
		Object selection = JOptionPane.showInputDialog(null, "Pick session to change to",
				"Tracking Method", JOptionPane.PLAIN_MESSAGE, null, selectionValues, initialSelection);
		currentSession.setText((String) selection);


		//Object[] possibilities = {"ham", "spam", "yam"};
		//String s = (String)JOptionPane.showInputDialog(frame);

	} 



	public class ChangeSessionListener implements ItemListener {


		@Override
		public void itemStateChanged(ItemEvent e) {

			int index= changeSession.getSelectedIndex();
			if(index<0){
				//    	viewModel.getController().setCurrentSession(-1);
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
			//newSessionDialog();

			viewModel.getController().newSession(viewModel);
			



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

	@Override
	public synchronized void reFresh(long[] position, boolean rePaintImage) {
	
		//System.out.println("position[3]+1: "+(position[3]+1));
		
		//This section puts frame number in console area
		toggleNumbers.setState(viewModel.isDrawNumbers());
		
		appendText(model.getMsg());
		
		if(viewModel.isTracking()){

			int frameNumber = viewModel.getCurrentFrameNumber();

			text.selectAll();
			text.append("Tracking Frame " + frameNumber + "\n");
		}
		
		int tempSessionSize = viewModel.getController().getSessions().size();
		
		if(tempSessionSize > 0){
			currentMethod.setText(viewModel.getController().getCurrentSession().getTypeName());
			if(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking()==null){
				altTracking.setVisible(false);
				altOptionMenu.setVisible(false);
			}else{
			altTracking.setText(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking());
			altMenu.setText("Start " + viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking());
			altOptionMenu.setText(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking() + " Options");
			altTracking.setVisible(true);
			altOptionMenu.setVisible(true);
			
			}
			
			if (viewModel.isTracking()){
				start.setText("Stop Tracking");
				start.setForeground(Color.red);
				startMenu.setText("Stop Tracking");
				altMenu.setVisible(false);
				altOptionMenu.setVisible(false);
				
				altTracking.setVisible(false);;
			} else {
				start.setText("Start Tracking");
				startMenu.setText("Start Tracking");
				start.setForeground(Color.blue);
				altMenu.setVisible(true);
				altOptionMenu.setVisible(true);
				if(!(viewModel.getController().getCurrentSession().getPolicy().getLabelForAlternateTracking()==null)){
				altTracking.setVisible(true);
				}
			}
		}else currentMethod.setText("");
		trackerTable.removeListener();


		workingFolder.setText(viewModel.getController().getWorkspace());

		//if(((SpinnerNumberModel)zSpinner.getModel()).getNumber().intValue()-1 !=position[2]+1){
		zSpinner.removeChangeListener(zSpinner.getChangeListeners()[0]);
		zSpinner.setValue((int )(position[2]+1));
		zSpinner.addChangeListener(new ZSpinnerListener());
		//}

		//if(((SpinnerNumberModel)frameSpinner.getModel()).getNumber().intValue()-1 !=position[3]+1){
		frameSpinner.removeChangeListener(frameSpinner.getChangeListeners()[0]);
		frameSpinner.setValue((int )(position[3]+1));
		frameSpinner.addChangeListener(new FrameSpinnerListener());
		//}

		//if(((SpinnerNumberModel)cSpinner.getModel()).getNumber().intValue()-1 !=position[4]+1){
		cSpinner.removeChangeListener(cSpinner.getChangeListeners()[0]);
		cSpinner.setValue((int )(position[4]+1));
		cSpinner.addChangeListener(new CSpinnerListener());
		//}

		if(model.isStruckturalChange()){


			//This will get visible





			List<Sequence<? extends Trackable>> test = viewModel.getVisibleSequences();




			Object[][]trace = new Object[test.size()][7];
			int i = 0;
			for(Sequence<? extends Trackable> seq : test){

				trace[i][0]=seq.getId();
				trace[i][1]=seq.getColor();
				trace[i][2]=seq.getLabel();
				trace[i][3]=seq.getSession().getChannelNumnber()+1;
				trace[i][4]=seq.getSession().getLabel();
				trace[i][5]=seq.getTypeName();
				trace[i][6]=seq.getLastFrame()-seq.getFirstFrame()+1;
				i++;


			}

			trackerTable.updateData(trace);







			//		int i=0;
			List<Session<? extends Trackable, IT>> tempSessionList = viewModel.getController().getSessions();


			int index=0;
			int count=0;


			for(Session<? extends Trackable, IT> session : tempSessionList){

				if(session.getId()==viewModel.getController().getCurrentSessionId())
					index=count;

				count++;

			}

			//Takes care of the session list.

			String[] sessionNamesList = new String[tempSessionList.size()];
			//if(rePopulate){
			cBoxes.clear();
			checkPanel.removeAll();
			changeSession.removeAll();
			//}

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


		if (viewModel.getController().getCurrentSessionId()==-1){
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

		}else
		{editSession.setEnabled(true);
		this.changeSession.setEnabled(true);
		this.deleteSession.setEnabled(true);
		this.start.setEnabled(true);
		altMenu.setEnabled(true);
		altOptionMenu.setEnabled(true);
		startMenu.setEnabled(true);
		this.altTracking.setEnabled(true);

		if(viewModel.getController().getSelectionList().size()==0){
			merge.setEnabled(false);
			delete.setEnabled(false);
			split.setEnabled(false);
			trim.setEnabled(false);
			//this.saveAll.setEnabled(false);
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





	}




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
			// Deletes tace
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
			// TODO Saves all traces
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
			// TODO Auto-generated method stub
			viewModel.saveAll();
		}

	}
	public class LoadAllListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub

	}





	@Override
	public void close() {
		// TODO Auto-generated method stub

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
			// TODO Auto-generated method stub
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
			// TODO Auto-generated method stub
			viewModel.toggleDrawOverlays();	
		}

	}
	
public class AltTrackingListener implements ActionListener{

	@Override
	public void actionPerformed(ActionEvent arg0) {
		// TODO Auto-generated method stub
		viewModel.toggleTracking(true);
	}
	
}

	public class ContrastListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			IJ.doCommand("Brightness/Contrast...");
		}

	}

	public class MergeListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			viewModel.getController().mergeSequenences();
		}

	}
	
	
	public class AltOptionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			viewModel.getController().getCurrentSession().showAlternatePropertiesDialog();
			
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





	@Override
	public void open() {
		// TODO Auto-generated method stub

	}

	public void appendText(String arg){
		text.append(arg + "\n");
	}

	private class ControlWindowListener implements WindowListener{

		@Override
		public void windowActivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowClosed(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowClosing(WindowEvent arg0) {
			for(ViewWindow<IT> vw: viewModel.getViewWindows()){
				vw.terminate();
			}

		}

		@Override
		public void windowDeactivated(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowDeiconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowIconified(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

		@Override
		public void windowOpened(WindowEvent arg0) {
			// TODO Auto-generated method stub

		}

	}




}
