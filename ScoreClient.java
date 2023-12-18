
// ScoreClient.java
// Andrew Davison, April 2005, ad@fivedots.coe.psu.ac.th
/*
  A test rig for ScoreServer with a GUI interface
  The client can send a name/score, and ask for the 
  current high scores. Clicking the close box of the
  window breaks the network link.

  Or we could just use:
     telnet localhost 1234
*/

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.*;


public class ScoreClient extends JFrame implements ActionListener
{
  private static final int PORT = 1234;     // server details
  private static final String HOST = "localhost";

  private Socket sock;
  private BufferedReader in;     // i/o for the client
  private PrintWriter out;  

  private JTextArea jtaMesgs;
  private JTextField jtfName, jtfScore;
  private JButton jbGetScores;


  public ScoreClient()
  {
     super( "High Score Client" );

     initializeGUI();
     makeContact();

     addWindowListener( new WindowAdapter() {
       public void windowClosing(WindowEvent e)
       { closeLink(); }
     });

     setSize(300,450);
     setVisible(true);
  } // end of ScoreClient();


  private void initializeGUI() {
    Container container = getContentPane();
    container.setLayout(new BorderLayout());

    jtaMesgs = new JTextArea(7, 7);
    jtaMesgs.setEditable(false);
    jtaMesgs.setBackground(new Color(255, 240, 245)); // Fondo rosa pastel (RGB)
    jtaMesgs.setFont(new Font("Arial", Font.PLAIN, 40)); // Tipo de letra Arial, tamaño 14
    JScrollPane scrollPane = new JScrollPane(jtaMesgs);
    container.add(scrollPane, BorderLayout.CENTER);

    JLabel nameLabel = new JLabel("Name: ");
    nameLabel.setForeground(Color.BLACK); // Texto negro
    jtfName = new JTextField(10);

    JLabel scoreLabel = new JLabel("Score: ");
    scoreLabel.setForeground(Color.BLACK); // Texto negro
    jtfScore = new JTextField(5);
    jtfScore.addActionListener(this);

    jbGetScores = new JButton("Get Scores");
    jbGetScores.addActionListener(this);
    jbGetScores.setBackground(new Color(204, 229, 255)); // Fondo azul pastel (RGB)

    JPanel nameScorePanel = new JPanel(new FlowLayout());
    nameScorePanel.add(nameLabel);
    nameScorePanel.add(jtfName);
    nameScorePanel.add(scoreLabel);
    nameScorePanel.add(jtfScore);
    nameScorePanel.setBackground(new Color(255, 240, 245)); // Fondo rosa pastel (RGB)

    JPanel getScoresPanel = new JPanel(new FlowLayout());
    getScoresPanel.add(jbGetScores);
    getScoresPanel.setBackground(new Color(204, 229, 255)); // Fondo azul pastel (RGB)

    JPanel controlPanel = new JPanel();
    controlPanel.setLayout(new BoxLayout(controlPanel, BoxLayout.Y_AXIS));
    controlPanel.add(nameScorePanel);
    controlPanel.add(getScoresPanel);
    controlPanel.setBackground(new Color(255, 240, 245)); // Fondo rosa pastel (RGB)

    container.add(controlPanel, BorderLayout.SOUTH);
}





  private void closeLink()
  {
    try {
      out.println("bye");    // tell server that client is disconnecting
      sock.close();
    }
    catch(Exception e)
    {  System.out.println( e );  }

    System.exit( 0 ); 
  }

    
  private void makeContact()
  {
    try {
      sock = new Socket(HOST, PORT);
      in  = new BufferedReader( 
		  		new InputStreamReader( sock.getInputStream() ) );
      out = new PrintWriter( sock.getOutputStream(), true );  // autoflush
    }
    catch(Exception e)
    {  System.out.println(e);  }
  }  // end of makeContact()



   public void actionPerformed(ActionEvent e)
   // Either a name/score is to be sent or the "Get Scores"
   // button has been pressed
   {
     if (e.getSource() == jbGetScores)
       sendGet();
     else if (e.getSource() == jtfScore)
       sendScore();
   }

   private void sendGet()
   {
   // Send "get" command, read response and display it
   // Response should be "HIGH$$ n1 & s1 & .... nN & sN & "
     try {
       out.println("get");
       String line = in.readLine();
       System.out.println(line);
       if ((line.length() >= 7) &&     // "HIGH$$ "
           (line.substring(0,6).equals("HIGH$$")))
         showHigh( line.substring(6).trim() );    
		    // remove HIGH$$ keyword and surrounding spaces
       else    // should not happen
         jtaMesgs.append( line + "\n");
     }
     catch(Exception ex)
     {  
       jtaMesgs.append("Problem obtaining high scores\n");
       System.out.println(ex);  
     }
   }  // end of sendGet()


  private void showHigh(String line)
  // Parse the high scores and display in a nicer way
  {
    StringTokenizer st = new StringTokenizer(line, "&");
    String name;
    int i, score;
    i = 1;
    try {
      while (st.hasMoreTokens()) {
        name = st.nextToken().trim();
        score = Integer.parseInt( st.nextToken().trim() );
        jtaMesgs.append("" + i + ". " + name + " : " + score + "\n");
        i++;
      }
      jtaMesgs.append("\n");
    }
    catch(Exception e)
    { 
      jtaMesgs.append("Problem parsing high scores\n");
      System.out.println("Parsing error with high scores: \n" + e);  
    }
  }  // end of showHigh()


  private void sendScore()
  // Check if the user has supplied a name and score, then
  // send "score name & score &" to server
  // NOte: we should check that score is an integer, but we don't
  {
    String name = jtfName.getText().trim();
    String score = jtfScore.getText().trim();
    // System.out.println("'"+name+"'   '"+score+"'");

    if ((name.equals("")) && (score.equals("")))
      JOptionPane.showMessageDialog( null, 
           "No name and score entered", "Send Score Error", 
			JOptionPane.ERROR_MESSAGE);
    else if (name.equals(""))
      JOptionPane.showMessageDialog( null, 
           "No name entered", "Send Score Error", 
			JOptionPane.ERROR_MESSAGE);
    else if (score.equals(""))
      JOptionPane.showMessageDialog( null, 
           "No score entered", "Send Score Error", 
			JOptionPane.ERROR_MESSAGE);
    else {
      out.println("score " + name + " & " + score + " &");
      jtaMesgs.append("Sent " + name + " & " + score + "\n");
    }
  }  // end of sendScore()


  // ------------------------------------

  public static void main(String args[]) 
  {  new ScoreClient();  }

} // end of ScoreClient class

