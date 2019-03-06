package org.processmining.interactivefiltering.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XAttributeMap;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.IFUtil;
import org.processmining.interactivefiltering.IQR;

public class ConditionalProbabilityModel {
	PluginContext context;
	XLog inputLog;
	ArrayList<String> colNames;

	//Event
	Set<String> eventSet;
	
	//Attribute
	XAttributeMap attrMap;
	Set<String> attrSet;
	Map<String, Integer> attrValueCountMap;
	Map<String, Integer> attrCountMap;
	ArrayList<Integer> exceptionList;

	//Threshold
	Double thresholdCategorical = 0.25;

	//isSelected for filtering?
	Map<String, ArrayList<Boolean>> isSelectedDataMap;

	//Relation
	ArrayList<String> relationList; // All direct follow relation in the log
	Map<String, ArrayList<XEvent>> eventDataMap;
	Map<String, Integer> absFreq;

	//Filtering
	ArrayList<XID> filteringList;

	//Map
	Map<String, Integer> conditionalProbabilityMap;
	Map<String, Double> probabilityMap;
	Map<String, Integer> inputActMap;
	Map<String, String> dataTypeMap;

	Map<String, ArrayList<Double>> numericalListMap;
	Map<String, Double> upperIQRBoundMap;
	Map<String, Double> lowerIQRBoundMap;

	//CPP parameter
	Map<String, Integer> prefixCnt;

