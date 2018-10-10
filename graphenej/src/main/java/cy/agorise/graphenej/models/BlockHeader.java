package cy.agorise.graphenej.models;

/**
 * Class used to represent the response to the 'get_block_header' API call.
 */
public class BlockHeader {
    public String previous;
    public String timestamp;
    public String witness;
    public String transaction_merkle_root;
}
