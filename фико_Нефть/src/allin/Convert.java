package allin;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import jxl.Cell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.VerticalAlignment;
import jxl.read.biff.BiffException;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class Convert extends JFrame
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2133593774556351181L;
	public final static int		WIDTH				= 500;
	public final static int		HEIGHT				= 150;

	private static String		pathFile			= "";
	private static String		pathFolder			= "";

	JTextField					jFilePath			= null;
	JTextField					jFolderPath			= null;
	JButton						jBrowseFile			= null;
	JButton						jBrowseFolder		= null;

	public Convert() throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(WIDTH, HEIGHT);
		setLocation((screenSize.width - WIDTH) / 2, (screenSize.height - HEIGHT) / 2);

		setContentPane(getJContentPane());
		setTitle("Нефть");
		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter()
		{
			public void windowClosing(WindowEvent e)
			{
				System.exit(0);
			}
		});

		this.setVisible(true);
	}

	private JPanel getJContentPane()
	{

		JPanel jContentPane = new JPanel();
		jContentPane.setLayout(new GridLayout(5, 1));
		jContentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		jFilePath = new JTextField();
		jFolderPath = new JTextField();

		jFilePath.setEditable(false);
		jFolderPath.setEditable(false);

		JButton jConvert = new JButton();
		jConvert.setText("Преобразовать");
		jConvert.setFont(new Font("Dialog", Font.BOLD, 10));

		jConvert.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (pathFile.equals("") && pathFolder.equals(""))
				{
					JOptionPane.showMessageDialog(null, "Данные введены некорректно");
				}
				else
				{
					convertClients();
					converRegion();
					convertProviders();
				}
			}
		});

		jContentPane.add(getFilePanel(), null);
		jContentPane.add(jFilePath, null);
		jContentPane.add(getFolderPanel(), null);
		jContentPane.add(jFolderPath, null);
		jContentPane.add(jConvert, null);

		return jContentPane;
	}

	/**
	 * панель выбора файла
	 * 
	 * @return jFilePanel
	 */
	private JPanel getFilePanel()
	{
		JPanel jFilePanel = new JPanel();

		jFilePanel.setLayout(new GridLayout(1, 2));

		JLabel jLabelFile = new JLabel();
		jLabelFile.setText("Путь к Excel файлу:");

		jFilePanel.add(jLabelFile, null);
		jFilePanel.add(getBrowseFile(), null);

		return jFilePanel;
	}

	/**
	 * панель выбора папки
	 * 
	 * @return jFolderPanel
	 */
	private JPanel getFolderPanel()
	{
		JPanel jFolderPanel = new JPanel();

		jFolderPanel.setLayout(new GridLayout(1, 2));

		JLabel jLabelFolder = new JLabel();
		jLabelFolder.setText("Куда будет выгружены файлы:");

		jFolderPanel.add(jLabelFolder, null);
		jFolderPanel.add(getBrowseFolder(), null);

		return jFolderPanel;
	}

	/**
	 * инициализация кнопки выбора пути файла
	 * 
	 * @return jBrowseFile
	 */
	private JButton getBrowseFile()
	{
		jBrowseFile = new JButton();
		jBrowseFile.setText("Обзор");
		jBrowseFile.setFont(new Font("Dialog", Font.BOLD, 10));

		jBrowseFile.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Создание диалога выбора файла
				JFileChooser fileChooser = new JFileChooser();
				// Добавление фильтра в диалог выбора файла
				fileChooser.setFileFilter(new TxtFilter());

				fileChooser.showOpenDialog(jBrowseFile);

				File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile != null)
				{
					// путь записывает в
					jFilePath.setText(selectedFile.getAbsolutePath());
					setPathFile(selectedFile.getAbsolutePath());
				}
			}

			// Класс, фильтрующий текстовые файлы
			class TxtFilter extends javax.swing.filechooser.FileFilter
			{
				public String getDescription()
				{
					return "*.xls";
				}

				public boolean accept(File f)
				{
					String filename = f.getName();
					return f.isDirectory() || filename.endsWith(".xls");
				}
			}
		});

		return jBrowseFile;
	}

	/**
	 * инициализация кнопки выбора пути к папке
	 * 
	 * @return jBrowseFolder
	 */
	private JButton getBrowseFolder()
	{
		jBrowseFolder = new JButton();
		jBrowseFolder.setText("Обзор");
		jBrowseFolder.setFont(new Font("Dialog", Font.BOLD, 10));

		jBrowseFolder.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				// Создание диалога выбора файла
				JFileChooser fileChooser = new JFileChooser();
				// Добавление фильтра в диалог выбора файла
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

				fileChooser.showOpenDialog(jBrowseFolder);
				File selectedFile = fileChooser.getSelectedFile();
				if (selectedFile != null)
				{
					// путь записывает в
					jFolderPath.setText(selectedFile.getAbsolutePath());
					pathFolder = selectedFile.getAbsolutePath() + "/";
				}
			}
		});

		return jBrowseFolder;
	}

	private static void convertClients()
	{
		File file = new File(getPathFile());
		try
		{
			WorkbookSettings ws = new WorkbookSettings();
			ws.setLocale(new Locale("ru", "RU"));
			Workbook workbook = Workbook.getWorkbook(file);

			Sheet sheet = workbook.getSheet("заказчики");
			Cell cell = null;

			// Создаём хмл файл
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			doc.setXmlVersion("1.0");
			doc.setXmlStandalone(true);

			int id_district = 0; // счетчик айди района
			int id_city = 0; // счетчик айди города по районам

			// Создаём тег услуги, сод. районы
			Element service = doc.createElement("service");
			// Добавляем атрибут: название услуги
			service.setAttribute("name", "Заказчики");

			// определяем стартовую позицию
			int beginList = searchBegion(sheet);

			// определяем конечную позицию
			int endList = searchEnd(sheet);

			Element district = doc.createElement("district");
			Element city = doc.createElement("city");
			Element provider = doc.createElement("provider");

			// начинаем поиск
			for (int i = beginList; i < endList; i++)
			{

				// название района\посёлка
				cell = sheet.getCell("B" + Integer.toString(i));
				if (cell.isHidden() != true)
				{
					// Определяем строку документов: если пустая строка, то
					// нор. документ не изменился ...
					// ... относительно прошлого поставщика(строки)

					cell = sheet.getCell("B" + Integer.toString(i));
					if (cell.getContents().equals("") == false)
					{
						// проверяем на район
						cell = sheet.getCell("B" + Integer.toString(i));
						if (cell.getContents().indexOf("Муницип") != -1)
						{
							// определили как район
							// Если это посёлок
							if (id_district > 0)
							{
								if (id_city > 0)
								{
									district.appendChild(city);
								}

								// зыкрываем тег </district>
								service.appendChild(district);
							}

							district = doc.createElement("district");

							// индификационный номер района;
							district.setAttribute("id", sheet.getCell("C" + Integer.toString(i)).getContents());

							id_district++; // новый район, новый айди
							id_city = 0; // обнуляем счетчик айди городов
						}
						else
						{
							if (id_city > 0)
							{
								district.appendChild(city);
							}
							// Создаём тег города
							city = doc.createElement("city");

							// Присваиваим индификационный номер
							city.setAttribute("id", sheet.getCell("C" + Integer.toString(i)).getContents());

							// увелич порядковый номер на 1
							id_city++;

							// Создаём тег поставщика
							provider = doc.createElement("provider");

							provider.setAttribute("name", sheet.getCell("D" + Integer.toString(i)).getContents());
							provider.setAttribute("address", sheet.getCell("E" + Integer.toString(i)).getContents());

							city.appendChild(provider);
						}
					}
					else
					{
						// Создаём тег поставщика
						provider = doc.createElement("provider");

						provider.setAttribute("name", sheet.getCell("D" + Integer.toString(i)).getContents());
						provider.setAttribute("address", sheet.getCell("E" + Integer.toString(i)).getContents());

						city.appendChild(provider);
					}
				}
			}

			if (id_city > 0)
			{
				district.appendChild(city);
			}
			service.appendChild(district);

			doc.appendChild(service);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			Source s = new DOMSource(doc);
			Result res = new StreamResult(new FileOutputStream(pathFolder + "clients.xml"));
			transformer.transform(s, res);

		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
		catch (BiffException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void convertProviders()
	{
		File file = new File(getPathFile());
		try
		{
			WorkbookSettings ws = new WorkbookSettings();
			ws.setLocale(new Locale("ru", "RU"));
			Workbook workbook = Workbook.getWorkbook(file);

			Sheet sheet = workbook.getSheet(2);

			// Создаём хмл файл
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			doc.setXmlVersion("1.0");
			doc.setXmlStandalone(true);

			int id_district = 0; // счетчик айди района
			int id_city = 0; // счетчик айди города по районам

			// Создаём тег услуги, сод. районы
			Element service = doc.createElement("service");
			// Добавляем атрибут: название услуги
			service.setAttribute("name", "Поставщики");

			// определяем стартовую позицию
			int beginList = searchBegion(sheet);

			// определяем конечную позицию
			int endList = searchEnd(sheet);

			Element district = doc.createElement("district");
			Element city = doc.createElement("city");
			Element provider = doc.createElement("provider");

			// начинаем поиск
			Next:
			for (int i = beginList; i < endList; i++)
			{
				// название района\посёлка
				if (sheet.getCell("B" + Integer.toString(i)).isHidden() != true)
				{
					// Определяем строку документов: если пустая строка, то
					// нор. документ не изменился ...
					// ... относительно прошлого поставщика(строки)
					// проверяем на район

					if (sheet.getCell("B" + Integer.toString(i)).getContents().indexOf("Муницип") != -1)
					{
						// определили как район
						// Если это посёлок
						if (id_district > 0)
						{
							if (id_city > 0)
								district.appendChild(city);

							// зыкрываем тег </district>
							service.appendChild(district);
						}

						district = doc.createElement("district");

						// индификационный номер района;
						district.setAttribute("id", sheet.getCell("C" + Integer.toString(i)).getContents());

						id_district++; // новый район, новый айди
						id_city = 0; // обнуляем счетчик айди городов
					}
					else
					{
						for (int p = 0; p < district.getChildNodes().getLength(); p++)
						{
							if (district.getChildNodes().item(p).getAttributes().getNamedItem("id").getNodeValue().equals(sheet.getCell("C" + Integer.toString(i)).getContents()))
							{
								// Создаём тег поставщика
								provider = doc.createElement("provider");

								provider.setAttribute("name", sheet.getCell("D" + Integer.toString(i)).getContents());
								provider.setAttribute("address", sheet.getCell("E" + Integer.toString(i)).getContents());
								provider.setAttribute("web", sheet.getCell("G" + Integer.toString(i)).getContents());
								provider.setAttribute("b80", sheet.getCell("H" + Integer.toString(i)).getContents());
								provider.setAttribute("b92", sheet.getCell("I" + Integer.toString(i)).getContents());
								provider.setAttribute("b95", sheet.getCell("J" + Integer.toString(i)).getContents());
								provider.setAttribute("bdis", sheet.getCell("K" + Integer.toString(i)).getContents());

								district.getChildNodes().item(p).appendChild(provider);
								continue Next;
							}
						}

						// Создаём тег города
						city = doc.createElement("city");

						// Присваиваим индификационный номер
						city.setAttribute("id", sheet.getCell("C" + Integer.toString(i)).getContents());

						// увелич порядковый номер на 1
						id_city++;

						// Создаём тег поставщика
						provider = doc.createElement("provider");

						provider.setAttribute("name", sheet.getCell("D" + Integer.toString(i)).getContents());
						provider.setAttribute("address", sheet.getCell("E" + Integer.toString(i)).getContents());
						provider.setAttribute("web", sheet.getCell("G" + Integer.toString(i)).getContents());
						provider.setAttribute("b80", sheet.getCell("H" + Integer.toString(i)).getContents());
						provider.setAttribute("b92", sheet.getCell("I" + Integer.toString(i)).getContents());
						provider.setAttribute("b95", sheet.getCell("J" + Integer.toString(i)).getContents());
						provider.setAttribute("bdis", sheet.getCell("K" + Integer.toString(i)).getContents());

						city.appendChild(provider);

						district.appendChild(city);
					}
				}
			}

			if (id_city > 0)
				district.appendChild(city);

			service.appendChild(district);

			doc.appendChild(service);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			Source s = new DOMSource(doc);
			Result res = new StreamResult(new FileOutputStream(pathFolder + "providers.xml"));
			transformer.transform(s, res);

		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
		catch (BiffException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private static void converRegion()
	{
		File file = new File(getPathFile());
		try
		{
			WorkbookSettings ws = new WorkbookSettings();
			ws.setLocale(new Locale("ru", "RU"));
			Workbook workbook = Workbook.getWorkbook(file);

			Sheet sheet = workbook.getSheet("регион");
			Cell cell = null;

			// Создаём хмл файл
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();

			Document doc = builder.newDocument();

			doc.setXmlVersion("1.0");
			doc.setXmlStandalone(true);

			int id_district = 0; // счетчик айди района
			int id_city = 0; // счетчик айди города по районам

			// Создаём тег услуги, сод. районы
			Element region = doc.createElement("region");
			// Добавляем атрибут: название услуги
			region.setAttribute("name", "Калужская область");

			// определяем стартовую позицию
			int beginList = searchBegion(sheet);

			// определяем конечную позицию
			int endList = searchEnd(sheet);

			Element district = doc.createElement("district");
			Element city = doc.createElement("city");

			// начинаем поиск
			for (int i = beginList; i < endList; i++)
			{
				// название района\посёлка
				cell = sheet.getCell("B" + Integer.toString(i));
				if (cell.isHidden() != true)
				{
					if (cell.getContents() != null)
					{
						// проверяем на район
						if (cell.getContents().indexOf("Муницип") != -1)
						{
							// определили как район
							// Если это посёлок
							if (id_district > 0)
							{
								if (id_city > 0)
								{
									district.appendChild(city);
								}

								// зыкрываем тег </district>
								region.appendChild(district);
							}

							id_city = 0;

							district = doc.createElement("district");

							// индификационный номер района;
							district.setAttribute("id", sheet.getCell("C" + Integer.toString(i)).getContents());

							// cell = sheet.getCell("D" + Integer.toString(i));
							district.setAttribute("main_id", "0");

							district.setAttribute("name", sheet.getCell("D" + Integer.toString(i)).getContents());

							district.setAttribute("fio", sheet.getCell("G" + Integer.toString(i)).getContents());

							district.setAttribute("fio_tel", sheet.getCell("H" + Integer.toString(i)).getContents());

							district.setAttribute("zam", sheet.getCell("I" + Integer.toString(i)).getContents());

							district.setAttribute("zam_tel", sheet.getCell("J" + Integer.toString(i)).getContents());

							id_district++; // новый район, новый айди
						}
						else
						{
							if (id_city > 0)
							{
								district.appendChild(city);
							}
							// Создаём тег города
							city = doc.createElement("city");

							// Присваиваим индификационный номер
							city.setAttribute("id", sheet.getCell("C" + Integer.toString(i)).getContents());

							city.setAttribute("name", sheet.getCell("D" + Integer.toString(i)).getContents());

							city.setAttribute("x", sheet.getCell("E" + Integer.toString(i)).getContents());
							city.setAttribute("y", sheet.getCell("F" + Integer.toString(i)).getContents());

							// увелич порядковый номер на 1
							id_city++;
						}
					}
				}
			}

			if (id_city > 0)
			{
				district.appendChild(city);
			}
			region.appendChild(district);

			doc.appendChild(region);
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();

			Source s = new DOMSource(doc);
			Result res = new StreamResult(new FileOutputStream(pathFolder + "regions.xml"));
			transformer.transform(s, res);
			res = null;

		}
		catch (ParserConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (TransformerConfigurationException e)
		{
			e.printStackTrace();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (TransformerException e)
		{
			e.printStackTrace();
		}
		catch (BiffException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public static void convertdis()
	{
		File file = new File("1.xls");
		Vector<Vector<String>> xls = new Vector<Vector<String>>();

		try
		{
			WorkbookSettings ws = new WorkbookSettings();
			ws.setLocale(new Locale("ru", "RU"));
			Workbook workbook = Workbook.getWorkbook(file);
			Sheet oldSheet = workbook.getSheet("Лист1");

			WritableWorkbook workbook2 = Workbook.createWorkbook(new File("2.xls"), ws);

			WritableSheet newSheet = workbook2.createSheet("k", 0);

			// определяем стартовую позицию
			int beginList = 3;

			// определяем конечную позицию
			int endList = 88;

			// начинаем поиск
			for (int i = beginList; i < endList; i = i + 3)
			{
				Vector<String> el = new Vector<String>();

				System.out.print((i + 1) + ":  " + oldSheet.getCell("A" + Integer.toString(i + 1)).getContents() + "    ");
				System.out.println(oldSheet.getCell("C" + Integer.toString(i + 2)).getContents());

				el.add(oldSheet.getCell("A" + Integer.toString(i + 1)).getContents().toString());
				el.add(oldSheet.getCell("C" + Integer.toString(i + 2)).getContents().toString());

				xls.add(el);
			}

			/*
			 * Основной формат ячеек
			 * Tahoma 9pt, no bold
			 * выравнивание по горизонтале: центр
			 * выравнивание по вертикале: центр
			 * перенос по словам
			 * стиль границы - все
			 * цвет фона - без цвета
			 */
			WritableCellFormat tahoma9pt = new WritableCellFormat(new WritableFont(WritableFont.TAHOMA, 9, WritableFont.NO_BOLD));
			tahoma9pt.setAlignment(Alignment.CENTRE);
			tahoma9pt.setVerticalAlignment(VerticalAlignment.CENTRE);
			tahoma9pt.setWrap(true);
			tahoma9pt.setBorder(Border.ALL, BorderLineStyle.MEDIUM);

			for (int i = 0; i < xls.size(); i++)
			{
				newSheet.addCell(new Label(0, i, xls.get(i).get(0), tahoma9pt));
				newSheet.addCell(new Label(1, i, xls.get(i).get(1), tahoma9pt));
			}

			workbook2.write();
			workbook2.close();
		}
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		}
		catch (BiffException e)
		{
			e.printStackTrace();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		catch (WriteException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws UnsupportedLookAndFeelException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws ClassNotFoundException 
	 */
	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException
	{
		// TODO Auto-generated method stub
		// convertClients();
		// converRegion();
		// convertProviders();
		// convertdis();

		new Convert();
	}

	/**
	 * Определяет строку с первым районом<br>
	 * Запись: "Муниципальный район "Бабынинский район""
	 * 
	 * @param _sheet
	 *            - Лист документа
	 * @return индекс строки
	 */
	private static int searchBegion(Sheet _sheet)
	{
		// определяем стартовую позицию
		for (int i = 1; i <= _sheet.getRows(); i++)
		{
			Cell cell = _sheet.getCell("B" + Integer.toString(i));
			if (cell.getContents().equals("Муниципальный район \"Бабынинский район\""))
			{
				return i;
			}
		}
		return -1;
	}

	/**
	 * Определяет строку с конечной записью<br>
	 * Запись: "$EndList"
	 * 
	 * @param _sheet
	 *            - Лист документа
	 * @return индекс строки
	 */
	private static int searchEnd(Sheet _sheet)
	{
		for (int i = 1; i <= _sheet.getRows(); i++)
		{
			Cell cell = _sheet.getCell("B" + Integer.toString(i));
			if (cell.getContents().equals("$EndList"))
			{
				return i;
			}
		}
		return -1;
	}

	public static String getPathFile()
	{
		return pathFile;
	}

	public static void setPathFile(String pathFile)
	{
		Convert.pathFile = pathFile;
	}
}
