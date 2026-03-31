package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.*;

public class SubmitAssignmentPanel extends JFrame {

    private User currentUser;
    private JComboBox<String> assignmentComboBox;
    private JLabel lblSelectedFile;
    private File selectedFile;

    public SubmitAssignmentPanel(User user) {
        this.currentUser = user;

        setTitle("Submit Assignment");
        setSize(650, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 12, 12, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        JLabel title = new JLabel("Submit Your Assignment", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(title, gbc);

        gbc.gridwidth = 1;

        // Assignment Selection
        gbc.gridy = 1; gbc.gridx = 0;
        mainPanel.add(new JLabel("Select Assignment:"), gbc);

        gbc.gridx = 1;
        assignmentComboBox = new JComboBox<>();
        loadPendingAssignments();
        mainPanel.add(assignmentComboBox, gbc);

        // File Selection
        gbc.gridy = 2; gbc.gridx = 0;
        mainPanel.add(new JLabel("Select File:"), gbc);

        gbc.gridx = 1;
        JButton btnChoose = new JButton("Choose File");
        lblSelectedFile = new JLabel("No file selected");
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filePanel.add(btnChoose);
        filePanel.add(lblSelectedFile);
        mainPanel.add(filePanel, gbc);

        // Submit Button
        gbc.gridy = 3; gbc.gridx = 0; gbc.gridwidth = 2;
        JButton btnSubmit = new JButton("Submit Assignment");
        btnSubmit.setFont(new Font("Arial", Font.BOLD, 14));
        mainPanel.add(btnSubmit, gbc);

        add(mainPanel);

        // Choose File Action
        btnChoose.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int result = chooser.showOpenDialog(this);
            if (result == JFileChooser.APPROVE_OPTION) {
                selectedFile = chooser.getSelectedFile();
                lblSelectedFile.setText(selectedFile.getName());
            }
        });

        // Submit Action
        btnSubmit.addActionListener(e -> submitAssignment());
    }

    private void loadPendingAssignments() {
        assignmentComboBox.removeAllItems();
        String sql = """
            SELECT a.assignment_id, a.title, c.title as course_title 
            FROM assignments a
            JOIN courses c ON a.course_id = c.course_id
            JOIN enrollments e ON a.course_id = e.course_id
            WHERE e.student_id = ?
              AND a.due_date > NOW()
              AND NOT EXISTS (SELECT 1 FROM submissions s 
                              WHERE s.assignment_id = a.assignment_id AND s.student_id = ?)
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUser.getUserId());
            ps.setInt(2, currentUser.getUserId());

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                assignmentComboBox.addItem(rs.getInt("assignment_id") + " - " + 
                                         rs.getString("course_title") + ": " + rs.getString("title"));
            }

            if (assignmentComboBox.getItemCount() == 0) {
                assignmentComboBox.addItem("No pending assignments");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void submitAssignment() {
        if (assignmentComboBox.getSelectedItem() == null || selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select assignment and file");
            return;
        }

        try {
            String selected = (String) assignmentComboBox.getSelectedItem();
            int assignmentId = Integer.parseInt(selected.split(" - ")[0]);

            // Create submissions folder
            File uploadDir = new File("uploads/submissions");
            if (!uploadDir.exists()) uploadDir.mkdirs();

            File destFile = new File(uploadDir, currentUser.getUsername() + "_" + selectedFile.getName());
            java.nio.file.Files.copy(selectedFile.toPath(), destFile.toPath(), 
                                   java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            // Save to database
            String sql = "INSERT INTO submissions (assignment_id, student_id, file_path) VALUES (?, ?, ?)";
            
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, assignmentId);
                ps.setInt(2, currentUser.getUserId());
                ps.setString(3, destFile.getAbsolutePath());

                int rows = ps.executeUpdate();
                if (rows > 0) {
                    JOptionPane.showMessageDialog(this, "Assignment submitted successfully!");
                    dispose();
                    
                    // Refresh notification count if Dashboard is open
                    JOptionPane.showMessageDialog(null, "Submission successful! Notification count will update on next login.");
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Submission failed: " + ex.getMessage());
        }
    }
}