// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Authors: Claudius Korzen <korzen@cs.uni-freiburg.de>

import org.junit.Test;
import org.junit.Assert;
import java.io.IOException;

/**
 * One unit test for each non-trivial method in the QGramIndex class.
 */
public class QGramIndexTest {
  @Test
  public void testConstructor() {
    QGramIndex qgi = new QGramIndex(3);
    Assert.assertEquals(3, qgi.q);
    Assert.assertEquals("$$", qgi.padding);
    Assert.assertEquals("{}", qgi.invertedLists.toString());
  }

  @Test
  public void testBuildFromFile() throws IOException {
    QGramIndex qgi = new QGramIndex(3);
    qgi.buildFromFile("example.tsv");
    Assert.assertEquals("{$$b=[2], $$f=[1], $br=[2], $fr=[1], bre=[2],"
                        + " ei$=[1, 2], fre=[1], i$$=[1, 2], rei=[1, 2]}",
                        qgi.invertedLists.toString());
  }

  @Test
  public void testPrefixEditDistance() {
    Assert.assertEquals(0, QGramIndex.prefixEditDistance("frei", "frei", 0));
    Assert.assertEquals(0, QGramIndex.prefixEditDistance(
        "frei", "freiburg", 0));
    Assert.assertEquals(1, QGramIndex.prefixEditDistance(
        "frei", "breifurg", 1));
    Assert.assertEquals(3, QGramIndex.prefixEditDistance(
        "freiburg", "stuttgart", 2));
  }

  @Test
  public void testNormalize() {
    Assert.assertEquals("freiburg", QGramIndex.normalize("freiburg"));
    Assert.assertEquals("freiburg", QGramIndex.normalize("Frei, burg !!"));
  }

  @Test
  public void testComputeQGrams() {
    QGramIndex qgi = new QGramIndex(3);
    Assert.assertEquals("[$$f, $fr, fre, rei, eib, ibu, bur, urg, rg$, g$$]",
                        qgi.computeQGrams("freiburg").toString());
  }
}
