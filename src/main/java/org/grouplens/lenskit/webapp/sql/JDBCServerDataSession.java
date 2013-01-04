package org.grouplens.lenskit.webapp.sql;

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
	
	public JDBCServerDataSession(Connection dbc, ServerSQLStatementFactory sfac) {
        super(dbc, sfac, true);
        connection = dbc;
        factory = sfac;
    }
    
    public JDBCServerDataSession(Connection dbc, ServerSQLStatementFactory sfac, boolean close) {
    	super(dbc, sfac, close);
    	connection = dbc;
    	factory = sfac;
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
}