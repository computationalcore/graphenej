package com.luminiasoft.labs.sample;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import cy.agorise.graphenej.api.ConnectionStatusUpdate;
import cy.agorise.graphenej.api.android.RxBus;
import cy.agorise.graphenej.api.bitshares.Nodes;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.WebSocket;
import okhttp3.WebSocketListener;

/**
 * Service in charge of mantaining a connection to the full node.
 */

public class NetworkService extends Service {
    private final String TAG = this.getClass().getName();

    private static final int NORMAL_CLOSURE_STATUS = 1000;

    private final IBinder mBinder = new LocalBinder();

    private WebSocket mWebSocket;

    private int mSocketIndex;

    private WebSocketListener mWebSocketListener = new WebSocketListener() {

        @Override
        public void onOpen(WebSocket webSocket, Response response) {
            super.onOpen(webSocket, response);
            mWebSocket = webSocket;
            RxBus.getBusInstance().send(new ConnectionStatusUpdate(ConnectionStatusUpdate.CONNECTED));
        }

        @Override
        public void onMessage(WebSocket webSocket, String text) {
            super.onMessage(webSocket, text);
            Log.d(TAG,"onMessage. text: "+text);
            RxBus.getBusInstance().send(text);
        }

        @Override
        public void onClosed(WebSocket webSocket, int code, String reason) {
            super.onClosed(webSocket, code, reason);
            Log.d(TAG,"onClosed");
            RxBus.getBusInstance().send(new ConnectionStatusUpdate(ConnectionStatusUpdate.DISCONNECTED));
        }

        @Override
        public void onFailure(WebSocket webSocket, Throwable t, Response response) {
            super.onFailure(webSocket, t, response);
            Log.d(TAG,"onFailure. Msg: "+t.getMessage());
            RxBus.getBusInstance().send(new ConnectionStatusUpdate(ConnectionStatusUpdate.DISCONNECTED));
            mSocketIndex++;
            connect();
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG,"onCreate");
        connect();
    }

    private void connect(){
        OkHttpClient client = new OkHttpClient();
        String url = Nodes.NODE_URLS[mSocketIndex % Nodes.NODE_URLS.length];
        Request request = new Request.Builder().url(url).build();
        client.newWebSocket(request, mWebSocketListener);
    }

    public void sendMessage(String message){
        if(mWebSocket.send(message)){
            Log.d(TAG,"Message enqueued");
        }else{
            Log.w(TAG,"Message not enqueued");
        }
    }


    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        mWebSocket.close(NORMAL_CLOSURE_STATUS, null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG,"onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind");
        return mBinder;
    }

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public NetworkService getService() {
            // Return this instance of LocalService so clients can call public methods
            return NetworkService.this;
        }
    }
}