	public ConditionalProbabilityModel(PluginContext context, XLog inputLog, int lengthCondition) {
		this.context = context;
		this.inputLog = inputLog;
		this.colNames = new ArrayList<String>();
		this.colNames.add("from");
		this.colNames.add("to");
		this.colNames.add("conditional probability");
		
		System.out.println("Length : " + lengthCondition);

		//isSelected
		isSelectedDataMap = new HashMap<String, ArrayList<Boolean>>();

		//Attribute
		attrValueCountMap = new HashMap<String, Integer>();
		attrCountMap = new HashMap<String, Integer>();
		attrSet = new HashSet<String>();
		exceptionList = new ArrayList<Integer>();

		//Relation
		relationList = new ArrayList<String>();
		eventDataMap = new HashMap<String, ArrayList<XEvent>>();
		absFreq = new HashMap<String, Integer>();

		//Event
		eventSet = new HashSet<String>();

		//Map
		conditionalProbabilityMap = new HashMap<String, Integer>();
		probabilityMap = new HashMap<String, Double>();
		inputActMap = new HashMap<String, Integer>(); 
		dataTypeMap = new HashMap<String, String>();
		numericalListMap = new HashMap<String, ArrayList<Double>>();
		upperIQRBoundMap = new HashMap<String, Double>();
		lowerIQRBoundMap = new HashMap<String, Double>();
		
		//Time

		//Filtering
		filteringList = new ArrayList<XID>();

		//Labeling
		//		labelingList = new ArrayList<String>();

		//length of condition
		prefixCnt = new HashMap<String, Integer>();

		ArrayList<XEvent> allEventList;
		for(XTrace trace : inputLog) {
			allEventList = new ArrayList<XEvent>();
			for(XEvent event : trace) {

				//Storing event
				eventSet.add(getEventName(event));

				//Collecting attributes
				attrMap = event.getAttributes();
				for(String attr : attrMap.keySet()) {
					attrSet.add(attr);
					String data = event.getAttributes().get(attr).toString();

					if(IFUtil.isDoubleString(data)) {
						data = data.replaceAll(",", ".");
						if(numericalListMap.containsKey(attr)) {
							ArrayList<Double> list = numericalListMap.get(attr);
							list.add(Double.parseDouble(data));
							numericalListMap.put(attr, list);
						} else {
							ArrayList<Double> list = new ArrayList<Double>();
							list.add(Double.parseDouble(data));
							numericalListMap.put(attr, list);
						}
					}

					if(!(dataTypeMap.containsKey(attr))) {
						if(IFUtil.isDoubleString(data)) {
							dataTypeMap.put(attr, "Numerical");
						} else if(attr.equals("concept:name")
								||
								attr.equals("time:timestamp")
								||
								attr.equals("lifecycle:transition")
								) {
							dataTypeMap.put(attr, "None");
						} else {
							dataTypeMap.put(attr, "Categorical");
						}
					}

					if(attrCountMap.containsKey(attr)) {
						attrCountMap.put(attr, attrCountMap.get(attr) + 1);
					} else {
						attrCountMap.put(attr, 1);
					}

					if(attrValueCountMap.containsKey(attr + ">>" + event.getAttributes().get(attr).toString())) {
						attrValueCountMap.put(attr + ">>" + event.getAttributes().get(attr).toString(), attrValueCountMap.get(attr + ">>" + event.getAttributes().get(attr).toString()) + 1);	
					} else {
						attrValueCountMap.put(attr + ">>" + event.getAttributes().get(attr).toString(), 1);
					}
				}

				//Build a event list to iterate
				allEventList.add(event);
			}
			if(trace.size() > lengthCondition) { // the number of event should be longer than length of condition
				for(int i = 0; i < trace.size() - lengthCondition; i++) {
					String fromString ="";
					String toString = "";
					for(int j = 0; j < lengthCondition ; j++) {
						fromString += trace.get(i+j).getAttributes().get("concept:name").toString();
						fromString += ">>>";
					}
					// Count prefix (condition part)
					if(prefixCnt.keySet().contains(fromString)) {
						prefixCnt.put(fromString, (prefixCnt.get(fromString) + 1));
					} else {
						prefixCnt.put(fromString, 1);
					}
					toString = trace.get(i + lengthCondition).getAttributes().get("concept:name").toString();
					
					String keyString = fromString + "->" + toString;
					relationList.add(keyString);
					
					//Adding Map to count frequency
					if(conditionalProbabilityMap.containsKey(keyString)) {
						conditionalProbabilityMap.put(keyString, conditionalProbabilityMap.get(keyString) + 1);
					} else {
						conditionalProbabilityMap.put(keyString, 1);
					}
					
					//Adding XEvent data to eventDatamap and isSelected Data as well
					if(eventDataMap.containsKey(keyString)) { //Update 
						//XEvent information
						ArrayList<XEvent> eventInfoList = eventDataMap.get(keyString);
						eventInfoList.add(trace.get(i + lengthCondition)); // adding target event inforamtion
						eventDataMap.put(keyString, eventInfoList);

						//isSelected information
						ArrayList<Boolean> isSelectedList = isSelectedDataMap.get(keyString);
						isSelectedList.add(false);
						isSelectedDataMap.put(keyString, isSelectedList);
					} else { //Init
						//XEvent information
						ArrayList<XEvent> eventInfoList = new ArrayList<XEvent>();
						eventInfoList.add(trace.get(i + lengthCondition));
						eventDataMap.put(keyString, eventInfoList);
						//isSelected information
						ArrayList<Boolean> isSelectedList = new ArrayList<Boolean>();
						isSelectedList.add(false);
						isSelectedDataMap.put(keyString, isSelectedList);
					}
					
					//Adding absFreq
					if(absFreq.containsKey(keyString)) {
						absFreq.put(keyString, absFreq.get(keyString) + 1);
					} else {
						absFreq.put(keyString, 1);
					}					
				}
			}
		}
		
		absFreq = IFUtil.sortByValue(absFreq);

		//Calculating conditional Probability
		for(String keyString : conditionalProbabilityMap.keySet()) {
			String prefixStr = keyString.split("->")[0];
			int prefixFreq = prefixCnt.get(prefixStr);
			int cppFreq = conditionalProbabilityMap.get(keyString);
			double conditionalProb = (cppFreq * 1.0) / prefixFreq;
			probabilityMap.put(keyString, conditionalProb);
		}
		
		probabilityMap = IFUtil.sortByDoubleValue(probabilityMap);

		//Find IQR Values
		ArrayList<String> attrList = new ArrayList<String>(numericalListMap.keySet());
		int numericalAttrCnt = attrList.size();
		IQR[] iqrData = new IQR[numericalAttrCnt];
		
		System.out.println("AttrList : " + attrList);
		for(int k = 0; k < numericalAttrCnt; k++) {
			System.out.println("Data List : " );
			iqrData[k] = new IQR(numericalListMap.get(attrList.get(k)));
			iqrData[k].pullstats();
			Double upperValue = iqrData[k].getUq();
			Double lowerValue = iqrData[k].getLq();
			upperIQRBoundMap.put(attrList.get(k), upperValue);
			lowerIQRBoundMap.put(attrList.get(k), lowerValue);
		}
	}


	public String getEventName(XEvent event) {
		return event.getAttributes().get("concept:name").toString();
	}


