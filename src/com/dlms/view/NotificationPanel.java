package com.dlms.view;

import com.dlms.model.User;
import com.dlms.util.DBConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class NotificationPanel extends JFrame {

    private User currentUser;
    private DefaultListModel<String> listModel;

    public NotificationPanel(User user) {
        this.currentUser = user;

        setTitle("🛎 Notifications - Pending Assignments");
        setSize(700, 500);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Pending / Upcoming Assignments", SwingConstants.CENTER);
        header.setFont(new Font("Arial", Font.BOLD, 18));
        mainPanel.add(header, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        JList<String> list = new JList<>(listModel);
        list.setFont(new Font("Arial", Font.PLAIN, 14));

        loadPendingAssignments();

        mainPanel.add(new JScrollPane(list), BorderLayout.CENTER);

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dispose());
        JPanel bottom = new JPanel();
        bottom.add(btnClose);
        mainPanel.add(bottom, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadPendingAssignments() {
        listModel.clear();

        String sql = """
            SELECT a.title, c.title as course_title, a.due_date,
                   CASE WHEN EXISTS (
                       SELECT 1 FROM submissions s 
                       WHERE s.assignment_id = a.assignment_id AND s.student_id = ?
                   ) THEN 'Submitted' ELSE 'Pending' END as status
            FROM assignments a
            JOIN courses c ON a.course_id = c.course_id
            JOIN enrollments e ON a.course_id = e.course_id
            WHERE e.student_id = ? 
              AND a.due_date > NOW()
            ORDER BY a.due_date ASC
            """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, currentUser.getUserId());
            ps.setInt(2, currentUser.getUserId());

            ResultSet rs = ps.executeQuery();
            boolean hasData = false;

            while (rs.next()) {
                hasData = true;
                String title = rs.getString("title");
                String course = rs.getString("course_title");
                Timestamp due = rs.getTimestamp("due_date");
                String status = rs.getString("status");

                listModel.addElement(String.format("📚 %s → %s  |  Due: %s  |  Status: %s", 
                    course, title, due.toString().substring(0, 16), status));
            }

            if (!hasData) {
                listModel.addElement("🎉 No pending assignments at the moment!");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            listModel.addElement("Error loading notifications.");
        }
    }
}