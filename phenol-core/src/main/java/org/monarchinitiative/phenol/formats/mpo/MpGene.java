package org.monarchinitiative.phenol.formats.mpo;

import org.monarchinitiative.phenol.ontology.data.TermId;

/**
 * A MpGene object corresponds to a genetic marker in MGI. It contains the MGI marker accession id,
 * the marker symbol, and the marker type (gene, transgene, etc.).
 * @author Hannah Blau (blauh)
 * @version 0.0.1
 * @since 12 Dec 2017
 */
public class MpGene {
    private final String geneSymbol;
    private final String markerType; //todo enum
    private final TermId mgiId;

    /**
     * Private constructor for MpGene objects.
     * @param mgiId        MGI marker accession id, e.g., MGI:97874
     * @param geneSymbol   MGI gene symbol, e.g., Rb1
     * @param markerType   MGI marker type, one of those listed below
     */
    private MpGene(TermId mgiId, String geneSymbol, String markerType) {
        this.mgiId = mgiId;
        this.geneSymbol = geneSymbol;
        this.markerType = markerType;
    }

    /**
     * Factory method to construct an MpGene object.
     * @param mgiId        MGI marker accession id, e.g., MGI:97874
     * @param geneSymbol   MGI gene symbol, e.g., Rb1
     * @param markerType   MGI marker type, one of:
     *                     BAC/YAC end; Complex/Cluster/Region; Cytogenetic Marker; DNA Segment;
     *                     Gene; Other Genome Feature; Pseudogene; QTL; Transgene
     * @return             MpGene object
     */
    public static MpGene makeImmutableGene(TermId mgiId, String geneSymbol, String markerType) {
        return new MpGene(mgiId, geneSymbol, markerType);
    }

    /**
     * Getter method for gene symbol.
     * @return   String the gene symbol
     */
    public String getGeneSymbol() {
        return geneSymbol;
    }

    /**
     * Getter method for marker type.
     * @return   String one of the 9 marker types listed above
     */
    public String getMarkerType() {
        return markerType;
    }

    /**
     * Getter method for MGI marker accession id.
     * @return   String the MGI id
     */
    public TermId getMgiGeneId() {
        return mgiId;
    }

    /**
     * Predicate to test whether this MpGene has marker type Transgene.
     * @return boolean true if marker type is Transgene, false otherwise
     */
    public boolean isTransgene() {
         return markerType.equals("Transgene");
    }

    /**
     * toString method of MpGene.
     * @return String     printable representation of this MpGene object
     */
    @Override
    public String toString() {
        return "MpGene{" +
                "mgiId: " + mgiId +
                ", geneSymbol: " + geneSymbol +
                ", markerType: " + markerType +
                '}';
    }
}
