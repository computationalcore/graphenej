package com.luminiasoft.bitshares;

import org.bitcoinj.core.ECKey;

import java.io.IOException;

public class Main {

    // Brain key from Nelson's app referencing the bilthon-83 account
    public static final String BRAIN_KEY = "PUMPER ISOTOME SERE STAINER CLINGER MOONLIT CHAETA UPBRIM AEDILIC BERTHER NIT SHAP SAID SHADING JUNCOUS CHOUGH";

    //public static final String BRAIN_KEY = "TWIXT SERMO TRILLI AUDIO PARDED PLUMET BIWA REHUNG MAUDLE VALVULA OUTBURN FEWNESS ALIENER UNTRACE PRICH TROKER";
    //public static final String BRAIN_KEY = "SIVER TIKKER FOGO HOMINAL PRAYER LUTEIN SMALLY ACARID MEROPIA TRANCE BOGONG IDDAT HICKORY SOUTANE MOOD DOWSER";
    public static final String BIP39_KEY = "abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon abandon about";

    // WIF from Nelson's app referencing the bilthon-83 account
//    public static final String WIF = "5J96pne45qWM1WpektoeazN6k9Mt93jQ7LyueRxFfEMTiy6yxjM";
    // Brain key from an empty account created by the cli_wallet
//    public static final String BRAIN_KEY = "TWIXT SERMO TRILLI AUDIO PARDED PLUMET BIWA REHUNG MAUDLE VALVULA OUTBURN FEWNESS ALIENER UNTRACE PRICH TROKER";
    // WIF from an emty account created by the cli_wallet
    public static final String WIF = "5KMzB2GqGhnh7ufhgddmz1eKPHS72uTLeL9hHjSvPb1UywWknF5";

    public static final String EXTERNAL_SIGNATURE = "1f36c41acb774fcbc9c231b5895ec9701d6872729098d8ea56d78dda72a6b54252694db85d7591de5751b7aea06871da15d63a1028758421607ffc143e53ef3306";

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
        //test.testUserAccountSerialization();
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
        test.testCreateBinFile();
    }
}
