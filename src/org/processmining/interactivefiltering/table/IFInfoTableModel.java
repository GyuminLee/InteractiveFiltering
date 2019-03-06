package org.processmining.interactivefiltering.table;

import java.util.ArrayList;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XEvent;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.IFConstant;
import org.processmining.interactivefiltering.IFModel;

public class IFInfoTableModel extends AbstractTableModel {

	PluginContext context;
	IFModel model;
	//	DirectFollowModel model;
	int selectedPattern;

	ArrayList<String> attrList;
	int selectedRow;
	double thresholdCategorical;
	int rowCnt = 0;
	int colCnt = 0;


	public IFInfoTableModel(PluginContext context, IFModel model, int selectedRow) {
		this.context = context;
		this.model = model;
		this.selectedPattern = model.getSelectedPattern();
		this.selectedRow = selectedRow;

		if(selectedPattern == IFConstant.CPP_INT) {
			this.attrList = new ArrayList<>(model.getCppModel().getAttrSet());
			this.thresholdCategorical = model.getCppModel().getThresholdCategorical();
			rowCnt = model.getCppModel().getIsSelectedDataMap().get(model.getCppModel().getAbsFreq().keySet().toArray()[selectedRow]).size();

		} else if(selectedPattern == IFConstant.EFR_INT) {
			this.attrList = new ArrayList<>(model.getEfrModel().getAttrSet());
			this.thresholdCategorical = model.getEfrModel().getThresholdCategorical();
			rowCnt = model.getEfrModel().getIsSelectedDataMap().get(model.getEfrModel().getAbsFreq().keySet().toArray()[selectedRow]).size();

		} else { //DFR_INT
			this.attrList = new ArrayList<>(model.getDfrModel().getAttrSet());
			this.thresholdCategorical = model.getDfrModel().getThresholdCategorical();
			rowCnt = model.getDfrModel().getIsSelectedDataMap().get(model.getDfrModel().getAbsFreq().keySet().toArray()[selectedRow]).size();
		}

		colCnt = attrList.size() + 1;

	}


	public int getRowCount() {
		return rowCnt;
	}


	public Class<?> getColumnClass(int columnIndex) {
		//Fisrt column always shows checkbox.
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
	}

