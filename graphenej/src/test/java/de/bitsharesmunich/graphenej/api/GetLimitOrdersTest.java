package de.bitsharesmunich.graphenej.api;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.net.ssl.SSLContext;

import de.bitsharesmunich.graphenej.Asset;
import de.bitsharesmunich.graphenej.Converter;
import de.bitsharesmunich.graphenej.LimitOrder;
import de.bitsharesmunich.graphenej.interfaces.WitnessResponseListener;
import de.bitsharesmunich.graphenej.models.BaseResponse;
import de.bitsharesmunich.graphenej.models.WitnessResponse;
import de.bitsharesmunich.graphenej.test.NaiveSSLContext;

/**
 * Created by nelson on 3/24/17.
 */
public class GetLimitOrdersTest {
    private String BLOCK_PAY_DE = System.getenv("BLOCKPAY_DE");
    private final Asset base = new Asset("1.3.120", "EUR", 4);
    private final Asset quote = new Asset("1.3.121", "USD", 4);


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testGetLimitOrders(){
        SSLContext context = null;
        try {
            context = NaiveSSLContext.getInstance("TLS");
            WebSocketFactory factory = new WebSocketFactory();

            // Set the custom SSL context.
            factory.setSSLContext(context);

            WebSocket mWebSocket = factory.createSocket(BLOCK_PAY_DE);

            mWebSocket.addListener(new GetLimitOrders(base.getObjectId(), quote.getObjectId(), 100, new WitnessResponseListener() {
                @Override
                public void onSuccess(WitnessResponse response) {
                    List<LimitOrder> orders = (List<LimitOrder>) response.result;
                    Converter converter = new Converter();
                    System.out.println();
                    for(LimitOrder order : orders){
                        order.getSellPrice().base.getAsset().setPrecision(base.getPrecision());
                        order.getSellPrice().quote.getAsset().setPrecision(quote.getPrecision());
                        double baseToQuoteExchange = converter.getConversionRate(order.getSellPrice(), Converter.BASE_TO_QUOTE);
                        double quoteToBaseExchange = converter.getConversionRate(order.getSellPrice(), Converter.QUOTE_TO_BASE);
                        System.out.println(String.format("base to quote: %.5f, quote to base: %.5f", baseToQuoteExchange, quoteToBaseExchange));

                        synchronized (GetLimitOrdersTest.this){
                            GetLimitOrdersTest.this.notifyAll();
                        }
                    }
                }

                @Override
                public void onError(BaseResponse.Error error) {
                    System.out.println("onError. Msg: "+error.message);
                    synchronized (GetLimitOrdersTest.this){
                        GetLimitOrdersTest.this.notifyAll();
                    }
                }
            }));
            mWebSocket.connect();
            System.out.println("Waiting..");
            synchronized (this){
                wait();
            }
            System.out.println("Released!");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException. Msg: " + e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IOException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {

    }

}