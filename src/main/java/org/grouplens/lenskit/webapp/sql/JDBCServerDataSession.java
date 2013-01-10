package org.grouplens.lenskit.webapp.sql;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.grouplens.lenskit.data.sql.JDBCDataSession;

public class JDBCServerDataSession extends JDBCDataSession {
	
    private ServerSQLStatementFactory factory;
    private PreparedStatement addEventStatement;
    private PreparedStatement tableInitStatement;
    private PreparedStatement deleteEventStatement;
    private PreparedStatement getEventRevIdStatement;
    private Connection connection;
    private boolean closeConnection;
	
	public JDBCServerDataSession(Connection dbc, ServerSQLStatementFactory sfac) {
        super(dbc, sfac);
		connection = dbc;
        factory = sfac;
        closeConnection = true;
    }
    
    public JDBCServerDataSession(Connection dbc, ServerSQLStatementFactory sfac, boolean close) {
    	super(dbc, sfac, close);
    	connection = dbc;
    	factory = sfac;
    	closeConnection = close;
    }
    
    public PreparedStatement addEventStatement() throws SQLException {
    	if (addEventStatement == null)
    		addEventStatement = factory.prepareAddEvent(connection);
    	return addEventStatement;
    }
    
    public PreparedStatement prepareTableInitStatement() throws SQLException {
    	if (tableInitStatement == null)
    		tableInitStatement = factory.prepareTableInit(connection);
    	return tableInitStatement;
    }
    
    public PreparedStatement deleteEventStatement() throws SQLException {
    	if (deleteEventStatement == null)
    		deleteEventStatement = factory.prepareDeleteEvent(connection);
    	return deleteEventStatement;
    }

	public PreparedStatement getEventRevIdStatement() throws SQLException {
		if (getEventRevIdStatement == null)
			getEventRevIdStatement = factory.prepareEventRevId(connection);
		return getEventRevIdStatement;
	}
	
	private boolean closeStatement(PreparedStatement ps) {
		try {
			if (ps != null) {
				ps.close();
			}
			return true;
		} catch (SQLException e) {
			return false;
		}
	}
	
	public void close() throws IOException {
		boolean failed = false;
		try {
			super.close();
			failed = failed || closeStatement(addEventStatement);
			failed = failed || closeStatement(tableInitStatement);
			failed = failed || closeStatement(deleteEventStatement);
			failed = failed || closeStatement(getEventRevIdStatement);
			if (closeConnection) {
				connection.close();
			}
		} catch (SQLException e) {
			throw new IOException(e);
		}
		if (failed) {
			throw new IOException("Error closing SQL Statement");
		}
	}
}