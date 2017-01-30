package de.bitsharesmunich.graphenej;

/**
 * <p>
 * Generic class used to represent a graphene object as defined in
 * <a href="http://docs.bitshares.org/development/blockchain/objects.html"></a>
 * </p>
 * Created by nelson on 11/8/16.
 */
public class GrapheneObject {
    public static final String KEY_ID = "id";

    public static final int PROTOCOL_SPACE = 1;
    public static final int IMPLEMENTATION_SPACE = 2;

    protected String id;
    protected int space;
    protected int type;
    protected long instance;

    public GrapheneObject(String id){
        this.id = id;
        String[] parts = id.split("\\.");
        if(parts.length == 3){
            this.space = Integer.parseInt(parts[0]);
            this.type = Integer.parseInt(parts[1]);
            this.instance = Long.parseLong(parts[2]);
        }
    }

    /**
     *
     * @return: A String containing the full object apiId in the form {space}.{type}.{instance}
     */
    public String getObjectId(){
        return String.format("%d.%d.%d", space, type, instance);
    }

    /**
     * Returns the type of this object.
     * @return: Instance of the ObjectType enum.
     */
    public ObjectType getObjectType(){
        switch(space){
            case PROTOCOL_SPACE:
                switch(type){
                    case 1:
                        return ObjectType.BASE_OBJECT;
                    case 2:
                        return ObjectType.ACCOUNT_OBJECT;
                    case 3:
                        return ObjectType.ASSET_OBJECT;
                    case 4:
                        return ObjectType.FORCE_SETTLEMENT_OBJECT;
                    case 5:
                        return ObjectType.COMMITTEE_MEMBER_OBJECT;
                    case 6:
                        return ObjectType.WITNESS_OBJECT;
                    case 7:
                        return ObjectType.LIMIT_ORDER_OBJECT;
                    case 8:
                        return ObjectType.CALL_ORDER_OBJECT;
                    case 9:
                        return ObjectType.CUSTOM_OBJECT;
                    case 10:
                        return ObjectType.PROPOSAL_OBJECT;
                    case 11:
                        return ObjectType.OPERATION_HISTORY_OBJECT;
                    case 12:
                        return ObjectType.WITHDRAW_PERMISSION_OBJECT;
                    case 13:
                        return ObjectType.VESTING_BALANCE_OBJECT;
                    case 14:
                        return ObjectType.WORKER_OBJECT;
                    case 15:
                        return ObjectType.BALANCE_OBJECT;
                }
            case IMPLEMENTATION_SPACE:
                switch(type){
                    case 0:
                        return ObjectType.GLOBAL_PROPERTY_OBJECT;
                    case 1:
                        return ObjectType.DYNAMIC_GLOBAL_PROPERTY_OBJECT;
                    case 3:
                        return ObjectType.ASSET_DYNAMIC_DATA;
                    case 4:
                        return ObjectType.ASSET_BITASSET_DATA;
                    case 5:
                        return ObjectType.ACCOUNT_BALANCE_OBJECT;
                    case 6:
                        return ObjectType.ACCOUNT_STATISTICS_OBJECT;
                    case 7:
                        return ObjectType.TRANSACTION_OBJECT;
                    case 8:
                        return ObjectType.BLOCK_SUMMARY_OBJECT;
                    case 9:
                        return ObjectType.ACCOUNT_TRANSACTION_HISTORY_OBJECT;
                    case 10:
                        return ObjectType.BLINDED_BALANCE_OBJECT;
                    case 11:
                        return ObjectType.CHAIN_PROPERTY_OBJECT;
                    case 12:
                        return ObjectType.WITNESS_SCHEDULE_OBJECT;
                    case 13:
                        return ObjectType.BUDGET_RECORD_OBJECT;
                    case 14:
                        return ObjectType.SPECIAL_AUTHORITY_OBJECT;
                }
        }
        return null;
    }
}