package org.monarchinitiative.phenol.io.assoc;

import com.google.common.collect.*;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.formats.Gene;
import org.monarchinitiative.phenol.formats.hpo.*;
import org.monarchinitiative.phenol.ontology.data.Ontology;
import org.monarchinitiative.phenol.ontology.data.TermId;
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
 * <p>The goal of this class is to provide associations <br><br>
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

  private final Ontology hpoOntology;
  /** Path to Homo_sapiens.gene_info.gz */
  private final File homoSapiensGeneInfoFile;
  /** Path to mim2gene_medgen. */
  private final File mim2geneMedgenFile;
  /** File representing Orphanet's gene file, en_product6.xml.*/
  private final File orphaToGeneFile;
  /** File representing the phenotype.hpoa file */
  private final File phenotypeDotHpoaFile;
  /** Key--an EntrezGene id; value--the corresponding symbol. all */
  private BiMap<TermId,String> allGeneIdToSymbolMap;
  private ImmutableMap<TermId, String> geneIdToSymbolMap;
  /** Key: an OMIM curie (e.g., OMIM:600100); value--corresponding GeneToAssociation object). For instance,
   * MICROVASCULAR COMPLICATIONS OF DIABETES, SUSCEPTIBILITY TO, 1; is associated to the gene VEGF as POLYGENIC,
   * and MARFAN SYNDROME is associated to the gene FBN1 as MENDELIAN.*/
  private ImmutableMultimap<TermId,GeneToAssociation> associationMap;
  /** Key: a disease Id; Value: a geneId */
  private ImmutableMultimap<TermId,TermId> diseaseToGeneMap;
  /** Key: a gene Id; Value: a diseaseId */
  private ImmutableMultimap<TermId,TermId> geneToDiseaseMap;
  /** Key: a disease Id; Value:  disease obj, all gene associations. */
  private ImmutableMap<TermId, DiseaseToGeneAssociation> diseaseToAssociationsMap;
  /** Key: an phenotype Id Value: disease obj, HpoDisease, Immutable. */
  private Map<TermId, HpoDisease> termToDisease;
  /** Key: a phenotype Id; Value: geneId */
  private ImmutableList<HpoGeneAnnotation> phenotypeToGeneList;
  /** List of all associations */
  private List<DiseaseToGeneAssociation> associationList;

  private static final String ENTREZ_GENE_PREFIX = "NCBIGene";
  private static final String OMIM_PREFIX = "OMIM";


  public HpoAssociationParser(String geneInfoPath,
                              String mim2geneMedgenPath,
                              String orphaToGenePath,
                              String phenotypeHpoaPath,
                              Ontology hpoOntology){
    this.hpoOntology = hpoOntology;
    this.homoSapiensGeneInfoFile = new File(geneInfoPath);
    this.mim2geneMedgenFile = new File(mim2geneMedgenPath);
    this.orphaToGeneFile = new File(orphaToGenePath);
    this.phenotypeDotHpoaFile = new File(phenotypeHpoaPath);
    checkCoreFiles();
    checkOrphaFile();
    parse();
    ingestPhenotypeHpoaFile();
  }

  public HpoAssociationParser(File geneInfoFile,
                              File mim2geneMedgenFile,
                              File orphaToGeneFile,
                              File phenotypeHpoaFile,
                              Ontology hpoOntology){
    this.hpoOntology = hpoOntology;
    this.homoSapiensGeneInfoFile = geneInfoFile;
    this.mim2geneMedgenFile = mim2geneMedgenFile;
    this.orphaToGeneFile = orphaToGeneFile;
    this.phenotypeDotHpoaFile = phenotypeHpoaFile;
    checkCoreFiles();
    checkOrphaFile();
    parse();
    ingestPhenotypeHpoaFile();
  }

  /** Parse everything except the Orphanet data!.*/
  public HpoAssociationParser(String geneInfoPath, String mim2geneMedgenPath, Ontology hpoOntology){
    this.hpoOntology = hpoOntology;
    this.homoSapiensGeneInfoFile = new File(geneInfoPath);
    this.mim2geneMedgenFile = new File(mim2geneMedgenPath);
    this.orphaToGeneFile = null;
    this.phenotypeDotHpoaFile = null;
    parse();
  }

  private void checkCoreFiles() {
    if (! homoSapiensGeneInfoFile.exists()) {
      throw new PhenolRuntimeException("Cannot find Homo_sapiens.gene_info.gz file");
    }
    if (! mim2geneMedgenFile.exists()) {
      throw new PhenolRuntimeException("Cannot find mim2gene_medgen file");
    }
  }

  private void checkOrphaFile() {
    if (! orphaToGeneFile.exists()) {
      throw new PhenolRuntimeException("Cannot find en_product6.xml file");
    }
  }


  /**
   * Ingest the phenotype.hpoa file. This will population {@link #phenotypeToGeneList}.
   */
  private void ingestPhenotypeHpoaFile() {
    if (! phenotypeDotHpoaFile.exists()) {
      throw new PhenolRuntimeException("Cannot find phenotype.hpoa file");
    }
    List<String> desiredDatabasePrefixes=ImmutableList.of("OMIM");
    Map<TermId, HpoDisease> diseaseMap = HpoDiseaseAnnotationParser.loadDiseaseMap(this.phenotypeDotHpoaFile.getAbsolutePath(),
      hpoOntology,
      desiredDatabasePrefixes);
    Multimap<TermId, TermId> phenotypeToDisease = ArrayListMultimap.create();
    for (Map.Entry<TermId,HpoDisease> entry : diseaseMap.entrySet()) {
      for (HpoAnnotation hpoAnnot : entry.getValue().getPhenotypicAbnormalities()) {
        TermId hpoId = hpoAnnot.getTermId();
        phenotypeToDisease.put(hpoId,entry.getKey()); // diseaseId to HPO id multimpa
      }
    }
    setTermToGene(phenotypeToDisease);
  }



  public Map<TermId,DiseaseToGeneAssociation> getDiseaseToAssociationsMap() { return this.diseaseToAssociationsMap; }

  public Map<TermId,String> getGeneIdToSymbolMap() { return this.geneIdToSymbolMap;}

  public Multimap<TermId,TermId> getDiseaseToGeneIdMap() { return this.diseaseToGeneMap; }

  public Multimap<TermId,TermId> getGeneToDiseaseIdMap() { return this.geneToDiseaseMap; }

  public List<HpoGeneAnnotation> getPhenotypeToGene() { return this.phenotypeToGeneList; }

  public Map<TermId, HpoDisease> getTermToDisease() { return this.termToDisease; }

  public Multimap<TermId, GeneToAssociation> getDiseasetoGeneAssociation(){ return this.associationMap; }

  /*
      Builds a list of HpoGeneAnnotations, which are just an object that represents a relationship
      from Gene to HP Term.

      @Parameter: Map of PhenotypeID's to DiseaseID's
   */
  public void setTermToGene(Multimap<TermId, TermId> phenotypeToDisease) {

    if(this.diseaseToGeneMap.isEmpty()){
      throw new PhenolRuntimeException("Error: Associations not parsed. Please call parse then set the term to gene mapping.");
    }

    ImmutableList.Builder<HpoGeneAnnotation> builderGeneAnnotationList = new ImmutableList.Builder<>();

    for(TermId phenotype : phenotypeToDisease.keySet()){
     Set<TermId> mappedGenes = new HashSet<>();
     phenotypeToDisease.get(phenotype).stream()
        .flatMap(disease -> this.diseaseToGeneMap.get(disease).stream()).collect(Collectors.toList()).forEach((gene) -> {
          try {
            Integer entrezId = Integer.parseInt(gene.getId());
            if(!mappedGenes.contains(gene)){
              String entrezGeneSymbol = this.geneIdToSymbolMap.get(gene);
              if(entrezGeneSymbol == null){
                entrezGeneSymbol = "-";
              }
              String hpoTermName = hpoOntology.getTermMap().get(phenotype).getName();
              HpoGeneAnnotation geneAnnotation = new HpoGeneAnnotation(entrezId, entrezGeneSymbol, hpoTermName, phenotype);
              builderGeneAnnotationList.add(geneAnnotation);
              mappedGenes.add(gene);
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
      Generate and set all the bla32 maps
      Disease -> Gene Entire Object
      Multimap DiseaseId to GeneId
      Multimap GeneId -> DiseaseID
   */
  private void setAssociationMaps(){
    Multimap<TermId, TermId> geneToDisease = ArrayListMultimap.create();
    ImmutableMap.Builder<TermId,DiseaseToGeneAssociation> builderDiseasetoAssociation = new ImmutableMap.Builder<>();

    for (DiseaseToGeneAssociation g2p : associationList) {
      TermId diseaseId = g2p.getDiseaseId();
      List<Gene> geneList = g2p.getGeneList();
      builderDiseasetoAssociation.put(diseaseId, g2p);
      for (Gene g: geneList) {
        TermId geneId = g.getId();
        if(!geneToDisease.containsEntry(geneId, diseaseId)){
          geneToDisease.put(geneId, diseaseId);
        }
      }
    }

    ImmutableMultimap.Builder<TermId,TermId> builderGeneToDisease = new ImmutableMultimap.Builder<>();
    builderGeneToDisease.putAll(geneToDisease);
    this.geneToDiseaseMap = builderGeneToDisease.build();
    this.diseaseToGeneMap = builderGeneToDisease.build().inverse();
    this.diseaseToAssociationsMap = builderDiseasetoAssociation.build();
  }


  private void parse() {

    ImmutableList.Builder<DiseaseToGeneAssociation> builder = new ImmutableList.Builder<>();

    try {
      parseGeneInfo();
      parseDiseaseToGene();
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
  private void parseDiseaseToGene() throws IOException {
    Multimap<TermId,String> orphaToGene;
    Multimap<TermId, GeneToAssociation> associationMap = ArrayListMultimap.create();
    Map<TermId, String> geneMap = new HashMap<>();
    try (BufferedReader br = new BufferedReader(new FileReader(mim2geneMedgenFile))) {
      String line;
      while ((line = br.readLine()) != null) {
        if (line.startsWith("#")) continue;
        String[] associations = line.split("\t");
        if (associations[2].equals("phenotype")) {
          String mimid = associations[0];
          TermId omimCurie = TermId.of(OMIM_PREFIX, mimid);
          String entrezGeneNumber = associations[1];
          TermId entrezId = TermId.of(ENTREZ_GENE_PREFIX, entrezGeneNumber);
          String symbol = this.allGeneIdToSymbolMap.get(entrezId);
          if (!"-".equals(entrezGeneNumber)) {
            if (symbol == null) {
              symbol = "-";
            } else {
              if (!geneMap.containsKey(entrezId)) {
                geneMap.put(entrezId, symbol);
              }
            }
            TermId geneId = TermId.of(ENTREZ_GENE_PREFIX, entrezGeneNumber);
            Gene gene = new Gene(geneId, symbol);
            if (associations[5].contains("susceptibility")) {
              GeneToAssociation g2a = new GeneToAssociation(gene, AssociationType.POLYGENIC);
              if (!associationMap.containsEntry(omimCurie, g2a)) {
                associationMap.put(omimCurie, g2a);
              }
            } else {
              GeneToAssociation g2a = new GeneToAssociation(gene, AssociationType.MENDELIAN);
              if (!associationMap.containsEntry(omimCurie, g2a)) {
                associationMap.put(omimCurie, g2a);
              }
            }
          }
        }
      }
    }

    if(this.orphaToGeneFile != null){
      Map<String, TermId> geneSymbolToId = this.allGeneIdToSymbolMap.inverse();
      try{
        OrphaGeneToDiseaseParser parser = new OrphaGeneToDiseaseParser(this.orphaToGeneFile);
        orphaToGene = parser.getOrphaDiseaseToGeneSymbolMap();
        for (Map.Entry<TermId, String> entry : orphaToGene.entries()) {
          TermId orpha = entry.getKey();
          String geneSymbol = entry.getValue();
          if(geneSymbolToId.containsKey(geneSymbol)){
            Gene gene = new Gene(geneSymbolToId.get(geneSymbol), geneSymbol);
            GeneToAssociation g2a = new GeneToAssociation(gene, AssociationType.UNKNOWN);
            if(!associationMap.containsEntry(orpha,g2a)){
              associationMap.put(orpha, g2a);
            }
          }
        }
      }catch(PhenolException e){
        System.err.println(e.toString());
      }
    }

    ImmutableMultimap.Builder<TermId,GeneToAssociation> associationBuilder = new ImmutableMultimap.Builder<>();
    associationBuilder.putAll(associationMap);
    this.associationMap = associationBuilder.build();

    ImmutableMap.Builder<TermId, String> geneBuilder = new ImmutableMap.Builder<>();
    geneBuilder.putAll(geneMap);
    geneIdToSymbolMap = geneBuilder.build();

    this.allGeneIdToSymbolMap = null;
  }

  /**
   * Parse the NCBI Homo_sapiens_gene_info.gz file
   * Add the mappings to a Guava bimap, e.g., NCBIGene:150-ADRA2A
   * @throws IOException if the file cannot be read
   */
  private void parseGeneInfo() throws IOException {
    ImmutableBiMap.Builder<TermId,String> builder=new ImmutableBiMap.Builder<>();
    InputStream fileStream = new FileInputStream(homoSapiensGeneInfoFile);
    InputStream gzipStream = new GZIPInputStream(fileStream);
    Reader decoder = new InputStreamReader(gzipStream);
    BufferedReader br = new BufferedReader(decoder);
    String line;
    // We have seen that occasionally the Homo_sapiens_gene_info.gz
    // contains duplicate lines, which is an error but we do not want the code
    // to crash, so we check for previously found term ids with the seen set.
    // The TermId <-> symbol mapping is one to one.
    Set<TermId> seen = new HashSet<>();
    while ((line=br.readLine())!=null) {
      String[] a = line.split("\t");
      String taxon=a[0];
      if (! taxon.equals("9606")) continue; // i.e., we want only Homo sapiens sapiens and not Neaderthal etc.
      if(!("unknown".equals(a[9]) | "tRNA".equals(a[9]) | "rRNA".equals(a[9]) | "pseudo".equals(a[9]))){
        String geneId=a[1];
        String symbol=a[2];
        TermId tid = TermId.of(ENTREZ_GENE_PREFIX,geneId);
        if (seen.contains(tid)) {
          continue;
        }
        seen.add(tid);
        builder.put(tid,symbol);
      }
    }
    this.allGeneIdToSymbolMap = builder.build();
  }

}
