package ru.fiko.oil.data;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import ru.fiko.oil.supp.JXLConstant;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

public class OutputSvod {

    private WritableWorkbook workbook;
    private WritableSheet sheet;

    public OutputSvod() throws IOException, WriteException {

	workbook = Workbook.createWorkbook(new File(
		"svod_data.xls"));

	sheet = workbook.createSheet("Мнониторинг цен", 0);

	workbook.write();
	workbook.close();
	JOptionPane.showMessageDialog(null, "Готово");
    }
    
    private void cape() throws RowsExceededException, WriteException{
	
	  sheet.addCell(new Label(0, 1,
		    "Электроэнергия (тыс. кВт•ч)",
		    JXLConstant.tahoma9pt));
	;
    }
}
