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
import ru.fiko.oil.panels.Optov;

/**
 * Это был пиздец, выживал как мог!
 * 
 * @author kirill
 * 
 */
public class ItemOptov extends JPanel {

    /**
     * 
     */
    private static final long serialVersionUID = 1897101076822595088L;

    private String commnId;
    private String date;
    private String status;
    private String title;

    private String b80_t = "";
    private String b92_t = "";
    private String b95_t = "";
    private String bdis_mc_t = "";
    private String bdis_winter_t = "";
    private String bdis_leto1_t = "";
    private String bdis_leto2_t = "";

    private String b80_l = "";
    private String b92_l = "";
    private String b95_l = "";
    private String bdis_mc_l = "";
    private String bdis_winter_l = "";
    private String bdis_leto1_l = "";
    private String bdis_leto2_l = "";

    private String changeId = "";

    private JLabel label_date;

    private JTextField label_b80_t;
    private JTextField label_b92_t;
    private JTextField label_b95_t;
    private JTextField label_bdis_mc_t;
    private JTextField label_bdis_winter_t;
    private JTextField label_bdis_leto1_t;
    private JTextField label_bdis_leto2_t;

    private JTextField label_b80_l;
    private JTextField label_b92_l;
    private JTextField label_b95_l;
    private JTextField label_bdis_mc_l;
    private JTextField label_bdis_winter_l;
    private JTextField label_bdis_leto1_l;
    private JTextField label_bdis_leto2_l;

    private ItemOptov aaa;

    private Optov stations;

