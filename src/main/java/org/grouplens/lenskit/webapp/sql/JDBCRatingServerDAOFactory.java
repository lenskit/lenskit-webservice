package org.grouplens.lenskit.webapp.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.grouplens.lenskit.data.sql.JDBCRatingDAO;
import org.grouplens.lenskit.webapp.Configuration;
import org.h2.tools.RunScript;

public class JDBCRatingServerDAOFactory extends JDBCRatingDAO.Factory {

	private String cxnUrl;
	private ServerSQLStatementFactory factory;
	private Configuration config;

	public JDBCRatingServerDAOFactory(Configuration config) {
		super(config.getProperty("rec.dao.url"), getServerSQLStatementFactory(config));
		cxnUrl = config.getProperty("rec.dao.url");
		this.factory = getServerSQLStatementFactory(config);
		this.config = config;
	}

	@Override
	public JDBCRatingServerDAO create() {
		
		if (cxnUrl == null) {
			throw new UnsupportedOperationException("Cannot open session w/o URL");
		}

		Connection dbc;
		try {
			Class.forName("org.h2.Driver");
			dbc = DriverManager.getConnection(cxnUrl);
			JDBCRatingServerDAO retDao = new JDBCRatingServerDAO(new JDBCServerDataSession(dbc, factory), true);
			boolean reloadEnabled = Boolean.valueOf(config.getProperty("rec.dao.reload-enabled", "false"));
			String schemaScript = config.getProperty("rec.dao.schema");
			if (schemaScript != null && reloadEnabled) {
				RunScript.execute(cxnUrl, null, null, schemaScript, null, false);
			}
			String dataScript = config.getProperty("rec.dao.data");
			if (dataScript != null && reloadEnabled) {
				RunScript.execute(cxnUrl, null, null, dataScript, null, false);
			}
			return retDao;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	private static ServerSQLStatementFactory getServerSQLStatementFactory(Configuration config) {
		return config.getInstance(ServerSQLStatementFactory.class, "org.grouplens.lenskit.webapp.sql.BasicServerSQLStatementFactory");
	}
}