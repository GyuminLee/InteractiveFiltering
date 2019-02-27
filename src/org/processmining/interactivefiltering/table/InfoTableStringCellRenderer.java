package org.processmining.interactivefiltering.table;

import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class InfoTableStringCellRenderer extends DefaultTableCellRenderer{

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		IFInfoTableModel tableModel = (IFInfoTableModel)table.getModel();
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

		System.out.println("Value : " + value + "Row : " + row + "Col : " + column);
		
		if(value instanceof String) {
			if(tableModel.isCategoricalOutlier(row, column)) {
				c.setBackground(java.awt.Color.red);
			} else {
				c.setBackground(java.awt.Color.green);
			}
		}
		return c;
	}
}
