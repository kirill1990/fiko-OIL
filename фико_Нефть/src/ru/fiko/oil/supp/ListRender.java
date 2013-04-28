package ru.fiko.oil.supp;

import java.awt.Component;
import java.util.EventObject;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

public class ListRender extends DefaultTableModel implements TableCellRenderer, TableCellEditor
{

	@Override
	public void addCellEditorListener(CellEditorListener arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelCellEditing()
	{
		// TODO Auto-generated method stub

	}

	@Override
	public Object getCellEditorValue()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCellEditable(EventObject arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeCellEditorListener(CellEditorListener arg0)
	{
		// TODO Auto-generated method stub

	}

	@Override
	public boolean shouldSelectCell(EventObject arg0)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean stopCellEditing()
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Component getTableCellEditorComponent(JTable arg0, Object arg1, boolean arg2, int arg3, int arg4)
	{
		JPanel result = (JPanel) arg1;
		return result;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col)
	{
		JPanel result = (JPanel) value;
		return result;
	}

	// public Component getListCellRendererComponent(JList list, Object value,
	// int index, boolean isSelected, boolean cellHasFocus)
	// {
	// // TODO Auto-generated method stub
	//
	// JPanel result = (JPanel) value;
	//
	// if (isSelected)
	// {
	// result.setBackground(new Color(159, 193, 227));
	// // ((Torrent) value).setForegroundColor(Color.WHITE);
	// }
	// else
	// {
	// result.setBackground(new Color(242, 241, 240));
	// // ((Torrent) value).setForegroundColor(Color.BLACK);
	// }
	// return result;
	// }

}
