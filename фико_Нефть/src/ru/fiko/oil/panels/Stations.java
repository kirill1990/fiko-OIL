package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import ru.fiko.oil.data.OutputData;
import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.ComboItem;

/**
 * Панель с данными по "Поставщикам"
 * 
 * @author kirill
 * 
 */
public class Stations extends JPanel
{
	private static final long	serialVersionUID	= 1L;

	private JTable				jDataTable			= null;

	private JComboBox			cbDistrict			= null;
	private JComboBox			cbComm				= null;

	// Индексы последних изменений, выпадающих списков
	private int					indexDistrict		= 0;
	private int					indexComm			= 0;

	public Stations() throws SQLException, ClassNotFoundException
	{
		Class.forName("org.sqlite.JDBC");

		ini();
	}

	/**
	 * Инициализицаия панели
	 * 
	 * @throws SQLException
	 */
	public void ini() throws SQLException
	{
		this.removeAll();
		this.setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel toolsPanel = new JPanel(new BorderLayout(5, 5));
		this.add(toolsPanel, BorderLayout.NORTH);

		/*
		 * ***********************************************************
		 * Выпадающий список для фильтрации АЗС по: <br>
		 * - районам Калужской области
		 * - сетевым организациям
		 * ***********************************************************
		 */

		JPanel comboPanel = new JPanel(new GridLayout(1, 2));
		toolsPanel.add(comboPanel, BorderLayout.CENTER);

		// Список районов Калужской области
		cbDistrict = new JComboBox();
		{
			// для вывода всех АЗС области
			cbDistrict.addItem(new ComboItem("%", "Калужская область"));

			ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title FROM district;");

			while (rs.next())
				cbDistrict.addItem(new ComboItem(rs.getString(1), rs.getString(2)));

			if (indexDistrict < cbDistrict.getItemCount())
				cbDistrict.setSelectedIndex(indexDistrict);

			comboPanel.add(cbDistrict);

			cbDistrict.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					indexDistrict = cbDistrict.getSelectedIndex();
					try
					{
						refreshTable();
					}
					catch (SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		// Сетевые организация АЗС по Калужской области
		cbComm = new JComboBox();
		{
			// для вывода всех АЗС области
			cbComm.addItem(new ComboItem("%", "Все АЗС"));

			ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title FROM commercial;");

			while (rs.next())
				cbComm.addItem(new ComboItem(rs.getString(1), rs.getString(2)));

			if (indexComm < cbComm.getItemCount())
				cbComm.setSelectedIndex(indexComm);

			comboPanel.add(cbComm);

			cbComm.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					indexComm = cbComm.getSelectedIndex();
					try
					{
						refreshTable();
					}
					catch (SQLException e1)
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			});
		}

		/*
		 * ***********************************************************
		 * Кнопки редактирования АЗС
		 * - добавление новой азс
		 * - реактирование
		 * - удаление
		 * ***********************************************************
		 */

		JPanel btnPanel = new JPanel(new BorderLayout());

		JPanel temp_btnPanel = new JPanel(new BorderLayout());
		// temp_btnPanel.add(btnPanel, BorderLayout.EAST);

		toolsPanel.add(temp_btnPanel, BorderLayout.SOUTH);

		JButton addStation = new JButton("Добавить");
		JButton changeStation = new JButton("Редактировать");
		JButton delStation = new JButton("Удалить");

		addStation.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				try
				{
					PreparedStatement pst = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).prepareStatement("INSERT INTO station VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

					pst.setInt(2, 0);

					pst.setInt(3, 0);

					pst.setInt(4, 5);

					pst.setString(5, "false");

					pst.setString(6, "Новая");
					pst.setString(7, "Новая");
					pst.setString(8, "Новая");

					pst.addBatch();
					pst.executeBatch();
					pst.close();

					refreshTable();
				}
				catch (SQLException e)
				{
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});

		btnPanel.add(addStation, BorderLayout.WEST);
		btnPanel.add(changeStation, BorderLayout.CENTER);
		btnPanel.add(delStation, BorderLayout.EAST);