    public void initialization() throws SQLException {
	
	 ResultSet value = DriverManager
		    .getConnection("jdbc:sqlite:" + Oil.PATH)
		    .createStatement()
		    .executeQuery(
			    "SELECT * FROM commercial WHERE id LIKE '"
				    + commnId + "';");
	 if(value.next())
	     title = value.getString("title");
	
	this.setLayout(new BorderLayout(5, 5));
	this.setBorder(BorderFactory.createTitledBorder(null, title,
		TitledBorder.CENTER, TitledBorder.TOP, getFont(), Color.BLACK));
	// this.setBackground(Color.BLACK);
	this.setMaximumSize(new Dimension(1500, 130));

	JPanel panel = new JPanel(new BorderLayout());
	this.add(panel, BorderLayout.WEST);

	label_date = new JLabel(date);
	panel.add(label_date, BorderLayout.CENTER);

	// JTextField address = new JTextField(this.address);
	// address.setEditable(false);
	// address.addMouseListener(new ads());
	// this.add(address, BorderLayout.CENTER);

	JPanel toplivo = new JPanel(new GridLayout(2, 1));
	this.add(toplivo, BorderLayout.CENTER);

	label_b80_t = new JTextField(b80_t);
	label_b92_t = new JTextField(b92_t);
	label_b95_t = new JTextField(b95_t);
	label_bdis_mc_t = new JTextField(bdis_mc_t);
	label_bdis_winter_t = new JTextField(bdis_winter_t);
	label_bdis_leto1_t = new JTextField(bdis_leto1_t);
	label_bdis_leto2_t = new JTextField(bdis_leto2_t);

	label_b80_l = new JTextField(b80_l);
	label_b92_l = new JTextField(b92_l);
	label_b95_l = new JTextField(b95_l);
	label_bdis_mc_l = new JTextField(bdis_mc_l);
	label_bdis_winter_l = new JTextField(bdis_winter_l);
	label_bdis_leto1_l = new JTextField(bdis_leto1_l);
	label_bdis_leto2_l = new JTextField(bdis_leto2_l);

	label_b80_t.addMouseListener(new ads());
	label_b92_t.addMouseListener(new ads());
	label_b95_t.addMouseListener(new ads());
	label_bdis_mc_t.addMouseListener(new ads());
	label_bdis_winter_t.addMouseListener(new ads());
	label_bdis_leto1_t.addMouseListener(new ads());
	label_bdis_leto2_t.addMouseListener(new ads());

	label_b80_l.addMouseListener(new ads());
	label_b92_l.addMouseListener(new ads());
	label_b95_l.addMouseListener(new ads());
	label_bdis_mc_l.addMouseListener(new ads());
	label_bdis_winter_l.addMouseListener(new ads());
	label_bdis_leto1_l.addMouseListener(new ads());
	label_bdis_leto2_l.addMouseListener(new ads());

	label_b80_t.addKeyListener(new mmm());
	label_b92_t.addKeyListener(new mmm());
	label_b95_t.addKeyListener(new mmm());
	label_bdis_mc_t.addKeyListener(new mmm());
	label_bdis_winter_t.addKeyListener(new mmm());
	label_bdis_leto1_t.addKeyListener(new mmm());
	label_bdis_leto2_t.addKeyListener(new mmm());

	label_b80_l.addKeyListener(new mmm());
	label_b92_l.addKeyListener(new mmm());
	label_b95_l.addKeyListener(new mmm());
	label_bdis_mc_l.addKeyListener(new mmm());
	label_bdis_winter_l.addKeyListener(new mmm());
	label_bdis_leto1_l.addKeyListener(new mmm());
	label_bdis_leto2_l.addKeyListener(new mmm());

	JPanel toplivo1 = new JPanel(new GridLayout(2, 7));
	toplivo1.setBorder(BorderFactory.createEtchedBorder());
	toplivo1.setPreferredSize(new Dimension(800, 50));

	toplivo1.add(new JLabel("АИ 80, руб\\т", JLabel.CENTER));
	toplivo1.add(new JLabel("АИ 92, руб\\т", JLabel.CENTER));
	toplivo1.add(new JLabel("АИ 95, руб\\т", JLabel.CENTER));
	toplivo1.add(new JLabel("ДТ\\м-сез", JLabel.CENTER));
	toplivo1.add(new JLabel("ДТ\\зим", JLabel.CENTER));
	toplivo1.add(new JLabel("ДТ\\лето1", JLabel.CENTER));
	toplivo1.add(new JLabel("ДТ\\лето2", JLabel.CENTER));

	toplivo1.add(label_b80_t);
	toplivo1.add(label_b92_t);
	toplivo1.add(label_b95_t);
	toplivo1.add(label_bdis_mc_t);
	toplivo1.add(label_bdis_winter_t);
	toplivo1.add(label_bdis_leto1_t);
	toplivo1.add(label_bdis_leto2_t);

	JPanel toplivo2 = new JPanel(new GridLayout(2, 7));
	toplivo2.setBorder(BorderFactory.createEtchedBorder());
	toplivo2.setPreferredSize(new Dimension(600, 50));

	toplivo2.add(new JLabel("АИ 80, руб\\л", JLabel.CENTER));
	toplivo2.add(new JLabel("АИ 92, руб\\л", JLabel.CENTER));
	toplivo2.add(new JLabel("АИ 95, руб\\л", JLabel.CENTER));
	toplivo2.add(new JLabel("ДТ\\м-сез", JLabel.CENTER));
	toplivo2.add(new JLabel("ДТ\\зим", JLabel.CENTER));
	toplivo2.add(new JLabel("ДТ\\лето1", JLabel.CENTER));
	toplivo2.add(new JLabel("ДТ\\лето2", JLabel.CENTER));

	toplivo2.add(label_b80_l);
	toplivo2.add(label_b92_l);
	toplivo2.add(label_b95_l);
	toplivo2.add(label_bdis_mc_l);
	toplivo2.add(label_bdis_winter_l);
	toplivo2.add(label_bdis_leto1_l);
	toplivo2.add(label_bdis_leto2_l);

	toplivo.add(toplivo1);
	toplivo.add(toplivo2);

	aaa = this;
	this.addMouseListener(new ads());

    }

    private class ads extends MouseAdapter {
	// TODO refactor
	@Override
	public void mouseClicked(MouseEvent e) {
	    if (stations.current != null)
		stations.current.setColor(new Color(242, 241, 240));

	    stations.current = aaa;
	    setColor(new Color(115, 171, 255));
	}
    }

