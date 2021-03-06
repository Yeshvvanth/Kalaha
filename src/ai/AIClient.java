package ai;

import ai.Global;
import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import kalaha.*;

/**
 * This is the main class for your Kalaha AI bot. Currently
 * it only makes a random, valid move each turn.
 *
 * @author Johan Hagelbäck
 */
public class AIClient implements Runnable
{
    private int player;
    private JTextArea text;

    private PrintWriter out;
    private BufferedReader in;
    private Thread thr;
    private Socket socket;
    private boolean running;
    private boolean connected;

    /**
     * Creates a new client.
     */
    public AIClient()
    {
  player = -1;
        connected = false;

        //This is some necessary client stuff. You don't need
        //to change anything here.
        initGUI();

        try
        {
            addText("Connecting to localhost:" + KalahaMain.port);
            socket = new Socket("localhost", KalahaMain.port);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            addText("Done");
            connected = true;
        }
        catch (Exception ex)
        {
            addText("Unable to connect to server");
            return;
        }
    }

    /**
     * Starts the client thread.
     */
    public void start()
    {
        //Don't change this
        if (connected)
        {
            thr = new Thread(this);
            thr.start();
        }
    }

    /**
     * Creates the GUI.
     */
    private void initGUI()
    {
        //Client GUI stuff. You don't need to change this.
        JFrame frame = new JFrame("My AI Client");
        frame.setLocation(Global.getClientXpos(), 445);
        frame.setSize(new Dimension(420,250));
        frame.getContentPane().setLayout(new FlowLayout());

        text = new JTextArea();
        JScrollPane pane = new JScrollPane(text);
        pane.setPreferredSize(new Dimension(400, 210));

        frame.getContentPane().add(pane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);
    }

    /**
     * Adds a text string to the GUI textarea.
     *
     * @param txt The text to add
     */
    public void addText(String txt)
    {
        //Don't change this
        text.append(txt + "\n");
        text.setCaretPosition(text.getDocument().getLength());
    }

    /**
     * Thread for server communication. Checks when it is this
     * client's turn to make a move.
     */
    public void run()
    {
        String reply;
        running = true;

        try
        {
            while (running)
            {
                //Checks which player you are. No need to change this.
                if (player == -1)
                {
                    out.println(Commands.HELLO);
                    reply = in.readLine();

                    String tokens[] = reply.split(" ");
                    player = Integer.parseInt(tokens[1]);

                    addText("I am player " + player);
                }

                //Check if game has ended. No need to change this.
                out.println(Commands.WINNER);
                reply = in.readLine();
                if(reply.equals("1") || reply.equals("2") )
                {
                    int w = Integer.parseInt(reply);
                    if (w == player)
                    {
                        addText("I won!");
                    }
                    else
                    {
                        addText("I lost...");
                    }
                    running = false;
                }
                if(reply.equals("0"))
                {
                    addText("Even game!");
                    running = false;
                }

                //Check if it is my turn. If so, do a move
                out.println(Commands.NEXT_PLAYER);
                reply = in.readLine();
                if (!reply.equals(Errors.GAME_NOT_FULL) && running)
                {
                    int nextPlayer = Integer.parseInt(reply);

                    if(nextPlayer == player)
                    {
                        out.println(Commands.BOARD);
                        String currentBoardStr = in.readLine();
                        boolean validMove = false;
                        while (!validMove)
                        {
                            long startT = System.currentTimeMillis();
                            //This is the call to the function for making a move.
                            //You only need to change the contents in the getMove()
                            //function.
                            GameState currentBoard = new GameState(currentBoardStr);
                            int cMove = getMove(currentBoard);

                            //Timer stuff
                            long tot = System.currentTimeMillis() - startT;
                            double e = (double)tot / (double)1000;

                            out.println(Commands.MOVE + " " + cMove + " " + player);
                            reply = in.readLine();
                            if (!reply.startsWith("ERROR"))
                            {
                                validMove = true;
                                addText("Made move " + cMove + " in " + e + " secs");
                            }
                        }
                    }
                }

                //Wait
                Thread.sleep(100);
            }
  }
        catch (Exception ex)
        {
            running = false;
        }

        try
        {
            socket.close();
            addText("Disconnected from server");
        }
        catch (Exception ex)
        {
            addText("Error closing connection: " + ex.getMessage());
        }
    }

