# Eden: Garden of Words

Eden: Garden of Words is an interactive web platform designed for creative writing and content sharing. It allows users to register, write, publish, and engage with posts in a visually appealing "garden" theme. The platform provides social features such as commenting, liking, and searching for posts while ensuring high performance and security.

## Features

- **User Authentication:** Secure signup and login system with encrypted passwords (bcrypt) and session management for personalized user profiles.
- **Create & Edit Posts:** Authors can compose, edit, or delete posts with rich-text formatting via a Quill editor.
- **Comments & Discussion:** Users can comment on posts, and authors can edit or delete their own comments. Discussion feeds are available for all posts.
- **Likes & Engagement:** Users can like or unlike posts, with live like counts displayed, encouraging interaction between users.
- **Search & Browsing:** Built-in search bar for quick post discovery. Supports pagination and sorting by various criteria (e.g., newest or most popular).
- **Post Summarization:** AI-powered automatic post summaries (using Hugging Face’s BART), which are cached in Redis for fast retrieval.
- **Rich API:** JSON-based RESTful API endpoints for retrieving posts, comments, likes, and more, facilitating easy integration and extension for developers.
- **High Performance:** Optimized for performance with HikariCP connection pooling, Redis caching for summaries, and efficient static file serving.
- **Security & Rate Limiting:** Built-in security features like input sanitization, password hashing, and per-endpoint rate limiting to prevent abuse.
- **Developer-Friendly:** Clean, modular code using standard Java servlets, MySQL, Redis, and SLF4J logging. Easily extendable for developers.

## Technologies Used

- **Backend:** Java Servlets, Tomcat 10.1, HikariCP, MySQL, Redis, SLF4J
- **Frontend:** HTML, CSS, JavaScript, Quill editor
- **Authentication:** Bcrypt password hashing, session-based management
- **AI Summarization:** Hugging Face’s BART

## Getting Started

To get a local copy of this project up and running, follow these steps:

### Prerequisites

- **Apache Tomcat 10.1**: Ensure you have Tomcat 10.1 installed and running. You can download it from [Tomcat's official website](https://tomcat.apache.org/).
- **MySQL Database**: You will need a MySQL server running and a database setup for the application.
- **Redis**: Ensure Redis is installed and running for caching AI-generated post summaries.

### Setup Instructions

1. **Clone the repository**:

   ```bash
   git clone https://github.com/RufusVictor/Eden-GardenOfWords.git

2. **Database Setup**:

- Create a MySQL database for the project.

3. **Configure Database Connection**:

- Set the correct MySQL connection details (username, password, and database name).
- You may need to add MySQL JDBC dependencies to your WEB-INF/lib folder if not already included.

4. **Redis Setup**:

- Install Redis by following the official instructions from Redis Documentation.
- Ensure Redis is running on your local machine.

5. **Build and Deploy to Tomcat**:

- After setting up the project, navigate to the Tomcat webapps directory.
- Copy the entire Eden-GardenOfWords folder into the webapps folder of your Tomcat installation.

6. **Start Tomcat**:

- Navigate to the bin directory of your Tomcat installation and run the Tomcat startup script to launch the server:

  ```bash
  ./startup.sh   # For Linux/MacOS
  ./startup.bat  # For Windows

7. **Access the Platform**:

- Once Tomcat is running, open your browser and navigate to:

   ```bash
   http://localhost:8080/Eden-GardenOfWords

- You should now be able to access the platform locally!

## Acknowledgements

- [Hugging Face](https://huggingface.co/) for the BART model used in AI-based summarization.
- [HikariCP](https://github.com/brettwooldridge/HikariCP) for connection pooling.
- [MySQL](https://www.mysql.com/) and [Redis](https://redis.io/) for database and caching solutions.
- [Apache Tomcat](https://tomcat.apache.org/) for providing the servlet container (Tomcat 10.1) for deployment.
