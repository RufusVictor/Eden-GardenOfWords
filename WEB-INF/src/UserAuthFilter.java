import java.io.IOException;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@WebFilter("/*")
public class UserAuthFilter implements Filter {

    private static final long WINDOW_SIZE_MS = 60_000; // 1 minute in milliseconds
    private static final long CLIENT_EXPIRY_MS = 1800_000; // 30 minutes - when to clean up stale client data

    // Map to store client request data with last activity timestamp for memory
    // management
    private static class ClientData {
        final Map<String, Deque<Long>> pathRequestTimestamps = new ConcurrentHashMap<>();
        long lastActivityTime;

        ClientData() {
            this.lastActivityTime = System.currentTimeMillis();
        }

        void updateLastActivity() {
            this.lastActivityTime = System.currentTimeMillis();
        }
    }

    // Shared map to store request timestamps for clients without sessions
    private final Map<String, ClientData> clientRequestData = new ConcurrentHashMap<>();
    private final Map<String, Integer> pathRateLimits = new ConcurrentHashMap<>();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // Initialize rate limits for specific paths
        pathRateLimits.put("/submitPost", 30);
        pathRateLimits.put("/getPosts", 60);
        pathRateLimits.put("/likePost", 50);
        pathRateLimits.put("/getLikeCount", 40);
        pathRateLimits.put("/searchPosts", 50);
        pathRateLimits.put("/commentPost", 30);
        pathRateLimits.put("/getComments", 60);
        pathRateLimits.put("/deletePost", 20);
        pathRateLimits.put("/deleteComment", 20);
        pathRateLimits.put("/editPost", 25);
        pathRateLimits.put("/editComment", 25);
        pathRateLimits.put("/getPostDetails", 50);
        pathRateLimits.put("/getCommentDetails", 50);
        pathRateLimits.put("/summarizePost", 20);
        pathRateLimits.put("/register", 10);
        pathRateLimits.put("/login", 20);
        pathRateLimits.put("/logout", 20);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        // Set headers to prevent caching
        res.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        res.setHeader("Pragma", "no-cache");
        res.setHeader("Expires", "0");

        String path = req.getServletPath();
        boolean isLoggedIn = (session != null && session.getAttribute("user") != null);

        // Define public pages that bypass authentication
        boolean isPublicPage = path.equals("/login.html") || path.equals("/register.html") ||
                path.equals("/register") || path.equals("/login") ||
                path.startsWith("/assets/") || path.equals("/") || path.equals("/code429.html");

        // Redirect unauthenticated users to the login page for non-public pages
        if (!isLoggedIn && !isPublicPage) {
            res.sendRedirect("login.html");
            return;
        }

        // Check if the request is for a static file (exclude from rate limiting)
        boolean isStaticFile = path.endsWith(".html") ||
                path.endsWith(".css") ||
                path.endsWith(".js") ||
                path.endsWith(".png") ||
                path.endsWith(".jpg") ||
                path.endsWith(".webp") ||
                path.endsWith(".ico");

        if (!isStaticFile) {
            String clientIdentifier = getClientIdentifier(req);
            // Get the rate limit for the current path
            int maxRequestsPerMinute = pathRateLimits.getOrDefault(path, 60); // Default to 60 if not specified
            if (!checkRateLimit(session, clientIdentifier, path, maxRequestsPerMinute)) {
                // Rate limit exceeded - Return 429 status
                res.setStatus(429);
                res.setContentType("application/json");
                res.getWriter().write("{\"message\": \"Too many requests. Please try again later.\"}");
                return;
            }
        }

        // Cleanup stale entries occasionally
        if (Math.random() < 0.01) {
            cleanupStaleEntries();
        }

