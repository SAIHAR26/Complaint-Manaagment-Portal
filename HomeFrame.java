import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class HomeFrame extends JFrame implements ActionListener {

    JButton studentBtn, adminBtn;

    public HomeFrame() {

        setTitle("Complaint Management Portal");
        setSize(600,400);
        setLocationRelativeTo(null); // center on screen
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Background
        getContentPane().setBackground(new Color(230,240,255));
        setLayout(new BorderLayout());

        // ====== TITLE PANEL ======
        JPanel titlePanel = new JPanel();
        titlePanel.setBackground(new Color(30,144,255));

        JLabel title = new JLabel("Complaint Management Portal");
        title.setFont(new Font("Arial", Font.BOLD, 26));
        title.setForeground(Color.WHITE);

        titlePanel.add(title);

        // ====== CENTER PANEL ======
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(230,240,255));
        centerPanel.setLayout(new GridBagLayout());

        studentBtn = new JButton("Student Login");
        adminBtn = new JButton("Admin Login");

        studentBtn.setPreferredSize(new Dimension(180,40));
        adminBtn.setPreferredSize(new Dimension(180,40));

        // Button styling
        studentBtn.setBackground(new Color(60,179,113));
        studentBtn.setForeground(Color.WHITE);
        studentBtn.setFont(new Font("Arial", Font.BOLD, 15));

        adminBtn.setBackground(new Color(70,130,180));
        adminBtn.setForeground(Color.WHITE);
        adminBtn.setFont(new Font("Arial", Font.BOLD, 15));

        centerPanel.add(studentBtn);
        centerPanel.add(Box.createHorizontalStrut(20));
        centerPanel.add(adminBtn);

        add(titlePanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        studentBtn.addActionListener(this);
        adminBtn.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==studentBtn) {
            new StudentLoginFrame();
        }
        if(e.getSource()==adminBtn) {
            new AdminLoginFrame();
        }
    }

    public static void main(String[] args) {
        new HomeFrame();
    }
}
