/**
 * 
 */
package ru.fiko.oil.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Vector;

import javax.swing.JOptionPane;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.TestSer;
import ru.fiko.oil.supp.TestSer.FileParam;
import ru.fiko.oil.supp.TestSer.Param;

/**
 * @author kirill
 * 
 */
public class OutputData {

    /**
     * Путь к xml файлам
     */
    private String pathFolder = "";

    /**
     * Результат для вывода в xml по АИ-95
     */
    private Vector<double[]> res_b95;

    /**
     * Результат для вывода в xml по АИ-92
     */
    private Vector<double[]> res_b92;

    /**
     * Результат для вывода в xml по ДТ
     */
    private Vector<double[]> res_bdis;

    public OutputData() throws ClassNotFoundException, FileNotFoundException,
	    ParserConfigurationException, TransformerException, SQLException,
	    TransformerFactoryConfigurationError, ParseException {

	Class.forName("org.sqlite.JDBC");

	pathFolder = "карта/xml";
	new File(pathFolder).mkdirs();
	pathFolder += "/";

	regions();
	providers();
	_main();
	clients();
	grafics();

	String url = "http://fondim.kaluga.net/map_oil/upload.php";
	// String url = "http://localhost/map_oil/upload.php";
	String[] filename = new String[5];
	filename[0] = "main.xml";
	filename[1] = "providers.xml";
	filename[2] = "regions.xml";
	filename[3] = "clients.xml";
	filename[4] = "grafics.xml";

	for (int i = 0; i < filename.length; i++) {
	    ArrayList<TestSer.Param> params = new ArrayList<TestSer.Param>();
	    params.add(new Param("pass", "876954361"));

	    ArrayList<TestSer.FileParam> fileParams = new ArrayList<TestSer.FileParam>();
	    fileParams.add(new FileParam("filename", filename[i], new File(
		    pathFolder + filename[i]), ""));

	    TestSer aa = new TestSer(url, params, fileParams);
	    aa.send();
	}

	JOptionPane.showMessageDialog(null, "Готово");
    }

    private void grafics() throws ParserConfigurationException,
	    TransformerFactoryConfigurationError, FileNotFoundException,
	    TransformerException, SQLException, ParseException {

	/**
	 * Задание временных рамок(12 недель)<br>
	 * дата округляется к концу текущего дня.<br>
	 * 86400000 - один день
	 */
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	Date date = formatter.parse(formatter.format(new Date(System
		.currentTimeMillis())));
	date = new Date(date.getTime() + 86400000);

	/**
	 * расчет дат за квартал<br>
	 * 604800000 - одна неделя
	 */
	long[] kvartal = new long[12];
	for (int i = 0; i < kvartal.length; i++) {
	    kvartal[i] = date.getTime();
	    date = new Date(date.getTime() - 604800000);
	}

	/**
	 * Формирование файла grafics.xml
	 */
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();

	Document doc = builder.newDocument();

	doc.setXmlVersion("1.0");
	doc.setXmlStandalone(true);

	Element grafics = doc.createElement("grafics");
	grafics.setAttribute("name", "Калужская область");

	/**
	 * Получение данных
	 */

	/**
	 * Результат для вывода в xml по каждому виду топлива
	 */
	res_b95 = new Vector<double[]>();
	res_b92 = new Vector<double[]>();
	res_bdis = new Vector<double[]>();

	/**
	 * Используются следующие сетевые организцаии:<br>
	 * 1 - Калуганефтепродукт<br>
	 * 2 - Лукойл<br>
	 * 3 - Газпром
	 */
	int[] commercial = { 1, 3, 2 };

	/**
	 * Title линий графика в легенде
	 */
	String[] comm_name = new String[commercial.length + 1];

	/*
	 * Получение названий сетевых поставщиков из БД
	 */
	for (int comm = 0; comm < commercial.length; comm++) {
	    ResultSet title = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT title FROM commercial WHERE id LIKE '"
				    + commercial[comm] + "';");
	    while (title.next()) {
		comm_name[comm] = title.getString("title");
	    }
	    title.close();
	}

