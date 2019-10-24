// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Author: Claudius Korzen <korzen@cs.uni-freiburg.de>.

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * A list of postings of form (docId, score).
 */
public class PostingList {
  /**
   * The docIds of the postings in this list.
   */
  protected int[] ids;

  /**
   * The scores of the postings in this list.
   */
  protected int[] scores;

  /**
   * The capacity of this list.
   */
  protected int capacity;

  /**
   * The number of postings in this list.
   */
  protected int numPostings;

  // ==========================================================================

  /**
   * Reads a posting list from the given file.
   *
   * @param fileName
   *        The path to the file to read.
   */
  public void readFromFile(String fileName) {
    try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
      // Read the number of postings in the file from the first line.
      int numPostings = Integer.parseInt(br.readLine());

      // Reserve enough space for the postings.
      reserve(numPostings + 2);

      // Read the file line by line, each in the format <id>WHITESPACE<score>.
      String line;
      while ((line = br.readLine()) != null) {
        // Split the line into <id> and <score>.
        String[] parts = line.split("\\s+");
        int id = Integer.parseInt(parts[0]);
        int score = Integer.parseInt(parts[1]);

        // Add the posting (id, score) to this list.
        addPosting(id, score);
      }
    } catch (IOException e) {
      System.err.println("Couldn't read the file: " + e.getMessage());
      System.exit(1);
    }
  }

  // ==========================================================================

  /**
   * Intersects the two given posting lists using the basic "zipper" algorithm.
   *
   * @param l1
   *        The first posting list.
   * @param l2
   *        The second posting list.
   *
   * @return The intersection of the two lists.
   */
  public static PostingList intersectBaseline(PostingList l1, PostingList l2) {
    PostingList result = new PostingList();
    result.reserve(Math.min(l1.size(), l2.size()));

    int i1 = 0;
    int i2 = 0;
    while (i1 < l1.size() && i2 < l2.size()) {
      while (i1 < l1.size() && l1.getId(i1) < l2.getId(i2)) {
        i1++;
      }

      if (i1 == l1.size()) {
        break;
      }

      while (i2 < l2.size() && l2.getId(i2) < l1.getId(i1)) {
        i2++;
      }

      if (i2 == l2.size()) {
        break;
      }

      if (l1.getId(i1) == l2.getId(i2)) {
        result.addPosting(l1.getId(i1), l1.getScore(i1) + l2.getScore(i2));
        i1++;
        i2++;
      }
    }
    return result;
  }

  /**
   * Intersects the two given posting lists using an improved algorithm that
   * uses at least three non-trivial ideas presented in the lecture.
   *
   * @param l1
   *        The first posting list.
   * @param l2
   *        The second posting list.
   *
   * @return The intersection of the two lists.
   */
  public static PostingList intersect(PostingList l1, PostingList l2) {
    // Implement a new method for intersecting two posting lists that
    // uses at least three non-trivial ideas presented in the lecture. The goal
    // is to beat the baseline implementation for all scenarios of the exercise
    // sheet. Note that you can also implement several algorithms and switch
    // between them depending on the sizes of the input lists (or depending on
    // any information that you find to be useful). Note: Each implemented
    // method must pass the test case provided for the baseline implementation.

    if (l1.size() <= Math.sqrt(l2.size())
        || Math.sqrt(l1.size()) >= l2.size()) {
      return intersectBinary(l1, l2);
    } else {
      return intersectSentinel(l1, l2);
    }
  }

  /**
   * Intersects the two given posting lists using an improved algorithm that
   * uses at least three non-trivial ideas presented in the lecture.
   *
   * @param l1
   *        The first posting list.
   * @param l2
   *        The second posting list.
   *
   * @return The intersection of the two lists.
   */
  public static PostingList intersectSentinel(PostingList l1, PostingList l2) {
    //Implementation with Sentinels.
    PostingList result = new PostingList();
    result.reserve(Math.min(l1.size(), l2.size()));
    l1.addPosting(Integer.MAX_VALUE, 0);
    l2.addPosting(Integer.MAX_VALUE, 0);
    int i1 = 0;
    int i2 = 0;
    while (true) {
      while (l1.getId(i1) < l2.getId(i2)) {
        i1++;
      }
      while (l1.getId(i1) > l2.getId(i2)) {
        i2++;
      }
      if (l1.getId(i1) == Integer.MAX_VALUE
          || l2.getId(i2) == Integer.MAX_VALUE) {
        break;
      }
      if (l1.getId(i1) == l2.getId(i2)) {
        result.addPosting(l1.getId(i1), l1.getScore(i1) + l2.getScore(i2));
        i1++;
        i2++;
      }
    }
    return result;
  }

  /**
   * Intersects the two given posting lists
   *
   * @param l1
   *        The first posting list.
   * @param l2
   *        The second posting list.
   *
   * @return The intersection of the two lists.
   */
  public static PostingList intersectBinary(PostingList l1, PostingList l2) {
    //Implementation Binary search of remaining long list.
    PostingList result = new PostingList();
    result.reserve(Math.min(l1.size(), l2.size()));
    PostingList a = new PostingList();
    PostingList b = new PostingList();
    if (l1.size() < l2.size()) {
      a = l1;
      b = l2;
    } else {
      a = l2;
      b  = l1;
    }
    int c = 0;
    int remainingListSize = b.size();
    int remainingListStart = 0;
    while (c < a.size()) {
      int value = a.getId(c);
      int searcher = remainingListStart + remainingListSize / 2;
      int counter = 2;
      boolean loopWorks = true;
      while (a.getId(c) != b.getId(searcher)) {
        int h = (int)(remainingListSize / Math.pow(2, counter));
        if (a.getId(c) > b.getId(searcher)) {
          searcher += h;
        } else {
          searcher -= h;
        }
        if (counter > Math.log(l2.size()) / Math.log(2)) {
          loopWorks = false;
          break;
        }
        counter++;
      }
      if (loopWorks) {
        result.addPosting(a.getId(c), a.getScore(c) + b.getScore(searcher));
        remainingListStart = searcher + 1;
        remainingListSize -= searcher;
      }
      c++;



    }

    return result;
  }

  // ==========================================================================

  /**
   * Reserves space for n postings in this list.
   *
   * @param n
   *        The number of postings.
   */
  public void reserve(int n) {
    this.ids = new int[n];
    this.scores = new int[n];
    this.capacity = n - 2;
    this.numPostings = 0;
  }

  /**
   * Adds the given posting to this list.
   *
   * @param id
   *        The id of the posting.
   * @param score
   *        The score of the posting.
   */
  public void addPosting(int id, int score) {
    this.ids[this.numPostings] = id;
    this.scores[this.numPostings] = score;
    this.numPostings++;
  }

  /**
   * Returns the id of the i-th posting.
   *
   * @param i
   *        The index of the posting.
   *
   * @return The id of the i-th posting.
   */
  public int getId(int i) {
    return this.ids[i];
  }

  /**
   * Returns the score of the i-th posting.
   *
   * @param i
   *        The index of the posting.
   *
   * @return The score of the i-th posting.
   */
  public int getScore(int i) {
    return this.scores[i];
  }

  /**
   * Returns the number of postings in this list.
   *
   * @return The number of postings in this list.
   */
  public int size() {
    return this.numPostings;
  }

  // ==========================================================================

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    for (int i = 0; i < size(); i++) {
      sb.append("(" + getId(i) + ", " + getScore(i) + ")");
      if (i < size() - 1) {
        sb.append(", ");
      }
    }
    sb.append("]");
    return sb.toString();
  }
}
