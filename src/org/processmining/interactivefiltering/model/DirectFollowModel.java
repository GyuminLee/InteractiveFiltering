package org.processmining.interactivefiltering.model;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
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
import org.deckfour.xes.model.impl.XAttributeLiteralImpl;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.IFUtil;
import org.processmining.interactivefiltering.IQR;

public class DirectFollowModel {

	PluginContext context;
	XLog inputLog;

	//isSelected for filtering?
	Map<String, ArrayList<Boolean>> isSelectedDataMap;

	//Attribute
	XAttributeMap attrMap;
	Set<String> attrSet;
	Map<String, Integer> attrValueCountMap;
	Map<String, Integer> attrCountMap;
	ArrayList<String> exceptionStringList;

	//Relation
	ArrayList<String> relationList; // All direct follow relation in the log
	Map<String, ArrayList<XEvent>> eventDataMap;
	Map<String, Integer> absFreq;

	//Event
	Set<String> eventSet;
	//Time 
	Map<String, List<Double>> waitingTimeMap;
	Map<String, Double> activityOutlierPercentageMap;

	//Map
	Map<String, Integer> directFollowMap;
	Map<String, Integer> inputActMap;
	Map<String, String> dataTypeMap;

	Map<String, ArrayList<Double>> numericalListMap;
	Map<String, Double> upperIQRBoundMap;
	Map<String, Double> lowerIQRBoundMap;

	//Threshold
	Double thresholdCategorical = 0.25;

	//Filtering
	ArrayList<XID> filteringList;

	//Labeling
	ArrayList<String> labelingList;

	//ColumnNames
	ArrayList<String> colNames;

	//Attr exceptionList

