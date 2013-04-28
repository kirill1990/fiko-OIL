package ru.fiko.oil.main;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import jxl.write.WriteException;

import ru.fiko.oil.data.ConnectionToBD;
import ru.fiko.oil.data.OutputSvod;
import ru.fiko.oil.panels.Citys;
import ru.fiko.oil.panels.Districts;
import ru.fiko.oil.panels.Main;
import ru.fiko.oil.panels.Stations;

/**
 * 
 * 
 * 
 * @author kirill
 * 
 */
public class Oil extends JFrame {
    private static final long serialVersionUID = 7076741630948996202L;

    public static final String PATH = "oil.db";

    private static int WIDTH = 830;
    private static int HEIGHT = 550;

    private static Connection conn = null;
    private static Statement stat = null;

    public Oil() throws SQLException, ClassNotFoundException,
	    InstantiationException, IllegalAccessException,
	    UnsupportedLookAndFeelException {
	/*
	 * для владельцев ОС windows...
	 */
	UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

	/*
	 * Инициализация параметров окна
	 */
	this.setSize(WIDTH, HEIGHT);
	// всегда по центру экрана
	this.setLocation(
		(Toolkit.getDefaultToolkit().getScreenSize().width - WIDTH) / 2,
		(Toolkit.getDefaultToolkit().getScreenSize().height - HEIGHT) / 2);
	this.setTitle("Нефтепродукты");
	this.setExtendedState(JFrame.MAXIMIZED_BOTH);
	this.setVisible(true);

	/*
	 * Уничтожение процесса после закрытия окна
	 */
	this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	this.addWindowListener(new WindowAdapter() {
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

	/*
	 * Заполнение формы
	 */

	JTabbedPane jtp = new JTabbedPane();
	getContentPane().add(jtp);
	// jtp.setTabPlacement(JTabbedPane.LEFT);

	jtp.add("Поставщики", new Stations());
	jtp.add("Базовые настройки", new Main());
	jtp.add("Главы районов", new Districts());
	jtp.add("Города", new Citys());

	validate();
    }

    /**
     * Подключение к бд, запуск окна
     * 
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws UnsupportedLookAndFeelException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws IOException 
     * @throws WriteException 
     */
    public static void main(String[] args) throws SQLException,
	    ClassNotFoundException, InstantiationException,
	    IllegalAccessException, UnsupportedLookAndFeelException,
	    ParserConfigurationException,
	    TransformerException, IOException, WriteException {
	Class.forName("org.sqlite.JDBC");
	conn = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH);
	stat = conn.createStatement();

	/*
	 * проверка наличие бд. если нет, то создаёт новые
	 */
	stat.executeUpdate("CREATE TABLE IF NOT EXISTS district(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "title STRING, "
		+ "glava_fio STRING, "
		+ "glava_tel STRING, "
		+ "zam_fio STRING, "
		+ "zam_tel STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS commercial(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "title STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS city(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "title STRING, " + "x STRING, " + "y STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS client(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "city_id INTEGER REFERENCES city(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "title STRING, " + "address STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS station(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "city_id INTEGER REFERENCES city(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "comm_id INTEGER REFERENCES commercial(id) ON UPDATE CASCADE , "
		+ "active STRING, "
		+ "title STRING, "
		+ "address STRING, "
		+ "tel STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS change(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "station_id INTEGER REFERENCES station(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "changedate STRING, "
		+ "b80 STRING, "
		+ "b92 STRING, "
		+ "b95 STRING, " + "bdis STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS main(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "texturl STRING, "
		+ "url STRING, "
		+ "text STRING, "
		+ "bname STRING, "
		+ "baddress STRING, "
		+ "b80 STRING, "
		+ "b92 STRING, "
		+ "b95 STRING, "
		+ "bdis STRING, "
		+ "binfo STRING, " + "orgname STRING, " + "orgaddress STRING);");

	stat.executeUpdate("CREATE TABLE IF NOT EXISTS optov(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, "
		+ "comm_id INTEGER REFERENCES commercial(id) ON UPDATE CASCADE ON DELETE CASCADE, "
		+ "changedate STRING,"
		+ "b80_t STRING,"
		+ "b92_t STRING,"
		+ "b95_t STRING,"
		+ "bdis_mc_t STRING,"
		+ "bdis_winter_t STRING,"
		+ "bdis_leto1_t STRING,"
		+ "bdis_leto2_t STRING,"
		+ "b80_l STRING,"
		+ "b92_l STRING,"
		+ "b95_l STRING,"
		+ "bdis_mc_l STRING,"
		+ "bdis_winter_l STRING,"
		+ "bdis_leto1_l STRING,"
		+ "bdis_leto2_l STRING);");

	stat.close();
	conn.close();
	// ConnectionToBD bd = new ConnectionToBD();

	//new Oil();

	new OutputSvod();
	
	// new OutputData();

	// try {
	//
	// SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	//
	// Date test = formatter.parse(formatter.format(new
	// Date(System.currentTimeMillis())));
	//
	// Date plusone = new Date(test.getTime()+86400000);
	//
	// Date date1 = formatter.parse("19.04.2013");
	// Date date2 = formatter.parse("26.04.2013");
	// //86400000
	// //604800000
	//
	// System.out.println(date1.getTime()-date2.getTime());
	// } catch (ParseException e) {
	// e.printStackTrace();
	// }

	// try
	// {
	// SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	// Date date = formatter.parse("01.02.2013");
	// // Преобразование даты в строку.
	//
	// System.out.println(date.getTime());
	//
	// }
	// catch (ParseException e)
	// {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
	// // get current date time with Date()
	// Date date = new Date();
	//
	// String d = Long.toString(System.currentTimeMillis());
	// System.out.println(d);
	// date.setTime(Long.parseLong(d));
	//
	// System.out.println(dateFormat.format(date));

    }
}
