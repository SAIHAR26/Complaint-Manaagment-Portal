import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class AdminLoginFrame extends JFrame implements ActionListener {

    JTextField userField;
    JPasswordField passField;
    JButton loginBtn;

    public AdminLoginFrame() {

        setTitle("Admin Login");
        setSize(450,350);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(245,248,255));

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70,130,180));

        JLabel title = new JLabel("ADMIN LOGIN");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);
        topPanel.add(title);

        // ===== CENTER PANEL =====
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(245,248,255));
        centerPanel.setLayout(new GridLayout(4,1,10,15));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(30,60,30,60));

        userField = new JTextField();
        passField = new JPasswordField();
        loginBtn = new JButton("Login");

        userField.setFont(new Font("Arial", Font.PLAIN, 14));
        passField.setFont(new Font("Arial", Font.PLAIN, 14));

        loginBtn.setBackground(new Color(70,130,180));
        loginBtn.setForeground(Color.WHITE);
        loginBtn.setFont(new Font("Arial", Font.BOLD, 15));

        centerPanel.add(new JLabel("Username"));
        centerPanel.add(userField);
        centerPanel.add(new JLabel("Password"));
        centerPanel.add(passField);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245,248,255));
        buttonPanel.add(loginBtn);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        loginBtn.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        String username = userField.getText();
        String password = String.valueOf(passField.getPassword());

        if(username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter all fields");
            return;
        }

        try {

            Connection con = DBConnection.getConnection();

            String sql = "SELECT * FROM admins WHERE username=? AND password=?";
            PreparedStatement pst = con.prepareStatement(sql);

            pst.setString(1, username);
            pst.setString(2, password);

            ResultSet rs = pst.executeQuery();

            if(rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Admin Login Successful!");
                new AdminDashboardFrame(username);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Invalid Username or Password");
            }

            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Database Error!");
        }
    }
}

