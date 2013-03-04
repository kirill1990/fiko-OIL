package ru.fiko.oil.main;

import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ru.fiko.oil.panels.Stations;

public class Oil extends JFrame
{
	private static final long	serialVersionUID	= 7076741630948996202L;

	public static final String	PATH				= "oil.db";

	private static int			WIDTH				= 830;
	private static int			HEIGHT				= 500;

	private static Connection	conn				= null;
	private static Statement	stat				= null;

	// private ResultSet rs = null;
	// private PreparedStatement pst = null;

	public Oil() throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		/*
		 * для владельцев ОС windows...
		 */
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		/*
		 * Инициализация параметров окна
		 */
		this.setSize(WIDTH, HEIGHT);
		// всегда по центру экрана
		this.setLocation((Toolkit.getDefaultToolkit().getScreenSize().width - WIDTH) / 2, (Toolkit.getDefaultToolkit().getScreenSize().height - HEIGHT) / 2);
		this.setTitle("Нефтепродукты");
		this.setVisible(true);

		/*
		 * Уничтожение процесса после закрытия окна
		 */
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		JTabbedPane jtp = new JTabbedPane();
		jtp.setTabPlacement(JTabbedPane.LEFT);

		jtp.add("АЗС", new Stations());

		getContentPane().add(jtp);
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
	 */
	public static void main(String[] args) throws SQLException, ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		Class.forName("org.sqlite.JDBC");
		conn = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH);
		stat = conn.createStatement();

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS district(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title STRING, glava_fio STRING, glava_tel STRING, zam_fio STRING, zam_tel STRING);");

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS commercial(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title STRING);");

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS city(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, title STRING, x STRING, y STRING);");

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS client(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, city_id INTEGER REFERENCES city(id) ON UPDATE CASCADE ON DELETE CASCADE, title STRING, address STRING);");

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS station(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, city_id INTEGER REFERENCES city(id) ON UPDATE CASCADE ON DELETE CASCADE, comm_id INTEGER REFERENCES commercial(id) ON UPDATE CASCADE , active STRING, title STRING, address STRING, tel STRING);");

		stat.executeUpdate("CREATE TABLE IF NOT EXISTS change(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, station_id INTEGER REFERENCES station(id) ON UPDATE CASCADE ON DELETE CASCADE, changedate STRING, b80 STRING, b92 STRING, b95 STRING, bdis STRING);");

		new Oil();
		

		// ConnectionToBD bd = new ConnectionToBD();

//		try
//		{
//			SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
//			Date date = formatter.parse("01.02.2013");
//			// Преобразование даты в строку.
//
//			System.out.println(date.getTime());
//
//		}
//		catch (ParseException e)
//		{
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
//		// get current date time with Date()
//		Date date = new Date();
//
//		String d = Long.toString(System.currentTimeMillis());
//		System.out.println(d);
//		date.setTime(Long.parseLong(d));
//
//		System.out.println(dateFormat.format(date));

	}

}
