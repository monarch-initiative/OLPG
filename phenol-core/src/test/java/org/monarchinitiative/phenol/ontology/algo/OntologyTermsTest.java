package org.monarchinitiative.phenol.ontology.algo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.TreeSet;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.Test;

import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.testdata.vegetables.VegetableOntologyTestBase;


class OntologyTermsTest extends VegetableOntologyTestBase {

  @Test
  void testVisitChildrenOf() {
    Set<TermId> children = new TreeSet<>();

    OntologyTerms.visitChildrenOf(
      idRootVegetable,
      ontology,
      (ontology, termId) -> {
      children.add(termId);
      return true;
    });

    assertEquals(
      ImmutableSet.of(
        TermId.of("VO:0000002"),
        TermId.of("VO:0000004"),
        TermId.of("VO:0000005"),
        TermId.of("VO:0000006"),
        TermId.of("VO:0000007")),
      children);
  }

  @Test
  void testChildrenOf() {
    assertEquals(
      ImmutableSet.of(
        TermId.of("VO:0000004"),
        TermId.of("VO:0000005"),
        TermId.of("VO:0000006"),
        TermId.of("VO:0000007"),
        TermId.of("VO:0000002")),
      OntologyTerms.childrenOf(idRootVegetable, ontology));
  }

  @Test
  void testVisitParentsOf() {
    Set<TermId> parents = new TreeSet<>();

    OntologyTerms.visitParentsOf(
      idBlueCarrot,
      ontology,
      (ontology, termId) -> {
        parents.add(termId);
        return true;
      });

    assertEquals(
      ImmutableSet.of(
        TermId.of("VO:0000001"),
        TermId.of("VO:0000002"),
        TermId.of("VO:0000004"),
        TermId.of("VO:0000007")),
      parents);
  }

  @Test
  void testParentsOf() {
    assertEquals(ImmutableSet.of(
      TermId.of("VO:0000004"),
      TermId.of("VO:0000007"),
      TermId.of("VO:0000001"),
      TermId.of("VO:0000002")),
      OntologyTerms.parentsOf(idBlueCarrot, ontology));
  }
}
