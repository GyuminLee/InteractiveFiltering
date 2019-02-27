package org.processmining.interactivefiltering.table;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XEvent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.model.DirectFollowModel;

public class IFInfoTableModel extends AbstractTableModel {

	PluginContext context;
	DirectFollowModel model;
	ArrayList<String> attrList;
	int selectedRow;
	double thresholdCategorical;
	int rowCnt = 0;
	int colCnt = 0;


	public IFInfoTableModel(PluginContext context, DirectFollowModel model, int selectedRow) {
		this.context = context;
		this.model = model;
		this.attrList = new ArrayList<>(model.getAttrSet());
		this.selectedRow = selectedRow;
		this.thresholdCategorical = model.getThresholdCategorical();
		rowCnt = model.getIsSelectedDataMap().get(model.getAbsFreq().keySet().toArray()[selectedRow]).size();
		colCnt = attrList.size() + 1;

	}

	//Dummy Data
	public int getRowCount() {
		return rowCnt;
		//		return model.getIsSelectedDataMap().get(model.getAbsFreq().keySet().toArray()[selectedRow]).size();
	}


	public Class<?> getColumnClass(int columnIndex) {
		if(columnIndex == 0) {
			return Boolean.class;
		}
		return super.getColumnClass(columnIndex);
	}

	public String getColumnName(int column) {
		if(column == 0) {
			return "Remove?";
		}
		return attrList.get(column - 1);
	}

	public int getColumnCount() {
		return colCnt;
		//		return attrList.size() + 1;
	}

	public void removeColumn(ArrayList<Integer> exceptionList) {
		//		Vector rows = dataVector;

	}


	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex < 1) {
			return true;
		} else {
			return false;
		}
	}



	public Object getValueAt(int rowIndex, int columnIndex) {
		if(columnIndex == 0) {

			boolean isSelected = model.getIsSelectedDataMap().get(model.getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
			return isSelected;
		}
		for(int i = 0; i < model.getAttrSet().size() + 1; i++) {
			if(columnIndex == i + 1) {
				String attr = attrList.get(i);
				XEvent event = model.getEventDataMap().get(model.getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
				if(event.getAttributes().get(attr) != null) {
					return event.getAttributes().get(attr).toString();	
				} else {
					return "";
				}
			}	
		}
		return null;
	}

	public int getFocusedIndex() {
		return selectedRow;
	}
	
	public boolean isSelectedAll() {
		boolean isSelectedAll = true;
		for(int i = 0; i < rowCnt; i++) {
			if(!((boolean) getValueAt(i, 0))) {
				isSelectedAll = false;
			}
		}
		return isSelectedAll;
	}
	
	public void deselectAll() {
		String keyStr = (String) model.getAbsFreq().keySet().toArray()[selectedRow];
		Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getIsSelectedDataMap();
		ArrayList<Boolean> isSelectedList = model.getIsSelectedDataMap().get(keyStr);
		int listSize = isSelectedList.size();
		isSelectedList.clear();
		ArrayList<XID> filteringList = model.getFilteringList();

		for(int i = 0; i < listSize; i++) {
			XEvent event = model.getEventDataMap().get(keyStr).get(i);
			filteringList.remove(event.getID());
			isSelectedList.add(false);
		}
		
		isSelectedDataMap.put(keyStr, isSelectedList);
		
		model.setFilteringList(filteringList);
		model.setIsSelectedDataMap(isSelectedDataMap);
		System.out.println("Filtering List : " + filteringList);

		System.out.println("Deselect all!");
	}
	
	public void selectAll() {
		String keyStr = (String) model.getAbsFreq().keySet().toArray()[selectedRow];
		Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getIsSelectedDataMap();
		ArrayList<Boolean> isSelectedList = model.getIsSelectedDataMap().get(keyStr);
		int listSize = isSelectedList.size();
		isSelectedList.clear();
		ArrayList<XID> filteringList = model.getFilteringList();
		
		for(int i = 0; i < listSize; i++) {
			XEvent event = model.getEventDataMap().get(keyStr).get(i);
			filteringList.add(event.getID());
			isSelectedList.add(true);
		}
		isSelectedDataMap.put(keyStr, isSelectedList);
		
		model.setFilteringList(filteringList);
		model.setIsSelectedDataMap(isSelectedDataMap);
		System.out.println("Filtering List : " + filteringList);

		System.out.println("Select all!");
	}
	
	public boolean isNumericalOutlier(int row, int column) {
		boolean isOutlier = false;
		String attrName = getColumnName(column);
		String value = (String)getValueAt(row, column);
		value = value.replaceAll(",", ".");
		Double numericalValue = Double.parseDouble(value);

		Double upperBound = model.getUpperIQRBoundMap().get(attrName);
		Double lowerBound = model.getLowerIQRBoundMap().get(attrName);

//		System.out.println("===========================");
//		System.out.println("Attr Name : " + attrName);
//		System.out.println("UpperBound : " + upperBound);
//		System.out.println("LowerBound : " + lowerBound);
//		System.out.println("===========================");

		if(numericalValue < lowerBound || numericalValue > upperBound) {
			isOutlier = true;
		}

		return isOutlier;
	}

	public boolean isCategoricalOutlier(int row, int column) {
		boolean isOutlier = false;

		String attrName = getColumnName(column);
		String value = (String)getValueAt(row, column);

		//Decide Value category (Numerical, Categorical..)

		if(model.getAttrValueCountMap().containsKey(attrName + ">>" + value)
				&&
				model.getAttrCountMap().containsKey(attrName)	
				) {
			int attrCnt = model.getAttrCountMap().get(attrName);
			int attrNumCnt = model.getAttrValueCountMap().get(attrName + ">>" + value);
			if((attrNumCnt * 1.0 / attrCnt) < thresholdCategorical) {

//				System.out.println(attrName + " has : " + attrNumCnt * 1.0 / attrCnt + " threshold : " + thresholdCategorical);
				isOutlier = true;
			}
			//			System.out.println(attrName + value + "Relation Percentage : " + (attrNumCnt * 1.0 / attrNumCnt));
		}

		return isOutlier;
	}

}
