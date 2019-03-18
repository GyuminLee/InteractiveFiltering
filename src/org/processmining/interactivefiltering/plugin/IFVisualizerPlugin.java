package org.processmining.interactivefiltering.plugin;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.contexts.uitopia.annotations.Visualizer;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.interactivefiltering.IFConstant;
import org.processmining.interactivefiltering.IFModel;
import org.processmining.interactivefiltering.RelativeLayout;
import org.processmining.interactivefiltering.model.ConditionalProbabilityModel;
import org.processmining.interactivefiltering.model.DirectFollowModel;
import org.processmining.interactivefiltering.model.EventualFollowModel;
import org.processmining.interactivefiltering.table.IFInfoTableModel;
import org.processmining.interactivefiltering.table.IFListTableModel;
import org.processmining.interactivefiltering.table.InfoTableCellRenderer;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
import com.fluxicon.slickerbox.components.SlickerProgressBar;
import com.fluxicon.slickerbox.factory.SlickerFactory;

@Plugin(name = "Visualize IF",
		parameterLabels = {"Visualizing Filtered Log"},
		returnLabels = {"JPanel"},
		returnTypes = {JPanel.class})
@Visualizer
public class IFVisualizerPlugin {
	@PluginVariant(requiredParameterLabels = {0})
	public static JPanel visualize (PluginContext context, IFModel model) {
		
		return new MainView(context, model);
	}
	
}

/**
 * Parent View
 * @author gyumin
 *
 */
class MainView extends JPanel {
	
	static final int DATA_TYPE_CATEGORICAL = 0;
	static final int DATA_TYPE_NUMERICAL = 1;
	static final int DATA_TYPE_NONE = 2;
	
	PluginContext context;
	IFModel model;
	RightPanel rightPanel;
	LeftPanel leftPanel;
	
	public MainView(PluginContext context, IFModel model) {
		this.context = context;
		this.model = model;
		
		System.out.println("MainView Constructor");
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.X_AXIS);
		rl.setFill(true);
		this.setLayout(rl);

		this.leftPanel = new LeftPanel(context, model, rightPanel);
		this.rightPanel = new RightPanel(context, model, leftPanel);
		
		this.add(leftPanel, new Float(70));
		this.add(rightPanel, new Float(30));
		leftPanel.listPanel.createListTable(IFConstant.DFR_INT);
		leftPanel.infoPanel.createInfoTable(0);//Initially showing the first row information.
		
	}
}

/**
 * This panel shows setting information
 * @author gyumin
 *
 */
class RightPanel extends JPanel {
	PluginContext context;
	LeftPanel leftPanel;	
	IFModel model;
	
	// Attributes Setting
	JCheckBox[] attrCheckBox;
	
	JComboBox<String>[] dataTypeSelection;
	JComboBox<String> patternSelection;
	JLabel conditionLengthLabel;
	JComboBox<Integer> conditionLengthSelection;
	DataTypeSelectionListener dataTypeSelectionListener;
	PatternSelectionListener patternSelectionListener;
	
	//Slider
	NiceIntegerSlider thresholdCategoricalSlider;
	ThresholdCategoricalListener thresholdCategoricalListener;
	
	// Button (Export, refresh)
	JButton exportNoiseLogButton;
	ExportNoiseButtonListener exportNoiseListener;
	JButton exportFilteredLogButton;
	ExportButtonListener exportListener;
	JButton refreshButton;
	RefreshButtonListener refreshListener;
	JButton addLabelButton;
	AddLabelButtonListener addLabelListener;
	
	
	public RightPanel(PluginContext context, IFModel model, LeftPanel leftPanel) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		this.setLayout(rl);

		
		conditionLengthLabel = new JLabel("The length of condition");
		conditionLengthSelection = new JComboBox<Integer>();
		for(int i = 1; i < 5; i++) { // TODO Need to change the length 
			conditionLengthSelection.addItem(i);
		}
		conditionLengthLabel.setVisible(false);
		conditionLengthSelection.setVisible(false);
		conditionLengthSelection.setSelectedItem(2);
		
		patternSelection = new JComboBox<String>();
		patternSelection.addItem(IFConstant.DFR_STRING);
		patternSelection.addItem(IFConstant.CPP_STRING);
		patternSelection.addItem(IFConstant.EFR_STRING);
		patternSelectionListener = new PatternSelectionListener(context, model, leftPanel, patternSelection);
		patternSelection.addActionListener(patternSelectionListener);
		
		ArrayList<String> list = new ArrayList<String>(model.getDfrModel().getAttrSet()); 
		attrCheckBox = new JCheckBox[list.size()];
		dataTypeSelection = new JComboBox[list.size()];
		JPanel attrPanel = new JPanel();
		attrPanel.setLayout(new BoxLayout(attrPanel, BoxLayout.Y_AXIS));
	
