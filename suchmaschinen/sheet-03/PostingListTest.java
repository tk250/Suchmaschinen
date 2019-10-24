// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Author: Claudius Korzen <korzen@cs.uni-freiburg.de>.

import org.junit.Assert;
import org.junit.Test;

/**
 * One unit test for each non-trivial method in the PostingList class.
 */
public class PostingListTest {
  /**
   * Tests for the method readFromFile().
   */
  @Test
  public void testReadFromFile() {
    PostingList l1 = new PostingList();
    PostingList l2 = new PostingList();
    PostingList l3 = new PostingList();
    l1.readFromFile("example1.txt");
    l2.readFromFile("example2.txt");
    l3.readFromFile("example3.txt");
    Assert.assertEquals("[(2, 5), (3, 1), (6, 2)]", l1.toString());
    Assert.assertEquals("[(1, 1), (2, 4), (4, 3), (6, 3)]", l2.toString());
    Assert.assertEquals("[(5, 1), (7, 2)]", l3.toString());
  }

  /**
   * Tests for the method intersectBinary().
   */
  @Test
  public void testIntersectBinary() {
    PostingList l1 = new PostingList();
    PostingList l2 = new PostingList();
    PostingList l3 = new PostingList();
    l1.readFromFile("example1.txt");
    l2.readFromFile("example2.txt");
    l3.readFromFile("example3.txt");
    PostingList result1 = PostingList.intersectBinary(l1, l2);
    PostingList result2 = PostingList.intersectBinary(l1, l3);
    Assert.assertEquals("[(2, 9), (6, 5)]", result1.toString());
    Assert.assertEquals("[]", result2.toString());
  }


  /**
   * Tests for the method intersectSentinel().
   */
  @Test
  public void testIntersectSentinel() {
    PostingList l1 = new PostingList();
    PostingList l2 = new PostingList();
    PostingList l3 = new PostingList();
    l1.readFromFile("example1.txt");
    l2.readFromFile("example2.txt");
    l3.readFromFile("example3.txt");
    PostingList result1 = PostingList.intersectSentinel(l1, l2);
    PostingList result2 = PostingList.intersectSentinel(l1, l3);
    Assert.assertEquals("[(2, 9), (6, 5)]", result1.toString());
    Assert.assertEquals("[]", result2.toString());
  }


  /**
   * Tests for the method intersect().
   */
  @Test
  public void testIntersect() {
    PostingList l1 = new PostingList();
    PostingList l2 = new PostingList();
    PostingList l3 = new PostingList();
    l1.readFromFile("example1.txt");
    l2.readFromFile("example2.txt");
    l3.readFromFile("example3.txt");
    PostingList result1 = PostingList.intersect(l1, l2);
    PostingList result2 = PostingList.intersect(l1, l3);
    Assert.assertEquals("[(2, 9), (6, 5)]", result1.toString());
    Assert.assertEquals("[]", result2.toString());
  }

  /**
   * +++ IMPORTANT +++
   *
   * You have to implement tests for each new method you add to the PostingList
   * class.
   *
   * In particular, your "intersect" method should run the test for the
   * intersectBaseline() method above successfully.
   *
   * Please note: If you add several intersection methods with different
   * strategies, EACH of them must must pass the test case provided for the
   * baseline implementation.
   **/
}
