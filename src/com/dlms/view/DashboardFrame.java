package com.dlms.view;

import com.dlms.model.User;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class DashboardFrame extends JFrame {

    private User currentUser;
    private JButton btnNotifications;
    public DashboardFrame(User user) {
        this.currentUser = user;

        setTitle("DLMS - Dashboard (" + user.getRole().toUpperCase() + ")");
        setSize(850, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Header
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFullName() + "!", SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        mainPanel.add(welcomeLabel, BorderLayout.NORTH);

        // Role Information
        JLabel roleLabel = new JLabel("Role: " + user.getRole().toUpperCase(), SwingConstants.CENTER);
        roleLabel.setFont(new Font("Arial", Font.PLAIN, 16));
        mainPanel.add(roleLabel, BorderLayout.CENTER);  // temporary

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new GridLayout(0, 2, 15, 15));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        JButton btnLogout = new JButton("Logout");

        if ("admin".equalsIgnoreCase(user.getRole())) {
            addAdminButtons(buttonPanel);
        } else if ("teacher".equalsIgnoreCase(user.getRole())) {
            addTeacherButtons(buttonPanel);
        } else if ("student".equalsIgnoreCase(user.getRole())) {
            addStudentButtons(buttonPanel);
        }

        buttonPanel.add(btnLogout);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Logout Action
        btnLogout.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Do you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                dispose();
                new LoginFrame().setVisible(true);
            }
        });
        refreshNotificationCount()
        add(mainPanel);
    }

    private void addAdminButtons(JPanel panel) {
        JButton btnManageUsers = new JButton("Manage Users");
        JButton btnManageCourses = new JButton("Manage Courses");
        panel.add(btnManageUsers);
        panel.add(btnManageCourses);
        
        // TODO: Add action listeners later
    }

    private void addTeacherButtons(JPanel panel) {
        JButton btnMyCourses = new JButton("My Courses");
        JButton btnCreateCourse = new JButton("Create New Course");     // New button
        JButton btnCreateAssignment = new JButton("Create New Assignment");
        JButton btnUploadMaterial = new JButton("Upload Study Material");
        JButton btnViewSubmissions = new JButton("View Submissions");

        panel.add(btnMyCourses);
        panel.add(btnCreateCourse);        // Added
        panel.add(btnCreateAssignment);
        panel.add(btnUploadMaterial);
        panel.add(btnViewSubmissions);

        btnUploadMaterial.addActionListener(e -> new TeacherPanel(currentUser).setVisible(true));

        // New: Open Create Course window
        btnCreateCourse.addActionListener(e -> {
            new CreateCourseFrame(currentUser).setVisible(true);
        });
        btnCreateAssignment.addActionListener(e -> {
            new CreateAssignmentFrame(currentUser).setVisible(true);
        });
        JButton btnViewSubmissions = new JButton("View Submissions");
        panel.add(btnViewSubmissions);

        btnViewSubmissions.addActionListener(e -> {
            new ViewSubmissionsPanel(currentUser).setVisible(true);
        });
    }

    private void addStudentButtons(JPanel panel) {
        JButton btnEnrolledCourses = new JButton("My Enrolled Courses");
        JButton btnBrowseCourses = new JButton("Browse & Enroll Courses");
        JButton btnViewAssignments = new JButton("View Assignments & Submit");

        btnNotifications = new JButton("🛎 Notifications (0)");
        btnNotifications.addActionListener(e -> new NotificationPanel(currentUser).setVisible(true));
        btnViewAssignments.addActionListener(e -> {
            new SubmitAssignmentPanel(currentUser).setVisible(true);
        });
        panel.add(btnEnrolledCourses);
        panel.add(btnBrowseCourses);
        panel.add(btnViewAssignments);
        panel.add(btnNotifications);           // Added

        btnEnrolledCourses.addActionListener(e -> new StudentPanel(currentUser).setVisible(true));
        btnBrowseCourses.addActionListener(e -> JOptionPane.showMessageDialog(this, "Browse & Enroll feature coming soon!"));
    }
    private void refreshNotificationCount() {
        if (!"student".equalsIgnoreCase(currentUser.getRole())) return;

        String sql = """
            SELECT COUNT(*) FROM assignments a
            JOIN enrollments e ON a.course_id = e.course_id
            WHERE e.student_id = ? 
              AND a.due_date > NOW()
              AND NOT EXISTS (
                  SELECT 1 FROM submissions s 
                  WHERE s.assignment_id = a.assignment_id AND s.student_id = ?
              )
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUser.getUserId());
            ps.setInt(2, currentUser.getUserId());

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                int count = rs.getInt(1);
                btnNotifications.setText("🛎 Notifications (" + count + ")");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}