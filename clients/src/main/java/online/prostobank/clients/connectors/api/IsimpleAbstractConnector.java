package online.prostobank.clients.connectors.api;

import online.prostobank.clients.config.properties.IsimpleAbstractConnectorProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

public abstract class IsimpleAbstractConnector {

	@Autowired private IsimpleAbstractConnectorProperties config;

	public DataSource ds() {
		DriverManagerDataSource ds = new DriverManagerDataSource();
		ds.setUsername(config.getDsUsername());
		ds.setPassword(config.getDsPassword());
		ds.setDriverClassName(config.getDsDriverClassName());
		ds.setUrl(config.getDsUrl());
		return ds;
	}
}