		for(int i = 0; i < list.size(); i++) {
			String attrName = list.get(i);
			String dataType = model.getDfrModel().getDataTypeMap().get(attrName);
			
			JPanel attrDataTypePanel = new JPanel();
			attrDataTypePanel.setLayout(new BoxLayout(attrDataTypePanel, BoxLayout.X_AXIS));
			attrCheckBox[i] = new JCheckBox(list.get(i));
			attrCheckBox[i].setSelected(true);
			attrDataTypePanel.add(attrCheckBox[i]);
			dataTypeSelection[i] = new JComboBox<String>();
			dataTypeSelection[i].addItem("Categorical");
			dataTypeSelection[i].addItem("Numerical");
			dataTypeSelection[i].addItem("None");
			if(dataType.equals("Numerical")) {
				dataTypeSelection[i].setSelectedItem("Numerical");
			} else if(dataType.equals("Categorical")) {
				dataTypeSelection[i].setSelectedItem("Categorical");
			} else {
				dataTypeSelection[i].setSelectedItem("None");
			}
			attrDataTypePanel.add(dataTypeSelection[i]);
			attrPanel.add(attrDataTypePanel);
		}
		
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(attrPanel);
		
		this.thresholdCategoricalSlider = SlickerFactory.instance().createNiceIntegerSlider("Set Categorical Threshold", 0, 100, 25, Orientation.HORIZONTAL);
		thresholdCategoricalListener = new ThresholdCategoricalListener(context, model, thresholdCategoricalSlider, leftPanel);
		this.thresholdCategoricalSlider.addChangeListener(thresholdCategoricalListener);
		this.addLabelButton = new JButton("Add the Noise Label");
		addLabelListener = new AddLabelButtonListener(context, model);
		this.addLabelButton.addActionListener(addLabelListener);
		this.exportNoiseLogButton = new JButton("Export the noise Log");
		exportNoiseListener = new ExportNoiseButtonListener(context, model);
		this.exportNoiseLogButton.addActionListener(exportNoiseListener);
		this.exportFilteredLogButton = new JButton("Export the flitered Log");
		exportListener = new ExportButtonListener(context, model);
		this.exportFilteredLogButton.addActionListener(exportListener);
		this.refreshButton = new JButton("Change the Attribute Setting");
		refreshListener = new RefreshButtonListener(context, model, attrCheckBox, dataTypeSelection, thresholdCategoricalSlider, conditionLengthSelection,  patternSelection, conditionLengthLabel, leftPanel);
		this.refreshButton.addActionListener(refreshListener);
		
		/**
		 * Adding Widget to Panel
		 */
		
		//Attribute List
		JLabel attrLabel = new JLabel("Attribute List");
		this.add(attrLabel);
		this.add(scrollPanel, new Float(30));

		JLabel patternLabel = new JLabel("Select Pattern");
		this.add(patternLabel);
		this.add(patternSelection);
		
		this.add(conditionLengthLabel);
		this.add(conditionLengthSelection);

		JLabel refreshLabel = new JLabel("Changed the setting");
		this.add(refreshLabel);
		this.add(refreshButton);

		
		JLabel thresholdLabel = new JLabel("Thresholds");
		this.add(thresholdLabel);
		this.add(thresholdCategoricalSlider);
		
		//Button (Export, Refresh)
		JLabel buttonLabel = new JLabel("Export log");
		this.add(buttonLabel);
		this.add(addLabelButton);
		this.add(exportFilteredLogButton);
		this.add(exportNoiseLogButton);
		
	}
}
/**
 * This panel contains Info, List panel
 * @author gyumin
 *
 */
class LeftPanel extends JPanel {
	PluginContext context;
	IFModel model;
	RightPanel rightPanel;
	ListPanel listPanel;
	InfoPanel infoPanel;
	ArrayList<Integer> exceptionList;
	SelectionCheckBoxChangeListener selectionCheckBoxChangeListener;
	JCheckBox selectionCheckBox;
	IFInfoTableModel tableModel;
	
	public LeftPanel(PluginContext context, IFModel model, RightPanel rightPanel) {
		this.context = context;
		this.model = model;
		
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		this.setLayout(rl);
		
		this.infoPanel = new InfoPanel(context, model, this);
		this.listPanel = new ListPanel(context, model, infoPanel);
		
		JLabel listLabel = new JLabel("Relation List");
		this.add(listLabel);
		this.add(listPanel, new Float(75));
		JLabel infoLabel = new JLabel("Detail of selected event");
		selectionCheckBox = new JCheckBox("Select / Deselect all");
		
		selectionCheckBoxChangeListener = new SelectionCheckBoxChangeListener(context, model, infoPanel, this);
		selectionCheckBox.addActionListener(selectionCheckBoxChangeListener);
		this.add(infoLabel);
		this.add(selectionCheckBox);
		this.add(infoPanel, new Float(25));
		
	}
	
