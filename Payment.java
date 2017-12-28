import java.util.ArrayList;
import java.util.HashMap;

public class Payment {

    private String name;
    private ArrayList<Account> accounts = new ArrayList<>();
    private HashMap<Account, Double> amounts = new HashMap<>();

    public double getAmountPayed(Account act){
        return amounts.get(act) == null ? 0 : amounts.get(act);
    }

    public void setAmountPayed(Account act, double amt){
        amounts.put(act, amt);
    }

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }

    public ArrayList<Account> getAccounts(){
        return accounts;
    }
}
