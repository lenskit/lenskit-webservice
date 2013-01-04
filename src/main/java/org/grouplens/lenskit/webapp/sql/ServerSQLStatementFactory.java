package org.grouplens.lenskit.webapp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.grouplens.lenskit.data.sql.SQLStatementFactory;

public interface ServerSQLStatementFactory extends SQLStatementFactory {
	
    /**
     * Prepare a statement to satisfy 
     * {@link org.grouplens.lenskit.webapp.ServerDataAccessObject#addEvent(long) ServerDataAccessObject.addEvent(long)}
     * The statement should insert a single row containing event ID, user ID,
     * item ID, rating, timestamp, and revision ID.
     * 
     * @return A <tt>PreparedStatement</tt> inserting the specified event.
     */
    PreparedStatement prepareAddEvent(Connection dbc) throws SQLException;
    
    PreparedStatement prepareTableInit(Connection dbc) throws SQLException;
    
    PreparedStatement prepareDeleteEvent(Connection dbc) throws SQLException;

	PreparedStatement prepareEventRevId(Connection dbc) throws SQLException;
}