	public void selectCheckBox() {
		IFInfoTableModel tableModel = (IFInfoTableModel) infoPanel.table.getModel();
		if(tableModel.isSelectedAll()) {
			selectionCheckBox.setSelected(true);
		} else {
			selectionCheckBox.setSelected(false);
		}
	}
	
}

/**
 * This panel shows a list of relation 
 * @author Gyumin Lee
 *
 */
class ListPanel extends JPanel {
	PluginContext context;
	IFModel model;
	InfoPanel infoPanel;
	JTable table;
	
	public ListPanel(PluginContext context, IFModel model, InfoPanel infoPanel) {
		this.context = context;
		this.model = model;
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		this.setLayout(rl);
		this.infoPanel = infoPanel;
	}
	
	public void createListTable(int selectedPattern) {
		this.removeAll();
		this.updateUI();
		System.out.println("Create List Table");
		IFListTableModel tableModel;

		model.setSelectedPattern(selectedPattern);
		/*
		if(selectedPattern == IFConstant.CPP_INT) {
			model.setCppModel(new ConditionalProbabilityModel(context, model.getInputLog(), model.getLengthCondition()));
		} else if (selectedPattern == IFConstant.EFR_INT) {
			model.setEfrModel(new EventualFollowModel(context, model.getInputLog()));
		} else {
			model.setDfrModel(new DirectFollowModel(context, model.getInputLog()));
		}
		*/
		tableModel = new IFListTableModel(context, model);		
		JTable jTable = new JTable(tableModel);
		table = jTable;

		jTable.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				int row = table.getSelectedRow();
				int selectedPattern = model.getSelectedPattern();
				
				if(selectedPattern == IFConstant.CPP_INT) {
					infoPanel.createInfoTable(row);
				} else if (selectedPattern == IFConstant.EFR_INT) {
					infoPanel.createInfoTable(row);
				} else {
					infoPanel.createInfoTable(row);				
				}
				
			}
			public void keyPressed(KeyEvent e) {}
		});
		jTable.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				int row = table.getSelectedRow();
				int selectedPattern = model.getSelectedPattern();
				
				if(selectedPattern == IFConstant.CPP_INT) {
					infoPanel.createInfoTable(row);
					String keyStr = (String) model.getCppModel().getAbsFreq().keySet().toArray()[row];
					System.out.println("Key String in ListTable : " + keyStr);
				} else if (selectedPattern == IFConstant.EFR_INT) {
					infoPanel.createInfoTable(row);
					String keyStr = (String) model.getEfrModel().getAbsFreq().keySet().toArray()[row];
					System.out.println("Key String in ListTable : " + keyStr);
				} else {
					infoPanel.createInfoTable(row);
					String keyStr = (String) model.getDfrModel().getAbsFreq().keySet().toArray()[row];
					System.out.println("Key String in ListTable : " + keyStr);
				}
				

			}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
		});
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(jTable);
		this.add(scrollPanel);
//		this.add(scrollPanel, new Float(70));
	}
}
/**
 * This panel shows detail of selected relation
 * @author gyumin
 *
 */
