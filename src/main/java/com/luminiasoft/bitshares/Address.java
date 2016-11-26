package com.luminiasoft.bitshares;

import com.google.common.primitives.Bytes;
import com.google.gson.internal.LinkedTreeMap;
import static com.luminiasoft.bitshares.Test.OPENLEDGER_WITNESS_URL;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
import com.luminiasoft.bitshares.models.BaseResponse;
import com.luminiasoft.bitshares.models.WitnessResponse;
import com.luminiasoft.bitshares.test.NaiveSSLContext;
import com.luminiasoft.bitshares.ws.GetAccountNameById;
import com.luminiasoft.bitshares.ws.GetAccountsByAddress;
import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.util.Arrays;
import java.util.List;
import javafx.util.Pair;
import javax.net.ssl.SSLContext;

/**
 * Class used to encapsulate address-related operations.
 */
public class Address {

    public final static String DEFAULT_PREFIX = "BTS";

    private ECKey key;
    private String prefix;
    private String accountName = null;
    private String accountId = null;

    public Address(ECKey key) {
        this.key = key;
        this.prefix = DEFAULT_PREFIX;
    }

    public Address(ECKey key, String prefix) {
        this.key = key;
        this.prefix = prefix;
    }

    @Override
    public String toString() {
        byte[] pubKey = key.getPubKey();
        byte[] checksum = new byte[160 / 8];
        RIPEMD160Digest ripemd160Digest = new RIPEMD160Digest();
        ripemd160Digest.update(pubKey, 0, pubKey.length);
        ripemd160Digest.doFinal(checksum, 0);
        byte[] pubKeyChecksummed = Bytes.concat(pubKey, Arrays.copyOfRange(checksum, 0, 4));
        return this.prefix + Base58.encode(pubKeyChecksummed);
    }

    public void getAccountDetail() {
        try {
            SSLContext context = NaiveSSLContext.getInstance("TLS");
            WebSocketFactory factory = new WebSocketFactory();
            factory.setSSLContext(context);
            WebSocket mWebSocket = factory.createSocket(OPENLEDGER_WITNESS_URL);
            mWebSocket.addListener(new GetAccountsByAddress(this.toString(), accountIdListener));
            System.out.println("Before connecting");
            mWebSocket.connect();
        } catch (IOException e) {
            System.out.println("IOException. Msg: " + e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (NoSuchAlgorithmException e) {
            System.out.println("NoSuchAlgorithmException. Msg: " + e.getMessage());
        }
    }

    public String getAccountName() {
        return accountName;
    }

    public String getAccountId() {
        return accountId;
    }

    WitnessResponseListener accountIdListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            if (response.result.getClass() == ArrayList.class) {
                List l = (List) response.result;
                if (l.size() > 0) {
                    if (l.get(0).getClass() == ArrayList.class) {
                        List sl = (List) l.get(0);
                        if (sl.size() > 0) {
                            accountId = (String) sl.get(0);
                            try {
                                // Create a custom SSL context.
                                SSLContext context = NaiveSSLContext.getInstance("TLS");
                                WebSocketFactory factory = new WebSocketFactory();
                                factory.setSSLContext(context);

                                WebSocket mWebSocket = factory.createSocket(OPENLEDGER_WITNESS_URL);
                                mWebSocket.addListener(new GetAccountNameById(accountId, accountListener));
                                mWebSocket.connect();
                            } catch (IOException e) {
                                System.out.println("IOException. Msg: " + e.getMessage());
                            } catch (WebSocketException e) {
                                System.out.println("WebSocketException. Msg: " + e.getMessage());
                            } catch (NoSuchAlgorithmException ex) {
                            }
                        } else {
                            //TODO Error empty answer
                        }
                    } else {
                        //TODO Error bad type of answer   
                        System.out.println("Got empty list!");
                    }
                } else {
                    //TODO Error bad type of answer
                    System.out.println("Got empty list!");
                }
            } else {
                //TODO Error in response
                System.out.println("accountIdListener Got other: " + response.result.getClass());
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError. message: " + error.message);
        }
    };

    WitnessResponseListener accountListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            if (response.result.getClass() == ArrayList.class) {
                List l = (List) response.result;
                if (l.size() > 0) {
                    System.out.println("list class " + l.get(0).getClass());
                    if (l.get(0).getClass() == LinkedTreeMap.class) {
                        LinkedTreeMap ltm = (LinkedTreeMap) l.get(0);
                        accountName = (String) ltm.get("name");
                    } else {
                        //TODO Error bad type of answer   
                        System.out.println("Got bad type!");
                    }
                } else {
                    //TODO Error bad type of answer
                    System.out.println("Got empty list!");
                }
            } else {
                //TODO Error in response
                System.out.println("accountIdListener Got other: " + response.result.getClass());
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError. message: " + error.message);
        }
    };
}
