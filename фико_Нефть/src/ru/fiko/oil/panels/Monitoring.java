package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import jxl.write.WriteException;

import ru.fiko.oil.data.OutputData;
import ru.fiko.oil.data.OutputSvod;
import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.ComboItem;
import ru.fiko.oil.supp.ItemStation;

public class Monitoring extends JPanel {
    private static final long serialVersionUID = 1L;

    private JPanel table = null;

    private JComboBox cbDistrict = null;
    private JComboBox cbComm = null;

    // Индексы последних изменений, выпадающих списков
    private int indexDistrict = 0;
    private int indexComm = 0;

    private String[][] label = {
	    { "ОАО Калуганефтепродукт", "0" },
	    { "ООО Газпромнефть-Центр", "1" },
	    { "ООО ТД Альфа-Трейд", "2" },
	    { "ООО Лукойл-Центрнефтепродукт", "3" },
	    { "ИП Палашичев", "4" },
	    { "ООО ТрансАЗС-Сервис", "5" },
	    { "ООО Октан", "6" },
	    { "ИП Пешков", "7" },
	    { "ООО Восток-Ойл", "8" },
	    { "Остальные", "9" } };

    private String[][][] gas = {
	    {
		    /*
		     * Калуганефтепродукт
		     */
		    { "г. Калуга розница", "250" },
		    { "Область розница", "251" },
		    { "г. Боровск", "23" },
		    { "г. Обнинск", "212" },
		    { "г. Обнинск", "213" },
		    { "г. Обнинск", "214" },
		    { "Жиздринский р-н", "58" },
		    { "Жиздринский р-н", "59" },
		    { "Бабынинский р-н", "5" },
		    { "Дзержинский р-н", "40" },
		    { "Козельский р-н", "90" } },
	    {
		    /*
		     * Газпромнефть
		     */
		    { "г. Калуга розница", "252" },
		    { "Область розница", "253" },
		    { "Дзержинский р-н", "41" },
		    { "Дзержинский р-н", "42" },
		    { "Дзержинский р-н", "43" },
		    { "Износковский р-н", "85" },
		    { "Жиздринский р-н", "62" },
		    { "Барятинский р-н", "14" } },
	    {
		    /*
		     * ТД Альфа-Трейд
		     */
		    { "г. Калуга", "195" },
		    { "г. Калуга", "196" },
		    { "г. Калуга", "197" },
		    { "г. Калуга", "198" },
		    { "в т.ч. мини АЗС", "254" } },
	    {
	    /*
	     * Лукойл
	     */
	    { "г. Калуга розница", "255" }, { "Область розница", "256" } },
	    {
		    /*
		     * ИП Палашичев
		     */
		    { "г. Калуга", "199" },
		    { "г. Калуга", "200" },
		    { "г. Калуга", "201" },
		    { "г. Калуга", "202" },
		    { "г. Калуга", "203" },
		    { "г. Калуга", "204" },
		    { "г. Калуга", "205" },
		    { "Бабынинский р-н", "9" } },
	    {
		    /*
		     * ТрансАЗС-сервис
		     */
		    { "г. Обнинск", "215" },
		    { "г. Обнинск", "216" },
		    { "г. Обнинск", "217" } },
	    {
	    /*
	     * Октан
	     */
	    { "г. Обнинск", "218" }, { "г. Обнинск", "219" } },
	    {
		    /*
		     * ИП Пешков
		     */
		    { "Сухиничский р-н", "144" },
		    { "Ульяновский р-н", "155" },
		    { "Перемышельский р-н", "138" } },
	    {
		    /*
		     * Восток-Ойл
		     */
		    { "г. Мосальск", "133" },
		    { "Малоярославецкий", "123" },
		    { "Мещовский р-н", "130" },
		    { "Куйбышевский р-н", "94" } },
	    {
		    /*
		     * Остальные
		     */
		    { "Бабынинский р-н", "13" },
		    { "Изнсковский р-н", "86" },
		    { "Ферзиковский р-н", "157" },
		    { "Спас-Деменский р-н", "142" },
		    { "Хвастовичский р-н", "161" },
		    { "Хвастовичский р-н", "160" } }

    };

    public ItemStation current = null;

    public Monitoring() throws SQLException, ClassNotFoundException {
	Class.forName("org.sqlite.JDBC");

	/*
	 * инициализация панели выведены в отдельный метод за счет необходимости
	 * переопределение оной, после ухода из меню редактирование отдельной
	 * станции АЗС
	 */
	ini();
    }