class InfoPanel extends JPanel {
	PluginContext context;
	IFModel model;
	JTable table;
	LeftPanel leftPanel;
//	int selectedRow;
	public InfoPanel(PluginContext context, IFModel model, LeftPanel leftPanel) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		RelativeLayout rl = new RelativeLayout(RelativeLayout.Y_AXIS);
		rl.setFill(true);
		this.setLayout(rl);
	}

	public void createInfoTable(final int selectedRow) {
		this.removeAll();
		this.updateUI();
		IFInfoTableModel tableModel;
		tableModel = new IFInfoTableModel(context, model, selectedRow);
		JTable jTable = new JTable(tableModel);
		table = jTable;

		jTable.setDefaultRenderer(Object.class, new InfoTableCellRenderer());
		
		jTable.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				int row = table.getSelectedRow();
				int col = table.getSelectedColumn();
				int selectedPattern = model.getSelectedPattern();
				if(col == 0) {
					
					if(selectedPattern == IFConstant.CPP_INT) {
						String keyStr = (String)model.getCppModel().getAbsFreq().keySet().toArray()[selectedRow];
						boolean isSelected = model.getCppModel().getIsSelectedDataMap().get(keyStr).get(row);
						
						System.out.println("Key String in infoTable : " + keyStr);
						
						if(isSelected) { //Update the value
							ArrayList<Boolean> list = model.getCppModel().getIsSelectedDataMap().get(keyStr);
							list.remove(row);
							list.add(row, false);
							model.getCppModel().getIsSelectedDataMap().put(keyStr, list);
							
							//Update filtering list
							ArrayList<XID> filteringList = model.getCppModel().getFilteringList();
							XEvent event = model.getCppModel().getEventDataMap().get(keyStr).get(row);
							filteringList.remove((event.getID()));
							model.getCppModel().setFilteringList(filteringList);
							System.out.println("Filtering List : " + filteringList);
							
						} else {
							ArrayList<Boolean> list = model.getCppModel().getIsSelectedDataMap().get(keyStr);
							list.remove(row);
							list.add(row, true);
							model.getCppModel().getIsSelectedDataMap().put(keyStr, list);
							//Update filtering List
							ArrayList<XID> filteringList = model.getCppModel().getFilteringList();
							XEvent event = model.getCppModel().getEventDataMap().get(keyStr).get(row);
							filteringList.add((event.getID()));
							model.getCppModel().setFilteringList(filteringList);
							System.out.println("Filtering List : " + filteringList);
						}
					} else if(selectedPattern == IFConstant.EFR_INT) {
						String keyStr = (String)model.getEfrModel().getAbsFreq().keySet().toArray()[selectedRow];
						boolean isSelected = model.getEfrModel().getIsSelectedDataMap().get(keyStr).get(row);
						
						System.out.println("Key String in infoTable : " + keyStr);
						
						if(isSelected) { //Update the value
							ArrayList<Boolean> list = model.getEfrModel().getIsSelectedDataMap().get(keyStr);
							list.remove(row);
							list.add(row, false);
							model.getEfrModel().getIsSelectedDataMap().put(keyStr, list);
							
							//Update filtering list
							ArrayList<XID> filteringList = model.getEfrModel().getFilteringList();
							XEvent event = model.getEfrModel().getEventDataMap().get(keyStr).get(row);
							filteringList.remove((event.getID()));
							model.getEfrModel().setFilteringList(filteringList);
							System.out.println("Filtering List : " + filteringList);
							
						} else {
							ArrayList<Boolean> list = model.getEfrModel().getIsSelectedDataMap().get(keyStr);
							list.remove(row);
							list.add(row, true);
							model.getEfrModel().getIsSelectedDataMap().put(keyStr, list);
							//Update filtering List
							ArrayList<XID> filteringList = model.getEfrModel().getFilteringList();
							XEvent event = model.getEfrModel().getEventDataMap().get(keyStr).get(row);
							filteringList.add((event.getID()));
							model.getEfrModel().setFilteringList(filteringList);
							System.out.println("Filtering List : " + filteringList);
						}
					} else { //DFR_INT
						String keyStr = (String)model.getDfrModel().getAbsFreq().keySet().toArray()[selectedRow];
						boolean isSelected = model.getDfrModel().getIsSelectedDataMap().get(keyStr).get(row);
						
						System.out.println("Key String in infoTable : " + keyStr);
						
						if(isSelected) { //Update the value
							ArrayList<Boolean> list = model.getDfrModel().getIsSelectedDataMap().get(keyStr);
							list.remove(row);
							list.add(row, false);
							model.getDfrModel().getIsSelectedDataMap().put(keyStr, list);
							
							//Update filtering list
							ArrayList<XID> filteringList = model.getDfrModel().getFilteringList();
							XEvent event = model.getDfrModel().getEventDataMap().get(keyStr).get(row);
							filteringList.remove((event.getID()));
							model.getDfrModel().setFilteringList(filteringList);
							System.out.println("Filtering List : " + filteringList);
							
						} else {
							ArrayList<Boolean> list = model.getDfrModel().getIsSelectedDataMap().get(keyStr);
							list.remove(row);
							list.add(row, true);
							model.getDfrModel().getIsSelectedDataMap().put(keyStr, list);
							//Update filtering List
							ArrayList<XID> filteringList = model.getDfrModel().getFilteringList();
							XEvent event = model.getDfrModel().getEventDataMap().get(keyStr).get(row);
							filteringList.add((event.getID()));
							model.getDfrModel().setFilteringList(filteringList);
							System.out.println("Filtering List : " + filteringList);
						}
					}
				}	
			}
			public void mousePressed(MouseEvent e) {}
			public void mouseExited(MouseEvent e) {}
			public void mouseEntered(MouseEvent e) {}
			public void mouseClicked(MouseEvent e) {}
		});
		
		JScrollPane scrollPanel = new JScrollPane();
		scrollPanel.setViewportView(jTable);
		this.add(scrollPanel, new Float(30));
		
		leftPanel.selectCheckBox();
	}
}

