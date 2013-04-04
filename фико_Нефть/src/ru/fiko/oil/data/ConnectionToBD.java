package ru.fiko.oil.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.Vector;

import javax.swing.ComboBoxModel;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.read.biff.BiffException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.fiko.oil.main.Oil;

public class ConnectionToBD {
    private Connection conn = null;
    private Statement stat = null;
    private ResultSet rs = null;
    private PreparedStatement pst = null;

    public ConnectionToBD() {
	try {
	    Class.forName("org.sqlite.JDBC");
	    conn = DriverManager.getConnection("jdbc:sqlite:oil.db");
	    stat = conn.createStatement();

	    // stat.executeUpdate("CREATE TABLE IF NOT EXISTS district(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title STRING, glava_fio STRING, glava_tel STRING, zam_fio STRING, zam_tel STRING);");
	    //
	    // stat.executeUpdate("CREATE TABLE IF NOT EXISTS commercial(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, title STRING);");
	    //
	    // stat.executeUpdate("CREATE TABLE IF NOT EXISTS city(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, title STRING, x STRING, y STRING);");
	    //
	    // stat.executeUpdate("CREATE TABLE IF NOT EXISTS client(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, city_id INTEGER REFERENCES city(id) ON UPDATE CASCADE ON DELETE CASCADE, title STRING, address STRING);");
	    //
	    // stat.executeUpdate("CREATE TABLE IF NOT EXISTS station(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, district_id INTEGER REFERENCES district(id) ON UPDATE CASCADE ON DELETE CASCADE, city_id INTEGER REFERENCES city(id) ON UPDATE CASCADE ON DELETE CASCADE, comm_id INTEGER REFERENCES commercial(id) ON UPDATE CASCADE , active STRING, title STRING, address STRING, tel STRING);");
	    //
	    // stat.executeUpdate("CREATE TABLE IF NOT EXISTS change(id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, station_id INTEGER REFERENCES station(id) ON UPDATE CASCADE ON DELETE CASCADE, changedate STRING, b80 STRING, b92 STRING, b95 STRING, bdis STRING);");
	    // for(int station_id=966;station_id<=1541;station_id++)
	    // DriverManager.getConnection("jdbc:sqlite:" +
	    // Oil.PATH).createStatement().executeUpdate("DELETE FROM change WHERE id = '"
	    // + station_id + "';");

	    // add();
	    // client();
	    // pro();
	    // stat.executeUpdate("UPDATE title SET id = 100 WHERE id = '2';");
	    // rs = stat.executeQuery("select * from district;");
	    // while (rs.next())
	    // System.out.println(rs.getString(2));

	    // pst =
	    // conn.prepareStatement("INSERT INTO main VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");
	    //
	    // for(int i=2;i<14;i++)
	    // pst.setString(i, "");
	    //
	    // pst.addBatch();
	    //
	    // pst.executeBatch();

	    // stat.executeUpdate("DROP TABLE  client;");

	    // client();
	    //
	    // pst =
	    // conn.prepareStatement("INSERT INTO city VALUES (?, ?, ?, ?, ?);");
	    // pst.setInt(1, 17012);
	    // pst.setInt(2, 17);
	    // pst.setString(3, "Деревня Игнатовское");
	    // pst.setString(4, "0.0");
	    // pst.setString(5, "0.0");
	    // pst.addBatch();
	    // pst.executeBatch();
	    //
	    // pst =
	    // conn.prepareStatement("INSERT INTO city VALUES (?, ?, ?, ?, ?);");
	    // pst.setInt(1, 14019);
	    // pst.setInt(2, 14);
	    // pst.setString(3, "с. Калужская геологоразведочная партия");
	    // pst.setString(4, "0.0");
	    // pst.setString(5, "0.0");
	    // pst.addBatch();
	    // pst.executeBatch();
	    //
	    // pst =
	    // conn.prepareStatement("INSERT INTO city VALUES (?, ?, ?, ?, ?);");
	    // pst.setInt(1, 8014);
	    // pst.setInt(2, 8);
	    // pst.setString(3, "Деревня Слаговищи");
	    // pst.setString(4, "0.0");
	    // pst.setString(5, "0.0");
	    // pst.addBatch();
	    // pst.executeBatch();
	    //
	    // pst =
	    // conn.prepareStatement("INSERT INTO city VALUES (?, ?, ?, ?, ?);");
	    // pst.setInt(1, 19018);
	    // pst.setInt(2, 19);
	    // pst.setString(3, "Деревня Русино");
	    // pst.setString(4, "0.0");
	    // pst.setString(5, "0.0");
	    // pst.addBatch();
	    // pst.executeBatch();
	    //
	    // pst =
	    // conn.prepareStatement("INSERT INTO city VALUES (?, ?, ?, ?, ?);");
	    // pst.setInt(1, 11013);
	    // pst.setInt(2, 11);
	    // pst.setString(3, "Деревня Дошино");
	    // pst.setString(4, "0.0");
	    // pst.setString(5, "0.0");
	    // pst.addBatch();
	    // pst.executeBatch();
	    //
	} catch (SQLException e) {
	    e.printStackTrace();
	} catch (ClassNotFoundException e) {
	    e.printStackTrace();
	} finally {
	    try {
		stat.close();
		conn.close();
	    } catch (Exception e) {
		e.printStackTrace();
	    }
	}
    }

