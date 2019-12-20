package org.monarchinitiative.phenol.annotations.formats.mpo;

/**
 * Modifiers used in Mammalian Phenotype Ontology (MP) annotations.
 * @author <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public enum MpModifierType {
  /** An MP phenotype observed specifically in female mice */
  FEMALE_SPECIFIC_ABNORMAL,
  /** An MP phenotype observed specifically in male mice */
  MALE_SPECIFIC_ABNORMAL,
  /** An MP phenotype excluded specifically in female mice */
  FEMALE_SPECIFIC_NORMAL,
  /** An MP phenotype excluded specifically in male mice */
  MALE_SPECIFIC_NORMAL
}
