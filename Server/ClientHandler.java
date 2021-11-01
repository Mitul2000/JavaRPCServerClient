import java.io.*;
import java.net.*;
import java.security.MessageDigest;
import java.util.*;
//youtube link to the demo https://youtu.be/Xu8b1VmmILI
public class ClientHandler extends Thread{
    
    //Each Client thread needs to refrence the server it belongs to
    //Each threads has its own objectinput and output stream
    //Boolean variables are used for the flow of control
    Socket socket;
    Server server;
    ObjectInputStream objectInputStream;
    ObjectOutputStream objectOutputStream;
    boolean shouldRun = true;

    //constructor which sets the socket and server
    public ClientHandler(Socket socket, Server sServer) throws IOException, ClassNotFoundException{
        super("ServerConnectionThread");
        this.socket = socket;
        this.server = sServer;
    }

    //responsible for sending messages to client based on the referenced ClientHandler thread
    public void sendStringToClient(Message text) throws IOException, ClassNotFoundException{
        objectOutputStream.writeObject(text);
        objectOutputStream.flush();
        
    }

    //Reponsible for Getting all the members in a room using their unique id
    //A hashmap finds the arraylist associated with the userid and itherates through all ClinetHandler Threads
    //If the action of the thread is Result, it does not append the people in current room
    //Calls the sendStringtoClient message and individally sends the information to all the clients
    public void notifyRoomMembers(Message text) throws IOException, ClassNotFoundException{

        String roomid = server.userroom.get(text.userID);
        String roomp = stringpeoplelist(server.roomTable.get(roomid));
        Message ackpeople = text;
        if(!text.action.equals("RESULT")){
            ackpeople.message = roomp;
        }
        ArrayList<ClientHandler> currentroom = server.roomConnection.get(roomid);
        for(int i=0; i< currentroom.size(); i++){
            ClientHandler ch = currentroom.get(i);
            ch.sendStringToClient(ackpeople);
        }
        System.out.println("Server Size = " + server.connections.size());

    }

    //This function behaves in like a broadcast unit
    //It gets all the ClientHandler thread that are connected and sends the desired message
    public void sendStringToAllClient(Message text) throws IOException, ClassNotFoundException{
        Iterator hmIterator = server.conTable.entrySet().iterator();
        while(hmIterator.hasNext()){
            Map.Entry mapElement = (Map.Entry)hmIterator.next();
            ClientHandler ch = (ClientHandler) mapElement.getValue();
            ch.sendStringToClient(text);
        }

    }

    //run meathod is used as the main opperation for the server logic.
    //Each incoming message is passed through the actionNavigator which decides how the client input should be handled
    // There are referecne to input and output stream for communication
    public void run() {
        try{
            objectInputStream = new ObjectInputStream(socket.getInputStream());
            objectOutputStream = new ObjectOutputStream(socket.getOutputStream());
    
            while (shouldRun){
                Message share = (Message) objectInputStream.readObject();
                actionNavigator(share);
                System.out.println("Message sent By: " + share.name + " Action: "+share.action+ " "+ "Message : " + share.message);
                //set change to arraylist here;
                sendStringToClient(share);
            }

            objectOutputStream.close();
            objectInputStream.close();
            socket.close();

        }catch(IOException e){
            e.printStackTrace();
        }catch(ClassNotFoundException e){
            e.printStackTrace();
        }

    }

