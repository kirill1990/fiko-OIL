import static org.junit.Assert.*;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import org.junit.Test;

import ru.fiko.oil.main.Oil;
import ru.fiko.oil.panels.Station;
import ru.fiko.oil.panels.Stations;
import ru.fiko.oil.supp.ComboItem;


public class TestCase
{

	@Test
	public void test() throws SQLException, ClassNotFoundException
	{
		Vector<Vector<String>> values = new Vector<Vector<String>>();
		{
			String district_id = "0";
			String comm_id = "0";

		ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + "oil.db").createStatement().executeQuery("SELECT id,title,active FROM station WHERE district_id LIKE '" + district_id + "' AND comm_id LIKE '" + comm_id + "';");

	while (rs.next())
	{
		// поиск последнего изменения
		ResultSet time = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT changedate,b80,b92,b95,bdis FROM change WHERE station_id LIKE '" + rs.getString(1) + "';");

		Long max_time = (long) 0;
		String b80 = "";
		String b92 = "";
		String b95 = "";
		String bdis = "";

		while (time.next())
		{
			Long temp = Long.parseLong(time.getString(1));

			if (temp > max_time)
			{
				max_time = temp;
				b80 = time.getString(2);
				b92 = time.getString(3);
				b95 = time.getString(4);
				bdis = time.getString(5);
			}
		}
		time.close();

		DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
		Date date = new Date(max_time);

		Vector<String> item = new Vector<String>();

		// id
		item.add(rs.getString(1));
		// дата последнего изменения
		item.add(dateFormat.format(date).toString());
		// Состояние
		item.add(rs.getString(3));
		// Наименование
		item.add(rs.getString(2));
		// Топливо
		item.add(b80);
		item.add(b92);
		item.add(b95);
		item.add(bdis);

		values.add(item);
	}

	rs.close();
}
	}

}
