package edu.internet2.consent.car;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;

import org.apache.http.HeaderElement;
import org.apache.http.HeaderElementIterator;
import org.apache.http.HttpResponse;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicHeaderElementIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
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
			
			pool.setValidateAfterInactivity(-1);
			
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
			
			client = HttpClients.custom().setDefaultRequestConfig(
				    RequestConfig.custom().build()
					).setConnectionManagerShared(true).setKeepAliveStrategy(new CarConnectionKeepAliveStrategy()).setConnectionManager(pool).setSSLSocketFactory(factory).build();
			
			// Spin a thread to watch for idle connections in the pool manager
			
			CarIdleConnectionMonitorThread watcher = new CarIdleConnectionMonitorThread(pool);
			
			watcher.start();
			
			// never join back
			// watcher.join(100);  // or if you do , only wait 100ms for completion
		} catch (Exception e) {
			// On exception fall through and allow the failure to trigger a downstream exception
			CarUtility.locError("ERR1136",e.getMessage());
		}
	}
	
	public static CloseableHttpClient getHttpsClient() throws KeyManagementException, NoSuchAlgorithmException {
		
		if (client != null) {
			return client;
		}
		CarUtility.locError("ERR1136","Failed retrieving client from factory");
		throw new RuntimeException("Failed retrieving client from SSL connection factory");
	}
	
	public static void releaseInstance() {
		// Instance release just involves nulling the client
		client = null;
	}
}	

// Define a keep alive strategy that honors KeepAlive headers and uses 1 minute
// timeouts for connections that don't receive keepalive values in responses

class CarConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
	
	@Override
	public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
        HeaderElementIterator it = new BasicHeaderElementIterator
                (response.headerIterator(HTTP.CONN_KEEP_ALIVE));
            while (it.hasNext()) {
                HeaderElement he = it.nextElement();
                String param = he.getName();
                String value = he.getValue();
                if (value != null && param.equalsIgnoreCase
                   ("timeout")) {
                    return Long.parseLong(value) * 1000;
                }
            }
            return 60 * 1000;  // Default to 1-minute 
        }
}

// Define a Thread subclass that handles monitoring the HTTP connection
// pool for stale members and reaps them.

class CarIdleConnectionMonitorThread extends Thread {
    private final HttpClientConnectionManager connMgr;
    private volatile boolean shutdown;

    public CarIdleConnectionMonitorThread(
      PoolingHttpClientConnectionManager connMgr) {
        super();
        this.connMgr = connMgr;
    }
    @Override
    public void run() {
        try {
            while (!shutdown) {
                synchronized (this) {
                    wait(1000);
                    connMgr.closeExpiredConnections();
                    connMgr.closeIdleConnections(90, TimeUnit.SECONDS);
                }
            }
        } catch (InterruptedException ex) {
            shutdown();
        }
    }
    public void shutdown() {
        shutdown = true;
        synchronized (this) {
            notifyAll();
        }
    }
}