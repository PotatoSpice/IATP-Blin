package evolution;

import impl.Point;
import interf.IPoint;
import interf.IUIConfiguration;
import maps.Maps;

import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import maps.Maps;
import viewer.PathViewer;

public class Cromossoma implements Comparable<vitor.Cromossoma> {

    public static IUIConfiguration conf;

    static {
        try {
            conf = Maps.getMap(6);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Conf dataConf = new Conf();

    private int maxTam = 5;
    private int minTam = 0;
    private int maxMap = 600;
    private int minMap = 0;

    private int colisionWeight = 10000;

    private double totaldist;

    protected int tam = numeroAleatorio(minTam,maxTam);
    public List<IPoint> points = new ArrayList<>();
    public List<Rectangle> rectangles;

    public Cromossoma() {
        points.add(conf.getStart());
        this.starting();
        points.add(conf.getEnd());
        rectangles = conf.getObstacles();
        totaldist = 0.0;
    }

    public Cromossoma(Cromossoma cromossoma) { //Para as mutações

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

    public Cromossoma mutate() {
        Cromossoma novo = new Cromossoma(this);
        Random random = new Random();


        return novo;
    }

    public void givePoints(){
        for(int ix=1; ix<this.points.size(); ix++){
            Point point = (Point) this.points.get(ix);
            Point prevPoint = (Point) this.points.get(ix-1);
            System.out.println(prevPoint.getX()+"; "+ point.getX()+"; "+prevPoint.getY()+"; "+ point.getY());
        }
    }

    public boolean colisionChecker(){

        boolean colflag = false;

        for(int ix=1; ix<this.points.size(); ix++){
            Point point = (Point) this.points.get(ix);
            Point prevPoint = (Point) this.points.get(ix-1);
            Line2D line2d = new Line2D.Double(prevPoint.getX(), point.getX(), prevPoint.getY(), point.getY());
            // System.out.println(prevPoint.getX()+"; "+ point.getX()+"; "+prevPoint.getY()+"; "+ point.getY());
            for(int jx=0; jx<this.rectangles.size(); jx++){
                boolean collided = line2d.intersects(this.rectangles.get(jx));
                if(collided){
                    colflag = true;
                }
                //System.out.println("Rectangle: "+jx+"; Touches: "+collided);
            }
        }
        return colflag;
    }

    public double getDistance(){
        totaldist = 0.0;
        for(int ix=1; ix<this.points.size(); ix++){
            Point point = (Point) this.points.get(ix);
            Point prevPoint = (Point) this.points.get(ix-1);
            totaldist = totaldist + Math.sqrt((point.getY() - prevPoint.getY()) * (point.getY() - prevPoint.getY()) + (point.getX() - prevPoint.getX()) * (point.getX() - prevPoint.getX()));
        }
        return totaldist;
    }

    public double getFitness(){
        double value = 0.0;

        if(colisionChecker()){
            value = colisionWeight;
        }

        value = value + getDistance();

        return value;
    }

    @Override
    public int compareTo(vitor.Cromossoma o) {
        if (o.getFitness() < this.getFitness())
            return 1;
        else if (o.getFitness() > this.getFitness())
            return -1;
        else return 0;
    }

    @Override
    public String toString() {
        return "Fitness: "+ getFitness()
                +"Pontos: ";
    }
}