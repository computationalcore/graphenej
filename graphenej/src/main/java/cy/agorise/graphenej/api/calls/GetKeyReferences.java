package cy.agorise.graphenej.api.calls;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cy.agorise.graphenej.Address;
import cy.agorise.graphenej.RPC;
import cy.agorise.graphenej.api.ApiAccess;
import cy.agorise.graphenej.errors.MalformedAddressException;
import cy.agorise.graphenej.models.ApiCall;

/**
 * Wrapper around the 'get_key_references' API call.
 */
public class GetKeyReferences implements ApiCallable {
    public static final int REQUIRED_API = ApiAccess.API_NONE;

    private List<Address> addresses = new ArrayList<>();

    public GetKeyReferences(String addr) throws MalformedAddressException, IllegalArgumentException {
        this(new Address(addr));
    }

    public GetKeyReferences(Address address){
        addresses.add(address);
    }

    public GetKeyReferences(List<Address> addressList){
        addresses.addAll(addressList);
    }

    @Override
    public ApiCall toApiCall(int apiId, long sequenceId) {
        ArrayList<Serializable> inner = new ArrayList<Serializable>();
        for(Address addr : addresses){
            inner.add(addr.toString());
        }
        ArrayList<Serializable> params = new ArrayList<>();
        params.add(inner);
        return new ApiCall(apiId, RPC.CALL_GET_KEY_REFERENCES, params, RPC.VERSION, sequenceId);
    }
}
