# graphenej
A Java library for mobile app Developers; Graphene/Bitshares blockchain.

## Usage

In your root build.gradle, add this if you don't already have it.

```Groovy
allprojects {
    repositories {
        jcenter()
    }
}
```

In yout app module, add the following dependency:

```Groovy
dependencies {
    compile 'com.github.bilthon:graphenej:0.4.2'
}
```

## Example

### Very simple funds transfer

This is a simple transfer operation of 1 BTS from account **bilthon-15** to **bilthon-5**
```java
// Creating a transfer operation
TransferOperation transferOperation = new TransferOperationBuilder()
        .setTransferAmount(new AssetAmount(UnsignedLong.valueOf(100000), new Asset("1.3.0")))
        .setSource(new UserAccount("1.2.143563"))       // bilthon-15
        .setDestination(new UserAccount("1.2.139313"))  // bilthon-5
        .setFee(new AssetAmount(UnsignedLong.valueOf(264174), new Asset("1.3.0")))
        .build();
        
// Adding operations to the operation list
ArrayList<BaseOperation> operationList = new ArrayList<>();
operationList.add(transferOperation);

// Creating a transaction instance
Transaction transaction = new Transaction(sourcePrivateKey, null, operationList);
```

From here on, it is just a matter of creating a websocket connection and using a custom handler called
```TransactionBroadcastSequence``` in order to broadcast it to the witness node.

```java
// Setting up a secure websocket connection.
SSLContext context = null;
context = NaiveSSLContext.getInstance("TLS");
WebSocketFactory factory = new WebSocketFactory();
factory.setSSLContext(context);

WebSocket mWebSocket = factory.createSocket(FULL_NODE_URL);

mWebSocket.addListener(new TransactionBroadcastSequence(transaction, new Asset("1.3.0"), listener));
mWebSocket.connect();
```

The provided ```listener``` is an instance of the class ```WitnessResponseListener```.
