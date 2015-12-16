package hello.service;


import com.google.common.io.CharStreams;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.impl.nio.reactor.IOReactorConfig;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOReactorException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;


@Service
public class HomerService {

	private CloseableHttpAsyncClient httpclient;

	@PostConstruct
	public void init() throws InterruptedException, IOReactorException {

		// Create I/O reactor configuration
		IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
			.setIoThreadCount(Runtime.getRuntime().availableProcessors())
			.setConnectTimeout(30000)
			.setSoTimeout(30000)
			.build();

		// Create a custom I/O reactort
		ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor(ioReactorConfig);

		// Create a connection manager with custom configuration.
		PoolingNHttpClientConnectionManager connManager = new PoolingNHttpClientConnectionManager(ioReactor);

		// Create message constraints
//		MessageConstraints messageConstraints = MessageConstraints.custom()
//			.setMaxHeaderCount(200)
//			.setMaxLineLength(2000)
//			.build();
//		// Create connection configuration
//		ConnectionConfig connectionConfig = ConnectionConfig.custom()
//			.setMalformedInputAction(CodingErrorAction.IGNORE)
//			.setUnmappableInputAction(CodingErrorAction.IGNORE)
//			.setCharset(Consts.UTF_8)
//			.setMessageConstraints(messageConstraints)
//			.build();
		// Configure the connection manager to use connection configuration either
		// by default or for a specific host.
		//connManager.setDefaultConnectionConfig(connectionConfig);
		connManager.setConnectionConfig(new HttpHost("localhost"), ConnectionConfig.DEFAULT);

		// Configure total max or per route limits for persistent connections
		// that can be kept in the pool or leased by the connection manager.
		connManager.setMaxTotal(3000);
		connManager.setDefaultMaxPerRoute(3000);
		connManager.setMaxPerRoute(new HttpRoute(new HttpHost("localhost")), 20);

//		// Use custom cookie store if necessary.
//		CookieStore cookieStore = new BasicCookieStore();
//		// Use custom credentials provider if necessary.
//		CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//		// Create global request configuration
//		RequestConfig defaultRequestConfig = RequestConfig.custom()
//			.setCookieSpec(CookieSpecs.DEFAULT)
//			.setExpectContinueEnabled(true)
//			.setTargetPreferredAuthSchemes(Arrays.asList(AuthSchemes.NTLM, AuthSchemes.DIGEST))
//			.setProxyPreferredAuthSchemes(Arrays.asList(AuthSchemes.BASIC))
//			.build();

		// Create an HttpClient with the given custom dependencies and configuration.
		httpclient = HttpAsyncClients.custom()
			.setConnectionManager(connManager)
			.build();
		httpclient.start();


	}

	public void process(ResponseCallBack callback) {
			HttpGet request = new HttpGet("http://localhost:64997/test.json");
			request.setHeader("content-type","application/json");
			httpclient.execute(request, new FutureCallback<HttpResponse>() {
				@Override
				public void completed(HttpResponse httpResponse) {
					try {
						String result = CharStreams.toString(new InputStreamReader(httpResponse.getEntity().getContent()));
						callback.process(result, null);
					} catch (IOException e) {
						callback.process(null, e);
					}
				}

				@Override
				public void failed(Exception e) {
					callback.process(null, e);

				}

				@Override
				public void cancelled() {
					callback.process(null, new RuntimeException("Cancelled"));

				}
			});
	}

	@FunctionalInterface
	public interface ResponseCallBack {
		void process(String response, Throwable throwable);
	}

}
