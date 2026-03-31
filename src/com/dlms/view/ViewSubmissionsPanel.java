package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ViewSubmissionsPanel extends JFrame {

    private User currentUser;
    private JComboBox<String> assignmentComboBox;
    private JTextArea submissionsArea;

    public ViewSubmissionsPanel(User user) {
        this.currentUser = user;

        setTitle("View Student Submissions");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.add(new JLabel("Select Assignment:"));
        assignmentComboBox = new JComboBox<>();
        loadTeacherAssignments();
        topPanel.add(assignmentComboBox);

        JButton btnRefresh = new JButton("Refresh");
        topPanel.add(btnRefresh);

        mainPanel.add(topPanel, BorderLayout.NORTH);

        submissionsArea = new JTextArea();
        submissionsArea.setEditable(false);
        submissionsArea.setFont(new Font("Arial", Font.PLAIN, 14));
        mainPanel.add(new JScrollPane(submissionsArea), BorderLayout.CENTER);

        btnRefresh.addActionListener(e -> loadSubmissions());

        assignmentComboBox.addActionListener(e -> loadSubmissions());

        add(mainPanel);
    }

    private void loadTeacherAssignments() {
        assignmentComboBox.removeAllItems();
        String sql = """
            SELECT a.assignment_id, a.title, c.title as course_title 
            FROM assignments a
            JOIN courses c ON a.course_id = c.course_id
            WHERE a.created_by = ?
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUser.getUserId());
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                assignmentComboBox.addItem(rs.getInt("assignment_id") + " - " + 
                                         rs.getString("course_title") + ": " + rs.getString("title"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void loadSubmissions() {
        if (assignmentComboBox.getSelectedItem() == null) return;

        try {
            String selected = (String) assignmentComboBox.getSelectedItem();
            int assignmentId = Integer.parseInt(selected.split(" - ")[0]);

            String sql = """
                SELECT u.full_name, s.file_path, s.submitted_at, s.marks_obtained, s.feedback
                FROM submissions s
                JOIN users u ON s.student_id = u.user_id
                WHERE s.assignment_id = ?
                """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, assignmentId);
                ResultSet rs = ps.executeQuery();

                StringBuilder sb = new StringBuilder();
                sb.append("Student Submissions:\n\n");

                boolean hasSubmissions = false;
                while (rs.next()) {
                    hasSubmissions = true;
                    sb.append("Student: ").append(rs.getString("full_name")).append("\n");
                    sb.append("Submitted: ").append(rs.getTimestamp("submitted_at")).append("\n");
                    sb.append("File: ").append(rs.getString("file_path")).append("\n");
                    sb.append("Marks: ").append(rs.getObject("marks_obtained") != null ? rs.getInt("marks_obtained") : "Not Graded").append("\n");
                    sb.append("Feedback: ").append(rs.getString("feedback") != null ? rs.getString("feedback") : "No feedback yet").append("\n");
                    sb.append("----------------------------------------\n\n");
                }

                if (!hasSubmissions) {
                    sb.append("No submissions received yet.");
                }

                submissionsArea.setText(sb.toString());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
