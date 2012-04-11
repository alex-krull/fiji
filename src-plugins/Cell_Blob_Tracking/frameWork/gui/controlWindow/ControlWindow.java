package frameWork.gui.controlWindow;

import ij.gui.GenericDialog;

import java.awt.BorderLayout;
import java.awt.Choice;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
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

		text = new JTextArea(5,5);

		text.setLineWrap(true);
		text.setText("Ready to start tracking!");
		text.append("\n");

		JScrollPane feedbackPanel = new JScrollPane(text);
		feedbackPanel.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		feedbackPanel.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);


		//bottomPanel.add(scroller);

		

		
		

		
		
		
		// The controls on the 

		
		//This controls the left Panel
				merge = new JButton("Merge");
				merge.addActionListener(new MergeTrace());


			    delete = new JButton("Delete");
				delete.addActionListener(new DeleteListener());

				//JButton jump = new JButton("Go to  ");
				split = new JButton("Split");
				split.addActionListener(new splitListener());
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




/*
				leftPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				//leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));

				leftPanel.add(new JLabel());
				leftPanel.add(new JLabel());
				leftPanel.add(new JLabel());
				leftPanel.add(new JLabel());

				leftPanel.add(merge);
				leftPanel.add(split);
				leftPanel.add(trim);
				leftPanel.add(delete);

				leftPanel.add(new JLabel());
				//leftPanel.add(jump);
				leftPanel.add(saveAll);
				//leftPanel.add(saveTo);

				leftPanel.add(loadAllButton);
				//leftPanel.add(loadTo);
*/
		
		

		rightPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		//rightPanel.setLayout(new GridLayout(16, 0));
		SpinnerModel model1 = new SpinnerNumberModel();
		frameSpinner = new JSpinner(model1);



		JLabel frameLabel = new JLabel("Frame #");
		frameLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);




		frameSpinner.setPreferredSize(labelDim2);
		frameSpinner.setMaximumSize(labelDim);

		SpinnerModel zSpinnerModel = new SpinnerNumberModel();
		zSpinner = new JSpinner(zSpinnerModel);


		zSpinner.setPreferredSize(labelDim2);
		zSpinner.setMaximumSize(labelDim);
		JLabel zLabel = new JLabel("Z #");
		zLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		

		SpinnerModel cSpinnerModel = new SpinnerNumberModel(20, 0, 40, 1);
		cSpinner = new JSpinner(cSpinnerModel);


		cSpinner.setPreferredSize(labelDim2);
		cSpinner.setMaximumSize(labelDim);
		JLabel cLabel = new JLabel("Channel #");
		cLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);
		
		spinnerPanel = new JPanel();
		
		spinnerPanel.add(frameLabel);
		spinnerPanel.add(frameSpinner);
		spinnerPanel.add(zLabel);
		spinnerPanel.add(zSpinner);
		spinnerPanel.add(cLabel);
		spinnerPanel.add(cSpinner);
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
		checkScroll.setPreferredSize(new Dimension(100, 200));
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
		rightButtonPanel.add(new JLabel(""));
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
		trackerTable.setPreferredSize(new Dimension(300, 100));
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

		JMenuItem saveAllMenu = new JMenuItem("Save");
		saveAllMenu.addActionListener(new SaveAllListener());

		JMenuItem saveTo = new JMenuItem("Save to");
		saveTo.addActionListener(new SaveToListener());

		JMenuItem loadFrom = new JMenuItem("Load from");
		loadFrom.addActionListener(new LoadFromListener());

		JMenuItem loadAll = new JMenuItem("Load");
		loadAll.addActionListener(new LoadAllListener());
		
		JMenuItem newSessionMenu = new JMenuItem("New Session");
		newSessionMenu.addActionListener(new NewSessionListener());
		fileMenu.add(newSessionMenu);
		fileMenu.add(saveAllMenu);
		fileMenu.add(saveTo);
		fileMenu.add(loadAll);
		fileMenu.add(loadFrom);
				
		editMenu.add(editSession);
		
		
		menuBar.add(fileMenu);
		menuBar.add(editMenu);
		menuBar.add(windowMenu);
		menuBar.add(helpMenu);
		menuBar.setBackground(Color.white);
		menuBar.setForeground(Color.BLACK);
		

		frame.setJMenuBar(menuBar);

		// This makes the Dialog for tracking method


		// This puts everything in the frame
		//frame.getContentPane().add(BorderLayout.SOUTH, feedbackPanel);
		frame.getContentPane().add(BorderLayout.EAST, rightPanel);
		frame.getContentPane().add(BorderLayout.CENTER, centerPanel);
		frame.getContentPane().add(BorderLayout.WEST, leftPanel);
		frame.setSize(700,400);
		frame.setVisible(true);
		frame.setMinimumSize(new Dimension(700, 400));

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
		String userSessionName = gd.getNextString();


		//sessionList.add(userSessionName);


		String methodChoice = gd.getNextChoice();
		int channelChoice =Integer.valueOf(gd.getNextChoice());

		currentMethod.setText(methodChoice);

		//String[] newValues = new String[sessionList.size()];
		//sessionList.toArray(newValues);
		//visList.setListData(newValues);




		/*		text.selectAll();
		text.append((String) trace[2][0] + "\n");*/


		viewModel.getController().addSession(methodChoice, userSessionName, channelChoice-1, viewModel);
		int newsessionID = viewModel.getController().getSessions().size()-1;
		viewModel.getController().setCurrentSession(newsessionID, viewModel);
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
				
					viewModel.getController().setCurrentSession(session.getId(),viewModel);
	
					


			
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
	public synchronized void reFresh(long[] position, boolean rePaintImage) {
		//System.out.println("updating Spinners !!!!!!!!!!!");
		//System.out.println("position[3]+1: "+(position[3]+1));

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
	trackerTable.removeListener();
		if (viewModel.isTracking()){
			start.setText("Stop Tracking");
		} else {
			start.setText("Start Tracking");
		}

		//This will get visible


		


			List<Sequence<? extends Trackable>> test = viewModel.getVisibleSequences();




			Object[][]trace = new Object[test.size()][6];
			int i = 0;
			for(Sequence<? extends Trackable> seq : test){

				trace[i][0]=seq.getId();
				trace[i][1]=seq.getColor();
				trace[i][2]=seq.getLabel();
				trace[i][3]=seq.getSession().getLabel();
				trace[i][4]=seq.getTypeName();
				trace[i][5]=seq.getLastFrame()-seq.getFirstFrame()+1;
				i++;


			}

			trackerTable.updateData(trace);


		
			//This section puts frame number in console area
			if(viewModel.isTracking()){
			
			int frameNumber = viewModel.getCurrentFrameNumber();
			
			text.selectAll();
			text.append("Tracking Frame " + frameNumber + "\n");
			}


	
//		int i=0;
		List<Session<? extends Trackable, IT>> tempSessionList = viewModel.getController().getSessions();
		
		
		int index=0;
		int count=0;

	
		for(Session<? extends Trackable, IT> session : tempSessionList){

			if(session.getId()==viewModel.getController().getCurrentSessionId())
				index=count;

			count++;

		}
		

		String[] sessionNamesList = new String[tempSessionList.size()];
		//if(rePopulate){
		cBoxes.clear();
		checkPanel.removeAll();
		changeSession.removeAll();
		//}
		
		i=0;
		for(Session<? extends Trackable, IT> session : tempSessionList){
			sessionNamesList[i]=session.getLabel();
			
		
				changeSession.addItem(session.getLabel());
			

				JCheckBox temps = new JCheckBox(sessionNamesList[i]);
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
			
			this.saveAll.setEnabled(false);

		}else
			{editSession.setEnabled(true);
			this.changeSession.setEnabled(true);
			this.deleteSession.setEnabled(true);
			this.start.setEnabled(true);
			
			if(viewModel.getController().getSelectionList().size()==0){
				merge.setEnabled(false);
				delete.setEnabled(false);
				split.setEnabled(false);
				trim.setEnabled(false);
				this.saveAll.setEnabled(false);
				}else{
					merge.setEnabled(true);
					delete.setEnabled(true);
					split.setEnabled(true);
					trim.setEnabled(true);
					this.saveAll.setEnabled(true);
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

	public class MergeTrace implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent arg0) {
			// TODO Auto-generated method stub
			viewModel.getController().mergeSequenences();
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
