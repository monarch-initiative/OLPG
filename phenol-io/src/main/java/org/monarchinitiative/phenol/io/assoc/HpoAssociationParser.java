package org.monarchinitiative.phenol.io.assoc;

import com.google.common.collect.*;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.formats.Gene;
import org.monarchinitiative.phenol.formats.hpo.*;
import org.monarchinitiative.phenol.ontology.data.Term;
import org.monarchinitiative.phenol.ontology.data.TermId;
import org.monarchinitiative.phenol.ontology.data.TermPrefix;
import org.monarchinitiative.phenol.io.obo.hpo.HpoDiseaseAnnotationParser;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

/**
 * <p>This class parses the files {@code mim2gene_medgen}, available from
 * <a href="ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/mim2gene_medgen">ftp://ftp.ncbi.nlm.nih.gov/gene/DATA/mim2gene_medgen</a>
 * as well as the file {@code Homo_sapiens_gene_info.gz}, available from
 * <a href="ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz">
 *     ftp://ftp.ncbi.nih.gov/gene/DATA/GENE_INFO/Mammalia/Homo_sapiens.gene_info.gz</a>.
 * mim2gene_medgen contains the MIM number of diseases and EntrezGene number of genes associated with the disease;
 * The relevant lines of the file are marked with "phenotype". The Homo_sapiens_gene_info.gz file contains the  entrez gene
 * number of genes as well as their gene symbol. </p>
 * <p>The goal of this class it to provide associations <br><br>
 *
 *   geneIdToSymbol - Key: EntrezGeneId [{@link TermId}] , Value: EntrezGeneSymbol-String <br>
 *   associationMap - Key: DiseaseId [{@link TermId}] , Value: GeneToAssociation [{@link GeneToAssociation}]<br>
 *   diseaseToGeneMap - Key: DiseaseId [{@link TermId}] , Value: GeneId [{@link TermId}]<br>
 *   geneToDiseaseMap - Key: GeneId [{@link TermId}] , Value: Disease [{@link TermId}]<br>
 *   diseaseToAssociations - Key: DiseaseId [{@link TermId}] , Value: List of genes [{@link DiseaseToGeneAssociation}],<br>
 *   termToDisease - Key: phenotypeId {@link TermId} , Value: Disease [{@link HpoDisease}]  FROM {@link HpoDiseaseAnnotationParser}<br>
 *   phenotypeToGeneList - List of {@link HpoGeneAnnotation} generated from linking termToDisease, diseaseToGene<br>
 *   associationList - List of all {@link DiseaseToGeneAssociation}<br>
 *
 * </p>
 * <a href="mailto:michael.gargano@jax.org">Michael Gargano</a>
 * <a href="mailto:peter.robinson@jax.org">Peter Robinson</a>
 */
public class HpoAssociationParser {

  private final HpoOntology hpoOntology;

  private final String homoSapiensGeneInfoPath;

  private final String mim2gene_medgenPath;
  /** Key--an EntrezGene id; value--the corresponding symbol. */
  private Map<TermId,String> geneIdToSymbolMap;
  /** Key: an OMIM curie (e.g., OMIM:600100); value--corresponding GeneToAssociation object). */
  private ImmutableMultimap<TermId,GeneToAssociation> associationMap;
  /** Key: a disease Id; Value: a geneId */
  private ImmutableMultimap<TermId,TermId> diseaseToGeneMap;
  /** Key: a gene Id; Value: a diseaseId */
  private ImmutableMultimap<TermId,TermId> geneToDiseaseMap;
  /** Key: a disease Id; Value:  disease obj, all gene associations */
  private ImmutableMap<TermId, DiseaseToGeneAssociation> diseaseToAssociationsMap;
  /** Key: an phenotype Id Value: disease obj, HpoDisease, Immutable. */
  private Map<TermId, HpoDisease> termToDisease;
  /** Key: a phenotype Id; Value: geneId */
  private ImmutableList<HpoGeneAnnotation> phenotypeToGeneList;
  /** List of all associations */
  private List<DiseaseToGeneAssociation> associationList;