        // Proceed with the filter chain (for both static and dynamic requests)
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // Clear client data map to free memory
        clientRequestData.clear();
    }

    /**
     * Gets a more accurate client identifier, handling proxies better
     */
    private String getClientIdentifier(HttpServletRequest request) {
        String clientIp = null;

        // Check headers that might contain the real client IP when behind a proxy
        String[] headerNames = {
                "X-Forwarded-For",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_X_FORWARDED_FOR",
                "HTTP_CLIENT_IP"
        };

        for (String header : headerNames) {
            String value = request.getHeader(header);
            if (value != null && value.length() > 0 && !"unknown".equalsIgnoreCase(value)) {
                // X-Forwarded-For might contain multiple IPs (client, proxy1, proxy2...) - use
                // the first one
                if (header.equals("X-Forwarded-For")) {
                    int commaIndex = value.indexOf(',');
                    if (commaIndex > 0) {
                        clientIp = value.substring(0, commaIndex).trim();
                    } else {
                        clientIp = value.trim();
                    }
                } else {
                    clientIp = value.trim();
                }

                // Validate the extracted IP address
                if (clientIp != null && isValidIpAddress(clientIp)) {
                    break; // Use this valid IP and stop checking further headers
                } else {
                    clientIp = null; // Invalid IP, continue checking other headers
                }
            }
        }

        // If no valid IP was found in headers, fallback to remote address
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = "UNKNOWN_IP";
        }
        HttpSession session = request.getSession(false);
        if (session != null) {
            User loggedInUser = (User) session.getAttribute("user");
            if (loggedInUser != null) {
                int userID = loggedInUser.getUserID();
                if (userID > 0) {
                    return String.valueOf(userID);
                }
            }
        }
        return clientIp;
    }

    /**
     * Checks if the request exceeds the rate limit and records the current request
     * 
     * return true if request is allowed, false if rate limit is exceeded
     */
    @SuppressWarnings("unchecked")
    private boolean checkRateLimit(HttpSession session, String clientIdentifier, String path,
            int maxRequestsPerMinute) {
        long now = System.currentTimeMillis();
        ClientData clientData = clientRequestData.compute(clientIdentifier, (key, existing) -> {
            if (existing == null) {
                return new ClientData();
            }
            existing.updateLastActivity();
            return existing;
        });

        // Get or create the deque for the specific path
        Deque<Long> timestamps = clientData.pathRequestTimestamps.computeIfAbsent(path, k -> new LinkedList<>());

        synchronized (timestamps) {
            return checkAndUpdateTimestamps(timestamps, now, maxRequestsPerMinute);
        }
    }

    /**
     * Core rate limiting logic: checks and updates the sliding window
     * 
     * return true if request is allowed, false if rate limit exceeded
     */
    private boolean checkAndUpdateTimestamps(Deque<Long> timestamps, long now, int maxRequestsPerMinute) {
        // Remove timestamps outside the current window (older than WINDOW_SIZE_MS)
        long windowStart = now - WINDOW_SIZE_MS;
        while (!timestamps.isEmpty() && timestamps.peekFirst() < windowStart) {
            timestamps.pollFirst();
        }

        // Check if rate limit would be exceeded
        if (timestamps.size() >= maxRequestsPerMinute) {
            return false; // Rate limit exceeded
        }

        // Record this request time
        timestamps.addLast(now);
        return true; // Request is allowed
    }

    /**
     * Cleans up client data that hasn't been active recently to prevent memory
     * leaks
     */
    private void cleanupStaleEntries() {
        long now = System.currentTimeMillis();
        long staleThreshold = now - CLIENT_EXPIRY_MS;
        clientRequestData.entrySet().removeIf(entry -> {
            ClientData clientData = entry.getValue();
            // Remove stale paths
            clientData.pathRequestTimestamps.entrySet().removeIf(pathEntry -> {
                Deque<Long> timestamps = pathEntry.getValue();
                synchronized (timestamps) {
                    while (!timestamps.isEmpty() && timestamps.peekFirst() < staleThreshold) {
                        timestamps.pollFirst();
                    }
                    return timestamps.isEmpty();
                }
            });
            // Remove the client if all paths are stale
            return clientData.pathRequestTimestamps.isEmpty() && clientData.lastActivityTime < staleThreshold;
        });
    }

    // Check valid ip
    private boolean isValidIpAddress(String ip) {
        return ip.matches("^((\\d{1,3}\\.){3}\\d{1,3})$");
    }
}