	public void removeColumn(ArrayList<Integer> exceptionList) {
		//TODO implement?
	}


	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if(columnIndex < 1) {
			return true;
		} else {
			return false;
		}
	}



	public Object getValueAt(int rowIndex, int columnIndex) {

		if(selectedPattern == IFConstant.CPP_INT) {
			if(columnIndex == 0) {

				boolean isSelected = model.getCppModel().getIsSelectedDataMap().get(model.getCppModel().getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
				return isSelected;
			}
			for(int i = 0; i < model.getCppModel().getAttrSet().size() + 1; i++) {
				if(columnIndex == i + 1) {
					String attr = attrList.get(i);
					XEvent event = model.getCppModel().getEventDataMap().get(model.getCppModel().getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
					if(event.getAttributes().get(attr) != null) {
						return event.getAttributes().get(attr).toString();	
					} else {
						return "";
					}
				}	
			}
		} else if(selectedPattern == IFConstant.EFR_INT) {
			if(columnIndex == 0) {

				boolean isSelected = model.getEfrModel().getIsSelectedDataMap().get(model.getEfrModel().getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
				return isSelected;
			}
			for(int i = 0; i < model.getEfrModel().getAttrSet().size() + 1; i++) {
				if(columnIndex == i + 1) {
					String attr = attrList.get(i);
					XEvent event = model.getEfrModel().getEventDataMap().get(model.getEfrModel().getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
					if(event.getAttributes().get(attr) != null) {
						return event.getAttributes().get(attr).toString();	
					} else {
						return "";
					}
				}	
			}
		} else { //DFR_INT
			if(columnIndex == 0) {

				boolean isSelected = model.getDfrModel().getIsSelectedDataMap().get(model.getDfrModel().getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
				return isSelected;
			}
			for(int i = 0; i < model.getDfrModel().getAttrSet().size() + 1; i++) {
				if(columnIndex == i + 1) {
					String attr = attrList.get(i);
					XEvent event = model.getDfrModel().getEventDataMap().get(model.getDfrModel().getAbsFreq().keySet().toArray()[selectedRow]).get(rowIndex);
					if(event.getAttributes().get(attr) != null) {
						return event.getAttributes().get(attr).toString();	
					} else {
						return "";
					}
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

		if(selectedPattern == IFConstant.CPP_INT) {
			String keyStr = (String) model.getCppModel().getAbsFreq().keySet().toArray()[selectedRow];
			Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getCppModel().getIsSelectedDataMap();
			ArrayList<Boolean> isSelectedList = model.getCppModel().getIsSelectedDataMap().get(keyStr);
			int listSize = isSelectedList.size();
			isSelectedList.clear();
			ArrayList<XID> filteringList = model.getCppModel().getFilteringList();

			for(int i = 0; i < listSize; i++) {
				XEvent event = model.getCppModel().getEventDataMap().get(keyStr).get(i);
				filteringList.remove(event.getID());
				isSelectedList.add(false);
			}

			isSelectedDataMap.put(keyStr, isSelectedList);

			model.getCppModel().setFilteringList(filteringList);
			model.getCppModel().setIsSelectedDataMap(isSelectedDataMap);

			System.out.println("Filtering List : " + filteringList);
		} else if(selectedPattern == IFConstant.EFR_INT) {
			String keyStr = (String) model.getEfrModel().getAbsFreq().keySet().toArray()[selectedRow];
			Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getEfrModel().getIsSelectedDataMap();
			ArrayList<Boolean> isSelectedList = model.getEfrModel().getIsSelectedDataMap().get(keyStr);
			int listSize = isSelectedList.size();
			isSelectedList.clear();
			ArrayList<XID> filteringList = model.getEfrModel().getFilteringList();

			for(int i = 0; i < listSize; i++) {
				XEvent event = model.getEfrModel().getEventDataMap().get(keyStr).get(i);
				filteringList.remove(event.getID());
				isSelectedList.add(false);
			}

			isSelectedDataMap.put(keyStr, isSelectedList);

			model.getEfrModel().setFilteringList(filteringList);
			model.getEfrModel().setIsSelectedDataMap(isSelectedDataMap);

			System.out.println("Filtering List : " + filteringList);
		} else { //DFR_INT
			String keyStr = (String) model.getDfrModel().getAbsFreq().keySet().toArray()[selectedRow];
			Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getDfrModel().getIsSelectedDataMap();
			ArrayList<Boolean> isSelectedList = model.getDfrModel().getIsSelectedDataMap().get(keyStr);
			int listSize = isSelectedList.size();
			isSelectedList.clear();
			ArrayList<XID> filteringList = model.getDfrModel().getFilteringList();

			for(int i = 0; i < listSize; i++) {
				XEvent event = model.getDfrModel().getEventDataMap().get(keyStr).get(i);
				filteringList.remove(event.getID());
				isSelectedList.add(false);
			}

			isSelectedDataMap.put(keyStr, isSelectedList);

			model.getDfrModel().setFilteringList(filteringList);
			model.getDfrModel().setIsSelectedDataMap(isSelectedDataMap);

			System.out.println("Filtering List : " + filteringList);
		}
		System.out.println("Deselect all!");
	}

	public void selectAll() {
		if(selectedPattern == IFConstant.CPP_INT) {
			String keyStr = (String) model.getCppModel().getAbsFreq().keySet().toArray()[selectedRow];
			Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getCppModel().getIsSelectedDataMap();
			ArrayList<Boolean> isSelectedList = model.getCppModel().getIsSelectedDataMap().get(keyStr);
			int listSize = isSelectedList.size();
			isSelectedList.clear();
			ArrayList<XID> filteringList = model.getCppModel().getFilteringList();

			for(int i = 0; i < listSize; i++) {
				XEvent event = model.getCppModel().getEventDataMap().get(keyStr).get(i);
				filteringList.add(event.getID());
				isSelectedList.add(true);
			}
			isSelectedDataMap.put(keyStr, isSelectedList);

			model.getCppModel().setFilteringList(filteringList);
			model.getCppModel().setIsSelectedDataMap(isSelectedDataMap);
			System.out.println("Filtering List : " + filteringList);

		} else if(selectedPattern == IFConstant.EFR_INT) {
			String keyStr = (String) model.getEfrModel().getAbsFreq().keySet().toArray()[selectedRow];
			Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getEfrModel().getIsSelectedDataMap();
			ArrayList<Boolean> isSelectedList = model.getEfrModel().getIsSelectedDataMap().get(keyStr);
			int listSize = isSelectedList.size();
			isSelectedList.clear();
			ArrayList<XID> filteringList = model.getEfrModel().getFilteringList();

			for(int i = 0; i < listSize; i++) {
				XEvent event = model.getEfrModel().getEventDataMap().get(keyStr).get(i);
				filteringList.add(event.getID());
				isSelectedList.add(true);
			}
			isSelectedDataMap.put(keyStr, isSelectedList);

			model.getEfrModel().setFilteringList(filteringList);
			model.getEfrModel().setIsSelectedDataMap(isSelectedDataMap);
			System.out.println("Filtering List : " + filteringList);

		} else { //DFR_INT
			String keyStr = (String) model.getDfrModel().getAbsFreq().keySet().toArray()[selectedRow];
			Map<String , ArrayList<Boolean>> isSelectedDataMap = model.getDfrModel().getIsSelectedDataMap();
			ArrayList<Boolean> isSelectedList = model.getDfrModel().getIsSelectedDataMap().get(keyStr);
			int listSize = isSelectedList.size();
			isSelectedList.clear();
			ArrayList<XID> filteringList = model.getDfrModel().getFilteringList();

			for(int i = 0; i < listSize; i++) {
				XEvent event = model.getDfrModel().getEventDataMap().get(keyStr).get(i);
				filteringList.add(event.getID());
				isSelectedList.add(true);
			}
			isSelectedDataMap.put(keyStr, isSelectedList);

			model.getDfrModel().setFilteringList(filteringList);
			model.getDfrModel().setIsSelectedDataMap(isSelectedDataMap);
			System.out.println("Filtering List : " + filteringList);

			System.out.println("Select all!");
		}
	}

	public boolean isNumericalOutlier(int row, int column) {
		boolean isOutlier = false;
		String attrName = getColumnName(column);
		String value = (String)getValueAt(row, column);
		value = value.replaceAll(",", ".");
		Double numericalValue = Double.parseDouble(value);

		if(selectedPattern == IFConstant.CPP_INT) {
			Double upperBound = model.getCppModel().getUpperIQRBoundMap().get(attrName);
			Double lowerBound = model.getCppModel().getLowerIQRBoundMap().get(attrName);

			if(numericalValue < lowerBound || numericalValue > upperBound) {
				isOutlier = true;
			}

		} else if(selectedPattern == IFConstant.EFR_INT) {
			Double upperBound = model.getEfrModel().getUpperIQRBoundMap().get(attrName);
			Double lowerBound = model.getEfrModel().getLowerIQRBoundMap().get(attrName);

			if(numericalValue < lowerBound || numericalValue > upperBound) {
				isOutlier = true;
			}

		} else { //DFR_INT
			Double upperBound = model.getDfrModel().getUpperIQRBoundMap().get(attrName);
			Double lowerBound = model.getDfrModel().getLowerIQRBoundMap().get(attrName);

			if(numericalValue < lowerBound || numericalValue > upperBound) {
				isOutlier = true;
			}

		}

		
		
		//		System.out.println("===========================");
		//		System.out.println("Attr Name : " + attrName);
		//		System.out.println("UpperBound : " + upperBound);
		//		System.out.println("LowerBound : " + lowerBound);
		//		System.out.println("===========================");

		return isOutlier;
	}

	public boolean isCategoricalOutlier(int row, int column) {
		boolean isOutlier = false;

		String attrName = getColumnName(column);
		String value = (String)getValueAt(row, column);

		//Decide Value category (Numerical, Categorical..)

		if(selectedPattern == IFConstant.CPP_INT) {
			if(model.getCppModel().getAttrValueCountMap().containsKey(attrName + ">>" + value)
					&&
					model.getCppModel().getAttrCountMap().containsKey(attrName)	
					) {
				int attrCnt = model.getCppModel().getAttrCountMap().get(attrName);
				int attrNumCnt = model.getCppModel().getAttrValueCountMap().get(attrName + ">>" + value);
				if((attrNumCnt * 1.0 / attrCnt) < thresholdCategorical) {

					//				System.out.println(attrName + " has : " + attrNumCnt * 1.0 / attrCnt + " threshold : " + thresholdCategorical);
					isOutlier = true;
				}
				//			System.out.println(attrName + value + "Relation Percentage : " + (attrNumCnt * 1.0 / attrNumCnt));
			}
		} else if(selectedPattern == IFConstant.EFR_INT) {
			if(model.getEfrModel().getAttrValueCountMap().containsKey(attrName + ">>" + value)
					&&
					model.getEfrModel().getAttrCountMap().containsKey(attrName)	
					) {
				int attrCnt = model.getEfrModel().getAttrCountMap().get(attrName);
				int attrNumCnt = model.getEfrModel().getAttrValueCountMap().get(attrName + ">>" + value);
				if((attrNumCnt * 1.0 / attrCnt) < thresholdCategorical) {

					//				System.out.println(attrName + " has : " + attrNumCnt * 1.0 / attrCnt + " threshold : " + thresholdCategorical);
					isOutlier = true;
				}
				//			System.out.println(attrName + value + "Relation Percentage : " + (attrNumCnt * 1.0 / attrNumCnt));
			}
		} else { //DFR_INT
			if(model.getDfrModel().getAttrValueCountMap().containsKey(attrName + ">>" + value)
					&&
					model.getDfrModel().getAttrCountMap().containsKey(attrName)	
					) {
				int attrCnt = model.getDfrModel().getAttrCountMap().get(attrName);
				int attrNumCnt = model.getDfrModel().getAttrValueCountMap().get(attrName + ">>" + value);
				if((attrNumCnt * 1.0 / attrCnt) < thresholdCategorical) {

					//				System.out.println(attrName + " has : " + attrNumCnt * 1.0 / attrCnt + " threshold : " + thresholdCategorical);
					isOutlier = true;
				}
				//			System.out.println(attrName + value + "Relation Percentage : " + (attrNumCnt * 1.0 / attrNumCnt));
			}
		}
		
	

		return isOutlier;
	}

}