    /**
     * Инициализицаия панели с АЗС
     * 
     * @throws SQLException
     */
    public void ini() throws SQLException {
	this.removeAll();

	// настройки по умолчанию
	this.setLayout(new BorderLayout());
	this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	/*
	 * Хранит два выпадающих списка для фильтрации АЗС
	 */
	JPanel toolsPanel = new JPanel(new BorderLayout(5, 5));
	this.add(toolsPanel, BorderLayout.NORTH);

	/*
	 * ***********************************************************
	 * Выпадающий список для фильтрации АЗС по: <br> - районам Калужской
	 * области - сетевым организациям
	 * ***********************************************************
	 */

	JPanel comboPanel = new JPanel(new GridLayout(1, 2));
	toolsPanel.add(comboPanel, BorderLayout.CENTER);

	// Список районов Калужской области
	cbDistrict = new JComboBox();
	{
	    // // для вывода всех АЗС области
	    // cbDistrict.addItem(new ComboItem("%", "Калужская область"));
	    //
	    // ResultSet rs = DriverManager
	    // .getConnection("jdbc:sqlite:" + Oil.PATH).createStatement()
	    // .executeQuery("SELECT id,title FROM district;");
	    //
	    // while (rs.next())
	    // cbDistrict.addItem(new ComboItem(rs.getString(1), rs
	    // .getString(2)));

	    for (int i = 0; i < label.length; i++)
		cbDistrict.addItem(new ComboItem(label[i][1], label[i][0]));

	    if (indexDistrict < cbDistrict.getItemCount())
		cbDistrict.setSelectedIndex(indexDistrict);

	    comboPanel.add(cbDistrict);

	    cbDistrict.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    indexDistrict = cbDistrict.getSelectedIndex();
		    try {
			refreshDataPanel();
		    } catch (SQLException e1) {
			e1.printStackTrace();
		    }
		}
	    });
	}

	JButton btn = new JButton("Вывод в excel");
	toolsPanel.add(btn, BorderLayout.EAST);
	btn.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		try {
		    new OutputSvod();
		} catch (WriteException e) {
		    e.printStackTrace();
		} catch (IOException e) {
		    e.printStackTrace();
		} catch (ParseException e) {
		    e.printStackTrace();
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	});

	// Формирование информации о АЗС
	refreshDataPanel();
    }

    /**
     * Обновление данных, учитывая район и сетевого поставщика<br>
     * Вывод:<br>
     * - Дата<br>
     * - Состояние<br>
     * - Наименование<br>
     * - Топливо<br>
     * 
     * @throws SQLException
     */
    public void refreshDataPanel() throws SQLException {
	/*
	 * Удаляет компоненты с инф о АЗС
	 */
	if (this.getComponentCount() > 1)
	    this.remove(1);

	/*
	 * переопределяем панель с АЗС
	 */
	table = new JPanel();
	table.setLayout(new BoxLayout(table, BoxLayout.PAGE_AXIS));

	/*
	 * Данные для фильтрации АЗС
	 */
	int label_id = Integer.parseInt(((ComboItem) cbDistrict
		.getItemAt(cbDistrict.getSelectedIndex())).getValue());

	/*
	 * Вывод данных АЗС
	 */
	for (int index = 0; index < gas[label_id].length; index++) {
	    /*
	     * поиск последнего изменения
	     */
	    ResultSet time = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT changedate,b80,b92,b95,bdis,id FROM change WHERE station_id LIKE '"
				    + gas[label_id][index][1] + "';");

	    Long max_time = (long) 0;
	    String b80 = "";
	    String b92 = "";
	    String b95 = "";
	    String bdis = "";
	    String changeid = "";

	    while (time.next()) {
		Long temp = Long.parseLong(time.getString("changedate"));

		if (temp > max_time) {
		    max_time = temp;
		    b80 = time.getString("b80");
		    b92 = time.getString("b92");
		    b95 = time.getString("b95");
		    bdis = time.getString("bdis");
		    changeid = time.getString("id");
		}
	    }
	    time.close();

	    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	    Date date = new Date(max_time);

	    ItemStation item = new ItemStation();

	    ResultSet rs = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT id,title,active,address FROM station WHERE id LIKE '"
				    + gas[label_id][index][1] + "';");
	    if (rs.next()) {
		// id
		item.setStationId(rs.getString("id"));
		// id последнего изменения
		// item.setChangeId(changeid);
		// дата последнего изменения
		item.setDate((dateFormat.format(date).toString()));
		// Состояние
		item.setStatus(rs.getString("active"));
		// Наименование
		item.setTitle(rs.getString("title") + " - "
			+ gas[label_id][index][0]);
		// Адрес месторасположения
		item.setAddress(rs.getString("address"));
		// Топливо
		item.setB80(b80);
		item.setB92(b92);
		item.setB95(b95);
		item.setBdis(bdis);
		item.setChangeId(changeid);

		// создание карточки
		item.initialization();
		table.add(item);
	    }
	    rs.close();
	}
	// rs.close();

	/*
	 * Скроллбар из-за количества записей, не влезавших в видимую область +
	 * увеличина прокрутка колесом мыши
	 */
	JScrollPane pane = new JScrollPane(table);
	JScrollBar jsp = pane.getVerticalScrollBar();
	jsp.setUnitIncrement(20);

	this.add(pane, BorderLayout.CENTER);

	this.repaint();
	this.validate();
    }

    /**
     * Удаление поставщика со всеми ценами
     * 
     * @param station_id
     *            - id удаляемого поставщика
     * @throws SQLException
     */
    @SuppressWarnings("unused")
    private void delStation(String station_id) throws SQLException {
	DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeUpdate(
			"DELETE FROM change WHERE station_id = '" + station_id
				+ "';");
	DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeUpdate(
			"DELETE FROM station WHERE id = '" + station_id + "';");

	refreshDataPanel();
    }
}
