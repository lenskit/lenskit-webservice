package org.grouplens.lenskit.webapp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;

public class BasicServerSQLStatementFactory extends BasicSQLStatementFactory implements ServerSQLStatementFactory {
	
	private String revisionColumn = "revision";
	public static final int ID_COLUMN_INDEX = 1;
	public static final int USER_COLUMN_INDEX = 2;
	public static final int ITEM_COLUMN_INDEX = 3;
	public static final int RATING_COLUMN_INDEX = 4;
	public static final int TIMESTAMP_COLUMN_INDEX = 5;
	public static final int REVISION_COLUMN_INDEX = 6;
	
	public BasicServerSQLStatementFactory() {
		super();
	}
	
	@Override
	public PreparedStatement prepareAddEvent(Connection dbc) throws SQLException {
		String SQL = "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)";
		String stmt = String.format(SQL, getTableName(), getIdColumn(), getUserColumn(), getItemColumn(),
				getRatingColumn(), getTimestampColumn(), getRevisionColumn());
        return dbc.prepareStatement(stmt);
	}
	
	@Override
	public PreparedStatement prepareTableInit(Connection dbc) throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS %s\n" +
        		"(%s BIGINT,\n" +
        		"%s BIGINT,\n" +
				"%s BIGINT,\n" +
				"%s REAL,\n" +
				"%s BIGINT,\n" +
				"%s TEXT,\n" +
				"primary key (%s));";
        String stmt = String.format(SQL, getTableName(), getIdColumn(), getUserColumn(),
        		getItemColumn(), getRatingColumn(), getTimestampColumn(),
        		getRevisionColumn(), getIdColumn());
		return dbc.prepareStatement(stmt);		
	}
	
	@Override
	public PreparedStatement prepareDeleteEvent(Connection dbc) throws SQLException {
		String SQL = "DELETE FROM %s WHERE %s=?";
		String stmt = String.format(SQL, getTableName(), getIdColumn());
		return dbc.prepareStatement(stmt);
	}

	@Override
	public PreparedStatement prepareEventRevId(Connection dbc) throws SQLException {
		String SQL = "SELECT %s FROM %s WHERE %s=?";
		String stmt = String.format(SQL, getRevisionColumn(), getTableName(), getIdColumn());
		return dbc.prepareStatement(stmt);
	}
	
	public String getRevisionColumn() {
		return revisionColumn;
	}
}
