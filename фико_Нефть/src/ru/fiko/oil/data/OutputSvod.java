package ru.fiko.oil.data;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.swing.JOptionPane;

import ru.fiko.oil.supp.JXLConstant;

import jxl.Workbook;
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

    private WritableWorkbook workbook;
    private WritableSheet sheet;

    public OutputSvod() throws IOException, WriteException {

	/**
	 * Формирование дат отчетности
	 */

	/**
	 * текущие время
	 */
	long current = System.currentTimeMillis();

	current = 1366920000000l + 86400000 - 1;

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
	     *  коэффицент смещения дней
	     */
	    int mod = 1;

	    /**
	     * День недели i-ого числа
	     */
	    String day = new SimpleDateFormat("E", Locale.ENGLISH).format(time[i + 1] - 86400000);

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

	SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
	for (long index : time) {

	    try {
		System.out.println(formatter.parse(formatter.format(new Date(
			index))));
	    } catch (ParseException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }
	}

	workbook = Workbook.createWorkbook(new File("svod_data.xls"));

	sheet = workbook.createSheet("Мнониторинг цен", 0);

	workbook.write();
	workbook.close();
	JOptionPane.showMessageDialog(null, "Готово");
    }

    private void cape() throws RowsExceededException, WriteException {

	sheet.addCell(new Label(0, 1, "Электроэнергия (тыс. кВт•ч)",
		JXLConstant.tahoma9pt));
	;
    }
}
