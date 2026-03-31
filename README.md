# Digital Learning Management System (DLMS)

A desktop-based **Learning Management System** built using **Java Swing**, **JDBC**, and **MySQL**.

## 🎯 Features

### Admin
- Manage users and courses

### Teacher
- Create new courses
- Upload study materials (PDF, Docs, etc.)
- Create assignments
- View student submissions

### Student
- View enrolled courses
- Download study materials
- Get assignment notifications
- Submit assignments with file upload
- View pending assignments

## 🛠 Technologies Used

- **Frontend**: Java Swing (GUI)
- **Backend**: JDBC (Database Connectivity)
- **Database**: MySQL 8.0
- **IDE**: Eclipse
- **Build Tool**: Maven (optional)
## 🚀 How to Run the Project

1. **Database Setup**
   - Create database `dlms_db` in MySQL
   - Import the SQL script (tables + sample data)
   - Update password in `DBConnection.java`

2. **Run the Project**
   - Open project in Eclipse
   - Run `com.dlms.main.Main.java`

3. **Login Credentials**

   | Role     | Username     | Password      |
   |----------|--------------|---------------|
   | Admin    | admin        | admin123      |
   | Teacher  | teacher1     | teacher123    |
   | Student  | student1     | student123    |



## ✨ Key Highlights

- Complete **Role-based Access Control**
- File upload and download functionality
- Real-time assignment notification system
- Clean MVC-style architecture
- Proper exception handling

## 🔮 Future Enhancements

- Web version using Spring Boot + React
- Email notifications
- Online video lecture support
- Grading system with percentage calculation
- Admin dashboard with analytics

---

**Made with ❤️ by Pavan Kumar**
