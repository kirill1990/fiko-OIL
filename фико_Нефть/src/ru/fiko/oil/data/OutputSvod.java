package ru.fiko.oil.data;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

import javax.swing.JOptionPane;

import ru.fiko.oil.main.Oil;
import ru.fiko.oil.supp.JXLConstant;

import jxl.Workbook;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class OutputSvod {

    /**
     * Последовательность дат формирования отчета.<br>
     * От меньшего к большему. Сб и Вс не учитываются.
     */
    long[] time = new long[6];

    /**
     * Список всех марок топлива
     */
    String[] label_toplivo = {
	    "АИ-80",
	    "АИ-92",
	    "АИ-95",
	    "ДТ/лет",
	    "ДТ/лет",
	    "ДТ/м-сез.",
	    "ДТ/зим" };

    JXLConstant font = new JXLConstant();

    Vector<Integer> ave_k = new Vector<Integer>();
    Vector<Integer> ave_obl_reg = new Vector<Integer>();
    Vector<Integer> ave_obl_all = new Vector<Integer>();

    public OutputSvod() throws IOException, WriteException, ParseException,
	    SQLException {
	/**
	 * Формирование дат отчетности
	 */

	/**
	 * текущие время
	 */
	long current = System.currentTimeMillis();

	// TODO Заглушка времени
//	current = 1366920000000l + 86400000 - 1;

	/**
	 * заполнение начинается с последнего элемента
	 */
	time[time.length - 1] = current;
	for (int i = time.length - 2; i >= 0; i--) {
	    /**
	     * Если i-ая дата не является Сб или Вс записывается предыдущий день
	     * Если i-ая дата Сб или Вс - записывается Пятница
	     */

	    /**
	     * коэффицент смещения дней
	     */
	    int mod = 1;

	    /**
	     * День недели i-ого числа
	     */
	    String day = new SimpleDateFormat("E", Locale.ENGLISH)
		    .format(time[i + 1] - 86400000);

	    /**
	     * Корректировка коэффицента смещения, если i-ый день Сб или Вс
	     */
	    if (day.equals("Sat"))
		mod = 2;
	    else if (day.equals("Sun"))
		mod = 3;

	    /**
	     * Сохраняем нужную дату
	     */
	    time[i] = time[i + 1] - 86400000 * mod;
	}

	/**
	 * Округление времени к 23:59:59
	 */
	for (int i = 0; i < time.length; i++) {
	    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	    Date date = formatter.parse(formatter.format(new Date(time[i])));
	    time[i] = date.getTime() + 86400000 - 1;
	}

	// for (long index : time)
	// System.out.println(new Date(index));
	
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

	WritableWorkbook workbook = Workbook.createWorkbook(new File(
		"svod_data_"+formatter.format(new Date(System.currentTimeMillis()))+".xls"));

	WritableSheet sheet = workbook.createSheet("Мнониторинг цен", 0);
	sheet.addCell(new Label(
		0,
		0,
		"Мониторинг цен на нефтепродукты, реализуемые на автозаправочных станциях Калуги и Калужской области ",
		font.tahoma14ptBold));
	sheet.mergeCells(0, 0, 22, 0);
	sheet.setRowView(0, 700);

	int column = 1;
	int row = 4;

	column = cape(column, row, sheet);

	// калуганефтепродукт
	column = pro1(column, row, sheet);
	// газпром
	column = pro2(column, row, sheet);
	// Альфа-трейд
	column = pro3(column, row, sheet);

	column = cape(column, row, sheet);

	// лукойл
	column = pro4(column, row, sheet);
	// ИП палашичев
	column = pro5(column, row, sheet);
	// тран азс сервис
	column = pro6(column, row, sheet);
	// октан
	column = pro7(column, row, sheet);
	// ПИ пешков
	column = pro8(column, row, sheet);
	// ИП журавлева
	column = pro9(column, row, sheet);
	// березка
	column = pro10(column, row, sheet);
	// Восток ойл
	column = pro11(column, row, sheet);
	// солид
	column = pro12(column, row, sheet);
	// экоресурс
	column = pro13(column, row, sheet);
	// Белова
	column = pro14(column, row, sheet);
	// Хвастовичи
	column = pro15(column, row, sheet);
	ave_result(5, 65, sheet);

	workbook.write();
	workbook.close();
	JOptionPane.showMessageDialog(null, "Готово");
    }

    /**
     * Формирование таблицы "Изменение средней розничной цены"<br>
     * <br>
     * Номера колонок берутся из 3 массивов:<br>
     * - ave_k - по городу Калуга<br>
     * - ave_obl_all - розница в области по сетевым(Калуганефтепродукт,
     * Газпромнефть и Лукойл)<br>
     * - ave_obl_reg - розница в области по АЗС входяших в мониторинг<br>
     * <br>
     * <br>
     * СРЗНАЧ по области берётся от СРЗНАЧ(СРЗНАЧ(мелких АЗС);
     * Калуганефтепродукта; Газпромнефти; Лукойли)
     * 
     * @param column
     *            - начальная колонка таблицы
     * @param row
     *            - начальная строка таблицы
     * @param sheet
     *            - в этот лист будет заносится таблица
     * @throws RowsExceededException
     * @throws WriteException
     */
    private void ave_result(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException {

	/**
	 * составление шапки
	 */
	sheet.addCell(new Label(column, row,
		"Изменение средней розничной цены", font.tahomaValue));
	sheet.mergeCells(column, row, column + 6, row++);

	sheet.addCell(new Label(column, row, "г. Калуга", font.tahomaValue));
	sheet.mergeCells(column, row, column + 2, row);

	sheet.addCell(new Label(column + 4, row, "Калужская область",
		font.tahomaValue));
	sheet.mergeCells(column + 4, row, column + 6, row++);

	/*
	 * обработка данных - формирование формул
	 */
	for (int i_toplivo = 0; i_toplivo < label_toplivo.length
		&& i_toplivo < 4; i_toplivo++) {

	    /**
	     * Наименование топлива в описание строки
	     */
	    for (int i = 0; i < 2; i++)
		sheet.addCell(new Label(column + i * 4, row + i_toplivo,
			label_toplivo[i_toplivo], font.tahomaValue));

	    /**
	     * Формирование формул СРЗНАЧ
	     */

	    /**
	     * По Калуге
	     */
	    // в процентах
	    String average_kaluga_per = getFormulaAVERAGE(getFormulaCells(
		    50 + i_toplivo, ave_k));
	    // в рублях
	    String average_kaluga_val = getFormulaAVERAGE(getFormulaCells(
		    58 + i_toplivo, ave_k));

	    /**
	     * по области
	     */

	    // в процентах

	    sheet.addCell(new Formula(
		    column + 9,
		    row + i_toplivo,
		    getFormulaCOUNT(getFormulaCells(50 + i_toplivo, ave_obl_reg)),
		    font.tahomaValue_white));

	    sheet.addCell(new Formula(
		    column + 8,
		    row + i_toplivo,
		    getFormulaSUM(getFormulaCells(50 + i_toplivo, ave_obl_reg)),
		    font.tahomaValue_white));

	    String div = toColumnExcel(column + 8) + (row + i_toplivo + 1)
		    + "/" + toColumnExcel(column + 9) + (row + i_toplivo + 1);

	    sheet.addCell(new Formula(column + 7, row + i_toplivo,
		    "IF(ISERROR(" + div + "),0," + div + ")",
		    font.tahomaValue_white));

	    Vector<String> ave_obl_all_per = getFormulaCells(50 + i_toplivo,
		    ave_obl_all);
	    ave_obl_all_per.add(toColumnExcel(column + 7)
		    + (row + i_toplivo + 1));

	    String average_obl_per = getFormulaAVERAGE(ave_obl_all_per);

	    // в рублях

	    sheet.addCell(new Formula(
		    column + 12,
		    row + i_toplivo,
		    getFormulaCOUNT(getFormulaCells(58 + i_toplivo, ave_obl_reg)),
		    font.tahomaValue_white));

	    sheet.addCell(new Formula(
		    column + 11,
		    row + i_toplivo,
		    getFormulaSUM(getFormulaCells(58 + i_toplivo, ave_obl_reg)),
		    font.tahomaValue_white));

	    String div2 = toColumnExcel(column + 11) + (row + i_toplivo + 1)
		    + "/" + toColumnExcel(column + 12) + (row + i_toplivo + 1);

	    sheet.addCell(new Formula(column + 10, row + i_toplivo,
		    "IF(ISERROR(" + div2 + "),0," + div2 + ")",
		    font.tahomaValue_white));

	    Vector<String> ave_obl_all_val = getFormulaCells(58 + i_toplivo,
		    ave_obl_all);
	    ave_obl_all_val.add(toColumnExcel(column + 10)
		    + (row + i_toplivo + 1));

	    String average_obl_val = getFormulaAVERAGE(ave_obl_all_val);

	    /*
	     * Запись формул в лист
	     */

	    /**
	     * Калуга
	     */
	    sheet.addCell(new Formula(column + 1, row + i_toplivo,
		    "IF(ISERROR(" + average_kaluga_per + "),0,"
			    + average_kaluga_per + ")", font.tahomaValuePer));

	    sheet.addCell(new Formula(column + 2, row + i_toplivo,
		    "IF(ISERROR(" + average_kaluga_val + "),0,"
			    + average_kaluga_val + ")", font.tahomaValue));

	    /**
	     * Область
	     */
	    sheet.addCell(new Formula(column + 5, row + i_toplivo,
		    "IF(ISERROR(" + average_obl_per + "),0," + average_obl_per
			    + ")", font.tahomaValuePer));

	    sheet.addCell(new Formula(column + 6, row + i_toplivo,
		    "IF(ISERROR(" + average_obl_val + "),0," + average_obl_val
			    + ")", font.tahomaValue));
	}
    }

    /**
     * Формирование формулы СРЗНАЧ<br>
     * <br>
     * Ячейка выбирается по след. принципу: ave_k[i].concat(rows)
     * 
     * @param rows
     *            - строка, по которой делается СРЗНАЧ
     * @param ave_k
     *            - массив с номерами колонок
     * @return формула СРЗНАЧ
     */
    private String getFormulaAVERAGE(Vector<String> ave_k) {

	return getFormulaSUM(ave_k) + " / " + getFormulaCOUNT(ave_k);
    }

    private Vector<String> getFormulaCells(int rows, Vector<Integer> ave_k) {
	Vector<String> result = new Vector<String>();

	for (int columnNum : ave_k)
	    result.add(toColumnExcel(columnNum) + rows);

	return result;
    }

    private String getFormulaCOUNT(Vector<String> ave_k) {
	String count = "";

	for (String column : ave_k) {

	    if (count.length() > 0)
		count += ",";

	    count += "IF(OR(" + column + ">0," + column + "<0)," + column
		    + ",\"a\")";
	}

	return "COUNT(" + count + ")";
    }

    private String getFormulaSUM(Vector<String> ave_k) {
	String sum = "";

	for (String column : ave_k) {

	    if (sum.length() > 0)
		sum += ",";

	    sum += "IF(ISNUMBER(" + column + ")," + column + ",0)";
	}

	return "SUM(" + sum + ")";
    }

    private int pro1(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {

	sheet.addCell(new Label(column, row, "ОАО \"Калуганефтепродукт\"",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 11, row);

	column = opt(column, row + 1, sheet, 1);

	column = nadbavka(column, row + 1, sheet);

	ave_k.add(column);
	column = vill(column, row + 1, sheet, 250, "г. Калуга розница");

	ave_obl_all.add(column);
	column = vill(column, row + 1, sheet, 251, "Область розница");

	column = vill(column, row + 1, sheet, 23, "г. Боровск");
	column = vill(column, row + 1, sheet, 212, "г. Обнинск");
	column = vill(column, row + 1, sheet, 58, "Жиздринский р-н");
	column = vill(column, row + 1, sheet, 5, "Бабынинский р-н");
	column = vill(column, row + 1, sheet, 40, "Дзержинский р-н");
	column = vill(column, row + 1, sheet, 90, "Козельский р-н");

	return column;
    }

    private int pro2(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"Газпромнефть-Центр\"",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 9, row);

	column = opt(column, row + 1, sheet, 2);

	column = nadbavka(column, row + 1, sheet);

	ave_k.add(column);
	column = vill(column, row + 1, sheet, 252, "г. Калуга розница");

	ave_obl_all.add(column);
	column = vill(column, row + 1, sheet, 253, "Область розница");

	column = vill(column, row + 1, sheet, 41, "Дзержинский р-н");
	column = vill(column, row + 1, sheet, 85, "Износковский р-н");
	column = vill(column, row + 1, sheet, 62, "Жиздринский р-н");
	column = vill(column, row + 1, sheet, 14, "Бабынинский р-н");

	return column;
    }

    private int pro3(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО ТД \"Альфа-Трейд\"",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 1, row);

	ave_k.add(column);
	column = vill(column, row + 1, sheet, 195, "г. Калуга");
	// sheet.addCell(new Label(column, row + 1, "в т.ч. мини АЗС",
	// font.tahomaLabelTitle));
	// column++;
	column = vill(column, row + 1, sheet, 254, "в т.ч. мини АЗС");

	return column;
    }

    private int pro4(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {

	sheet.addCell(new Label(column, row,
		"ООО \"Луйкойл-Центрнефтепродукт\"", font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 1, row);

	ave_k.add(column);
	column = vill(column, row + 1, sheet, 255, "г. Калуга розница");

	ave_obl_all.add(column);
	column = vill(column, row + 1, sheet, 256, "Область розница");

	return column;
    }

    private int pro5(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ИП Палашичев",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 1, row);

	ave_k.add(column);
	column = vill(column, row + 1, sheet, 199, "г. Калуга");

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 9, "Бабынинский р-н");

	return column;
    }

    private int pro6(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"ТрансАЗС-Сервис\"",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 215, "г. Обнинск");

	return column;
    }

    private int pro7(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"Октан\"",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 218, "г. Обнинск");

	return column;
    }

    private int pro8(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ИП Пешков",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 2, row);

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 144, "Сухиничский р-н");
	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 155, "Ульяновский р-н");
	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 138, "Перемышельский р-н");

	return column;
    }

    private int pro9(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ИП Журавлева",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 13, "Барятинский р-н");

	return column;
    }

    private int pro10(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"Березка\"",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 86, "Износковский р-н");

	return column;
    }

    private int pro11(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"Восток-Ойл\"",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column + 3, row);

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 133, "г. Мосальск");
	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 123, "Малоярославецкий р-н");
	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 130, "Мещовский р-н");
	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 94, "Куйбышевский р-н");

	return column;
    }

    private int pro12(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"Солид\"",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 157, "Ферзиковский р-н");

	return column;
    }

    private int pro13(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ООО \"Экоресурс\"",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 142, "Спас-Деменский р-н");

	return column;
    }

    private int pro14(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "АЗС №50 ИП Белова",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 161, "Хвастовичский р-н");

	return column;
    }

    private int pro15(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException, SQLException {
	sheet.addCell(new Label(column, row, "ОАО \"АЗС Хвастовичи\"",
		font.tahoma9ptBoldMedion));

	ave_obl_reg.add(column);
	column = vill(column, row + 1, sheet, 160, "Хвастовичский р-н");

	return column;
    }

    private int vill(int column, int row, WritableSheet sheet, int station_id,
	    String title) throws SQLException, RowsExceededException,
	    WriteException {
	sheet.addCell(new Label(column, row, title, font.tahomaLabelTitle));

	Vector<Integer> stationsComm = new Vector<Integer>();
	stationsComm.add(station_id);
	String[][] value = ave(stationsComm);

	osn(column, row + 1, sheet, value);
	return column + 1;
    }

    private int nadbavka(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException {
	sheet.addCell(new Label(column, row, "Размер надбавки % Калуга",
		font.tahomaValue));
	sheet.addCell(new Label(column + 1, row, "Размер надбавки % Область",
		font.tahomaValue));
	row++;
	for (int tau = 0; tau < time.length; tau++) {
	    for (int i = 0; i < 4; i++) {

		String fl = toColumnExcel(column - 1)
			+ Integer.toString(row + tau * 7 + i + 1);
		String fkaluga = toColumnExcel(column + 2)
			+ Integer.toString(row + tau * 7 + i + 1);
		String foblast = toColumnExcel(column + 3)
			+ Integer.toString(row + tau * 7 + i + 1);

		sheet.addCell(new Formula(column, row + tau * 7 + i,
			"IF(ISERROR(SUM(" + fkaluga + "-" + fl + ") / " + fl
				+ "),\"-\",SUM(" + fkaluga + "-" + fl + ") / "
				+ fl + ")", font.tahomaValuePer));
		sheet.addCell(new Formula(column + 1, row + tau * 7 + i,
			"IF(ISERROR(SUM(" + foblast + "-" + fl + ") / " + fl
				+ "),\"-\",SUM(" + foblast + "-" + fl + ") / "
				+ fl + ")", font.tahomaValuePer));
	    }
	}
	return column + 2;
    }

    @SuppressWarnings("unused")
    private int kaluga(int column, int row, WritableSheet sheet, int commId)
	    throws RowsExceededException, WriteException, SQLException {

	sheet.addCell(new Label(column, row, "г. Калуга розница",
		font.tahomaValue));

	/**
	 * хранит id всех АЗС одного сетевого поставщика по Калуги
	 */
	Vector<Integer> stationsComm = new Vector<Integer>();

	/**
	 * Получение id всех станций АЗС одного сетевого поставщика по Калуги
	 */
	ResultSet bdStations = DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeQuery(
			"SELECT id FROM station WHERE comm_id LIKE '" + commId
				+ "' AND district_id == '24';");

	while (bdStations.next()) {
	    stationsComm.add(bdStations.getInt("id"));
	}
	bdStations.close();

	String[][] value = ave(stationsComm);

	osn(column, row + 1, sheet, value);

	return column + 1;
    }

    @SuppressWarnings("unused")
    private int oblast(int column, int row, WritableSheet sheet, int commId)
	    throws RowsExceededException, WriteException, SQLException {

	sheet.addCell(new Label(column, row, "Область розница",
		font.tahomaValue));

	/**
	 * хранит id всех АЗС одного сетевого поставщика по Калуги
	 */
	Vector<Integer> stationsComm = new Vector<Integer>();

	/**
	 * Получение id всех станций АЗС одного сетевого поставщика по Калуги
	 */
	ResultSet bdStations = DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeQuery(
			"SELECT id FROM station WHERE comm_id LIKE '" + commId
				+ "' AND district_id != '24';");

	while (bdStations.next()) {
	    stationsComm.add(bdStations.getInt("id"));
	}
	bdStations.close();

	String[][] value = ave(stationsComm);

	osn(column, row + 1, sheet, value);

	return column + 1;
    }

    private void osn(int column, int row, WritableSheet sheet, String[][] value)
	    throws RowsExceededException, WriteException {
	int index = 0;

	String[][] result = new String[2][4];

	for (int tau = 0; tau < value.length; tau++) {
	    for (int i = 0; i < value[tau].length; i++) {
		/**
		 * Запоминаем индексы строк и столбцов первой и последней даты
		 * для отображения в формуле
		 * 
		 * Если первая дата: ставится значение с черной цветом(т.е. по
		 * отношение к пред. дате не было)
		 */
		if (tau == 0) {
		    result[0][i] = toColumnExcel(column)
			    + Integer.toString(row + i + tau * 7 + 1);

		    sheet.addCell(new Label(column, row + i + tau * 7,
			    toStr(value[tau][i]), font.tahomaValue));
		} else {
		    try {
			double val = Double.valueOf(value[tau][i])
				- Double.valueOf(value[tau - 1][i]);

			if (val > 0)
			    sheet.addCell(new Label(column, row + i + tau * 7,
				    toStr(value[tau][i]), font.tahomaValue_red));
			else if (val < 0)
			    sheet.addCell(new Label(column, row + i + tau * 7,
				    toStr(value[tau][i]), font.tahomaValue_blue));
			else

			    sheet.addCell(new Label(column, row + i + tau * 7,
				    toStr(value[tau][i]), font.tahomaValue));

		    } catch (Exception e) {
			sheet.addCell(new Label(column, row + i + tau * 7,
				toStr(value[tau][i]), font.tahomaValue));
		    }

		}

		if (tau == time.length - 1) {
		    result[1][i] = toColumnExcel(column)
			    + Integer.toString(row + i + tau * 7 + 1);
		    // для удобства
		    index = row + 7 + tau * 7;
		}
	    }
	}

	index++;

	/**
	 * составление формул<br>
	 * Если нет ошибки записывается формула<br>
	 * Если есть ошибка записывается "-"
	 */
	for (int i = 0; i < result[0].length; i++) {

	    sheet.addCell(new Formula(column, index, "IF(ISERROR(SUM("
		    + result[1][i] + "-" + result[0][i] + ") / " + result[0][i]
		    + "),\"-\",SUM(" + result[1][i] + "-" + result[0][i]
		    + ") / " + result[0][i] + ")", font.tahomaValuePer));

	    sheet.addCell(new Formula(column, index + 8, "IF(ISERROR(SUM("
		    + result[1][i] + "-" + result[0][i] + ")),\"-\"," + "SUM("
		    + result[1][i] + "-" + result[0][i] + "))",
		    font.tahomaValue));

	    index++;
	}
    }

    private String[][] ave(Vector<Integer> stationsComm) throws SQLException {
	/**
	 * Цены на каждой АЗС за весь период(квартал) - ОДНОГО ПОСТАВЩИКА
	 */
	Vector<String[]> all_b80 = new Vector<String[]>();
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
			    "SELECT changedate, b95, b92, bdis, b80 FROM change WHERE station_id LIKE '"
				    + stationsComm.get(st) + "';");

	    Vector<String[]> timeLine = new Vector<String[]>();
	    while (stChange.next()) {
		String[] change = new String[5];

		change[0] = stChange.getString("changedate");
		change[1] = stChange.getString("b95");
		change[2] = stChange.getString("b92");
		change[3] = stChange.getString("bdis");
		change[4] = stChange.getString("b80");

		timeLine.add(0, change);
	    }
	    stChange.close();
	    /**
	     * проверка на наличие изменений в АЗС (в нефтепродуктах была АЗС
	     * без цен)
	     */
	    if (timeLine.size() > 0) {
		/**
		 * данные одной АЗС за квартал
		 */
		String[] data_b80 = new String[time.length];
		String[] data_b95 = new String[time.length];
		String[] data_b92 = new String[time.length];
		String[] data_bdis = new String[time.length];

		/**
		 * Обработка данных
		 */
		for (int tau = 0; tau < time.length && timeLine.size() > 0; tau++) {
		    /**
		     * поиск записи, наиболее приблежённой к времени тау
		     */
		    String[] current_data = { "0", "-", "-", "-", "-" };

		    for (int p = 0; p < timeLine.size(); p++) {
			if (Long.parseLong(timeLine.get(p)[0]) < time[tau]
				&& Long.parseLong(current_data[0]) < Long
					.parseLong(timeLine.get(p)[0])) {
			    current_data = timeLine.get(p);
			}
		    }

		    /**
		     * сохранение найденного значения
		     */
		    data_b80[tau] = current_data[4];
		    data_b95[tau] = current_data[1];
		    data_b92[tau] = current_data[2];
		    data_bdis[tau] = current_data[3];
		}

		/**
		 * сохранение найденных значений АЗС за квартал
		 */
		all_b80.add(data_b80);
		all_b95.add(data_b95);
		all_b92.add(data_b92);
		all_bdis.add(data_bdis);
	    }
	}

	/**
	 * Поиск среднего значения для сетевого поставщика
	 */

	/**
	 * Среднее значение поставщика по каждому виду топлива
	 */
	double[] b80 = new double[time.length];
	double[] b95 = new double[time.length];
	double[] b92 = new double[time.length];
	double[] bdis = new double[time.length];

	/**
	 * обнуление значений, для вычисление средних значений
	 */
	for (int i = 0; i < time.length; i++) {
	    b80[i] = 0f;
	    b95[i] = 0f;
	    b92[i] = 0f;
	    bdis[i] = 0f;
	}

	/**
	 * суммирование значений всех АЗС одного поставщика за квартал(12
	 * недель)
	 */
	for (int station = 0; station < all_b95.size(); station++) {
	    String[] temp_80 = all_b80.get(station);
	    String[] temp_95 = all_b95.get(station);
	    String[] temp_92 = all_b92.get(station);
	    String[] temp_dis = all_bdis.get(station);

	    for (int i = 0; i < time.length; i++) {
		b80[i] = new BigDecimal(b80[i]
			+ parseStringToDouble(temp_80[i])).setScale(2,
			RoundingMode.HALF_UP).doubleValue();

		b95[i] = new BigDecimal(b95[i]
			+ parseStringToDouble(temp_95[i])).setScale(2,
			RoundingMode.HALF_UP).doubleValue();

		b92[i] = new BigDecimal(b92[i]
			+ parseStringToDouble(temp_92[i])).setScale(2,
			RoundingMode.HALF_UP).doubleValue();

		bdis[i] = new BigDecimal(bdis[i]
			+ parseStringToDouble(temp_dis[i])).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
	    }
	}

	/**
	 * приведение к среднему значению(деление суммы на количество цен(АЗС))
	 */
	if (all_b80.size() > 0)
	    for (int i = 0; i < time.length; i++) {
		b80[i] = new BigDecimal(b80[i] / all_b80.size()).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
		b95[i] = new BigDecimal(b95[i] / all_b95.size()).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
		b92[i] = new BigDecimal(b92[i] / all_b92.size()).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
		bdis[i] = new BigDecimal(bdis[i] / all_bdis.size()).setScale(2,
			RoundingMode.HALF_UP).doubleValue();
	    }

	String[][] result = new String[time.length][4];
	for (int i = 0; i < time.length; i++) {
	    result[i][0] = (b80[i] > 0) ? Double.toString(b80[i]) : "-";
	    result[i][1] = (b92[i] > 0) ? Double.toString(b92[i]) : "-";
	    result[i][2] = (b95[i] > 0) ? Double.toString(b95[i]) : "-";
	    result[i][3] = (bdis[i] > 0) ? Double.toString(bdis[i]) : "-";
	}
	return result;
    }

    private int opt(int column, int row, WritableSheet sheet, int commId)
	    throws RowsExceededException, WriteException, SQLException {
	int index = 0;

	/**
	 * Заголовок
	 */
	sheet.addCell(new Label(column, row, "Опт руб/т", font.tahomaValue));
	sheet.addCell(new Label(column + 1, row, "Опт руб/л", font.tahomaValue));

	/**
	 * Поиск значений для временного отрезка
	 */
	ResultSet changeComm = DriverManager
		.getConnection("jdbc:sqlite:" + Oil.PATH)
		.createStatement()
		.executeQuery(
			"SELECT * FROM optov WHERE comm_id LIKE '" + commId
				+ "';");
	/**
	 * хранит значения временной линии
	 */
	Vector<String[]> changeTimeLine = new Vector<String[]>();

	while (changeComm.next()) {
	    String[] change = new String[15];

	    change[0] = changeComm.getString("changedate");
	    change[1] = changeComm.getString("b80_t");
	    change[2] = changeComm.getString("b92_t");
	    change[3] = changeComm.getString("b95_t");
	    change[4] = changeComm.getString("bdis_leto1_t");
	    change[5] = changeComm.getString("bdis_leto2_t");
	    change[6] = changeComm.getString("bdis_mc_t");
	    change[7] = changeComm.getString("bdis_winter_t");

	    change[8] = changeComm.getString("b80_l");
	    change[9] = changeComm.getString("b92_l");
	    change[10] = changeComm.getString("b95_l");
	    change[11] = changeComm.getString("bdis_leto1_l");
	    change[12] = changeComm.getString("bdis_leto2_l");
	    change[13] = changeComm.getString("bdis_mc_l");
	    change[14] = changeComm.getString("bdis_winter_l");

	    changeTimeLine.add(0, change);
	}
	changeComm.close();

	/**
	 * Обработка данных
	 */
	if (changeTimeLine.size() > 0) {

	    String[][] result = new String[4][7];

	    /**
	     * поиск записи, наиболее приблежённой к времени тау
	     */
	    String[][] current_data = new String[time.length][changeTimeLine
		    .get(0).length];

	    /**
	     * отсеивание лишних значений(не подходяших к времени тау)
	     */
	    for (int tau = 0; tau < time.length && changeTimeLine.size() > 0; tau++) {

		/**
		 * минимальное время и => значения нет
		 */
		current_data[tau][0] = "0";
		for (int i = 1; i < current_data.length; i++)
		    current_data[tau][i] = "-";

		/**
		 * сравнение времени текущего и просматриваемого
		 */
		for (int p = 0; p < changeTimeLine.size(); p++) {
		    if (Long.parseLong(changeTimeLine.get(p)[0]) < time[tau]
			    && Long.parseLong(current_data[tau][0]) < Long
				    .parseLong(changeTimeLine.get(p)[0])) {
			/**
			 * нашли более выгодное время
			 */
			current_data[tau] = changeTimeLine.get(p);
		    }
		}

		/**
		 * занесение найденного значения
		 */
		for (int i = 1; i < 8; i++) {
		    /**
		     * Запоминаем индексы строк и столбцов первой и последней
		     * даты для отображения в формуле
		     */
		    if (tau == 0) {
			result[0][i - 1] = toColumnExcel(column)
				+ Integer.toString(row + i + tau * 7 + 1);

			result[1][i - 1] = toColumnExcel(column + 1)
				+ Integer.toString(row + i + tau * 7 + 1);

			/**
			 * тонны
			 */
			sheet.addCell(new Label(column, row + i + tau * 7,
				toStr(current_data[tau][i]), font.tahomaValue));
			/**
			 * литры
			 */
			sheet.addCell(new Label(column + 1, row + i + tau * 7,
				toStr(current_data[tau][i + 7]),
				font.tahomaValue));
		    } else {
			try {
			    double val = Double.valueOf((current_data[tau][i]))
				    - Double.valueOf((current_data[tau - 1][i]));
			    double val2 = Double
				    .valueOf((current_data[tau][i + 7]))
				    - Double.valueOf((current_data[tau - 1][i + 7]));

			    if (val > 0)
				sheet.addCell(new Label(column, row + i + tau
					* 7, toStr(current_data[tau][i]),
					font.tahomaValue_red));
			    else if (val < 0)
				sheet.addCell(new Label(column, row + i + tau
					* 7, toStr(current_data[tau][i]),
					font.tahomaValue_blue));
			    else
				sheet.addCell(new Label(column, row + i + tau
					* 7, toStr(current_data[tau][i]),
					font.tahomaValue));

			    if (val2 > 0)
				sheet.addCell(new Label(column + 1, row + i
					+ tau * 7,
					toStr(current_data[tau][i + 7]),
					font.tahomaValue_red));
			    else if (val2 < 0)
				sheet.addCell(new Label(column + 1, row + i
					+ tau * 7,
					toStr(current_data[tau][i + 7]),
					font.tahomaValue_blue));
			    else
				sheet.addCell(new Label(column + 1, row + i
					+ tau * 7,
					toStr(current_data[tau][i + 7]),
					font.tahomaValue));

			} catch (Exception e) {
			    sheet.addCell(new Label(column, row + i + tau * 7,
				    toStr(current_data[tau][i]),
				    font.tahomaValue));

			    sheet.addCell(new Label(column + 1, row + i + tau
				    * 7, toStr(current_data[tau][i + 7]),
				    font.tahomaValue));
			}
		    }

		    if (tau == time.length - 1) {
			result[2][i - 1] = toColumnExcel(column)
				+ Integer.toString(row + i + tau * 7 + 1);

			result[3][i - 1] = toColumnExcel(column + 1)
				+ Integer.toString(row + i + tau * 7 + 1);
			// для удобства
			index = row + i + tau * 7;
		    }
		}
	    }
	    index += 2;

	    /**
	     * составление формул Если нет ошибки записывается формула Если есть
	     * ошибка записывается "-"
	     */
	    for (int i = 0; i < result[0].length; i++) {

		for (int p = 0; p < 2; p++) {
		    sheet.addCell(new Formula(column + p, index,
			    "IF(ISERROR(SUM(" + result[2 + p][i] + "-"
				    + result[0 + p][i] + ") / "
				    + result[0 + p][i] + "),\"-\",SUM("
				    + result[2 + p][i] + "-" + result[0 + p][i]
				    + ") / " + result[0 + p][i] + ")",
			    font.tahomaValuePer));

		    sheet.addCell(new Formula(column + p, index + 8,
			    "IF(ISERROR(SUM(" + result[2 + p][i] + "-"
				    + result[p][i] + ")),\"-\"," + "SUM("
				    + result[2 + p][i] + "-" + result[p][i]
				    + "))", font.tahomaValue));
		}
		index++;
	    }

	}
	return column + 2;
    }

    /**
     * Создание шапки с датами и марками топлива
     * 
     * @param column
     *            - номер первой колонки
     * @param row
     *            - номер первой строки
     * @param sheet
     *            - лист, на котором будет записываться
     * @throws RowsExceededException
     * @throws WriteException
     */
    private int cape(int column, int row, WritableSheet sheet)
	    throws RowsExceededException, WriteException {

	/**
	 * формат записи даты<br>
	 * Пример 26.04.2013 г.
	 */
	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");

	/**
	 * Список надписей под результатом
	 */
	String[] result = { "Изменение цен %", "Изменение цен руб." };

	/*
	 * Поле "Дата"
	 */
	sheet.addCell(new Label(column, row, "Дата", font.tahoma9ptBoldMedion));
	sheet.mergeCells(column, row, column, row + 1);
	sheet.setColumnView(column, 15);

	/**
	 * Поле "Марка бензина"
	 */
	sheet.addCell(new Label(column + 1, row, "Марка бензина",
		font.tahoma9ptBoldMedion));
	sheet.mergeCells(column + 1, row, column + 1, row + 1);
	sheet.setColumnView(column + 1, 10);

	/**
	 * первая строчка под наименование поставщика
	 */
	sheet.setRowView(row, 900);
	/**
	 * вторая строчка под пояснение выбранного значения
	 */
	sheet.setRowView(row + 1, 2000);

	/**
	 * Запись даты и марки топлива
	 */
	for (int i = 0; i < time.length; i++) {
	    /**
	     * дата
	     */
	    sheet.addCell(new Label(column, row + 2 + i * 7, formatter
		    .format(new Date(time[i])) + " г.",
		    font.tahoma9ptBoldMedion));
	    /**
	     * склеивание со строчками марки топлива
	     */
	    sheet.mergeCells(column, row + 2 + i * 7, column, row + 2 + i * 7
		    + 6);

	    /**
	     * запись марки топлива
	     */
	    for (int p = 0; p < label_toplivo.length; p++) {
		sheet.addCell(new Label(column + 1, row + 2 + i * 7 + p,
			label_toplivo[p], font.tahomaLabelToplivo));
	    }
	}

	// для удобства
	int nextRow = row + 2 + 7 * time.length;

	/**
	 * Запись результата
	 */
	for (String res : result) {
	    /**
	     * Титульник
	     */
	    sheet.addCell(new Label(column, nextRow, res,
		    font.tahoma9ptBoldMedion));
	    sheet.mergeCells(column, nextRow, column + 1, nextRow);

	    /**
	     * марка топлива
	     */
	    for (int p = 0; p < label_toplivo.length; p++) {
		sheet.addCell(new Label(column, nextRow + 1 + p,
			label_toplivo[p], font.tahoma9ptBoldMedion));
		sheet.mergeCells(column, nextRow + 1 + p, column + 1, nextRow
			+ 1 + p);
	    }

	    /**
	     * смещение к след пункту, без потери номера строки
	     */
	    nextRow += label_toplivo.length + 1;
	}

	return column + 2;
    }

    /**
     * Заменяет точку(.) на запяую(,)
     * 
     * @param str
     *            - значение(23.2)
     * @return исправльное значение(23,2)
     */
    private String toStr(String str) {
	String result = "";
	if(str != null)
	result = str.replace(".", ",");

	return result;
    }

    /**
     * Вычисляет символьное представления индекса колонки
     * 
     * @param value
     *            - цифровой индекс колонки(33)
     * @return текстовый индекс колонки(AF)
     */
    private String toColumnExcel(Integer value) {
	// промежуточный результат
	String result = "";
	// для определения первого символа
	boolean first = true;

	while (value / 26 > 0) {
	    if (first) {
		result += (char) (65 + value % 26);
		first = false;
	    } else {
		result += (char) (64 + value % 26);
	    }

	    value = value / 26;
	}

	if (first) {
	    result += (char) (65 + value % 26);
	} else {
	    result += (char) (64 + value % 26);
	}

	// переварачиваем результат EFA = > AFE
	String res = "";

	for (int i = 0; i < result.length(); i++) {
	    res += result.substring(result.length() - i - 1, result.length()
		    - i);
	}

	return res;
    }

    public boolean checkString(String string) {
	try {
	    Double.parseDouble(string);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }

    private Double parseStringToDouble(String value) {
	if (value != null) {
	    value = value.replace(" ", "");
	    value = value.replace(" ", "");
	    value = value.replace(",", ".");

	    try {
		return Double.parseDouble(value);
	    } catch (Exception e) {
		// JOptionPane.showMessageDialog(null,
		// "Ошибка; Позвонить Кириллу!");
		// System.exit(0);
		return 0.0;
	    }
	}

	return 0.0;
    }
}