		changeStation.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent arg0)
			{
				if (jDataTable.getSelectedRows().length > 0)
				{
					try
					{
						String id = jDataTable.getValueAt(jDataTable.getSelectedRow(), 0).toString();
						editableStation(id);
					}
					catch (SQLException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

		delStation.addActionListener(new ActionListener()
		{

			@Override
			public void actionPerformed(ActionEvent e)
			{
				if (jDataTable.getSelectedRows().length > 0)
				{
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
									delStation(id);
								}
								catch (SQLException e1)
								{
									e1.printStackTrace();
								}
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
			}
		});

		/*
		 * ***********************************************************
		 * Таблица данных
		 * ***********************************************************
		 */

		JPanel tablePanel = new JPanel(new GridLayout(1, 1, 5, 0));
		this.add(tablePanel, BorderLayout.CENTER);

		jDataTable = new JTable()
		{
			private static final long	serialVersionUID	= 1L;

			/*
			 * Запрет на редактирование ячеек
			 */
			@Override
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};

		//Двойной клик
		jDataTable.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (e.getClickCount() == 2)
				{
					try
					{
						if (jDataTable.getSelectedRow() > -1)
						{
							String id = jDataTable.getValueAt(jDataTable.getSelectedRow(), 0).toString();
							editableStation(id);
						}
					}
					catch (SQLException e1)
					{
						e1.printStackTrace();
					}
				}
			}
		});

		// PopUP
		jDataTable.addMouseListener(new MouseAdapter()
		{
			public void mouseReleased(MouseEvent Me)
			{
				JPopupMenu Pmenu = new JPopupMenu();

				// Вывод инфморации
				JMenuItem output = new JMenuItem("Вывод в xml");
				Pmenu.add(output);
				

				// Разделитель
				JMenuItem _Records = new JMenuItem("----------");
				Pmenu.add(_Records);

				// Добавление новой станции
				JMenuItem addRecords = new JMenuItem("Добавить станцию");
				Pmenu.add(addRecords);
				
				output.addActionListener(new ActionListener()
				{
					
					@Override
					public void actionPerformed(ActionEvent arg0)
					{
						try
						{
							new OutputData();
						}
						catch (FileNotFoundException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (ClassNotFoundException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (ParserConfigurationException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (TransformerException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						catch (SQLException e)
						{
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						JOptionPane.showMessageDialog(null, "Готово");
					}
				});

				// Добавление новой станции
				addRecords.addActionListener(new ActionListener()
				{
					public void actionPerformed(ActionEvent e)
					{
						try
						{
							PreparedStatement pst = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).prepareStatement("INSERT INTO station VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

							pst.setInt(2, 0);

							pst.setInt(3, 0);

							pst.setInt(4, 5);

							pst.setString(5, "false");

							pst.setString(6, "Новая");
							pst.setString(7, "Новая");
							pst.setString(8, "Новая");

							pst.addBatch();
							pst.executeBatch();
							pst.close();

							refreshTable();
						}
						catch (SQLException e1)
						{
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});

				if (0 < jDataTable.getSelectedRows().length && Me.isMetaDown())
				{

					// Удаление станции
					JMenuItem deletRecords = new JMenuItem("Удалить:" + jDataTable.getSelectedRows().length);
					Pmenu.add(deletRecords);

					// Активация станции
					JMenuItem activeRecords = new JMenuItem("Активировать:" + jDataTable.getSelectedRows().length);
					Pmenu.add(activeRecords);

					// Деактивация станции
					JMenuItem deactiveRecords = new JMenuItem("Деактивировать:" + jDataTable.getSelectedRows().length);
					Pmenu.add(deactiveRecords);

					// показываем PopUp меню
					Pmenu.show(Me.getComponent(), Me.getX(), Me.getY());

					// Удаление станции
					deletRecords.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
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
									try
									{
										// удаление
										for (int i = 0; i < jDataTable.getSelectedRows().length; i++)
										{
											String id = jDataTable.getValueAt(jDataTable.getSelectedRows()[i], 0).toString();

											delStation(id);
										}
										refreshTable();
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

					// Активация станции
					activeRecords.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								for (int i = 0; i < jDataTable.getSelectedRows().length; i++)
								{
									String id = jDataTable.getValueAt(jDataTable.getSelectedRows()[i], 0).toString();

									DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET active = 'true' WHERE id LIKE '" + id + "';");
								}
								refreshTable();
							}
							catch (SQLException e1)
							{
								e1.printStackTrace();
							}
						}
					});

					// Деактивация станции
					deactiveRecords.addActionListener(new ActionListener()
					{
						public void actionPerformed(ActionEvent e)
						{
							try
							{
								for (int i = 0; i < jDataTable.getSelectedRows().length; i++)
								{
									String id = jDataTable.getValueAt(jDataTable.getSelectedRows()[i], 0).toString();

									DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE station SET active = 'false' WHERE id LIKE '" + id + "';");
								}
								refreshTable();
							}
							catch (SQLException e1)
							{
								e1.printStackTrace();
							}
						}
					});
				}

				if (Me.isMetaDown())
					Pmenu.show(Me.getComponent(), Me.getX(), Me.getY());
			}
		});

		tablePanel.add(new JScrollPane(jDataTable), null);
		refreshTable();

		this.repaint();
	}

	/**
	 * Обновление данных в таблице, учитывая район и сетевого поставщика<br>
	 * Вывод:<br>
	 * - id<br>
	 * - Дата<br>
	 * - Состояние<br>
	 * - Наименование<br>
	 * - топливо<br>
	 * 
	 * @throws SQLException
	 */
	private void refreshTable() throws SQLException
	{
		Vector<String> header = new Vector<String>();
		{
			header.add("id");
			header.add("Дата");
			header.add("Состояние");
			header.add("Наименование");
			header.add("80");
			header.add("92");
			header.add("95");
			header.add("dis");
		}

		Vector<Vector<String>> values = new Vector<Vector<String>>();
		{
			String district_id = ((ComboItem) cbDistrict.getItemAt(cbDistrict.getSelectedIndex())).getValue();
			String comm_id = ((ComboItem) cbComm.getItemAt(cbComm.getSelectedIndex())).getValue();

			ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id,title,active FROM station WHERE district_id LIKE '" + district_id + "' AND comm_id LIKE '" + comm_id + "';");

			while (rs.next())
			{
				// поиск последнего изменения
				ResultSet time = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT changedate,b80,b92,b95,bdis FROM change WHERE station_id LIKE '" + rs.getString(1) + "';");

				Long max_time = (long) 0;
				String b80 = "";
				String b92 = "";
				String b95 = "";
				String bdis = "";

				while (time.next())
				{
					Long temp = Long.parseLong(time.getString(1));

					if (temp > max_time)
					{
						max_time = temp;
						b80 = time.getString(2);
						b92 = time.getString(3);
						b95 = time.getString(4);
						bdis = time.getString(5);
					}
				}
				time.close();

				DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
				Date date = new Date(max_time);

				Vector<String> item = new Vector<String>();

				// id
				item.add(rs.getString(1));
				// дата последнего изменения
				item.add(dateFormat.format(date).toString());
				// Состояние
				item.add(rs.getString(3));
				// Наименование
				item.add(rs.getString(2));
				// Топливо
				item.add(b80);
				item.add(b92);
				item.add(b95);
				item.add(bdis);

				values.add(item);
			}

			rs.close();
		}

		// Помещаю в модель таблицы данные
		DefaultTableModel dtm = (DefaultTableModel) jDataTable.getModel();
		// Сначала данные, потом шапка
		dtm.setDataVector(values, header);
		// задаем ширину каждого столбца, кроме наименования
		// id
		jDataTable.getColumnModel().getColumn(0).setMaxWidth(40);
		// дата
		jDataTable.getColumnModel().getColumn(1).setMaxWidth(90);
		// состояние
		jDataTable.getColumnModel().getColumn(2).setMaxWidth(40);
		// топливо
		jDataTable.getColumnModel().getColumn(4).setMaxWidth(40);
		jDataTable.getColumnModel().getColumn(5).setMaxWidth(40);
		jDataTable.getColumnModel().getColumn(6).setMaxWidth(40);
		jDataTable.getColumnModel().getColumn(7).setMaxWidth(40);

		this.validate();
	}

	/**
	 * Панель с информации о АЗС
	 * 
	 * @param station_id
	 *            - id АЗС
	 * @throws SQLException
	 */
	private void editableStation(String station_id) throws SQLException
	{
		this.removeAll();
		this.add(new Station(this, station_id));
		this.repaint();
	}

	/**
	 * Удаление поставщика со всеми ценами
	 * 
	 * @param station_id
	 *            - id удаляемого поставщика
	 * @throws SQLException
	 */
	private void delStation(String station_id) throws SQLException
	{
		DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("DELETE FROM change WHERE station_id = '" + station_id + "';");
		DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("DELETE FROM station WHERE id = '" + station_id + "';");

		refreshTable();
	}
}