class PatternSelectionListener implements ActionListener {
	PluginContext context;
	IFModel model;
	JComboBox<String> patternSelection;
	LeftPanel leftPanel;
	
	public PatternSelectionListener(PluginContext context, IFModel model, LeftPanel leftPanel, JComboBox<String> patternSelection) {
		this.context = context;
		this.model = model;
		this.patternSelection = patternSelection;
		this.leftPanel = leftPanel;
	}
	
	public void actionPerformed(ActionEvent e) {
		String selectedItem = patternSelection.getSelectedItem().toString();
		int selectedPattern = IFConstant.DFR_INT;
		
		if(selectedItem.equals(IFConstant.CPP_STRING)) {
			selectedPattern = IFConstant.CPP_INT;
			System.out.println("CPP Selected");
		} else if(selectedItem.equals(IFConstant.EFR_STRING)) {
			selectedPattern = IFConstant.EFR_INT;
			System.out.println("EFR Selected");
		}
		System.out.println("Param : " + selectedItem + " / " + selectedPattern);
	}
}

class DataTypeSelectionListener implements ActionListener {

	PluginContext context;
	IFModel model;
	JComboBox<String>[] dataTypeSelection;
	int index;

	public DataTypeSelectionListener(PluginContext context, IFModel model, JComboBox<String>[] dataTypeSelection, int index) {
		this.context = context;
		this.model = model;
		this.dataTypeSelection = dataTypeSelection;
		this.index = index;
	}

	public void actionPerformed(ActionEvent e) {
		String selectedItem = dataTypeSelection[index].getSelectedItem().toString();
		System.out.println("Selected Item : " + selectedItem);
	}
}

class SelectionCheckBoxChangeListener implements ActionListener{

	PluginContext context;
	IFModel model;
	InfoPanel infoPanel;
	JCheckBox[] attrCheckBoxList;
	LeftPanel leftPanel;
	int focusedIdx;
	
	public SelectionCheckBoxChangeListener(PluginContext context, IFModel model, InfoPanel infoPanel, LeftPanel leftPanel) {
		this.context = context;
		this.model = model;
		this.infoPanel = infoPanel;
		this.leftPanel = leftPanel;
	}

	public void actionPerformed(ActionEvent e) {
		System.out.println("Selection is clicked");
		IFInfoTableModel tableModel = (IFInfoTableModel) infoPanel.table.getModel();
		if(tableModel.isSelectedAll()) { 
			tableModel.deselectAll();
		} else {
			tableModel.selectAll();
		}
		ArrayList<Integer> exceptionList = new ArrayList<>();
		leftPanel.infoPanel.createInfoTable(tableModel.getFocusedIndex());
		
	}

}

class RefreshButtonListener implements ActionListener {
	PluginContext context;
	IFModel model;
	JCheckBox[] attrCheckBoxList;
	JComboBox<String>[] dataTypeSelectionList;
	NiceIntegerSlider sliderCategorical;
	JComboBox<String> patternSelection;
	JComboBox<Integer> conditionLengthSelection;
	JLabel conditionLengthLabel;
	
	LeftPanel leftPanel;
	
	SlickerProgressBar progress;
	
	public RefreshButtonListener(PluginContext context, IFModel model, JCheckBox[] attrCheckBoxList, JComboBox<String>[] dataTypeSelectionList, NiceIntegerSlider sliderCategorical, JComboBox<Integer> conditionLengthSelection, JComboBox<String> patternSelection, JLabel conditionLengthLabel, LeftPanel leftPanel) {
		this.context = context;
		this.model = model;
		this.attrCheckBoxList = attrCheckBoxList;
		this.leftPanel = leftPanel;
		this.dataTypeSelectionList = dataTypeSelectionList;
		this.sliderCategorical = sliderCategorical;
		this.patternSelection = patternSelection;
		this.conditionLengthSelection = conditionLengthSelection;
		this.conditionLengthLabel = conditionLengthLabel;
	}

