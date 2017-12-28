public class Account {

    private String name;
    private double totalPayed = 0;
    private double totalOwed = 0;

    public String getName(){
        return name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String toString(){
        return name;
    }

    public double getTotalPayed(){
        return totalPayed;
    }

    public double getTotalOwed(){
        return totalOwed;
    }

    public void setTotalPayed(double payed){
        totalPayed = payed;
    }

    public void setTotalOwed(double owed){
        totalOwed = owed;
    }

}