	/**
	 * Послденяя запись - это "средняя цена"
	 */
	comm_name[comm_name.length - 1] = "Средняя цена";

	/**
	 * формирование значений цен результата для вывода
	 */
	for (int comm = 0; comm < commercial.length; comm++) {
//	    int count_det = 0;

	    /**
	     * хранит id всех АЗС одного сетевого поставщика
	     */
	    Vector<Integer> stationsComm = new Vector<Integer>();

	    /**
	     * Получение id всех станций АЗС одного сетевого поставщика
	     */
	    ResultSet bdStations = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT id,active FROM station WHERE comm_id LIKE '"
				    + commercial[comm] + "';");

	    while (bdStations.next()) {
		if (bdStations.getString("active").equals("true"))
		    stationsComm.add(bdStations.getInt("id"));
	    }
	    bdStations.close();

	    /**
	     * Цены на каждой АЗС за весь период(квартал) - ОДНОГО ПОСТАВЩИКА
	     */
	    Vector<String[]> all_b95 = new Vector<String[]>();
	    Vector<String[]> all_b92 = new Vector<String[]>();
	    Vector<String[]> all_bdis = new Vector<String[]>();

	    /**
	     * сбор данных за последний квартал(12 недель)
	     */
	    for (int st = 0; st < stationsComm.size(); st++) {

		/**
		 * Считывание данных из базы
		 */
		ResultSet stChange = DriverManager
			.getConnection("jdbc:sqlite:" + Oil.PATH)
			.createStatement()
			.executeQuery(
				"SELECT changedate, b95, b92, bdis FROM change WHERE station_id LIKE '"
					+ stationsComm.get(st) + "';");

		Vector<String[]> time = new Vector<String[]>();
		while (stChange.next()) {
		    String[] change = new String[4];

		    change[0] = stChange.getString("changedate");
		    change[1] = stChange.getString("b95");
		    change[2] = stChange.getString("b92");
		    change[3] = stChange.getString("bdis");

		    time.add(0, change);
		}
		stChange.close();

		/**
		 * проверка на наличие изменений в АЗС (в нефтепродуктах была
		 * АЗС без цен)
		 */
		if (time.size() > 0) {
		    /**
		     * данные одной АЗС за квартал
		     */
		    String[] data_b95 = new String[kvartal.length];
		    String[] data_b92 = new String[kvartal.length];
		    String[] data_bdis = new String[kvartal.length];

		    boolean blya = false;

		    /**
		     * Обработка данных
		     */
		    for (int tau = 0; tau < kvartal.length && time.size() > 0; tau++) {
			/**
			 * поиск записи, наиболее приблежённой к времени тау
			 */
			String[] current_data = { "0", "0", "0", "0" };

			for (int p = 0; p < time.size(); p++) {
			    if (Long.parseLong(time.get(p)[0]) < kvartal[tau]
				    && Long.parseLong(current_data[0]) < Long
					    .parseLong(time.get(p)[0])) {
				current_data = time.get(p);
			    }
			}

			if (current_data[0].equals("0")) {
			    blya = true;
			}

			/**
			 * сохранение найденного значения
			 */
			data_b95[tau] = current_data[1];
			data_b92[tau] = current_data[2];
			data_bdis[tau] = current_data[3];
		    }

		    // /**
		    // * сохранение найденных значений АЗС за квартал
		    // */
		    // all_b95.add(data_b95);
		    // all_b92.add(data_b92);
		    // all_bdis.add(data_bdis);

		    if (blya) {
			// count_det++;
			System.out.println("blya " + stationsComm.get(st));
		    } else {
			/**
			 * сохранение найденных значений АЗС за квартал
			 */
			all_b95.add(data_b95);
			all_b92.add(data_b92);
			all_bdis.add(data_bdis);
		    }
		}
	    }