	public void actionPerformed(ActionEvent e) {
		progress = new SlickerProgressBar();
		progress.setVisible(true);
		progress.setIndeterminate(true);
		
		//Change Attributes
		ArrayList<Integer> exceptionList = new ArrayList<>();
		for(int i = 0; i < attrCheckBoxList.length; i++) {
			if(!attrCheckBoxList[i].isSelected()) {
				exceptionList.add(i);
			}
		}
		Collections.sort(exceptionList, Collections.reverseOrder());

		System.out.println("ExceptionList : " + exceptionList);
		
		//if Attributes exception list contains 
		//Remove attributes in the model (in the attributes map)
		ArrayList<String> originalAttrList = new ArrayList<String>(model.getOriginalAttrSet());
		ArrayList<String> exceptionStringList = new ArrayList<String>();
		
		for(int idx : exceptionList) {
			String attrString = originalAttrList.get(idx);
			exceptionStringList.add(attrString);
		}
		
		int selectedPattern = IFConstant.DFR_INT;
		if(patternSelection.getSelectedItem().equals(IFConstant.CPP_STRING)) {
			selectedPattern = IFConstant.CPP_INT;
			model.setCppModel(new ConditionalProbabilityModel(context, model.getInputLog(), model.getLengthCondition(), exceptionStringList));
		
		} else if(patternSelection.getSelectedItem().equals(IFConstant.EFR_STRING)) {
			selectedPattern = IFConstant.EFR_INT;
			model.setEfrModel(new EventualFollowModel(context, model.getInputLog(), exceptionStringList));
		} else { // DFR 
			selectedPattern = IFConstant.DFR_INT;
			model.setDfrModel(new DirectFollowModel(context, model.getInputLog(), exceptionStringList));
		}
		model.setSelectedPattern(selectedPattern);
		
		
		
		if(selectedPattern == IFConstant.CPP_INT) {
			ArrayList<String> attrList = new ArrayList<String>(model.getOriginalAttrSet());
			Map<String, String> dataTypeMap = new HashMap<String, String>();
			
			//Change Data Type (Categorical / Numerical)
			for(int i = 0; i < dataTypeSelectionList.length; i++) {
				String selectedItem = dataTypeSelectionList[i].getSelectedItem().toString();
				String attrName = attrList.get(i);
				
				dataTypeMap.put(attrName, selectedItem);
			}
			model.getCppModel().setDataTypeMap(dataTypeMap);
			
			//Change Threshold
			double threshold = ( sliderCategorical.getValue() * 1.0 ) / 100;
			model.getCppModel().setThresholdCategorical(threshold);
			System.out.println("Threshold Changed : " + threshold);
			
			//Change Condition Length
			model.setLengthCondition(Integer.parseInt(conditionLengthSelection.getSelectedItem().toString()));
			
			//Visibility
			conditionLengthLabel.setVisible(true);
			conditionLengthSelection.setVisible(true);
			
		} else if(selectedPattern == IFConstant.EFR_INT) {
			ArrayList<String> attrList = new ArrayList<String>(model.getOriginalAttrSet());
			Map<String, String> dataTypeMap = new HashMap<String, String>();
			
			//Change Data Type (Categorical / Numerical)
			for(int i = 0; i < dataTypeSelectionList.length; i++) {
				String selectedItem = dataTypeSelectionList[i].getSelectedItem().toString();
				String attrName = attrList.get(i);
				
				dataTypeMap.put(attrName, selectedItem);
			}
			model.getEfrModel().setDataTypeMap(dataTypeMap);
			
			//Change Threshold
			double threshold = ( sliderCategorical.getValue() * 1.0 ) / 100;
			model.getEfrModel().setThresholdCategorical(threshold);
			System.out.println("Threshold Changed : " + threshold);
			//Visibility
			conditionLengthLabel.setVisible(false);
			conditionLengthSelection.setVisible(false);
		} else { //DFR_INT
			ArrayList<String> attrList = new ArrayList<String>(model.getOriginalAttrSet());
			Map<String, String> dataTypeMap = new HashMap<String, String>();
			
			//Change Data Type (Categorical / Numerical)
			for(int i = 0; i < dataTypeSelectionList.length; i++) {
				String selectedItem = dataTypeSelectionList[i].getSelectedItem().toString();
				String attrName = attrList.get(i);
				
				dataTypeMap.put(attrName, selectedItem);
			}
			model.getDfrModel().setDataTypeMap(dataTypeMap);
			
			//Change Threshold
			double threshold = ( sliderCategorical.getValue() * 1.0 ) / 100;
			model.getDfrModel().setThresholdCategorical(threshold);
			System.out.println("Threshold Changed : " + threshold);
			
			//Visibility
			conditionLengthLabel.setVisible(false);
			conditionLengthSelection.setVisible(false);
		}		
		
		//Update List View
		leftPanel.listPanel.createListTable(selectedPattern);
		//Update Info View
		IFInfoTableModel tableModel = (IFInfoTableModel) leftPanel.infoPanel.table.getModel();
		
		//If changed pattern, showing index0 information
		if(model.getPrevPattern() == selectedPattern) {
			leftPanel.infoPanel.createInfoTable(tableModel.getFocusedIndex());
		} else {
			leftPanel.infoPanel.createInfoTable(0);
		}
		model.setPrevPattern(selectedPattern);

		JOptionPane.showMessageDialog(new JFrame(), "Refresh is done", "Done", JOptionPane.CLOSED_OPTION);
	}
}


