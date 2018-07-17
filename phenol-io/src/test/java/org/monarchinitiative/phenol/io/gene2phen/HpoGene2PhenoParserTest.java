package org.monarchinitiative.phenol.io.gene2phen;


import com.google.common.collect.Multimap;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.monarchinitiative.phenol.formats.Gene;
import org.monarchinitiative.phenol.formats.hpo.Disease2GeneAssociation;
import org.monarchinitiative.phenol.io.utils.ResourceUtils;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertTrue;

public class HpoGene2PhenoParserTest {
  private static HpoDisease2GeneParser parser;

  @ClassRule
  public static TemporaryFolder tmpFolder = new TemporaryFolder();
  @BeforeClass
  public static void init() throws IOException {
    System.setProperty("user.timezone", "UTC"); // Somehow setting in pom.xml does not work :(
    File mim2gene = tmpFolder.newFile("mim2gene_medgen");
    ResourceUtils.copyResourceToFile("/mim2gene_medgen.excerpt", mim2gene);
    File geneInfo=tmpFolder.newFile("Homo_sapiens.gene_info.gz");
    ResourceUtils.copyResourceToFile("/Homo_sapiens.gene_info.excerpt.gz",geneInfo);
    parser = new HpoDisease2GeneParser(geneInfo.getAbsolutePath(),mim2gene.getAbsolutePath());
  }

  @Test
  public void testNotNull() {
    assertNotNull(parser);
  }

  /*
  @Test
  public void testIt(){
    List<Disease2GeneAssociation> g2p_list = parser.parse();
    for (Disease2GeneAssociation g2p : g2p_list) {
      System.out.println(g2p);
    }
  }*/

  /** TBX5 is the only gene involved with Holt-Oram syndrome (OMIM:142900), and
   * TBX5 is not associated with other diseases. TBX5 has the EntrezGene id 6910
    */
  @Test
  public void testHoltOram() {
    Map<TermId,Disease2GeneAssociation> diseasemap= parser.getDiseaseId2AssociationMap();
    TermId holtOramId = TermId.constructWithPrefix("OMIM:142900");
    assertTrue(diseasemap.containsKey(holtOramId));
    Disease2GeneAssociation holtOramAssociation = diseasemap.get(holtOramId);
    List<Gene> geneList = holtOramAssociation.getGeneList();
    assertEquals(1,geneList.size());
    Gene gene = geneList.get(0);
    TermId tbx5Id = TermId.constructWithPrefix("NCBIGene:6910");
    assertEquals(tbx5Id, gene.getId());
    String symbol = "TBX5";
    assertEquals(symbol,gene.getSymbol());
  }

  /**
   *  ARHGAP31 is the only gene assicuated with Adams-Oliver syndrome type 1 (OMIM:100300)
   *  ARHGAP31 is not associated with other diseases and it has the EntrezGene id 57514
   */
  @Test
  public void testAdamsOliver() {
    Map<TermId,Disease2GeneAssociation> diseasemap= parser.getDiseaseId2AssociationMap();
    TermId adamsOliver1Id = TermId.constructWithPrefix("OMIM:100300");
    assertTrue(diseasemap.containsKey(adamsOliver1Id));
    Disease2GeneAssociation holtOramAssociation = diseasemap.get(adamsOliver1Id);
    List<Gene> geneList = holtOramAssociation.getGeneList();
    assertEquals(1,geneList.size());
    Gene gene = geneList.get(0);
    TermId tbx5Id = TermId.constructWithPrefix("NCBIGene:57514");
    assertEquals(tbx5Id, gene.getId());
    String symbol = "ARHGAP31";
    assertEquals(symbol,gene.getSymbol());
  }

