package ru.fiko.oil.supp;

import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WriteException;

public class JXLConstant {

    public WritableCellFormat tahoma9ptBoldMedion = new WritableCellFormat(
	    new WritableFont(WritableFont.TAHOMA, 9, WritableFont.BOLD));

    public WritableCellFormat tahoma14ptBold = new WritableCellFormat(
	    new WritableFont(WritableFont.TAHOMA, 14, WritableFont.BOLD));

    public WritableCellFormat tahomaLabelToplivo = new WritableCellFormat(
	    new WritableFont(WritableFont.TAHOMA, 9, WritableFont.BOLD));

    public WritableCellFormat tahomaValue = new WritableCellFormat(
	    NumberFormats.FLOAT);

    public WritableCellFormat tahomaValuePer = new WritableCellFormat(
	    NumberFormats.PERCENT_FLOAT);

    public JXLConstant() throws WriteException {
	/*
	 * Основной формат ячеек Tahoma 9pt, no bold выравнивание по
	 * горизонтале: центр выравнивание по вертикале: центр перенос по словам
	 * стиль границы - все цвет фона - без цвета
	 */
	tahoma9ptBoldMedion.setAlignment(Alignment.CENTRE);
	tahoma9ptBoldMedion.setVerticalAlignment(VerticalAlignment.CENTRE);
	tahoma9ptBoldMedion.setWrap(true);
	tahoma9ptBoldMedion.setBorder(Border.ALL, BorderLineStyle.THIN);

	tahomaValue.setAlignment(Alignment.CENTRE);
	tahomaValue.setVerticalAlignment(VerticalAlignment.CENTRE);
	tahomaValue.setWrap(true);
	tahomaValue.setBorder(Border.ALL, BorderLineStyle.THIN);
	tahomaValue.setFont(new WritableFont(WritableFont.TAHOMA, 9, WritableFont.NO_BOLD));

	tahomaValuePer.setAlignment(Alignment.CENTRE);
	tahomaValuePer.setVerticalAlignment(VerticalAlignment.CENTRE);
	tahomaValuePer.setWrap(true);
	tahomaValuePer.setBorder(Border.ALL, BorderLineStyle.THIN);
	tahomaValuePer.setFont(new WritableFont(WritableFont.TAHOMA, 9,
		WritableFont.NO_BOLD));

	tahomaLabelToplivo.setAlignment(Alignment.LEFT);
	tahomaLabelToplivo.setVerticalAlignment(VerticalAlignment.CENTRE);
	tahomaLabelToplivo.setWrap(true);
	tahomaLabelToplivo.setBorder(Border.ALL, BorderLineStyle.THIN);

	tahoma14ptBold.setAlignment(Alignment.CENTRE);
	tahoma14ptBold.setVerticalAlignment(VerticalAlignment.CENTRE);
	tahoma14ptBold.setWrap(true);
	tahoma14ptBold.setBorder(Border.NONE, BorderLineStyle.THIN);
    }

}
