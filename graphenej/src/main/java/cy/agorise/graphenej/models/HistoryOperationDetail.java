package cy.agorise.graphenej.models;

import java.util.List;

/**
 * Model class used to represent the struct defined in graphene::app::history_operation_detail and
 * returned as response to the 'get_account_history_by_operations' API call.
 */
public class HistoryOperationDetail {
    private long total_count;
    List<OperationHistory> operation_history_objs;

    public long getTotalCount() {
        return total_count;
    }

    public void setTotalCount(long total_count) {
        this.total_count = total_count;
    }

    public List<OperationHistory> getOperationHistoryObjs() {
        return operation_history_objs;
    }

    public void setOperationHistoryObjs(List<OperationHistory> operation_history_objs) {
        this.operation_history_objs = operation_history_objs;
    }
}
