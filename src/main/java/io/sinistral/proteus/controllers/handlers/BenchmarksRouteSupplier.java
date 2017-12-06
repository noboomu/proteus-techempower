package io.sinistral.proteus.controllers.handlers;

import static io.sinistral.proteus.server.Extractors.*;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import io.sinistral.controllers.Benchmarks;
import io.undertow.server.HandlerWrapper;
import io.undertow.server.HttpHandler;
import io.undertow.server.RoutingHandler;
import io.undertow.server.handlers.PathHandler;

import java.lang.String;
import java.util.Map;
import java.util.function.Supplier;

public class BenchmarksRouteSupplier implements Supplier<HttpHandler> {
  protected final Benchmarks benchmarksController;

  protected final Map<String, HandlerWrapper> registeredHandlerWrappers;

  @Inject
  public BenchmarksRouteSupplier(Benchmarks benchmarksController,
      @Named("registeredHandlerWrappers") Map<String, HandlerWrapper> registeredHandlerWrappers) {
    this.benchmarksController = benchmarksController;
    this.registeredHandlerWrappers = registeredHandlerWrappers;
  }

  public HttpHandler get() {
	  
    final PathHandler router = new PathHandler();

    final io.undertow.server.HttpHandler benchmarksDbPostgresHandler = new io.undertow.server.HttpHandler() {
      @java.lang.Override
      public void handleRequest(final io.undertow.server.HttpServerExchange exchange) throws
          java.lang.Exception {

        benchmarksController.dbPostgres(exchange);
      }
    };

    router.addExactPath("/db",new io.undertow.server.handlers.BlockingHandler(benchmarksDbPostgresHandler));
    


    final io.undertow.server.HttpHandler benchmarksDbMySqlHandler = new io.undertow.server.HttpHandler() {
      @java.lang.Override
      public void handleRequest(final io.undertow.server.HttpServerExchange exchange) throws
          java.lang.Exception {

        benchmarksController.dbMySql(exchange);
      }
    };

    router.addExactPath("/db/mysql",new io.undertow.server.handlers.BlockingHandler(benchmarksDbMySqlHandler));

    final io.undertow.server.HttpHandler benchmarksFortunesMysqlHandler = new io.undertow.server.HttpHandler() {
      @java.lang.Override
      public void handleRequest(final io.undertow.server.HttpServerExchange exchange) throws
          java.lang.Exception {

        benchmarksController.fortunesMysql(exchange);
      }
    };

    router.addExactPath("/fortunes/mysql",new io.undertow.server.handlers.BlockingHandler(benchmarksFortunesMysqlHandler));

    final io.undertow.server.HttpHandler benchmarksFortunesPostgresHandler = new io.undertow.server.HttpHandler() {
      @java.lang.Override
      public void handleRequest(final io.undertow.server.HttpServerExchange exchange) throws
          java.lang.Exception {

        benchmarksController.fortunesPostgres(exchange);
      }
    };

    router.addExactPath("/fortunes",new io.undertow.server.handlers.BlockingHandler(benchmarksFortunesPostgresHandler));

    final io.undertow.server.HttpHandler benchmarksPlaintextHandler = new io.undertow.server.HttpHandler() {
      @java.lang.Override
      public void handleRequest(final io.undertow.server.HttpServerExchange exchange) throws
          java.lang.Exception {

        benchmarksController.plaintext(exchange);
      }
    };

    router.addExactPath("/plaintext",benchmarksPlaintextHandler);

    final io.undertow.server.HttpHandler benchmarksJsonHandler = new io.undertow.server.HttpHandler() {
      @java.lang.Override
      public void handleRequest(final io.undertow.server.HttpServerExchange exchange) throws
          java.lang.Exception {

        benchmarksController.json(exchange);
      }
    };

    router.addExactPath("/json",benchmarksJsonHandler);


    return router;
  }
}
