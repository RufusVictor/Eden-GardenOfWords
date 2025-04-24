import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import org.mindrot.jbcrypt.BCrypt;

@WebServlet({ "/register", "/login", "/logout" })
public class UserServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/register".equals(path)) {
            handleRegister(request, response);
        } else if ("/login".equals(path)) {
            handleLogin(request, response);
        }
    }

    private void handleRegister(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = sanitizeInput(request.getParameter("name"));
        String email = sanitizeInput(request.getParameter("email"));
        String password = request.getParameter("password");

        if (name == null || email == null || password == null || name.isEmpty() || email.isEmpty()
                || password.isEmpty()) {
            response.sendRedirect("register.html?error=All fields are required!");
            return;
        }

        if (!isValidEmail(email)) {
            response.sendRedirect("register.html?error=Invalid email format!");
            return;
        }

        name = name.length() > 50 ? name.substring(0, 50) : name;
        email = email.length() > 100 ? email.substring(0, 100) : email;
        String hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt());

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            // Check if username or email already exists
            String checkUserQuery = "SELECT COUNT(*) FROM users WHERE username = ? OR email = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkUserQuery)) {
                checkStmt.setString(1, name);
                checkStmt.setString(2, email);
                ResultSet rs = checkStmt.executeQuery();
                rs.next();
                if (rs.getInt(1) > 0) {
                    response.sendRedirect("register.html?error=Username or Email already in use");
                    return;
                }
            }

            // Insert new user (default role: 'user')
            String insertUserQuery = "INSERT INTO users (username, email, password) VALUES (?, ?, ?)";
            try (PreparedStatement insertStmt = conn.prepareStatement(insertUserQuery)) {
                insertStmt.setString(1, name);
                insertStmt.setString(2, email);
                insertStmt.setString(3, hashedPassword);
                insertStmt.executeUpdate();
            }

            response.sendRedirect("login.html?success=Registration successful, Please login!");
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("register.html?error=Database error!");
        }
    }

    private void handleLogin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = sanitizeInput(request.getParameter("email"));
        String password = request.getParameter("password");

        if (email == null || password == null || email.isEmpty() || password.isEmpty()) {
            response.sendRedirect("login.html?error=All fields are required!");
            return;
        }

        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(
                        "SELECT user_id, username, email, password, role FROM users WHERE email = ?")) {

            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedHashedPassword = rs.getString("password");

                if (BCrypt.checkpw(password, storedHashedPassword)) {
                    try (PreparedStatement updateLoginStmt = conn.prepareStatement(
                            "UPDATE users SET last_login = CURRENT_TIMESTAMP WHERE email = ?")) {
                        updateLoginStmt.setString(1, email);
                        updateLoginStmt.executeUpdate();
                    }

                    User foundUser = new User(rs.getInt("user_id"), rs.getString("username"), rs.getString("email"),
                            rs.getString("role"));

                    HttpSession session = request.getSession();
                    session.setAttribute("user", foundUser);
                    response.sendRedirect("index.html");
                } else {
                    response.sendRedirect("login.html?error=Invalid credentials!");
                }
            } else {
                response.sendRedirect("login.html?error=Invalid credentials!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendRedirect("login.html?error=Database error!");
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String path = request.getServletPath();

        if ("/logout".equals(path)) {
            handleLogout(request, response);
        }
    }

    private void handleLogout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Remove JSESSIONID cookie
        Cookie cookie = new Cookie("JSESSIONID", "");
        cookie.setMaxAge(0);
        cookie.setPath(request.getContextPath());
        response.addCookie(cookie);

        response.sendRedirect("login.html?success=Logged out successfully!");
    }

    private String sanitizeInput(String input) {
        if (input == null || input.isEmpty())
            return "";
        String sanitized = input.replaceAll("<[^>]*>", "");
        sanitized = sanitized.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");

        return sanitized.trim();
    }

    private boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }
}
