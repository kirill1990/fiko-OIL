package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import ru.fiko.oil.main.Oil;

public class Main extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 410780410126105826L;
	private JTextField			texturl;
	private JTextField			url;
	private JTextField			text;
	private JTextField			bname;
	private JTextField			baddress;
	private JTextField			b80;
	private JTextField			b92;
	private JTextField			b95;
	private JTextField			bdis;
	private JTextField			binfo;
	private JTextField			orgname;
	private JTextField			orgaddress;
	private JButton	btn;

	public Main() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");

		this.setLayout(new BorderLayout(5, 5));
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JPanel panel = new JPanel(new GridLayout(17,1));
		panel.setPreferredSize(new Dimension(500, 550));
		
		ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("SELECT * FROM main WHERE id LIKE '1';");
		if (rs.next())
		{

			texturl = new JTextField(rs.getString("texturl"));
			url = new JTextField(rs.getString("url"));

			text = new JTextField(rs.getString("text"));
			bname = new JTextField(rs.getString("bname"));
			baddress = new JTextField(rs.getString("baddress"));
			b80 = new JTextField(rs.getString("b80"));
			b92 = new JTextField(rs.getString("b92"));
			b95 = new JTextField(rs.getString("b95"));
			bdis = new JTextField(rs.getString("bdis"));
			binfo = new JTextField(rs.getString("binfo"));
			
			orgname = new JTextField(rs.getString("orgname"));
			orgaddress = new JTextField(rs.getString("orgaddress"));
		

		
		texturl.getDocument().addDocumentListener(new SearchDocumentListener());
		url.getDocument().addDocumentListener(new SearchDocumentListener());

		text.getDocument().addDocumentListener(new SearchDocumentListener());
		bname.getDocument().addDocumentListener(new SearchDocumentListener());
		baddress.getDocument().addDocumentListener(new SearchDocumentListener());
		b80.getDocument().addDocumentListener(new SearchDocumentListener());
		b92.getDocument().addDocumentListener(new SearchDocumentListener());
		b95.getDocument().addDocumentListener(new SearchDocumentListener());
		bdis.getDocument().addDocumentListener(new SearchDocumentListener());
		binfo.getDocument().addDocumentListener(new SearchDocumentListener());
		JScrollPane aa = new JScrollPane(panel);
		aa.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.add(aa, BorderLayout.CENTER);
		
		btn = new JButton("Обновить");
		btn.setEnabled(false);
		panel.add(btn);
		panel.add(new JLabel("Методичка"));
		panel.add(texturl);
		panel.add(url);
		panel.add(new JLabel("Справочная информация"));
		panel.add(text);
		panel.add(new JLabel("в таблице АЗС"));
		panel.add(bname);
		panel.add(baddress);
		panel.add(b80);
		panel.add(b92);
		panel.add(b95);
		panel.add(bdis);
		panel.add(binfo);
		panel.add(new JLabel("в таблице  Заказчики"));
		panel.add(orgname);
		panel.add(orgaddress);
		

		}
		rs.close();
	
		
		btn.addActionListener(new ActionListener()
		{
			
			@Override
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET texturl = '" + texturl.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET url = '" + url.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET text = '" + text.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET bname = '" + bname.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET baddress = '" + baddress.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET b80 = '" + b80.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET b92 = '" + b92.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET b95 = '" + b95.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET bdis = '" + bdis.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET binfo = '" + binfo.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET orgname = '" + orgname.getText() + "' WHERE id LIKE '1';");
					
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE main SET orgaddress = '" + orgaddress.getText() + "' WHERE id LIKE '1';");
					btn.setEnabled(false);
				}
				catch (SQLException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});
	}
	
	public class SearchDocumentListener implements DocumentListener
	{
		public void changedUpdate(DocumentEvent e)
		{
			updateSearchString();
		}

		public void removeUpdate(DocumentEvent e)
		{
			updateSearchString();
		}

		public void insertUpdate(DocumentEvent e)
		{
			updateSearchString();
		}

		public void updateSearchString()
		{
			btn.setEnabled(true);
		}
	}
}
