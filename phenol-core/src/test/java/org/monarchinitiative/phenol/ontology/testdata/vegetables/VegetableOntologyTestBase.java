package org.monarchinitiative.phenol.ontology.testdata.vegetables;

import java.util.List;

import org.monarchinitiative.phenol.ontology.data.*;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSortedMap;
import com.google.common.collect.Lists;

/**
 * Re-useable base class for ontology-using tests.
 *
 * <p>Provides a simple ontology of vegetables with annotations, occurence in recipes.
 *
 * @author <a href="mailto:manuel.holtgrewe@bihealth.de">Manuel Holtgrewe</a>
 * @author <a href="mailto:HyeongSikKim@lbl.gov">HyeongSik Kim</a>
 */
public class VegetableOntologyTestBase {

  protected static final TermId rootTermId;

  protected static final Ontology ontology;

  protected static final TermId idVegetable;
  protected static final TermId idRootVegetable;
  protected static final TermId idLeafVegetable;
  protected static final TermId idCarrot;
  protected static final TermId idBeet;
  protected static final TermId idPumpkin;
  protected static final TermId idBlueCarrot;

  protected static final List<VegetableRecipeAnnotation> recipeAnnotations;

  static {

    idVegetable = TermId.of("VO:0000001");
    idRootVegetable = TermId.of("VO:0000002");
    idLeafVegetable = TermId.of("VO:0000003");
    idCarrot = TermId.of("VO:0000004");
    idBeet = TermId.of("VO:0000005");
    idPumpkin = TermId.of("VO:0000006");
    idBlueCarrot = TermId.of("VO:0000007");

    rootTermId = idVegetable;

    ImmutableMap.Builder<TermId, Term> termMapBuilder = ImmutableMap.builder();
    termMapBuilder.put(
      idVegetable,
      Term.builder()
        .id(idVegetable)
        .name("vegetable")
        .definition("part of a plant that is consumed")
        .build()
    );
    termMapBuilder.put(
      idRootVegetable,
      Term.builder()
        .id(idRootVegetable)
        .name("root vegetable")
        .definition("consumed root part of plant")
        .build()
    );
    termMapBuilder.put(
      idLeafVegetable,
      Term.builder()
        .id(idLeafVegetable)
        .name("leaf vegetable")
        .definition("consumed leaf part of plant")
        .build()
    );
    termMapBuilder.put(
      idCarrot,
      Term.builder()
        .id(idCarrot)
        .name("carrot")
        .definition("carrots are very tasty root vegetables")
        .build()
    );
    termMapBuilder.put(
      idBlueCarrot,
      Term.builder()
        .id(idBlueCarrot)
        .name("blue carrot")
        .definition("blue ones are even better")
        .build()
    );
    termMapBuilder.put(
      idBeet,
      Term.builder()
        .id(idBeet)
        .name("beet root")
        .definition("beets are tasty and can be used for coloring")
        .build()
    );
    termMapBuilder.put(
      idPumpkin,
      Term.builder()
        .id(idPumpkin)
        .name("pumpkin")
        .definition("pumpkins are great for soup and pickling")
        .build()
    );
    ImmutableMap<TermId, Term> termMap = termMapBuilder.build();

    ImmutableMap.Builder<Integer, Relationship> relationMapBuilder = ImmutableMap.builder();
    relationMapBuilder.put(1, new Relationship(idRootVegetable, idVegetable, 1, RelationshipType.IS_A));
    relationMapBuilder.put(2, new Relationship(idLeafVegetable, idVegetable, 2, RelationshipType.IS_A));
    relationMapBuilder.put(3, new Relationship(idCarrot, idRootVegetable, 3, RelationshipType.IS_A));
    relationMapBuilder.put(4, new Relationship(idBeet, idRootVegetable, 4, RelationshipType.IS_A));
    relationMapBuilder.put(5, new Relationship(idBeet, idLeafVegetable, 5, RelationshipType.IS_A));
    relationMapBuilder.put(6, new Relationship(idPumpkin, idRootVegetable, 6, RelationshipType.IS_A));
    relationMapBuilder.put(7, new Relationship(idBlueCarrot, idCarrot, 7, RelationshipType.IS_A));
    ImmutableMap<Integer, Relationship> relationMap = relationMapBuilder.build();

    ontology = ImmutableOntology.builder()
      .metaInfo(ImmutableSortedMap.of())
      .terms(termMap.values())
      .relationships(relationMap.values())
      .build();

    recipeAnnotations =
        Lists.newArrayList(
            new VegetableRecipeAnnotation(idCarrot, "pumpkin soup"),
            new VegetableRecipeAnnotation(idPumpkin, "pumpkin soup"),
            new VegetableRecipeAnnotation(idPumpkin, "pickled pumpkin"),
            new VegetableRecipeAnnotation(idBeet, "pumpkin soup"),
            new VegetableRecipeAnnotation(idBlueCarrot, "beet surpreme"),
            new VegetableRecipeAnnotation(idBeet, "beet surpreme"));
  }
}
