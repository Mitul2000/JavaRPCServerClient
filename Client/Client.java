import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

//youtube link to the demo https://youtu.be/Xu8b1VmmILI

public class Client {
    
    //Client attributes 
    Boolean createdGame;
    ClientConnection cc;
    String uniqueID;
    String name;
    int wallet;
    Boolean inGame;
    Boolean isHost;
    Boolean isfirstTime;

    //main calls the client 
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        new Client();
       
    }

    //Client logic implentation
    //Connects to the server on the local host and port 7777
    //When client makes a connection, it runs the setup function
    //when the setup meathod is done, it will run the emitdata function
    public Client() throws IOException, ClassNotFoundException{
        isHost = false;
        isfirstTime = true;
        inGame = false;
        System.out.println("Connected!");
        Socket socket = new Socket("localhost", 7777);
        cc = new ClientConnection(socket, this);
        cc.start();

        Message usertask = Usersetup();
        cc.sendStringToServer(usertask);
        System.out.println("----UserSetup Complete---");
        emitData();

    }

    //emit data is responsbile for the client to start the game
    //it also insures that the user continues to play the game until the user decides to quit
    // printstatemnets guide the user into choseing the opiton
    //first if statment only runs once by the host to start the game and will not run again
    //the game make a move will continue to run
    public void emitData()throws IOException, ClassNotFoundException{
        Scanner scanner = new Scanner(System.in);
        while(true){

            if(inGame || createdGame){
                if(isHost && isfirstTime){
                    System.out.println("Start the game: [Y]");
                    String startGame = scanner.nextLine();
                    Message messages1 = new Message(uniqueID,name,wallet,"SGAME",startGame);
                    isfirstTime = false;
                    cc.sendStringToServer(messages1);
                }
                System.out.println("======GAMESTARTED SELECT YOUR OPTIONS=============");
                System.out.println("Make a move R, P, S : ");
                String input = scanner.nextLine();
                if (input.equals("quit")){
                    break;
                }
                Message messages = new Message(uniqueID,name,wallet,"PLAYERCHOICE",input);
                System.out.println("Sending messages to the ServerSocket");
                cc.sendStringToServer(messages);

            }else{
                System.out.println("Please Wait till Host Begains ....");
                String waiting = scanner.nextLine();
                System.out.println("Press Enter To Continue....");
            }

        }

        // cc.close();
    }
    
    //User first starts at the userstep function after server connection
    //this function sets up the user name, wallet, id 
    //It also finds out if a user wants to create a room or join
    //depending on what the uesr deciedes to create or setup the room
    //a message will be sent ot the server accordingly
    public Message Usersetup(){

        
        String randomString = usingMath();
        uniqueID = UUID.randomUUID().toString();

        Scanner myObj = new Scanner(System.in);

        System.out.println("-------Welcome To Multiplayer Rock/Paper/Sisssor-------");
        System.out.println("-------Play Againest Server-------");
        System.out.println("How much money would you like to sit with?  ");
        wallet=0;
        if(myObj.hasNextInt()) {
            wallet = myObj.nextInt();
        }
        System.out.println("What is your name? ");
        name = myObj.next();

        //function to either create or join a room        
        System.out.println("Create a Room with friends [C] or Join a room [Anykey]");
        String userChoice = myObj.next();
        userChoice.toUpperCase();
        if(userChoice.equals("C")){
            System.out.println("Your Room id is: "+ randomString);
            System.out.println("Share this code with your friends to join the same table");
            Message messageCreateRoom = new Message(uniqueID,name,wallet,"CREATEROOM",randomString);
            createdGame = true;
            isHost = true;
            return messageCreateRoom;
        }
        else{
            System.out.println("Input the room you would like to Join");
            String roomChoice = myObj.next();
            Message messageJoinRoom = new Message(uniqueID,name,wallet,"JOINROOM",roomChoice);
            createdGame = false;
            return messageJoinRoom;
        }
        
    }

    // the math function is used to generate a room id that is alpha numeric and returns a string
    //this code is taken from the source https://codippa.com/how-to-generate-random-string-in-java/
    static String usingMath() {
        String alphabetsInUpperCase = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String alphabetsInLowerCase = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        // create a super set of all characters
        String allCharacters = alphabetsInLowerCase + alphabetsInUpperCase + numbers;
        // initialize a string to hold result
        StringBuffer randomString = new StringBuffer();
        // loop for 10 times
        for (int i = 0; i < 10; i++) {
            // generate a random number between 0 and length of all characters
            int randomIndex = (int)(Math.random() * allCharacters.length());
            // retrieve character at index and add it to result
            randomString.append(allCharacters.charAt(randomIndex));
        }
        return randomString.toString();
    }
}