    private void setColor(Color color) {
	// TODO comments
	this.setBackground(color);
    }

    private class mmm extends KeyAdapter {
	// TODO refactor
	public void keyReleased(KeyEvent e) {
	    JTextField target = ((JTextField) e.getComponent());

	    if (target.getText().length() > 7) {
		target.setText(target.getText().substring(0, 7));
	    }
	    label_date.getText();

	    try {

		if (b80_l.equals(label_b80_l.getText())
			&& b92_l.equals(label_b92_l.getText())
			&& b95_l.equals(label_b95_l.getText())
			&& bdis_leto1_l.equals(label_bdis_leto1_l.getText())
			&& bdis_leto2_l.equals(label_bdis_leto2_l.getText())
			&& bdis_mc_l.equals(label_bdis_mc_l.getText())
			&& bdis_winter_l.equals(label_bdis_winter_l.getText())
			&& b80_t.equals(label_b80_t.getText())
			&& b92_t.equals(label_b92_t.getText())
			&& b95_t.equals(label_b95_t.getText())
			&& bdis_leto1_t.equals(label_bdis_leto1_t.getText())
			&& bdis_leto2_t.equals(label_bdis_leto2_t.getText())
			&& bdis_mc_t.equals(label_bdis_mc_t.getText())
			&& bdis_winter_t.equals(label_bdis_winter_t.getText())

		) {
		    // удалить
		    DriverManager
			    .getConnection("jdbc:sqlite:" + Oil.PATH)
			    .createStatement()
			    .executeUpdate(
				    "DELETE FROM optov WHERE id = '" + changeId
					    + "';");

		    label_date.setText(date);
		    label_date.setForeground(new Color(76, 76, 76));
		} else {
		    // добавление новой записи

		    SimpleDateFormat dateFormat = new SimpleDateFormat(
			    "dd.MM.yyyy");
		    Date temp_date = new Date(System.currentTimeMillis());

		    String current_date = dateFormat.format(temp_date)
			    .toString();

		    if (current_date.equals(label_date.getText())) {
			// update записи текущего дня

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET b80_l = '"
						+ label_b80_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET b92_l = '"
						+ label_b92_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET b95_l = '"
						+ label_b95_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_mc_l = '"
						+ label_bdis_mc_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_winter_l = '"
						+ label_bdis_winter_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_leto1_l = '"
						+ label_bdis_leto1_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");
			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_leto2_l = '"
						+ label_bdis_leto2_l.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET b80_t = '"
						+ label_b80_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET b92_t = '"
						+ label_b92_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET b95_t = '"
						+ label_b95_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_mc_t = '"
						+ label_bdis_mc_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_winter_t = '"
						+ label_bdis_winter_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_leto1_t = '"
						+ label_bdis_leto1_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");
			DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeUpdate(
					"UPDATE optov SET bdis_leto2_t = '"
						+ label_bdis_leto2_t.getText()
						+ "' WHERE id LIKE '"
						+ changeId + "';");

		    } else {
			// new records
			PreparedStatement pst = DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.prepareStatement(
					"INSERT INTO optov VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);");

			pst.setInt(2, Integer.parseInt(commnId));
			
			//1367145498916 - 28 апреля
			pst.setString(3,
				Long.toString(System.currentTimeMillis()));
//			pst.setString(3,
//				Long.toString(1367145498916l - 86400000*2));

			pst.setString(4, label_b80_t.getText());
			pst.setString(5, label_b92_t.getText());
			pst.setString(6, label_b95_t.getText());
			pst.setString(7, label_bdis_mc_t.getText());
			pst.setString(8, label_bdis_winter_t.getText());
			pst.setString(9, label_bdis_leto1_t.getText());
			pst.setString(10, label_bdis_leto2_t.getText());

			pst.setString(11, label_b80_l.getText());
			pst.setString(12, label_b92_l.getText());
			pst.setString(13, label_b95_l.getText());
			pst.setString(14, label_bdis_mc_l.getText());
			pst.setString(15, label_bdis_winter_l.getText());
			pst.setString(16, label_bdis_leto1_l.getText());
			pst.setString(17, label_bdis_leto2_l.getText());

			pst.addBatch();

			pst.executeBatch();
			pst.close();

			ResultSet rs = DriverManager
				.getConnection("jdbc:sqlite:" + Oil.PATH)
				.createStatement()
				.executeQuery(
					"select seq from sqlite_sequence where name like 'optov';");
			while (rs.next())
			    changeId = rs.getString(1);

			rs.close();
		    }

		    label_date.setText(current_date);
		    label_date.setForeground(Color.red);
		}
	    } catch (SQLException e1) {
		e1.printStackTrace();
	    }
	}
    }

