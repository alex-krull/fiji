/*
 * Copyright (c) 1995, 2008, Oracle and/or its affiliates. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *
 *   - Neither the name of Oracle or the names of its
 *     contributors may be used to endorse or promote products derived
 *     from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */ 


/*
 * TableSort.java requires no other files.
 * 
 * Edited by Damien 12.3.2012
 * 
 */

package frameWork.gui.controlWindow;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

//import TableSort.MyTableModel;


public class TableSort extends JPanel {
    private final boolean DEBUG = false;
    private MyTableModel tableModel;
    private JTable table;
    
    
    public TableSort() {
        super(new GridLayout(1,0));

        tableModel = new MyTableModel();
        
        
        Object[][] temp = {
       		    {"Blob 10", new Color(255, 0, 0),
       		     "Session 1", "Blob", new Integer(5)},
       		    {"Blob 2", new Color(150, 0, 0),
       		     "Session 1", "Blob", new Integer(10)},
       		    {"Cell 1", new Color(0, 150, 0),
       		     "Session 1", "Cell", new Integer(15)},
       		    {"Cell 200", new Color(155, 255, 0),
       		     "Session 1", "Cell", new Integer(30)},
       		    {"Cell 3", Color.pink,
       		     "Session 1", "Cell", new Integer(12)}
       	        };
        
        
        tableModel.setTableData(temp);
        
        table = new JTable(tableModel);
        
        
        //JTable table = new JTable(new MyTableModel());
        
        
        table.setPreferredScrollableViewportSize(new Dimension(500, 200));
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);




        //Set up renderer and editor for the Favorite Color column.
        table.setDefaultRenderer(Color.class,
                                 new ColorRenderer(true));
        table.setDefaultEditor(Color.class,
                               new ColorEditor());
        //Add the scroll pane to this panel.
        add(scrollPane);
    }

    public void updateData(Object[][] data){
    	tableModel.setTableData(data);
    	table.repaint();
    }
    
    public Color[] getColor(){
    	Color[] userColor = new Color[table.getModel().getRowCount()];
    	
    	for (int i=0; i< table.getModel().getRowCount(); i++){
    		userColor[i]=(Color) table.getModel().getValueAt(i, 1);


    	}
    	return userColor;
    }
    
    
    class MyTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Trace Name",
                                        "Color",
                                        "Session",
                                        "Tracking Method",
                                        "Trace Length"};
        public Object[][] data = {
	    {"Blob 1", new Color(255, 0, 0),
	     "Session 1", "Blob", new Integer(5)},
	    {"Blob 2", new Color(150, 0, 0),
	     "Session 1", "Blob", new Integer(10)},
	    {"Cell 1", new Color(0, 150, 0),
	     "Session 1", "Cell", new Integer(15)},
	    {"Cell 2", new Color(0, 255, 0),
	     "Session 1", "Cell", new Integer(30)},
	    {"Cell 3", Color.pink,
	     "Session 1", "Cell", new Integer(12)}
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
		public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }

        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        @Override
		public boolean isCellEditable(int row, int col) {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 2) {
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
        TableSort newContentPane = new TableSort();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);

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