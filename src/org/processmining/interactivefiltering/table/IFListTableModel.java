package org.processmining.interactivefiltering.table;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.model.DirectFollowModel;

public class IFListTableModel extends AbstractTableModel{

	PluginContext context;
	DirectFollowModel model;
	ArrayList<String> relationList;
	Map<String, Integer> absFreq;
	
	public IFListTableModel(PluginContext context, DirectFollowModel model) {
		this.context = context;
		this.model = model;
		this.absFreq = model.getAbsFreq();
		this.relationList = new ArrayList<String>(absFreq.keySet());
	}
	
	public int getRowCount() {
		return relationList.size();
	}

	
	public String getColumnName(int column) {
		return model.getColNames().get(column);
	}

	public int getColumnCount() {
		// TODO Auto-generated method stub
		return model.getColNames().size();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		switch (columnIndex) {
			case 0 :
				return relationList.get(rowIndex).split("->")[0];
			case 1 :
				return relationList.get(rowIndex).split("->")[1];
			case 2 :
				return absFreq.get(relationList.get(rowIndex));
				
			default :
				break;
		}
		return null;
	}

}
