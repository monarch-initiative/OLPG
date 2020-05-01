package org.monarchinitiative.phenol.stats;


import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.DirectAndIndirectTermAnnotations;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.algo.OntologyAlgorithm;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;

import java.util.*;

/**
 * This class hides all the details about how the p values are calculated
 * from the multiple test correction.
 *
 * @author Sebastian Bauer
 * @author Peter Robinson (refactor)
 */
public abstract class ParentChildPValuesCalculation extends PValueCalculation {
  /**
   * For GO analysis, the parent term of cellular component, biological process, and molecular function
   * is owl:Thing. If we do a statistical test for parent-child with respect to this parent, artefactual
   * results are produced, because owl:Thing is added as an "artificial root" by phenol. We can just skip
   * these tests.
   */
  private final TermId OWL_THING = TermId.of("owl:Thing");

  protected final Map<TermId, DirectAndIndirectTermAnnotations> studySetAnnotationMap;

  public ParentChildPValuesCalculation(Ontology graph,
                                       AssociationContainer goAssociations,
                                       StudySet populationSet,
                                       StudySet studySet,
                                       MultipleTestingCorrection mtc) {
    super(graph, goAssociations, populationSet, studySet, mtc);
    this.studySetAnnotationMap = this.studySet.getAnnotationMap();
  }

  @Override
  public List<GoTerm2PValAndCounts> calculatePVals() {
    Map<TermId, DirectAndIndirectTermAnnotations> populationSetAnnotationMap = this.populationSet.getAnnotationMap();
    List<GoTerm2PValAndCounts> results = new ArrayList<>();
    for (Map.Entry<TermId, DirectAndIndirectTermAnnotations> entry : studySetAnnotationMap.entrySet()) {
      if (entry.getValue().totalAnnotatedCount() < 2) {
        continue; // only a single annotated entry -- do not perform a statistical test
      }
      TermId goId = entry.getKey();
      if (goId.equals(OWL_THING)) {
        continue;
      } else if (!this.annotationMap.containsKey(goId)) {
        System.err.println("ERROR -- study set contains ID but pop set does not: " + goId.getValue());
        continue;
      }
      int goidAnnotatedPopGeneCount = populationSetAnnotationMap.get(goId).totalAnnotatedCount();
      int goidAnnotatedStudyGeneCount = studySetAnnotationMap.get(goId).totalAnnotatedCount();
      if (goidAnnotatedStudyGeneCount != 0) {
        /*
         * The parent child calculation is a one-sided Fisher exact test where the population is defined based not
         * on the entire set of genes in an experiment but instead on the set of genes that annotate the parent(s) of
         * the GO term of interest (t). If we denote pa(t) as the parent term(s) of t, then pa(t) will have some
         * direct annotations to pa(t) and will also include annotations from distinct children of pa(t). It is possible
         * that t has more than one parent term. In this case, parent-child union takes the set of all genes that
         * annotate any term in pa(t), and parent-child intersect takes the set of genes that are annotated to all of
         * the terms in pa(t). The latter tends to be more conservative.
         * The study gene count is identical as with term for term, but we need to substitute the counts for the population
         */

        // get parents of current GO term. Do not include original term in this set
        Set<TermId> parents = OntologyAlgorithm.getParentTerms(ontology, goId, false);
        // methodology for Term for Term was like this:
        //        double raw_pval = hyperg.phypergeometric(popGeneCount,
        //          (double) goidAnnotatedPopGeneCount / (double) popGeneCount,
        //          studyGeneCount,
        //          goidAnnotatedStudyGeneCount);
        double raw_pval;
        Counts count = getCounts(goId, parents);
        int m_pa_t = count.m_pa_t;
        int n_pa_t = count.n_pa_t;
        int m_t = count.m_t;
        if (m_t == m_pa_t) {
          // this can (rarely) happen if the parent of term t is not annotated to
          // any additional genes.
          raw_pval = 1.0;
        } else {
          double proportion = count.get_proportion();
          raw_pval = hyperg.phypergeometric(m_pa_t,
            proportion,
            n_pa_t,
            goidAnnotatedStudyGeneCount);
        }

        GoTerm2PValAndCounts goPval = new GoTerm2PValAndCounts(goId, raw_pval, goidAnnotatedStudyGeneCount, goidAnnotatedPopGeneCount);
        results.add(goPval);
      }
    }
    // Now do multiple testing correction
    this.testCorrection.adjustPvals(results);
    return results;
  }


  /**
   * Return value type for getCounts().
   */
  protected static class Counts {
    public final int m_pa_t;
    public final int n_pa_t;
    public final int m_t;

    public Counts(int m, int n, int m_t) {
      this.m_pa_t = m;
      this.n_pa_t = n;
      this.m_t = m_t;
    }

    public double get_proportion() {
      return (double) m_t / (double) m_pa_t;
    }
  }

  /**
   * Calculate the counts for the given study set ids for the term.
   * Implemented by the PC Union/Intersection subclasses.
   *
   * @return the count structure.
   */
  protected abstract Counts getCounts(TermId goId, Set<TermId> parents);


}
