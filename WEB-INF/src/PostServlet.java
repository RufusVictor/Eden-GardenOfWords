import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebServlet({ "/submitPost", "/getPosts", "/likePost", "/getLikeCount", "/searchPosts", "/commentPost",
        "/getComments", "/deletePost/*", "/deleteComment/*", "/editPost/*", "/editComment/*", "/getPostDetails/*",
        "/getCommentDetails/*", "/summarizePost" })
public class PostServlet extends HttpServlet {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy hh:mm a");

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect("login.html");
            return;
        }

        User user = (User) session.getAttribute("user");
        int userId = user.getUserID();
        String action = request.getServletPath();

        if ("/submitPost".equals(action)) {
            String title = sanitizeInput(request.getParameter("title"));
            String content = request.getParameter("content");

            if (!title.isEmpty() && !content.isEmpty()) {
                savePostToDatabase(title, content, userId);
            }
            response.sendRedirect("index.html");

        } else if ("/likePost".equals(action)) {
            try {
                int postId = Integer.parseInt(request.getParameter("postId"));
                String jsonResponse = likePost(postId, userId);
                response.getWriter().write(jsonResponse);
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false}");
            }
        } else if ("/commentPost".equals(action)) {
            try {
                int postId = Integer.parseInt(request.getParameter("postId"));
                String comment = sanitizeInput(request.getParameter("comment"));
                comment = truncateText(comment, 10000);
                if (!comment.isEmpty()) {
                    saveCommentToDatabase(postId, userId, comment);
                }
                response.getWriter().write("success");
            } catch (NumberFormatException e) {
                response.getWriter().write("error");
            }
        } else if ("/summarizePost".equals(action)) {
            handleSummarizePost(request, response);
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String action = request.getServletPath();

        if ("/getPosts".equals(action)) {
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer
                    .parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");
            String sort = request.getParameter("sort") != null ? request.getParameter("sort") : "desc";
            int totalPosts = getTotalPostCount();
            int totalPages = (int) Math.ceil((double) totalPosts / pageSize);
            if (page > totalPages) {
                page = totalPages;
            } else if (page < 1) {
                page = 1;
            }
            int offset = (page - 1) * pageSize;
            HttpSession session = request.getSession();
            User loggedInUser = (User) session.getAttribute("user");
            int userId = loggedInUser.getUserID();
            List<Post> posts = fetchPostsFromDatabase(userId, pageSize, offset, sort);
            StringBuilder json = new StringBuilder("{\"posts\":[");
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                json.append("{\"postId\":").append(post.getPostId())
                        .append(", \"title\":\"").append(escapeJson(post.getTitle()))
                        .append("\", \"content\":\"").append(escapeJson(post.getContent()))
                        .append("\", \"username\":\"").append(escapeJson(post.getUsername()))
                        .append("\", \"timestamp\":\"").append(post.getTimestamp())
                        .append("\", \"likes\":").append(post.getLikeCount())
                        .append(", \"liked\":").append(post.isLiked())
                        .append(", \"isAuthor\":").append(post.isAuthor())
                        .append("}");
                if (i < posts.size() - 1) {
                    json.append(",");
                }
            }
            json.append("], \"totalPosts\":").append(totalPosts).append(", \"currentPage\":").append(page)
                    .append(", \"totalPages\":").append(totalPages).append("}");

            response.getWriter().write(json.toString());
        } else if ("/getComments".equals(action)) {
            try {
                int postId = Integer.parseInt(request.getParameter("postId"));
                int offset = Integer.parseInt(request.getParameter("offset"));

                HttpSession session = request.getSession(false);
                User loggedInUser = session != null ? (User) session.getAttribute("user") : null;
                int userId = loggedInUser != null ? loggedInUser.getUserID() : -1; // Default to -1 if no user is logged
                                                                                   // in

                List<Comment> comments = fetchCommentsFromDatabase(postId, userId, offset, 5);

                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < comments.size(); i++) {
                    Comment comment = comments.get(i);
                    json.append("{\"commentId\":").append(comment.getCommentId())
                            .append(", \"username\":\"").append(escapeJson(comment.getUsername()))
                            .append("\", \"comment\":\"").append(escapeJson(comment.getContent()))
                            .append("\", \"timestamp\":\"").append(comment.getTimestamp())
                            .append("\", \"isAuthor\":").append(comment.isAuthor()) // Add isAuthor flag
                            .append("}");
                    if (i < comments.size() - 1) {
                        json.append(",");
                    }
                }
                json.append("]");

                response.getWriter().write(json.toString());
            } catch (NumberFormatException e) {
                response.setStatus(400); // Invalid parameters
            }
        } else if ("/getLikeCount".equals(action)) {
            try {
                int postId = Integer.parseInt(request.getParameter("postId"));
                HttpSession session = request.getSession(false);
                User loggedInUser = (User) session.getAttribute("user");
                int userId = loggedInUser != null ? loggedInUser.getUserID() : -1;

                String sql = "SELECT COUNT(*) AS likeCount, EXISTS(SELECT 1 FROM likes WHERE post_id = ? AND user_id = ?) AS liked "
                        +
                        "FROM likes WHERE post_id = ?";
                try (Connection conn = DatabaseConnectionPool.getConnection();
                        PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setInt(1, postId);
                    stmt.setInt(2, userId);
                    stmt.setInt(3, postId);
                    try (ResultSet rs = stmt.executeQuery()) {
                        if (rs.next()) {
                            int likeCount = rs.getInt("likeCount");
                            boolean liked = rs.getBoolean("liked");
                            response.getWriter().write(String.format(
                                    "{\"success\": true, \"likeCount\": %d, \"liked\": %b}", likeCount, liked));
                        } else {
                            response.getWriter().write("{\"success\": false}");
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                        response.getWriter().write("{\"success\": false, \"message\": \"Database error\"}");
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    response.getWriter().write("{\"success\": false, \"message\": \"Database error\"}");
                }
            } catch (NumberFormatException e) {
                response.getWriter().write("{\"success\": false, \"message\": \"Invalid post ID\"}");
            }
        } else if ("/searchPosts".equals(action)) {
            String query = request.getParameter("query");
            String sort = request.getParameter("sort");
            int page = Integer.parseInt(request.getParameter("page") != null ? request.getParameter("page") : "1");
            int pageSize = Integer
                    .parseInt(request.getParameter("pageSize") != null ? request.getParameter("pageSize") : "10");

            HttpSession session = request.getSession();
            User loggedInUser = (User) session.getAttribute("user");
            int userId = loggedInUser.getUserID();

            List<Post> posts = searchPostsFromDatabase(query, sort, userId, page, pageSize);
            int totalPosts = getTotalSearchPostCount(query);

            StringBuilder json = new StringBuilder("{\"posts\":[");
            for (int i = 0; i < posts.size(); i++) {
                Post post = posts.get(i);
                json.append("{\"postId\":").append(post.getPostId())
                        .append(", \"title\":\"").append(escapeJson(post.getTitle()))
                        .append("\", \"content\":\"").append(escapeJson(post.getContent()))
                        .append("\", \"username\":\"").append(escapeJson(post.getUsername()))
                        .append("\", \"timestamp\":\"").append(post.getTimestamp())
                        .append("\", \"likes\":").append(post.getLikeCount())
                        .append(", \"liked\":").append(post.isLiked())
                        .append(", \"isAuthor\":").append(post.isAuthor())
                        .append("}");
                if (i < posts.size() - 1) {
                    json.append(",");
                }
            }
            json.append("], \"totalPosts\":").append(totalPosts).append("}");
            response.getWriter().write(json.toString());
        } else if ("/getPostDetails".equals(action)) {
            handleGetPostDetails(request, response);
        } else if ("/getCommentDetails".equals(action)) {
            handleGetCommentDetails(request, response);
        }
    }

    private List<Post> searchPostsFromDatabase(String query, String sort, int userId, int page, int pageSize) {
        List<Post> posts = new ArrayList<>();
        String orderByClause = "";
        switch (sort) {
            case "desc":
                orderByClause = "ORDER BY p.timestamp DESC";
                break;
            case "likes_desc":
                orderByClause = "ORDER BY likes DESC";
                break;
            case "asc":
                orderByClause = "ORDER BY p.timestamp ASC";
                break;
            default:
                orderByClause = "ORDER BY p.timestamp DESC";
        }

        String sql = "SELECT p.post_id, p.title, p.content, u.username, p.timestamp, " +
                "(SELECT COUNT(*) FROM likes WHERE post_id = p.post_id) AS likes, " +
                "(SELECT 1 FROM likes WHERE post_id = p.post_id AND user_id = ?) AS liked, " +
                "(CASE WHEN p.user_id = ? THEN 1 ELSE 0 END) AS is_author " +
                "FROM posts p " +
                "JOIN users u ON p.user_id = u.user_id " +
                "WHERE p.title LIKE ? OR p.content LIKE ? OR u.username LIKE ? " +
                orderByClause + " LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); // For the "liked" subquery
            stmt.setInt(2, userId); // For the "is_author" check
            stmt.setString(3, "%" + query + "%");
            stmt.setString(4, "%" + query + "%");
            stmt.setString(5, "%" + query + "%");
            stmt.setInt(6, pageSize);
            stmt.setInt(7, (page - 1) * pageSize);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int postId = rs.getInt("post_id");
                LocalDateTime postTime = rs.getTimestamp("timestamp").toLocalDateTime()
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        .toLocalDateTime();
                boolean liked = rs.getInt("liked") == 1;
                boolean isAuthor = rs.getInt("is_author") == 1;

                posts.add(new Post(
                        postId,
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("username"),
                        postTime.format(formatter),
                        rs.getInt("likes"),
                        liked,
                        isAuthor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    private int getTotalSearchPostCount(String query) {
        String sql = "SELECT COUNT(*) FROM posts p JOIN users u ON p.user_id = u.user_id " +
                "WHERE p.title LIKE ? OR p.content LIKE ? OR u.username LIKE ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + query + "%");
            stmt.setString(2, "%" + query + "%");
            stmt.setString(3, "%" + query + "%");
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private int getLikeCount(int postId) {
        String sql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private void handleSummarizePost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final String HUGGING_FACE_API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
        final String AUTH_TOKEN = "";
        String plainText = request.getParameter("plainText");
        if (plainText == null || plainText.isEmpty()) {
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Missing content!\"}");
            return;
        }
        if (plainText.length() < 600) {
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"The post content is too short to summarize. Please try again with longer text.\"}");
            return;
        }
        if (plainText.length() > 4300) {
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"error\": \"The post content is too long to summarize. Please try again with shorter text.\"}");
            return;
        }
        String cacheKey = "summary:" + plainText.hashCode();
        String cachedSummary = RedisCache.getFromCache(cacheKey);
        if (cachedSummary != null) {
            response.setContentType("application/json");
            response.getWriter().write(cachedSummary);
            return;
        }
        try {
            int maxLength = 180;
            int minLength = 100;
            boolean doSample = true;
            int numBeams = 2;
            String jsonPayload = "{"
                    + "\"inputs\": \"" + escapeJson(plainText) + "\","
                    + "\"parameters\": {"
                    + "\"max_length\": " + maxLength + ","
                    + "\"min_length\": " + minLength + ","
                    + "\"do_sample\": " + doSample + ","
                    + "\"num_beams\": " + numBeams
                    + "}"
                    + "}";
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(URI.create(HUGGING_FACE_API_URL))
                    .header("Authorization", "Bearer " + AUTH_TOKEN)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();
            HttpResponse<String> httpResponse = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (httpResponse.statusCode() != 200) {
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"API Error\"}");
                return;
            }
            String responseBody = httpResponse.body();
            RedisCache.storeInCache(cacheKey, responseBody, 3600);
            response.setContentType("application/json");
            response.getWriter().write(responseBody);
        } catch (Exception e) {
            e.printStackTrace();
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"An error occurred while processing the request\"}");
        }
    }

    private void savePostToDatabase(String title, String content, int userId) {
        title = truncateText(title, 100);
        content = truncateText(content, 65000);
        String sql = "INSERT INTO posts (title, content, user_id, timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, title);
            stmt.setString(2, content);
            stmt.setInt(3, userId);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getTotalPostCount() {
        String sql = "SELECT COUNT(*) FROM posts";
        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    private String likePost(int postId, int userId) {
        String checkPostSql = "SELECT 1 FROM posts WHERE post_id = ?";
        String checkLikeSql = "SELECT 1 FROM likes WHERE post_id = ? AND user_id = ?";
        String insertLikeSql = "INSERT INTO likes (post_id, user_id) VALUES (?, ?)";
        String deleteLikeSql = "DELETE FROM likes WHERE post_id = ? AND user_id = ?";
        String updateLikeCountSql = "UPDATE posts SET likes = (SELECT COUNT(*) FROM likes WHERE post_id = ?) WHERE post_id = ?";
        String countLikesSql = "SELECT COUNT(*) FROM likes WHERE post_id = ?";

        boolean liked = false;
        int likeCount = 0;

        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement checkPostStmt = conn.prepareStatement(checkPostSql)) {

            checkPostStmt.setInt(1, postId);
            ResultSet postRs = checkPostStmt.executeQuery();
            if (!postRs.next()) {
                return "{\"success\": false, \"message\": \"Post not found\"}";
            }

            try (PreparedStatement checkLikeStmt = conn.prepareStatement(checkLikeSql);
                    PreparedStatement countLikesStmt = conn.prepareStatement(countLikesSql);
                    PreparedStatement updateLikeCountStmt = conn.prepareStatement(updateLikeCountSql)) {

                checkLikeStmt.setInt(1, postId);
                checkLikeStmt.setInt(2, userId);
                ResultSet rs = checkLikeStmt.executeQuery();

                if (rs.next()) {
                    try (PreparedStatement deleteLikeStmt = conn.prepareStatement(deleteLikeSql)) {
                        deleteLikeStmt.setInt(1, postId);
                        deleteLikeStmt.setInt(2, userId);
                        deleteLikeStmt.executeUpdate();
                    }
                } else {
                    try (PreparedStatement insertLikeStmt = conn.prepareStatement(insertLikeSql)) {
                        insertLikeStmt.setInt(1, postId);
                        insertLikeStmt.setInt(2, userId);
                        insertLikeStmt.executeUpdate();
                        liked = true;
                    }
                }

                // Update the likes count in the posts table
                updateLikeCountStmt.setInt(1, postId);
                updateLikeCountStmt.setInt(2, postId);
                updateLikeCountStmt.executeUpdate();

                countLikesStmt.setInt(1, postId);
                ResultSet countRs = countLikesStmt.executeQuery();
                if (countRs.next()) {
                    likeCount = countRs.getInt(1);
                }

                return "{\"success\": true, \"likeCount\": " + likeCount + ", \"liked\": " + liked + "}";
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "{\"success\": false, \"message\": \"Database error\"}";
        }
    }

    private void saveCommentToDatabase(int postId, int userId, String comment) {
        String checkPostSql = "SELECT 1 FROM posts WHERE post_id = ?";
        String insertSql = "INSERT INTO comments (post_id, user_id, content, timestamp) VALUES (?, ?, ?, NOW())";

        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement(checkPostSql)) {

            checkStmt.setInt(1, postId);
            ResultSet rs = checkStmt.executeQuery();

            if (!rs.next()) {
                return;
            }

            try (PreparedStatement stmt = conn.prepareStatement(insertSql)) {
                stmt.setInt(1, postId);
                stmt.setInt(2, userId);
                stmt.setString(3, comment);
                stmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private List<Post> fetchPostsFromDatabase(int userId, int limit, int offset, String sort) {
        List<Post> posts = new ArrayList<>();
        String baseSql = "SELECT p.post_id, p.title, p.content, u.username, p.timestamp, " +
                "(SELECT COUNT(*) FROM likes WHERE post_id = p.post_id) AS likes, " +
                "(SELECT 1 FROM likes WHERE post_id = p.post_id AND user_id = ?) AS liked, " +
                "(CASE WHEN p.user_id = ? THEN 1 ELSE 0 END) AS is_author " +
                "FROM posts p " +
                "JOIN users u ON p.user_id = u.user_id ";

        String orderBy = "";
        switch (sort) {
            case "asc":
                orderBy = "ORDER BY p.timestamp ASC ";
                break;
            case "likes_desc":
                orderBy = "ORDER BY (SELECT COUNT(*) FROM likes WHERE post_id = p.post_id) DESC ";
                break;
            default: // "desc"
                orderBy = "ORDER BY p.timestamp DESC ";
                break;
        }

        String sql = baseSql + orderBy + "LIMIT ? OFFSET ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); // For the "liked" subquery
            stmt.setInt(2, userId); // For the "is_author" check
            stmt.setInt(3, limit); // Number of posts per page
            stmt.setInt(4, offset); // Offset for pagination
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int postId = rs.getInt("post_id");
                LocalDateTime postTime = rs.getTimestamp("timestamp").toLocalDateTime()
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        .toLocalDateTime();
                boolean liked = rs.getInt("liked") == 1;
                boolean isAuthor = rs.getInt("is_author") == 1;

                posts.add(new Post(
                        postId,
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getString("username"),
                        postTime.format(formatter),
                        rs.getInt("likes"),
                        liked,
                        isAuthor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return posts;
    }

    private List<Comment> fetchCommentsFromDatabase(int postId, int userId, int offset, int limit) {
        List<Comment> comments = new ArrayList<>();
        String sql = "SELECT c.comment_id, c.content, u.username, c.timestamp, " +
                "(CASE WHEN c.user_id = ? THEN 1 ELSE 0 END) AS is_author " +
                "FROM comments c " +
                "JOIN users u ON c.user_id = u.user_id " +
                "WHERE c.post_id = ? " +
                "ORDER BY c.comment_id ASC " +
                "LIMIT ? OFFSET ?";

        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId); // For the "is_author" check
            stmt.setInt(2, postId); // Post ID
            stmt.setInt(3, limit); // Number of comments per page
            stmt.setInt(4, offset); // Offset for pagination
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                LocalDateTime commentTime = rs.getTimestamp("timestamp").toLocalDateTime()
                        .atZone(ZoneId.of("UTC"))
                        .withZoneSameInstant(ZoneId.of("Asia/Kolkata"))
                        .toLocalDateTime();

                boolean isAuthor = rs.getInt("is_author") == 1;

                comments.add(new Comment(
                        rs.getInt("comment_id"),
                        postId,
                        rs.getString("username"),
                        rs.getString("content"),
                        commentTime.format(formatter),
                        isAuthor));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return comments;
    }

    private String sanitizeInput(String input) {
        return input == null ? ""
                : input.replace("&", "&amp;").replace("<", "&lt;")
                        .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;").trim();
    }

    private String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n")
                .replace("\r", "\\r").replace("\t", "\\t");
    }

    private String truncateText(String text, int maxLength) {
        if (text.length() > maxLength) {
            return text.substring(0, maxLength - 3) + "...";
        }
        return text;
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String resourceType = request.getServletPath();
        String fullPath = request.getPathInfo();
        if (fullPath == null || fullPath.isEmpty()) {
            response.setStatus(400); // Bad Request
            return;
        }
        int resourceId;
        try {
            resourceId = Integer.parseInt(fullPath.substring(1));
        } catch (NumberFormatException e) {
            response.setStatus(400); // Invalid resource ID
            return;
        }
        if ("/deletePost".equals(resourceType)) {
            handleDeletePost(request, response, resourceId);
        } else if ("/deleteComment".equals(resourceType)) {
            handleDeleteComment(request, response, resourceId);
        } else {
            response.setStatus(404); // Unsupported resource type
            return;
        }
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = session != null ? (User) session.getAttribute("user") : null;

        if (loggedInUser == null) {
            response.setStatus(401); // Unauthorized
            return;
        }

        String resourceType = request.getServletPath(); // e.g., "/editPost"
        String fullPath = request.getPathInfo(); // e.g., "/123"

        if (fullPath == null || fullPath.isEmpty()) {
            response.setStatus(400); // Bad Request
            return;
        }

        int resourceId;
        try {
            resourceId = Integer.parseInt(fullPath.substring(1)); // Remove the leading "/"
        } catch (NumberFormatException e) {
            response.setStatus(400); // Invalid resource ID
            return;
        }

        if ("/editPost".equals(resourceType)) {
            handleEditPost(request, response, loggedInUser, resourceId);
        } else if ("/editComment".equals(resourceType)) {
            handleEditComment(request, response, loggedInUser, resourceId);
        } else {
            response.setStatus(404); // Unsupported resource type
            return;
        }
    }

    private void handleEditComment(HttpServletRequest request, HttpServletResponse response,
            User loggedInUser, int commentId) throws ServletException, IOException {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        String content = "";
        try {
            String[] keyValuePairs = requestBody.toString().split("&");
            for (String pair : keyValuePairs) {
                String[] entry = pair.split("=");
                if (entry.length == 2 && "content".equals(entry[0])) {
                    content = java.net.URLDecoder.decode(entry[1], "UTF-8").trim();
                }
            }
        } catch (Exception e) {
            response.setStatus(400); // Invalid request body
            return;
        }

        if (content.isEmpty()) {
            response.setStatus(400); // Content cannot be empty
            return;
        }
        content = sanitizeInput(content);
        content = truncateText(content, 10000); // Truncate content to max length

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            // Check if the comment exists and belongs to the logged-in user
            String checkCommentSql = "SELECT user_id FROM comments WHERE comment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkCommentSql)) {
                stmt.setInt(1, commentId);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    response.setStatus(404); // Comment not found
                    return;
                }

                int commentUserId = rs.getInt("user_id");
                if (commentUserId != loggedInUser.getUserID()) {
                    response.setStatus(403); // Forbidden
                    response.setContentType("application/json");
                    response.getWriter().write("{\"redirectUrl\": \"https://nicetrymate.netlify.app/\"}");
                    return;
                }
            }

            // Update the comment
            String updateCommentSql = "UPDATE comments SET content = ? WHERE comment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updateCommentSql)) {
                stmt.setString(1, content);
                stmt.setInt(2, commentId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    response.setStatus(500); // Internal Server Error
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
            return;
        }

        response.setStatus(204); // No Content
    }

    private void handleDeleteComment(HttpServletRequest request, HttpServletResponse response,
            int commentId) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = session != null ? (User) session.getAttribute("user") : null;

        int userId;
        if (loggedInUser != null) {
            userId = loggedInUser.getUserID();
        } else {
            String userIdParam = request.getParameter("user_id");
            if (userIdParam == null || userIdParam.isEmpty()) {
                response.setStatus(401); // Unauthorized
                return;
            }
            try {
                userId = Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                response.setStatus(400); // Invalid user ID
                return;
            }
        }

        boolean commentExists = false;
        boolean isAuthorized = false;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            // Check if the comment exists and belongs to the logged-in user
            String checkCommentSql = "SELECT user_id FROM comments WHERE comment_id = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(checkCommentSql)) {
                stmt.setInt(1, commentId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    commentExists = true;
                    isAuthorized = rs.getInt("user_id") == userId;
                }
            }

            if (!commentExists) {
                response.setStatus(404); // Comment not found
                return;
            }

            if (!isAuthorized) {
                response.setStatus(403); // Forbidden
                response.setContentType("application/json");
                response.getWriter().write("{\"redirectUrl\": \"https://nicetrymate.netlify.app/\"}");
                return;
            }

            // Delete the comment
            String deleteCommentSql = "DELETE FROM comments WHERE comment_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deleteCommentSql)) {
                stmt.setInt(1, commentId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    response.setStatus(500); // Internal Server Error
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
            return;
        }

        response.setStatus(204); // No Content
    }

    private void handleEditPost(HttpServletRequest request, HttpServletResponse response,
            User loggedInUser, int postId) throws ServletException, IOException {
        StringBuilder requestBody = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                requestBody.append(line);
            }
        }

        String title = "";
        String content = "";
        try {
            String[] keyValuePairs = requestBody.toString().split("&");
            for (String pair : keyValuePairs) {
                String[] entry = pair.split("=");
                if (entry.length == 2) {
                    String key = entry[0];
                    String value = java.net.URLDecoder.decode(entry[1], "UTF-8");
                    if ("title".equals(key)) {
                        title = sanitizeInput(value.trim());
                    } else if ("content".equals(key)) {
                        content = value.trim();
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(400); // Invalid request body
            return;
        }

        if (title.isEmpty() || content.isEmpty()) {
            response.setStatus(400); // Title or content cannot be empty
            return;
        }

        title = truncateText(title, 100);
        content = truncateText(content, 65000);

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            // Check if the post exists and belongs to the logged-in user
            String checkPostSql = "SELECT user_id FROM posts WHERE post_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(checkPostSql)) {
                stmt.setInt(1, postId);
                ResultSet rs = stmt.executeQuery();
                if (!rs.next()) {
                    response.setStatus(404); // Post not found
                    return;
                }

                int postUserId = rs.getInt("user_id");
                if (postUserId != loggedInUser.getUserID()) {
                    response.setStatus(403); // Forbidden
                    response.setContentType("application/json");
                    response.getWriter().write("{\"redirectUrl\": \"https://nicetrymate.netlify.app/\"}");
                    return;
                }
            }

            // Update the post
            String updatePostSql = "UPDATE posts SET title = ?, content = ? WHERE post_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(updatePostSql)) {
                stmt.setString(1, title);
                stmt.setString(2, content);
                stmt.setInt(3, postId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    response.setStatus(500); // Internal Server Error
                    return;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
            return;
        }
        response.setStatus(204); // No Content
    }

    private void handleGetPostDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullPath = request.getPathInfo();
        if (fullPath == null || fullPath.isEmpty()) {
            response.setStatus(400); // Bad Request
            return;
        }

        String[] pathParts = fullPath.split("/");
        int postId;
        try {
            if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
                postId = Integer.parseInt(pathParts[1]);
            } else {
                response.setStatus(400); // Bad Request
                return;
            }
        } catch (NumberFormatException e) {
            response.setStatus(400); // Invalid postId
            return;
        }

        String sql = "SELECT title, content FROM posts WHERE post_id = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, postId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                response.setStatus(404); // Post not found
                return;
            }

            String jsonResponse = String.format(
                    "{\"title\":\"%s\", \"content\":\"%s\"}",
                    escapeJson(rs.getString("title")),
                    escapeJson(rs.getString("content")));

            response.setContentType("application/json");
            response.getWriter().write(jsonResponse);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
        }
    }

    private void handleGetCommentDetails(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String fullPath = request.getPathInfo();
        if (fullPath == null || fullPath.isEmpty()) {
            response.setStatus(400); // Bad Request
            return;
        }

        String[] pathParts = fullPath.split("/");
        int commentId;
        try {
            if (pathParts.length == 2 && !pathParts[1].isEmpty()) {
                commentId = Integer.parseInt(pathParts[1]);
            } else {
                response.setStatus(400); // Bad Request
                return;
            }
        } catch (NumberFormatException e) {
            response.setStatus(400); // Invalid comment ID
            return;
        }

        String sql = "SELECT content, post_id FROM comments WHERE comment_id = ?";
        try (Connection conn = DatabaseConnectionPool.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, commentId);
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                response.setStatus(404); // Comment not found
                return;
            }

            String content = rs.getString("content");
            int postId = rs.getInt("post_id");

            String jsonResponse = String.format(
                    "{\"content\":\"%s\", \"postId\":%d}",
                    escapeJson(content),
                    postId);

            response.setContentType("application/json");
            response.getWriter().write(jsonResponse);
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(500); // Internal Server Error
        }
    }

    private void handleDeletePost(HttpServletRequest request, HttpServletResponse response, int postId)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User loggedInUser = session != null ? (User) session.getAttribute("user") : null;

        int userId;
        if (loggedInUser != null) {
            userId = loggedInUser.getUserID();
        } else {
            String userIdParam = request.getParameter("user_id");
            if (userIdParam == null || userIdParam.isEmpty()) {
                response.setStatus(401);
                return;
            }
            try {
                userId = Integer.parseInt(userIdParam);
            } catch (NumberFormatException e) {
                response.setStatus(400);
                return;
            }
        }

        boolean postExists = false;
        boolean isAuthorized = false;

        try (Connection conn = DatabaseConnectionPool.getConnection()) {
            String checkPostSql = "SELECT user_id FROM posts WHERE post_id = ? FOR UPDATE";
            try (PreparedStatement stmt = conn.prepareStatement(checkPostSql)) {
                stmt.setInt(1, postId);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    postExists = true;
                    isAuthorized = rs.getInt("user_id") == userId;
                }
            }

            if (!postExists) {
                response.setStatus(404);
                return;
            }

            if (!isAuthorized) {
                response.setStatus(403);
                response.setContentType("application/json");
                response.getWriter().write("{\"redirectUrl\": \"https://nicetrymate.netlify.app/\"}");
                return;
            }

            String deletePostSql = "DELETE FROM posts WHERE post_id = ?";
            try (PreparedStatement stmt = conn.prepareStatement(deletePostSql)) {
                stmt.setInt(1, postId);
                int rowsAffected = stmt.executeUpdate();

                if (rowsAffected == 0) {
                    response.setStatus(500);
                    return;
                }
            }
        } catch (SQLException e) {
            response.setStatus(500);
            return;
        }

        response.setStatus(204);
    }
}
