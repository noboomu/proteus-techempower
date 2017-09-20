package io.sinistral;

import static io.undertow.util.Headers.CONTENT_TYPE;

import java.nio.ByteBuffer;

import com.jsoniter.output.EncodingMode;
import com.jsoniter.output.JsonStream;

import io.sinistral.controllers.Benchmarks;
import io.sinistral.models.Message;
import io.sinistral.models.MessageEncoder;
import io.sinistral.models.World;
import io.sinistral.models.WorldEncoder;
import io.sinistral.proteus.ProteusApplication;
import io.sinistral.proteus.services.AssetsService;
import io.sinistral.proteus.services.SwaggerService;
import io.sinistral.services.MySqlService;
import io.sinistral.services.PostgresService;
import io.undertow.Undertow;
import io.undertow.UndertowOptions;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.PathHandler;
import io.undertow.server.handlers.SetHeaderHandler;

/**
 * Hello world!
 *
 */
public class ExampleApplication extends ProteusApplication
{
	
	private static final ByteBuffer MESSAGE_BUFFER;
    private static final String MESSAGE = "Hello, World!";
    
    static {
    	MESSAGE_BUFFER = ByteBuffer.allocateDirect(MESSAGE.length());
    	try {
    		MESSAGE_BUFFER.put(MESSAGE.getBytes("US-ASCII"));
     } catch (Exception e) {
    	 throw new RuntimeException(e);
     	}
    	MESSAGE_BUFFER.flip();
    	
    	JsonStream.setMode(EncodingMode.STATIC_MODE);
    	 
    	 
    }
    
    
    public static void main( String[] args )
    {
        
        ExampleApplication app = new ExampleApplication();
        
        app.addService(SwaggerService.class);
		 
		app.addService(AssetsService.class);

		app.addService(MySqlService.class);

		app.addService(PostgresService.class);
 
		app.addController(Benchmarks.class);  
		
		app.start();
		
		PathHandler pathHandler = new PathHandler();
		
		pathHandler.addExactPath("/json", new HttpHandler(){
			 @Override
			  public void handleRequest(HttpServerExchange exchange) {
			
			 exchange.getResponseHeaders().put(CONTENT_TYPE, "application/json");
			   
			 ByteBuffer json = JsonStream.serializeToBytes( new Message("Hello, World!") ); 
			 
			 exchange.getResponseSender().send( json  );
			 }
		});
		
		pathHandler.addExactPath("/plaintext", new HttpHandler(){
			 @Override
			  public void handleRequest(HttpServerExchange exchange) {
			
				 exchange.getResponseHeaders().put(CONTENT_TYPE, "text/plain");
				    exchange.getResponseSender().send(MESSAGE_BUFFER.duplicate());
			 }
		});
		
		  
		
		 HttpHandler rootHandler = new SetHeaderHandler(pathHandler, "Server", "U-tow");
		    Undertow.builder()
		            .addHttpListener(8094, "0.0.0.0")
		            // In HTTP/1.1, connections are persistent unless declared
		            // otherwise.  Adding a "Connection: keep-alive" header to every
		            // response would only add useless bytes.
		            .setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
		            .setHandler(rootHandler)
		            .build()
		            .start();
		    
		    HttpHandler rootHandler2 = new SetHeaderHandler(pathHandler, "Server", "U-tow");
		    Undertow.builder()
		            .addHttpListener(8095, "0.0.0.0")
		            .setBufferSize(16 * 1024)
					.setIoThreads(8)
					.setServerOption(UndertowOptions.ENABLE_HTTP2, false)
					.setServerOption(UndertowOptions.ALWAYS_SET_DATE, true)
					.setSocketOption(org.xnio.Options.BACKLOG, 100000)
					.setServerOption(UndertowOptions.ALWAYS_SET_KEEP_ALIVE, false)
					.setServerOption(UndertowOptions.RECORD_REQUEST_START_TIME, false) 
					.setWorkerThreads(200)
					.setHandler(rootHandler2)
					 .build()
			            .start();
    }
    
}
