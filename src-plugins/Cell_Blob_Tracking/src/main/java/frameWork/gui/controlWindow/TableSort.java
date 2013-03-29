/*******************************************************************************
 * This software implements the tracking method descibed in the following paper: 
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
 *     Damien Ramunno-Johnson (GUI)
 *******************************************************************************/


/*
 * TableSort.java requires no other files.
 * 
 * Edited by Damien 12.3.2012
 * 
 */

package frameWork.gui.controlWindow;

import ij.IJ;

import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;

import frameWork.Model;
import frameWork.gui.ViewModel;



//import TableSort.MyTableModel;


public class TableSort extends JPanel {
    private final boolean DEBUG = false;
    private final MyTableModel tableModel;
    private final JTable table;
    private int tableSelected = 0;
    private final ViewModel<?> viewModel;
    private final Model<?> model;
    private final MySelectionListener selectionListener;

  
    
    public TableSort(ViewModel<?> vm, Model<?> m) {
        super(new GridLayout(1,0));
        viewModel=vm;
        model=m;
        //GenericEditor ge= new GenericEditor();

        tableModel = new MyTableModel();
        
        
        Object[][] temp = {};
        
        
        tableModel.setTableData(temp);
        
        table = new JTable(tableModel);
        
        //Makes a selection listener
        selectionListener = new MySelectionListener();
        
   
        
        
        
        table.getSelectionModel().addListSelectionListener(selectionListener);
        table.addMouseListener(selectionListener);
        table.getColumnModel().getColumn(0).setPreferredWidth(30);
        table.getColumnModel().getColumn(3).setPreferredWidth(26);
        
        
        
        
        //table.getColumnModel().getSelectionModel().addListSelectionListener(listener);
        //JTable table = new JTable(new MyTableModel());
        
        
        //table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);


        

        //Set up renderer and editor for the Favorite Color column.
        table.setDefaultRenderer(Color.class,
                                 new ColorRenderer(true));
        table.setDefaultEditor(Color.class,
                               new ColorEditor(viewModel));
        table.getDefaultEditor(String.class).addCellEditorListener(new CellListener());
        
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    
    private class CellListener implements CellEditorListener{

		@Override
		public void editingStopped(ChangeEvent e) {
			
			for(int i=0;i<table.getModel().getRowCount();i++){
			
				int id = (Integer)table.getModel().getValueAt(i, 0);
				String value= (String)table.getModel().getValueAt(i, 2);
				if(!value.equals(model.getSequence(id).getLabel())){
					viewModel.getController().setSqequenceLabel(id , value );
					return;
				}
			}
			
			
		}

		@Override
		public void editingCanceled(ChangeEvent e) {
			IJ.error("Hurra! editing canceled!");
			
		}

		
    	
    	
    }
    
    public void removeListener(){
    	table.getSelectionModel().removeListSelectionListener(selectionListener);
    //	tableModel.removeTableModelListener(modelListener);
    }
    
    public void addListener(){
    	table.getSelectionModel().addListSelectionListener(selectionListener);
   // 	tableModel.addTableModelListener(modelListener);
    }

    public void updateData(Object[][] data){
    	synchronized (viewModel){
    	//table.setAutoCreateRowSorter(false);	
    
    		//if(!(data.length==0)){
    			tableModel.setTableData(data);


    			table.getRowSorter().allRowsChanged();
    			table.revalidate();	
    			table.repaint();

    			//table.setRowSelectionInterval(0, 0);
    			int cnt = 0;
    			table.clearSelection();
    			for(int i=0; i<table.getRowCount();i++){

    				int currentId=(Integer)table.getModel().getValueAt(i, 0);
    				if(viewModel.isSelected( currentId)){
    					int viewIndex= table.convertRowIndexToView(i);
    					table.addRowSelectionInterval(viewIndex, viewIndex);
    				}
    			}
    		
    			
    		//}
    	}

    //	IJ.error(tableModel.getTableModelListeners()[0].getClass().getName());
    }
    
    public Color[] getColor(){
    	Color[] userColor = new Color[table.getModel().getRowCount()];
    	
    	for (int i=0; i< table.getModel().getRowCount(); i++){
    		userColor[i]=(Color) table.getModel().getValueAt(i, 1);


    	}
    	return userColor;
    }
    
    

    
public class MySelectionListener implements MouseListener, ListSelectionListener{

		
    

		@Override
		public void mouseClicked(MouseEvent e) {
	   		
		}

		@Override
		public void mousePressed(MouseEvent e) {
			
			List <Integer> results = new ArrayList<Integer>();
    		for(int i=0; i<table.getRowCount();i++){
    			if(table.isRowSelected(i)&&
    					model.getSequence((Integer)table.getValueAt(i, 0)).getSession().getId()
    					==viewModel.getController().getCurrentSessionId() ){
    				
    				results.add((Integer)table.getValueAt(i, 0));
    				if(e.getClickCount()>1){
    					int f= model.getSequence((Integer)table.getValueAt(i, 0)).getFirstFrame();
    					if(f==viewModel.getCurrentFrameNumber()) 
    						f=model.getSequence((Integer)table.getValueAt(i, 0)).getLastFrame();
    					viewModel.setPosition(3, f);
    				}
    			}
    				
    		}
    		viewModel.setSelectionList(results);
    		tableSelected++;
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			
			
    		
    	
		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			List <Integer> results = new ArrayList<Integer>();
    		for(int i=0; i<table.getRowCount();i++){
    			if(table.isRowSelected(i)&&
    					model.getSequence((Integer)table.getValueAt(i, 0)).getSession().getId()
    					==viewModel.getController().getCurrentSessionId() ){
    				
    				results.add((Integer)table.getValueAt(i, 0));
    			}
    				
    		}
    		viewModel.setSelectionList(results);
    		tableSelected++;
		}
    	
    	
    }
    
    
    class MyTableModel extends AbstractTableModel {
        private final String[] columnNames = {"ID #",
                                        "Color", "Label","Ch",
                                        "Session",
                                        "Method",
                                        "Frist Frame", "Last Frame"};
        public Object[][] data = {
	    {"Blob 1", new Color(255, 0, 0), "Place holder",
	     "Session 1", "Blob", new Integer(5), new Integer(4), new Integer(4)},

        };
        

        public void setTableData(Object[][] temp){
        	    	
       	data = temp;
       }

        @Override
		public int getColumnCount() {
            return columnNames.length;
        }

        @Override
		public int getRowCount() {
            return data.length;
        }

        @Override
		public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
		public Object getValueAt(int row, int col) {
           

        	return data[row][col];

        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @Override
		public Class<?> getColumnClass(int c) {
        	switch (c){
        	case 0: return Integer.class;
        	case 1: return Color.class;
        	case 2: return String.class;
        	case 3: return Integer.class;
        	case 4: return String.class;
        	case 5: return String.class;
        	case 6: return Integer.class;
        	case 7: return Integer.class;
        	default: return null;
        	}
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        @Override
		public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col > 0 && col < 3) {
                return true;
            } else {
                return false;
            }
        }

        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
		public void setValueAt(Object value, int row, int col) {
            if (DEBUG) {
                System.out.println("Setting value at " + row + "," + col
                                   + " to " + value
                                   + " (an instance of "
                                   + value.getClass() + ")");
            }

            data[row][col] = value;
            // Normally, one should call fireTableCellUpdated() when 
            // a value is changed.  However, doing so in this demo
            // causes a problem with TableSorter.  The tableChanged()
            // call on TableSorter that results from calling
            // fireTableCellUpdated() causes the indices to be regenerated
            // when they shouldn't be.  Ideally, TableSorter should be
            // given a more intelligent tableChanged() implementation,
            // and then the following line can be uncommented.
            // fireTableCellUpdated(row, col);

            if (DEBUG) {
                System.out.println("New value of data:");
                printDebugData();
            }
        }

        private void printDebugData() {
            int numRows = getRowCount();
            int numCols = getColumnCount();

            for (int i=0; i < numRows; i++) {
                System.out.print("    row " + i + ":");
                for (int j=0; j < numCols; j++) {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }

    /**
     * Create the GUI and show it.  For thread safety,
     * this method should be invoked from the
     * event-dispatching thread.
     */
    private static void createAndShowGUI() {
        //Create and set up the window.
        JFrame frame = new JFrame("TableSort");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        //Create and set up the content pane.
        /*TableSort newContentPane = new TableSort(viewModel);
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);*/

        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            @Override
			public void run() {
                createAndShowGUI();
            }
        });
    }

	
}