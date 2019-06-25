package org.monarchinitiative.phenol.io.obo.mpo;

import com.google.common.collect.*;
import org.monarchinitiative.phenol.base.PhenolException;
import org.monarchinitiative.phenol.base.PhenolRuntimeException;
import org.monarchinitiative.phenol.formats.mpo.*;
import org.monarchinitiative.phenol.ontology.data.TermId;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Class to parse a number of files from the MGI site.
 * 1.  MGI_Strain.rpt
 * 2.  MGI_Nonstandard_Strain.rpt
 * 3.  MRK_List2.rpt  (Markers, i.e., mainly genes)
 * 4.  MGI_GenePheno.rpt (for now, omit  MGI_PhenoGenoMP.rpt).
 * 5.  MGI_Pheno_Sex.rpt
 * // Plan to have a parallel parser for http://www.mousephenotype.org/ data.
 * // The MpAnnotation class needs to be flexible enough to work with both
 * // c.f. HpAnnotation and HpDiseas
 */
public class MpAnnotationParser {
  /** Path of MGI_GenePheno.rpt file.*/
  private final String genePhenoPath;
  /** Input stream corresponding to MGI_GenePhenot.rpt */
  private final InputStream genePhenoInputstream;
  /** Path of MGI_GenePheno.rpt file.*/
  private final String phenoSexPath;
  /** Input stream corresponding to MGI_GenePhenot.rpt */
  private final InputStream phenoSexInputstream;
  /** Used to store sexSpecific-specific phenotype annotations. */
  private Map<TermId,Map<TermId,MpAnnotation>> geno2ssannotMap = ImmutableMap.of();
  /** Key: A term id representing the genotype accession id of an MPO model
   * Value: the corresponding MpSimpleModel object
   */
  private Map<TermId, MpSimpleModel> genotypeAccessionToMpModelMap;
  /** Show verbose debugging information. */
  private boolean verbose = true;

  private List<String> parseErrors;

  /**
   * @param path Path of MGI_GenePheno.rpt file.
   */
  public MpAnnotationParser(String path) throws PhenolException {
    this.genePhenoPath=path;
    this.phenoSexPath=null;//do not use sexSpecific spcific phenotypes
    this.phenoSexInputstream=null; // not needed
    parseErrors=new ArrayList<>();
    try {
      this.genePhenoInputstream = new FileInputStream(this.genePhenoPath);
      parse();
    } catch (IOException e) {
      throw new PhenolException("Could not parse MGI_GenePheno.rpt: " + e.getMessage());
    }
  }

  /**
   * Input single-gene phenotype data but disregard sexSpecific-specific phenotypes.
   * @param is Input stream that corresponds to MGI_GenePheno.rpt
   * @throws PhenolException if there is an I/O problem
   */
  public MpAnnotationParser(InputStream is ) throws PhenolException {
    this.genePhenoPath="n/a"; // unknown since we are starting with an input stream
    this.phenoSexPath="n/a"; // not needed
    this.genePhenoInputstream =is;
    this.phenoSexInputstream=null; // not needed
    parseErrors=new ArrayList<>();
    try {
      System.err.println("PARSE SIMPLE");
      parse();
    } catch (IOException e) {
      throw new PhenolException("Could not parse MGI_GenePheno.rpt: " + e.getMessage());
    }
  }

  /**
   * Input single-gene phenotype data and include sexSpecific-specific phenotypes using
   * appropriate modifiers
   * @param phenoSexPath Path to MGI_GenePheno.rpt
   * @param genePhenoPath Path to MGI_Pheno_Sex.rpt
   * @throws PhenolException if there is an I/O problem
   */
  public MpAnnotationParser(String genePhenoPath,String phenoSexPath) throws PhenolException {
    this.genePhenoPath=genePhenoPath;
    this.phenoSexPath=phenoSexPath;
    parseErrors=new ArrayList<>();
    try {
      this.genePhenoInputstream = new FileInputStream(this.genePhenoPath);
      this.phenoSexInputstream=new FileInputStream((this.phenoSexPath));
      parsePhenoSexData();
      parse();
    } catch (IOException e) {
      throw new PhenolException("Could not parse MGI_GenePheno.rpt: " + e.getMessage());
    }
  }

