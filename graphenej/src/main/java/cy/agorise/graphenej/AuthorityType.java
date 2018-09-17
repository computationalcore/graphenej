package cy.agorise.graphenej;

/**
 * Enum-type used to specify the different roles of an authority.
 *
 * @see <a href="https://bitshares.org/doxygen/authority_8hpp_source.html">Authority</a>
 */

public enum AuthorityType {
    OWNER,
    ACTIVE,
    MEMO;

    @Override
    public String toString() {
        return String.format("%d", this.ordinal());
    }
}