	public ArrayList<String> getColNames() {
		return colNames;
	}

	public void setColNames(ArrayList<String> colNames) {
		this.colNames = colNames;
	}

	public Map<String, Integer> getAbsFreq() {
		return absFreq;
	}

	public void setAbsFreq(Map<String, Integer> absFreq) {
		this.absFreq = absFreq;
	}

	public XAttributeMap getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(XAttributeMap attrMap) {
		this.attrMap = attrMap;
	}

	public Set<String> getAttrSet() {
		return attrSet;
	}

	public void setAttrSet(Set<String> attrSet) {
		this.attrSet = attrSet;
	}

	public Map<String, Integer> getAttrValueCountMap() {
		return attrValueCountMap;
	}

	public void setAttrValueCountMap(Map<String, Integer> attrValueCountMap) {
		this.attrValueCountMap = attrValueCountMap;
	}

	public Map<String, Integer> getAttrCountMap() {
		return attrCountMap;
	}

	public void setAttrCountMap(Map<String, Integer> attrCountMap) {
		this.attrCountMap = attrCountMap;
	}

	public ArrayList<Integer> getExceptionList() {
		return exceptionList;
	}

	public void setExceptionList(ArrayList<Integer> exceptionList) {
		this.exceptionList = exceptionList;
	}

	public Double getThresholdCategorical() {
		return thresholdCategorical;
	}

	public void setThresholdCategorical(Double thresholdCategorical) {
		this.thresholdCategorical = thresholdCategorical;
	}

	public Map<String, ArrayList<Boolean>> getIsSelectedDataMap() {
		return isSelectedDataMap;
	}

	public void setIsSelectedDataMap(Map<String, ArrayList<Boolean>> isSelectedDataMap) {
		this.isSelectedDataMap = isSelectedDataMap;
	}

	public Map<String, ArrayList<XEvent>> getEventDataMap() {
		return eventDataMap;
	}

	public void setEventDataMap(Map<String, ArrayList<XEvent>> eventDataMap) {
		this.eventDataMap = eventDataMap;
	}

	public ArrayList<XID> getFilteringList() {
		return filteringList;
	}

	public void setFilteringList(ArrayList<XID> filteringList) {
		this.filteringList = filteringList;
	}

	public Map<String, String> getDataTypeMap() {
		return dataTypeMap;
	}

	public void setDataTypeMap(Map<String, String> dataTypeMap) {
		this.dataTypeMap = dataTypeMap;
	}

	public Map<String, Double> getUpperIQRBoundMap() {
		return upperIQRBoundMap;
	}
	public void setUpperIQRBoundMap(Map<String, Double> upperIQRBoundMap) {
		this.upperIQRBoundMap = upperIQRBoundMap;
	}

	public Map<String, Double> getLowerIQRBoundMap() {
		return lowerIQRBoundMap;
	}

	public void setLowerIQRBoundMap(Map<String, Double> lowerIQRBoundMap) {
		this.lowerIQRBoundMap = lowerIQRBoundMap;
	}

	public Map<String, Double> getProbabilityMap() {
		return probabilityMap;
	}

	public void setProbabilityMap(Map<String, Double> probabilityMap) {
		this.probabilityMap = probabilityMap;
	}
	
	public static List<Double> getOutliers(List<Double> input) {
		List<Double> output = new ArrayList<Double>();
		List<Double> data1 = new ArrayList<Double>();
		List<Double> data2 = new ArrayList<Double>();
		if (input.size() % 2 == 0) {
			data1 = input.subList(0, input.size() / 2);
			data2 = input.subList(input.size() / 2, input.size());
		} else {
			data1 = input.subList(0, input.size() / 2);
			data2 = input.subList(input.size() / 2 + 1, input.size());
		}
		if (input.size()==1) {
			return data1;
		}
		double q1 = getMedian(data1);
		double q3 = getMedian(data2);
		double iqr = q3 - q1;
		double lowerFence = q1 - 1.5 * iqr;
		double upperFence = q3 + 1.5 * iqr;
		for (int i = 0; i < input.size(); i++) {
			if (input.get(i) < lowerFence || input.get(i) > upperFence)
				output.add(input.get(i));
		}
		return output;
	}

	private static double getMedian(List<Double> data) {
		if (data.size() % 2 == 0)
			return (data.get(data.size() / 2) + data.get(data.size() / 2 - 1)) / 2;
		else
			return data.get(data.size() / 2);
	}

}
