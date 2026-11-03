package com.talentiq.dao;

import com.talentiq.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Base Data Access Object providing boilerplate JDBC handling and connection acquisition.
 */
public abstract class BaseDAO {

    protected final Logger logger = Logger.getLogger(getClass().getName());

    protected Connection getConnection() throws SQLException {
        return DatabaseConfig.getConnection();
    }

    protected void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                logger.log(Level.FINEST, "Failed to close resource quietly", e);
            }
        }
    }

    protected void closeQuietly(Connection conn, Statement stmt, ResultSet rs) {
        closeQuietly(rs);
        closeQuietly(stmt);
        closeQuietly(conn);
    }
}