  public Map<TermId, MpSimpleModel> getGenotypeAccessionToMpModelMap() {
    return genotypeAccessionToMpModelMap;
  }

  public int getParsedAnnotationCount() { return genotypeAccessionToMpModelMap.size();}


  /**
   * Parse data from MGI_Pheno_Sex.rpt.
   * Note that there may be multiple lines that suuport the same assertion that differ only in PMID
   * @throws IOException if MGI_Pheno_Sex.rpt cannot be successfully parsed
   * @throws PhenolException upon parse issues with MGI_Pheno_Sex.rpt.
   */
  private void parsePhenoSexData() throws IOException, PhenolException {
    int EXPECTED_NUMBER_SEXSPECIFIC_FIELDS=7;
    BufferedReader br = new BufferedReader(new InputStreamReader(this.phenoSexInputstream));
    //this.sexSpecificAnnotationMap = ArrayListMultimap.create();
    this.geno2ssannotMap=new HashMap<>();
    String line;
    line=br.readLine(); // the header
    if (! line.startsWith("Genotype ID")) {
      throw new PhenolException("Malformed header of MGI_Pheno_Sex.rpt: " + line);
    }
    while ((line=br.readLine())!=null) {
      //System.out.println(line);
      String[] A = line.split("\t");
      if (A.length < EXPECTED_NUMBER_SEXSPECIFIC_FIELDS) {
        if (verbose) {
          //throw new PhenolException("Unexpected number of fields (" + A.length + ") in line " + line);
        System.err.println("[Phenol-ERROR] Unexpected number of fields in MGI_Pheno_Sex.rpt(" + A.length + ") in line " + line);
        }
        continue;
      }
      SexSpecificAnnotationLine ssaline = new SexSpecificAnnotationLine(A);
      TermId genotypeId=ssaline.genotypeID;
      MpAnnotation annot = ssaline.toMpAnnotation();
      geno2ssannotMap.putIfAbsent(genotypeId,new HashMap<>());
      Map<TermId,MpAnnotation> annotset = geno2ssannotMap.get(genotypeId);
      if (annotset.containsKey(annot.getTermId())) {
        // there is a previous annotation for this MP term --
        // the current annotation is from a separate PMID
        MpAnnotation previousannot=annotset.get(annot.getTermId());
        MpAnnotation mergedannot=MpAnnotation.merge(previousannot,annot);
        annotset.put(mergedannot.getTermId(),mergedannot);
      } else {
        annotset.put(annot.getTermId(),annot);
      }
    }
  }

