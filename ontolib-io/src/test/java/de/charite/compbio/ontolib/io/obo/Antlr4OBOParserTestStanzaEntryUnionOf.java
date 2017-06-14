package de.charite.compbio.ontolib.io.obo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import de.charite.compbio.ontolib.io.obo.parser.Antlr4OBOParser;
import de.charite.compbio.ontolib.io.obo.parser.Antlr4OBOParser.KeyValueUnionOfContext;
import de.charite.compbio.ontolib.io.obo.parser.Antlr4OBOParser.TermStanzaKeyValueContext;

public class Antlr4OBOParserTestStanzaEntryUnionOf extends Antlr4OBOParserTestBase {

  @Test
  public void testParseNoModifierNoCommentAsTermKeyValue() {
    final String text = "union_of: HP:1\n";
    final Antlr4OBOParser parser = buildParser(text);
    final TermStanzaKeyValueContext ctx = parser.termStanzaKeyValue();
    final StanzaEntry stanzaEntry = (StanzaEntry) getListener().getValue(ctx);

    assertEquals(StanzaEntryType.UNION_OF, stanzaEntry.getType());
    assertNull(stanzaEntry.getTrailingModifier());
    assertNull(stanzaEntry.getComment());
  }

  @Test
  public void testParseNoModifierNoCommentAsKeyValueUnionOf() {
    final String text = "union_of: HP:1\n";
    final Antlr4OBOParser parser = buildParser(text);
    final KeyValueUnionOfContext ctx = parser.keyValueUnionOf();
    final StanzaEntryUnionOf stanzaEntry = (StanzaEntryUnionOf) getListener().getValue(ctx);

    assertEquals(StanzaEntryType.UNION_OF, stanzaEntry.getType());
    assertEquals("HP:1", stanzaEntry.getId());
    assertNull(stanzaEntry.getTrailingModifier());
    assertNull(stanzaEntry.getComment());
  }

  @Test
  public void testParseModifierNoCommentAsKeyValueUnionOf() {
    final String text = "union_of: HP:1 {key=value}\n";
    final Antlr4OBOParser parser = buildParser(text);
    final KeyValueUnionOfContext ctx = parser.keyValueUnionOf();
    final StanzaEntryUnionOf stanzaEntry = (StanzaEntryUnionOf) getListener().getValue(ctx);

    assertEquals(StanzaEntryType.UNION_OF, stanzaEntry.getType());
    assertEquals("HP:1", stanzaEntry.getId());
    assertEquals("TrailingModifier [keyValue=[KeyValue [key=key, value=value]]]",
        stanzaEntry.getTrailingModifier().toString());
    assertNull(stanzaEntry.getComment());
  }

  @Test
  public void testParseNoModifierCommentAsKeyValueUnionOf() {
    final String text = "union_of: HP:1 ! comment\n";
    final Antlr4OBOParser parser = buildParser(text);
    final KeyValueUnionOfContext ctx = parser.keyValueUnionOf();
    final StanzaEntryUnionOf stanzaEntry = (StanzaEntryUnionOf) getListener().getValue(ctx);

    assertEquals(StanzaEntryType.UNION_OF, stanzaEntry.getType());
    assertEquals("HP:1", stanzaEntry.getId());
    assertNull(stanzaEntry.getTrailingModifier());
    assertEquals("comment", stanzaEntry.getComment().toString());
  }

  @Test
  public void testParseModifierCommentAsKeyValueUnionOf() {
    final String text = "union_of: HP:1 {key=value} ! comment\n";
    final Antlr4OBOParser parser = buildParser(text);
    final KeyValueUnionOfContext ctx = parser.keyValueUnionOf();
    final StanzaEntryUnionOf stanzaEntry = (StanzaEntryUnionOf) getListener().getValue(ctx);

    assertEquals(StanzaEntryType.UNION_OF, stanzaEntry.getType());
    assertEquals("HP:1", stanzaEntry.getId());
    assertEquals("TrailingModifier [keyValue=[KeyValue [key=key, value=value]]]",
        stanzaEntry.getTrailingModifier().toString());
    assertEquals("comment", stanzaEntry.getComment());
  }

}
