package ru.fiko.oil.supp;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;

public class JXLConstant {

    public static WritableCellFormat tahoma9pt = new WritableCellFormat(
		new WritableFont(WritableFont.TAHOMA, 9,
			WritableFont.NO_BOLD));
    
    public JXLConstant() throws WriteException{
	/*
	 * Основной формат ячеек Tahoma 9pt, no bold выравнивание по
	 * горизонтале: центр выравнивание по вертикале: центр перенос
	 * по словам стиль границы - все цвет фона - без цвета
	 */
	
	tahoma9pt.setAlignment(Alignment.CENTRE);
	tahoma9pt.setVerticalAlignment(VerticalAlignment.CENTRE);
	tahoma9pt.setWrap(true);
	tahoma9pt.setBorder(Border.ALL, BorderLineStyle.THIN);
    }
    
}
