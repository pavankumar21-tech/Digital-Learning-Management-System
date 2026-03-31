package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class CreateCourseFrame extends JFrame {

    private User currentUser;
    private JTextField txtCourseCode, txtTitle;
    private JTextArea txtDescription;

    public CreateCourseFrame(User user) {
        this.currentUser = user;

        setTitle("Create New Course");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        panel.add(new JLabel("Create New Course", SwingConstants.CENTER), gbc);

        gbc.gridwidth = 1;
        gbc.gridy = 1; gbc.gridx = 0;
        panel.add(new JLabel("Course Code:"), gbc);

        gbc.gridx = 1;
        txtCourseCode = new JTextField(15);
        panel.add(txtCourseCode, gbc);

        gbc.gridy = 2; gbc.gridx = 0;
        panel.add(new JLabel("Course Title:"), gbc);

        gbc.gridx = 1;
        txtTitle = new JTextField(15);
        panel.add(txtTitle, gbc);

        gbc.gridy = 3; gbc.gridx = 0;
        panel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        txtDescription = new JTextArea(5, 15);
        panel.add(new JScrollPane(txtDescription), gbc);

        gbc.gridy = 4; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnCreate = new JButton("Create Course");
        btnCreate.setFont(new Font("Arial", Font.BOLD, 14));
        panel.add(btnCreate, gbc);

        add(panel);

        btnCreate.addActionListener(e -> createCourse());
    }

    private void createCourse() {
        String code = txtCourseCode.getText().trim();
        String title = txtTitle.getText().trim();
        String desc = txtDescription.getText().trim();

        if (code.isEmpty() || title.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Course Code and Title are required!");
            return;
        }

        String sql = "INSERT INTO courses (course_code, title, description, teacher_id) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, code);
            ps.setString(2, title);
            ps.setString(3, desc);
            ps.setInt(4, currentUser.getUserId());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                JOptionPane.showMessageDialog(this, "Course created successfully!");
                dispose(); // close window
            }
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1062) { // Duplicate entry
                JOptionPane.showMessageDialog(this, "Course Code already exists!");
            } else {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error creating course: " + ex.getMessage());
            }
        }
    }
}
