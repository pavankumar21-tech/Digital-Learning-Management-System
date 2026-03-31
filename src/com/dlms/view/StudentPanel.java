package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class StudentPanel extends JFrame {

    private User currentUser;
    private JComboBox<String> courseComboBox;
    private DefaultListModel<String> materialListModel;
    private JList<String> materialList;

    public StudentPanel(User user) {
        this.currentUser = user;

        setTitle("Student Panel - My Learning");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        // Header
        JLabel header = new JLabel("Welcome " + user.getFullName() + " - Student Dashboard", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(header, BorderLayout.NORTH);

        // Center Panel
        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));

        // Course Selection
        JPanel coursePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        coursePanel.add(new JLabel("Select Course:"));
        courseComboBox = new JComboBox<>();
        loadEnrolledCourses();
        courseComboBox.addActionListener(e -> loadMaterials());
        coursePanel.add(courseComboBox);
        centerPanel.add(coursePanel, BorderLayout.NORTH);

        // Materials List
        materialListModel = new DefaultListModel<>();
        materialList = new JList<>(materialListModel);
        materialList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JScrollPane scrollPane = new JScrollPane(materialList);
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        // Download Button
        JButton btnDownload = new JButton("Download Selected Material");
        btnDownload.addActionListener(e -> downloadSelectedMaterial());

        JPanel bottomPanel = new JPanel();
        bottomPanel.add(btnDownload);
        centerPanel.add(bottomPanel, BorderLayout.SOUTH);

        mainPanel.add(centerPanel, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadEnrolledCourses() {
        courseComboBox.removeAllItems();
        String sql = """
            SELECT c.course_id, c.title 
            FROM courses c 
            JOIN enrollments e ON c.course_id = e.course_id 
            WHERE e.student_id = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();

            boolean hasCourses = false;
            while (rs.next()) {
                hasCourses = true;
                int id = rs.getInt("course_id");
                String title = rs.getString("title");
                courseComboBox.addItem(id + " - " + title);
            }

            if (!hasCourses) {
                courseComboBox.addItem("You have not enrolled in any courses yet");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses");
        }
    }

    private void loadMaterials() {
        materialListModel.clear();

        if (courseComboBox.getSelectedItem() == null || 
            courseComboBox.getSelectedItem().toString().contains("not enrolled")) {
            return;
        }

        try {
            String selected = (String) courseComboBox.getSelectedItem();
            int courseId = Integer.parseInt(selected.split(" - ")[0]);

            String sql = "SELECT title, file_path FROM materials WHERE course_id = ? ORDER BY uploaded_at DESC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, courseId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    String title = rs.getString("title");
                    String filePath = rs.getString("file_path");
                    materialListModel.addElement(title + " | " + filePath);
                }

                if (materialListModel.isEmpty()) {
                    materialListModel.addElement("No study materials available for this course yet.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void downloadSelectedMaterial() {
        String selected = materialList.getSelectedValue();
        if (selected == null || selected.contains("No study materials")) {
            JOptionPane.showMessageDialog(this, "Please select a material to download");
            return;
        }

        try {
            // Extract file path from the list item
            String filePath = selected.substring(selected.lastIndexOf("|") + 1).trim();

            File file = new File(filePath);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
                JOptionPane.showMessageDialog(this, "File opened successfully!");
            } else {
                JOptionPane.showMessageDialog(this, "File not found at: " + filePath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Could not open file: " + ex.getMessage());
        }
    }
}