    public void add() throws SQLException {
	pst = conn.prepareStatement("INSERT INTO commercial VALUES (?, ?);");

	// pst.setString(2, "ОАО «Калуганефтепродукт»");
	// pst.setString(2, "ООО «Газпромнефть-Центр»");
	// pst.setString(2, "ООО «Лукойл-центр нефтепродукт»");
	pst.setString(2, "Другие");

	pst.addBatch();

	pst.executeBatch();
    }

    private void reg() throws SQLException {
	File file = new File("таблица.xls");
	try {
	    WorkbookSettings ws = new WorkbookSettings();
	    ws.setLocale(new Locale("ru", "RU"));
	    Workbook workbook = Workbook.getWorkbook(file);

	    Sheet sheet = workbook.getSheet("регион");
	    Cell cell = null;

	    String id_district = ""; // счетчик айди района
	    int id_city = 0; // счетчик айди города по районам

	    // определяем стартовую позицию
	    int beginList = searchBegion(sheet);

	    // определяем конечную позицию
	    int endList = searchEnd(sheet);

	    // начинаем поиск
	    for (int i = beginList; i < endList; i++) {
		// название района\посёлка
		cell = sheet.getCell("B" + Integer.toString(i));
		if (cell.isHidden() != true) {
		    if (cell.getContents() != null) {
			// проверяем на район
			if (cell.getContents().indexOf("Муницип") != -1) {

			    id_city = 0;

			    pst = conn
				    .prepareStatement("INSERT INTO district VALUES (?, ?, ?, ?, ?, ?);");

			    pst.setInt(1, Integer.parseInt(sheet.getCell(
				    "C" + Integer.toString(i)).getContents()));
			    id_district = sheet.getCell(
				    "C" + Integer.toString(i)).getContents();

			    pst.setString(2,
				    sheet.getCell("D" + Integer.toString(i))
					    .getContents());

			    pst.setString(3,
				    sheet.getCell("G" + Integer.toString(i))
					    .getContents());

			    pst.setString(4,
				    sheet.getCell("H" + Integer.toString(i))
					    .getContents());

			    pst.setString(5,
				    sheet.getCell("I" + Integer.toString(i))
					    .getContents());

			    pst.setString(6,
				    sheet.getCell("J" + Integer.toString(i))
					    .getContents());

			    pst.addBatch();

			    pst.executeBatch();

			    // district = doc.createElement("district");
			    //
			    // // индификационный номер района;
			    // district.setAttribute("id", sheet.getCell("C" +
			    // Integer.toString(i)).getContents());
			    //
			    // // cell = sheet.getCell("D" +
			    // Integer.toString(i));
			    // district.setAttribute("main_id", "0");
			    //
			    // district.setAttribute("name", sheet.getCell("D" +
			    // Integer.toString(i)).getContents());
			    //
			    // district.setAttribute("fio", sheet.getCell("G" +
			    // Integer.toString(i)).getContents());
			    //
			    // district.setAttribute("fio_tel",
			    // sheet.getCell("H" +
			    // Integer.toString(i)).getContents());
			    //
			    // district.setAttribute("zam", sheet.getCell("I" +
			    // Integer.toString(i)).getContents());
			    //
			    // district.setAttribute("zam_tel",
			    // sheet.getCell("J" +
			    // Integer.toString(i)).getContents());

			} else {

			    pst = conn
				    .prepareStatement("INSERT INTO city VALUES (?, ?, ?, ?, ?);");

			    String id = sheet
				    .getCell("C" + Integer.toString(i))
				    .getContents();
			    if (id.length() < 2)
				id = id_district + "00" + id;
			    else
				id = id_district + "0" + id;

			    System.out.println(id);
			    System.out.println(sheet.getCell(
				    "D" + Integer.toString(i)).getContents());

			    pst.setInt(1, Integer.parseInt(id));

			    pst.setInt(2, Integer.parseInt(id_district));

			    pst.setString(3,
				    sheet.getCell("D" + Integer.toString(i))
					    .getContents());

			    pst.setString(4,
				    sheet.getCell("E" + Integer.toString(i))
					    .getContents());

			    pst.setString(5,
				    sheet.getCell("F" + Integer.toString(i))
					    .getContents());

			    pst.addBatch();

			    pst.executeBatch();

			    // Создаём тег города
			    // city = doc.createElement("city");
			    //
			    // // Присваиваим индификационный номер
			    // city.setAttribute("id", sheet.getCell("C" +
			    // Integer.toString(i)).getContents());
			    //
			    // city.setAttribute("name", sheet.getCell("D" +
			    // Integer.toString(i)).getContents());
			    //
			    // city.setAttribute("x", sheet.getCell("E" +
			    // Integer.toString(i)).getContents());
			    // city.setAttribute("y", sheet.getCell("F" +
			    // Integer.toString(i)).getContents());
			    //
			    // // увелич порядковый номер на 1
			    // id_city++;
			}
		    }
		}
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (BiffException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void client() throws SQLException {
	File file = new File("таблица.xls");
	try {
	    WorkbookSettings ws = new WorkbookSettings();
	    ws.setLocale(new Locale("ru", "RU"));
	    Workbook workbook = Workbook.getWorkbook(file);

	    Sheet sheet = workbook.getSheet("заказчики");
	    Cell cell = null;

	    String id_district = ""; // счетчик айди района
	    String id_city = ""; // счетчик айди города по районам

	    // определяем стартовую позицию
	    int beginList = searchBegion(sheet);

	    // определяем конечную позицию
	    int endList = searchEnd(sheet);

	    // начинаем поиск
	    for (int i = beginList; i < endList; i++) {

		// название района\посёлка
		cell = sheet.getCell("B" + Integer.toString(i));
		if (cell.isHidden() != true) {
		    // Определяем строку документов: если пустая строка, то
		    // нор. документ не изменился ...
		    // ... относительно прошлого поставщика(строки)

		    cell = sheet.getCell("B" + Integer.toString(i));
		    if (cell.getContents().equals("") == false) {
			// проверяем на район
			cell = sheet.getCell("B" + Integer.toString(i));
			if (cell.getContents().indexOf("Муницип") != -1) {
			    id_district = sheet.getCell(
				    "C" + Integer.toString(i)).getContents();
			    // id_city = 0; // обнуляем счетчик айди городов
			} else {

			    // увелич порядковый номер на 1

			    id_city = sheet.getCell("C" + Integer.toString(i))
				    .getContents();
			    if (id_city.length() < 2)
				id_city = id_district + "00" + id_city;
			    else
				id_city = id_district + "0" + id_city;

			    pst = conn
				    .prepareStatement("INSERT INTO client VALUES (?, ?, ?, ?, ?);");

			    pst.setInt(2, Integer.parseInt(id_district));

			    pst.setInt(3, Integer.parseInt(id_city));

			    pst.setString(4,
				    sheet.getCell("D" + Integer.toString(i))
					    .getContents());

			    pst.setString(5,
				    sheet.getCell("E" + Integer.toString(i))
					    .getContents());

			    pst.addBatch();

			    pst.executeBatch();

			    // // Создаём тег поставщика
			    // provider = doc.createElement("provider");
			    //
			    // provider.setAttribute("name", sheet.getCell("D" +
			    // Integer.toString(i)).getContents());
			    // provider.setAttribute("address",
			    // sheet.getCell("E" +
			    // Integer.toString(i)).getContents());
			    //
			    // city.appendChild(provider);
			}
		    } else {
			// // Создаём тег поставщика
			// provider = doc.createElement("provider");
			//
			// provider.setAttribute("name", sheet.getCell("D" +
			// Integer.toString(i)).getContents());
			// provider.setAttribute("address", sheet.getCell("E" +
			// Integer.toString(i)).getContents());
			//
			// city.appendChild(provider);

			pst = conn
				.prepareStatement("INSERT INTO client VALUES (?, ?, ?, ?, ?);");

			pst.setInt(2, Integer.parseInt(id_district));

			pst.setInt(3, Integer.parseInt(id_city));

			pst.setString(4,
				sheet.getCell("D" + Integer.toString(i))
					.getContents());

			pst.setString(5,
				sheet.getCell("E" + Integer.toString(i))
					.getContents());

			pst.addBatch();

			pst.executeBatch();
		    }
		}
	    }

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (BiffException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
    }

    private void pro() throws SQLException {
	File file = new File(
		"/home/kirill/Work/Карта Нефть/Карты Excel/8. карта по бензину 11.03.2013 (1).xls");
	try {
	    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	    Date date = formatter.parse("11.03.2013");

	    WorkbookSettings ws = new WorkbookSettings();
	    ws.setLocale(new Locale("ru", "RU"));
	    Workbook workbook = Workbook.getWorkbook(file);

	    Sheet sheet = workbook.getSheet(2);

	    String id_district = ""; // счетчик айди района
	    String id_city = ""; // счетчик айди города по районам

	    // определяем стартовую позицию
	    int beginList = searchBegion(sheet);

	    // определяем конечную позицию
	    int endList = searchEnd(sheet);

	    // начинаем поиск
	    Next: for (int i = beginList; i < endList; i++) {
		// название района\посёлка
		if (sheet.getCell("B" + Integer.toString(i)).isHidden() != true) {
		    // Определяем строку документов: если пустая строка, то
		    // нор. документ не изменился ...
		    // ... относительно прошлого поставщика(строки)
		    // проверяем на район

		    if (sheet.getCell("B" + Integer.toString(i)).getContents()
			    .indexOf("Муницип") != -1) {
			id_district = sheet.getCell("C" + Integer.toString(i))
				.getContents();
		    } else {

			id_city = sheet.getCell("C" + Integer.toString(i))
				.getContents();
			if (id_city.length() < 2)
			    id_city = id_district + "00" + id_city;
			else
			    id_city = id_district + "0" + id_city;

			rs = stat
				.executeQuery("SELECT id FROM station WHERE id LIKE '"
					+ i + "';");

			if (rs.next()) {
			    // обновление change

			    pst = conn
				    .prepareStatement("INSERT INTO change VALUES (?, ?, ?, ?, ?, ?, ?);");

			    pst.setInt(2, i);

			    pst.setString(3, Long.toString(date.getTime()));

			    pst.setString(4,
				    sheet.getCell("H" + Integer.toString(i))
					    .getContents());
			    pst.setString(5,
				    sheet.getCell("I" + Integer.toString(i))
					    .getContents());
			    pst.setString(6,
				    sheet.getCell("J" + Integer.toString(i))
					    .getContents());
			    pst.setString(7,
				    sheet.getCell("K" + Integer.toString(i))
					    .getContents());

			    pst.addBatch();

			    pst.executeBatch();
			} else {
			    // запись

			    pst = conn
				    .prepareStatement("INSERT INTO station VALUES (?, ?, ?, ?, ?, ?, ?, ?);");

			    pst.setInt(1, i);

			    pst.setInt(2, Integer.parseInt(id_district));

			    pst.setInt(3, Integer.parseInt(id_city));

			    rs = stat
				    .executeQuery("SELECT id FROM commercial WHERE title LIKE '"
					    + sheet.getCell(
						    "G" + Integer.toString(i))
						    .getContents() + "';");

			    if (rs.next()) {
				pst.setInt(4, rs.getInt(1));
			    } else {
				pst.setInt(4, 4);
				System.out.println("Ошибка: " + i);
			    }

			    pst.setString(5, "true");

			    pst.setString(6,
				    sheet.getCell("D" + Integer.toString(i))
					    .getContents());
			    pst.setString(7,
				    sheet.getCell("E" + Integer.toString(i))
					    .getContents());
			    pst.setString(8,
				    sheet.getCell("F" + Integer.toString(i))
					    .getContents());

			    pst.addBatch();

			    pst.executeBatch();

			    pst = conn
				    .prepareStatement("INSERT INTO change VALUES (?, ?, ?, ?, ?, ?, ?);");

			    pst.setInt(2, i);

			    pst.setString(3, Long.toString(date.getTime()));

			    pst.setString(4,
				    sheet.getCell("H" + Integer.toString(i))
					    .getContents());
			    pst.setString(5,
				    sheet.getCell("I" + Integer.toString(i))
					    .getContents());
			    pst.setString(6,
				    sheet.getCell("J" + Integer.toString(i))
					    .getContents());
			    pst.setString(7,
				    sheet.getCell("K" + Integer.toString(i))
					    .getContents());

			    pst.addBatch();

			    pst.executeBatch();
			}
		    }
		}
	    }
	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (BiffException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	} catch (ParseException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }

    private static int searchBegion(Sheet _sheet) {
	// определяем стартовую позицию
	for (int i = 1; i <= _sheet.getRows(); i++) {
	    Cell cell = _sheet.getCell("B" + Integer.toString(i));
	    if (cell.getContents().equals(
		    "Муниципальный район \"Бабынинский район\"")) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Определяет строку с конечной записью<br>
     * Запись: "$EndList"
     * 
     * @param _sheet
     *            - Лист документа
     * @return индекс строки
     */
    private static int searchEnd(Sheet _sheet) {
	for (int i = 1; i <= _sheet.getRows(); i++) {
	    Cell cell = _sheet.getCell("B" + Integer.toString(i));
	    if (cell.getContents().equals("$EndList")) {
		return i;
	    }
	}
	return -1;
    }

    public void getData() throws SQLException {

	rs = stat.executeQuery("select * from district;");

	while (rs.next()) {
	    System.out.println(rs.getString(2));
	}
    }

}
