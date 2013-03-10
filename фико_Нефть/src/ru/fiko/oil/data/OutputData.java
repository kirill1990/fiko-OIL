/**
 * 
 */
package ru.fiko.oil.data;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import ru.fiko.oil.main.Oil;

/**
 * @author kirill
 * 
 */
public class OutputData
{

	private String	pathFolder	= "";

	public OutputData() throws ClassNotFoundException, FileNotFoundException, ParserConfigurationException, TransformerException, SQLException
	{
		Class.forName("org.sqlite.JDBC");
		regions();
		providers();
//		clients();
	}

	private void regions() throws ParserConfigurationException, FileNotFoundException, TransformerException, SQLException
	{
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
		ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM district;");

		while (rs.next())
		{
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
			ResultSet rs_city = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM city WHERE district_id LIKE '" + district_id + "';");
			while (rs_city.next())
			{
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

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result res = new StreamResult(new FileOutputStream(pathFolder + "regions.xml"));
		transformer.transform(new DOMSource(doc), res);
		res = null;
	}

	private void providers() throws ParserConfigurationException, FileNotFoundException, TransformerException, SQLException
	{
		// Создаём хмл файл
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();

		Document doc = builder.newDocument();

		doc.setXmlVersion("1.0");
		doc.setXmlStandalone(true);

		Element service = doc.createElement("service");
		service.setAttribute("name", "Поставщики");

		// Выборка Региона
		ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id FROM district;");

		while (rs.next())
		{
			// район
			Element district = doc.createElement("district");
			district.setAttribute("id", rs.getString("id"));

			int district_id = rs.getInt("id");

			// выборка городов
			ResultSet rs_city = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id FROM city WHERE district_id LIKE '" + district_id + "';");
			while (rs_city.next())
			{
				int city_id = rs_city.getInt("id");

				// выборка азс города
				ResultSet rs_provider = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM station WHERE district_id LIKE '" + district_id + "' AND city_id LIKE '" + city_id + "';");
				while (rs_provider.next())
				{
					// выбираем только действующие АЗС(с пометкой актив == true)
					if (rs_provider.getString("active").equals("true"))
					{
						int station_id = rs_provider.getInt("id");

						Long max_time = (long) 0;
						String b80 = "";
						String b92 = "";
						String b95 = "";
						String bdis = "";

						// поиск последнего изменения
						ResultSet time = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT changedate,b80,b92,b95,bdis FROM change WHERE station_id LIKE '" + station_id + "';");
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

						// определение имени сетевого поставщика
						int comm_id = rs_provider.getInt("comm_id");
						String comm_str = "";

						ResultSet comm = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT title FROM commercial WHERE id LIKE '" + comm_id + "';");
						if (comm.next())
							comm_str = comm.getString("title");
						comm.close();

						// создание поставщика
						Element provider = doc.createElement("provider");
						provider.setAttribute("name", rs_provider.getString("title"));
						provider.setAttribute("address", rs_provider.getString("address"));
						provider.setAttribute("web", comm_str);
						provider.setAttribute("b80", b80);
						provider.setAttribute("b92", b92);
						provider.setAttribute("b95", b95);
						provider.setAttribute("bdis", bdis);

						// определяем, куда впихнуть поставщика:
						boolean isCity = false;
						// если создан города, включаем поставщика в него
						for (int p = 0; p < district.getChildNodes().getLength(); p++)
						{
							if (district.getChildNodes().item(p).getAttributes().getNamedItem("id").getNodeValue().equals(Integer.toString(city_id)))
							{
								district.getChildNodes().item(p).appendChild(provider);
								isCity = true;
							}
						}
						// если город не был добавлен:
						// создаётся новый элемент и поставщик вносится в него
						if (!isCity)
						{
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

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result res = new StreamResult(new FileOutputStream(pathFolder + "providers.xml"));
		transformer.transform(new DOMSource(doc), res);
		res = null;
	}

	private void clients() throws ParserConfigurationException, FileNotFoundException, TransformerException, SQLException
	{
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
		ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id FROM district;");

		while (rs.next())
		{
			// район
			Element district = doc.createElement("district");
			district.setAttribute("id", rs.getString("id"));

			int district_id = rs.getInt("id");

			// выборка городов
			ResultSet rs_city = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT id FROM city WHERE district_id LIKE '" + district_id + "';");
			while (rs_city.next())
			{
				int city_id = rs_city.getInt("id");

				// выборка азс города
				ResultSet rs_client = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM client WHERE district_id LIKE '" + district_id + "' AND city_id LIKE '" + city_id + "';");
				while (rs_client.next())
				{
					Element provider = doc.createElement("provider");
					provider.setAttribute("name", rs_client.getString("title"));
					provider.setAttribute("address", rs_client.getString("address"));
					
					// определяем, куда впихнуть поставщика:
					boolean isCity = false;
					// если создан города, включаем поставщика в него
					for (int p = 0; p < district.getChildNodes().getLength(); p++)
					{
						if (district.getChildNodes().item(p).getAttributes().getNamedItem("id").getNodeValue().equals(Integer.toString(city_id)))
						{
							district.getChildNodes().item(p).appendChild(provider);
							isCity = true;
						}
					}
					// если город не был добавлен:
					// создаётся новый элемент и поставщик вносится в него
					if (!isCity)
					{
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

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		Result res = new StreamResult(new FileOutputStream(pathFolder + "clients.xml"));
		transformer.transform(new DOMSource(doc), res);
		res = null;
	}
}
