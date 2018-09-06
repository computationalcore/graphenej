package cy.agorise.graphenej.api;

import com.neovisionaries.ws.client.WebSocketException;

import org.junit.Test;

import java.util.List;
import cy.agorise.graphenej.UserAccount;
import cy.agorise.graphenej.interfaces.WitnessResponseListener;
import cy.agorise.graphenej.models.BaseResponse;
import cy.agorise.graphenej.models.OperationHistory;
import cy.agorise.graphenej.models.WitnessResponse;
import cy.agorise.graphenej.operations.TransferOperation;

/**
 * Created by nelson on 9/25/17.
 */

public class GetRelativeAccountHistoryTest extends BaseApiTest {
    private final String TAG = this.getClass().getName();

    private int HISTORICAL_TRANSFER_BATCH_SIZE = 10;
    private final UserAccount bilthon_7 = new UserAccount("1.2.140994");
    private int historicalTransferCount;

    @Test
    public void testRelativeAccountHistory(){
        int start = 0;
        GetRelativeAccountHistory mGetRelativeAccountHistory = new GetRelativeAccountHistory(bilthon_7, 0, HISTORICAL_TRANSFER_BATCH_SIZE, start, mTransferHistoryListener);
        mWebSocket.addListener(mGetRelativeAccountHistory);

        try{
            mWebSocket.connect();
            synchronized (this){
                wait();
            }
        }catch (WebSocketException e) {
            System.out.println("WebSocketException. Msg: " + e.getMessage());
        } catch (InterruptedException e) {
            System.out.println("InterruptedException. Msg: "+e.getMessage());
        }
    }

    /**
     * Callback activated once we get a response back from the full node telling us about the
     * transfer history of the current account.
     */
    private WitnessResponseListener mTransferHistoryListener = new WitnessResponseListener() {

        @Override
        public void onSuccess(WitnessResponse response) {
            System.out.println("mTransferHistoryListener.onSuccess");
            historicalTransferCount++;
            WitnessResponse<List<OperationHistory>> resp = response;
            for(OperationHistory historicalTransfer : resp.result){
                if(historicalTransfer.getOperation() != null){
                    System.out.println("Got transfer operation!");
                    TransferOperation transferOperation = (TransferOperation) historicalTransfer.getOperation();
                    System.out.println(String.format("%s - > %s, memo: %s",
                            transferOperation.getFrom().getObjectId(),
                            transferOperation.getTo().getObjectId(),
                            transferOperation.getMemo() == null ? "*" : transferOperation.getMemo().getStringMessage()));
                }
            }
        }

        @Override
        public void onError(BaseResponse.Error error) {
            System.out.println("onError. Msg: "+error.message);
        }
    };
}
