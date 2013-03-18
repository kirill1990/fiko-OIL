package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.ComboItem;

/**
 * Панель с информации о АЗС и возможности обновления данных
 * 
 * @author kirill
 * 
 */
public class Station extends JPanel
{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 5438102583382526235L;
	private JComboBox			cbDistrict;
	private JComboBox			cbCity;
	private JTextField			text_name;
	private JTextField			text_address;
	private JTextField			text_tel;
	private JComboBox			cbComm;
	private JComboBox			cbStatus;
	private JTable				jDataTable;
	private JTextField			text_data;
	private JTextField			text_bdis;
	private JTextField			text_b95;
	private JTextField			text_b92;
	private JTextField			text_b80;
	private JButton				update;

	private String				station_id			= "0";

	private boolean				isChangeTable		= true;

	private Stations			main;
	private JButton				update_close;

	public Station(Stations _main, final String stationId) throws SQLException
	{
		this.station_id = stationId;
		this.main = _main;
		this.setLayout(new BorderLayout(5, 5));
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		/*
		 * ***********************************************************
		 * Получение информации из БД о АЗС - station_id
		 * ***********************************************************
		 */
		ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM station WHERE id LIKE '" + station_id + "';");

		Vector<String> data = new Vector<String>(7);

		if (rs.next())
			for (int i = 2; i < 9; i++)
				data.add(rs.getString(i));
		rs.close();

		/*
		 * ***********************************************************
		 * Шапка - информация о АЗС
		 * ***********************************************************
		 */

		JPanel titlePanel = new JPanel(new BorderLayout(5, 5));
		this.add(titlePanel, BorderLayout.NORTH);

		/*
		 * ***********************************************************
		 * Выпадающий список принадлежности АЗС к: <br>
		 * - району Калужской области
		 * - городу района
		 * ***********************************************************
		 */

		JPanel comboPanel = new JPanel(new GridLayout(1, 2, 5, 5));
		titlePanel.add(comboPanel, BorderLayout.NORTH);

		// Список районов Калужской области
		cbDistrict = new JComboBox();
		// Сетевые организация АЗС по Калужской области
		cbCity = new JComboBox();
		{
			/*
			 * ***********************************************************
			 * Заполнение районов
			 * ***********************************************************
			 */
			rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title FROM district;");

			while (rs.next())
			{
				cbDistrict.addItem(new ComboItem(rs.getString(1), rs.getString(2)));
				if (data.get(0).equals(rs.getString(1)))
					cbDistrict.setSelectedIndex(cbDistrict.getItemCount() - 1);
			}

			comboPanel.add(cbDistrict);

			cbDistrict.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setButtonEnabled(true);
					// обновление списка городов
					try
					{
						String id = ((ComboItem) cbDistrict.getSelectedItem()).getValue();
						ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title FROM city WHERE district_id LIKE '" + id + "';");
						cbCity.removeAllItems();
						while (rs.next())
						{
							cbCity.addItem(new ComboItem(rs.getString(1), rs.getString(2)));
						}
						rs.close();
					}
					catch (SQLException e1)
					{
						e1.printStackTrace();
					}
				}
			});

			/*
			 * ***********************************************************
			 * Заполнение городов
			 * ***********************************************************
			 */
			rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title FROM city WHERE district_id LIKE '" + data.get(0) + "';");

			while (rs.next())
			{
				cbCity.addItem(new ComboItem(rs.getString(1), rs.getString(2)));

				if (data.get(1).equals(rs.getString(1)))
					cbCity.setSelectedIndex(cbCity.getItemCount() - 1);
			}

			comboPanel.add(cbCity);

			cbCity.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setButtonEnabled(true);
				}
			});
		}

		/*
		 * ***********************************************************
		 * Информация о АЗС
		 * - наименование
		 * - адрес
		 * - телефон
		 * - принадлежность к сетевой организации
		 * - статус АЗС
		 * ***********************************************************
		 */

		JPanel label = new JPanel(new GridLayout(5, 1, 5, 5));
		titlePanel.add(label, BorderLayout.WEST);
		label.add(new JLabel("Наименование:"));
		label.add(new JLabel("Адрес:"));
		label.add(new JLabel("Телефон:"));
		label.add(new JLabel("Сетевая организация:"));
		label.add(new JLabel("Статус:"));

		JPanel text = new JPanel(new GridLayout(5, 1, 5, 5));
		titlePanel.add(text, BorderLayout.CENTER);

		text_name = new JTextField(data.get(4));
		text_address = new JTextField(data.get(5));
		text_tel = new JTextField(data.get(6));

		text.add(text_name);
		text.add(text_address);
		text.add(text_tel);

		text_name.getDocument().addDocumentListener(new DocumentListener()
		{

			public void changedUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}

			public void removeUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}

			public void insertUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}
		});

		text_address.getDocument().addDocumentListener(new DocumentListener()
		{

			public void changedUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}

			public void removeUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}

			public void insertUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}
		});

		text_tel.getDocument().addDocumentListener(new DocumentListener()
		{

			public void changedUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}

			public void removeUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}

			public void insertUpdate(DocumentEvent e)
			{
				setButtonEnabled(true);
			}
		});

		/*
		 * ***********************************************************
		 * Сетевые организация АЗС по Калужской области
		 * ***********************************************************
		 */
		cbComm = new JComboBox();
		{
			rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title FROM commercial;");

			while (rs.next())
			{
				cbComm.addItem(new ComboItem(rs.getString(1), rs.getString(2)));

				if (data.get(2).equals(rs.getString(1)))
					cbComm.setSelectedIndex(cbComm.getItemCount() - 1);
			}

			text.add(cbComm);

			cbComm.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setButtonEnabled(true);
				}
			});
		}

		/*
		 * ***********************************************************
		 * Статус АЗС
		 * ***********************************************************
		 */
		cbStatus = new JComboBox();
		{

			cbStatus.addItem(new ComboItem("true", "Активен"));
			cbStatus.addItem(new ComboItem("false", "Не используется"));
			if (data.get(3).equals("false"))
				cbStatus.setSelectedIndex(1);

			text.add(cbStatus);

			cbStatus.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					setButtonEnabled(true);
				}
			});
		}

		/*
		 * ***********************************************************
		 * Информация о цене на топливо
		 * ***********************************************************
		 */

		JPanel table = new JPanel(new GridLayout(1, 2, 5, 5));
		this.add(table, BorderLayout.CENTER);

		/*
		 * ***********************************************************
		 * Последняя добавленная информации
		 * + возможность обновление
		 * ***********************************************************
		 */
		JPanel temp_oil = new JPanel(new BorderLayout());
		table.add(temp_oil);

		JPanel oil = new JPanel(new BorderLayout(5, 5));
		temp_oil.add(oil, BorderLayout.NORTH);

		JPanel title_oil = new JPanel(new GridLayout(6, 1, 5, 5));
		oil.add(title_oil, BorderLayout.WEST);

		title_oil.add(new JLabel("Обновлён:"));
		title_oil.add(new JLabel("ДТ:"));
		title_oil.add(new JLabel("АИ-95:"));
		title_oil.add(new JLabel("АИ-92:"));
		title_oil.add(new JLabel("АИ-80:"));
		title_oil.add(new JLabel(""));

		JPanel text_oil = new JPanel(new GridLayout(6, 1, 5, 5));
		oil.add(text_oil, BorderLayout.CENTER);

		text_data = new JTextField();
		text_bdis = new JTextField();
		text_b95 = new JTextField();
		text_b92 = new JTextField();
		text_b80 = new JTextField();

		text_oil.add(text_data);
		text_oil.add(text_bdis);
		text_oil.add(text_b95);
		text_oil.add(text_b92);
		text_oil.add(text_b80);

		JButton btnAddRecord = new JButton("Добавить запись");
		text_oil.add(btnAddRecord);

		btnAddRecord.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				try
				{
					PreparedStatement pst = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).prepareStatement("INSERT INTO change VALUES (?, ?, ?, ?, ?, ?, ?);");

					pst.setInt(2, Integer.parseInt(station_id));

					pst.setString(3, Long.toString(System.currentTimeMillis()));

					pst.setString(4, text_b80.getText());
					pst.setString(5, text_b92.getText());
					pst.setString(6, text_b95.getText());
					pst.setString(7, text_bdis.getText());

					pst.addBatch();

					pst.executeBatch();
					pst.close();

					refreshCostOil();
				}
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		/*
		 * ***********************************************************
		 * Таблица со всеми изменениями цены
		 * ***********************************************************
		 */

		jDataTable = new JTable()
		{
			private static final long	serialVersionUID	= 1L;

			/*
			 * Запрет на редактирование 1 ячейки(ID)
			 */
			@Override
			public boolean isCellEditable(int row, int column)
			{
				if (column > 0)
					return true;
				else
					return false;
			}
		};

		table.add(new JScrollPane(jDataTable));

		refreshCostOil();

		// Реализация PopUp Menu
		jDataTable.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent Me)
			{
				if (0 < jDataTable.getSelectedRows().length && Me.isMetaDown())
				{
					JPopupMenu Pmenu = new JPopupMenu();

					// количество выделенных записей
					// для удобства пользователей
					JMenuItem numberRecords = new JMenuItem("Выделено: " + jDataTable.getSelectedRows().length);
					Pmenu.add(numberRecords);

					// удаляем выделенные элементы
					JMenuItem delRecords = new JMenuItem("Удалить:" + jDataTable.getSelectedRows().length);
					Pmenu.add(delRecords);

					// показываем PopUp меню
					Pmenu.show(Me.getComponent(), Me.getX(), Me.getY());

					// удаление записей
					delRecords.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							// Сообщение
							// варианты ответа пользователя
							String[] choices = { "Да", "Нет" };

							// создание сообщения
							int response = JOptionPane.showOptionDialog(null // В
																				// центре
																				// окна
							, "Вы уверены, что хотите удалить?" // Сообщение
							, "" // Титульник сообщения
							, JOptionPane.YES_NO_OPTION // Option type
							, JOptionPane.PLAIN_MESSAGE // messageType
							, null // Icon (none)
							, choices // Button text as above.
							, "" // Default button's labelF
							);

							// обработка ответа пользователя
							switch (response)
							{
								case 0:
									// удаление
									for (int i = 0; i < jDataTable.getSelectedRows().length; i++)
									{
										String id = jDataTable.getValueAt(jDataTable.getSelectedRows()[i], 0).toString();

										try
										{
											DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("DELETE FROM change WHERE id = '" + id + "';");
										}
										catch (SQLException e1)
										{
											e1.printStackTrace();
										}
									}
									try
									{
										refreshCostOil();
									}
									catch (SQLException e1)
									{
										e1.printStackTrace();
									}
									break;
								case 1:
									// ничего не удаляем
									break;
								case -1:
									// окно было закрыто - ничего не удаляем
								default:
									break;
							}

						}
					});
				}
			}
		});

		/*
		 * ***********************************************************
		 * Панель кнопок
		 * ***********************************************************
		 */

		JPanel btPanel = new JPanel(new GridLayout(1, 1));
		this.add(btPanel, BorderLayout.SOUTH);

		JButton cancel = new JButton("Назад/Отмена");
		btPanel.add(cancel);
		cancel.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					main.ini();
				}
				catch (SQLException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});

		update = new JButton("Обновить");
		update.setEnabled(false);
		btPanel.add(update);
		update.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateTitle(false);
			}

		});

		update_close = new JButton("Обновить и закрыть");
		update_close.setEnabled(false);
		btPanel.add(update_close);
		update_close.addActionListener(new ActionListener()
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				updateTitle(true);
			}
		});
	}

	/**
	 * Активация или деактивация кнопок с функционалом "обновить данные"
	 * 
	 * @param b
	 *            - true - активация; false - деактивация
	 */
	private void setButtonEnabled(boolean b)
	{
		update.setEnabled(b);
		update_close.setEnabled(b);
	}

	/**
	 * Обновление данных поставщика(АЗС не включены).<br>
	 * + возможность перейти к списку всех АЗС
	 * 
	 * @param b
	 *            - true - с переходом ко всем АЗС; false - без перехода
	 */
	private void updateTitle(boolean b)
	{
		try
		{
			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET title = '" + text_name.getText() + "' WHERE id LIKE '" + station_id + "';");

			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET address = '" + text_address.getText() + "' WHERE id LIKE '" + station_id + "';");

			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET tel = '" + text_tel.getText() + "' WHERE id LIKE '" + station_id + "';");

			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET district_id = '" + ((ComboItem) cbDistrict.getSelectedItem()).getValue() + "' WHERE id LIKE '" + station_id + "';");

			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET city_id = '" + ((ComboItem) cbCity.getSelectedItem()).getValue() + "' WHERE id LIKE '" + station_id + "';");

			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET comm_id = '" + ((ComboItem) cbComm.getSelectedItem()).getValue() + "' WHERE id LIKE '" + station_id + "';");

			DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET active = '" + ((ComboItem) cbStatus.getSelectedItem()).getValue() + "' WHERE id LIKE '" + station_id + "';");

			if (b)
			{
				main.ini();
			}
		}
		catch (SQLException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	/**
	 * Обновление данных в таблице цен + последнее обновление<br>
	 * - id<br>
	 * - Дата<br>
	 * - АИ - 80<br>
	 * - АИ - 92<br>
	 * - АИ - 95<br>
	 * - Дизель<br>
	 * 
	 * @throws SQLException
	 */
	private void refreshCostOil() throws SQLException
	{
		isChangeTable = false;
		Vector<String> header = new Vector<String>();
		{
			header.add("id");
			header.add("Дата");
			header.add("80");
			header.add("92");
			header.add("95");
			header.add("dis");
		}

		Vector<Vector<String>> values = new Vector<Vector<String>>();
		{
			// поиск последнего изменения
			ResultSet time = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT changedate,b80,b92,b95,bdis,id FROM change WHERE station_id LIKE '" + station_id + "';");

			while (time.next())
			{
				Vector<String> item = new Vector<String>();

				item.add(time.getString(6));

				Long temp = Long.parseLong(time.getString(1));
				SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				Date date = new Date(temp);

				item.add(dateFormat.format(date).toString());
				item.add(time.getString(2));
				item.add(time.getString(3));
				item.add(time.getString(4));
				item.add(time.getString(5));

				values.add(0, item);
			}
			time.close();
		}

		DefaultTableModel dtm = (DefaultTableModel) jDataTable.getModel();
		// Сначала данные, потом шапка
		dtm.setDataVector(values, header);
		// задаем ширину каждого столбца, кроме наименования
		// id
		jDataTable.getColumnModel().getColumn(0).setMinWidth(40);
		// дата
		jDataTable.getColumnModel().getColumn(1).setMinWidth(100);
		// топливо
		jDataTable.getColumnModel().getColumn(2).setMinWidth(40);
		jDataTable.getColumnModel().getColumn(3).setMinWidth(40);
		jDataTable.getColumnModel().getColumn(4).setMinWidth(40);
		jDataTable.getColumnModel().getColumn(5).setMinWidth(40);

		jDataTable.getModel().addTableModelListener(new JTableChanged());
		isChangeTable = true;

		/*
		 * **********************************
		 * Последнее обновление
		 * **********************************
		 */

		ResultSet getOil = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT changedate,b80,b92,b95,bdis FROM change WHERE station_id LIKE '" + station_id + "';");

		Long max_time = (long) 0;
		String b80 = "";
		String b92 = "";
		String b95 = "";
		String bdis = "";

		while (getOil.next())
		{
			Long temp = Long.parseLong(getOil.getString(1));

			if (temp > max_time)
			{
				max_time = temp;
				b80 = getOil.getString(2);
				b92 = getOil.getString(3);
				b95 = getOil.getString(4);
				bdis = getOil.getString(5);
			}
		}
		getOil.close();

		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = new Date(max_time);

		text_data.setText(dateFormat.format(date).toString());
		text_data.setEditable(false);

		text_bdis.setText(bdis);
		text_b95.setText(b95);
		text_b92.setText(b92);
		text_b80.setText(b80);
		
		jDataTable.setAutoCreateRowSorter(true);
		
		this.validate();
		this.repaint();

	}

	/**
	 * Обновление бд, после изменения значения в таблице
	 * 
	 * @author kirill
	 * 
	 */
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
					String b80 = jDataTable.getModel().getValueAt(e.getFirstRow(), 2).toString();
					String b92 = jDataTable.getModel().getValueAt(e.getFirstRow(), 3).toString();
					String b95 = jDataTable.getModel().getValueAt(e.getFirstRow(), 4).toString();
					String bdis = jDataTable.getModel().getValueAt(e.getFirstRow(), 5).toString();
					
					try
					{
						SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

						Date date = formatter.parse(jDataTable.getModel().getValueAt(e.getFirstRow(), 1).toString());
						String changedate = Long.toString(date.getTime());

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET b80 = '" + b80 + "' WHERE id LIKE '" + id + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET b92 = '" + b92 + "' WHERE id LIKE '" + id + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET b95 = '" + b95 + "' WHERE id LIKE '" + id + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET bdis = '" + bdis + "' WHERE id LIKE '" + id + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET changedate = '" + changedate + "' WHERE id LIKE '" + id + "';");
					}
					catch (SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					catch (ParseException e1)
					{
						JOptionPane.showMessageDialog(null, "Ошибка в дате, обновление отменено!");
					}
				}
			}
		}

	}
}
