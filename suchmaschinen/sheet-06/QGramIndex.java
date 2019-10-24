// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Author: Hannah Bast <bast@cs.uni-freiburg.de>,
//         Claudius Korzen <korzen@cs.uni-freiburg.de>.

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeMap;

/**
 * A simple q-gram index as explained in lecture 5.
 */
public class QGramIndex {
  /**
   * The value of q.
   */
  protected int q;

  /**
   * The padding (q-1 times $).
   */
  protected String padding;

  /**
   * The inverted lists.
   */
  protected TreeMap<String, List<IntIntPair>> invertedLists;

  /**
   * The list of entities.
   */
  protected List<Entity> entities;

  /**
   * The boolean flag that indicates whether to use synonyms or not.
   */
  protected boolean withSynonyms;

  /**
   * Creates an empty q-gram index.
   *
   * @param q
   *        The value of q.
   * @param withSynonyms
   *        The boolean flag that indicates whether to use synonyms or not.
   */
  public QGramIndex(int q, boolean withSynonyms) {
    this.q = q;
    this.withSynonyms = withSynonyms;
    this.padding = String.join("", Collections.nCopies(q - 1, "$"));
    this.invertedLists = new TreeMap<>();
    this.entities = new ArrayList<>();
  }

  // ==========================================================================
  // Exercise 1.1

  /**
   * Builds the index from the given file (one line per entity, see ES5).
   *
   * @param fileName
   *        the name of the file to read.
   */
  protected void buildFromFile(String fileName) {
    try (BufferedReader br = Files.newBufferedReader(Paths.get(fileName))) {
      // Ignore the first line (which includes the column headers).
      br.readLine();

      // Iterate through the remaining lines.
      String line;
      int entityId = 0;

      while ((line = br.readLine()) != null) {
        entityId++;
        // Split the line and fetch the several fields.
        String[] parts = line.split("\t", -1);

        String name = parts.length > 0 ? parts[0] : null;
        int score = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
        String description = parts.length > 2 ? parts[2] : null;
        String wikipediaUrl = parts.length > 3 ? parts[3] : null;
        String wikidataId = parts.length > 4 ? parts[4] : null;
        List<String> synonyms = new ArrayList<>();
        if (parts.length > 5 && !parts[5].isEmpty()) {
          synonyms.addAll(Arrays.asList(parts[5].split(";")));
        }

        if (name != null) {
          // Compute the q-grams of the entity name and add them to the index.
          for (String qGram : computeQGrams(name)) {
            if (!this.invertedLists.containsKey(qGram)) {
              this.invertedLists.put(qGram, new ArrayList<>());
            }
            List<IntIntPair> il = this.invertedLists.get(qGram);
            // Check if the id of the last pair is equal to the current id.
            if (!il.isEmpty() && il.get(il.size() - 1).first == entityId) {
              // Increment the frequency of the last pair.
              il.get(il.size() - 1).second++;
            } else {
              // Add a new pair (entityId, 1) to the list.
              il.add(new IntIntPair(entityId, 1));
            }
          }

          if (this.withSynonyms) {
            // Compute the q-grams of the synonyms and add them to the index.
            for (String synonym : synonyms) {
              for (String qGram : computeQGrams(synonym)) {
                if (!this.invertedLists.containsKey(qGram)) {
                  this.invertedLists.put(qGram, new ArrayList<>());
                }
                List<IntIntPair> il = this.invertedLists.get(qGram);
                // Check if the id of the last pair is equal to the current id.
                if (!il.isEmpty() && il.get(il.size() - 1).first == entityId) {
                  // Increment the frequency of the last pair.
                  il.get(il.size() - 1).second++;
                } else {
                  // Add a new pair (entityId, 1) to the list.
                  il.add(new IntIntPair(entityId, 1));
                }
              }
            }
          }

          // Cache the entity.
          this.entities.add(new Entity(name, score, description, wikipediaUrl,
              wikidataId, synonyms));
        }
      }
    } catch (IOException e) {
      System.err.println("Could not read \"" + fileName + "\"");
      e.printStackTrace();
    }
  }

  // ==========================================================================
  // Exercise 1.2

  /**
   * Merges the given inverted lists.
   *
   * @param lists
   *        The inverted lists to merge.
   *
   * @return A list of pairs (id, frequency) where 'id' is an element that
   *         occurs at least in one of the lists and 'frequency' is the
   *         frequency of the id in the lists.
   */
  protected static List<IntIntPair> mergeLists(List<List<IntIntPair>> lists) {
    if (lists.isEmpty()) {
      return new ArrayList<>();
    }

    List<IntIntPair> union = lists.get(0);
    for (int i = 1; i < lists.size(); i++) {
      union = mergeLists(union, lists.get(i));
    }

    return union;
  }

  /**
   * Merges the two given inverted lists.
   *
   * @param list1
   *        The first list to merge.
   * @param list2
   *        The second list to merge.
   *
   * @return A list of pairs (id, frequency) where 'id' is an element that
   *         occurs at least in one of the lists and 'frequency' is the
   *         frequency of the id in the lists.
   */
  protected static List<IntIntPair> mergeLists(List<IntIntPair> list1,
      List<IntIntPair> list2) {
    // The pointer in the first list.
    int i = 0;
    // The pointer in the second list.
    int j = 0;

    List<IntIntPair> result = new ArrayList<>();

    // Iterate the lists in an interleaving order and aggregate the frequencies.
    while (i < list1.size() && j < list2.size()) {
      if (list1.get(i).first == list2.get(j).first) {
        result.add(new IntIntPair(list1.get(i).first,
            list1.get(i).second + list2.get(j).second));
        i++;
        j++;
      } else if (list1.get(i).first < list2.get(j).first) {
        result.add(list1.get(i));
        i++;
      } else {
        result.add(list2.get(j));
        j++;
      }
    }

    // Append the rest of the first list.
    while (i < list1.size()) {
      result.add(list1.get(i));
      i++;
    }

    // Append the rest of the second list.
    while (j < list2.size()) {
      result.add(list2.get(j));
      j++;
    }

    return result;
  }

