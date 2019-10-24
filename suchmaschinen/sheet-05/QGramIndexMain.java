// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Authors: Claudius Korzen <korzen@cs.uni-freiburg.de>

import java.io.IOException;
import java.util.Map;
import java.util.ArrayList;

/**
 * The main class for our QGramIndex.
 */
public class QGramIndexMain {
  /**
   * The main method.
   */
  public static void main(String[] args) throws IOException {
    // Parse the commmand line arguments.
    if (args.length != 1) {
      System.out.println("Usage: java -jar QGramIndexMain <entity-file>");
      System.exit(1);
    }

    String fileName = args[0];
    System.out.print("Building index from '" +  fileName + "' ...");

    // Build a 3-gram index from the given file.
    QGramIndex qgi = new QGramIndex(3);

    long start = System.currentTimeMillis();
    qgi.buildFromFile(fileName);
    long end = System.currentTimeMillis();

    System.out.println(" done in " + (end - start) + "ms.");

    // Print all q-grams and the lengths of their inverted list.
    for (String qGram : qgi.invertedLists.keySet()) {
      System.out.println(qGram + "\t" + qgi.invertedLists.get(qGram).size());
    }
  }
}
