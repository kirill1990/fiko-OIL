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

import ru.fiko.oil.data.OutputData;
import ru.fiko.oil.data.OutputStations;
import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.ComboItem;
import ru.fiko.oil.supp.ItemStation;

/**
 * Панель с данными по "Поставщикам"
 * 
 * @author kirill
 * 
 */
public class Stations extends JPanel {
    private static final long serialVersionUID = 1L;

    private JPanel table = null;

    private JComboBox cbDistrict = null;
    private JComboBox cbComm = null;

    // Индексы последних изменений, выпадающих списков
    private int indexDistrict = 0;
    private int indexComm = 0;

    public ItemStation current = null;

    public Stations() throws SQLException, ClassNotFoundException {
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
	    // для вывода всех АЗС области
	    cbDistrict.addItem(new ComboItem("%", "Калужская область"));

	    ResultSet rs = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH).createStatement()
		    .executeQuery("SELECT id,title FROM district;");

	    while (rs.next())
		cbDistrict.addItem(new ComboItem(rs.getString(1), rs
			.getString(2)));

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

	// Сетевые организация АЗС по Калужской области
	cbComm = new JComboBox();
	{
	    // для вывода всех АЗС области
	    cbComm.addItem(new ComboItem("%", "Все АЗС"));

	    ResultSet rs = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH).createStatement()
		    .executeQuery("SELECT id,title FROM commercial;");

	    while (rs.next())
		cbComm.addItem(new ComboItem(rs.getString(1), rs.getString(2)));

	    if (indexComm < cbComm.getItemCount())
		cbComm.setSelectedIndex(indexComm);

	    comboPanel.add(cbComm);

	    cbComm.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    indexComm = cbComm.getSelectedIndex();
		    try {
			refreshDataPanel();
		    } catch (SQLException e1) {
			e1.printStackTrace();
		    }
		}
	    });
	}
	
	JButton btn = new JButton("Вывод в xml");
	btn.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent arg0) {
		try {
		    outputDat();
		} catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
	
	JButton btn2 = new JButton("Вывод списка АЗС");	
	btn2.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent arg0) {

		try {
		    new OutputStations(new Date(System.currentTimeMillis()));
		    JOptionPane.showMessageDialog(null, "Готово");
		}catch (Exception e) {
		    e.printStackTrace();
		}
	    }
	});
	
	JPanel btnpanel = new JPanel();
	toolsPanel.add(btnpanel, BorderLayout.EAST);
	btnpanel.add(btn2);
	btnpanel.add(btn);

	// Формирование информации о АЗС
	refreshDataPanel();
    }

    /*
     * Popup окно
     */
    public class PopUpTable extends MouseAdapter {
	public void mouseReleased(MouseEvent Me) {
	    JPopupMenu Pmenu = new JPopupMenu();

	    final String station_id = ((ItemStation) Me.getComponent())
		    .getStationId();

	    // Вывод инфморации
	    JMenuItem output = new JMenuItem("Вывод в xml");
	    Pmenu.add(output);

	    // Разделитель
	    JMenuItem _Records = new JMenuItem("Редактировать");
	    Pmenu.add(_Records);

	    // Добавление новой станции
	    JMenuItem addRecords = new JMenuItem("Добавить станцию");
	    Pmenu.add(addRecords);

	    _Records.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
		    try {
			editableStation(station_id);
		    } catch (SQLException e) {
			e.printStackTrace();
		    }
		}
	    });

	    output.addActionListener(new ActionListener() {

		@Override
		public void actionPerformed(ActionEvent arg0) {
		    try {
			outputDat();
		    } catch (Exception e) {
			e.printStackTrace();
		    }
		}
	    });

	    // Добавление новой станции
	    addRecords.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			PreparedStatement pst = DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.prepareStatement(
					"INSERT INTO station VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

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

			refreshDataPanel();
		    } catch (SQLException e1) {
			e1.printStackTrace();
		    }
		}
	    });

	    if (Me.isMetaDown())
		Pmenu.show(Me.getComponent(), Me.getX(), Me.getY());
	}
    }

    /**
     * Вывод данных в xml
     * 
     * @throws SQLException
     * @throws FileNotFoundException
     * @throws ClassNotFoundException
     * @throws ParserConfigurationException
     * @throws TransformerException
     * @throws ParseException
     * @throws TransformerFactoryConfigurationError
     */
    private void outputDat() throws SQLException, FileNotFoundException,
	    ClassNotFoundException, ParserConfigurationException,
	    TransformerException, TransformerFactoryConfigurationError,
	    ParseException {
	/*
	 * Получение текста справочной информации
	 */
	ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeQuery("SELECT text FROM main WHERE id LIKE '1';");

	String text = "";
	if (rs.next()) {
	    text = rs.getString("text");
	}
	rs.close();

	/*
	 * Изменение справочной информации пользователем и вывод данных
	 */

	String str = (String) JOptionPane.showInputDialog(null,
		"Введите справочную информацию: ", "Нефтепродукты", 1, null,
		null, text);

	if (str != null) {
	    DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeUpdate(
			    "UPDATE main SET text = '" + str
				    + "' WHERE id LIKE '1';");

	    new OutputData();
	}
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
	String district_id = ((ComboItem) cbDistrict.getItemAt(cbDistrict
		.getSelectedIndex())).getValue();
	String comm_id = ((ComboItem) cbComm.getItemAt(cbComm
		.getSelectedIndex())).getValue();

	/*
	 * Вывод данных АЗС
	 */
	ResultSet rs = DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeQuery(
			"SELECT id,title,active,address FROM station WHERE district_id LIKE '"
				+ district_id + "' AND comm_id LIKE '"
				+ comm_id + "';");

	while (rs.next()) {
	    /*
	     * поиск последнего изменения
	     */
	    ResultSet time = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT changedate,b80,b92,b95,bdis,id FROM change WHERE station_id LIKE '"
				    + rs.getString(1) + "';");

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

	    // id
	    item.setStationId(rs.getString("id"));
	    // id последнего изменения
	    // item.setChangeId(changeid);
	    // дата последнего изменения
	    item.setDate((dateFormat.format(date).toString()));
	    // Состояние
	    item.setStatus(rs.getString("active"));
	    // Наименование
	    item.setTitle(rs.getString("title"));
	    // Адрес месторасположения
	    item.setAddress(rs.getString("address"));
	    // Топливо
	    item.setB80(b80);
	    item.setB92(b92);
	    item.setB95(b95);
	    item.setBdis(bdis);
	    item.setChangeId(changeid);

	    // PopUP
	    item.addMouseListener(new PopUpTable());

	    // создание карточки
	    item.initialization();
	    table.add(item);
	}
	rs.close();

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
     * Вывод панели с информации о АЗС
     * 
     * @param station_id
     *            - id АЗС
     * @throws SQLException
     */
    private void editableStation(String station_id) throws SQLException {
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