    public String getCommId() {
	return commnId;
    }

    public void setCommId(String id) {
	this.commnId = id;
    }

    public String getDate() {
	return date;
    }

    public void setDate(String date) {
	this.date = date;
    }

    public String getStatus() {
	return status;
    }

    public void setStatus(String status) {
	this.status = status;
    }

    public String getTitle() {
	return title;
    }

    public void setTitle(String title) {
	this.title = title;
    }

    public String getChangeId() {
	return changeId;
    }

    public void setChangeId(String changeId) {
	this.changeId = changeId;
    }

    public void setThis(Optov stations) {
	this.stations = stations;
    }

    public String getB80_t() {
	return b80_t;
    }

    public void setB80_t(String b80_t) {
	this.b80_t = b80_t;
    }

    public String getB92_t() {
	return b92_t;
    }

    public void setB92_t(String b92_t) {
	this.b92_t = b92_t;
    }

    public String getB95_t() {
	return b95_t;
    }

    public void setB95_t(String b95_t) {
	this.b95_t = b95_t;
    }

    public String getBdis_mc_t() {
	return bdis_mc_t;
    }

    public void setBdis_mc_t(String bdis_mc_t) {
	this.bdis_mc_t = bdis_mc_t;
    }

    public String getBdis_winter_t() {
	return bdis_winter_t;
    }

    public void setBdis_winter_t(String bdis_winter_t) {
	this.bdis_winter_t = bdis_winter_t;
    }

    public String getBdis_leto1_t() {
	return bdis_leto1_t;
    }

    public void setBdis_leto1_t(String bdis_leto1_t) {
	this.bdis_leto1_t = bdis_leto1_t;
    }

    public String getBdis_leto2_t() {
	return bdis_leto2_t;
    }

    public void setBdis_leto2_t(String bdis_leto2_t) {
	this.bdis_leto2_t = bdis_leto2_t;
    }

    public String getB80_l() {
	return b80_l;
    }

    public void setB80_l(String b80_l) {
	this.b80_l = b80_l;
    }

    public String getB92_l() {
	return b92_l;
    }

    public void setB92_l(String b92_l) {
	this.b92_l = b92_l;
    }

    public String getB95_l() {
	return b95_l;
    }

    public void setB95_l(String b95_l) {
	this.b95_l = b95_l;
    }

    public String getBdis_mc_l() {
	return bdis_mc_l;
    }

    public void setBdis_mc_l(String bdis_mc_l) {
	this.bdis_mc_l = bdis_mc_l;
    }

    public String getBdis_winter_l() {
	return bdis_winter_l;
    }

    public void setBdis_winter_l(String bdis_winter_l) {
	this.bdis_winter_l = bdis_winter_l;
    }

    public String getBdis_leto1_l() {
	return bdis_leto1_l;
    }

    public void setBdis_leto1_l(String bdis_leto1_l) {
	this.bdis_leto1_l = bdis_leto1_l;
    }

    public String getBdis_leto2_l() {
	return bdis_leto2_l;
    }

    public void setBdis_leto2_l(String bdis_leto2_l) {
	this.bdis_leto2_l = bdis_leto2_l;
    }
}
