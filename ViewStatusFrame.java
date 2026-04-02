import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.sql.*;

public class ViewStatusFrame extends JFrame implements ActionListener {

    JTable table;
    JButton backBtn, viewDetailsBtn;

    public ViewStatusFrame(String studentId) {

        setTitle("View Complaint Status");
        setSize(850,500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240,248,255));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(255,140,0));

        JLabel title = new JLabel("COMPLAINT STATUS");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        topPanel.add(title);
        add(topPanel, BorderLayout.NORTH);

        try {
            Connection con = DBConnection.getConnection();
            ensureScreenshotColumnExists(con);

            String sql = "SELECT complaint_id, title, status, category, description, date_submitted, screenshot_path " +
                    "FROM complaints WHERE student_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, studentId);

            ResultSet rs = pst.executeQuery();

            String[] columns = {"ID","Title","Status","Category","Description","Date","Screenshot"};

            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("complaint_id"),
                        rs.getString("title"),
                        rs.getString("status"),
                        rs.getString("category"),
                        rs.getString("description"),
                        rs.getString("date_submitted"),
                        rs.getString("screenshot_path")
                });
            }

            table = new JTable(model);
            table.setRowHeight(28);
            table.setFont(new Font("Arial", Font.PLAIN, 14));

            table.getTableHeader().setFont(new Font("Arial", Font.BOLD, 15));
            table.getTableHeader().setBackground(new Color(70,130,180));
            table.getTableHeader().setForeground(Color.WHITE);

            DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
                @Override
                public Component getTableCellRendererComponent(JTable table,
                                                               Object value,
                                                               boolean isSelected,
                                                               boolean hasFocus,
                                                               int row,
                                                               int column) {

                    Component c = super.getTableCellRendererComponent(
                            table,value,isSelected,hasFocus,row,column);

                    if(column == 2) {
                        String status = value.toString();

                        if(status.equals("Pending")) {
                            c.setForeground(Color.RED);
                        }
                        else if(status.equals("In Progress")) {
                            c.setForeground(new Color(255,140,0));
                        }
                        else if(status.equals("Resolved")) {
                            c.setForeground(new Color(34,139,34));
                        }
                    } else {
                        c.setForeground(Color.BLACK);
                    }

                    return c;
                }
            };

            table.getColumnModel().getColumn(2).setCellRenderer(renderer);

            table.removeColumn(table.getColumnModel().getColumn(6));
            table.removeColumn(table.getColumnModel().getColumn(5));
            table.removeColumn(table.getColumnModel().getColumn(4));
            table.removeColumn(table.getColumnModel().getColumn(3));

            table.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if(e.getClickCount() == 2) {
                        showSelectedComplaintDetails();
                    }
                }
            });

            JScrollPane sp = new JScrollPane(table);
            add(sp, BorderLayout.CENTER);

            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
        }

        backBtn = new JButton("Back");
        backBtn.setBackground(Color.DARK_GRAY);
        backBtn.setForeground(Color.WHITE);
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));

        viewDetailsBtn = new JButton("View Details");
        viewDetailsBtn.setBackground(new Color(70,130,180));
        viewDetailsBtn.setForeground(Color.WHITE);
        viewDetailsBtn.setFont(new Font("Arial", Font.BOLD, 14));

        backBtn.addActionListener(this);
        viewDetailsBtn.addActionListener(this);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(240,248,255));
        bottomPanel.add(viewDetailsBtn);
        bottomPanel.add(backBtn);

        add(bottomPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == viewDetailsBtn) {
            showSelectedComplaintDetails();
        }

        if(e.getSource() == backBtn) {
            dispose();
        }
    }

    private void showSelectedComplaintDetails() {
        int row = table.getSelectedRow();
        if(row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a complaint first.");
            return;
        }

        String id = table.getModel().getValueAt(row,0).toString();
        String title = table.getModel().getValueAt(row,1).toString();
        String status = table.getModel().getValueAt(row,2).toString();
        String category = table.getModel().getValueAt(row,3).toString();
        String description = table.getModel().getValueAt(row,4).toString();
        String dateSubmitted = table.getModel().getValueAt(row,5).toString();
        String screenshotPath = (String) table.getModel().getValueAt(row,6);

        JPanel detailsPanel = new JPanel(new BorderLayout(10,10));

        JTextArea info = new JTextArea(
                "Complaint ID: " + id +
                "\nTitle: " + title +
                "\nStatus: " + status +
                "\nCategory: " + category +
                "\nDate Submitted: " + dateSubmitted +
                "\n\nDescription:\n" + description);
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
