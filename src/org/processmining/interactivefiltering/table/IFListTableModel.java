package org.processmining.interactivefiltering.table;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.IFConstant;
import org.processmining.interactivefiltering.IFModel;

public class IFListTableModel extends AbstractTableModel{

	PluginContext context;
	IFModel model;
	int selectedPattern;
//	DirectFollowModel model;
	ArrayList<String> relationList;
	Map<String, Integer> absFreq;
	Map<String, Double> condFreq;
	public IFListTableModel(PluginContext context, IFModel model) {
		this.context = context;
		this.model = model;
		this.selectedPattern = model.getSelectedPattern();
		
		System.out.println("IN Table Length : " + model.getLengthCondition());
		
		//Getting each freq to build table
		if(selectedPattern == IFConstant.CPP_INT) {
			this.condFreq = model.getCppModel().getProbabilityMap();
			this.relationList = new ArrayList<String>(condFreq.keySet());
//			this.absFreq = model.getCppModel().getAbsFreq();
		} else if(selectedPattern == IFConstant.EFR_INT) {
			this.absFreq = model.getEfrModel().getAbsFreq();
			this.relationList = new ArrayList<String>(absFreq.keySet());
		} else { //DFR_INT
			this.absFreq = model.getDfrModel().getAbsFreq();
			this.relationList = new ArrayList<String>(absFreq.keySet());
		}
	}
	
	public int getRowCount() {
		return relationList.size();
	}

	
	public String getColumnName(int column) {
		//Getting columnNames from model.
		if(selectedPattern == IFConstant.CPP_INT) {
			return model.getCppModel().getColNames().get(column);
		} else if(selectedPattern == IFConstant.EFR_INT) {
			return model.getEfrModel().getColNames().get(column);
		} else { //DFR_INT
			return model.getDfrModel().getColNames().get(column);
		}
	}

	public int getColumnCount() {
		//Getting columnCount from model.
		if(selectedPattern == IFConstant.CPP_INT) {
			return model.getCppModel().getColNames().size();
		} else if(selectedPattern == IFConstant.EFR_INT) {
			return model.getEfrModel().getColNames().size();
		} else { //DFR_INT
			return model.getDfrModel().getColNames().size();
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		
		switch (columnIndex) {
			case 0 :
				if(selectedPattern == IFConstant.CPP_INT) {
					String prefixStr = relationList.get(rowIndex).split("->")[0].replaceAll(">>>", "->");
					return prefixStr.substring(0, prefixStr.length() - 2);
				} else {
					return relationList.get(rowIndex).split("->")[0];
				}
			case 1 :
				return relationList.get(rowIndex).split("->")[1];
			case 2 :
				if(selectedPattern == IFConstant.CPP_INT) {
					Double prob = model.getCppModel().getProbabilityMap().get(relationList.get(rowIndex));
					DecimalFormat df = new DecimalFormat("#0.0000");
					String formattedStr = df.format(prob);
					return formattedStr;
				} else {
					return absFreq.get(relationList.get(rowIndex));	
				}
			default :
				break;
		}
		return null;
	}

}
