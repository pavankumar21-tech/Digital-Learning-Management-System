package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class TeacherPanel extends JFrame {

    private User currentUser;
    private JComboBox<String> courseComboBox;
    private JTextField txtMaterialTitle;
    private JTextArea txtDescription;
    private JLabel lblSelectedFile;

    public TeacherPanel(User user) {
        this.currentUser = user;

        setTitle("Teacher Panel - Upload Study Material");
        setSize(700, 550);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel titleLabel = new JLabel("Upload Study Material", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        mainPanel.add(titleLabel, gbc);

        gbc.gridwidth = 1;

        // Course Selection
        gbc.gridy = 1; gbc.gridx = 0;
        mainPanel.add(new JLabel("Select Course:"), gbc);

        gbc.gridx = 1;
        courseComboBox = new JComboBox<>();
        loadTeacherCourses();   // Load courses assigned to this teacher
        mainPanel.add(courseComboBox, gbc);

        // Material Title
        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(new JLabel("Material Title:"), gbc);

        gbc.gridx = 1;
        txtMaterialTitle = new JTextField(20);
        mainPanel.add(txtMaterialTitle, gbc);

        // Description
        gbc.gridy = 3; gbc.gridx = 0;
        mainPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        txtDescription = new JTextArea(4, 20);
        txtDescription.setLineWrap(true);
        mainPanel.add(new JScrollPane(txtDescription), gbc);

        // File Selection
        gbc.gridy = 4; gbc.gridx = 0;
        mainPanel.add(new JLabel("Select File:"), gbc);

        gbc.gridx = 1;
        JButton btnChooseFile = new JButton("Choose File");
        lblSelectedFile = new JLabel("No file selected");
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(btnChooseFile);
        filePanel.add(lblSelectedFile);
        mainPanel.add(filePanel, gbc);

        // Upload Button
        gbc.gridy = 5; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnUpload = new JButton("Upload Material");
        btnUpload.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(btnUpload, gbc);

        add(mainPanel);

        // File Chooser Action
        final File[] selectedFile = new File[1];
        btnChooseFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            int result = fileChooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile[0] = fileChooser.getSelectedFile();
                lblSelectedFile.setText(selectedFile[0].getName());
            }
        });

        // Upload Action
        btnUpload.addActionListener(e -> {
            uploadMaterial(selectedFile[0]);
        });
    }

    private void loadTeacherCourses() {
        courseComboBox.removeAllItems();
        String sql = "SELECT course_id, title FROM courses WHERE teacher_id = ?";
        
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            
            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();
            
            while (rs.next()) {
                int id = rs.getInt("course_id");
                String title = rs.getString("title");
                courseComboBox.addItem(id + " - " + title);
            }
            
            if (courseComboBox.getItemCount() == 0) {
                courseComboBox.addItem("No courses assigned yet");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading courses");
        }
    }

    private void uploadMaterial(File file) {
        if (courseComboBox.getSelectedItem() == null || txtMaterialTitle.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select course and enter title");
            return;
        }

        if (file == null) {
            JOptionPane.showMessageDialog(this, "Please select a file to upload");
            return;
        }

        try {
            // Get course_id from combo box
            String selected = (String) courseComboBox.getSelectedItem();
            int courseId = Integer.parseInt(selected.split(" - ")[0]);

            // Create uploads folder if not exists
            File uploadDir = new File("uploads/materials");
            if (!uploadDir.exists()) uploadDir.mkdirs();

            // Copy file to uploads folder
            File destFile = new File(uploadDir, file.getName());
            java.nio.file.Files.copy(file.toPath(), destFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Save to database
            String sql = "INSERT INTO materials (course_id, title, file_path, file_type, uploaded_by) VALUES (?, ?, ?, ?, ?)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                
                ps.setInt(1, courseId);
                ps.setString(2, txtMaterialTitle.getText().trim());
                ps.setString(3, destFile.getAbsolutePath());
                ps.setString(4, getFileExtension(file.getName()));
                ps.setInt(5, currentUser.getUserId());
                
                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Material uploaded successfully!");
                    // Clear fields
                    txtMaterialTitle.setText("");
                    txtDescription.setText("");
                    lblSelectedFile.setText("No file selected");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Upload failed: " + ex.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return (lastDot == -1) ? "" : filename.substring(lastDot + 1).toLowerCase();
    }
}