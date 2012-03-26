package frameWork.gui.controlWindow;

import ij.gui.GenericDialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.imglib2.type.NativeType;
import net.imglib2.type.numeric.NumericType;
import net.imglib2.type.numeric.RealType;
import frameWork.Model;
import frameWork.Sequence;
import frameWork.Trackable;
import frameWork.gui.HotKeyListener;
import frameWork.gui.ViewModel;
import frameWork.gui.ViewWindow;





public class ControlWindow < IT extends  NumericType<IT> & NativeType<IT> & RealType<IT> > extends ViewWindow<IT> {

	public ControlWindow(Model <IT>mod, String title, ViewModel <IT> vm) {
		super(mod, title, vm);
		// TODO Auto-generated constructor stub
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
	JPanel bottomPanel;
	JList visList;
	JButton start;
	JTextArea text;
	TableSort trackerTable;
	
	volatile JSpinner frameSpinner;
	volatile JSpinner zSpinner;
	volatile JSpinner cSpinner;
/*
	public static void main(String[] args) {
		ControlWindow2 window = new ControlWindow2();
		window.go();
	}
*/
	
	public void go(){
		frame = new JFrame("Control Window");
		
		//frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		rightPanel = new JPanel();
		leftPanel = new JPanel();
		centerPanel = new JPanel();
		bottomPanel = new JPanel();
		// Some constants
		Dimension labelDim = new Dimension(60, 28);
		Dimension labelDim2 = new Dimension(60, 19);


		//Bottom Area
		bottomPanel.setBackground(Color.LIGHT_GRAY);

		text = new JTextArea(5,5);

		text.setLineWrap(true);
		text.setText("Ready to start tracking!");
		text.append("\n");

		JScrollPane scroller = new JScrollPane(text);
		scroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


		//bottomPanel.add(scroller);

		//This controls the left Panel
		JButton merge = new JButton("Merge");
		JButton delete = new JButton("Delete");
		delete.addActionListener(new DeleteListener());
		
		JButton jump = new JButton("Go to  ");
		JButton split = new JButton("Split");
		split.addActionListener(new splitListener());
		//split.setAlignmentX(JButton.CENTER_ALIGNMENT);
		JButton trim = new JButton("Trim");
		trim.addActionListener(new TrimListener());

		JButton saveAll = new JButton("Save All");
		saveAll.addActionListener(new SaveAllListener());


		leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));


		leftPanel.add(merge);
		leftPanel.add(split);
		leftPanel.add(trim);
		leftPanel.add(delete);
		leftPanel.add(jump);
		leftPanel.add(saveAll);


		// The controls on the right

		rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

		SpinnerModel model1 = new SpinnerNumberModel();
		frameSpinner = new JSpinner(model1);
		
		

