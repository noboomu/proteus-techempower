/**
 * 
 */
package io.sinistral.services;

import java.sql.Connection;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.zaxxer.hikari.HikariDataSource;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPool;
import io.reactiverse.pgclient.PgPoolOptions;
import io.sinistral.proteus.services.BaseService;

/**
 * @author jbauer
 */
@Singleton
public class PgClientService extends BaseService
{

	protected PgPool client;

	private static Logger log = LoggerFactory.getLogger(PostgresService.class.getCanonicalName());

	@Inject
	@Named("postgres.pgclient.database")
	protected String database;

	@Inject
	@Named("postgres.pgclient.username")
	protected String username;

	@Inject
	@Named("postgres.pgclient.password")
	protected String password;

	@Inject
	@Named("postgres.pgclient.maximumPoolSize")
	protected Integer maximumPoolSize;

	@Inject
	@Named("postgres.pgclient.host")
	protected String host;
 
	public void configure(Binder binder)
	{
		log.debug("Binding " + this.getClass().getSimpleName());
		binder.bind(PgClientService.class).toInstance(this);
	}

	@Override
	protected void startUp() throws Exception
	{
		super.startUp();
 
		PgPoolOptions options = new PgPoolOptions();
		options.setDatabase(database);
		options.setHost(host);
		options.setPort(5432);
		options.setUser(username);
		options.setPassword(password);
		options.setCachePreparedStatements(true);

		client = PgClient.pool(new PgPoolOptions(options).setMaxSize(maximumPoolSize));

		log.info(this.getClass().getSimpleName() + " started with client " + client);

	}

	public PgClient getClient() 
	{
		return this.client;
	}

	@Override
	protected void shutDown() throws Exception
	{
		super.shutDown(); 
		this.client.close();
	}

}
