package dorm.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Central DB provider (HikariCP) for MySQL.
 * Configure using environment variables: DB_URL, DB_USER, DB_PASS.
 * Default: jdbc:mysql://localhost:3306/dormdb
 */
public final class Db {
    private static HikariDataSource ds;

    private Db() {}

    public static DataSource dataSource() {
        if (ds == null) {
            HikariConfig cfg = new HikariConfig();

            // MySQL connection URL with proper timezone and SSL settings
            cfg.setJdbcUrl(System.getenv().getOrDefault("DB_URL", 
                    "jdbc:mysql://localhost:3306/dormdb?serverTimezone=UTC&useSSL=false"));
            cfg.setUsername(System.getenv().getOrDefault("DB_USER", "root"));
            cfg.setPassword(System.getenv().getOrDefault("DB_PASS", ""));

            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setPoolName("DormPool");
            cfg.setDriverClassName("com.mysql.cj.jdbc.Driver");

            ds = new HikariDataSource(cfg);
        }
        return ds;
    }
}
