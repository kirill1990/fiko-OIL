package ru.fiko.oil.supp;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import ru.fiko.oil.main.Oil;
import ru.fiko.oil.panels.Stations;

public class ItemStation extends JPanel
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1897101076822595088L;

	private String				stationId;
	private String				date;
	private String				status;
	private String				title;
	private String				address;
	private String				b80;
	private String				b92;
	private String				b95;
	private String				bdis;
	private String				changeId			= "";

	private JLabel				label_date;

	private JTextField			label_b80;

	private JTextField			label_b92;

	private JTextField			label_b95;

	private JTextField			label_bdis;


	public void initialization()
	{
		this.setLayout(new BorderLayout(5, 5));
		this.setBorder(BorderFactory.createTitledBorder(null, title, TitledBorder.CENTER, TitledBorder.TOP, getFont(), Color.BLACK));
		//this.setBackground(Color.BLACK);
		
		
		JPanel panel = new JPanel(new BorderLayout());
		this.add(panel, BorderLayout.WEST);

		label_date = new JLabel(date);
		panel.add(label_date, BorderLayout.NORTH);
		if (status.equals("true"))
			panel.add(new JLabel("Активен", JLabel.CENTER), BorderLayout.SOUTH);
		else
			panel.add(new JLabel("Не активен", JLabel.CENTER), BorderLayout.SOUTH);

		JTextField address = new JTextField(this.address);
		address.setEditable(false);
		address.addMouseListener(new ads());
		this.add(address, BorderLayout.CENTER);

		JPanel toplivo = new JPanel(new GridLayout(2, 4));
		toplivo.setBorder(BorderFactory.createEtchedBorder());
		toplivo.setPreferredSize(new Dimension(200, 50));
		this.add(toplivo, BorderLayout.EAST);

		this.setMaximumSize(new Dimension(1500, 75));
		toplivo.add(new JLabel("80", JLabel.CENTER));
		toplivo.add(new JLabel("92", JLabel.CENTER));
		toplivo.add(new JLabel("95", JLabel.CENTER));
		toplivo.add(new JLabel("ДТ", JLabel.CENTER));

		label_b80 = new JTextField(b80);
		label_b92 = new JTextField(b92);
		label_b95 = new JTextField(b95);
		label_bdis = new JTextField(bdis);

		label_b80.setName("b80");
		label_b92.setName("b92");
		label_b95.setName("b95");
		label_bdis.setName("bdis");

		label_b80.addKeyListener(new mmm());
		label_b92.addKeyListener(new mmm());
		label_b95.addKeyListener(new mmm());
		label_bdis.addKeyListener(new mmm());
		
		label_b80.addMouseListener(new ads());
		label_b92.addMouseListener(new ads());
		label_b95.addMouseListener(new ads());
		label_bdis.addMouseListener(new ads());

		toplivo.add(label_b80);
		toplivo.add(label_b92);
		toplivo.add(label_b95);
		toplivo.add(label_bdis);
		
		
		this.addMouseListener(new ads());

	}
	
	private class ads extends MouseAdapter
	{
		//TODO refactor
		@Override
		public void mouseClicked(MouseEvent e)
		{
//			if(stations.current!=null)
//				stations.current.setColor(new Color(242,241,240));
//			
//			stations.current = aaa;
//			setColor(new Color(115,171,255));
		}
	}
	

	private class mmm extends KeyAdapter
	{
		//TODO refactor
		public void keyReleased(KeyEvent e)
		{
			JTextField target = ((JTextField) e.getComponent());

			if (target.getText().length() > 5)
			{
				target.setText(target.getText().substring(0, 5));
			}
			label_date.getText();
			
			try
			{

				if (b80.equals(label_b80.getText()) && b92.equals(label_b92.getText()) && b95.equals(label_b95.getText()) && bdis.equals(label_bdis.getText()))
				{
					// удалить
					DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("DELETE FROM change WHERE id = '" + changeId + "';");
					
					label_date.setText(date);
					label_date.setForeground(new Color(76,76,76));					
				}
				else
				{
					// добавление новой записи

					SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
					Date temp_date = new Date(System.currentTimeMillis());

					String current_date = dateFormat.format(temp_date).toString();

					if (current_date.equals(label_date.getText()))
					{
						// update
						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET b80 = '" + label_b80.getText() + "' WHERE id LIKE '" + changeId + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET b92 = '" + label_b92.getText() + "' WHERE id LIKE '" + changeId + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET b95 = '" + label_b95.getText() + "' WHERE id LIKE '" + changeId + "';");

						DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeUpdate("UPDATE change SET bdis = '" + label_bdis.getText() + "' WHERE id LIKE '" + changeId + "';");
					}
					else
					{
						// new records
						PreparedStatement pst = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).prepareStatement("INSERT INTO change VALUES (?, ?, ?, ?, ?, ?, ?);");

						pst.setInt(2, Integer.parseInt(stationId));

						pst.setString(3, Long.toString(System.currentTimeMillis()));

						pst.setString(4, label_b80.getText());
						pst.setString(5, label_b92.getText());
						pst.setString(6, label_b95.getText());
						pst.setString(7, label_bdis.getText());

						pst.addBatch();

						pst.executeBatch();
						pst.close();
						
						ResultSet rs = DriverManager.getConnection("jdbc:sqlite:" + Oil.PATH).createStatement().executeQuery("select seq from sqlite_sequence where name like 'change';");
						while (rs.next())
						{
							changeId = rs.getString(1);
						}
						rs.close();
					}
					label_date.setText(current_date);
					label_date.setForeground(Color.red);
				}
			}
			catch (SQLException e1)
			{
				e1.printStackTrace();
			}
		}
	}

	public String getStationId()
	{
		return stationId;
	}

	public void setStationId(String id)
	{
		this.stationId = id;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}

	public String getStatus()
	{
		return status;
	}

	public void setStatus(String status)
	{
		this.status = status;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getAddress()
	{
		return address;
	}

	public void setAddress(String address)
	{
		this.address = address;
	}

	public String getB80()
	{
		return b80;
	}

	public void setB80(String b80)
	{
		this.b80 = b80;
	}

	public String getB92()
	{
		return b92;
	}

	public void setB92(String b92)
	{
		this.b92 = b92;
	}

	public String getB95()
	{
		return b95;
	}

	public void setB95(String b95)
	{
		this.b95 = b95;
	}

	public String getBdis()
	{
		return bdis;
	}

	public void setBdis(String bdis)
	{
		this.bdis = bdis;
	}

	public String getChangeId()
	{
		return changeId;
	}

	public void setChangeId(String changeId)
	{
		this.changeId = changeId;
	}
}
