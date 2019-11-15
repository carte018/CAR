package edu.internet2.consent.car;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;

public class CarHttpClientFactory {

	private static CloseableHttpClient client;
	private static PoolingHttpClientConnectionManager pool = null;
	
	// Statically construct a factory for generating custom HttpClient objects with 
	// pooling connection management and a registry to handle state for SSL connections.
	//
	// Use the default SSL connection context and trust arrangements
	//
	static {
		try {
			SSLContext sslc = SSLContexts.createDefault().getDefault();
			SSLConnectionSocketFactory factory = new SSLConnectionSocketFactory(sslc);
			Registry reg = RegistryBuilder.create().register("https",factory).build();
			pool = new PoolingHttpClientConnectionManager(reg);
			
			// Set some configuration parameters for the connection pool.
			//
			// This pool will be shared by the whole of the CAR environment, so we need
			// to have sufficient pooled connections to service expected simultaneous
			// connections.  Each connection will typically be handled by a different car
			// thread, and we'll typically have 400 threads at maximum, so we can estimate
			// pool requirements at just less then 400 connections total.
			//
			// Set the maximum conn count to 350.
			//
			// Also, we need to constrain the per-route connection count.  For car purposes
			// there are really only two relevant routes -- one to the ICM and one to the 
			// informed content service.  On average, there will be more informed connections
			// than ICM connections, but at any given instant, the two may be flipped.
			// 
			// Allow up to 350 per route and let usage control the spread.
			//
			
			pool.setMaxTotal(350);
			pool.setDefaultMaxPerRoute(350);
			
			// We must make the connection manager "shared" since the connections it mints
			// may be used across multiple threads over time.  Otherwise we will encounter
			// shutdown factory exceptions.
			
			client = HttpClients.custom().setConnectionManagerShared(true).setConnectionManager(pool).setSSLSocketFactory(factory).build();
		} catch (Exception e) {
			// On exception fall through and allow the failure to trigger a downstream exception
			CarUtility.locError("ERR1136", LogCriticality.error,e.getMessage());
		}
	}
	
	public static CloseableHttpClient getHttpsClient() throws KeyManagementException, NoSuchAlgorithmException {
		
		if (client != null) {
			return client;
		}
		CarUtility.locError("ERR1136", LogCriticality.error,"Failed retrieving client from factory");
		throw new RuntimeException("Failed retrieving client from SSL connection factory");
	}
	
	public static void releaseInstance() {
		// Instance release just involves nulling the client
		client = null;
	}
}
