
// ScoreServer.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th

/*
   A sequential server that stores a client's score (and name) in a
   list of top-10 high scores.

   Understood input messages:
		get					   -- returns the high score list
        score name & score &   -- add the score for name
		bye					   -- terminates the client link

   The list is maintained in a file SCORFN, and loaded when the
   server starts.
   The server is terminated with a ctrl-C
*/

import java.io.*;
import java.net.*;
import java.util.*;


public class ScoreServer
{
  private static final int PORT = 1234;
  private HighScores hs;
  
  public ScoreServer()
  // Sequentially process clients forever
  {
    hs = new HighScores();
    try {
      ServerSocket serverSock = new ServerSocket(PORT);
      Socket clientSock;
      BufferedReader in;     // i/o for the server
      PrintWriter out;

      while (true) {
        System.out.println("Waiting for a client...");
        clientSock = serverSock.accept();
        System.out.println("Client connection from " + 
				clientSock.getInetAddress().getHostAddress() );

        // Get I/O streams from the socket
        in  = new BufferedReader( 
		  		new InputStreamReader( clientSock.getInputStream() ) );
        out = new PrintWriter( clientSock.getOutputStream(), true );  // autoflush

        // interact with a client
        processClient(in, out);
 
        // Close client connection
        clientSock.close();
        System.out.println("Client connection closed\n");
        hs.saveScores();      // backup high scores after each client has finished
      }
    }
    catch(Exception e)
    {  System.out.println(e);  }
  }  // end of ScoreServer()


   private void processClient(BufferedReader in, PrintWriter out)
   // Stop when the input stream closes (is null) or "bye" is sent
   // Otherwise pass the input to doRequest()
   {
     String line;
     boolean done = false;
     try {
       while (!done) {
         if((line = in.readLine()) == null)
           done = true;
         else {
           System.out.println("Client msg: " + line);
           if (line.trim().equals("bye"))
             done = true;
           else 
             doRequest(line, out);
         }
       }
     }
     catch(IOException e)
     {  System.out.println(e);  }
   }  // end of processClient()


  private void doRequest(String line, PrintWriter out)
  /*  The input line can be one of:
             "score name & score &"
      or     "get"
  */
  {
    if (line.trim().toLowerCase().equals("get")) {
      System.out.println("Processing 'get'");
      out.println( hs.toString() );
    }
    else if ((line.length() >= 6) &&     // "score "
        (line.substring(0,5).toLowerCase().equals("score"))) {
      System.out.println("Processing 'score'");
      hs.addScore( line.substring(5) );    // cut the score keyword
    }
    else
      System.out.println("Ignoring input line");
  }  // end of doRequest()


  // ------------------------------------

  public static void main(String args[]) 
  {  new ScoreServer();  }

} // end of ScoreServer class

