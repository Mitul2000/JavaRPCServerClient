import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
//youtube link to the demo https://youtu.be/Xu8b1VmmILI
public class Server {

    //following datastructures are used to create database like key value connections to allow for multiple room and user finding
    ServerSocket ss;
    boolean shouldRun = true;
    ArrayList<ClientHandler> connections = new ArrayList<ClientHandler>();
    HashMap<String, ClientHandler> conTable = new HashMap<String, ClientHandler>();
    HashMap<String, ArrayList<String>> roomTable = new HashMap<String, ArrayList<String>>();
    HashMap<String, ArrayList<ClientHandler>> roomConnection = new HashMap<String, ArrayList<ClientHandler>>();
    HashMap<String, String> userroom = new HashMap<String, String>();

    
    //Runs the server
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Server();
    }

    //Starts the server constructor
    //Starts the serversocket and waits for incomming conection.
    //new thread is created for each client for concurrent excution
    public Server() throws IOException, ClassNotFoundException{
        ss = new ServerSocket(7777);
        System.out.println("ServerSocket awaiting connections...");
        
        while (shouldRun){
            Socket socket = ss.accept();
            System.out.println("Connection from " + socket + "!");
            ClientHandler ch = new ClientHandler(socket, this);
            
            ch.start();
            connections.add(ch);
        }

        
    }


}