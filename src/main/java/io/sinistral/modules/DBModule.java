/**
 * 
 */
package io.sinistral.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Singleton;

import io.sinistral.services.PostgresService;
import io.sinistral.services.MySqlService;

/**
 * @author jbauer
 *
 */
@Singleton
public class DBModule extends AbstractModule  
{
	private static Logger log = LoggerFactory.getLogger(DBModule.class.getCanonicalName());
  
	@Inject
	protected MySqlService sqlService;
 
	@Inject
	protected PostgresService postgresService;
	
	@SuppressWarnings("unchecked")
	@Override
	protected void configure()
	{
		log.debug("DBModule starting configuration");
		
		this.binder().requestInjection(this);
		
		this.binder().bind(MySqlService.class).toInstance(sqlService);
		
		this.binder().bind(PostgresService.class).toInstance(postgresService);


	}

 
 

}
