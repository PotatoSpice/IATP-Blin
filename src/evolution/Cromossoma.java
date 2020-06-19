package evolution;

import impl.Point;
import interf.IPoint;
import interf.IUIConfiguration;
import maps.Maps;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.image.CropImageFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import maps.Maps;
import viewer.PathViewer;

public class Cromossoma implements Comparable<Cromossoma> {

    public static IUIConfiguration conf;

    private final int maxMap = Conf.map_size, minMap = 0; // limites do mapa
    private int colisionN = 0; // contador de colisões
    private final int colisionWeight = 30000; // valor de peso no fitness para uma colisão

    private double totaldist;  // distância total do percurso
    public List<IPoint> points = new ArrayList<>(); // lista dos pontos no percurso (mudanças de trajetória)
    public List<Rectangle> rectangles; // lista de obstáculos no mapa

    /**
     * Nova instância de um Cromossoma. Poderá ser gerado um trajeto aleatoriamente ou baseado em mutação ou cruzamento.
     *
     * @param random true é gerado um trajeto aleatório, false não são inicializados nenhuns pontos para o trajeto
     *               (utilizado na construção de cromossomas baseados em cruzamento ou mutação)
     */
    public Cromossoma(boolean random) {
        rectangles = conf.getObstacles();
        totaldist = 0.0;
        if (random) { // # gerar pontos aleatórios do trajeto
            points.add(conf.getStart());
            this.starting();
            points.add(conf.getEnd());
        }
    }

    /**
     * Gera um trajeto aleatório para este cromossoma, se ainda não existir um.
     */
    public void starting() {
        if (lineIntersects((Point) points.get(0), (Point) conf.getEnd())) {
            Point oldPoint = (Point) points.get(0);
            Point newPoint;
            int x;
            int y;
            do { // gera vários pontos até que a linha descrita com o ponto final não colida com um obstáculo...
                do { // gera um novo ponto até que a linha descrita entre o antigo e o novo não colida com um obstáculo...
                    x = numeroAleatorio(minMap, maxMap);
                    y = numeroAleatorio(minMap, maxMap);
                    newPoint = new Point(x, y);
                } while (lineIntersects(oldPoint, newPoint));
                points.add(newPoint);
                oldPoint = newPoint;
            } while(lineIntersects(oldPoint, (Point) conf.getEnd()));
        }
    }

    /**
     * Número aleatório entre dois extremos.
     *
     * @param min extremo inferior
     * @param max extremo superior
     * @return valor aleatório obtido
     */
    public int numeroAleatorio(int min, int max) {

        Random rand = new Random();
        int randomNum = rand.nextInt((max - min) + 1) + min;

        return randomNum;
    }

    /**
     * Executa uma mutação sobre a informação deste cromossoma, gerando um novo cromossoma.
     *
     * @return novo cromossoma gerado por mutação
     */
    public Cromossoma mutate() {
        Cromossoma novo = new Cromossoma(false);

        for (int ix = 0; ix < points.size(); ix++) {
            novo.points.add(this.points.get(ix));
        }

        Random random = new Random();
        int newx, newy;

        if (this.colisionChecker()) {
            if (points.size() <= 3) {

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
        return novo;
    }

    /**
     * Executa um cruzamento entre este cromossoma e um outro passado por parâmetro gerando dois novos cromossomas.
     *
     * @param other cromossoma para cruzamento
     * @return array de duas posições com cada um dos cromossomas gerados
     */
    public Cromossoma[] cross(Cromossoma other) {

        Cromossoma filho1 = new Cromossoma(false);
        Cromossoma filho2 = new Cromossoma(false);

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

    /**
     * Lista de todos os pontos existentes no trajeto descrito por este cromossoma
     *
     * @return lista de IPoint
     */
    public List<IPoint> getPoints() {
        return points;
    }

    /**
     * Calcula o número de colisões ocorridas pelo trajeto descrito por este cromossoma
     *
     * @return true se houveram colisões, false no contrário
     */
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

    /**
     * Cria uma linha entre dois pontos e verifica se existem colisoes com obstaculos.
     *
     * @param point1 primeiro ponto
     * @param point2 segundo ponto
     * @return true se a linha descrita entre os pontos toca em qualquer um dos obstáculos
     */
    public boolean lineIntersects(Point point1, Point point2) {
        Line2D line2d = new Line2D.Double(point1.getX(), point1.getY(), point2.getX(), point2.getY());
        for (int ix = 0; ix < this.rectangles.size(); ix++) {
            if (line2d.intersects(this.rectangles.get(ix)))
                return true;

        }
        return false;
    }

    /**
     * Calcula a distância do trajeto descrito neste cromossoma
     *
     * @return valor da distância
     */
    public double getDistance() {
        totaldist = 0.0;
        for (int ix = 1; ix < this.points.size(); ix++) {
            Point point = (Point) this.points.get(ix);
            Point prevPoint = (Point) this.points.get(ix - 1);
            totaldist = totaldist + Math.sqrt((point.getY() - prevPoint.getY()) * (point.getY() - prevPoint.getY()) + (point.getX() - prevPoint.getX()) * (point.getX() - prevPoint.getX()));
        }
        return totaldist;
    }

    /**
     * Calcula o fitness deste cromossoma para a execução do algoritmo genético.
     *
     * @return valor do fitness para este cromossoma.
     */
    public double getFitness() {
        double value = 0.0;

        //Penalização por colisões
        if (colisionChecker()) {
            value = colisionWeight * colisionN;
        }

        //Penalização por distancia do trajeto
        value = value + getDistance();
        //Penalização por número de pontos/vértices do trajeto
        value = value + (100*points.size());

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