package com.luminiasoft.bitshares;

import com.google.common.primitives.UnsignedLong;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.luminiasoft.bitshares.errors.MalformedTransactionException;
import com.luminiasoft.bitshares.interfaces.WitnessResponseListener;
import com.luminiasoft.bitshares.models.*;
import com.luminiasoft.bitshares.ws.GetAccountByName;
import com.luminiasoft.bitshares.ws.GetRequiredFees;
import com.luminiasoft.bitshares.ws.TransactionBroadcastSequence;
import com.neovisionaries.ws.client.*;
import org.bitcoinj.core.*;
import org.spongycastle.crypto.Digest;
import org.spongycastle.crypto.digests.SHA512Digest;
import org.spongycastle.crypto.prng.DigestRandomGenerator;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by nelson on 11/9/16.
 */
public class Test {
    public static final String WITNESS_URL = "ws://api.devling.xyz:8088";
    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }


    private WitnessResponseListener mListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            if(response.result.getClass() == AccountProperties.class){
                AccountProperties accountProperties = (AccountProperties) response.result;
                System.out.println("Got account properties");
                System.out.println("id: "+accountProperties.id);
            }else if(response.result.getClass() == ArrayList.class){
                List l = (List) response.result;
                if(l.size() > 0){
                    if(l.get(0).getClass() == AssetAmount.class){
                        AssetAmount assetAmount = (AssetAmount) l.get(0);
                        System.out.println("Got fee");
                        System.out.println("amount: "+assetAmount.getAmount()+", asset id: "+assetAmount.getAsset().getObjectId());
                    }
                }else{
                    System.out.println("Got empty list!");
                }
            }else{
                System.out.println("Got other: "+response.result.getClass());
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError. message: "+error.message);
        }
    };

    public ECKey.ECDSASignature testSigning() {
        byte[] serializedTransaction = this.transaction.toBytes();
        Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction));
        byte[] bytesDigest = hash.getBytes();
        ECKey sk = transaction.getPrivateKey();
        ECKey.ECDSASignature signature = sk.sign(hash);
        return signature;
    }

    public String testSigningMessage() {
        byte[] serializedTransaction = this.transaction.toBytes();
        Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction));
        ECKey sk = transaction.getPrivateKey();
        return sk.signMessage(hash.toString());
    }

    public byte[] signMessage() {
        byte[] serializedTransaction = this.transaction.toBytes();
        Sha256Hash hash = Sha256Hash.wrap(Sha256Hash.hash(serializedTransaction));
        System.out.println(">> digest <<");
        System.out.println(Util.bytesToHex(hash.getBytes()));
        ECKey sk = transaction.getPrivateKey();
        System.out.println("Private key bytes");
        System.out.println(Util.bytesToHex(sk.getPrivKeyBytes()));
        boolean isCanonical = false;
        int recId = -1;
        ECKey.ECDSASignature sig = null;
        while (!isCanonical) {
            sig = sk.sign(hash);
            if (!sig.isCanonical()) {
                System.out.println("Signature was not canonical, retrying");
                continue;
            } else {
                System.out.println("Signature is canonical");
                isCanonical = true;
            }
            // Now we have to work backwards to figure out the recId needed to recover the signature.
            for (int i = 0; i < 4; i++) {
                ECKey k = ECKey.recoverFromSignature(i, sig, hash, sk.isCompressed());
                if (k != null && k.getPubKeyPoint().equals(sk.getPubKeyPoint())) {
                    recId = i;
                    break;
                } else {
                    if (k == null) {
                        System.out.println("Recovered key was null");
                    }
                    if (k.getPubKeyPoint().equals(sk.getPubKeyPoint())) {
                        System.out.println("Recovered pub point is not equal to sk pub point");
                    }
                }
            }
            if (recId == -1)
                throw new RuntimeException("Could not construct a recoverable key. This should never happen.");
        }
        int headerByte = recId + 27 + (sk.isCompressed() ? 4 : 0);
        byte[] sigData = new byte[65];  // 1 header + 32 bytes for R + 32 bytes for S
        sigData[0] = (byte) headerByte;
        System.arraycopy(Utils.bigIntegerToBytes(sig.r, 32), 0, sigData, 1, 32);
        System.arraycopy(Utils.bigIntegerToBytes(sig.s, 32), 0, sigData, 33, 32);
        System.out.println("recId: " + recId);
        System.out.println("r: " + Util.bytesToHex(sig.r.toByteArray()));
        System.out.println("s: " + Util.bytesToHex(sig.s.toByteArray()));
        return sigData;
//        return new String(Base64.encode(sigData), Charset.forName("UTF-8"));
    }

    public byte[] testTransactionSerialization(long head_block_number, String head_block_id, long relative_expiration) {
        BlockData blockData = new BlockData(head_block_number, head_block_id, relative_expiration);

        ArrayList<BaseOperation> operations = new ArrayList<BaseOperation>();
        UserAccount from = new UserAccount("1.2.138632");
        UserAccount to = new UserAccount("1.2.129848");
        AssetAmount amount = new AssetAmount(UnsignedLong.valueOf(100), new Asset("1.3.120"));
        AssetAmount fee = new AssetAmount(UnsignedLong.valueOf(264174), new Asset("1.3.0"));
        operations.add(new Transfer(from, to, amount, fee));
        this.transaction = new Transaction(Main.WIF, blockData, operations);
        byte[] serializedTransaction = this.transaction.toBytes();
        System.out.println("Serialized transaction");
        System.out.println(Util.bytesToHex(serializedTransaction));
        return serializedTransaction;
    }

    public void testWebSocketTransfer() throws IOException {
        String login = "{\"id\":%d,\"method\":\"call\",\"params\":[1,\"login\",[\"\",\"\"]]}";
        String getDatabaseId = "{\"method\": \"call\", \"params\": [1, \"database\", []], \"jsonrpc\": \"2.0\", \"id\": %d}";
        String getHistoryId = "{\"method\": \"call\", \"params\": [1, \"history\", []], \"jsonrpc\": \"2.0\", \"id\": %d}";
        String getNetworkBroadcastId = "{\"method\": \"call\", \"params\": [1, \"network_broadcast\", []], \"jsonrpc\": \"2.0\", \"id\": %d}";
        String getDynamicParameters = "{\"method\": \"call\", \"params\": [0, \"get_dynamic_global_properties\", []], \"jsonrpc\": \"2.0\", \"id\": %d}";
        String rawPayload = "{\"method\": \"call\", \"params\": [%d, \"broadcast_transaction\", [{\"expiration\": \"%s\", \"signatures\": [\"%s\"], \"operations\": [[0, {\"fee\": {\"amount\": 264174, \"asset_id\": \"1.3.0\"}, \"amount\": {\"amount\": 100, \"asset_id\": \"1.3.120\"}, \"to\": \"1.2.129848\", \"extensions\": [], \"from\": \"1.2.138632\"}]], \"ref_block_num\": %d, \"extensions\": [], \"ref_block_prefix\": %d}]], \"jsonrpc\": \"2.0\", \"id\": %d}";

//        String url = "wss://bitshares.openledger.info/ws";
        String url = "ws://api.devling.xyz:8088";
        WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);

        // Create a WebSocket. The timeout value set above is used.
        WebSocket ws = factory.createSocket(url);

        ws.addListener(new WebSocketAdapter() {

            private DynamicGlobalProperties dynProperties;
            private int networkBroadcastApiId;

            @Override
            public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
                System.out.println("onConnected");
                String payload = String.format(login, 1);
                System.out.println(">>");
                System.out.println(payload);
                websocket.sendText(payload);
            }

            @Override
            public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
                System.out.println("onDisconnected");
            }

            @Override
            public void onTextFrame(WebSocket websocket, WebSocketFrame frame) throws Exception {
                System.out.println("<<");
                String response = frame.getPayloadText();
                System.out.println(response);
                Gson gson = new Gson();
                BaseResponse baseResponse = gson.fromJson(response, BaseResponse.class);
//                if(baseResponse.id.equals("1")){
//                    String payload = String.format(getDatabaseId, 2);
//                    System.out.println(">>");
//                    System.out.println(payload);
//                    websocket.sendText(payload);
//                }else if(baseResponse.id.equals("2")){
//                    String payload = String.format(getHistoryId, 3);
//                    System.out.println(">>");
//                    System.out.println(payload);
//                    websocket.sendText(payload);
//                }else if(baseResponse.id.equals("3")){
                if (baseResponse.id == 1) {
                    String payload = String.format(getNetworkBroadcastId, 2);
                    System.out.println(">>");
                    System.out.println(payload);
                    websocket.sendText(payload);
//                }else if(baseResponse.id.equals("4")){
                }
                if (baseResponse.id == 2) {
                    String payload = String.format(getDynamicParameters, 3);
                    Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
                    WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
                    networkBroadcastApiId = witnessResponse.result.intValue();
                    System.out.println(">>");
                    System.out.println(payload);
                    websocket.sendText(payload);
                } else if (baseResponse.id == 3) {
                    // Got dynamic properties
                    Type DynamicGlobalPropertiesResponse = new TypeToken<WitnessResponse<DynamicGlobalProperties>>() {
                    }.getType();
                    WitnessResponse<DynamicGlobalProperties> witnessResponse = gson.fromJson(response, DynamicGlobalPropertiesResponse);
                    dynProperties = witnessResponse.result;

                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
                    Date date = dateFormat.parse(dynProperties.time);
                    long expirationTime = (date.getTime() / 1000) + 30;
                    testTransactionSerialization(dynProperties.head_block_number, dynProperties.head_block_id, expirationTime);

                    BlockData blockData = new BlockData(dynProperties.head_block_number, dynProperties.head_block_id, expirationTime);
                    byte[] signatureBytes = signMessage();

                    String payload = String.format(
                            rawPayload,
                            networkBroadcastApiId,
                            dateFormat.format(new Date(expirationTime * 1000)),
                            Util.bytesToHex(signatureBytes),
                            blockData.getRefBlockNum(),
                            blockData.getRefBlockPrefix(),
                            4);
                    System.out.println(">>");
                    System.out.println(payload);
                    websocket.sendText(payload);
                }
            }

            @Override
            public void onError(WebSocket websocket, WebSocketException cause) throws Exception {
                System.out.println("onError");
            }

            @Override
            public void onUnexpectedError(WebSocket websocket, WebSocketException cause) throws Exception {
                System.out.println("onUnexpectedError");
            }

            @Override
            public void handleCallbackError(WebSocket websocket, Throwable cause) throws Exception {
                System.out.println("handleCallbackError. Msg: " + cause.getMessage());
                StackTraceElement[] stackTrace = cause.getStackTrace();
                for (StackTraceElement line : stackTrace) {
                    System.out.println(line.toString());
                }
            }
        });
        try {
            // Connect to the server and perform an opening handshake.
            // This method blocks until the opening handshake is finished.
            ws.connect();
        } catch (OpeningHandshakeException e) {
            // A violation against the WebSocket protocol was detected
            // during the opening handshake.
            System.out.println("OpeningHandshakeException");
        } catch (WebSocketException e) {
            // Failed to establish a WebSocket connection.
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        }
    }

    public void testCustomSerializer() {
        AssetAmount amount = new AssetAmount(UnsignedLong.valueOf(100), new Asset("1.3.120"));
        String jsonAmount = amount.toJsonString();
        System.out.println("JSON amount");
        System.out.println(jsonAmount);
    }

    public void testTransactionSerialization() {
        try {
            Transaction transaction = new TransferTransactionBuilder()
                    .setSource(new UserAccount("1.2.138632"))
                    .setDestination(new UserAccount("1.2.129848"))
                    .setAmount(new AssetAmount(UnsignedLong.valueOf(100), new Asset("1.3.120")))
                    .setFee(new AssetAmount(UnsignedLong.valueOf(264174), new Asset("1.3.0")))
                    .setBlockData(new BlockData(43408, 1430521623, 1479231969))
                    .setPrivateKey(DumpedPrivateKey.fromBase58(null, Main.WIF).getKey())
                    .build();

            ArrayList<Serializable> transactionList = new ArrayList<>();
            transactionList.add(transaction);
            ApiCall call = new ApiCall(4, "call", "broadcast_transaction", transactionList, "2.0", 1);
            String jsonCall = call.toJsonString();
            System.out.println("json call");
            System.out.println(jsonCall);
        } catch (MalformedTransactionException e) {
            System.out.println("MalformedTransactionException. Msg: " + e.getMessage());
        }
    }

    public void testLoginSerialization() {
        ArrayList<Serializable> loginParams = new ArrayList<>();
//        loginParams.add("nelson");
//        loginParams.add("supersecret");
        loginParams.add(null);
        loginParams.add(null);
        ApiCall loginCall = new ApiCall(1, "login", loginParams, "2.0", 1);
        String jsonLoginCall = loginCall.toJsonString();
        System.out.println("login call");
        System.out.println(jsonLoginCall);
    }

    public void testNetworkBroadcastSerialization() {
        ArrayList<Serializable> params = new ArrayList<>();
        ApiCall networkParamsCall = new ApiCall(3, "network_broadcast", params, "2.0", 1);
        String call = networkParamsCall.toJsonString();
        System.out.println("network broadcast");
        System.out.println(call);
    }

    public void testNetworkBroadcastDeserialization(){
        String response = "{\"id\":2,\"result\":2}";
        Gson gson = new Gson();
        Type ApiIdResponse = new TypeToken<WitnessResponse<Integer>>() {}.getType();
        WitnessResponse<Integer> witnessResponse = gson.fromJson(response, ApiIdResponse);
    }

    public void testGetDynamicParams() {
        ArrayList<Serializable> emptyParams = new ArrayList<>();
        ApiCall getDynamicParametersCall = new ApiCall(0, "get_dynamic_global_properties", emptyParams, "2.0", 0);
        System.out.println(getDynamicParametersCall.toJsonString());
    }

    public void testRequiredFeesResponse() {
        String response = "{\"id\":1,\"result\":[{\"amount\":264174,\"asset_id\":\"1.3.0\"}]}";
        Type AccountLookupResponse = new TypeToken<WitnessResponse<List<AssetAmount>>>() {}.getType();
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(AssetAmount.class, new AssetAmount.AssetDeserializer());
        WitnessResponse<List<AssetAmount>> witnessResponse = gsonBuilder.create().fromJson(response, AccountLookupResponse);
        for(AssetAmount assetAmount : witnessResponse.result){
            System.out.println("asset : "+assetAmount.toJsonString());
        }
    }

    public void testTransactionBroadcastSequence(){
        String url = "ws://api.devling.xyz:8088";
        WitnessResponseListener listener = new WitnessResponseListener() {
            @Override
            public void onSuccess(WitnessResponse response) {
                System.out.println("onSuccess");
            }

            @Override
            public void onError(BaseResponse.Error error) {
                System.out.println("onError");
                System.out.println(error.data.message);
            }
        };

        try{
            Transaction transaction = new TransferTransactionBuilder()
                    .setSource(new UserAccount("1.2.138632"))
                    .setDestination(new UserAccount("1.2.129848"))
                    .setAmount(new AssetAmount(UnsignedLong.valueOf(100), new Asset("1.3.120")))
                    .setFee(new AssetAmount(UnsignedLong.valueOf(264174), new Asset("1.3.0")))
                    .setBlockData(new BlockData(43408, 1430521623, 1479231969))
                    .setPrivateKey(DumpedPrivateKey.fromBase58(null, Main.WIF).getKey())
                    .build();

            ArrayList<Serializable> transactionList = new ArrayList<>();
            transactionList.add(transaction);
            ApiCall call = new ApiCall(4, "call", "broadcast_transaction", transactionList, "2.0", 1);

            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            try {
                WebSocket mWebSocket = factory.createSocket(url);
                mWebSocket.addListener(new TransactionBroadcastSequence(transaction, listener));
                mWebSocket.connect();
            } catch (IOException e) {
                System.out.println("IOException. Msg: "+e.getMessage());
            } catch (WebSocketException e) {
                System.out.println("WebSocketException. Msg: "+e.getMessage());
            }
        }catch(MalformedTransactionException e){
            System.out.println("MalformedTransactionException. Msg: "+e.getMessage());
        }
    }

    public void testAccountLookupDeserialization(){
        String response = "{\"id\":1,\"result\":[[\"ken\",\"1.2.3111\"],[\"ken-1\",\"1.2.101491\"],[\"ken-k\",\"1.2.108646\"]]}";
        Type AccountLookupResponse = new TypeToken<WitnessResponse<List<List<String>>>>() {}.getType();
        Gson gson = new Gson();
        WitnessResponse<List<List<String>>> witnessResponse = gson.fromJson(response, AccountLookupResponse);
        for(int i = 0; i < witnessResponse.result.size(); i++){
            System.out.println("suggested name: "+witnessResponse.result.get(i).get(0));
        }
    }

    public void testPrivateKeyManipulations(){
        ECKey privateKey = DumpedPrivateKey.fromBase58(null, Main.WIF).getKey();
        System.out.println("private key..............: "+Util.bytesToHex(privateKey.getSecretBytes()));
        System.out.println("public key uncompressed..: "+Util.bytesToHex(privateKey.getPubKey()));
        System.out.println("public key compressed....: "+Util.bytesToHex(privateKey.getPubKeyPoint().getEncoded(true)));
        System.out.println("base58...................: "+Base58.encode(privateKey.getPubKeyPoint().getEncoded(true)));
        System.out.println("base58...................: "+Base58.encode(privateKey.getPubKey()));
        String brainKeyWords = "PUMPER ISOTOME SERE STAINER CLINGER MOONLIT CHAETA UPBRIM AEDILIC BERTHER NIT SHAP SAID SHADING JUNCOUS CHOUGH";
        BrainKey brainKey = new BrainKey(brainKeyWords, 0);
    }

    public void testGetAccountByName(){
        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            WebSocket mWebSocket = factory.createSocket(WITNESS_URL);
            mWebSocket.addListener(new GetAccountByName("bilthon-83", mListener));
            mWebSocket.connect();
        } catch (IOException e) {
            System.out.println("IOException. Msg: "+e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: "+e.getMessage());
        }
    }

    public void testGetRequiredFees() {
        ArrayList<Serializable> accountParams = new ArrayList<>();
        Asset asset = new Asset("1.3.0");
        UserAccount from = new UserAccount("1.2.138632");
        UserAccount to = new UserAccount("1.2.129848");
        AssetAmount amount = new AssetAmount(UnsignedLong.valueOf(100), new Asset("1.3.120"));
        AssetAmount fee = new AssetAmount(UnsignedLong.valueOf(264174), new Asset("1.3.0"));
        Transfer transfer = new Transfer(from, to, amount, fee);
        ArrayList<BaseOperation> operations = new ArrayList<>();
        operations.add(transfer);

        accountParams.add(operations);
        accountParams.add(asset.getObjectId());

        try {
            WebSocketFactory factory = new WebSocketFactory().setConnectionTimeout(5000);
            WebSocket mWebSocket = factory.createSocket(WITNESS_URL);
            mWebSocket.addListener(new GetRequiredFees(operations, asset, mListener));
            mWebSocket.connect();
        } catch (IOException e) {
            System.out.println("IOException. Msg: "+e.getMessage());
        } catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: "+e.getMessage());
        }
    }

    public void testRandomNumberGeneration(){
        byte[] seed = new byte[] { new Long(System.nanoTime()).byteValue() };
        doCountTest(new SHA512Digest(), seed);
    }

    private void doCountTest(Digest digest, byte[] seed)//, byte[] expectedXors)
    {
        DigestRandomGenerator generator = new DigestRandomGenerator(digest);
        byte[] output = new byte[digest.getDigestSize()];
        int[] averages = new int[digest.getDigestSize()];
        byte[] ands = new byte[digest.getDigestSize()];
        byte[] xors = new byte[digest.getDigestSize()];
        byte[] ors = new byte[digest.getDigestSize()];

        generator.addSeedMaterial(seed);

        for (int i = 0; i != 1000000; i++)
        {
            generator.nextBytes(output);
            for (int j = 0; j != output.length; j++)
            {
                averages[j] += output[j] & 0xff;
                ands[j] &= output[j];
                xors[j] ^= output[j];
                ors[j] |= output[j];
            }
        }

        for (int i = 0; i != output.length; i++) {
            if ((averages[i] / 1000000) != 127) {
                System.out.println("average test failed for " + digest.getAlgorithmName());
            }
            System.out.println("averages["+i+"] / 1000000: "+averages[i] / 1000000);
            if (ands[i] != 0) {
                System.out.println("and test failed for " + digest.getAlgorithmName());
            }
            if ((ors[i] & 0xff) != 0xff) {
                System.out.println("or test failed for " + digest.getAlgorithmName());
            }
//            if (xors[i] != expectedXors[i]) {
//                System.out.println("xor test failed for " + digest.getAlgorithmName());
//            }
        }
    }

    /**
     * The final purpose of this test is to convert the plain brainkey at Main.BRAIN_KEY
     * into the WIF at Main.WIF
     */
    public void testBrainKeyOperations(){
        BrainKey brainKey = new BrainKey(Main.BRAIN_KEY, 0);
    }

}
