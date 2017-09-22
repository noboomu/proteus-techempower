/**
 * 
 */
package io.sinistral.controllers;

import static io.undertow.util.Headers.CONTENT_TYPE;

import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.jsoniter.output.EncodingMode;
import com.jsoniter.output.JsonStream;

import io.sinistral.models.Fortune;
import io.sinistral.models.Message;
import io.sinistral.models.MessageEncoder;
import io.sinistral.models.World;
import io.sinistral.models.WorldEncoder;
import io.sinistral.proteus.annotations.Blocking;
import io.sinistral.services.MySqlService;
import io.sinistral.services.PostgresService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

/**
 * Much of this borrowed with reverence from Light-Java
 * 
 * @author jbauer
 *
 */

@Api(tags="benchmark")
@Path("")
@Produces((MediaType.APPLICATION_JSON)) 
@Consumes((MediaType.MEDIA_TYPE_WILDCARD)) 
@Singleton
public class Benchmarks
{
	private static final String HTML_UTF8_TYPE = io.sinistral.proteus.server.MediaType.TEXT_HTML_UTF8.toString();
	private static final String PLAINTEXT_TYPE = io.sinistral.proteus.server.MediaType.TEXT_PLAIN.toString();
	private static final ByteBuffer MESSAGE_BUFFER;
    private static final String MESSAGE = "Hello, World!";
    
    private static final MustacheFactory mustacheFactory =
    	      new DefaultMustacheFactory();

 
    static {
    	MESSAGE_BUFFER = ByteBuffer.allocateDirect(MESSAGE.length());
    	try {
    		MESSAGE_BUFFER.put(MESSAGE.getBytes("US-ASCII"));
     } catch (Exception e) {
    	 throw new RuntimeException(e);
     	}
    	MESSAGE_BUFFER.flip();
    	
    	JsonStream.setMode(EncodingMode.STATIC_MODE);
    	 
    	
    	JsonStream.registerNativeEncoder(Message.class, new MessageEncoder());
    	JsonStream.registerNativeEncoder(World.class, new WorldEncoder());

    }
    
    
 
	protected final MySqlService sqlService;
	
	 
	protected final PostgresService postgresService;
	
   
    public static int randomWorld() {
        return 1 + ThreadLocalRandom.current().nextInt(10000);
    }

