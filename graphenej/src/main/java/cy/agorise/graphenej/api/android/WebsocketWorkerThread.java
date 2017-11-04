package cy.agorise.graphenej.api.android;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketListener;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;

import cy.agorise.graphenej.interfaces.NodeErrorListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.test.NaiveSSLContext;

/**
 *  Class used to encapsulate the thread where the WebSocket does the requests.
 *
 */
public class WebsocketWorkerThread extends Thread {
    private final String TAG = this.getClass().getName();

    // When debugging we'll use a NaiveSSLContext
    public static final boolean DEBUG = true;

    private final int TIMEOUT = 5000;
    private WebSocket mWebSocket;
    private NodeErrorListener mErrorListener;

    /**
     * Constructor
     *
     * @param url   URL of the WebSocket
     */
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

    /**
     * Constructor with connection error listener.
     *
     * @param url               URL of the WebSocket
     * @param errorListener     a class implementing the NodeErrorListener interface. This
     *                          should be implemented by the party interested in being notified
     *                          about the failure of the connection.
     */
    public WebsocketWorkerThread(String url, NodeErrorListener errorListener){
        mErrorListener = errorListener;
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
            mErrorListener.onError(new BaseResponse.Error(e.getMessage()));
        } catch(NullPointerException e){
            System.out.println("NullPointerException at WebsocketWorkerThreas. Msg: "+e.getMessage());
            mErrorListener.onError(new BaseResponse.Error(e.getMessage()));
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException. Msg: "+e.getMessage());
            mErrorListener.onError(new BaseResponse.Error(e.getMessage()));
        } catch(IllegalArgumentException e){
            System.out.println("IllegalArgumentException. Msg: "+e.getMessage());
            mErrorListener.onError(new BaseResponse.Error(e.getMessage()));
        }
    }

    /**
     * Method call when the thread is started.
     */
    @Override
    public void run() {
        try {
            mWebSocket.connect();
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: "+e.getMessage());
            mErrorListener.onError(new BaseResponse.Error(e.getMessage()));
        }
    }

    /**
     * Add a WebSocketListener to the thread that will run. This should be implemented by the party
     * interested in being notified about the response value of a request.
     *
     * @param listener  listener implemented to be notified when the socket get a response from the
     *                  node
     */
    public void addListener(WebSocketListener listener){
        mWebSocket.addListener(listener);
    }
}