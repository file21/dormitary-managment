package edu.aau.dorm.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

/**
 * Central DB provider (HikariCP).
 * Configure using environment variables: DB_URL, DB_USER, DB_PASS.
 */
public final class Db {
    private static HikariDataSource ds;

    private Db() {}

    public static DataSource dataSource() {
        if (ds == null) {
            HikariConfig cfg = new HikariConfig();

            cfg.setJdbcUrl(System.getenv().getOrDefault("DB_URL", "jdbc:postgresql://localhost:5432/dormdb"));
            cfg.setUsername(System.getenv().getOrDefault("DB_USER", "postgres"));
            cfg.setPassword(System.getenv().getOrDefault("DB_PASS", "postgres"));

            cfg.setMaximumPoolSize(10);
            cfg.setMinimumIdle(2);
            cfg.setPoolName("DormPool");

            ds = new HikariDataSource(cfg);
        }
        return ds;
    }
}
