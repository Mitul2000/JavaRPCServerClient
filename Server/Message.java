import java.io.Serializable;
//youtube link to the demo https://youtu.be/Xu8b1VmmILI
// must implement Serializable in order to be sent
public class Message implements Serializable{

    public String userID;
    public String name;
    public String action;
    public int money;
    public String message;

    public Message(String userId, String Name, int Money, String Action, String Message) {
        this.userID = userId;
        this.name = Name;
        this.money = Money;
        this.message = Message;
        this.action = Action;
    }

    public String getText() {
        return userID + ", "+ name + " , "+ money + ", "+ action+ ", "+ message;
    }
}