import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebListener 
public class DatabaseConnectionPool implements ServletContextListener {
    private static volatile HikariDataSource dataSource;
    private static final Logger LOGGER = Logger.getLogger(DatabaseConnectionPool.class.getName());
    private static int failedConnectionCount = 0;
    private static final int MAX_FAILURES_BEFORE_RESET = 5;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        initializeDataSource();
        // Schedule automatic reset every 6 hours
        scheduler.scheduleAtFixedRate(() -> {
            LOGGER.warning("⏳ Scheduled HikariCP reset triggered...");
            resetPool();
        }, 6, 6, TimeUnit.HOURS);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        closePool();
    }

    private static synchronized void initializeDataSource() {
        if (dataSource != null) return;

        try (InputStream inputStream = Thread.currentThread()
                .getContextClassLoader().getResourceAsStream("hikari.properties")) {
            if (inputStream == null) {
                throw new IllegalArgumentException("Cannot find hikari.properties");
            }

            Properties properties = new Properties();
            properties.load(inputStream);
            HikariConfig config = new HikariConfig(properties);
            dataSource = new HikariDataSource(config);
            LOGGER.info("✅ HikariCP Connection Pool Initialized Successfully!");
        } catch (IOException e) {
            throw new RuntimeException("Failed to load HikariCP configuration", e);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize HikariCP", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new IllegalStateException("DataSource is not initialized");
        }
        try {
            Connection connection = dataSource.getConnection();
            failedConnectionCount = 0;
            return connection;
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "⚠ Database connection failed: " + e.getMessage(), e);
            if (++failedConnectionCount >= MAX_FAILURES_BEFORE_RESET) {
                LOGGER.warning("🔥 Too many failed connections! Resetting HikariCP...");
                resetPool();
            }
            throw e;
        }
    }

    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
            LOGGER.info("✅ Connection Pool Closed.");
        }

        // Shut down MySQL abandoned connection cleanup thread
        try {
            Class.forName("com.mysql.cj.jdbc.AbandonedConnectionCleanupThread");
            com.mysql.cj.jdbc.AbandonedConnectionCleanupThread.uncheckedShutdown();
            LOGGER.info("✅ MySQL Cleanup Thread Shut Down.");
        } catch (ClassNotFoundException e) {
            LOGGER.warning("⚠ MySQL Cleanup Thread not found. Skipping shutdown.");
        }

        // Shut down the scheduled executor service
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
            LOGGER.info("✅ Scheduled Executor Service Shut Down.");
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
            LOGGER.warning("⚠ Executor Service Interrupted During Shutdown.");
        }
    }

    public static synchronized void resetPool() {
        LOGGER.warning("🔄 Resetting HikariCP Connection Pool...");
        closePool();
        initializeDataSource();
        failedConnectionCount = 0;
        LOGGER.info("✅ HikariCP Pool Reset Complete.");
    }
}