import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class StudentLoginFrame extends JFrame implements ActionListener {

    JTextField idField;
    JPasswordField passField;
    JButton loginBtn, createBtn;

    public StudentLoginFrame() {

        setTitle("Student Login");
        setSize(450,350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        getContentPane().setBackground(new Color(245,248,255));

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(60,179,113));

        JLabel title = new JLabel("STUDENT LOGIN");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        topPanel.add(title);

        // ===== CENTER PANEL =====
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(245,248,255));
        centerPanel.setLayout(new GridLayout(4,1,10,15));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30,60,30,60));

        idField = new JTextField();
        passField = new JPasswordField();
        loginBtn = new JButton("Login");

        idField.setFont(new Font("Arial", Font.PLAIN, 14));
        passField.setFont(new Font("Arial", Font.PLAIN, 14));

        loginBtn.setBackground(new Color(60,179,113));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 15));

        centerPanel.add(new JLabel("Student ID"));
        centerPanel.add(idField);
        centerPanel.add(new JLabel("Password"));
        centerPanel.add(passField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBackground(new Color(245,248,255));

        JPanel loginRow = new JPanel();
        loginRow.setBackground(new Color(245,248,255));
        loginRow.add(loginBtn);

        JPanel createRow = new JPanel();
        createRow.setBackground(new Color(245,248,255));
        JLabel registerLabel = new JLabel("No account?");
        createBtn = new JButton("Create One");
        createBtn.setBorderPainted(false);
        createBtn.setContentAreaFilled(false);
        createBtn.setForeground(new Color(30,144,255));
        createBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createRow.add(registerLabel);
        createRow.add(createBtn);

        buttonPanel.add(loginRow);
        buttonPanel.add(createRow);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(this);
        createBtn.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if(e.getSource()==createBtn) {
            new StudentRegisterFrame();
            return;
        }

        String studentId = idField.getText();
        String password = String.valueOf(passField.getPassword());

        if(studentId.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter all fields");
            return;
        }

        try {
            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM students WHERE student_id=? AND password=?";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, studentId);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {

                JOptionPane.showMessageDialog(this,
                        "Login Successful!");

                // ✅ PASS studentId to Dashboard
                new StudentDashboardFrame(studentId);

                dispose();

            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid Student ID or Password");
            }

            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database Error!");
        }
    }
}