	    System.out.println("Размер 92 " + all_b92.size());
	    /**
	     * Поиск среднего значения для сетевого поставщика
	     */

	    /**
	     * Среднее значение поставщика по каждому виду топлива
	     */
	    double[] b95 = new double[kvartal.length];
	    double[] b92 = new double[kvartal.length];
	    double[] bdis = new double[kvartal.length];

	    /**
	     * обнуление значений, для вычисление средних значений
	     */
	    for (int i = 0; i < kvartal.length; i++) {
		b95[i] = 0f;
		b92[i] = 0f;
		bdis[i] = 0f;
	    }

	    /**
	     * суммирование значений всех АЗС одного поставщика за квартал(12
	     * недель)
	     */
	    for (int station = 0; station < all_b95.size(); station++) {
		String[] temp_95 = all_b95.get(station);
		String[] temp_92 = all_b92.get(station);
		String[] temp_dis = all_bdis.get(station);

		for (int i = 0; i < kvartal.length; i++) {
		    b95[i] = new BigDecimal(b95[i]
			    + parseStringToDouble(temp_95[i])).setScale(2,
			    RoundingMode.HALF_UP).doubleValue();
		    // if (comm == 0 && i == 0)
		    // System.out.println(b95[i]);
		    b92[i] = new BigDecimal(b92[i]
			    + parseStringToDouble(temp_92[i])).setScale(2,
			    RoundingMode.HALF_UP).doubleValue();

		    bdis[i] = new BigDecimal(bdis[i]
			    + parseStringToDouble(temp_dis[i])).setScale(2,
			    RoundingMode.HALF_UP).doubleValue();
		}
	    }

	    /**
	     * приведение к среднему значению(деление суммы на количество
	     * цен(АЗС))
	     */
	    for (int i = 0; i < kvartal.length; i++) {
		b95[i] = new BigDecimal(b95[i] / (all_b95.size())).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
		b92[i] = new BigDecimal(b92[i] / (all_b95.size())).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
		bdis[i] = new BigDecimal(bdis[i] / (all_b95.size())).setScale(
			2, RoundingMode.HALF_UP).doubleValue();
	    }

