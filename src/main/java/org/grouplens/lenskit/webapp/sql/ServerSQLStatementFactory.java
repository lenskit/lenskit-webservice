package org.grouplens.lenskit.webapp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.grouplens.lenskit.data.sql.SQLStatementFactory;

public interface ServerSQLStatementFactory extends SQLStatementFactory {
	
    /**
     * Construct an SQL statement that will add a new event to an existing table.
     * @param dbc The JDBC database connection.
     * @return A <tt>PreparedStatement</tt> to add a new event.
     * @throws SQLException
     */
    PreparedStatement prepareAddEvent(Connection dbc) throws SQLException;
    
    /**
     * Construct an SQL statement that initializes a table to store rating data.
     * @param dbc The database connection.
     * @return A <tt>PreparedStatement</tt> to initialize a rating table.
     * @throws SQLException
     */
    PreparedStatement prepareTableInit(Connection dbc) throws SQLException;
    
    /**
     * Construct an SQL statement that removes a rating from an existing table.
     * @param dbc The database connection.
     * @return A <tt>PreparedStatement </tt> to remove a rating from a table.
     * @throws SQLException
     */
    PreparedStatement prepareDeleteEvent(Connection dbc) throws SQLException;

	/**
	 * Construct an SQL statement that retrieves the revision ID of a rating
	 * in an existing table.
	 * @param dbc The database connection.
	 * @return A <tt>PreparedStatement</tt> to retrieve a revision ID.
	 * @throws SQLException
	 */
    PreparedStatement prepareEventRevId(Connection dbc) throws SQLException;
    
    /**
     * Construct an SQL statement that will add a new item to an existing table.
     * @param dbc The JDBC database connection.
     * @return A <tt>PreparedStatement</tt> to add a new item.
     * @throws SQLException
     */
    PreparedStatement prepareAddItem(Connection dbc) throws SQLException;
    
    /**
     * Construct an SQL statement that will retrieve item metadata from an existing table.
     * @param dbc The JDBC database connection.
     * @return A <tt>PreparedStatement</tt> to retrieve item metadata.
     * @throws SQLException
     */
    PreparedStatement prepareItemMetadata(Connection dbc) throws SQLException;
}