    //actionNavigator behaves similar to a middleware. 
    //Each Client input has an action associated to it.
    //when the inputput stream gets a client message, it will have an action assoicated to it
    //each action guides how the clientMessageThread should be handled
    public void actionNavigator(Message mess) throws IOException, ClassNotFoundException {
        ArrayList<String> roomPeople;
        ArrayList<ClientHandler> temproomconnection;
        // mess.message contains the roomid

        //CREATEROOM saves the user id as a key and adds it to an arraylist of all Clienthandlers
        //It also saves the user id as a key and adds it to an arraylist of all room associated people.
        if(mess.action.equals("CREATEROOM")){
            System.out.println("new User to Hashmap");
            InitilizeHashmap(mess);
            if(!server.roomTable.containsKey(mess.message)){
                roomPeople = new ArrayList<String>();
                roomPeople.add(mess.name);
                server.roomTable.put(mess.message,roomPeople);

                temproomconnection = new ArrayList<ClientHandler>();
                temproomconnection.add(this);
                server.roomConnection.put(mess.message, temproomconnection);

                server.userroom.put(mess.userID, mess.message);

                Message ack = mess;
                ack.message = "Your Room is successfully Created";
                // sendStringToClient(ack);
                notifyRoomMembers(ack);
            }
        }
        //JOINROOM is responsible for joinging a room based on a certain key
        //this key is provided by the client message and appeands it to the hashmap 
        // appends it to both the roomtable and room connection
        //If the key is invaid, and error message is sent
        else if( mess.action.equals("JOINROOM")){
            System.out.println("new User to Hashmap");
            InitilizeHashmap(mess);
            if(server.roomTable.containsKey(mess.message)){
                server.roomTable.get(mess.message).add(mess.name);
                server.roomConnection.get(mess.message).add(this);
                server.userroom.put(mess.userID, mess.message);
                Message ack = mess;
                ack.message = "Joined RoomId ";
                // sendStringToClient(ack);
                notifyRoomMembers(ack);
            }
            else{
                Message failed = mess;
                failed.action = "ERROR";
                failed.message = "Invalid room Id Try reconnecting";
                sendStringToClient(failed);
            }
        }
        //this is used to start the game for the user
        //this treads makes the computer ai which is used to play the game
        else if(mess.action.equals("SGAME")){
            System.out.println("sending messages to everyone");
            ComputerAI comp = new ComputerAI();
            Message compcreated = mess;
            compcreated.action = "STARTGAME";
            compcreated.message = "Playing with Friends against Computer";
            notifyRoomMembers(compcreated);
        }
        //this function is used to play the rock paper sissor game
        //It compares user input to the computer input
        //calcuates the win condition and emits it all the players in the loby
        else if(mess.action.equals("PLAYERCHOICE")){
            
            String playerC = mess.message;
            String computerC = computermove();
            String winmessage = winningdecisionString(mess.name,playerC,computerC);
            int moneytransfer = winnings(playerC, computerC);
            System.out.println(mess.name + " " +playerC+ " C "+computerC);
            System.out.println(winmessage);
            Message resultmess = mess;
            resultmess.action = "RESULT";
            resultmess.message = winmessage;
            if(moneytransfer == 1){
                resultmess.money = resultmess.money*2;
            }
            if(moneytransfer == -1){
                resultmess.money = resultmess.money/2;
            }
            notifyRoomMembers(resultmess);
        }

    }
    //used to initiliaze the hashamp in the server object
    public void InitilizeHashmap(Message mess) throws IOException, ClassNotFoundException{
        
        server.conTable.put(mess.userID, this);
        System.out.println(server.conTable.get(mess.userID));
    }
    //Returns all the people in a list as a string
    public String stringpeoplelist(ArrayList<String> pnames){
        String listString = "In Lobby : ";

        for (String s : pnames)
        {
            listString += s + ", ";
        }
        return listString;
    }

    //randoomly assigns a computer move
    public String computermove(){
        String[] arr={"R", "P", "S"};
      	Random r=new Random();        
      	int randomNumber=r.nextInt(arr.length);
        return arr[randomNumber];
    }

    //game logic implentation for rock paper sissor
    //compares user input to the computer move and returns a string
    public String winningdecisionString(String playername, String playerc, String computerc){
        if(playerc.equals(computerc)){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT : DRAW";
        }
        else if(playerc.equals("R") && computerc.equals("P")){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT Computer WINS";
        }
        else if(playerc.equals("R") && computerc.equals("S")){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT :" +playername +" WINS";
        }
        else if(playerc.equals("S") && computerc.equals("P")){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT :" +playername +" WINS";
        }
        else if(playerc.equals("S") && computerc.equals("R")){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT : Computer WINS";
        }
        else if(playerc.equals("P") && computerc.equals("R")){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT :" +playername +" WINS";
        }
        else if(playerc.equals("P") && computerc.equals("S")){
            return playername + " : "+ playerc+ " , Computer : "+ computerc + " RESULT : Computer WINS";
        }
        else{
            return playername + " made Invalid Move";
        }
    }


    // Winnings assigns a numberical value and is used to change the client wallet based on outcome
    public int winnings(String playerc, String computerc){
        if(playerc.equals(computerc)){
            return 0;
        }
        else if(playerc.equals("R") && computerc.equals("P")){
            return -1;
        }
        else if(playerc.equals("R") && computerc.equals("S")){
            return 1;
        }
        else if(playerc.equals("S") && computerc.equals("P")){
            return 1;
        }
        else if(playerc.equals("S") && computerc.equals("R")){
            return -1;
        }
        else if(playerc.equals("P") && computerc.equals("R")){
            return 1;
        }
        else if(playerc.equals("P") && computerc.equals("S")){
            return -1;
        }
        else{
            return 0;
        }
    }

    

}