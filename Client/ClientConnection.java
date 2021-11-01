import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
//youtube link to the demo https://youtu.be/Xu8b1VmmILI

public class ClientConnection extends Thread{

    //Client connection attributes
    Socket socket;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    boolean shouldRun = true;
    Client c;


    //Clientconnection constructor similar to the server ClientHandler thread
    public ClientConnection(Socket s, Client client) throws IOException, ClassNotFoundException{
       
        socket = s;
        this.c = client;
    }

    //this function is used to send message from the client to the sever
    public void sendStringToServer(Message sent) throws ClassNotFoundException{
        try{
            objectOutputStream.writeObject(sent);
            objectOutputStream.flush();

        }catch (IOException e){
            e.printStackTrace();
            close();
        }
       

    }
    //This client thread is to read server callbacks asycnronously
    //a sepreate client thread runs similar to the server code
    //the while loop is running the receiveing function 
    //it will check if the incomming message is an error, to startgame, or result
    //each of those actions work like a middleware and perform some action
    public void run(){
        try{

            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            while(shouldRun){
                Message recieved = (Message) objectInputStream.readObject();
                if(recieved.action.equals("ERROR")){
                    System.out.println("there is an error");

                }
                else if(recieved.action.equals("STARTGAME")){
                    c.inGame = true;
                    System.out.println(c.name+" gameStarted");
                }
                else if(recieved.action.equals("RESULT")){
                    c.wallet = recieved.money;
                    System.out.println(recieved.message + "=== "+ "Wallet :" + c.wallet);
                }
                else{
                    System.out.println(recieved.message);
                }
            }
        }catch(IOException e){
            e.printStackTrace();
            close();
        }catch (ClassNotFoundException e){
            e.printStackTrace();
        }

    }

    //close funtion to safely close the thread
    public void close() {
        try{
            System.out.println("Closing socket and terminating program.");
            objectInputStream.close();
            objectOutputStream.close();
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
        
    }

}