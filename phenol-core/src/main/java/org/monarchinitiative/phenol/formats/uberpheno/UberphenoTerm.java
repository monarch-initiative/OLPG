package org.monarchinitiative.phenol.formats.uberpheno;

import org.monarchinitiative.phenol.ontology.data.Dbxref;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermSynonym;
import java.util.Date;
import java.util.List;

/**
 * Representation of a term in the Uberpheno ontology.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:sebastian.koehler@charite.de">Sebastian Koehler</a>
 */
public class UberphenoTerm implements Term {

  /** Serial UID for serialization. */
  private static final long serialVersionUID = 1L;

  /** The GO term's Id. */
  private final TermId id;

  /** Alternative term Ids. */
  private final List<TermId> altTermIds;

  /** The human-readable name of the term. */
  private final String name;

  /** The term's definition. */
  private final String definition;

  /** The term's comment string. */
  private final String comment;

  /** The names of the subsets that the term is in, empty if none. */
  private final List<String> subsets;

  /** The list of term synonyms. */
  private final List<TermSynonym> synonyms;

  /** Whether or not the term is obsolete. */
  private final boolean obsolete;

  /** The term's author name. */
  private final String createdBy;

  /** The term's creation date. */
  private final Date creationDate;

  /** List of xrefs. */
  private final List<Dbxref> xrefs;

  /**
   * Constructor.
   *
   * @param id The term's Id.
   * @param altTermIds Alternative term Ids.
   * @param name Human-readable term name.
   * @param definition Term definition.
   * @param comment Term comment.
   * @param subsets The names of the subset that the term is in, empty if none.
   * @param synonyms The synonyms for the term.
   * @param obsolete Whether or not the term is obsolete.
   * @param createdBy Author of the term.
   * @param creationDate Date of creation of the term.
   * @param xrefs List of xrefs.
   */
  public UberphenoTerm(TermId id, List<TermId> altTermIds, String name, String definition,
      String comment, List<String> subsets, List<TermSynonym> synonyms, boolean obsolete,
      String createdBy, Date creationDate, List<Dbxref> xrefs) {
    this.id = id;
    this.altTermIds = altTermIds;
    this.name = name;
    this.definition = definition;
    this.comment = comment;
    this.subsets = subsets;
    this.synonyms = synonyms;
    this.obsolete = obsolete;
    this.createdBy = createdBy;
    this.creationDate = creationDate;
    this.xrefs = xrefs;
  }

  @Override
  public TermId getId() {
    return id;
  }

  @Override
  public List<TermId> getAltTermIds() {
    return altTermIds;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public String getDefinition() {
    return definition;
  }

  @Override
  public String getComment() {
    return comment;
  }

  @Override
  public List<String> getSubsets() {
    return subsets;
  }

  @Override
  public List<TermSynonym> getSynonyms() {
    return synonyms;
  }

  @Override
  public boolean isObsolete() {
    return obsolete;
  }

  @Override
  public String getCreatedBy() {
    return createdBy;
  }

  @Override
  public Date getCreationDate() {
    return creationDate;
  }

  @Override
  public List<Dbxref> getXrefs() {
    return xrefs;
  }

  @Override
  public String toString() {
    return "UberphenoTerm [id=" + id + ", altTermIds=" + altTermIds + ", name=" + name
        + ", definition=" + definition + ", comment=" + comment + ", subsets=" + subsets
        + ", synonyms=" + synonyms + ", obsolete=" + obsolete + ", createdBy=" + createdBy
        + ", creationDate=" + creationDate + ", xrefs=" + xrefs + "]";
  }

}