	public DirectFollowModel(PluginContext context, XLog inputLog, ArrayList<String> exceptionStringList) {
		this.context = context;
		this.inputLog = inputLog;
		this.colNames = new ArrayList<String>();
		this.colNames.add("from");
		this.colNames.add("to");
		this.colNames.add("absolute frequency");

		//isSelected
		isSelectedDataMap = new HashMap<String, ArrayList<Boolean>>();

		//Attribute
		attrValueCountMap = new HashMap<String, Integer>();
		attrCountMap = new HashMap<String, Integer>();
		attrSet = new HashSet<String>();

		//Relation
		relationList = new ArrayList<String>();
		eventDataMap = new HashMap<String, ArrayList<XEvent>>();
		absFreq = new HashMap<String, Integer>();

		//Event
		eventSet = new HashSet<String>();

		//Map
		directFollowMap = new HashMap<String, Integer>(); 
		inputActMap = new HashMap<String, Integer>(); 
		dataTypeMap = new HashMap<String, String>();
		numericalListMap = new HashMap<String, ArrayList<Double>>();
		upperIQRBoundMap = new HashMap<String, Double>();
		lowerIQRBoundMap = new HashMap<String, Double>();
		//Time
		waitingTimeMap = new HashMap<String, List<Double>>();
		activityOutlierPercentageMap = new HashMap<String, Double>();

		//Filtering
		filteringList = new ArrayList<XID>();

		//Labeling
		labelingList = new ArrayList<String>();


		this.exceptionStringList = exceptionStringList;

		
		System.out.println("DFR Calculation start");
		Date startDate = new Date();
		long startTime =  startDate.getTime();
		
		ArrayList<XEvent> allEventList;
		for(XTrace trace : inputLog) {
			allEventList = new ArrayList<XEvent>();
			String prevTime = "";
			String nextTime = "";
			for(XEvent event : trace) {
				//Store event 
				eventSet.add(getEventName(event));
				//Collect Attributes
				attrMap = event.getAttributes();
				for(String attr : attrMap.keySet()) {

					//if attr is not in the exceptionList
					if(!exceptionStringList.contains(attr)) {
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
				}
				//Build a eventList
				allEventList.add(event);
			}

			for(int i = 0; i < allEventList.size(); i++) {
				if(i == 0) { // first idx , Adding dummy Start event
					String fromString = "START EVENT (Dummy)";
					String toString = getEventName(allEventList.get(i));

					addInformation(fromString, toString, i, allEventList);

				} else if (i == allEventList.size() - 1) { //Last idx, Adding dummy End event

					String fromString = getEventName(allEventList.get(i - 1));
					String toString = getEventName(allEventList.get(i));
					addInformation(fromString, toString, i, allEventList);

					fromString = getEventName(allEventList.get(i));
					toString = "END EVENT (Dummy)";

					addInformation(fromString, toString, i, allEventList);


				} else {
					String fromString = getEventName(allEventList.get(i - 1));
					String toString = getEventName(allEventList.get(i));

					addInformation(fromString, toString, i, allEventList);
				}
			}

		}

		absFreq = IFUtil.sortByValue(absFreq);

		//Find IQR Values
		ArrayList<String> attrList = new ArrayList<String>(numericalListMap.keySet());
		int numericalAttrCnt = attrList.size();
		IQR[] iqrData = new IQR[numericalAttrCnt];

		System.out.println("AttrList : " + attrList);
		for(int i = 0; i < numericalAttrCnt; i++) {
			System.out.println("Data List : " );
			iqrData[i] = new IQR(numericalListMap.get(attrList.get(i)));
			iqrData[i].pullstats();
			Double upperValue = iqrData[i].getUq();
			Double lowerValue = iqrData[i].getLq();
			upperIQRBoundMap.put(attrList.get(i), upperValue);
			lowerIQRBoundMap.put(attrList.get(i), lowerValue);
			//			System.out.println("===========================");
			//			System.out.println("Attr Name : " + attrList.get(i));
			//			System.out.println("Data List : " + numericalListMap.get(attrList.get(i)));
			//			System.out.println("UpperBound : " + upperValue);
			//			System.out.println("LowerBound : " + lowerValue);
			//			System.out.println("===========================");
		}



		//Find the outlier in execution times
		Set<String> activitiesSet = waitingTimeMap.keySet();
		String[] activityArray = activitiesSet.toArray(new String[activitiesSet.size()]);
		for(int i = 0; i < activityArray.length; i++) {
			List<Double> tempWaitingTime = waitingTimeMap.get(activityArray[i]);
			Collections.sort(tempWaitingTime);
			List<Double> sd = getOutliers(tempWaitingTime);
			double ss = sd.size()*1.0 / tempWaitingTime.size();
			activityOutlierPercentageMap.put(activityArray[i], ss);
		}
		
		
		System.out.println("DFR Calculation has been finished");

		Date endDate = new Date();
		long endTime =  endDate.getTime();
		
		System.out.println("Takes " + (endTime - startTime) / 1000 + "sec");
	}

	public String getEventName(XEvent event) {
		return event.getAttributes().get("concept:name").toString();
	}

	public XLog getInputLog() {
		return inputLog;
	}

	public void setInputLog(XLog inputLog) {
		this.inputLog = inputLog;
	}

	public XAttributeMap getAttrMap() {
		return attrMap;
	}

	public void setAttrMap(XAttributeMap attrMap) {
		this.attrMap = attrMap;
	}

	public Double getThresholdCategorical() {
		return thresholdCategorical;
	}

	public void setThresholdCategorical(Double thresholdCategorical) {
		this.thresholdCategorical = thresholdCategorical;
	}

	public Set<String> getAttrSet() {
		return attrSet;
	}

	public void setAttrSet(Set<String> attrSet) {
		this.attrSet = attrSet;
	}

	public ArrayList<String> getRelationList() {
		return relationList;
	}

	public void setRelationList(ArrayList<String> relationList) {
		this.relationList = relationList;
	}

	public ArrayList<String> getColNames() {
		return colNames;
	}

	public void setColNames(ArrayList<String> colNames) {
		this.colNames = colNames;
	}

	public Map<String, ArrayList<XEvent>> getEventDataMap() {
		return eventDataMap;
	}

	public void setEventDataMap(Map<String, ArrayList<XEvent>> eventDataMap) {
		this.eventDataMap = eventDataMap;
	}

	public Map<String, Integer> getAbsFreq() {
		return absFreq;
	}

	public void setAbsFreq(Map<String, Integer> absFreq) {
		this.absFreq = absFreq;
	}

	public Map<String, ArrayList<Boolean>> getIsSelectedDataMap() {
		return isSelectedDataMap;
	}

	public void setIsSelectedDataMap(Map<String, ArrayList<Boolean>> isSelectedDataMap) {
		this.isSelectedDataMap = isSelectedDataMap;
	}


	public ArrayList<String> getExceptionStringList() {
		return exceptionStringList;
	}

	public void setExceptionStringList(ArrayList<String> exceptionStringList) {
		this.exceptionStringList = exceptionStringList;
	}

	public ArrayList<XID> getFilteringList() {
		return filteringList;
	}

	public void setFilteringList(ArrayList<XID> filteringList) {
		this.filteringList = filteringList;
	}

	public ArrayList<String> getLabelingList() {
		return labelingList;
	}

	public void setLabelingList(ArrayList<String> labelingList) {
		this.labelingList = labelingList;
	}

	public Map<String, Double> getActivityOutlierPercentageMap() {
		return activityOutlierPercentageMap;
	}

	public void setActivityOutlierPercentageMap(Map<String, Double> activityOutlierPercentageMap) {
		this.activityOutlierPercentageMap = activityOutlierPercentageMap;
	}

	public Map<String, Integer> getDirectFollowMap() {
		return directFollowMap;
	}

	public void setDirectFollowMap(Map<String, Integer> directFollowMap) {
		this.directFollowMap = directFollowMap;
	}

	public Map<String, Integer> getInputActMap() {
		return inputActMap;
	}

	public void setInputActMap(Map<String, Integer> inputActMap) {
		this.inputActMap = inputActMap;
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


	public Map<String, List<Double>> getWaitingTimeMap() {
		return waitingTimeMap;
	}

	public void setWaitingTimeMap(Map<String, List<Double>> waitingTimeMap) {
		this.waitingTimeMap = waitingTimeMap;
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

	public void addInformation(String fromString, String toString, int idx, ArrayList<XEvent> allEventList) {

		XEvent event = allEventList.get(idx);
		String prevTime;
		String nextTime;

		//Adding relation to "relationList" 
		relationList.add(fromString + "->" + toString);

		//Adding Map 
		if(directFollowMap.containsKey(fromString + "->" + toString)) {
			directFollowMap.put(fromString + "->" + toString, directFollowMap.get(fromString + "->" + toString) + 1);
		} else {
			directFollowMap.put(fromString + "->" + toString,  1);
		}

		if(inputActMap.containsKey(fromString)) {
			inputActMap.put(fromString, inputActMap.get(fromString) + 1);
		} else {
			inputActMap.put(fromString, 1);
		}

		//Building waiting time map
		if(idx == 0) {
			prevTime = event.getAttributes().get("time:timestamp").toString();
		} else {
			prevTime = allEventList.get(idx-1).getAttributes().get("time:timestamp").toString();
		}

		nextTime = event.getAttributes().get("time:timestamp").toString();

		OffsetDateTime nextODT = OffsetDateTime.parse(nextTime);
		OffsetDateTime prevODT = OffsetDateTime.parse(prevTime);

		double diffTime = Duration.between(prevODT, nextODT).getSeconds();

		if(waitingTimeMap.containsKey(getEventName(event))) {
			List<Double> waitingTimeList = waitingTimeMap.get(getEventName(event));
			waitingTimeList.add(diffTime);
			waitingTimeMap.put(getEventName(event), waitingTimeList);
		} else {
			List<Double> waitingTimeList = new ArrayList<Double>();
			waitingTimeList.add(diffTime);
			waitingTimeMap.put(getEventName(event), waitingTimeList);
		}

		//Adding XEvent data to eventData Map and isSelected Data as well
		if(eventDataMap.containsKey(fromString + "->" + toString)) { //Update Value
			//XEvent
			ArrayList<XEvent> eventList = eventDataMap.get(fromString + "->" + toString);
			eventList.add(event);
			eventDataMap.put(fromString + "->" + toString, eventList);
			//isSelected
			ArrayList<Boolean> isSelectedList = isSelectedDataMap.get(fromString + "->" + toString);
			isSelectedList.add(false);
			isSelectedDataMap.put(fromString + "->" + toString, isSelectedList);
		} else { // Initial Value
			//XEvent
			ArrayList<XEvent> eventList = new ArrayList<XEvent>();
			eventList.add(event);
			eventDataMap.put(fromString + "->" + toString, eventList);
			//isSelected
			ArrayList<Boolean> isSelectedList = new ArrayList<Boolean>();
			isSelectedList.add(false);
			isSelectedDataMap.put(fromString + "->" + toString, isSelectedList);
		}

		//Adding absFreq
		if(absFreq.containsKey(fromString + "->" + toString)) {
			absFreq.put(fromString + "->" + toString, absFreq.get(fromString + "->" + toString) + 1);
		} else {
			absFreq.put(fromString + "->" + toString, 1);
		}
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

	public void findCategoricalOutlier() {

	}

	public void findNumericalOutlier() {

	}


	public XLog addAttributes(XLog inputLog) {
		XLog outputLog = (XLog) inputLog.clone();

		XAttributeLiteralImpl waitingAttrClean = new XAttributeLiteralImpl("waitingTime", "clean");
		XAttributeLiteralImpl waitingAttrDirty = new XAttributeLiteralImpl("waitingTime", "dirty");
		for(XTrace trace : outputLog) {
			String eventList = "";
			boolean isNoise = false;
			for(XEvent event : trace) {
				eventList += getEventName(event) + ">>"; 
			}

			String[] eventArray = eventList.split(">>");

			for(int i = 0; i < eventArray.length - 1; i++) {
				String prevEvent = eventArray[i];
				String nextEvent = eventArray[i+1];
				String directFollowRelation = prevEvent + "->" + nextEvent;
				if(activityOutlierPercentageMap.get(nextEvent) < 0.07
						&&
						directFollowMap.get(directFollowRelation) * 1.0 / inputActMap.get(prevEvent) < 0.25
						) {
					isNoise = true;
				}
			}
			if(isNoise) {
				trace.getAttributes().put("waitingTime", waitingAttrDirty);
			} else {
				trace.getAttributes().put("waitingTime", waitingAttrClean);
			}
		}

		return outputLog;
	}
}

