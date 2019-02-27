package org.processmining.interactivefiltering.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;

import org.processmining.framework.plugin.PluginContext;
import org.processmining.interactivefiltering.IFModel;

public class DataTypeSelectionListener implements ActionListener {

	PluginContext context;
	IFModel model;
	JComboBox<String>[] dataTypeSelection;
	int index;

	public DataTypeSelectionListener(PluginContext context, IFModel model, JComboBox<String>[] dataTypeSelection, int index) {
		this.context = context;
		this.model = model;
		this.dataTypeSelection = dataTypeSelection;
		this.index = index;
	}


	public void actionPerformed(ActionEvent e) {
		String selectedItem = dataTypeSelection[index].getSelectedItem().toString();
		// TODO Auto-generated method stub
		System.out.println("Selected Item : " + selectedItem);

	}


}
