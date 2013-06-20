/**
 * 
 */
package ru.fiko.oil.data;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.JXLConstant;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

/**
 * Вывод списка дейтвующих АЗС с их ценами на территории Калужской области в
 * Excel файл.
 * 
 * @author Demyanov Kirill
 * 
 */
public class OutputStations {

    /**
     * Шрифты для ячеек
     */
    private JXLConstant font = new JXLConstant();

    /**
     * Наименование заголовков таблицы
     */
    private String[] title = {
	    "Наименование",
	    "Адрес месторасположения",
	    "АИ - 80",
	    "АИ - 92",
	    "АИ - 95",
	    "ДТ" };

    /**
     * Вывод в Excel файл списка всех АЗС с ценами на time дату
     * 
     * @param time
     *            - дата, на которую формируется свод
     * @throws IOException
     * @throws WriteException
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws ParseException
     */
    public OutputStations(Date time) throws IOException, WriteException,
	    SQLException, ClassNotFoundException, ParseException {

	Class.forName("org.sqlite.JDBC");
	Connection conn = DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH);

	/**
	 * Округление даты к концу дня<br>
	 * т.е. к 23:59:59
	 */
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	Date date = formatter.parse(formatter.format(time));
	time = new Date(date.getTime() + 86400000 - 1);

	/**
	 * Создание документа:<br>
	 * - в каталоге с программой, наименование файла
	 * "АЗС Калужской области за dd.MM.yyyy";<br>
	 * - из настроек только указание на язык документа(RU);<br>
	 * - один лист "АЗС" с ценами на топливо.<br>
	 */

	/**
	 * Индекс строки.<br>
	 * С течением получения данных переменная увеличивается на 1.
	 */
	int row = 1;

	/**
	 * Индекс столбцов.<br>
	 * В таблице имеется 6 колонок:<br>
	 * 0 - "Наименование";<br>
	 * 1 - "Адрес месторасположения";<br>
	 * 2 - "АИ - 80";<br>
	 * 3 - "АИ - 92";<br>
	 * 4 - "АИ - 95";<br>
	 * 5 - "ДТ".
	 */
	final int column = 1;

	WorkbookSettings ws = new WorkbookSettings();
	ws.setLocale(new Locale("ru", "RU"));

	WritableWorkbook workbook = Workbook.createWorkbook(new File(
		"АЗС Калужской области за " + dateToString(time.getTime())
			+ ".xls"), ws);

	WritableSheet sheet = workbook.createSheet("АЗС", 0);

	/**
	 * Запись наименования заголовок колонок таблицы
	 */
	for (int i = 0; i < title.length; i++)
	    sheet.addCell(new Label(column + i, row, title[i], font.tahomaTitle));

	sheet.setRowView(row++, 300);

	/**
	 * Изменение ширины столбцов достаточно только у "наименование"(0
	 * индекс) и "адреса месторасположения"(1 индекс)
	 */
	sheet.setColumnView(column, 45);
	sheet.setColumnView(column + 1, 60);

	/**
	 * Заполнение листа данными
	 */

	/**
	 * Список районов.<br>
	 * Integer - id района;<br>
	 * String - наименование района.
	 */
	Map<Integer, String> districts = new HashMap<Integer, String>();

	/**
	 * Получение списка всех районов.<br>
	 * id - id района;<br>
	 * title - наименование района.
	 */
	Statement stat_districts = conn.createStatement();
	ResultSet rs_districts = stat_districts
		.executeQuery("SELECT id, title FROM district;");

	while (rs_districts.next()) {
	    districts.put(rs_districts.getInt("id"),
		    rs_districts.getString("title"));
	}
	rs_districts.close();
	stat_districts.close();

	/**
	 * Обход районов
	 */

	Set<Entry<Integer, String>> s = districts.entrySet();
	Iterator<Entry<Integer, String>> district = s.iterator();

	while (district.hasNext()) {
	    /**
	     * Запись района в документ
	     */
	    Entry<Integer, String> m = district.next();
	    // id района
	    int district_id = (Integer) m.getKey();
	    // Наименование района
	    String district_title = (String) m.getValue();

	    sheet.addCell(new Label(column, row, district_title,
		    font.tahomaDistrict));
	    sheet.mergeCells(column, row, column + 5, row);
	    sheet.setRowView(row++, 300);

	    /**
	     * Поиск АЗС в районе и запись информации найденной АЗС в документ.
	     */
	    Statement stat_stations = conn.createStatement();
	    ResultSet rs_stations = stat_stations
		    .executeQuery("SELECT id, title, address, active FROM station WHERE district_id LIKE '"
			    + district_id + "';");

	    /**
	     * АЗС так же должна быть действующей, т.к. есть дополнительные
	     * станция, которые отражают розницу по Калуги или области(их не
	     * надо включать в свод)
	     */
	    while (rs_stations.next()
		    && rs_stations.getString("active").equals("true")) {

		sheet.addCell(new Label(column, row, rs_stations
			.getString("title"), font.tahomaStation));

		sheet.addCell(new Label(column + 1, row, rs_stations
			.getString("address"), font.tahomaStation));

		int station_id = rs_stations.getInt("id");

		/**
		 * Поиск максимально приближенной записи изменение цены к
		 * необходимой дате time
		 */

		Statement stat_change = conn.createStatement();
		ResultSet rs_change = stat_change
			.executeQuery("SELECT * FROM change WHERE station_id LIKE '"
				+ station_id + "';");

		/**
		 * макс значение
		 */
		Long max_time = (long) 0;
		String b80 = "";
		String b92 = "";
		String b95 = "";
		String bdis = "";

		while (rs_change.next()) {
		    Long temp = Long.parseLong(rs_change
			    .getString("changedate"));

		    /**
		     * <= - т.к. time установлен на последнюю секунды
		     * дня(23:59:59)
		     */
		    if (temp > max_time && temp <= time.getTime()) {
			max_time = temp;
			b80 = rs_change.getString("b80");
			b92 = rs_change.getString("b92");
			b95 = rs_change.getString("b95");
			bdis = rs_change.getString("bdis");
		    }
		}
		rs_change.close();
		stat_change.close();

		/**
		 * Запись в документ данных максимально приближенных к дате time
		 */

		sheet.addCell(new Label(column + 2, row, b80, font.tahomaValue));
		sheet.addCell(new Label(column + 3, row, b92, font.tahomaValue));
		sheet.addCell(new Label(column + 4, row, b95, font.tahomaValue));
		sheet.addCell(new Label(column + 5, row, bdis, font.tahomaValue));

		sheet.setRowView(row++, 450);
	    }
	    rs_stations.close();
	    stat_stations.close();

	}

	conn.close();
	workbook.write();
	workbook.close();
    }

    private String dateToString(Long time) {
	DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	return dateFormat.format(new Date(time));
    }
}
