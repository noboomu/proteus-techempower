package io.sinistral;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import io.sinistral.controllers.Benchmarks;
import io.sinistral.proteus.ProteusApplication;
import io.sinistral.proteus.services.AssetsService;
import io.sinistral.proteus.services.SwaggerService;
import io.sinistral.services.MySqlService;
import io.sinistral.services.PostgresService;

/**
 * Hello world!
 *
 */
public class ExampleApplication extends ProteusApplication
{
    public static void main( String[] args )
    {
        
        ExampleApplication app = new ExampleApplication();
        
        app.addService(SwaggerService.class);
		 
		app.addService(AssetsService.class);

		app.addService(MySqlService.class);

		app.addService(PostgresService.class);
 
		app.addController(Benchmarks.class);  
		
		System.out.println(app.config.getString("application.tmpdir"));

		System.out.println(System.getProperty("java.io.tmpdir"));
		
		try
		{
			Path tmpFile = Files.createTempDirectory(app.config.getString("application.name"));
			
			System.out.println(tmpFile);
			
		} catch (Exception e)
		{
			e.printStackTrace();
		}
		app.start();
        
    }
}
