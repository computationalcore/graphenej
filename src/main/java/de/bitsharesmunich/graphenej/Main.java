package de.bitsharesmunich.graphenej;

public class Main {

    // Brain key from Nelson's app referencing the bilthon-83 account
    public static final String BILTHON_83_BRAIN_KEY = System.getenv("BILTHON_83_BRAIN_KEY");

    public static final String BILTHON_83_ORIGINAL_BRAIN_KEY = System.getenv("BILTHON_83_ORIGINAL_BRAIN_KEY");

    public static final String BILTHON_1_BRAIN_KEY = System.getenv("BILTHON_1_BRAIN_KEY");

    public static final String BILTHON_5_BRAIN_KEY = System.getenv("BILTHON_5_BRAIN_KEY");

    public static final String BILTHON_7_BRAIN_KEY = System.getenv("BILTHON_7_BRAIN_KEY");

    public static final String BIP39_KEY = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    public static final String WIF = "5KMzB2GqGhnh7ufhgddmz1eKPHS72uTLeL9hHjSvPb1UywWknF5";

    // Static block information used for transaction serialization tests
    public static int REF_BLOCK_NUM = 56204;
    public static int REF_BLOCK_PREFIX = 1614747814;
    public static int RELATIVE_EXPIRATION = 1478385607;

    public static void main(String[] args) {
        Test test = new Test();

//        test.testTransactionSerialization();
//        ECKey.ECDSASignature signature = test.testSigning();

//        try {
//            test.testWebSocketTransfer();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        test.testCustomSerializer();
//        test.testUserAccountSerialization();
//        test.testTransactionSerialization();
//        test.testLoginSerialization();
//        test.testNetworkBroadcastSerialization();
//        test.testNetworkBroadcastDeserialization();
//        test.testGetDynamicParams();
//        test.testGetRequiredFeesSerialization();
//        test.testRequiredFeesResponse();
//        test.testTransactionBroadcastSequence();
//        test.testAccountLookupDeserialization();
//        test.testPrivateKeyManipulations();
//        test.testPublicKeyManipulations();
//        test.testGetAccountByName();
//        test.testGetRequiredFees();
//        test.testRandomNumberGeneration();
//        test.testBrainKeyOperations(false);
//        test.testBip39Opertion();
//        test.testAccountNamebyAddress();
//        test.testAccountNameById();
//        test.testRelativeAccountHistory();
//        test.testingInvoiceGeneration();
//        test.testCompression();
//        test.testAccountUpdateSerialization();
//        test.testAccountUpdateOperationBroadcast();
//        test.testCreateBinFile();
//        test.testImportBinFile();
//          test.testLookupAccounts();
//        test.testLookupAccounts();
//        test.testDecodeMemo();
//        test.testGetRelativeAccountHistory();
//        test.testLookupAssetSymbols();
//        test.testListAssets();
//        test.testGetObjects();
//        test.testGetBlockHeader();
//        test.testGetLimitOrders();
//        test.testGetTradeHistory();
//        test.testAssetSerialization();
//        test.testGetMarketHistory();
        test.testGetAccountBalances();
    }
}
