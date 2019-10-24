// Copyright 2018, University of Freiburg,
// Chair of Algorithms and Data Structures.
// Authors: Claudius Korzen <korzen@cs.uni-freiburg.de>,
//          Hannah Bast <bast@cs.uni-freiburg.de>

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Basic server code that returns the contents of a requested file.
 */
public class SearchServerMain {
  /**
   * The main method.
   */
  public static void main(String[] args) throws IOException {
    // Parse the command line arguments.
    if (args.length < 2) {
      System.out.println("java -jar SearchServerMain <port> <file>");
      System.exit(1);
    }
    int port = Integer.parseInt(args[0]);
    System.out.println(InetAddress.getLocalHost());

    ServerSocket server = new ServerSocket(port);
    QGramIndex qgi = new QGramIndex(3, false);
    qgi.buildFromFile(args[1]);

    // Server loop.
    while (true) {
      // Wait for the client.

      System.out.print("Waiting for query on port " + port + " ... ");
      System.out.flush();
      Socket client = server.accept();
      System.out.println("client connected from " + client.getInetAddress());

      // Client connected; set read timeout.
      client.setSoTimeout(5000);


      // Read the first line from the the request (not enough for ES6).
      BufferedReader input = new BufferedReader(new InputStreamReader(
                                                  client.getInputStream()));
      String request = "";
      try {
        request = input.readLine();
      } catch (java.net.SocketTimeoutException e) {
        System.out.println("Timeout");
      }
      byte[] contentBytes = new byte[0];
      String contentType = "text/plain";
      String statusString = "HTTP/1.1 200 OK";
      if (!request.startsWith("GET ")) {
        String response = "Only GET requests";
        contentBytes = response.getBytes("UTF-8");
      } else {
        request = request.substring(5, request.indexOf(" HTTP/1.1"));
        // Check for API calls.
        int pos = request.indexOf("?");
        String params = "";
        if (pos != -1) {
          params = request.substring(pos + 7);
        }
        if (request.startsWith("api?query=")) {
          params = QGramIndex.normalize(params);
          int delta = (int) Math.floor(params.length() / 4);
          ObjectIntPair<List<Entity>> matches =
              qgi.findMatches(params, delta);
          int datalenght = Math.min(5, matches.first.size());
          List<Entity> bestFive = matches.first.subList(0, datalenght);
          StringBuilder matchBuilder = new StringBuilder();
          for (Entity entry : bestFive) {
            String replacement = entry.name + ";" + entry.score + ";"
                + entry.desc;
            matchBuilder.append(replacement + "\t" + "\r\n");
          }
          String matchString = matchBuilder.toString();
          System.out.println(matchString);
          contentBytes = matchString.getBytes("UTF-8");
          contentType = "application/json";
        } else {
          if (pos != -1) {
            request = request.substring(0, pos);
          }

          if (request.contains("/")) {
            statusString = "HTTP/1.1 403 Not allowed";
          } else {
            Path file = Paths.get(request);
            if (Files.isRegularFile(file) && Files.isReadable(file)) {
              // We found the file
              File reqFile = new File(request);
              FileInputStream fis = new FileInputStream(reqFile);
              contentBytes = new byte[(int) reqFile.length()];
              fis.read(contentBytes);
              if (request.endsWith(".html" + ".css")) {
                contentType = "text/" + request.substring(request.indexOf(".")
                  + 1);
              } else if (request.endsWith(".js")) {
                contentType = "application/javascript";
              }
            } else {
              // File isnt on server
              statusString = "HTTP/1.1 404 Not found";
            }
          }
        }
      }




      // Send the response.
      StringBuilder responseBuilder = new StringBuilder();
      responseBuilder.append(statusString + "\r\n");
      responseBuilder.append("Content Lenght: " + contentBytes.length + "\r\n");
      responseBuilder.append("Content Type: " + contentType + "\r\n");
      responseBuilder.append("\r\n");
      DataOutputStream output = new DataOutputStream(client.getOutputStream());
      output.write(responseBuilder.toString().getBytes("UTF-8"));
      output.write(contentBytes);

      output.close();
      input.close();
      client.close();
    }
  }
}
