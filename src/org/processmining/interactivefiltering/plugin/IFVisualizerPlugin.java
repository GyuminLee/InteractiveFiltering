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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;

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
import org.processmining.interactivefiltering.IFModel;
import org.processmining.interactivefiltering.RelativeLayout;
import org.processmining.interactivefiltering.listener.DataTypeSelectionListener;
import org.processmining.interactivefiltering.model.DirectFollowModel;
import org.processmining.interactivefiltering.table.IFInfoTableModel;
import org.processmining.interactivefiltering.table.IFListTableModel;
import org.processmining.interactivefiltering.table.InfoTableCellRenderer;

import com.fluxicon.slickerbox.components.NiceIntegerSlider;
import com.fluxicon.slickerbox.components.NiceSlider.Orientation;
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
		leftPanel.listPanel.createListTable();
		leftPanel.infoPanel.createInfoTable(0, model.getDfrModel().getExceptionList());//Initially showing the first row information.
		
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
//	AttributeChangeListener attrChangeListener;
	
	JComboBox<String>[] dataTypeSelection;
	DataTypeSelectionListener dataTypeSelectionListener;
	
	//Slider
	NiceIntegerSlider thresholdCategoricalSlider;

	
	// Button (Export, refresh)
	JButton exportButton;
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
		
		ArrayList<String> list = new ArrayList<String>(model.getDfrModel().getAttrSet()); 
		attrCheckBox = new JCheckBox[list.size()];
		dataTypeSelection = new JComboBox[list.size()];