  private static final TermPrefix ENTREZ_GENE_PREFIX=new TermPrefix("NCBIGene");

  private static final TermPrefix OMIM_PREFIX = new TermPrefix("OMIM");


  public HpoAssociationParser(String geneInfoPath, String mim2gene_medgenPath, HpoOntology ontology){
    this.hpoOntology = ontology;
    this.homoSapiensGeneInfoPath = geneInfoPath;
    this.mim2gene_medgenPath = mim2gene_medgenPath;
  }

  public HpoAssociationParser(File geneInfoPath, File mim2gene_medgenPath, HpoOntology ontology){
    this.hpoOntology = ontology;
    this.homoSapiensGeneInfoPath = geneInfoPath.getAbsolutePath();
    this.mim2gene_medgenPath = mim2gene_medgenPath.getAbsolutePath();
  }


  public Map<TermId,DiseaseToGeneAssociation> getDiseaseToAssociationsMap() { return this.diseaseToAssociationsMap; }

  public Map<TermId,String> getGeneIdToSymbolMap() { return this.geneIdToSymbolMap;}

  public Multimap<TermId,TermId> getDiseaseToGeneIdMap() { return this.diseaseToGeneMap; }

  public Multimap<TermId,TermId> getGeneToDiseaseIdMap() { return this.geneToDiseaseMap; }

  public List<HpoGeneAnnotation> getPhenotypeToGene() { return this.phenotypeToGeneList; }

  public Map<TermId, HpoDisease> getTermToDisease() { return this.termToDisease; }

  /*
      Builds a list of HpoGeneAnnotations, which are just an object that represents a relationship
      from Gene to HP Term.

      @Parameter: Map of PhenotypeID's to DiseaseID's
   */
  public void setTermToGene(Multimap<TermId, TermId> phenotypeToDisease) throws PhenolException{

    if(this.diseaseToGeneMap.isEmpty()){
      throw new PhenolException("Error: Associations not parsed. Please call parse then set the term to gene mapping.");
    }

    ImmutableList.Builder<HpoGeneAnnotation> builderGeneAnnotationList = new ImmutableList.Builder<>();

    for(TermId phenotype : phenotypeToDisease.keySet()){
     Map<Integer, Boolean> mappedGenes = new HashMap<>();
     phenotypeToDisease.get(phenotype).stream()
        .flatMap(disease -> this.diseaseToGeneMap.get(disease).stream()).collect(Collectors.toList()).forEach((gene) -> {
          try {
            Integer entrezId = Integer.parseInt(gene.getId());
            if(!mappedGenes.containsKey(entrezId)){
              String entrezGeneSymbol = this.geneIdToSymbolMap.get(gene);
              if(entrezGeneSymbol == null){
                entrezGeneSymbol = "-";
              }
              String hpoTermName = hpoOntology.getTermMap().get(phenotype).getName();
              this.geneIdToSymbolMap.get(gene);
              HpoGeneAnnotation geneAnnotation = new HpoGeneAnnotation(entrezId, entrezGeneSymbol, hpoTermName, phenotype);
              builderGeneAnnotationList.add(geneAnnotation);
              mappedGenes.put(entrezId, true);
            }
          }catch(Exception e){
            return;
          }
        });
    }
    this.phenotypeToGeneList = builderGeneAnnotationList.build();
  }

  public void setTermToDisease(Map<TermId, HpoDisease> termToDisease){

    this.termToDisease = termToDisease;
  }

