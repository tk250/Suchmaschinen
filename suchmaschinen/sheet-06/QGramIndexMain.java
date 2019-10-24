// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Author: Hannah Bast <bast@cs.uni-freiburg.de>,
//         Claudius Korzen <korzen@cs.uni-freiburg.de>.

import java.util.Collections;
import java.util.List;

/**
 * Builds an q-gram index from given file and lets the user type a query in
 * order to do a fuzzy search on the data.
 */
public class QGramIndexMain {
  /**
   * The main method.
   *
   * @param args
   *        The command line arguments.
   */
  public static void main(String[] args) {
    // Parse the command line arguments.
    if (args.length < 1) {
      System.out.println("Usage: java -jar FuzzySearchMain.jar <entity-file> "
          + "[--with-synonyms]");
      System.exit(1);
    }

    String fileName = args[0];
    boolean withSynonyms = args.length > 1 && args[1].equals("--with-synonyms");

    System.out.print("Building index from '" + fileName + "' ... ");
    System.out.flush();

    // Build a 3-gram index from the given file.
    long start = System.currentTimeMillis();
    QGramIndex index = new QGramIndex(3, withSynonyms);
    index.buildFromFile(fileName);
    long end = System.currentTimeMillis();

    System.out.println("done in " + (end - start) + "ms.");

    while (true) {
      System.out.println(String.join("", Collections.nCopies(80, "-")));
      String query = System.console().readLine("Query: ");

      // Normalize the query.
      query = QGramIndex.normalize(query);
      int delta = query.length() / 4;

      start = System.currentTimeMillis();
      ObjectIntPair<List<Entity>> result = index.findMatches(query, delta);
      end = System.currentTimeMillis();

      List<Entity> matches = result.first;

      System.out.println();
      System.out.printf("Found %d matches. ", matches.size());

      int numResults = Math.min(5, matches.size());
      if (numResults > 0) {
        System.out.printf("The top-%d results are:\n", numResults);

        for (int i = 0; i < numResults; i++) {
          Entity e = matches.get(i);

          System.out.printf("\n\033[1m(%d) %s\033[0m ", i + 1, e.name);
          if (e.matchedSynonym != null) {
            System.out.printf("(Matched Synonym: '%s')\n", e.matchedSynonym);
          } else {
            System.out.println();
          }
          System.out.printf("Description:   %s\n", e.desc);
          if (e.wikipediaUrl != null) {
            System.out.printf("Wikipedia-URL: %s\n", e.wikipediaUrl);
          }
          if (e.wikidataId != null) {
            System.out.printf("Wikidata-URL:  "
                + "http://www.wikidata.org/wiki/%s\n", e.wikidataId);
          }
          System.out.printf("PED:           %d\n", e.ped);
          System.out.printf("Score:         %d\n", e.score);
        }
      }

      System.out.println();
      System.out.printf("Time needed to find matches: %dms: ", (end - start));
      System.out.printf(" #PED computations: %d.\n", result.second);
    }
  }
}