		JLabel frameLabel = new JLabel("Frame #");
		frameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);


		rightPanel.add(frameLabel);
		rightPanel.add(frameSpinner);

		frameSpinner.setPreferredSize(labelDim2);
		frameSpinner.setMaximumSize(labelDim);

		SpinnerModel model2 = new SpinnerNumberModel();
		zSpinner = new JSpinner(model2);
		
		
		zSpinner.setPreferredSize(labelDim2);
		zSpinner.setMaximumSize(labelDim);
		JLabel label2 = new JLabel("Z #");
		label2.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(label2);
		rightPanel.add(zSpinner);

		SpinnerModel model3 = new SpinnerNumberModel(20, 0, 40, 1);
		cSpinner = new JSpinner(model3);

		
		cSpinner.setPreferredSize(labelDim2);
		cSpinner.setMaximumSize(labelDim);
		JLabel label3 = new JLabel("Channel #");
		label3.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(label3);
		rightPanel.add(cSpinner);

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

		JLabel labelSession = new JLabel("Current Session");
		labelSession.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		rightPanel.add(labelSession);

		String currentS = "";

		currentSession = new JTextField();
		currentSession.setText(currentS);
		currentSession.setHorizontalAlignment(JTextField.CENTER);
		currentSession.setMaximumSize(new Dimension(1000, 30));
		currentSession.setEditable(false);


		rightPanel.add(currentSession);

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
		

		
		visList = new JList(vis);
		//visList.setVisibleRowCount(3);
		//visList.setBorder(BorderFactory.createLineBorder(Color.black));
		visList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

		JScrollPane visScroller = new JScrollPane(visList);
		visScroller.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		visScroller.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		visScroller.setMaximumSize(new Dimension(1000, 100));
		
		rightPanel.add(visScroller);
		
		start = new JButton("Start Tracking");
		start.setAlignmentX(JButton.CENTER_ALIGNMENT);
		start.addActionListener(new StartListener());
		rightPanel.add(start);

		JButton newSession = new JButton("New Session");
		newSession.addActionListener(new NewSessionListener());
		newSession.setAlignmentX(JButton.CENTER_ALIGNMENT);
		rightPanel.add(newSession);

		JButton deleteSession = new JButton("Delete Session");
		//deleteSession.addActionListener(new NewSessionListener());
		deleteSession.setAlignmentX(JButton.CENTER_ALIGNMENT);
		rightPanel.add(deleteSession);

		JButton changeSession = new JButton("Change Session");
		//deleteSession.addActionListener(new NewSessionListener());
		changeSession.setAlignmentX(JButton.CENTER_ALIGNMENT);
		changeSession.addActionListener(new ChangeSessionListener());
		rightPanel.add(changeSession);

		//This controls the center panel

		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));




		trackerTable = new TableSort(viewModel, model);
		trackerTable.setOpaque(true);
		
		

		centerPanel.add(trackerTable);

		JTextArea details = new JTextArea("Place holder");
		Border raisedetched = BorderFactory.createEtchedBorder(EtchedBorder.RAISED);
		details.setBorder(raisedetched);
		centerPanel.add(details);

		// The is the menu bar area
		JMenuBar menuBar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");

		JMenuItem newMenuItem = new JMenuItem("New");
		fileMenu.add(newMenuItem);
		JMenuItem saveAllMenu = new JMenuItem("Save all");
		saveAllMenu.addActionListener(new SaveAllListener());
		fileMenu.add(saveAllMenu);
		
		menuBar.add(fileMenu);
		menuBar.setBackground(Color.white);
		menuBar.setForeground(Color.BLACK);

		frame.setJMenuBar(menuBar);

		// This makes the Dialog for tracking method


		// This puts everything in the frame
		frame.getContentPane().add(BorderLayout.SOUTH, scroller);
		frame.getContentPane().add(BorderLayout.EAST, rightPanel);
		frame.getContentPane().add(BorderLayout.CENTER, centerPanel);
		frame.getContentPane().add(BorderLayout.WEST, leftPanel);
		frame.setSize(1000,600);
		frame.setVisible(true);
		
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

	//This creates the newSessionDialog
	public void newSessionDialog() {

		JDialog.setDefaultLookAndFeelDecorated(true);

		//String[] trackingMethods = {"Blob", "Cell"};
		String[] trackingMethods = viewModel.getController().getPossibleSessionTypes();


		GenericDialog gd = new GenericDialog("New Session");
		gd.addStringField("Enter new session name: ", "");
		gd.addChoice("Pick tracking method", trackingMethods, null);
		gd.showDialog();

		String temp = gd.getNextString();
		sessionList.add(temp);

		String choices = gd.getNextChoice();
		
		currentMethod.setText(choices);
		
		String[] newValues = new String[sessionList.size()];
		sessionList.toArray(newValues);
		visList.setListData(newValues);
		
		
		
	     
/*		text.selectAll();
		text.append((String) trace[2][0] + "\n");*/
		
		
		viewModel.getController().addSession(choices, temp);
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



	public class ChangeSessionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent s) {
			changeSessionDialog();



		}

	}


	public class NewSessionListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent a) {
			newSessionDialog();



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
	public synchronized void rePaint(long[] position, boolean rePaintImage) {
		System.out.println("updating Spinners !!!!!!!!!!!");
		System.out.println("position[3]+1: "+(position[3]+1));
		
		zSpinner.setValue(position[2]+1);
		frameSpinner.setValue(position[3]+1);
		cSpinner.setValue(position[4]+1);
		
		if (viewModel.isTracking()){
			start.setText("Stop Tracking");
		} else {
			start.setText("Start Tracking");
		}
		
		//This will get visible

		
		if (viewModel.isTracking()==false){
			
		
		List<Sequence<? extends Trackable>> test = viewModel.getVisibleSequences();
		
		
		
		 
		Object[][]trace = new Object[test.size()][5];
		int i = 0;
		for(Sequence<? extends Trackable> seq : test){
			
			trace[i][0]=seq.getId();
			trace[i][1]=seq.getColor();
			trace[i][2]=seq.getTypeName();
			trace[i][3]=seq.getTypeName();
			trace[i][4]=seq.getLastFrame()-seq.getFirstFrame()+1;
			i++;
		}
		
		trackerTable.updateData(trace);
		} else {
			//This section puts frame number in console area
			Long frameNumber = (Long) frameSpinner.getModel().getValue();
			
			text.selectAll();
			text.append("Tracking Frame " + frameNumber + "\n");
		}
/*		 i = 0;
		Color[] tempColor = trackerTable.getColor();
		for(Sequence<? extends Trackable> seq : test){
			
			
			seq.setColor(tempColor[i]);
			i++;
		}*/
		
		
		//viewModel.getTCsToBeDisplayed();
		
		
		
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
	
	
	public class splitListener implements ActionListener {

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
			viewModel.toggleTracking();
														
						
		}
		
	}
	public class SaveAllListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Saves all traces
			viewModel.saveAll();
			
		}
		
	}
}