class ThresholdCategoricalListener implements ChangeListener {
	PluginContext context;
	IFModel model;
	XLog log;
	NiceIntegerSlider sliderCategorical;
	LeftPanel leftPanel;

	public ThresholdCategoricalListener(PluginContext context, IFModel model, NiceIntegerSlider sliderCategorical, LeftPanel leftPanel) {
		this.context = context;
		this.model = model;
		this.leftPanel = leftPanel;
		this.sliderCategorical = sliderCategorical;	
	}
	
	public void stateChanged(ChangeEvent e) {
		double threshold = ( sliderCategorical.getValue() * 1.0 ) / 100;
		
		int selectedPattern = model.getSelectedPattern();
		if(selectedPattern == IFConstant.CPP_INT) {
			model.getCppModel().setThresholdCategorical(threshold);
		} else if(selectedPattern == IFConstant.EFR_INT) {
			model.getEfrModel().setThresholdCategorical(threshold);
		} else {
			model.getDfrModel().setThresholdCategorical(threshold);	
		}
		
		System.out.println("Threshold Changed : " + threshold);
		
		IFInfoTableModel tableModel = (IFInfoTableModel) leftPanel.infoPanel.table.getModel();

		leftPanel.infoPanel.createInfoTable(tableModel.getFocusedIndex());

	}

	
}


//Adding Labeling for finding noise
class AddLabelButtonListener implements ActionListener {
	PluginContext context;
	IFModel model;
	XLog log;
	
	public AddLabelButtonListener(PluginContext context, IFModel model) {
		this.context = context;
		this.model = model;
		this.log = model.getInputLog();
	}
	public void actionPerformed(ActionEvent e) {
		int selectedPattern = model.getSelectedPattern();
		ArrayList<XID> filteringList;
		
		if(selectedPattern == IFConstant.CPP_INT) {
			filteringList = model.getCppModel().getFilteringList();
		} else if(selectedPattern == IFConstant.EFR_INT) {
			filteringList = model.getEfrModel().getFilteringList();
		} else {// DFR
			filteringList = model.getDfrModel().getFilteringList();
		}
		doLabeling(filteringList, model);
		JOptionPane.showMessageDialog(new JFrame(), "Labeling is done", "Done", JOptionPane.CLOSED_OPTION);
	}
	
	public void doLabeling(ArrayList<XID> filteringList, IFModel model) {
		
		XAttributeLiteralImpl waitingAttrClean = new XAttributeLiteralImpl("isNoise", "False");
		XAttributeLiteralImpl waitingAttrDirty = new XAttributeLiteralImpl("isNoise", "True");
		XLog outputLog = (XLog)log.clone();

		for(XTrace trace : outputLog) {
			String eventList = "";
			boolean isNoise = false;
			for(XEvent event : trace) {
				eventList += event.getAttributes().get("concept:name").toString() + ">>";
			}
			String[] eventArray = eventList.split(">>");
			for(int i = 0; i < eventArray.length - 1; i++) {
				String prevEvent = eventArray[i];
				String nextEvent = eventArray[i+1];
				String directFollowRelation = prevEvent + "->" + nextEvent;
				if(model.getDfrModel().getActivityOutlierPercentageMap().get(nextEvent) < 0.07
						&&
					model.getDfrModel().getDirectFollowMap().get(directFollowRelation) * 1.0 / model.getDfrModel().getInputActMap().get(prevEvent) < 0.25
					) {
					isNoise = true;
				}
			}
				
			if(isNoise) {
				trace.getAttributes().put("isNoise", waitingAttrDirty);
			} else {
				trace.getAttributes().put("isNoise", waitingAttrClean);
			}
		}
		

		context.getProvidedObjectManager().createProvidedObject("Labeled Log", outputLog, XLog.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(outputLog)
			.setFavorite(true);
		}
	}
	
}

class ExportNoiseButtonListener implements ActionListener {
	PluginContext context;
	IFModel model;
	XLog log;
	
	public ExportNoiseButtonListener(PluginContext context, IFModel model) {
		this.context = context;
		this.model = model;
		this.log = model.getInputLog();
	}
	
