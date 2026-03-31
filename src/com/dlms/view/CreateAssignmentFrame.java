package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CreateAssignmentFrame extends JFrame {

    private User currentUser;
    private JComboBox<String> courseComboBox;
    private JTextField txtTitle, txtMaxMarks;
    private JTextArea txtDescription;
    private JSpinner dueDateSpinner;

    public CreateAssignmentFrame(User user) {
        this.currentUser = user;

        setTitle("Create New Assignment");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel titleLabel = new JLabel("Create New Assignment", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Course Selection
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Select Course:"), gbc);

        gbc.gridx = 1;
        courseComboBox = new JComboBox<>();
        loadTeacherCourses();
        panel.add(courseComboBox, gbc);

        // Assignment Title
        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Assignment Title:"), gbc);

        gbc.gridx = 1;
        txtTitle = new JTextField(20);
        panel.add(txtTitle, gbc);

        // Description
        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        txtDescription = new JTextArea(5, 20);
        panel.add(new JScrollPane(txtDescription), gbc);

        // Max Marks
        gbc.gridy = 4; gbc.gridx = 0;
        panel.add(new JLabel("Maximum Marks:"), gbc);

        gbc.gridx = 1;
        txtMaxMarks = new JTextField("100");
        panel.add(txtMaxMarks, gbc);

        // Due Date
        gbc.gridy = 5; gbc.gridx = 0;
        panel.add(new JLabel("Due Date & Time:"), gbc);

        gbc.gridx = 1;
        SpinnerDateModel model = new SpinnerDateModel();
        dueDateSpinner = new JSpinner(model);
        JSpinner.DateEditor editor = new JSpinner.DateEditor(dueDateSpinner, "yyyy-MM-dd HH:mm");
        dueDateSpinner.setEditor(editor);
        panel.add(dueDateSpinner, gbc);

        // Create Button
        gbc.gridy = 6; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnCreate = new JButton("Create Assignment");
        btnCreate.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(btnCreate, gbc);

        add(panel);

        btnCreate.addActionListener(e -> createAssignment());
    }

    private void loadTeacherCourses() {
        courseComboBox.removeAllItems();
        String sql = "SELECT course_id, title FROM courses WHERE teacher_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                courseComboBox.addItem(rs.getInt("course_id") + " - " + rs.getString("title"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void createAssignment() {
        if (courseComboBox.getSelectedItem() == null || txtTitle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select course and enter title");
            return;
        }

        try {
            String selected = (String) courseComboBox.getSelectedItem();
            int courseId = Integer.parseInt(selected.split(" - ")[0]);

            String sql = "INSERT INTO assignments (course_id, title, description, due_date, max_marks, created_by) " +
                         "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, courseId);
                ps.setString(2, txtTitle.getText().trim());
                ps.setString(3, txtDescription.getText().trim());
                ps.setTimestamp(4, Timestamp.valueOf(((java.util.Date) dueDateSpinner.getValue()).toInstant()
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()));
                ps.setInt(5, Integer.parseInt(txtMaxMarks.getText().trim()));
                ps.setInt(6, currentUser.getUserId());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Assignment created successfully!");
                    dispose();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error creating assignment: " + ex.getMessage());
        }
    }
}