    /**
     * This is the method that makes a move each time it is your turn.
     * Here you need to change the call to the random method to your
     * Minimax search.
     *
     * @param currentBoard The current board state
     * @return Move to make (1-6)
     */
     public int getMove(GameState currentBoard)
   {

       return iterativeSearch(currentBoard);
   }

   public int iterativeSearch(GameState game)
   {
       int finalMove = 0;
       int finalState = Integer.MIN_VALUE; 
       int state=0;
       int depth = 0;
       long limit = System.currentTimeMillis() + (5000);
     
       while (true) 
      {
         
         long now = System.currentTimeMillis();
         //System.out.println(depth);
         if (now >= limit)
         {
               System.out.println("break");
               break;
         }
       
       for(int i=1; i < 7 ; i++)
       {
          now = System.currentTimeMillis();
          if(now >=limit) 
          {
          System.out.println("break");
          break;
          }          
           System.out.println(i);     
           if (game.moveIsPossible(i))
           {
               GameState gamestate =  game.clone();
               if(gamestate.makeMove(i)) {
                 state=minMax(gamestate, depth,true,Integer.MIN_VALUE,Integer.MAX_VALUE,limit);
               }else {
                 state=minMax(gamestate,depth,false,Integer.MIN_VALUE,Integer.MAX_VALUE,limit);
               }
               //System.out.println(i+" "+state)
               //setting the move, which results in best outcome         
           }
           if(state>finalState)
           {
               finalState=state;
               finalMove=i; 
           }
           
       }
       depth++;
           
       }
       //bringing out the best move
       return finalMove;
   }
   
   public int minMax(GameState gameBoard, int depth,boolean playerTurn,int alpha,int beta,long limit)
   {
     int finalState;
     
     
     if(!playerTurn) {
       finalState=Integer.MAX_VALUE;
     }else {
       finalState=Integer.MIN_VALUE;
     }
       int state;
       //reached the number of steps we considered, so return the score
       if(depth<=0) return getScore(gameBoard);
       
       boolean oneMoreChance =false;
       //iterating all the possible moves
       for(int i = 1;i<7;i++)
       {
           if(gameBoard.moveIsPossible(i)) {
           GameState gameState= gameBoard.clone();
           
           //to check if the last pebble is placed in house, so one more turn
           if(gameState.makeMove(i)) oneMoreChance = true;
           else oneMoreChance = false;

           if(playerTurn) {
             
             //recursive, reducing the depth after each move
               if(oneMoreChance) state=minMax(gameState,depth-1,true,alpha,beta,limit);
               else state=minMax(gameState,depth-1,false,alpha,beta,limit);
               
               finalState = Math.max(finalState, state);
               
               //alpha-beta pruning
               alpha = Math.max(alpha, state);
               if(beta<=alpha) break;
           }
           else {
               if(oneMoreChance) state=minMax(gameState,depth-1,false,alpha,beta,limit);
               else state=minMax(gameState,depth-1,true,alpha,beta,limit);
              
               finalState = Math.min(finalState, state);
               beta= Math.min(beta, state);
               if(beta<=alpha) break;
           }
          }
       }
       return finalState;
   }
 
   //used as a metric for measuring how good the move is for the ai player
   public int getScore(GameState game) {
       return game.getScore(player)-game.getScore(SwitchPlayer());
   }

   //used for getting the other players turn
  private int SwitchPlayer() {
    if(player==1){
        return 2;
    }
    else{
        return 1;
    }
  }
   
 }