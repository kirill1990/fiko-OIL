package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import ru.fiko.oil.main.Oil;

public class Citys extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3195316006745966015L;
	private JTable	jDataTable;
	private boolean isChangeTable = false;

	public Citys() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");

		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		jDataTable = new JTable()
		{
			private static final long	serialVersionUID	= 1L;
			
			/*
			 * Запрет на редактирование ячеек
			 */
			@Override
			public boolean isCellEditable(int row, int column)
			{
				if (column > 1)
					return true;
				else
					return false;
			}
		};
		jDataTable.setRowHeight((int) (jDataTable.getRowHeight()*1.2));

		this.add(new JScrollPane(jDataTable), null);

		refreshTable();

		this.repaint();
	}

	private void refreshTable() throws SQLException
	{
		isChangeTable = false;
		Vector<String> header = new Vector<String>();
		{
			header.add("id");
			header.add("Район");
			header.add("Наименование");
			header.add("х");
			header.add("у");
		}

		Vector<Vector<String>> values = new Vector<Vector<String>>();
		{
			ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM city;");

			while (rs.next())
			{
				Vector<String> item = new Vector<String>();

				// id
				item.add(rs.getString(1));
				// id района
				item.add(rs.getString(2));

				// title
				item.add(rs.getString(3));
				//x y
				item.add(rs.getString(4));
				item.add(rs.getString(5));

				values.add(item);
			}

			rs.close();
		}

		// Помещаю в модель таблицы данные
		DefaultTableModel dtm = (DefaultTableModel) jDataTable.getModel();
		// Сначала данные, потом шапка
		dtm.setDataVector(values, header);
		// задаем ширину каждого столбца
		// id
		jDataTable.getColumnModel().getColumn(0).setMaxWidth(60);
		jDataTable.getColumnModel().getColumn(1).setMaxWidth(40);

		jDataTable.getColumnModel().getColumn(3).setMaxWidth(100);

		jDataTable.getColumnModel().getColumn(4).setMaxWidth(100);

		jDataTable.getModel().addTableModelListener(new JTableChanged());

		this.validate();
		this.repaint();
		isChangeTable = true;
	}

	private class JTableChanged implements TableModelListener
	{

		@Override
		public void tableChanged(TableModelEvent e)
		{
			/*
			 * isChangeTable - возмодность обновление данных
			 * Необходим, чтобы избежать некорректного запроса к таблице,
			 * во время её обновления методом refreshCostOil()
			 */
			if (isChangeTable)
			{
				if (e.getColumn() > 0)
				{

					String id = jDataTable.getModel().getValueAt(e.getFirstRow(), 0).toString();
					
					String title = jDataTable.getModel().getValueAt(e.getFirstRow(), 2).toString();
					String x = jDataTable.getModel().getValueAt(e.getFirstRow(), 3).toString();
					String y = jDataTable.getModel().getValueAt(e.getFirstRow(), 4).toString();

					try
					{
						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE city SET title = '" + title + "' WHERE id LIKE '" + id + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE city SET x = '" + x + "' WHERE id LIKE '" + id + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE city SET y = '" + y + "' WHERE id LIKE '" + id + "';");

					}
					catch (SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}
		}

	}
}
