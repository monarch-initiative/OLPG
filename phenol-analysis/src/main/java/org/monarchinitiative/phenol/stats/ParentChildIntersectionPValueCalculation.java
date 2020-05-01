package org.monarchinitiative.phenol.stats;


import org.monarchinitiative.phenol.analysis.AssociationContainer;
import org.monarchinitiative.phenol.analysis.StudySet;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.stats.mtc.MultipleTestingCorrection;

import java.util.HashSet;
import java.util.Set;

/**
 * Calculate p-values according to Grossmann S, Bauer S, Robinson PN, Vingron M.
 * Improved detection of overrepresentation of Gene-Ontology annotations with parent
 * child analysis. Bioinformatics. 2007;23(22):3024‐3031. PMID: 17848398
 * Parent-Child Intersection method
 * @author Peter Robinson
 */
public class ParentChildIntersectionPValueCalculation extends ParentChildPValuesCalculation {
	public ParentChildIntersectionPValueCalculation(Ontology graph,
                                                  AssociationContainer goAssociations,
                                                  StudySet populationSet,
                                                  StudySet studySet,
                                                  MultipleTestingCorrection mtc) {
		super(graph, goAssociations, populationSet, studySet, mtc);
	}

  @Override
  protected Counts getCounts(TermId goId, Set<TermId> parents) {
    Set<TermId> parentsIntersection = new HashSet<>();
    Set<TermId> genesAnnotatedToGoId = annotationMap.get(goId).getTotalAnnotated();
    int m_t = genesAnnotatedToGoId.size(); // number of genes in population annotated to t
    // the following step performs an intersection with the study set genes
    genesAnnotatedToGoId.retainAll(studySet.getAnnotatedItemTermIds());
    int n_t = genesAnnotatedToGoId.size(); // number of genes in study set annotated to t
    for (TermId par : parents) {
      Set<TermId>  annotedGeneIds = annotationMap.get(par).getTotalAnnotated();
      if (parentsIntersection.isEmpty()) {
        parentsIntersection.addAll(annotedGeneIds);
      } else {
        // the following is set intersection
        parentsIntersection.retainAll(annotedGeneIds);
      }
    }
    int m_pa_t = parentsIntersection.size();
    parentsIntersection.retainAll(studySet.getAnnotatedItemTermIds());
    int n_pa_t = parentsIntersection.size();
    return new Counts(n_pa_t, m_pa_t, n_t, m_t);
  }


}
