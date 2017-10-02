package cy.agorise.graphenej;

/**
 * Enum type used to list all possible object types and obtain their space + type id
 */

public enum ObjectType {
    BASE_OBJECT,
    ACCOUNT_OBJECT,
    ASSET_OBJECT,
    FORCE_SETTLEMENT_OBJECT,
    COMMITTEE_MEMBER_OBJECT,
    WITNESS_OBJECT,
    LIMIT_ORDER_OBJECT,
    CALL_ORDER_OBJECT,
    CUSTOM_OBJECT,
    PROPOSAL_OBJECT,
    OPERATION_HISTORY_OBJECT,
    WITHDRAW_PERMISSION_OBJECT,
    VESTING_BALANCE_OBJECT,
    WORKER_OBJECT,
    BALANCE_OBJECT,
    GLOBAL_PROPERTY_OBJECT,
    DYNAMIC_GLOBAL_PROPERTY_OBJECT,
    ASSET_DYNAMIC_DATA,
    ASSET_BITASSET_DATA,
    ACCOUNT_BALANCE_OBJECT,
    ACCOUNT_STATISTICS_OBJECT,
    TRANSACTION_OBJECT,
    BLOCK_SUMMARY_OBJECT,
    ACCOUNT_TRANSACTION_HISTORY_OBJECT,
    BLINDED_BALANCE_OBJECT,
    CHAIN_PROPERTY_OBJECT,
    WITNESS_SCHEDULE_OBJECT,
    BUDGET_RECORD_OBJECT,
    SPECIAL_AUTHORITY_OBJECT;

    private int getSpace(){
        int space = 1;
        switch(this){
            case BASE_OBJECT:
            case ACCOUNT_OBJECT:
            case ASSET_OBJECT:
            case FORCE_SETTLEMENT_OBJECT:
            case COMMITTEE_MEMBER_OBJECT:
            case WITNESS_OBJECT:
            case LIMIT_ORDER_OBJECT:
            case CALL_ORDER_OBJECT:
            case CUSTOM_OBJECT:
            case PROPOSAL_OBJECT:
            case OPERATION_HISTORY_OBJECT:
            case WITHDRAW_PERMISSION_OBJECT:
            case VESTING_BALANCE_OBJECT:
            case WORKER_OBJECT:
            case BALANCE_OBJECT:
                space = 1;
                break;
            case GLOBAL_PROPERTY_OBJECT:
            case DYNAMIC_GLOBAL_PROPERTY_OBJECT:
            case ASSET_DYNAMIC_DATA:
            case ASSET_BITASSET_DATA:
            case ACCOUNT_BALANCE_OBJECT:
            case ACCOUNT_STATISTICS_OBJECT:
            case TRANSACTION_OBJECT:
            case BLOCK_SUMMARY_OBJECT:
            case ACCOUNT_TRANSACTION_HISTORY_OBJECT:
            case BLINDED_BALANCE_OBJECT:
            case CHAIN_PROPERTY_OBJECT:
            case WITNESS_SCHEDULE_OBJECT:
            case BUDGET_RECORD_OBJECT:
            case SPECIAL_AUTHORITY_OBJECT:
                space = 2;
                break;
        }
        return space;
    }

    private int getType(){
        int type = 0;
        switch(this){
            case BASE_OBJECT:
                type = 1;
                break;
            case ACCOUNT_OBJECT:
                type = 2;
                break;
            case ASSET_OBJECT:
                type = 3;
                break;
            case FORCE_SETTLEMENT_OBJECT:
                type = 4;
                break;
            case COMMITTEE_MEMBER_OBJECT:
                type = 5;
                break;
            case WITNESS_OBJECT:
                type = 6;
                break;
            case LIMIT_ORDER_OBJECT:
                type = 7;
                break;
            case CALL_ORDER_OBJECT:
                type = 8;
                break;
            case CUSTOM_OBJECT:
                type = 9;
                break;
            case PROPOSAL_OBJECT:
                type = 10;
                break;
            case OPERATION_HISTORY_OBJECT:
                type = 11;
                break;
            case WITHDRAW_PERMISSION_OBJECT:
                type = 12;
                break;
            case VESTING_BALANCE_OBJECT:
                type = 13;
                break;
            case WORKER_OBJECT:
                type = 14;
                break;
            case BALANCE_OBJECT:
                type = 15;
                break;
            case GLOBAL_PROPERTY_OBJECT:
                type = 0;
                break;
            case DYNAMIC_GLOBAL_PROPERTY_OBJECT:
                type = 1;
                break;
            case ASSET_DYNAMIC_DATA:
                type = 3;
                break;
            case ASSET_BITASSET_DATA:
                type = 4;
                break;
            case ACCOUNT_BALANCE_OBJECT:
                type = 5;
                break;
            case ACCOUNT_STATISTICS_OBJECT:
                type = 6;
                break;
            case TRANSACTION_OBJECT:
                type = 7;
                break;
            case BLOCK_SUMMARY_OBJECT:
                type = 8;
                break;
            case ACCOUNT_TRANSACTION_HISTORY_OBJECT:
                type = 9;
                break;
            case BLINDED_BALANCE_OBJECT:
                type = 10;
                break;
            case CHAIN_PROPERTY_OBJECT:
                type = 11;
                break;
            case WITNESS_SCHEDULE_OBJECT:
                type = 12;
                break;
            case BUDGET_RECORD_OBJECT:
                type = 13;
                break;
            case SPECIAL_AUTHORITY_OBJECT:
                type = 14;
        }
        return type;
    }

    /**
     * This method is used to return the generic object type in the form space.type.0.
     *
     * Not to be confused with {@link GrapheneObject#getObjectId()}, which will return
     * the full object id in the form space.type.id.
     *
     * @return: The generic object type
     */
    public String getGenericObjectId(){
        return String.format("%d.%d.0", getSpace(), getType());
    }
}