//		attrChangeListener = new AttributeChangeListener(context, model);
		JPanel attrPanel = new JPanel();
		attrPanel.setLayout(new BoxLayout(attrPanel, BoxLayout.Y_AXIS));
	
		for(int i = 0; i < list.size(); i++) {
			String attrName = list.get(i);
			String dataType = model.getDfrModel().getDataTypeMap().get(attrName);
			
			JPanel attrDataTypePanel = new JPanel();
			attrDataTypePanel.setLayout(new BoxLayout(attrDataTypePanel, BoxLayout.X_AXIS));
			attrCheckBox[i] = new JCheckBox(list.get(i));
			attrCheckBox[i].setSelected(true);
			attrCheckBox[i].setEnabled(false);
//			attrCheckBox[i].addActionListener(attrChangeListener);
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
	
		this.addLabelButton = new JButton("Add Label");
		addLabelListener = new AddLabelButtonListener(context, model);
		this.addLabelButton.addActionListener(addLabelListener);
		this.exportButton = new JButton("Export");
		exportListener = new ExportButtonListener(context, model);
		this.exportButton.addActionListener(exportListener);
		this.refreshButton = new JButton("Refresh");
		refreshListener = new RefreshButtonListener(context, model.getDfrModel(), attrCheckBox, dataTypeSelection, thresholdCategoricalSlider, leftPanel );
		this.refreshButton.addActionListener(refreshListener);
		
		/**
		 * Adding Widget to Panel
		 */
		
		//Attribute List
		JLabel attrLabel = new JLabel("Attribute List");
		this.add(attrLabel);
		this.add(scrollPanel, new Float(50));
		
		//Button (Export, Refresh)
		JLabel buttonLabel = new JLabel("Buttons");
		this.add(thresholdCategoricalSlider);
		this.add(exportButton);
		this.add(refreshButton);
		
		
		
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
	
	public void createListTable() {
		System.out.println("Create List Table");
		IFListTableModel tableModel;
		tableModel = new IFListTableModel(context, model.getDfrModel());
		
		JTable jTable = new JTable(tableModel);
		table = jTable;

		jTable.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				int row = table.getSelectedRow();
				
				infoPanel.createInfoTable(row, model.getDfrModel().getExceptionList());				
			}
			public void keyPressed(KeyEvent e) {}
		});
		jTable.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				int row = table.getSelectedRow();
				infoPanel.createInfoTable(row, model.getDfrModel().getExceptionList());
				
				String keyStr = (String) model.getDfrModel().getAbsFreq().keySet().toArray()[row];
				System.out.println("Key String in ListTable : " + keyStr);

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

	public void createInfoTable(final int selectedRow, ArrayList<Integer> exceptionList) {
		this.removeAll();
		this.updateUI();
		IFInfoTableModel tableModel;
		tableModel = new IFInfoTableModel(context, model.getDfrModel(), selectedRow);
		JTable jTable = new JTable(tableModel);
		table = jTable;
		exceptionList = model.getDfrModel().getExceptionList();

		Collections.sort(exceptionList, Collections.reverseOrder());

		jTable.setDefaultRenderer(Object.class, new InfoTableCellRenderer());
		
		jTable.addMouseListener(new MouseListener() {
			public void mouseReleased(MouseEvent e) {
				int row = table.getSelectedRow();
				int col = table.getSelectedColumn();
				if(col == 0) {
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
		leftPanel.infoPanel.createInfoTable(tableModel.getFocusedIndex(), exceptionList);
		
	}

}

class RefreshButtonListener implements ActionListener {
	PluginContext context;
	DirectFollowModel model;
	JCheckBox[] attrCheckBoxList;
	JComboBox[] dataTypeSelectionList;
	NiceIntegerSlider sliderCategorical;
	LeftPanel leftPanel;
	
	public RefreshButtonListener(PluginContext context, DirectFollowModel model, JCheckBox[] attrCheckBoxList, JComboBox<String>[] dataTypeSelectionList, NiceIntegerSlider sliderCategorical, LeftPanel leftPanel) {
		this.context = context;
		this.model = model;
		this.attrCheckBoxList = attrCheckBoxList;
		this.leftPanel = leftPanel;
		this.dataTypeSelectionList = dataTypeSelectionList;
		this.sliderCategorical = sliderCategorical;
		
	}

	public void actionPerformed(ActionEvent e) {
		//Change Attributes
		ArrayList<Integer> exceptionList = new ArrayList<>();
		for(int i = 0; i < attrCheckBoxList.length; i++) {
			if(!attrCheckBoxList[i].isSelected()) {
				exceptionList.add(i);
			}
		}
		
		model.setExceptionList(exceptionList);
		ArrayList<String> attrList = new ArrayList<String>(model.getAttrSet());
		Map<String, String> dataTypeMap = new HashMap<String, String>();
		
		//Change Data Type (Categorical / Numerical)
		for(int i = 0; i < dataTypeSelectionList.length; i++) {
			String selectedItem = dataTypeSelectionList[i].getSelectedItem().toString();
			String attrName = attrList.get(i);
			
			dataTypeMap.put(attrName, selectedItem);
		}
		model.setDataTypeMap(dataTypeMap);
		
		//Change Threshold
		double threshold = ( sliderCategorical.getValue() * 1.0 ) / 100;
		model.setThresholdCategorical(threshold);
		System.out.println("Threshold Changed : " + threshold);
		//Update View
		IFInfoTableModel tableModel = (IFInfoTableModel) leftPanel.infoPanel.table.getModel();
		leftPanel.infoPanel.createInfoTable(tableModel.getFocusedIndex(), exceptionList);
	}
}


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
		// TODO Auto-generated method stub
		ArrayList<XID> filteringList = model.getDfrModel().getFilteringList();
		doLabeling(filteringList, model);
	}
	
	public void doLabeling(ArrayList<XID> filteringList, IFModel model) {
		Set<Integer> removeSet = new HashSet<Integer>();
		
		XAttributeLiteralImpl waitingAttrClean = new XAttributeLiteralImpl("isNoise", "False");
		XAttributeLiteralImpl waitingAttrDirty = new XAttributeLiteralImpl("isNoise", "True");
		
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
		ArrayList<XID> filteringList = model.getDfrModel().getFilteringList();
		doFiltering(filteringList, model);
	}
	
	public void doFiltering(ArrayList<XID> filteringList, IFModel model) {
//		XLog outputLog = (XLog)log.clone();
//		XLog outputLog = log;
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
