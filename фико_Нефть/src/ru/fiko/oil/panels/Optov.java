package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;

import ru.fiko.oil.main.Oil;
import ru.fiko.oil.panels.Stations.PopUpTable;
import ru.fiko.oil.supp.ItemOptov;
import ru.fiko.oil.supp.ItemStation;

public class Optov extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1824439631027088992L;

    /**
     * id сетевых поставщиков, по которым происходит мониторинг оптовых поставок<br>
     * 1 - Калуганефтепродукт<br>
     * 2 - Газпромнефть
     */
    private int[] comm_ids = { 1, 2 };

    public ItemOptov current;

    public Optov() throws ClassNotFoundException, SQLException {
	Class.forName("org.sqlite.JDBC");

	this.setLayout(new BorderLayout());
	this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

	JPanel table = new JPanel();
	table.setLayout(new BoxLayout(table, BoxLayout.PAGE_AXIS));

	for (int comm_id : comm_ids) {

	    Long max_time = (long) 0;
	    String changeid = "";

	    String b80_t = "";
	    String b92_t = "";
	    String b95_t = "";
	    String bdis_mc_t = "";
	    String bdis_winter_t = "";
	    String bdis_leto1_t = "";
	    String bdis_leto2_t = "";

	    String b80_l = "";
	    String b92_l = "";
	    String b95_l = "";
	    String bdis_mc_l = "";
	    String bdis_winter_l = "";
	    String bdis_leto1_l = "";
	    String bdis_leto2_l = "";

	    ResultSet value = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT * FROM optov WHERE comm_id LIKE '"
				    + comm_id + "';");

	    while (value.next()) {
		Long temp = Long.parseLong(value.getString("changedate"));

		if (temp > max_time) {
		    max_time = temp;

		    b80_t = value.getString("b80_t");
		    b92_t = value.getString("b92_t");
		    b95_t = value.getString("b95_t");
		    bdis_mc_t = value.getString("bdis_mc_t");
		    bdis_winter_t = value.getString("bdis_winter_t");
		    bdis_leto1_t = value.getString("bdis_leto1_t");
		    bdis_leto2_t = value.getString("bdis_leto2_t");

		    b80_l = value.getString("b80_l");
		    b92_l = value.getString("b92_l");
		    b95_l = value.getString("b95_l");
		    bdis_mc_l = value.getString("bdis_mc_l");
		    bdis_winter_l = value.getString("bdis_winter_l");
		    bdis_leto1_l = value.getString("bdis_leto1_l");
		    bdis_leto2_l = value.getString("bdis_leto2_l");

		    changeid = value.getString("id");
		}
	    }

	    DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
	    Date date = new Date(max_time);

	    ItemOptov item = new ItemOptov();

	    item.setThis(this);

	    // id
	    item.setCommId(Integer.toString(comm_id));
	    // id последнего изменения
	    item.setChangeId(changeid);
	    // дата последнего изменения
	    item.setDate((dateFormat.format(date).toString()));
	    // Наименование
	    item.setTitle(Integer.toString(comm_id));

	    // Топливо
	    item.setB80_l(b80_l);
	    item.setB92_l(b92_l);
	    item.setB95_l(b95_l);
	    item.setBdis_leto1_l(bdis_leto1_l);
	    item.setBdis_leto2_l(bdis_leto2_l);
	    item.setBdis_mc_l(bdis_mc_l);
	    item.setBdis_winter_l(bdis_winter_l);

	    item.setB80_t(b80_t);
	    item.setB92_t(b92_t);
	    item.setB95_t(b95_t);
	    item.setBdis_leto1_t(bdis_leto1_t);
	    item.setBdis_leto2_t(bdis_leto2_t);
	    item.setBdis_mc_t(bdis_mc_t);
	    item.setBdis_winter_t(bdis_winter_t);

	    // создание карточки
	    item.initialization();
	    table.add(item);

	    value.close();
	}

	/*
	 * Скроллбар из-за количества записей, не влезавших в видимую область +
	 * увеличина прокрутка колесом мыши
	 */
	JScrollPane pane = new JScrollPane(table);
	JScrollBar jsp = pane.getVerticalScrollBar();
	jsp.setUnitIncrement(20);

	this.add(pane, BorderLayout.CENTER);

	this.repaint();
	this.validate();
    }

}