  /**
   * There are several disease associated with FBN1.
   * 1. Acromicric dysplasia 	OMIM:102370
   * 2. Ectopia lentis, familial 	OMIM:129600
   * 3. Geleophysic dysplasia 2 	OMIM:614185
   * 4. Marfan lipodystrophy syndrome 	OMIM:616914
   * 5. Marfan syndrome 	OMIM:154700
   * 6. MASS syndrome 	OMIM:604308 		3
   * 7. Stiff skin syndrome 	OMIM:184900
   * 8. Weill-Marchesani syndrome 2, dominant 	OMIM:608328
   */
  @Test
  public void testFbn1() {
    Multimap<TermId,TermId> mmap = parser.getGeneId2DiseaseIdMap();
    TermId Fbn1Id = TermId.constructWithPrefix("NCBIGene:2200");
    assertTrue(mmap.containsKey(Fbn1Id));
    Collection<TermId> diseaseIdCollection = mmap.get(Fbn1Id);
    assertEquals(8,diseaseIdCollection.size());
    TermId acromicricDysplasia = TermId.constructWithPrefix("OMIM:102370");
    assertTrue(diseaseIdCollection.contains(acromicricDysplasia));
    TermId ectopiaLentis = TermId.constructWithPrefix("OMIM:129600");
    assertTrue(diseaseIdCollection.contains(ectopiaLentis));
    TermId geleophysicDysplasia2 = TermId.constructWithPrefix("OMIM:614185");
    assertTrue(diseaseIdCollection.contains(geleophysicDysplasia2));
    TermId marfanLipodystrophySyndrome = TermId.constructWithPrefix("OMIM:616914");
    assertTrue(diseaseIdCollection.contains(marfanLipodystrophySyndrome));
    TermId marfanSyndrome = TermId.constructWithPrefix("OMIM:154700");
    assertTrue(diseaseIdCollection.contains(marfanSyndrome));
    TermId massSyndrome = TermId.constructWithPrefix("OMIM:604308");
    assertTrue(diseaseIdCollection.contains(massSyndrome));
    TermId stiffSkinSyndrome = TermId.constructWithPrefix("OMIM:184900");
    assertTrue(diseaseIdCollection.contains(stiffSkinSyndrome));
    TermId weillMarchesani2 = TermId.constructWithPrefix("OMIM:608328");
    assertTrue(diseaseIdCollection.contains(weillMarchesani2));
  }





  /**
   * OMIM:143890 is HYPERCHOLESTEROLEMIA, FAMILIAL
   * It is associated with a number of genes:
   * APOA2 (336)
   * ITIH4 (3700)
   * GHR (2690)
   * 	PPP1R17 (10842; aka	GSBS) 	604088
   * EPHX2 (2053) 	132811
   * ABCA1 (19)
   * LDLR (3949)
   *
   *
   *
   * 143890	10842	phenotype	 GeneMap	C0020445	susceptibility
   * 143890	19	phenotype	 GeneMap	C0020445	susceptibility
   * 143890	2053	phenotype	 GeneMap	C0020445	susceptibility; modifier
   * 143890	2690	phenotype	 GeneMap	C0020445	susceptibility; modifier
   * 143890	336	phenotype	 GeneMap	C0020445	susceptibility; modifier
   * 143890	3700	phenotype	 GeneMap	C0020445	susceptibility
   */
  @Test
  public void testSusceptibilityGenes() {
    Map<TermId,Disease2GeneAssociation> diseasemap= parser.getDiseaseId2AssociationMap();
    TermId familialHypercholesterolemia = TermId.constructWithPrefix("OMIM:143890");
    assertTrue(diseasemap.containsKey(familialHypercholesterolemia));
    Disease2GeneAssociation hypercholesterolemiaAssociation = diseasemap.get(familialHypercholesterolemia);
    List<Gene> geneList = hypercholesterolemiaAssociation.getGeneList();
    assertEquals(7,geneList.size());
    Gene APOA2 = new Gene(TermId.constructWithPrefix("NCBIGene:336"),"APOA2");
    assertTrue(geneList.contains(APOA2));
    Gene ITIH4 = new Gene(TermId.constructWithPrefix("NCBIGene:3700"),"ITIH4");
    assertTrue(geneList.contains(ITIH4));
    Gene GHR = new Gene(TermId.constructWithPrefix("NCBIGene:2690"),"GHR");
    assertTrue(geneList.contains(GHR));
    Gene PPP1R17 = new Gene(TermId.constructWithPrefix("NCBIGene:10842"),"PPP1R17");
    assertTrue(geneList.contains(PPP1R17));
    Gene EPHX2 = new Gene(TermId.constructWithPrefix("NCBIGene:2053"),"EPHX2");
    assertTrue(geneList.contains(EPHX2));
    Gene ABCA1 = new Gene(TermId.constructWithPrefix("NCBIGene:19"),"ABCA1");
    assertTrue(geneList.contains(ABCA1));
    Gene LDLR = new Gene(TermId.constructWithPrefix("NCBIGene:3949"),"LDLR");
    assertTrue(geneList.contains(LDLR));
  }




}
