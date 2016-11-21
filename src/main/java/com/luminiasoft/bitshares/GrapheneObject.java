package com.luminiasoft.bitshares;



/**
 * <p>
 * Generic class used to represent a graphene object as defined in
 * <a href="http://docs.bitshares.org/development/blockchain/objects.html"></a>
 * </p>
 * Created by nelson on 11/8/16.
 */
public class GrapheneObject {
    protected int space;
    protected int type;
    protected long instance;

    public GrapheneObject(String id){
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
}
