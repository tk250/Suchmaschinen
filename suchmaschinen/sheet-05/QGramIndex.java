// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Authors: Claudius Korzen <korzen@cs.uni-freiburg.de>

import java.nio.file.Files;
import java.nio.file.Paths;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.TreeMap;
import java.util.ArrayList;

/**
 * First steps towards a q-gram index, written during class.
 */
public class QGramIndex {
  // The q from the q-gram index.
  protected int q;

  // The padding (q - 1 times $).
  protected String padding;

  // The inverted lists (one per q-gram).
  protected TreeMap<String, ArrayList<Integer>> invertedLists;

  /**
   * Creates an empty QGramIndex.
   */
  public QGramIndex(int q) {
    this.q = q;
    this.padding = new String(new char[q - 1]).replace("\u0000", "$");
    this.invertedLists = new TreeMap<>();
  }

  /**
   * Builds the index from the given file (see ES5 for the exact file format).
   */
  public void buildFromFile(String fileName) throws IOException {
    String line;
    int entityId = 0;

    try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
      // Ignore the first line.
      br.readLine();
      // Iterate through the remaining lines.
      while ((line = br.readLine()) != null) {
        String entityName = line.split("\t")[0];
        entityId++;
        // Compute the q-grams of the (normalized) entity name.
        for (String qGram : computeQGrams(entityName)) {
          if (!invertedLists.containsKey(qGram)) {
            invertedLists.put(qGram, new ArrayList<>());
          }
          invertedLists.get(qGram).add(entityId);
        }
      }
    }
  }

  /**
   * Computes the prefix edit distance PED(x,y) for the two given strings x and
   * y. Returns PED(x,y) if it is smaller or equal to the given delta and
   * delta + 1 otherwise.
   *
   * @param x
   *        The first string.
   * @param y
   *        The second string.
   * @param delta
   *        The value of delta.
   * @return PED(x,y) if it is smaller or equal to the given delta; delta + 1
   *         otherwise.
   */
  public static int prefixEditDistance(String x, String y, int delta) {
    // Compute the dimensions n and m of the matrix.
    int n = x.length() + 1;
    // Note that it is enough to compute the first |x| + δ + 1 columns.
    int m = Math.min(x.length() + delta + 1, y.length() + 1);

    int[][] matrix = new int[n][m];

    // Initialize the first column.
    for (int i = 0; i < n; i++) {
      matrix[i][0] = i;
    }
    // Initialize the first row.
    for (int j = 0; j < m; j++) {
      matrix[0][j] = j;
    }

    // Compute the rest of the matrix.
    for (int i = 1; i < n; i++) {
      for (int j = 1; j < m; j++) {
        int s = x.charAt(i - 1) == y.charAt(j - 1) ? 0 : 1;
        int replaceCosts = matrix[i - 1][j - 1] + s;
        int addCosts = matrix[i][j - 1] + 1;
        int deleteCosts = matrix[i - 1][j] + 1;

        matrix[i][j] = Math.min(replaceCosts, Math.min(addCosts, deleteCosts));
      }
    }

    // Search the last row for the minimum value.
    int minDelta = delta + 1;
    for (int i = 0; i < m; i++) {
      if (matrix[n - 1][i] < minDelta) {
        minDelta = matrix[n - 1][i];
      }
    }

    return minDelta;
  }

  /**
   * Normalizes the given string (removes non-word characters and lowercase).
   */
  public static String normalize(String str) {
    return str.replaceAll("\\W", "").toLowerCase();
  }

  /**
   * Computes the q-grams for the padded, normalized version of the given word.
   */
  public ArrayList<String> computeQGrams(String word) {
    ArrayList<String> result = new ArrayList<>();
    word = padding + normalize(word) + padding;
    for (int i = 0; i < word.length() - q + 1; i++) {
      result.add(word.substring(i, i + q));
    }
    return result;
  }
}
