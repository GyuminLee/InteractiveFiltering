package org.processmining.interactivefiltering.plugin;


import org.deckfour.xes.model.XLog;
import org.processmining.contexts.uitopia.annotations.UITopiaVariant;
import org.processmining.framework.plugin.PluginContext;
import org.processmining.framework.plugin.annotations.Plugin;
import org.processmining.framework.plugin.annotations.PluginVariant;
import org.processmining.interactivefiltering.IFConstant;
import org.processmining.interactivefiltering.IFModel;

@Plugin(name = "Interactive Filtering",
parameterLabels = {"Log"} ,
returnLabels = {"Interactive Filtering Model"},
returnTypes = {IFModel.class})

public class IFPlugin {
	@UITopiaVariant(affiliation = "RWTH Aachen", author = "Gyumin Lee",
			email = "gyumin.lee@rwth-aachen.de")
	@PluginVariant(requiredParameterLabels = { 0 })
	public IFModel createInteractvieView(PluginContext context, XLog log) {
		System.out.println("create Interactive View Func Start");
		context.getProgress().setIndeterminate(true);

		return new IFModel(context, log, IFConstant.DFR_INT);
	}
}
