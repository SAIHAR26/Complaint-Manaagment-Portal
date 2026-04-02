import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class StudentDashboardFrame extends JFrame implements ActionListener {

    JButton complaintBtn, statusBtn, profileBtn, logoutBtn;
    String loggedInStudentId;   // store logged-in student ID

    public StudentDashboardFrame(String studentId) {

        this.loggedInStudentId = studentId;   // save ID

        setTitle("Student Dashboard");
        setSize(700,450);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(240,248,255));

        // ===== TOP PANEL =====
        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(60,179,113));

        JLabel title = new JLabel("STUDENT DASHBOARD");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        topPanel.add(title);

        // ===== CENTER PANEL =====
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(240,248,255));
        centerPanel.setLayout(new GridLayout(1,4,20,20));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(80,50,80,50));

        complaintBtn = new JButton("<html><center>Register<br>Complaint</center></html>");
        statusBtn = new JButton("<html><center>View<br>Status</center></html>");
        profileBtn = new JButton("<html><center>My<br>Profile</center></html>");
        logoutBtn = new JButton("<html><center>Logout</center></html>");

        styleButton(complaintBtn, new Color(70,130,180));
        styleButton(statusBtn, new Color(255,140,0));
        styleButton(profileBtn, new Color(138,43,226));
        styleButton(logoutBtn, new Color(220,20,60));

        centerPanel.add(complaintBtn);
        centerPanel.add(statusBtn);
        centerPanel.add(profileBtn);
        centerPanel.add(logoutBtn);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        complaintBtn.addActionListener(this);
        statusBtn.addActionListener(this);
        profileBtn.addActionListener(this);
        logoutBtn.addActionListener(this);

        setVisible(true);
    }

    // ===== BUTTON STYLE METHOD =====
    private void styleButton(JButton btn, Color bgColor) {
        btn.setBackground(bgColor);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Arial", Font.BOLD, 16));
        btn.setFocusPainted(false);
    }

    // ===== BUTTON ACTIONS =====
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == complaintBtn) {
            new ComplaintFormFrame(loggedInStudentId);  // pass student ID
        }

        if(e.getSource() == statusBtn) {
            new ViewStatusFrame(loggedInStudentId);     // pass student ID
        }

        if(e.getSource() == profileBtn) {
            new StudentProfileFrame(loggedInStudentId);
        }

        if(e.getSource() == logoutBtn) {
            dispose();
        }
    }
}
