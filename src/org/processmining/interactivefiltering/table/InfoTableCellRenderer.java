package org.processmining.interactivefiltering.table;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

import org.processmining.interactivefiltering.IFConstant;

public class InfoTableCellRenderer extends DefaultTableCellRenderer{

	JCheckBox checkBox = new JCheckBox();

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		IFInfoTableModel tableModel = (IFInfoTableModel)table.getModel();
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);


		if(value != null) {

			if(value.equals("")) {
				c.setBackground(java.awt.Color.white);
				return c;
			}

			if(value instanceof String) {
				String attrName = tableModel.getColumnName(column);
				String dataType = "";
				int selectedPattern = tableModel.model.getSelectedPattern();

				if(selectedPattern == IFConstant.CPP_INT) {
					dataType = tableModel.model.getCppModel().getDataTypeMap().get(attrName);
				} else if(selectedPattern == IFConstant.EFR_INT) {
					dataType = tableModel.model.getEfrModel().getDataTypeMap().get(attrName);
				} else { //DFR_INT
					dataType = tableModel.model.getDfrModel().getDataTypeMap().get(attrName);
				}

				if(dataType.equals("Numerical")) {
					//Data Type : Numerical value
					if(tableModel.isNumericalOutlier(row, column)) {
						c.setBackground(java.awt.Color.red);
					} else {
						c.setBackground(java.awt.Color.green);
					}
				} else if (dataType.equals("Categorical")) {
					//Data Type : Categorical Value
					if(tableModel.isCategoricalOutlier(row, column)) {
						c.setBackground(java.awt.Color.red);
					} else {
						c.setBackground(java.awt.Color.green);
					}
				} else {
					//Data Type : None
					c.setBackground(java.awt.Color.white);
					return c;
				}
			} 

			if(value instanceof Boolean) {
				checkBox.setEnabled(true);
				checkBox.setSelected(((Boolean)value).booleanValue());
				checkBox.setHorizontalAlignment(JLabel.CENTER);
				checkBox.addItemListener(new ItemListener() {
					public void itemStateChanged(ItemEvent e) {

					}
				});
				return checkBox;
			}
		}
		return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	}



}