  /** Parse the data in MGI_GenePheno.rpt. Interpolate the sexSpecific-specific data if available. */
  private void parse() throws IOException {
    BufferedReader br = new BufferedReader(new InputStreamReader(this.genePhenoInputstream));
    Multimap<TermId, AnnotationLine> annotationCollector = ArrayListMultimap.create();
    String line;
    while ((line = br.readLine()) != null) {
      //System.out.println(line);
      String[] A = line.split("\t");
      /* Expected number of fields of the MGI_GenePheno.rpt file (note -- there
         appears to be a stray tab  between the penultimate and last column) */
      int EXPECTED_NUMBER_OF_FIELDS = 8;
      if (A.length < EXPECTED_NUMBER_OF_FIELDS) {
        if (verbose) {
          //throw new PhenolException("Unexpected number of fields (" + A.length + ") in line " + line);
          System.err.println("[Phenol-ERROR] Unexpected number of fields in MGI_GenePheno.rpt (" + A.length + ") in line " + line);
        }
        continue;
      }
      try {
        AnnotationLine annot = new AnnotationLine(A);
        TermId modelId = annot.getGenotypeAccessionId();
        annotationCollector.put(modelId, annot);
      } catch (PhenolException e) {
        String err = String.format("[PARSE ERROR] %s (%s)", e.getMessage(), line);
        this.parseErrors.add(err);
      }
    }
    // When we get here, we have parsed all of the MGI_GenePheno.rpt file.
    // Annotation lines are groups according to genotype accession id in the multimap
    // our goal in the following is to parse everything into corresponding MpSimpleModel objects
    ImmutableMap.Builder<TermId, MpSimpleModel> builder = new ImmutableMap.Builder<>();
    for (TermId genoId : annotationCollector.keySet()) {
      Collection<AnnotationLine> annotationLines = annotationCollector.get(genoId);
      ImmutableList.Builder<MpAnnotation> annotbuilder = new ImmutableList.Builder<>();
      Iterator<AnnotationLine> it = annotationLines.iterator();
      MpStrain background = null;
      MpAllelicComposition allelicComp = null;
      TermId alleleId = null;
      String alleleSymbol = null;
      TermId markerId = null;
      // get the sexSpecific-specific annotations for this genotypeId, if any
      Map<TermId, MpAnnotation> sexSpecific = ImmutableMap.of(); // default, empty set
      if (this.geno2ssannotMap.containsKey(genoId)) {
        ImmutableMap.Builder<TermId, MpAnnotation> imapbuilder = new ImmutableMap.Builder<>();
        Map<TermId, MpAnnotation> annots = this.geno2ssannotMap.get(genoId);
        for (MpAnnotation mpann : annots.values()) {
          imapbuilder.put(mpann.getTermId(), mpann);
        }
        try {
          sexSpecific = imapbuilder.build();
        } catch (Exception e) {
          System.err.println("Error building map of sexSpecific-specific annotations for " + genoId.getValue() + ": " + e.getMessage());
        }
      }
      while (it.hasNext()) {
        AnnotationLine aline = it.next();
        MpAnnotation annot = aline.toMpAnnotation();
        TermId mpoId = aline.getMpId();
        background = aline.geneticBackground;
        allelicComp = aline.getAllelicComp();
        alleleId = aline.getAlleleId();
        alleleSymbol = aline.getAlleleSymbol();
        markerId = aline.getMarkerAccessionId();
        // TODO we could check that these are identical for any given genotype id
        // check if we have a sexSpecific-specific annotation matching the current annotation
        // the following adds mpoId if it is present in sexSpexifix, otherwise it adds the default annot
        annotbuilder.add(sexSpecific.getOrDefault(mpoId,annot));

        // Note that we do not check for sexSpecific-specific annotations that are not present in the "main" file
        // in practice, these are only sexSpecific-specific normal -- i.e., a phenotype was ruled out in one sexSpecific
        // even though "somebody" thought the phenotype might be present.
        // this type of normality (absence of a phenotype) is not useful for downstream analysis at the
        // present time and so we skip it to avoid unnecessarily complicating the implementation.
      }
      MpSimpleModel mod = new MpSimpleModel(genoId, background, allelicComp, alleleId, alleleSymbol, markerId, annotbuilder.build());
      builder.put(genoId, mod);
    }
    genotypeAccessionToMpModelMap = builder.build();
  }

  public boolean hasParseError(){ return this.parseErrors.size()>0;}
  public List<String> getParseErrors() { return this.parseErrors; }


  /**
   * A convenience class that allows us to collect all of the annotations that belong
   * to a given model (genotype accession id).
   * The MGI_GenePheno.rpt file has the following structure:
   *  <ul>
   *    <li>0. Allelic Composition	</li>
   *    <li>1. Allele Symbol(s)</li>
   *    <li>2. Allele ID(s)</li>
   *    <li>3. Genetic Background</li>
   *    <li>4. Mammalian Phenotype ID</li>
   *    <li>5. PubMed ID (pipe-delimited)</li>
   *    <li>6. MGI Marker Accession ID (pipe-delimited)</li>
   *    <li>7. MGI Genotype Accession ID (pipe-delimited)</li>
   * </ul>
   */
   private static class AnnotationLine {
    /** [0] Index of Allelic Composition	Allele Symbol(s) field */
    private final int ALLELIC_COMPOSITION_IDX=0;
    /** [1] Index of Allele Symbol(s) field. */
    private final int ALLELE_SYMBOL_IDX =1;
    /** [1] Index of Allele ID(s) field. */
    private final int ALLELE_ID_IDX=2;
    /** [2] Index of Genetic Background field */
    private final int GENETIC_BACKGROUND_IDX=3;
    /** [3] Index of Mammalian Phenotype ID */
    private final int MPO_IDX=4;
    /** Index of PubMed ID (pipe-delimited) */
    private final int PUBMED_IDX=5;
    /** Index of MGI Marker Accession ID (pipe-delimited). For example, for a model with
     * a mutation of the RB1 gene, this would be the id for that gene (MGI:97874). */
    private final int MGI_MARKER_IDX=6;
    /** Index of MGI Genotype Accession ID (pipe-delimited). */
    private final int GENOTYPE_ACCESSION_IDX=7;

