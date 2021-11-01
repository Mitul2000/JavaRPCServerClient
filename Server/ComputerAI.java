import java.util.ArrayList;
import java.util.Random;
//youtube link to the demo https://youtu.be/Xu8b1VmmILI
public class ComputerAI {

    String name;
    int money;
    ArrayList<String> players;

    public ComputerAI(){
        this.players = new ArrayList<>();
        this.money = 1000;
        this.name = "Albert Einstin";
    }

    public String move(){
        String[] arr={"Rock", "Paper", "Sissor"};
      	Random r=new Random();        
      	int randomNumber=r.nextInt(arr.length);
        return arr[randomNumber];
    }
}
