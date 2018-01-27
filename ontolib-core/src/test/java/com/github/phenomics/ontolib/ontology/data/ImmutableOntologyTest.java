package com.github.phenomics.ontolib.ontology.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Map;

public class ImmutableOntologyTest extends ImmutableOntologyTestBase {

  @Test
  public void test() {
    assertEquals("{}", ontology.getMetaInfo().toString());
    assertEquals(
        "ImmutableDirectedGraph [edgeLists={ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001]=ImmutableVertexEdgeList [inEdges=[], outEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], id=1], ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], id=2], ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], id=3]]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002]=ImmutableVertexEdgeList [inEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], id=1]], outEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=4]]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003]=ImmutableVertexEdgeList [inEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], id=2]], outEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=5]]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004]=ImmutableVertexEdgeList [inEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], id=3]], outEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=6]]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005]=ImmutableVertexEdgeList [inEdges=[ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=4], ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=5], ImmutableEdge [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=6]], outEdges=[]]}, edgeCount=6]",
        ontology.getGraph().toString());
    assertEquals(
        "{ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001]=TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], altTermIds=[], name=term1, definition=some definition 1, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002]=TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], altTermIds=[], name=term2, definition=some definition 2, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003]=TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], altTermIds=[], name=term3, definition=some definition 3, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004]=TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], altTermIds=[], name=term4, definition=some definition 4, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005]=TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], altTermIds=[], name=term5, definition=some definition 5, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]]}",
        ontology.getTermMap().toString());
    assertEquals(
        "{1=TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], id=1], 2=TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], id=2], 3=TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], id=3], 4=TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=4], 5=TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=5], 6=TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=6]}",
        ontology.getRelationMap().toString());
    assertTrue(ontology.isRootTerm(id5));
    assertEquals(
        "[ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005]]",
        ontology.getAllTermIds().toString());
    assertEquals(
        "[TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], altTermIds=[], name=term1, definition=some definition 1, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], altTermIds=[], name=term2, definition=some definition 2, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], altTermIds=[], name=term3, definition=some definition 3, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], altTermIds=[], name=term4, definition=some definition 4, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]], TestTerm [termId=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], altTermIds=[], name=term5, definition=some definition 5, comment=null, subsets=[], termSynonyms=[], obsolete=false, createdBy=null, creationDate=null, xrefs=[]]]",
        ontology.getTerms().toString());
    assertEquals(5, ontology.countAllTerms());
    assertEquals(
        "[ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005]]",
        ontology.getNonObsoleteTermIds().toString());
    assertEquals("[]", ontology.getObsoleteTermIds().toString());
    assertEquals(
        "[ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004]]",
        ontology.getParentTermIds(id1).toString());
  }


  /**
   * The subontology defined by Term with id4 should consist of only the terms id4 and id1.
   * The termmap should thus contain only two terms. The subontology does not contain the original root of the ontology, id5.
   */
  @Test
  public void testSubontologyCreation() {
    ImmutableOntology<TestTerm, TestTermRelation> subontology=(ImmutableOntology<TestTerm, TestTermRelation>)ontology.subOntology(id4);
    assertTrue(subontology.getTermMap().containsKey(id4));
    assertTrue(subontology.getTermMap().containsKey(id1));
    assertTrue(ontology.getTermMap().size()==5);
    assertTrue(subontology.getTermMap().size()==2);
    assertFalse(subontology.getTermMap().containsKey(id5));
  }

  /**
   * The parent ontology has six relations
   * 1  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], id=1]
   2  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], id=2]
   3  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], id=3]
   4  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000002], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=4]
   5  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000003], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=5]
   6  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000005], id=6]
   The subontology has just the terms id1 and id4, and thus should just have only one relation./subontology
   3  TestTermRelation [source=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000001], dest=ImmutableTermId [prefix=ImmutableTermPrefix [value=HP], id=0000004], id=3]
   */
  @Test
  public void testSubontologyRelations() {
    ImmutableOntology<TestTerm, TestTermRelation> subontology=(ImmutableOntology<TestTerm, TestTermRelation>)ontology.subOntology(id4);
    Map<Integer, TestTermRelation> relationMap = ontology.getRelationMap();
    int expectedSize=6;
    assertEquals(expectedSize,relationMap.size());
    relationMap = subontology.getRelationMap();
    expectedSize=1;
    assertEquals(expectedSize,relationMap.size());
  }





}
