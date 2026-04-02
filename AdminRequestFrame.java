import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminRequestFrame extends JFrame implements ActionListener {

    JTable table;
    JButton acceptBtn, rejectBtn, blockBtn, closeBtn, blockListBtn;

    public AdminRequestFrame() {
        setTitle("Student Registration Requests");
        setSize(980,500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(245,248,255));

        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBackground(new Color(255,140,0));
        JLabel title = new JLabel("REGISTRATION REQUESTS");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        blockListBtn = new JButton("Block List");
        blockListBtn.setBackground(Color.BLACK);
        blockListBtn.setForeground(Color.WHITE);

        JPanel rightTop = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 6));
        rightTop.setOpaque(false);
        rightTop.add(blockListBtn);

        topPanel.add(title, BorderLayout.CENTER);
        topPanel.add(rightTop, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);

        loadRequests();

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245,248,255));
        acceptBtn = new JButton("Accept");
        rejectBtn = new JButton("Reject");
        blockBtn = new JButton("Block");
        closeBtn = new JButton("Close");

        acceptBtn.setBackground(new Color(60,179,113));
        rejectBtn.setBackground(new Color(220,20,60));
        blockBtn.setBackground(Color.BLACK);
        closeBtn.setBackground(Color.DARK_GRAY);

        acceptBtn.setForeground(Color.WHITE);
        rejectBtn.setForeground(Color.WHITE);
        blockBtn.setForeground(Color.WHITE);
        closeBtn.setForeground(Color.WHITE);

        bottomPanel.add(acceptBtn);
        bottomPanel.add(rejectBtn);
        bottomPanel.add(blockBtn);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        acceptBtn.addActionListener(this);
        rejectBtn.addActionListener(this);
        blockBtn.addActionListener(this);
        closeBtn.addActionListener(this);
        blockListBtn.addActionListener(this);

        setVisible(true);
    }

    private void loadRequests() {
        try {
            Connection con = DBConnection.getConnection();
            if(con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            ensureRequestsTableExists(con);

            String sql = "SELECT request_id, student_id, name, email, department, phone, status, requested_on " +
                    "FROM student_requests ORDER BY requested_on DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            String[] columns = {"Request ID", "Student ID", "Name", "Email", "Department", "Phone", "Status", "Requested On"};
            DefaultTableModel model = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("phone"),
                        rs.getString("status"),
                        rs.getTimestamp("requested_on")
                });
            }

            table = new JTable(model);
            add(new JScrollPane(table), BorderLayout.CENTER);
            con.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading requests.");
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == closeBtn) {
            dispose();
            return;
        }

        if(e.getSource() == blockListBtn) {
            showBlockedStudents();
            return;
        }

        int row = table.getSelectedRow();
        if(row < 0) {
            JOptionPane.showMessageDialog(this, "Select a request first.");
            return;
        }

        int requestId = Integer.parseInt(table.getModel().getValueAt(row, 0).toString());
        String studentId = table.getModel().getValueAt(row, 1).toString();
        String name = table.getModel().getValueAt(row, 2).toString();
        String email = table.getModel().getValueAt(row, 3).toString();
        String department = table.getModel().getValueAt(row, 4).toString();
        String phone = table.getModel().getValueAt(row, 5).toString();
        String status = table.getModel().getValueAt(row, 6).toString();

        if(!"Pending".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "This request is already processed.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            if(con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            if(e.getSource() == acceptBtn) {
                acceptRequest(con, requestId, studentId, name, email, department, phone);
            } else if(e.getSource() == rejectBtn) {
                updateRequestStatus(con, requestId, "Rejected");
                JOptionPane.showMessageDialog(this, "Request rejected.");
            } else if(e.getSource() == blockBtn) {
                updateRequestStatus(con, requestId, "Blocked");
                JOptionPane.showMessageDialog(this, "Request blocked.");
            }

            con.close();
            refreshTable();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Action failed.");
        }
    }

    private void acceptRequest(Connection con, int requestId, String studentId, String name, String email,
                               String department, String phone) throws SQLException {
        String password = null;
        PreparedStatement readPwd = con.prepareStatement("SELECT password FROM student_requests WHERE request_id=?");
        readPwd.setInt(1, requestId);
        ResultSet rs = readPwd.executeQuery();
        if(rs.next()) {
            password = rs.getString("password");
        }
        rs.close();
        readPwd.close();

        if(password == null) {
            throw new SQLException("Password not found for request.");
        }

        PreparedStatement checkStudent = con.prepareStatement("SELECT COUNT(*) FROM students WHERE student_id=?");
        checkStudent.setString(1, studentId);
        ResultSet checkRs = checkStudent.executeQuery();
        checkRs.next();
        boolean exists = checkRs.getInt(1) > 0;
        checkRs.close();
        checkStudent.close();

        if(exists) {
            updateRequestStatus(con, requestId, "Rejected");
            JOptionPane.showMessageDialog(this, "Student ID already exists. Request marked Rejected.");
            return;
        }

        String insertSql = buildStudentInsertSql(con);
        PreparedStatement insertStudent = con.prepareStatement(insertSql);

        int idx = 1;
        insertStudent.setString(idx++, studentId);
        if(hasColumn(con, "students", "name")) insertStudent.setString(idx++, name);
        if(hasColumn(con, "students", "student_name")) insertStudent.setString(idx++, name);
        if(hasColumn(con, "students", "full_name")) insertStudent.setString(idx++, name);
        if(hasColumn(con, "students", "email")) insertStudent.setString(idx++, email);
        if(hasColumn(con, "students", "student_email")) insertStudent.setString(idx++, email);
        if(hasColumn(con, "students", "mail")) insertStudent.setString(idx++, email);
        if(hasColumn(con, "students", "department")) insertStudent.setString(idx++, department);
        if(hasColumn(con, "students", "phone")) insertStudent.setString(idx++, phone);
        insertStudent.setString(idx, password);
        insertStudent.executeUpdate();
        insertStudent.close();

        updateRequestStatus(con, requestId, "Accepted");
        JOptionPane.showMessageDialog(this, "Request accepted and student account created.");
    }

    private String buildStudentInsertSql(Connection con) throws SQLException {
        StringBuilder columns = new StringBuilder("student_id");
        StringBuilder values = new StringBuilder("?");

        if(hasColumn(con, "students", "name")) { columns.append(", name"); values.append(", ?"); }
        if(hasColumn(con, "students", "student_name")) { columns.append(", student_name"); values.append(", ?"); }
        if(hasColumn(con, "students", "full_name")) { columns.append(", full_name"); values.append(", ?"); }
        if(hasColumn(con, "students", "email")) { columns.append(", email"); values.append(", ?"); }
        if(hasColumn(con, "students", "student_email")) { columns.append(", student_email"); values.append(", ?"); }
        if(hasColumn(con, "students", "mail")) { columns.append(", mail"); values.append(", ?"); }
        if(hasColumn(con, "students", "department")) { columns.append(", department"); values.append(", ?"); }
        if(hasColumn(con, "students", "phone")) { columns.append(", phone"); values.append(", ?"); }

        columns.append(", password");
        values.append(", ?");

        return "INSERT INTO students (" + columns + ") VALUES (" + values + ")";
    }

    private void updateRequestStatus(Connection con, int requestId, String status) throws SQLException {
        PreparedStatement pst = con.prepareStatement("UPDATE student_requests SET status=? WHERE request_id=?");
        pst.setString(1, status);
        pst.setInt(2, requestId);
        pst.executeUpdate();
        pst.close();
    }

    private boolean hasColumn(Connection con, String tableName, String columnName) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?";
        PreparedStatement pst = con.prepareStatement(checkSql);
        pst.setString(1, tableName);
        pst.setString(2, columnName);
        ResultSet rs = pst.executeQuery();
        rs.next();
        boolean exists = rs.getInt(1) > 0;
        rs.close();
        pst.close();
        return exists;
    }

    private void ensureRequestsTableExists(Connection con) throws SQLException {
        String checkSql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES " +
                "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME='student_requests'";
        PreparedStatement pst = con.prepareStatement(checkSql);
        ResultSet rs = pst.executeQuery();
        rs.next();
        int exists = rs.getInt(1);
        rs.close();
        pst.close();

        if(exists == 0) {
            Statement st = con.createStatement();
            st.executeUpdate("CREATE TABLE student_requests (" +
                    "request_id INT PRIMARY KEY AUTO_INCREMENT, " +
                    "student_id VARCHAR(50) NOT NULL, " +
                    "name VARCHAR(100) NOT NULL, " +
                    "email VARCHAR(150) NOT NULL, " +
                    "department VARCHAR(100) NOT NULL, " +
                    "phone VARCHAR(30) NOT NULL, " +
                    "password VARCHAR(100) NOT NULL, " +
                    "status VARCHAR(20) NOT NULL DEFAULT 'Pending', " +
                    "requested_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            st.close();
        }
    }

    private void refreshTable() {
        dispose();
        new AdminRequestFrame();
    }

    private void showBlockedStudents() {
        JDialog dialog = new JDialog(this, "Blocked Students", true);
        dialog.setSize(760, 400);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        String[] columns = {"Request ID", "Student ID", "Name", "Email", "Department", "Phone", "Requested On"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        try {
            Connection con = DBConnection.getConnection();
            if(con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            String sql = "SELECT request_id, student_id, name, email, department, phone, requested_on " +
                    "FROM student_requests WHERE status='Blocked' ORDER BY requested_on DESC";
            PreparedStatement pst = con.prepareStatement(sql);
            ResultSet rs = pst.executeQuery();

            while(rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("request_id"),
                        rs.getString("student_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("department"),
                        rs.getString("phone"),
                        rs.getTimestamp("requested_on")
                });
            }

            con.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading blocked students.");
            return;
        }

        JTable blockedTable = new JTable(model);
        dialog.add(new JScrollPane(blockedTable), BorderLayout.CENTER);

        JButton removeBtn = new JButton("Remove from Blocklist");
        removeBtn.setBackground(new Color(70,130,180));
        removeBtn.setForeground(Color.WHITE);
        removeBtn.addActionListener(ev -> {
            int selectedRow = blockedTable.getSelectedRow();
            if(selectedRow < 0) {
                JOptionPane.showMessageDialog(dialog, "Select a blocked student first.");
                return;
            }

            int requestId = Integer.parseInt(blockedTable.getValueAt(selectedRow, 0).toString());
            try {
                Connection con = DBConnection.getConnection();
                if(con == null) {
                    JOptionPane.showMessageDialog(dialog, "Database connection failed.");
                    return;
                }

                PreparedStatement pst = con.prepareStatement(
                        "UPDATE student_requests SET status='Unblocked' WHERE request_id=?");
                pst.setInt(1, requestId);
                pst.executeUpdate();
                pst.close();
                con.close();

                ((DefaultTableModel) blockedTable.getModel()).removeRow(selectedRow);
                JOptionPane.showMessageDialog(dialog, "Removed from blocklist.");
                refreshTable();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(dialog, "Failed to remove from blocklist.");
            }
        });

        JButton closeDialogBtn = new JButton("Close");
        closeDialogBtn.setBackground(Color.DARK_GRAY);
        closeDialogBtn.setForeground(Color.WHITE);
        closeDialogBtn.addActionListener(ev -> dialog.dispose());

        JPanel bottom = new JPanel();
        bottom.add(removeBtn);
        bottom.add(closeDialogBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        if(model.getRowCount() == 0) {
            JOptionPane.showMessageDialog(this, "No blocked students found.");
        } else {
            dialog.setVisible(true);
        }
    }
}