    @Inject
    public Benchmarks(PostgresService postgresService, MySqlService sqlService)
    {
    	this.sqlService = sqlService;
    	this.postgresService = postgresService;
    }
	
	
	@GET
	@Path("/db/postgres")
	@Blocking
	@ApiOperation(value = "World postgres db endpoint",   httpMethod = "GET" , response = World.class)
	public void dbPostgres(HttpServerExchange exchange)
	{ 		
		final World world;
		
		try (final Connection connection = postgresService.getConnection())
		{
			try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM world WHERE id = ?"))
			{
				statement.setInt(1, randomWorld());
				try (ResultSet resultSet = statement.executeQuery())
				{
					resultSet.next();
					int id = resultSet.getInt("id");
					int randomNumber = resultSet.getInt("randomNumber");
					world = new World(id, randomNumber);
				}
			}

			exchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, "application/json");
			
			ByteArrayOutputStream os = new  ByteArrayOutputStream(128);
			WorldEncoder.encodeRaw(world, os); 
			 
			exchange.getResponseSender().send(ByteBuffer.wrap(os.toByteArray()));

		} catch (Exception e)
		{
			throw new IllegalArgumentException();
		}
		  
 		 
	}
	
	@GET
	@Path("/db/mysql")
	@ApiOperation(value = "World mysql db endpoint",   httpMethod = "GET" , response = World.class)
	public void dbMySql(HttpServerExchange exchange)
	{ 		
		final World world;
		
		try (final Connection connection = sqlService.getConnection())
		{
			try (PreparedStatement statement = connection.prepareStatement("SELECT id,randomNumber FROM world WHERE id = ?", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY))
			{
				statement.setInt(1, randomWorld());
				try (ResultSet resultSet = statement.executeQuery())
				{
					resultSet.next();
					world = new World(resultSet.getInt("id"), resultSet.getInt("randomNumber"));
				}
			}

			exchange.getResponseHeaders().put(io.undertow.util.Headers.CONTENT_TYPE, "application/json");
			ByteArrayOutputStream os = new  ByteArrayOutputStream(128);
			WorldEncoder.encodeRaw(world, os); 
			 
			exchange.getResponseSender().send(ByteBuffer.wrap(os.toByteArray()));

		} catch (Exception e)
		{
			throw new IllegalArgumentException();
		}
		  
 		 
	}
	
	

	@GET
	@Path("/fortunes/mysql")
	@Produces(MediaType.TEXT_HTML)
	@ApiOperation(value = "Fortunes mysql endpoint",   httpMethod = "GET"  )
	public void fortunesMysql(HttpServerExchange exchange  ) throws Exception
	{ 
 
		List<Fortune> fortunes = new ArrayList<>();
	        
			try (final Connection connection = postgresService.getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Fortune"))
				{
	
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							int id = resultSet.getInt("id");
							String msg = resultSet.getString("message");
	
							fortunes.add(new Fortune(id, msg));
						}
					}
				}
			}
			
	        fortunes.add(new Fortune(0, "Additional fortune added at request time."));
	        
	        fortunes.sort(null);
         
	        final String render = views.Fortunes.template(fortunes).render().toString(); 
	         
	        exchange.getResponseHeaders().put(
	                Headers.CONTENT_TYPE, "text/html");
	        exchange.getResponseSender().send(render); 
		  
	}
	

	@GET
	@Path("/fortunes/postgres")
	@Blocking
	@Produces(MediaType.TEXT_HTML)
	@ApiOperation(value = "Fortunes postgres endpoint",   httpMethod = "GET"  )
	public void fortunesPostgres(HttpServerExchange exchange) throws Exception
	{ 
			List<Fortune> fortunes = new ArrayList<>();
		  
			try (final Connection connection = postgresService.getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Fortune"))
				{
	
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							int id = resultSet.getInt("id");
							String msg = resultSet.getString("message");
	
							fortunes.add(new Fortune(id, msg));
						}
					}
				}
			}
			
	        fortunes.add(new Fortune(0, "Additional fortune added at request time."));
	        
	        fortunes.sort(null);
	        
	        final String render = views.Fortunes.template(fortunes).render().toString(); 
	         
	        exchange.getResponseHeaders().put(
	                Headers.CONTENT_TYPE, "text/html");
	        exchange.getResponseSender().send(render);  
	}
	
	@GET
	@Path("/fortunes/postgres/mustache")
	@Produces(MediaType.TEXT_HTML)
	@Blocking
	@ApiOperation(value = "Fortunes postgres endpoint with mustache",   httpMethod = "GET"  )
	public void fortunesPostgresMustache(HttpServerExchange exchange) throws Exception
	{ 
		

		  List<Fortune> fortunes = new ArrayList<>();
	      
			try (final Connection connection = postgresService.getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Fortune"))
				{
	
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							int id = resultSet.getInt("id");
							String msg = resultSet.getString("message");
	
							fortunes.add(new Fortune(id, msg));
						}
					}
				}
			}
			
	        fortunes.add(new Fortune(0, "Additional fortune added at request time."));
	        
	        Collections.sort(fortunes);
	        
 	        StringWriter writer = new StringWriter();
 	      
 	        Mustache mustache = mustacheFactory.compile("templates/Fortunes.mustache");

	        mustache.execute(writer, fortunes); 
	        	         
	        exchange.getResponseHeaders().put(
	                Headers.CONTENT_TYPE, "text/html");
	        exchange.getResponseSender().send(writer.toString());  
	}
	
	@GET
	@Path("/fortunes/postgres/mustache2")
	@Produces(MediaType.TEXT_HTML)
	@Blocking
	@ApiOperation(value = "Fortunes postgres endpoint with mustache",   httpMethod = "GET"  )
	public void fortunesPostgresMustache2(HttpServerExchange exchange) throws Exception
	{ 
		

		  List<Fortune> fortunes = new ArrayList<>();
	      
			try (final Connection connection = postgresService.getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Fortune"))
				{
	
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							int id = resultSet.getInt("id");
							String msg = resultSet.getString("message");
	
							fortunes.add(new Fortune(id, msg));
						}
					}
				}
			}
			
	        fortunes.add(new Fortune(0, "Additional fortune added at request time."));
	        
	        Collections.sort(fortunes);
	        
 	        StringWriter writer = new StringWriter();
 	      
 	        Mustache mustache = mustacheFactory.compile("templates/Fortunes.mustache");

	        mustache.execute(writer, fortunes).flush(); 
	        	         
	        exchange.getResponseHeaders().put(
	                Headers.CONTENT_TYPE, "text/html");
	        exchange.getResponseSender().send(writer.toString());  
	}
	
	private final static Mustache fortunesMustache = new DefaultMustacheFactory().compile("templates/Fortunes.mustache");
	                                                                     
	@GET
	@Path("/fortunes/postgres/mustache3")
	@Produces(MediaType.TEXT_HTML)
	@Blocking
	@ApiOperation(value = "Fortunes postgres endpoint with mustache",   httpMethod = "GET"  )
	public void fortunesPostgresMustache3(HttpServerExchange exchange) throws Exception
	{ 
		

		  List<Fortune> fortunes = new ArrayList<>();
	      
			try (final Connection connection = postgresService.getConnection())
			{
				try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Fortune"))
				{
	
					try (ResultSet resultSet = statement.executeQuery())
					{
						while (resultSet.next())
						{
							int id = resultSet.getInt("id");
							String msg = resultSet.getString("message");
	
							fortunes.add(new Fortune(id, msg));
						}
					}
				}
			}
			
	        fortunes.add(new Fortune(0, "Additional fortune added at request time."));
	        
	        Collections.sort(fortunes);
	        
 	        StringWriter writer = new StringWriter(); 

 	        fortunesMustache.execute(writer, fortunes).flush(); 
	        	         
	        exchange.getResponseHeaders().put(
	                Headers.CONTENT_TYPE, "text/html");
	        exchange.getResponseSender().send(writer.toString());  
	}
 
	
	@GET
	@Path("/plaintext")
	@ApiOperation(value = "Plaintext endpoint",   httpMethod = "GET" )
	public void plaintext(HttpServerExchange exchange)
	{ 
		   exchange.getResponseHeaders().put(CONTENT_TYPE, "text/plain");
		    exchange.getResponseSender().send(MESSAGE_BUFFER.duplicate());
	}
	
	@GET
	@Path("/json3")
	@ApiOperation(value = "Json serialization endpoint",   httpMethod = "GET" )
	public void json3(HttpServerExchange exchange)
	{ 
		 exchange.getResponseHeaders().put(CONTENT_TYPE, "application/json");
		 
		 ByteArrayOutputStream os = new  ByteArrayOutputStream(128);
		 
		 try
		 {
			 MessageEncoder.encodeRaw(  new Message("Hello, World!"), os);
		 }
		 catch(Exception e)
		 {
			 e.printStackTrace();
		 }
		 
		 exchange.getResponseSender().send( ByteBuffer.wrap(os.toByteArray())  );
		
	}

	

	@GET
	@Path("/json2")
	@ApiOperation(value = "Json serialization endpoint",   httpMethod = "GET" )
	public void json2(HttpServerExchange exchange)
	{ 
		 exchange.getResponseHeaders().put(CONTENT_TYPE, "application/json");
		 
		 ByteBuffer json = ByteBuffer.wrap("{\r\n\"message\": \"Hello, World!\"\r\n}".getBytes()); 
		 
		 exchange.getResponseSender().send( json  );
		
	}
	
	@GET
	@Path("/json")
	@ApiOperation(value = "Json serialization endpoint",   httpMethod = "GET" )
	public void json(HttpServerExchange exchange)
	{ 
		 exchange.getResponseHeaders().put(CONTENT_TYPE, "application/json");
		 
		 ByteArrayOutputStream os = new  ByteArrayOutputStream(128);
		 MessageEncoder.encodeRaw(  new Message("Hello, World!"), os); 
		 exchange.getResponseSender().send( ByteBuffer.wrap(os.toByteArray())   );
		
	}
}
