package org.processmining.interactivefiltering;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.model.ConditionalProbabilityModel;
import org.processmining.interactivefiltering.model.DirectFollowModel;
import org.processmining.interactivefiltering.model.EventualFollowModel;

public class IFModel {

	PluginContext context;
	XLog inputLog;
	XLog outputLog;
	int selectedPattern;
	int prevPattern = 0;
	int lengthCondition = 2;
	
	DirectFollowModel dfrModel;
	ConditionalProbabilityModel cppModel;
	EventualFollowModel efrModel;
	
	public IFModel(PluginContext context, XLog inputLog, int selectedPattern) {
		this.context = context;
		this.inputLog = inputLog;
		this.selectedPattern = selectedPattern;
//		selectedPattern = IFConstant.DFR_INT;
		if(selectedPattern == IFConstant.CPP_INT) {
			cppModel = new ConditionalProbabilityModel(context, inputLog, lengthCondition);
		} else if (selectedPattern == IFConstant.EFR_INT) {
			efrModel = new EventualFollowModel(context, inputLog);
		} else {
			dfrModel = new DirectFollowModel(context, inputLog);
		}
	}
	public int getLengthCondition() {
		return lengthCondition;
	}

	public void setLengthCondition(int lengthCondition) {
		this.lengthCondition = lengthCondition;
	}
	public ConditionalProbabilityModel getCppModel() {
		return cppModel;
	}


	public void setCppModel(ConditionalProbabilityModel cppModel) {
		this.cppModel = cppModel;
	}



	public EventualFollowModel getEfrModel() {
		return efrModel;
	}



	public void setEfrModel(EventualFollowModel efrModel) {
		this.efrModel = efrModel;
	}



	public int getSelectedPattern() {
		return selectedPattern;
	}


	public void setSelectedPattern(int selectedPattern) {
		this.selectedPattern = selectedPattern;
	}

	public DirectFollowModel getDfrModel() {
		return dfrModel;
	}



	public void setDfrModel(DirectFollowModel dfrModel) {
		this.dfrModel = dfrModel;
	}



	public XLog getInputLog() {
		return inputLog;
	}



	public int getPrevPattern() {
		return prevPattern;
	}
	public void setPrevPattern(int prevPattern) {
		this.prevPattern = prevPattern;
	}
	public void setInputLog(XLog inputLog) {
		this.inputLog = inputLog;
	}
	
}
