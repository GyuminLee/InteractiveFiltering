package org.processmining.interactivefiltering;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deckfour.xes.id.XID;
import org.deckfour.xes.model.XEvent;
import org.deckfour.xes.model.XLog;
import org.deckfour.xes.model.XTrace;
import org.processmining.contexts.uitopia.UIPluginContext;
import org.processmining.framework.plugin.PluginContext;

public class IFUtil {
	
	public static boolean isDoubleString(String s) {
		try {
			Double.parseDouble(s);
			return true;
		} catch (NumberFormatException ex){
			return false;
		}
	}
	
	
	public static Map<String, Integer> sortByValue(Map<String, Integer> unsortMap) {

		// 1. Convert Map to List of Map
		List<Map.Entry<String, Integer>> list =
				new LinkedList<Map.Entry<String, Integer>>(unsortMap.entrySet());

		// 2. Sort list with Collections.sort(), provide a custom Comparator
		//    Try switch the o1 o2 position for a different order
		Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
			public int compare(Map.Entry<String, Integer> o1,
					Map.Entry<String, Integer> o2) {
				return (o1.getValue()).compareTo(o2.getValue());
			}
		});

		// 3. Loop the sorted list and put it into a new insertion order Map LinkedHashMap
		Map<String, Integer> sortedMap = new LinkedHashMap<String, Integer>();
		for (Map.Entry<String, Integer> entry : list) {
			sortedMap.put(entry.getKey(), entry.getValue());
		}

		/*
	        //classic iterator example
	        for (Iterator<Map.Entry<String, Integer>> it = list.iterator(); it.hasNext(); ) {
	            Map.Entry<String, Integer> entry = it.next();
	            sortedMap.put(entry.getKey(), entry.getValue());
	        }*/

		return sortedMap;
	}
	

	public static void exportLog(PluginContext context, XLog log, ArrayList<XID> filteringList) {
		XLog outputLog = (XLog) log.clone();
		Set<Integer> removeSet = new HashSet<Integer>();
		for(XTrace trace : outputLog) {
			for(XEvent event : trace) {
				if(filteringList.contains(event.getID())) {
					removeSet.add(outputLog.indexOf(trace));
				}
			}
		}

		ArrayList<Integer> removeList = new ArrayList<Integer>(removeSet);
		for(int i = 0; i < removeList.size(); i++) {
			System.out.println("Remove Item : " + removeList.get(i));
			outputLog.remove(Integer.parseInt(removeList.get(i).toString()));
		}
		
		System.out.println("Do export!");
		context.getProvidedObjectManager().createProvidedObject("Filtered Log", outputLog, XLog.class, context);
		if (context instanceof UIPluginContext) {
			((UIPluginContext) context).getGlobalContext().getResourceManager().getResourceForInstance(outputLog)
			.setFavorite(true);
		}
	}
	
}