  // ==========================================================================
  // Exercise 1.3

  /**
   * Finds all entities y with PED(x, y) <= delta for a given integer delta and
   * a given prefix x.
   *
   * @param prefix
   *        The prefix.
   * @param delta
   *        The value of delta.
   * @return A pair (matches, numPEDComputations), where 'matches' is the list
   *         of matching entities and 'numPEDComputations' is the number of PED
   *         computations needed to compute the matches.
   */
  protected ObjectIntPair<List<Entity>> findMatches(String prefix, int delta) {
    List<Entity> matches = new ArrayList<>();
    int numPedComputations = 0;

    // Normalize the prefix.
    prefix = normalize(prefix);
    int threshold = prefix.length() - (this.q * delta);

    if (prefix.length() > 0) {
      // Fetch all the inverted lists for each q-gram of the prefix.
      List<List<IntIntPair>> lists = new ArrayList<>();
      for (String qGram : computeQGrams(prefix)) {
        if (this.invertedLists.containsKey(qGram)) {
          lists.add(this.invertedLists.get(qGram));
        }
      }

      for (IntIntPair pair : mergeLists(lists)) {
        int id = pair.first;
        int freq = pair.second;
        Entity entity = this.entities.get(id - 1); // ids are 1-based.

        // Compute the PED for all entities where comm(x,y) >= |x| - q * delta.
        if (freq >= threshold) {
          // Compute the PED to the name of the entity.
          int ped = prefixEditDistance(prefix, normalize(entity.name), delta);
          numPedComputations++;

          if (ped <= delta) {
            entity.ped = ped;
            entity.matchedSynonym = null;
            matches.add(entity);
            continue;
          }

          if (this.withSynonyms) {
            // Compute the best matching synonym (the synonym with lowest PED).
            String bestMatchingSynonym = null;
            int bestPed = Integer.MAX_VALUE;

            // Iterate through all synonyms and compute PED.
            for (String syn : entity.synonyms) {
              int synPed = prefixEditDistance(prefix, normalize(syn), delta);
              numPedComputations++;

              // Check if the synonym is the "best" matching synonym.
              if (synPed <= delta && synPed < bestPed) {
                bestPed = synPed;
                bestMatchingSynonym = syn;
              }
            }

            // Take the best matching synonym.
            if (bestMatchingSynonym != null) {
              entity.matchedSynonym = bestMatchingSynonym;
              entity.ped = bestPed;
              matches.add(entity);
            }
          }
        }
      }

      // Rank the matches.
      matches = rankMatches(matches);
    }

    return new ObjectIntPair<>(matches, numPedComputations);
  }

  // ==========================================================================
  // Exercise 1.4

  /**
   * Ranks the given list of entities (PED, s), where PED is the PED value and s
   * is the popularity score of an entity.
   *
   * @param matches
   *        The list of entities to rank.
   *
   * @return The list of entities sorted by (PED, s).
   */
  protected static List<Entity> rankMatches(List<Entity> matches) {
    // Sort the entities by (ped, s).
    Collections.sort(matches, new Comparator<Entity>() {
      @Override
      public int compare(Entity e1, Entity e2) {
        if (e1.ped != e2.ped) {
          return e1.ped - e2.ped;
        }
        return e2.score - e1.score;
      }
    });
    return matches;
  }

  // ==========================================================================

  /**
   * Computes the prefix edit distance PED(x,y) for the two given strings x and
   * y. Returns PED(x,y) if it is smaller or equal to the given delta; delta + 1
   * otherwise.
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
  protected static int prefixEditDistance(String x, String y, int delta) {
    // Compute the dimensions of the matrix.
    int n = x.length() + 1;
    // Note that it is enough to compute the first |x| + δ + 1 columns.
    int m = Math.min(x.length() + delta + 1, y.length() + 1);

    int[][] matrix = new int[n][m];

    // Initialize the first column.
    for (int i = 0; i < n; i++) {
      matrix[i][0] = i;
    }
    // Initialize the first row.
    for (int i = 0; i < m; ++i) {
      matrix[0][i] = i;
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
    for (int i = 0; i < m; ++i) {
      if (matrix[n - 1][i] < minDelta) {
        minDelta = matrix[n - 1][i];
      }
    }

    return minDelta;
  }

  /**
   * Computes all q-grams of the normalized version of the given string.
   *
   * @param string
   *        The string to process.
   *
   * @return The list of computed q-grams.
   */
  protected List<String> computeQGrams(String string) {
    List<String> result = new ArrayList<>();
    String padded = this.padding + normalize(string);
    for (int i = 0; i < padded.length() - this.q + 1; i++) {
      result.add(padded.substring(i, i + this.q));
    }
    return result;
  }

  /**
   * Transforms the given string to lower cases and removes all whitespaces.
   *
   * @param string
   *        The string to normalize.
   *
   * @return The normalized string.
   */
  protected static String normalize(String string) {
    return string.replaceAll("\\W", "").toLowerCase();
  }
}
