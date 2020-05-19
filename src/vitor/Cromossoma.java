package vitor;

import impl.Point;
import interf.IPoint;
import interf.IUIConfiguration;
import maps.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import maps.Maps;
import viewer.PathViewer;

public class Cromossoma implements Comparable<Cromossoma> {

    public static IUIConfiguration conf;

    static {
        try {
            conf = Maps.getMap(6);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int maxTam = 5;
    private int maxMap = 600;
    private int minMap = 0;

    protected int tam = numeroAleatorio(0,maxTam);
    public List<IPoint> points = new ArrayList<>(); ;

    public Cromossoma() {
        points.add(conf.getStart());
        this.starting();
        points.add(conf.getEnd());
    }

    public void starting(){
        for(int i =0;i<tam;i++){
            int x = numeroAleatorio(minMap,maxMap);
            int y = numeroAleatorio(minMap,maxMap);
            points.add(new Point(x,y));

        }
    }

    public int numeroAleatorio(int min, int max){

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public void map(){

        PathViewer pv = new PathViewer(conf);

        pv.setFitness(9999);
        pv.setStringPath("test");
        pv.paintPath(points);
    }

    @Override
    public int compareTo(Cromossoma o) {
        return 0;
    }
}
