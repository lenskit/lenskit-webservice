package org.grouplens.lenskit.webapp.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.grouplens.lenskit.data.sql.BasicSQLStatementFactory;

public class BasicServerSQLStatementFactory extends BasicSQLStatementFactory implements ServerSQLStatementFactory {
	
	private String eventRevisionColumn = "revision";
	public static final int EVENT_TABLE_ID_COLUMN_INDEX = 1;
	public static final int EVENT_TABLE_USER_COLUMN_INDEX = 2;
	public static final int EVENT_TABLE_ITEM_COLUMN_INDEX = 3;
	public static final int EVENT_TABLE_RATING_COLUMN_INDEX = 4;
	public static final int EVENT_TABLE_TIMESTAMP_COLUMN_INDEX = 5;
	public static final int EVENT_TABLE_REVISION_COLUMN_INDEX = 6;
	
	private String itemTableName = "items";
	private String itemIdColumn = "id";
	private String itemNameColumn = "name";
	private String itemTagsColumn = "tags";
	private String itemRevisionColumn = "revision";
	
	public static final int ITEM_TABLE_ID_COLUMN_INDEX = 1;
	public static final int ITEM_TABLE_NAME_COLUMN_INDEX = 2;
	public static final int ITEM_TABLE_TAG_COLUMN_INDEX = 3;
	public static final int ITEM_TABLE_REVISION_COLUMN_INDEX = 4;
	
	public BasicServerSQLStatementFactory() {
		super();
	}
	
	@Override
	public PreparedStatement prepareAddEvent(Connection dbc) throws SQLException {
		String SQL = "INSERT INTO %s (%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?)";
		String stmt = String.format(SQL, getTableName(), getIdColumn(), getUserColumn(), getItemColumn(),
				getRatingColumn(), getTimestampColumn(), getEventRevisionColumn());
        return dbc.prepareStatement(stmt);
	}
	
	@Override
	public PreparedStatement prepareTableInit(Connection dbc) throws SQLException {
        String SQL = "CREATE TABLE IF NOT EXISTS %s (%s BIGINT, %s BIGINT, %s BIGINT," +
				"%s REAL %s BIGINT, %s TEXT, primary key (%s));\n" +
        		
				"CREATE TABLE IF NOT EXISTS %s (%s BIGINT, %s TEXT, %s TEXT, %s TEXT, primary key (%s));";
        
        String stmt = String.format(SQL, getTableName(), getIdColumn(), getUserColumn(),
        		getItemColumn(), getRatingColumn(), getTimestampColumn(),
        		getEventRevisionColumn(), getIdColumn(),
        		
        		getItemTableName(), getItemIdColumn(), getItemNameColumn(), getItemTagsColumn(),
        			getItemRevisionColumn());
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
		String stmt = String.format(SQL, getEventRevisionColumn(), getTableName(), getIdColumn());
		return dbc.prepareStatement(stmt);
	}
	
	@Override
	public PreparedStatement prepareAddItem(Connection dbc) throws SQLException {
		String SQL = "INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)";
		String stmt = String.format(SQL, getItemTableName(), getItemIdColumn(), getItemNameColumn(),
				getItemTagsColumn(), getItemRevisionColumn());
		return dbc.prepareStatement(stmt);
	}
	
	@Override
	public PreparedStatement prepareItemMetadata(Connection dbc) throws SQLException {
		String SQL = "SELECT FROM %s WHERE %s=?";
		String stmt = String.format(SQL, getItemTableName(), getItemIdColumn());
		return dbc.prepareStatement(stmt);
	}
	
	public String getEventRevisionColumn() {
		return eventRevisionColumn;
	}
	
	public String getItemIdColumn() {
		return itemIdColumn;
	}
	
	public String getItemNameColumn() {
		return itemNameColumn;
	}
	
	public String getItemTagsColumn() {
		return itemTagsColumn;
	}
	
	public String getItemTableName() {
		return itemTableName;
	}
	
	public String getItemRevisionColumn() {
		return itemRevisionColumn;
	}
}
