package de.bitsharesmunich.graphenej.api;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.junit.Before;

import javax.net.ssl.SSLContext;

import de.bitsharesmunich.graphenej.test.NaiveSSLContext;

/**
 * Created by nelson on 4/14/17.
 */

public class BaseApiTest {
    protected String BLOCK_PAY_DE = System.getenv("OPENLEDGER_EU");

    protected SSLContext context;
    protected WebSocket mWebSocket;

    @Before
    public void setUp() throws Exception {
        context = NaiveSSLContext.getInstance("TLS");
        WebSocketFactory factory = new WebSocketFactory();

        // Set the custom SSL context.
        factory.setSSLContext(context);

        mWebSocket = factory.createSocket(BLOCK_PAY_DE);
    }

}
