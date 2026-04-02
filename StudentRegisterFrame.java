import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentRegisterFrame extends JFrame implements ActionListener {

    JTextField idField, nameField, emailField, deptField, phoneField;
    JPasswordField passField, confirmPassField;
    JButton requestBtn, closeBtn;

    public StudentRegisterFrame() {
        setTitle("Student Registration Request");
        setSize(520,520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(245,248,255));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(60,179,113));
        JLabel title = new JLabel("CREATE STUDENT ACCOUNT REQUEST");
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        topPanel.add(title);
        add(topPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setBackground(new Color(245,248,255));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20,20,20,20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8,8,8,8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Student ID:"), gbc);
        gbc.gridx = 1; idField = new JTextField(20); centerPanel.add(idField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1; nameField = new JTextField(20); centerPanel.add(nameField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1; emailField = new JTextField(20); centerPanel.add(emailField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Department:"), gbc);
        gbc.gridx = 1; deptField = new JTextField(20); centerPanel.add(deptField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Phone:"), gbc);
        gbc.gridx = 1; phoneField = new JTextField(20); centerPanel.add(phoneField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Password:"), gbc);
        gbc.gridx = 1; passField = new JPasswordField(20); centerPanel.add(passField, gbc);

        row++;
        gbc.gridx = 0; gbc.gridy = row; centerPanel.add(new JLabel("Confirm Password:"), gbc);
        gbc.gridx = 1; confirmPassField = new JPasswordField(20); centerPanel.add(confirmPassField, gbc);

        add(centerPanel, BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245,248,255));
        requestBtn = new JButton("Request");
        closeBtn = new JButton("Close");
        requestBtn.setBackground(new Color(60,179,113));
        requestBtn.setForeground(Color.WHITE);
        closeBtn.setBackground(Color.DARK_GRAY);
        closeBtn.setForeground(Color.WHITE);
        bottomPanel.add(requestBtn);
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        requestBtn.addActionListener(this);
        closeBtn.addActionListener(this);

        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() == closeBtn) {
            dispose();
            return;
        }

        String studentId = idField.getText().trim();
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String department = deptField.getText().trim();
        String phone = phoneField.getText().trim();
        String password = String.valueOf(passField.getPassword());
        String confirm = String.valueOf(confirmPassField.getPassword());

        if(studentId.isEmpty() || name.isEmpty() || email.isEmpty() ||
           department.isEmpty() || phone.isEmpty() || password.isEmpty() || confirm.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields.");
            return;
        }

        if(!password.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Password and Confirm Password do not match.");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();
            if(con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            ensureRequestsTableExists(con);

            PreparedStatement blockedCheck = con.prepareStatement(
                    "SELECT COUNT(*) FROM student_requests " +
                    "WHERE status='Blocked' " +
                    "AND student_id=? " +
                    "AND LOWER(name)=LOWER(?) " +
                    "AND LOWER(email)=LOWER(?) " +
                    "AND LOWER(department)=LOWER(?) " +
                    "AND phone=?");
            blockedCheck.setString(1, studentId);
            blockedCheck.setString(2, name);
            blockedCheck.setString(3, email);
            blockedCheck.setString(4, department);
            blockedCheck.setString(5, phone);
            ResultSet blockedRs = blockedCheck.executeQuery();
            blockedRs.next();
            if(blockedRs.getInt(1) > 0) {
                blockedRs.close();
                blockedCheck.close();
                JOptionPane.showMessageDialog(this, "You are blocked. Please contact admin.");
                con.close();
                return;
            }
            blockedRs.close();
            blockedCheck.close();

            PreparedStatement existingStudent = con.prepareStatement(
                    "SELECT COUNT(*) FROM students WHERE student_id=?");
            existingStudent.setString(1, studentId);
            ResultSet studentRs = existingStudent.executeQuery();
            studentRs.next();
            if(studentRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Student ID already exists. Please login.");
                con.close();
                return;
            }

            PreparedStatement existingRequest = con.prepareStatement(
                    "SELECT COUNT(*) FROM student_requests WHERE student_id=? AND status='Pending'");
            existingRequest.setString(1, studentId);
            ResultSet reqRs = existingRequest.executeQuery();
            reqRs.next();
            if(reqRs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "A pending request already exists for this Student ID.");
                con.close();
                return;
            }

            String sql = "INSERT INTO student_requests " +
                    "(student_id, name, email, department, phone, password, status, requested_on) " +
                    "VALUES (?, ?, ?, ?, ?, ?, 'Pending', NOW())";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, studentId);
            pst.setString(2, name);
            pst.setString(3, email);
            pst.setString(4, department);
            pst.setString(5, phone);
            pst.setString(6, password);
            pst.executeUpdate();

            con.close();

            JOptionPane.showMessageDialog(this, "Registration request sent to admin.");
            dispose();

        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error sending request.");
        }
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
}
