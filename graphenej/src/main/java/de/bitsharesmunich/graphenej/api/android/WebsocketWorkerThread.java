package de.bitsharesmunich.graphenej.api.android;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketListener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import de.bitsharesmunich.graphenej.test.NaiveSSLContext;

/**
 * Created by nelson on 11/17/16.
 */
public class WebsocketWorkerThread extends Thread {
    private final String TAG = this.getClass().getName();

    // When debugging we'll use a NaiveSSLContext
    public static final boolean DEBUG = true;

    private final int TIMEOUT = 5000;
    private WebSocket mWebSocket;

    public WebsocketWorkerThread(String url){
        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(TIMEOUT);

            if(DEBUG){
                SSLContext context = NaiveSSLContext.getInstance("TLS");

                // Set the custom SSL context.
                factory.setSSLContext(context);
            }

            mWebSocket = factory.createSocket(url);
        } catch (IOException e) {
            System.out.println("IOException. Msg: "+e.getMessage());
        } catch(NullPointerException e){
            System.out.println("NullPointerException at WebsocketWorkerThreas. Msg: "+e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException. Msg: "+e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            mWebSocket.connect();
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: "+e.getMessage());
        }
    }

    public void addListener(WebSocketListener listener){
        mWebSocket.addListener(listener);
    }
}