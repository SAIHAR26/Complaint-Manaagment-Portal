import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.nio.file.*;
import java.sql.*;

public class ComplaintFormFrame extends JFrame implements ActionListener {

    JTextField titleField;
    JTextArea descArea;
    JComboBox<String> categoryBox;
    JButton submitBtn, uploadBtn;
    String selectedScreenshotPath;

    String loggedInStudentId;

    public ComplaintFormFrame(String studentId) {

        this.loggedInStudentId = studentId;

        setTitle("Register Complaint");
        setSize(650,500);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(245,248,255));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(70,130,180));

        JLabel title = new JLabel("REGISTER COMPLAINT");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(Color.WHITE);

        topPanel.add(title);

        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(new Color(245,248,255));
        centerPanel.setLayout(new GridBagLayout());
        centerPanel.setBorder(BorderFactory.createEmptyBorder(20,40,20,40));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10,10,10,10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        centerPanel.add(new JLabel("Complaint Title:"), gbc);

        gbc.gridx = 1;
        titleField = new JTextField(20);
        centerPanel.add(titleField, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        centerPanel.add(new JLabel("Category:"), gbc);

        gbc.gridx = 1;
        categoryBox = new JComboBox<>(new String[]{
                "Academics","Hostel","Transport","Labs","Others"
        });
        centerPanel.add(categoryBox, gbc);

        gbc.gridx = 0; gbc.gridy = 2;
        gbc.anchor = GridBagConstraints.NORTH;
        centerPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        descArea = new JTextArea(6,20);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(descArea);
        centerPanel.add(scroll, gbc);

        gbc.gridx = 1; gbc.gridy = 3;
        uploadBtn = new JButton("Upload Screenshot (Optional)");
        uploadBtn.setBackground(new Color(255,140,0));
        uploadBtn.setForeground(Color.WHITE);
        centerPanel.add(uploadBtn, gbc);

        gbc.gridx = 1; gbc.gridy = 4;
        submitBtn = new JButton("Submit Complaint");
        submitBtn.setBackground(new Color(60,179,113));
        submitBtn.setForeground(Color.WHITE);
        submitBtn.setFont(new Font("Arial", Font.BOLD, 15));
        centerPanel.add(submitBtn, gbc);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);

        submitBtn.addActionListener(this);
        uploadBtn.addActionListener(this);

        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {

        if(e.getSource()==uploadBtn) {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Select Screenshot");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                    "Image Files", "png", "jpg", "jpeg", "gif", "bmp"));

            int result = chooser.showOpenDialog(this);
            if(result == JFileChooser.APPROVE_OPTION) {
                try {
                    Path uploadDir = Paths.get("uploads");
                    if(!Files.exists(uploadDir)) {
                        Files.createDirectories(uploadDir);
                    }

                    String originalName = chooser.getSelectedFile().getName();
                    String uniqueName = System.currentTimeMillis() + "_" + originalName;
                    Path destination = uploadDir.resolve(uniqueName);

                    Files.copy(chooser.getSelectedFile().toPath(),
                            destination,
                            StandardCopyOption.REPLACE_EXISTING);

                    selectedScreenshotPath = destination.toAbsolutePath().toString();
                    JOptionPane.showMessageDialog(this, "Screenshot selected successfully.");

                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error uploading screenshot!");
                }
            }
        }

        if(e.getSource()==submitBtn) {

            if(titleField.getText().isEmpty() ||
               descArea.getText().isEmpty()) {

                JOptionPane.showMessageDialog(this, "Please fill all required fields.");

            } else {

                try {
                    Connection con = DBConnection.getConnection();
                    ensureScreenshotColumnExists(con);

                    String sql = "INSERT INTO complaints " +
                            "(student_id, title, description, category, status, date_submitted, screenshot_path) " +
                            "VALUES (?, ?, ?, ?, ?, CURDATE(), ?)";

                    PreparedStatement pst = con.prepareStatement(sql);

                    pst.setString(1, loggedInStudentId);
                    pst.setString(2, titleField.getText());
                    pst.setString(3, descArea.getText());
                    pst.setString(4, categoryBox.getSelectedItem().toString());
                    pst.setString(5, "Pending");
                    pst.setString(6, selectedScreenshotPath);

                    pst.executeUpdate();

                    JOptionPane.showMessageDialog(this, "Complaint Submitted Successfully!");

                    con.close();
                    dispose();

                } catch(Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error submitting complaint!");
                }
            }
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
