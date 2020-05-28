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

public class Cromossoma implements Comparable<Cromossoma> {

    public static IUIConfiguration conf;

    public int map = 3;

    static {
        try {
            conf = Maps.getMap(3);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Conf dataConf = new Conf();

    private int maxTam = 5;
    private int minTam = 0;
    private int maxMap = 600;
    private int minMap = 0;
    private int colisionN = 0;

    private int colisionWeight = 30000;

    private double totaldist;

    protected int tam = numeroAleatorio(minTam, maxTam);
    public List<IPoint> points = new ArrayList<>();
    public List<Rectangle> rectangles;

    public Cromossoma() {
        rectangles = conf.getObstacles();
        points.add(conf.getStart());
        this.starting();
        points.add(conf.getEnd());
        totaldist = 0.0;
    }

    public Cromossoma(boolean child) { //Para as mutações
        rectangles = conf.getObstacles();
        totaldist = 0.0;
    }

    public void starting() {
        if (lineIntersects((Point) points.get(0), (Point) conf.getEnd())) {
            Point oldPoint = (Point) points.get(0);
            Point newPoint;
            int x;
            int y;
            do {
                do {
                    x = numeroAleatorio(minMap, maxMap);
                    y = numeroAleatorio(minMap, maxMap);
                    newPoint = new Point(x, y);
                } while (lineIntersects(oldPoint, newPoint));
                points.add(newPoint);
                oldPoint = newPoint;
                //System.out.println("X: " + x + "; Y: " + y);
            }while(lineIntersects(oldPoint, (Point) conf.getEnd()));
        }
    }

    public int numeroAleatorio(int min, int max) {

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    public void map() {
        PathViewer pv = new PathViewer(conf);
        pv.setFitness(9999);
        pv.setStringPath("test");
        pv.paintPath(points);
    }

    public Cromossoma mutate() {
        Cromossoma novo = new Cromossoma();
        novo.points.clear();

        for (int ix = 0; ix < points.size(); ix++) {
            novo.points.add(this.points.get(ix));
        }

        Random random = new Random();
        int newx;
        int newy;

        if (this.colisionChecker()) {
            if (points.size() <= 3) {
                /*newx = numeroAleatorio(minMap, maxMap);
                newy = numeroAleatorio(minMap, maxMap);
                int rand = 1 + random.nextInt((points.size()-2));
                novo.points.add(rand, new Point(newx, newy));
                novo.givePoints(); */
                //System.out.println(novo.points.get(novo.points.size()-2));
            } else {
                int rand;
                do {
                    rand = random.nextInt((points.size() - 2));
                    newx = numeroAleatorio(minMap, maxMap);
                    newy = numeroAleatorio(minMap, maxMap);
                    //System.out.println(newx + "; "+ newy);
                } while (newx <= 0 || newy <= 0 || newx >= 600 || newy >= 600 || rand == 0);
                Point newpoint = new Point(newx, newy);
                novo.points.set(rand, newpoint);
                //System.out.println(points.get(rand));
            }
        } else {
            if (points.size() <= 3) {
               /* newx = numeroAleatorio(minMap, maxMap);
                newy = numeroAleatorio(minMap, maxMap);
                int rand = 1 + random.nextInt((points.size()-2));
                novo.points.add(rand, new Point(newx, newy));
                novo.givePoints(); */
                //System.out.println(novo.points.get(novo.points.size()-2));
            } else {
                int rand;
                do {
                    rand = random.nextInt((points.size() - 1));
                    newx = points.get(rand).getX() + ThreadLocalRandom.current().nextInt((int) -Conf.mutation_rate, (int) Conf.mutation_rate);
                    newy = points.get(rand).getY() + ThreadLocalRandom.current().nextInt((int) -Conf.mutation_rate, (int) Conf.mutation_rate);
                } while (newx <= 0 || newy <= 0 || newx >= 600 || newy >= 600 || rand == 0);
                Point newpoint = new Point(newx, newy);
                novo.points.set(rand, newpoint);
                //System.out.println(points.get(rand));
            }
        }

       /* System.out.print("OLD:");
        this.givePoints();
        System.out.print("NEW:");
        novo.givePoints(); */

        return novo;
    }

    public Cromossoma[] cross(Cromossoma other) {

        Cromossoma filho1 = new Cromossoma(true);
        Cromossoma filho2 = new Cromossoma(true);

        filho1.points.clear();
        filho2.points.clear();

        filho1.points.add(conf.getStart());
        filho2.points.add(conf.getStart());


        int sizePai = this.points.size();
        int sizeMae = other.points.size();
        int minSize;
        if (sizeMae > sizePai) {
            minSize = sizePai;
        } else {
            minSize = sizeMae;
        }

        //logica filho1
        for (int i = 1; i < minSize - 1; i++) {
            int x = this.points.get(i).getX() + other.points.get(i).getX();
            int y = this.points.get(i).getY() + other.points.get(i).getY();
            filho1.points.add(new Point(x / 2, y / 2));
        }

        //logica filho 2
        for (int i = 1; i < minSize - 1; i++) {
            int x = this.points.get(i).getX() + other.points.get(i).getX();
            int y = this.points.get(i).getY() + other.points.get(i).getY();
            filho2.points.add(new Point(y / 2, x / 2));
        }

        filho1.points.add(conf.getEnd());
        filho2.points.add(conf.getEnd());


        Cromossoma[] novos = {filho1, filho2};

        return novos;
    }

    public String[] givePoints() {
        Point point;
        Point prevPoint;
        String[] lines = new String[points.size()];
        for (int ix = 1; ix < this.points.size(); ix++) {
            point = (Point) this.points.get(ix);
            prevPoint = (Point) this.points.get(ix - 1);
            lines[ix - 1] = (prevPoint.getX() + "; " + point.getX() + "; " + prevPoint.getY() + "; " + point.getY());
            System.out.println("Point " + ix + ": " + (prevPoint.getX() + "; " + point.getX() + "; " + prevPoint.getY() + "; " + point.getY()));
        }
        return lines;
    }

    public List<IPoint> getPoints() {
        return points;
    }

    public boolean colisionChecker() {
        boolean colflag = false;
        colisionN = 0;
        for (int ix = 1; ix < this.points.size(); ix++) {
            Point point = (Point) this.points.get(ix);
            Point prevPoint = (Point) this.points.get(ix - 1);
            Line2D line2d = new Line2D.Double(prevPoint.getX(), prevPoint.getY(), point.getX(), point.getY());
            // System.out.println(prevPoint.getX()+"; "+ point.getX()+"; "+prevPoint.getY()+"; "+ point.getY());
            for (int jx = 0; jx < this.rectangles.size(); jx++) {
                boolean collided = line2d.intersects(this.rectangles.get(jx));
                if (collided) {
                    colflag = true;
                    colisionN++;
                }
                //System.out.println("Rectangle: "+jx+"; Touches: "+collided);
            }
        }
        return colflag;
    }

    public boolean lineIntersects(Point point1, Point point2) {
        Line2D line2d = new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        for (int ix = 0; ix < this.rectangles.size(); ix++) {
            if (line2d.intersects(this.rectangles.get(ix)))
                return true;

        }
        return false;
    }

    public double getDistance() {
        totaldist = 0.0;
        for (int ix = 1; ix < this.points.size(); ix++) {
            Point point = (Point) this.points.get(ix);
            Point prevPoint = (Point) this.points.get(ix - 1);
            totaldist = totaldist + Math.sqrt((point.getY() - prevPoint.getY()) * (point.getY() - prevPoint.getY()) + (point.getX() - prevPoint.getX()) * (point.getX() - prevPoint.getX()));
        }
        return totaldist;
    }

    public double getFitness() {
        double value = 0.0;

        if (colisionChecker()) {
            value = colisionWeight * colisionN;
        }

        value = value + getDistance() + (100*points.size());

        return value;
    }

    @Override
    public int compareTo(Cromossoma o) {
        if (o.getFitness() < this.getFitness())
            return 1;
        else if (o.getFitness() > this.getFitness())
            return -1;
        else return 0;
    }

    @Override
    public String toString() {
        return "Fitness: " + getFitness()
                +"\nDistância: "+getDistance()
                + "\nPontos => ";
    }
}