	public void actionPerformed(ActionEvent e) {
		int selectedPattern = model.getSelectedPattern();
		ArrayList<XID> filteringList;
		
		if(selectedPattern == IFConstant.CPP_INT) {
			filteringList = model.getCppModel().getFilteringList();
		} else if(selectedPattern == IFConstant.EFR_INT) {
			filteringList = model.getEfrModel().getFilteringList();
		} else {// DFR
			filteringList = model.getDfrModel().getFilteringList();
		}
		doFiltering(filteringList, model);
		JOptionPane.showMessageDialog(new JFrame(), "Extraction is done", "Done", JOptionPane.CLOSED_OPTION);

	}
	
	public void doFiltering(ArrayList<XID> filteringList, IFModel model) {

		XLog outputLog = (XLog)log.clone();
		outputLog.removeAll(outputLog);
		for(XTrace trace : log) {
			for(XEvent event : trace) {
				if(filteringList.contains(event.getID())) {
					outputLog.add(trace);
					System.out.println("Trace - Noise: " + log.indexOf(trace));

				}
			}
		}
		
		System.out.println("====================================");
		System.out.println("Noise Log is exported!");
		System.out.println("====================================");
		
		context.getProvidedObjectManager().createProvidedObject("Noise Log", outputLog, XLog.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(outputLog)
			.setFavorite(true);
		}
	}
}

class ExportButtonListener implements ActionListener {
	PluginContext context;
	IFModel model;
	XLog log;
	
	public ExportButtonListener(PluginContext context, IFModel model) {
		this.context = context;
		this.model = model;
		this.log = model.getInputLog();
	}
	public void actionPerformed(ActionEvent e) {
		int selectedPattern = model.getSelectedPattern();

		ArrayList<XID> filteringList;
		
		if(selectedPattern == IFConstant.CPP_INT) {
			filteringList = model.getCppModel().getFilteringList();
		} else if(selectedPattern == IFConstant.EFR_INT) {
			filteringList = model.getEfrModel().getFilteringList();
		} else {// DFR
			filteringList = model.getDfrModel().getFilteringList();
		}
		doFiltering(filteringList, model);
		JOptionPane.showMessageDialog(new JFrame(), "Extraction is done", "Done", JOptionPane.CLOSED_OPTION);

	}
	
	public void doFiltering(ArrayList<XID> filteringList, IFModel model) {
		Set<Integer> removeSet = new HashSet<Integer>();
		int noiseCnt = 0;
		int removeCnt = 0;
		XAttributeLiteralImpl waitingAttrClean = new XAttributeLiteralImpl("waitingTime", "clean");
		XAttributeLiteralImpl waitingAttrDirty = new XAttributeLiteralImpl("waitingTime", "dirty");
		System.out.println("Filtering List : " + filteringList);
		
		for(XTrace trace : log) {
			String eventList = "";
			boolean isNoise = false;
			for(XEvent event : trace) {
				eventList += event.getAttributes().get("concept:name").toString() + ">>";
				if(filteringList.contains(event.getID())) {
					removeSet.add(log.indexOf(trace));
					System.out.println("Trace - Noise: " + log.indexOf(trace));
				}
			}
			String[] eventArray = eventList.split(">>");
			
			//TODO add other patterns
			for(int i = 0; i < eventArray.length - 1; i++) {
				String prevEvent = eventArray[i];
				String nextEvent = eventArray[i+1];
				String directFollowRelation = prevEvent + "->" + nextEvent;
				if(model.getDfrModel().getActivityOutlierPercentageMap().get(nextEvent) < 0.07
						&&
					model.getDfrModel().getDirectFollowMap().get(directFollowRelation) * 1.0 / model.getDfrModel().getInputActMap().get(prevEvent) < 0.25
					) {
					isNoise = true;
				}
			}
				
			if(isNoise) {
				trace.getAttributes().put("waitingTime", waitingAttrDirty);
				noiseCnt++;
			} else {
				trace.getAttributes().put("waitingTime", waitingAttrClean);
			}
		}
		XLog outputLog = (XLog)log.clone();
		System.out.println("Remove Set : " + removeSet);
		ArrayList<Integer> removeList = new ArrayList<Integer>(removeSet);
		Collections.sort(removeList, Collections.reverseOrder());
		System.out.println("Remove List : " + removeList.size());
		for(int i = 0; i < removeList.size(); i++) {
//			System.out.println("Remove Item : " + removeList.get(i));
			outputLog.remove(Integer.parseInt(removeList.get(i).toString()));
		}
		
		System.out.println("====================================");
		System.out.println("Noise Count : " + noiseCnt);
		System.out.println("====================================");
		System.out.println("Log is exported!");
		
		context.getProvidedObjectManager().createProvidedObject("Filtered Log", outputLog, XLog.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(outputLog)
			.setFavorite(true);
		}
	}
}
