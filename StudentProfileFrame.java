import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class StudentProfileFrame extends JFrame {

    private final String studentId;
    private JTextField nameField;
    private JTextField emailField;
    private JTextArea detailsArea;
    private String nameColumn;
    private String emailColumn;

    public StudentProfileFrame(String studentId) {

        this.studentId = studentId;

        setTitle("Student Profile");
        setSize(550,520);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        getContentPane().setBackground(new Color(245,248,255));

        JPanel topPanel = new JPanel();
        topPanel.setBackground(new Color(60,179,113));
        JLabel title = new JLabel("MY PROFILE");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Arial", Font.BOLD, 22));
        topPanel.add(title);
        add(topPanel, BorderLayout.NORTH);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(new Color(245,248,255));
        formPanel.setBorder(BorderFactory.createEmptyBorder(15,15,5,15));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6,6,6,6);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);

        gbc.gridx = 1;
        nameField = new JTextField(22);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:"), gbc);

        gbc.gridx = 1;
        emailField = new JTextField(22);
        formPanel.add(emailField, gbc);

        JButton saveBtn = new JButton("Save Changes");
        saveBtn.setBackground(new Color(60,179,113));
        saveBtn.setForeground(Color.WHITE);

        gbc.gridx = 1;
        gbc.gridy = 2;
        formPanel.add(saveBtn, gbc);

        add(formPanel, BorderLayout.WEST);

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);

        JScrollPane sp = new JScrollPane(detailsArea);
        sp.setBorder(BorderFactory.createEmptyBorder(10,15,15,15));
        add(sp, BorderLayout.CENTER);

        saveBtn.addActionListener(e -> saveProfileChanges());

        JButton closeBtn = new JButton("Close");
        closeBtn.setBackground(Color.DARK_GRAY);
        closeBtn.setForeground(Color.WHITE);
        closeBtn.addActionListener(e -> dispose());

        JPanel bottomPanel = new JPanel();
        bottomPanel.setBackground(new Color(245,248,255));
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        loadProfileData();
        setVisible(true);
    }

    private void loadProfileData() {
        try {
            Connection con = DBConnection.getConnection();
            if(con == null) {
                detailsArea.setText("Database connection failed.");
                return;
            }

            nameColumn = findExistingColumn(con, "students", "name", "student_name", "full_name");
            emailColumn = findExistingColumn(con, "students", "email", "mail", "student_email");

            String sql = "SELECT * FROM students WHERE student_id=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, studentId);
            ResultSet rs = pst.executeQuery();

            if(rs.next()) {
                if(nameColumn != null) {
                    nameField.setText(valueOrEmpty(rs.getString(nameColumn)));
                } else {
                    nameField.setEnabled(false);
                    nameField.setText("Name column not found");
                }

                if(emailColumn != null) {
                    emailField.setText(valueOrEmpty(rs.getString(emailColumn)));
                } else {
                    emailField.setEnabled(false);
                    emailField.setText("Email column not found");
                }

                detailsArea.setText(buildDetailsText(rs));
            } else {
                detailsArea.setText("Profile not found.");
            }

            con.close();

        } catch(Exception ex) {
            ex.printStackTrace();
            detailsArea.setText("Error loading profile details.");
        }
    }

    private void saveProfileChanges() {
        try {
            Connection con = DBConnection.getConnection();
            if(con == null) {
                JOptionPane.showMessageDialog(this, "Database connection failed.");
                return;
            }

            List<String> setParts = new ArrayList<>();
            List<String> values = new ArrayList<>();

            if(nameColumn != null) {
                setParts.add(nameColumn + "=?");
                values.add(nameField.getText().trim());
            }
            if(emailColumn != null) {
                setParts.add(emailColumn + "=?");
                values.add(emailField.getText().trim());
            }

            if(setParts.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Name/Email columns not found in students table.");
                con.close();
                return;
            }

            String sql = "UPDATE students SET " + String.join(", ", setParts) + " WHERE student_id=?";
            PreparedStatement pst = con.prepareStatement(sql);

            int index = 1;
            for(String value : values) {
                pst.setString(index++, value);
            }
            pst.setString(index, studentId);

            int updated = pst.executeUpdate();
            con.close();

            if(updated > 0) {
                JOptionPane.showMessageDialog(this, "Profile updated successfully!");
                loadProfileData();
            } else {
                JOptionPane.showMessageDialog(this, "No changes were saved.");
            }

        } catch(Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error updating profile.");
        }
    }

    private String buildDetailsText(ResultSet rs) throws SQLException {
        StringBuilder details = new StringBuilder();
        ResultSetMetaData meta = rs.getMetaData();

        for(int i = 1; i <= meta.getColumnCount(); i++) {
            String columnName = meta.getColumnName(i);
            if(columnName.equalsIgnoreCase("password")) {
                continue;
            }

            String value = rs.getString(i);
            if(value == null || value.trim().isEmpty()) {
                value = "-";
            }
            details.append(formatLabel(columnName))
                    .append(": ")
                    .append(value)
                    .append("\n");
        }
        return details.toString();
    }

    private String findExistingColumn(Connection con, String tableName, String... candidates) throws SQLException {
        for(String col : candidates) {
            String sql = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS " +
                    "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME=? AND COLUMN_NAME=?";
            PreparedStatement pst = con.prepareStatement(sql);
            pst.setString(1, tableName);
            pst.setString(2, col);
            ResultSet rs = pst.executeQuery();
            rs.next();
            int count = rs.getInt(1);
            rs.close();
            pst.close();
            if(count > 0) {
                return col;
            }
        }
        return null;
    }

    private String valueOrEmpty(String value) {
        return value == null ? "" : value;
    }

    private String formatLabel(String columnName) {
        return columnName.replace("_", " ").toUpperCase();
    }
}
