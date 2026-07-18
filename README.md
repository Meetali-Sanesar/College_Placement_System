# College Placement System

A modern, full-stack Campus Placement Management System built with **Spring Boot** and **Vanilla JavaScript**. This platform streamlines the recruitment process for colleges by connecting students with placement drives, managing applications, and providing AI-powered resume analysis.

---

## 🌟 Key Features

### 🎓 For Students
* **Automated Resume Analysis:** Upload your resume (PDF) and get instant feedback! The system parses your resume text, cross-references it with job descriptions, and provides an ATS score, missing keywords, and actionable suggestions.
* **Placement Drives Dashboard:** Browse open placement drives, check eligibility criteria (Branch, CGPA), and apply with a single click.
* **Application Tracking:** Real-time status tracking for all submitted applications (Applied, Shortlisted, Selected, Rejected).

### 🏢 For Administrators (Placement Cell)
* **Company & Drive Management:** Post new placement drives, set eligibility criteria, and manage participating companies.
* **Application Processing:** Review student applications, download resumes securely from the cloud, and update applicant statuses.
* **Student Directory:** View registered students, their skills, CGPA, and academic details.

---

## 🏗️ Architecture & Tech Stack

* **Backend Framework:** Java 21, Spring Boot 3.3.4
* **Security:** Spring Security with stateless JWT (JSON Web Token) authentication.
* **Database:** MySQL (Hibernate / Spring Data JPA)
* **Cloud Storage:** Cloudinary (for secure, permanent PDF resume storage).
* **Frontend:** Vanilla HTML5, CSS3, JavaScript (Fetch API), Bootstrap 5.
* **Resume Parsing:** Apache PDFBox for text extraction and dynamic analysis.

---

## 📸 Screenshots

*(Add your screenshots here! Just drag and drop images into this section on GitHub)*

1. **Student Dashboard & Resume Analysis**
2. **Admin Dashboard**
3. **Application Tracking**

---

## 🚀 Local Setup Instructions

### Prerequisites
* Java 21 or higher
* Maven
* MySQL Server (running on port 3306)

### 1. Database Configuration
Create a new MySQL database named `campus_placement_db`.

### 2. Environment Variables
You will need to set the following environment variables before running the application (or add them to an `.env` file):

```bash
DB_PASSWORD="your_mysql_password"
JWT_SECRET="your_secure_random_base64_string_for_tokens"
ADMIN_DEFAULT_PASSWORD="your_secure_admin_password"
CLOUDINARY_URL="cloudinary://<api_key>:<api_secret>@<cloud_name>"
```
*(Note: You can get a free `CLOUDINARY_URL` by creating an account at [Cloudinary.com](https://cloudinary.com))*

### 3. Build and Run
Open your terminal in the project directory and run:

```bash
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`. 

### 4. Default Admin Login
On startup, the system automatically seeds a default Admin account if one doesn't exist.
* **Email:** `admin@campusplacement.com`
* **Password:** *(Whatever you set as `ADMIN_DEFAULT_PASSWORD` in your environment variables)*

---

## ☁️ Deployment

This application is ready to be deployed to platforms like **Render**, **Railway**, or **Heroku**.
Because it uses Cloudinary for file storage, it is completely compatible with ephemeral file systems (like Render's free tier). Uploaded resumes will never be lost!

---
*Developed as a comprehensive project demonstrating full-stack development, cloud integration, and security best practices.*
