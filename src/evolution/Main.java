package evolution;

public class Main {
    public  static void main (String args[]) {
        AG ag = new AG();
        Cromossoma best = ag.run();
        //best.map();
        System.out.println("Melhor solução encontrada:");
        System.out.println(best);
        best.givePoints();
    }
}
