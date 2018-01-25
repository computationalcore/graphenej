package cy.agorise.graphenej;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Created by nelson on 5/5/17.
 */
public class ObjectTypeTest {

    @Test
    public void getGenericObjectId() throws Exception {
        ObjectType baseObject = ObjectType.BASE_OBJECT;
        ObjectType accountObject = ObjectType.ACCOUNT_OBJECT;
        ObjectType forceSettlementObject = ObjectType.FORCE_SETTLEMENT_OBJECT;
        ObjectType globalPropertiesObject = ObjectType.GLOBAL_PROPERTY_OBJECT;
        ObjectType specialAuthorityObject = ObjectType.SPECIAL_AUTHORITY_OBJECT;

        Assert.assertEquals("1.1.0", baseObject.getGenericObjectId());
        Assert.assertEquals("1.2.0", accountObject.getGenericObjectId());
        Assert.assertEquals("1.4.0", forceSettlementObject.getGenericObjectId());
        Assert.assertEquals("2.0.0", globalPropertiesObject.getGenericObjectId());
        Assert.assertEquals("2.14.0", specialAuthorityObject.getGenericObjectId());
    }
}