import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class AdminDashboardFrame extends JFrame implements ActionListener {

    JTable table;
    JButton updateBtn, deleteBtn, viewBtn, logoutBtn, searchBtn, profileBtn, requestsBtn;
    JComboBox<String> statusBox;
    JTextField searchField;
    String loggedInAdminUsername;

    public AdminDashboardFrame(String adminUsername) {
        this.loggedInAdminUsername = adminUsername;

        setTitle("Admin Dashboard - Manage Complaints");
        setSize(900,550);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(230,240,255));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(30,144,255));

        JLabel title = new JLabel("ADMIN CONTROL PANEL");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        title.setHorizontalAlignment(SwingConstants.CENTER);

        logoutBtn = new JButton("Logout");
        logoutBtn.setBackground(Color.DARK_GRAY);
        logoutBtn.setForeground(Color.WHITE);

        JPanel rightTopPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rightTopPanel.setOpaque(false);
        rightTopPanel.add(logoutBtn);

        requestsBtn = new JButton("Requests");
        requestsBtn.setBackground(new Color(255,140,0));
        requestsBtn.setForeground(Color.WHITE);
        JPanel leftTopPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        leftTopPanel.setOpaque(false);
        leftTopPanel.add(requestsBtn);

        topPanel.add(leftTopPanel, BorderLayout.WEST);
        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(rightTopPanel, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        loadTableData();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(230,240,255));

        statusBox = new JComboBox<>(new String[]{"Pending","In Progress","Resolved"});
        updateBtn = new JButton("Update Status");
        deleteBtn = new JButton("Delete Complaint");
        viewBtn = new JButton("View Details");
        profileBtn = new JButton("Profile");
        searchField = new JTextField(8);
        searchBtn = new JButton("Search ID");

        updateBtn.setBackground(new Color(60,179,113));
        deleteBtn.setBackground(new Color(220,20,60));
        viewBtn.setBackground(new Color(70,130,180));
        logoutBtn.setBackground(Color.DARK_GRAY);
        searchBtn.setBackground(new Color(255,165,0));
        profileBtn.setBackground(new Color(123,104,238));

        updateBtn.setForeground(Color.WHITE);
        deleteBtn.setForeground(Color.WHITE);
        viewBtn.setForeground(Color.WHITE);
        searchBtn.setForeground(Color.WHITE);
        profileBtn.setForeground(Color.WHITE);

        bottomPanel.add(new JLabel("Change Status:"));
        bottomPanel.add(statusBox);
        bottomPanel.add(updateBtn);
        bottomPanel.add(viewBtn);
        bottomPanel.add(deleteBtn);
        bottomPanel.add(new JLabel("Search:"));
        bottomPanel.add(searchField);
        bottomPanel.add(searchBtn);
        bottomPanel.add(profileBtn);

        updateBtn.addActionListener(this);
        deleteBtn.addActionListener(this);
        viewBtn.addActionListener(this);
        logoutBtn.addActionListener(this);
        searchBtn.addActionListener(this);
        profileBtn.addActionListener(this);
        requestsBtn.addActionListener(this);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void loadTableData() {
        try {
            Connection con = DBConnection.getConnection();
            ensureScreenshotColumnExists(con);

            String sql = "SELECT complaint_id, title, status, student_id, description, screenshot_path FROM complaints";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            String[] column = {"ID","Title","Status","Roll No","Description","Screenshot"};

            DefaultTableModel model = new DefaultTableModel(column, 0) {
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("complaint_id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("student_id"),
                        rs.getString("description"),
                        rs.getString("screenshot_path")
                });
            }

            table = new JTable(model);

            table.removeColumn(table.getColumnModel().getColumn(5));
            table.removeColumn(table.getColumnModel().getColumn(4));
            table.removeColumn(table.getColumnModel().getColumn(3));

            JScrollPane sp = new JScrollPane(table);
            add(sp, BorderLayout.CENTER);

            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }

    public void actionPerformed(ActionEvent e) {

        int row = table.getSelectedRow();

        if(e.getSource()==updateBtn) {

            if(row >= 0) {

                String complaintId = table.getModel().getValueAt(row,0).toString();
                String newStatus = statusBox.getSelectedItem().toString();

                try {
                    Connection con = DBConnection.getConnection();

                    String sql = "UPDATE complaints SET status=? WHERE complaint_id=?";
                    PreparedStatement pst = con.prepareStatement(sql);

                    pst.setString(1, newStatus);
                    pst.setString(2, complaintId);

                    pst.executeUpdate();
                    con.close();

                    dispose();
                    new AdminDashboardFrame(loggedInAdminUsername);

                } catch(Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this,"Error updating status!");
                }

            } else {
                JOptionPane.showMessageDialog(this,"Select a complaint first");
            }
        }

        if(e.getSource()==deleteBtn) {

            if(row >= 0) {

                String complaintId = table.getModel().getValueAt(row,0).toString();

                int confirm = JOptionPane.showConfirmDialog(this,
                        "Are you sure you want to delete this complaint?",
                        "Confirm Delete",
                        JOptionPane.YES_NO_OPTION);

                if(confirm == JOptionPane.YES_OPTION) {

                    try {
                        Connection con = DBConnection.getConnection();

                        String sql = "DELETE FROM complaints WHERE complaint_id=?";
                        PreparedStatement pst = con.prepareStatement(sql);
                        pst.setString(1, complaintId);

                        pst.executeUpdate();
                        con.close();

                        dispose();
                        new AdminDashboardFrame(loggedInAdminUsername);

                    } catch(Exception ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this,"Error deleting complaint!");
                    }
                }

            } else {
                JOptionPane.showMessageDialog(this,"Select a complaint first");
            }
        }

        if(e.getSource()==viewBtn) {

            if(row >= 0) {

                String id = table.getModel().getValueAt(row,0).toString();
                String title = table.getModel().getValueAt(row,1).toString();
                String status = table.getModel().getValueAt(row,2).toString();
                String roll = table.getModel().getValueAt(row,3).toString();
                String desc = table.getModel().getValueAt(row,4).toString();
                String screenshotPath = (String) table.getModel().getValueAt(row,5);

                JPanel detailsPanel = new JPanel(new BorderLayout(10,10));

                JTextArea info = new JTextArea(
                        "Complaint ID: " + id +
                        "\nRoll Number: " + roll +
                        "\nTitle: " + title +
                        "\nStatus: " + status +
                        "\n\nDescription:\n" + desc);
                info.setEditable(false);
                info.setLineWrap(true);
                info.setWrapStyleWord(true);
                info.setBackground(detailsPanel.getBackground());

                detailsPanel.add(info, BorderLayout.NORTH);

                if(screenshotPath != null && !screenshotPath.trim().isEmpty()) {
                    File imageFile = new File(screenshotPath);
                    if(imageFile.exists()) {
                        ImageIcon icon = new ImageIcon(screenshotPath);
                        Image scaledImage = icon.getImage().getScaledInstance(420, 260, Image.SCALE_SMOOTH);
                        JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
                        imageLabel.setBorder(BorderFactory.createTitledBorder("Uploaded Screenshot"));
                        detailsPanel.add(imageLabel, BorderLayout.CENTER);
                    } else {
                        detailsPanel.add(new JLabel("Screenshot file not found at saved path."),
                                BorderLayout.CENTER);
                    }
                } else {
                    detailsPanel.add(new JLabel("No screenshot uploaded for this complaint."),
                            BorderLayout.CENTER);
                }

                JOptionPane.showMessageDialog(this, detailsPanel, "Complaint Details", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,"Select a complaint first");
            }
        }

        if(e.getSource()==searchBtn) {

            String searchId = searchField.getText();
            boolean found = false;

            for(int i=0; i<table.getRowCount(); i++) {
                if(table.getModel().getValueAt(i,0).toString().equals(searchId)) {
                    table.setRowSelectionInterval(i,i);
                    found = true;
                    break;
                }
            }

            if(!found) {
                JOptionPane.showMessageDialog(this,"Complaint not found");
            }
        }

        if(e.getSource()==profileBtn) {
            new AdminProfileFrame(loggedInAdminUsername);
        }

        if(e.getSource()==requestsBtn) {
            new AdminRequestFrame();
        }

        if(e.getSource()==logoutBtn) {
            dispose();
        }
    }

    private void ensureScreenshotColumnExists(Connection con) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() " +
                "AND TABLE_NAME = 'complaints' " +
                "AND COLUMN_NAME = 'screenshot_path'";

        PreparedStatement checkPst = con.prepareStatement(checkSql);
        ResultSet rs = checkPst.executeQuery();
        rs.next();

        if(rs.getInt(1) == 0) {
            Statement st = con.createStatement();
            st.executeUpdate("ALTER TABLE complaints ADD COLUMN screenshot_path VARCHAR(500) NULL");
            st.close();
        }

        rs.close();
        checkPst.close();
    }
}
