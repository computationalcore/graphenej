package cy.agorise.graphenej;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;

/**
 * Created by nelson on 12/16/16.
 */
public class AuthorityTest {
    private Authority authority;
    private Authority sameAuthority;
    private Authority differentAuthority;
    private Authority keyAuthority1;
    private Authority keyAuthority2;

    @Before
    public void setUp() throws Exception {
        authority = new Authority();
        sameAuthority = new Authority();
        HashMap<UserAccount, Long> accountAuthorityMap = new HashMap<>();
        UserAccount userAccount = new UserAccount("1.2.20000");
        accountAuthorityMap.put(userAccount, 1l);
        differentAuthority = new Authority(1, null, accountAuthorityMap);

        Address address1 = new Address("BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
        Address address2 = new Address("BTS8RiFgs8HkcVPVobHLKEv6yL3iXcC9SWjbPVS15dDAXLG9GYhnY");
        PublicKey publicKey = address1.getPublicKey();
        PublicKey samePublicKey = address2.getPublicKey();
        HashMap<PublicKey, Long> keyMap1 = new HashMap<>();
        HashMap<PublicKey, Long> keyMap2 = new HashMap<>();
        keyMap1.put(publicKey, 1l);
        keyMap2.put(samePublicKey, 1l);
        keyAuthority1 = new Authority(1, keyMap1, null);
        keyAuthority2 = new Authority(1, keyMap2, null);

    }

    @org.junit.Test
    public void toBytes() throws Exception {

    }

    @Test
    public void equals() throws Exception {
        assertEquals("Equal authorities", authority, sameAuthority);
        assertEquals("Different authorities ", authority, differentAuthority);
        assertEquals("Two public keys with the same public key should be equal", keyAuthority1, keyAuthority2);
    }

    @After
    public void tearDown(){
    }
}