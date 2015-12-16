package hello.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;

public class TestVerticle extends AbstractVerticle {

	public static final int CLIENT_PORT = 8081;
	public static final String CLIENT_LOCALHOST = "localhost";
	public static final String PATH = "/test.json";
	public static final int SERVER_PORT = 8080;

	@Override
	public void start() {
		HttpServerOptions httpServerOptions = new HttpServerOptions();
		HttpServer httpServer = vertx.createHttpServer(httpServerOptions);
		Router router = Router.router(vertx);
		httpServer.requestHandler(router::accept);

		HttpClientOptions httpClientOptions = new HttpClientOptions();
		httpClientOptions.setMaxPoolSize(100);
		httpClientOptions.setKeepAlive(false);
		HttpClient client = vertx.createHttpClient(httpClientOptions);

		final Route route = router.route(HttpMethod.GET, "/vertx");
		route.handler(context -> {
					client.getNow(
							CLIENT_PORT,
							CLIENT_LOCALHOST,
							PATH,
							response -> {
								response.bodyHandler(buffer ->
												context.response().end(buffer)
								);
							}
					);
				}
		);
		route.failureHandler(context -> context.response().end("{\"error\":\"Server error\"}"));
		httpServer.listen(SERVER_PORT);
	}
}