	    /**
	     * сохранение результата
	     */
	    res_b95.add(b95);
	    res_b92.add(b92);
	    res_bdis.add(bdis);
	}

	/**
	 * Вычисление средних значений для каждого вида топлива
	 */

	/**
	 * Среднее значение цен топлива за квартал(12 недель)
	 */
	double[] ave_95 = new double[kvartal.length];
	double[] ave_92 = new double[kvartal.length];
	double[] ave_dis = new double[kvartal.length];

	/**
	 * обнуление значений, для вычисление средних значений
	 */
	for (int i = 0; i < kvartal.length; i++) {
	    ave_95[i] = 0f;
	    ave_92[i] = 0f;
	    ave_dis[i] = 0f;
	}

	/**
	 * Суммирование значений сетевых поставщиков
	 */
	for (int res = 0; res < res_b95.size(); res++) {
	    for (int i = 0; i < kvartal.length; i++) {
		ave_95[i] = new BigDecimal(ave_95[i] + res_b95.get(res)[i])
			.setScale(2, RoundingMode.HALF_UP).doubleValue();
		ave_92[i] = new BigDecimal(ave_92[i] + res_b92.get(res)[i])
			.setScale(2, RoundingMode.HALF_UP).doubleValue();
		ave_dis[i] = new BigDecimal(ave_dis[i] + res_bdis.get(res)[i])
			.setScale(2, RoundingMode.HALF_UP).doubleValue();
	    }
	}

	/**
	 * Приведение к среднему значению(делением суммы на количество
	 * поставщиков)
	 */
	for (int i = 0; i < kvartal.length; i++) {
	    ave_95[i] = new BigDecimal(ave_95[i] / res_b95.size()).setScale(2,
		    RoundingMode.HALF_UP).doubleValue();
	    ave_92[i] = new BigDecimal(ave_92[i] / res_b92.size()).setScale(2,
		    RoundingMode.HALF_UP).doubleValue();
	    ave_dis[i] = new BigDecimal(ave_dis[i] / res_bdis.size()).setScale(
		    2, RoundingMode.HALF_UP).doubleValue();
	}

	/**
	 * Сохранение среднего значения каждого вида топлива
	 */
	res_b95.add(ave_95);
	res_b92.add(ave_92);
	res_bdis.add(ave_dis);

	/**
	 * Формирование элемента Диз Топливо(bdis)
	 */

	/**
	 * наименование элементов
	 */
	String[] toplivo_name = { "bdis", "b95", "b92" };
	/**
	 * для простоты
	 */
	Vector<Vector<double[]>> toplivo_temp = new Vector<Vector<double[]>>();
	toplivo_temp.add(res_bdis);
	toplivo_temp.add(res_b95);
	toplivo_temp.add(res_b92);

	/**
	 * формирование элементов xml
	 */
	for (int index = 0; index < toplivo_name.length; index++) {
	    /**
	     * данные текущего топлива
	     */
	    Vector<double[]> toplivo_val = toplivo_temp.get(index);

	    /**
	     * элемент топлива
	     */
	    Element el_b = doc.createElement(toplivo_name[index]);

	    /**
	     * элемент настроек графика
	     */
	    Element setting = doc.createElement("setting");

	    /**
	     * макс и мин значение графика
	     */
	    Element x = doc.createElement("x");

	    int[] max_min = getMaxMin(toplivo_val);

	    Element max = doc.createElement("max");
	    max.setTextContent(Integer.toString(max_min[0]));
	    x.appendChild(max);

	    Element min = doc.createElement("min");
	    min.setTextContent(Integer.toString(max_min[1]));
	    x.appendChild(min);

	    setting.appendChild(x);

	    /**
	     * даты точек
	     */
	    Element y = doc.createElement("y");

	    for (int val = kvartal.length - 1; val >= 0; val--) {
		DateFormat dateFormat = new SimpleDateFormat("dd.MM");
		Element value = doc.createElement("value");
		value.setTextContent(dateFormat.format(new Date(
			kvartal[val] - 1)));
		y.appendChild(value);
	    }

	    setting.appendChild(y);

	    el_b.appendChild(setting);

	    /**
	     * заполнение элементов с ценой
	     */

	    for (int provider = 0; provider < toplivo_val.size(); provider++) {
		Element pro = doc.createElement("pro");
		pro.setAttribute("legend", comm_name[provider]);

		double[] val = toplivo_val.get(provider);

		for (int i = val.length - 1; i >= 0; i--) {
		    Element value = doc.createElement("value");
		    value.setTextContent(Double.toString(val[i]));
		    pro.appendChild(value);
		}

		el_b.appendChild(pro);
	    }

	    grafics.appendChild(el_b);

	}

	doc.appendChild(grafics);

	/**
	 * сохранение всего в xml
	 */
	Transformer transformer = TransformerFactory.newInstance()
		.newTransformer();
	Result res = new StreamResult(new FileOutputStream(pathFolder
		+ "grafics.xml"));
	transformer.transform(new DOMSource(doc), res);
	res = null;
    }

    /**
     * Получение максимального и минимального значения цены топлива res_b для
     * графика<br>
     * Макс - округляется до целого числа<br>
     * Мин - отбрасывает дробную часть
     * 
     * @param res_b
     *            - цены за период по одному виду топлива
     * @return макс[0] и мин[1] значения графика
     */
    private int[] getMaxMin(Vector<double[]> res_b) {
	double[] result = { 0.0, 999.0 };

	for (int res = 0; res < res_b.size(); res++) {
	    for (int i = 0; i < res_b.get(res).length; i++) {
		if (result[0] < res_b.get(res)[i]) {
		    result[0] = res_b.get(res)[i];
		}

		if (result[1] > res_b.get(res)[i]) {
		    result[1] = res_b.get(res)[i];
		}
	    }
	}

	int[] result_int = { 0, 999 };
	result_int[0] = (int) (result[0] + 1);
	result_int[1] = (int) result[1];

	return result_int;
    }

    private void regions() throws ParserConfigurationException,
	    FileNotFoundException, TransformerException, SQLException {
	// Создаём хмл файл
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();

	Document doc = builder.newDocument();

	doc.setXmlVersion("1.0");
	doc.setXmlStandalone(true);

	// Создаём тег услуги, сод. районы
	Element region = doc.createElement("region");
	region.setAttribute("name", "Калужская область");

	// Выборка Региона
	ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement().executeQuery("SELECT * FROM district;");

	while (rs.next()) {
	    // район
	    Element district = doc.createElement("district");
	    district.setAttribute("id", rs.getString("id"));
	    district.setAttribute("main_id", "0");
	    district.setAttribute("name", rs.getString("title"));
	    district.setAttribute("fio", rs.getString("glava_fio"));
	    district.setAttribute("fio_tel", rs.getString("glava_tel"));
	    district.setAttribute("zam", rs.getString("zam_fio"));
	    district.setAttribute("zam_tel", rs.getString("zam_tel"));

	    int district_id = rs.getInt("id");

	    // города
	    ResultSet rs_city = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT * FROM city WHERE district_id LIKE '"
				    + district_id + "';");

	    while (rs_city.next()) {
		Element city = doc.createElement("city");
		city.setAttribute("id", rs_city.getString("id"));
		city.setAttribute("name", rs_city.getString("title"));
		city.setAttribute("x", rs_city.getString("x"));
		city.setAttribute("y", rs_city.getString("y"));
		district.appendChild(city);
	    }
	    rs_city.close();

	    region.appendChild(district);
	}
	rs.close();

	doc.appendChild(region);

	Transformer transformer = TransformerFactory.newInstance()
		.newTransformer();
	Result res = new StreamResult(new FileOutputStream(pathFolder
		+ "regions.xml"));
	transformer.transform(new DOMSource(doc), res);
	res = null;
    }

    private void _main() throws ParserConfigurationException,
	    FileNotFoundException, TransformerException, SQLException {
	// Создаём хмл файл
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();

	Document doc = builder.newDocument();

	doc.setXmlVersion("1.0");
	doc.setXmlStandalone(true);

	Element main = doc.createElement("main");
	main.setAttribute("name", "Интерактивная карта калужской области");

	ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement().executeQuery("SELECT * FROM main;");

	if (rs.next()) {
	    Element url_btn = doc.createElement("url_btn");
	    url_btn.setAttribute("texturl", rs.getString("texturl"));
	    url_btn.setAttribute("url", rs.getString("url"));
	    main.appendChild(url_btn);

	    Element url_refresh = doc.createElement("url_refresh");
	    url_refresh.setAttribute("text", rs.getString("text"));
	    main.appendChild(url_refresh);

	    Element text_all = doc.createElement("text_all");

	    Element basic = doc.createElement("basic");

	    Element nameCol = doc.createElement("nameCol");
	    nameCol.setTextContent(rs.getString("bname"));
	    basic.appendChild(nameCol);

	    Element address = doc.createElement("address");
	    address.setTextContent(rs.getString("baddress"));
	    basic.appendChild(address);

	    Element b80 = doc.createElement("b80");
	    b80.setTextContent(rs.getString("b80"));
	    basic.appendChild(b80);

	    Element b92 = doc.createElement("b92");
	    b92.setTextContent(rs.getString("b92"));
	    basic.appendChild(b92);

	    Element b95 = doc.createElement("b95");
	    b95.setTextContent(rs.getString("b95"));
	    basic.appendChild(b95);

	    Element bdis = doc.createElement("bdis");
	    bdis.setTextContent(rs.getString("bdis"));
	    basic.appendChild(bdis);

	    Element binfo = doc.createElement("info");
	    binfo.setTextContent(rs.getString("binfo"));
	    basic.appendChild(binfo);

	    text_all.appendChild(basic);

	    Element client = doc.createElement("client");

	    Element nameCol2 = doc.createElement("nameCol");
	    nameCol2.setTextContent(rs.getString("orgname"));
	    client.appendChild(nameCol2);

	    Element address2 = doc.createElement("address");
	    address2.setTextContent(rs.getString("orgaddress"));
	    client.appendChild(address2);

	    text_all.appendChild(client);
	    main.appendChild(text_all);

	    Element anno_htmlText = doc.createElement("anno_htmlText");
	    anno_htmlText.setAttribute("textanno", "[ Справочная информация ]");
	    main.appendChild(anno_htmlText);
	}
	rs.close();

	doc.appendChild(main);

	Transformer transformer = TransformerFactory.newInstance()
		.newTransformer();
	Result res = new StreamResult(new FileOutputStream(pathFolder
		+ "main.xml"));
	transformer.transform(new DOMSource(doc), res);
	res = null;
    }

    private void providers() throws ParserConfigurationException,
	    FileNotFoundException, TransformerException, SQLException {
	// Создаём хмл файл
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();

	Document doc = builder.newDocument();

	doc.setXmlVersion("1.0");
	doc.setXmlStandalone(true);

	Element service = doc.createElement("service");
	service.setAttribute("name", "Поставщики");

	// Выборка Региона
	ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement().executeQuery("SELECT id FROM district;");

	while (rs.next()) {
	    // район
	    Element district = doc.createElement("district");
	    district.setAttribute("id", rs.getString("id"));

	    int district_id = rs.getInt("id");

	    // выборка городов
	    ResultSet rs_city = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT id FROM city WHERE district_id LIKE '"
				    + district_id + "';");
	    while (rs_city.next()) {
		int city_id = rs_city.getInt("id");

		// выборка азс города
		ResultSet rs_provider = DriverManager
			.getConnection("jdbc:sqlite:" + Oil.PATH)
			.createStatement()
			.executeQuery(
				"SELECT * FROM station WHERE district_id LIKE '"
					+ district_id + "' AND city_id LIKE '"
					+ city_id + "';");
		while (rs_provider.next()) {
		    // выбираем только действующие АЗС(с пометкой актив == true)
		    if (rs_provider.getString("active").equals("true")) {
			int station_id = rs_provider.getInt("id");

			Long max_time = (long) 0;
			String b80 = "";
			String b92 = "";
			String b95 = "";
			String bdis = "";

			// поиск последнего изменения
			ResultSet time = DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeQuery(
					"SELECT changedate,b80,b92,b95,bdis FROM change WHERE station_id LIKE '"
						+ station_id + "';");
			while (time.next()) {
			    Long temp = Long.parseLong(time.getString(1));

			    if (temp > max_time) {
				max_time = temp;
				b80 = time.getString(2);
				b92 = time.getString(3);
				b95 = time.getString(4);
				bdis = time.getString(5);
			    }
			}
			time.close();

			// определение имени сетевого поставщика
			int comm_id = rs_provider.getInt("comm_id");
			String comm_str = "";

			ResultSet comm = DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeQuery(
					"SELECT title FROM commercial WHERE id LIKE '"
						+ comm_id + "';");
			if (comm.next())
			    comm_str = comm.getString("title");
			comm.close();

			// создание поставщика
			Element provider = doc.createElement("provider");
			provider.setAttribute("name",
				rs_provider.getString("title"));
			provider.setAttribute("address",
				rs_provider.getString("address"));
			provider.setAttribute("web", comm_str);
			provider.setAttribute("b80", b80);
			provider.setAttribute("b92", b92);
			provider.setAttribute("b95", b95);
			provider.setAttribute("bdis", bdis);

			// определяем, куда впихнуть поставщика:
			boolean isCity = false;
			// если создан города, включаем поставщика в него
			for (int p = 0; p < district.getChildNodes()
				.getLength(); p++) {
			    if (district.getChildNodes().item(p)
				    .getAttributes().getNamedItem("id")
				    .getNodeValue()
				    .equals(Integer.toString(city_id))) {
				district.getChildNodes().item(p)
					.appendChild(provider);
				isCity = true;
			    }
			}
			// если город не был добавлен:
			// создаётся новый элемент и поставщик вносится в него
			if (!isCity) {
			    Element city = doc.createElement("city");
			    city.setAttribute("id", rs_city.getString("id"));
			    city.appendChild(provider);
			    district.appendChild(city);
			}
		    }
		}
		rs_provider.close();
	    }
	    rs_city.close();

	    service.appendChild(district);
	}
	rs.close();

	doc.appendChild(service);

	Transformer transformer = TransformerFactory.newInstance()
		.newTransformer();
	Result res = new StreamResult(new FileOutputStream(pathFolder
		+ "providers.xml"));
	transformer.transform(new DOMSource(doc), res);
	res = null;
    }

    private void clients() throws ParserConfigurationException,
	    FileNotFoundException, TransformerException, SQLException {
	System.out.println(1);
	// Создаём хмл файл
	DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	DocumentBuilder builder = factory.newDocumentBuilder();

	Document doc = builder.newDocument();

	doc.setXmlVersion("1.0");
	doc.setXmlStandalone(true);

	Element service = doc.createElement("service");
	service.setAttribute("name", "Заказчики");

	// Выборка Региона
	ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement().executeQuery("SELECT id FROM district;");

	while (rs.next()) {
	    // район
	    Element district = doc.createElement("district");
	    district.setAttribute("id", rs.getString("id"));

	    int district_id = rs.getInt("id");

	    // выборка городов
	    ResultSet rs_city = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT id FROM city WHERE district_id LIKE '"
				    + district_id + "';");
	    while (rs_city.next()) {
		int city_id = rs_city.getInt("id");

		// выборка азс города
		ResultSet rs_client = DriverManager
			.getConnection("jdbc:sqlite:" + Oil.PATH)
			.createStatement()
			.executeQuery(
				"SELECT * FROM client WHERE district_id LIKE '"
					+ district_id + "' AND city_id LIKE '"
					+ city_id + "';");
		while (rs_client.next()) {
		    Element provider = doc.createElement("provider");
		    provider.setAttribute("name", rs_client.getString("title"));
		    provider.setAttribute("address",
			    rs_client.getString("address"));

		    // определяем, куда впихнуть поставщика:
		    boolean isCity = false;
		    // если создан города, включаем поставщика в него
		    for (int p = 0; p < district.getChildNodes().getLength(); p++) {
			if (district.getChildNodes().item(p).getAttributes()
				.getNamedItem("id").getNodeValue()
				.equals(Integer.toString(city_id))) {
			    district.getChildNodes().item(p)
				    .appendChild(provider);
			    isCity = true;
			}
		    }
		    // если город не был добавлен:
		    // создаётся новый элемент и поставщик вносится в него
		    if (!isCity) {
			Element city = doc.createElement("city");
			city.setAttribute("id", rs_city.getString("id"));
			city.appendChild(provider);
			district.appendChild(city);
		    }
		}
		rs_client.close();
	    }
	    rs_city.close();

	    service.appendChild(district);
	}
	rs.close();

	doc.appendChild(service);

	Transformer transformer = TransformerFactory.newInstance()
		.newTransformer();
	Result res = new StreamResult(new FileOutputStream(pathFolder
		+ "clients.xml"));
	transformer.transform(new DOMSource(doc), res);
	res = null;
    }

    private Double parseStringToDouble(String value) {
	if (value != null) {
	    value = value.replace(" ", "");
	    value = value.replace(" ", "");
	    value = value.replace(",", ".");

	    try {
		return Double.parseDouble(value);
	    } catch (Exception e) {
		JOptionPane.showMessageDialog(null,
			"Ошибка; Позвонить Кириллу!");
		System.exit(0);
		return 0.0;
	    }
	}

	return 0.0;
    }
}