    private final MpAllelicComposition allelicComp;
    private final String alleleSymbol;
    private final TermId alleleId;
    private final MpStrain geneticBackground;
    private final TermId mpId;
    private final Set<String> pmidSet;
    private final TermId markerAccessionId;
    private final TermId genotypeAccessionId;

    AnnotationLine(String[] annotations) throws PhenolException {
      this.allelicComp = MpAllelicComposition.fromString(annotations[ALLELIC_COMPOSITION_IDX]);
      this.alleleSymbol = annotations[ALLELE_SYMBOL_IDX];
      this.alleleId = parseOrThrowException(annotations[ALLELE_ID_IDX]);
      this.geneticBackground = MpStrain.fromString(annotations[GENETIC_BACKGROUND_IDX]);
      this.mpId = parseOrThrowException(annotations[MPO_IDX]);
      String pmids = annotations[PUBMED_IDX];
      ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>();
      String[] pubMedIds = pmids.split(Pattern.quote("|"));
      for (String b : pubMedIds) {
        builder.add(b);
      }
      this.pmidSet = builder.build();
      this.markerAccessionId = parseOrThrowException(annotations[MGI_MARKER_IDX]);
      this.genotypeAccessionId = parseOrThrowException(annotations[GENOTYPE_ACCESSION_IDX]);
    }

    private TermId parseOrThrowException(String termId) throws PhenolException {
      try {
        return TermId.of(termId);
      } catch (PhenolRuntimeException e) {
        throw new PhenolException(e.getMessage());
      }
    }

    public MpAllelicComposition getAllelicComp() {
      return allelicComp;
    }

    public String getAlleleSymbol() {
      return alleleSymbol;
    }

    public TermId getAlleleId() {
      return alleleId;
    }

    public MpStrain getGeneticBackground() {
      return geneticBackground;
    }

    public TermId getMpId() {
      return mpId;
    }

    public Set<String> getPmidSet() {
      return pmidSet;
    }

    public TermId getMarkerAccessionId() {
      return markerAccessionId;
    }

    public TermId getGenotypeAccessionId() {
      return genotypeAccessionId;
    }


    /**
     * @return THe {@link MpAnnotation} object corresponding to this {@link AnnotationLine}.
     */
    public MpAnnotation toMpAnnotation() {
      return new MpAnnotation.Builder(this.mpId,this.pmidSet).build();
    }

  }

  /**
   * A convenience class that allows us to collect all of the annotations that belong
   * to a given model (genotype accession id).
   */
  private static class SexSpecificAnnotationLine {
    private final TermId genotypeID;
    private final MpSex sex;
    private final TermId mpId;
    private final MpAllelicComposition allelicComposition;
    private final MpStrain strain;
    private final boolean sexSpecificNormal;
    private final Set<String> pmidList;

    SexSpecificAnnotationLine(String[] A) throws PhenolException {
      this.genotypeID=TermId.of(A[0]);
      this.sex = MpSex.fromString(A[1]);
      this.mpId = TermId.of(A[2]);
      this.allelicComposition =  MpAllelicComposition.fromString(A[3]);
      this.strain = MpStrain.fromString(A[4]);
      sexSpecificNormal = A[5].equals("Y");
      String pmids=A[6];
      ImmutableSet.Builder<String> builder = new ImmutableSet.Builder<>();
      String[] B = pmids.split(Pattern.quote("|"));
      for (String b: B) {
        builder.add(b);
      }
      this.pmidList=builder.build();

    }

    public MpAnnotation toMpAnnotation() {
      if (sexSpecificNormal) {
        MpAnnotation.Builder builder = new MpAnnotation.Builder(this.mpId,this.pmidList)
          .sexSpecificNormal(this.sex);
        return builder.build();
      } else {
        MpAnnotation.Builder builder = new MpAnnotation.Builder(this.mpId, this.pmidList)
          .sexSpecific(this.sex);
        return builder.build();
      }
    }


  }



}
