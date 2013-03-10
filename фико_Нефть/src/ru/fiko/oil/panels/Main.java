package ru.fiko.oil.panels;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

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

	public Main() throws ClassNotFoundException, SQLException
	{
		Class.forName("org.sqlite.JDBC");

		this.setLayout(new BorderLayout(5, 5));
		this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		
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
		}
		rs.close();
		
		
		JPanel panel = new JPanel(new GridLayout(13,1));
		this.add(panel, BorderLayout.NORTH);
		panel.add(texturl);
		panel.add(url);
		panel.add(text);
		panel.add(bname);
		panel.add(baddress);
		panel.add(b80);
		panel.add(b92);
		panel.add(b95);
		panel.add(bdis);
		panel.add(binfo);
		panel.add(orgname);
		panel.add(orgaddress);
		
		JButton btn = new JButton("Обновить");
		panel.add(btn);
		
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
				}
				catch (SQLException e1)
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}
		});

	}
}
