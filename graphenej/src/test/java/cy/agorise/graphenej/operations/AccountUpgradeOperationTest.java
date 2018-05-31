package cy.agorise.graphenej.operations;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.UnsignedLong;

import org.bitcoinj.core.ECKey;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;

import cy.agorise.graphenej.Asset;
import cy.agorise.graphenej.AssetAmount;
import cy.agorise.graphenej.BaseOperation;
import cy.agorise.graphenej.BlockData;
import cy.agorise.graphenej.BrainKey;
import cy.agorise.graphenej.Chains;
import cy.agorise.graphenej.Transaction;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.Util;

public class AccountUpgradeOperationTest {
    private static final String BILTHON_16_BRAIN_KEY = "SOAPILY GASSING FIFIE OZONATE WHYO TOPLINE PRISMY ZEUGMA GLOTTIC DAVEN CORODY PFUI";

    private final Asset CORE = new Asset("1.3.0");

    // This was obtained by calling the 'serialized_transaction' command of the cli_wallet utility and stripping it away
    // from its signature.
    //
    // Ex:
    // >>> serialize_transaction {"expiration":"2017-04-18T21:42:47","signatures":["207dd63cb89d05266ed4af0935270f269839154d5f04229041997e6c98f10bba21752d6638eb7539c69fb22874fb1e39aad00a5eed6dd5e81e14845e79b60a0590"],"operations":[[8,{"fee":{"amount":69470219,"asset_id":"1.3.0"},"account_to_upgrade":"1.2.143569","upgrade_to_lifetime_member":"true","extensions":[]}]],"extensions":[],"ref_block_num":3703,"ref_block_prefix":2015738269}
    private final String SERIALIZED_TX = "770e9db925785788f65801080b0824040000000000d1e108010000";

    @Test
    public void testOperationSerialization(){
        AccountUpgradeOperationBuilder builder = new AccountUpgradeOperationBuilder()
                .setAccountToUpgrade(new UserAccount("1.2.143569"))
                .setFee(new AssetAmount(UnsignedLong.valueOf(69470219), CORE))
                .setIsUpgrade(true);

        AccountUpgradeOperation upgradeOperation = builder.build();

        ArrayList<BaseOperation> operations = new ArrayList<>();
        operations.add(upgradeOperation);
        ECKey privateKey = new BrainKey(BILTHON_16_BRAIN_KEY, 0).getPrivateKey();
        BlockData blockData = new BlockData(3703, 2015738269, 1492551764);

        Transaction tx = new Transaction(privateKey, blockData, operations);

        // Serialized transaction
        byte[] serialized = tx.toBytes();

        // The expected serialized transaction is a concatenation of the chain id + the serialized tx
        byte[] expectedTx = Bytes.concat(Util.hexToBytes(Chains.BITSHARES.CHAIN_ID),Util.hexToBytes(SERIALIZED_TX));

        Assert.assertArrayEquals(expectedTx, serialized);
    }
}