  /*
      Generate and set all the association maps
      Disease -> Gene Entire Object
      Multimap DiseaseId to GeneId
      Multimap GeneId -> DiseaseID
   */
  private void setAssociationMaps(){
    ImmutableMultimap.Builder<TermId,TermId> builderGeneToDisease = new ImmutableMultimap.Builder<>();
    ImmutableMap.Builder<TermId,DiseaseToGeneAssociation> builderDiseasetoAssociation = new ImmutableMap.Builder<>();
    for (DiseaseToGeneAssociation g2p : associationList) {
      TermId diseaseId = g2p.getDiseaseId();
      List<Gene> geneList = g2p.getGeneList();
      builderDiseasetoAssociation.put(diseaseId, g2p);
      for (Gene g: geneList) {
        TermId geneId = g.getId();
        builderGeneToDisease.put(geneId, diseaseId);
      }
    }
    this.geneToDiseaseMap = builderGeneToDisease.build();
    this.diseaseToGeneMap = builderGeneToDisease.build().inverse();
    this.diseaseToAssociationsMap = builderDiseasetoAssociation.build();
  }


  public void parse() {

    ImmutableList.Builder<DiseaseToGeneAssociation> builder = new ImmutableList.Builder<>();

    try {
      parseGeneInfo();
      parseMim2GeneMedgen();
      for (TermId omimCurie : associationMap.keySet()) {
        Collection<GeneToAssociation> g2aList = associationMap.get(omimCurie);
        DiseaseToGeneAssociation g2p = new DiseaseToGeneAssociation(omimCurie, ImmutableList.copyOf(g2aList));
        builder.add(g2p);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
    this.associationList = builder.build();
    this.setAssociationMaps();
  }


  /**
   * Creates a multimap from the medgene_medgen file. We need a multimap because some
   * OMIM phenotype id's (the key of the multimap) are associated with more than one
   * gene (EntrezGene id). This method must be called AFTER {@link #parseGeneInfo()}.
   * @throws IOException if the mim2gene_medgen file cannot be read
   */
  private void parseMim2GeneMedgen() throws IOException {
    ImmutableMultimap.Builder<TermId,GeneToAssociation> associationBuilder = new ImmutableMultimap.Builder<>();
    BufferedReader br = new BufferedReader(new FileReader(mim2gene_medgenPath));
    String line;
    while ((line=br.readLine())!=null) {
      if (line.startsWith("#")) continue;
      String a[] = line.split("\t");
      if (a[2].equals("phenotype")) {
        String mimid=a[0];
        TermId omimCurie = new TermId(OMIM_PREFIX, mimid);
        String entrezGeneNumber=a[1];
        TermId entrezId = new TermId(ENTREZ_GENE_PREFIX,entrezGeneNumber);
        String symbol = this.geneIdToSymbolMap.get(entrezId);
        if (symbol==null) {
          symbol="-";
        }
        TermId geneId=new TermId(ENTREZ_GENE_PREFIX,entrezGeneNumber);
        Gene gene = new Gene(geneId,symbol);
        if (a[5].contains("susceptibility")) {
          GeneToAssociation g2a = new GeneToAssociation(gene,AssociationType.POLYGENIC);
          associationBuilder.put(omimCurie,g2a);
        } else {
          GeneToAssociation g2a = new GeneToAssociation(gene,AssociationType.MENDELIAN);
          associationBuilder.put(omimCurie, g2a);
        }
      }
    }
    associationMap = associationBuilder.build();
  }


  private void parseGeneInfo() throws IOException {
    ImmutableMap.Builder<TermId,String> builder=new ImmutableMap.Builder<>();
    InputStream fileStream = new FileInputStream(homoSapiensGeneInfoPath);
    InputStream gzipStream = new GZIPInputStream(fileStream);
    Reader decoder = new InputStreamReader(gzipStream);
    BufferedReader br = new BufferedReader(decoder);
    String line;
    while ((line=br.readLine())!=null) {
      String a[] = line.split("\t");
      String taxon=a[0];
      if (! taxon.equals("9606")) continue; // i.e., we want only Homo sapiens sapiens and not Neaderthal etc.
      String geneId=a[1];
      String symbol=a[2];
      TermId tid = new TermId(ENTREZ_GENE_PREFIX,geneId);
      builder.put(tid,symbol);
    }
    this.geneIdToSymbolMap = builder.build();
  }

}