package org.processmining.interactivefiltering;

import org.deckfour.xes.model.XLog;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.model.DirectFollowModel;

public class IFModel {

	PluginContext context;
	XLog inputLog;
	XLog outputLog;
	
	DirectFollowModel dfrModel;
	

	public IFModel(PluginContext context, XLog inputLog) {
		this.context = context;
		this.inputLog = inputLog;
		dfrModel = new DirectFollowModel(context, inputLog);
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



	public void setInputLog(XLog inputLog) {
		this.inputLog = inputLog;
	}
	
}
