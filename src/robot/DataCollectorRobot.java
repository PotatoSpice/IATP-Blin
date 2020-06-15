package robot;

import evolution.AG;
import evolution.Cromossoma;
import impl.UIConfiguration;
import interf.IPoint;
import impl.Point;
import performance.EvaluateFire;
import robocode.*;
import utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static robocode.util.Utils.normalRelativeAngleDegrees;


public class DataCollectorRobot extends AdvancedRobot {

    private class Dados{
        String validationName;
        int hit = 0;
        double power;
        double distance;
        double bearing;
        double heading;
        double robotHeading;
        double gunHearing;
        int moving;

        public Dados(String validationName, double power, double distance, double bearing, double heading, double robotHeading, double gunHearing, int moving) {
            this.validationName = validationName;
            this.power = power;
            this.distance = distance;
            this.bearing = bearing;
            this.heading = heading;
            this.gunHearing= gunHearing;
            this.robotHeading = robotHeading;
            this.moving = moving;
        }
        public String toString(){
            return validationName + ";" + hit+ ";"+power +";"+distance+";"+bearing+";"+heading+";"+robotHeading+";"+gunHearing+";"+moving;
        }

    }

    HashMap<Bullet, Dados> balasLancadas = new HashMap<>();

    private List<Rectangle> obstacles;
    public static UIConfiguration conf;
    private List<IPoint> points;
    private HashMap<String, Rectangle> inimigos; //utilizada par associar inimigos a retângulos e permitir remover retângulos de inimigos já desatualizados
    private double turn;
    private int count;
    private MouseEvent mouseEvent = null;
    private int moveDirection = 1;
    //variável que contém o ponto atual para o qual o robot se está a dirigir
    private int currentPoint = -1;

    @Override
    public void run() {
        super.run();
        System.out.println("BLIN MACHINE IS RAD");
        obstacles = new ArrayList<>();
        inimigos = new HashMap<>();
        conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight() , obstacles);
        turn = 0;
        count = 0;
        while(true){
            if (currentPoint >= 0) {
                IPoint ponto = points.get(currentPoint);
                //se já está no ponto ou lá perto...
                if (Utils.getDistance(this, ponto.getX(), ponto.getY()) < 2){
                    currentPoint++;
                    //se chegou ao fim do caminho
                    if (currentPoint >= points.size()) {
                        currentPoint = -1;
                        count = 0;
                    }
                }
                advancedRobotGoTo(this, ponto.getX(), ponto.getY());

            }else{
                if(count==0) {
                    turnGunRight(360);
                    count++;
                }
            }
            this.execute();
        }
    }

    /**
     * ******** TODO: Necessário selecionar a opção Paint na consola do Robot *******
     * @param g
     */
    @Override
    public void onPaint(Graphics2D g) {
        super.onPaint(g);
        g.setColor(Color.RED);
        obstacles.stream().forEach(x -> g.drawRect(x.x, x.y, (int) x.getWidth(), (int) x.getHeight()));

        if (points != null)
        {
            for (int i=1;i<points.size();i++)
                drawThickLine(g, points.get(i-1).getX(), points.get(i-1).getY(), points.get(i).getX(), points.get(i).getY(), 2, Color.green);
        }
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        double gunTurnAmt = normalRelativeAngleDegrees(event.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        Bullet b;
        if(event.getDistance()<150)
            b = this.fireBullet(5);
        else if (event.getDistance()<270)
            b = this.fireBullet(3);
        else
            b = this.fireBullet(2);

        if(b == null)
            System.out.println("Não disparei");
        else {
            System.out.println("Disparei ao " + event.getName());

            double re = this.getDistanceRemaining();
            int moving;
            if(re==0){
                moving = 0;
            }else{
                moving = 1;
            }

            balasLancadas.put(b, new Dados(event.getName(), b.getPower(), event.getDistance(), event.getBearing(), event.getHeading(), this.getHeading(), this.getGunHeading(), moving));
        }

        System.out.println("Enemy spotted: "+event.getName());

        Point2D.Double ponto = getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        ponto.x -= this.getWidth()*2.5 / 2;
        ponto.y -= this.getHeight()*2.5 / 2;

        Rectangle rect = new Rectangle((int)ponto.x, (int)ponto.y, (int)(this.getWidth()*2.5), (int)(this.getHeight()*2.5));

        if (inimigos.containsKey(event.getName())) //se já existe um retângulo deste inimigo
            obstacles.remove(inimigos.get(event.getName()));//remover da lista de retângulos

        obstacles.add(rect);
        inimigos.put(event.getName(), rect);
        //System.out.println("Enemies at:");
        //obstacles.forEach(x -> System.out.println(x));
    }


    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);
        Rectangle rect = inimigos.get(event.getName());
        obstacles.remove(rect);
        inimigos.remove(event.getName());
    }

    /**
     * Devolve as coordenadas de um alvo
     *
     * @param robot o meu robot
     * @param bearing ângulo para o alvo, em graus
     * @param distance distância ao alvo
     * @return coordenadas do alvo
     * */
    public static Point2D.Double getEnemyCoordinates(DataCollectorRobot robot, double bearing, double distance){
        double angle = Math.toRadians((robot.getHeading() + bearing) % 360);

        return new Point2D.Double((robot.getX() + Math.sin(angle) * distance), (robot.getY() + Math.cos(angle) * distance));
    }

    @Override
    public void onMouseClicked(MouseEvent e) {
        super.onMouseClicked(e);
        mouseEvent = e;
        calculatePath(e);
    }

    private void calculatePath(MouseEvent e){
        IPoint startpoint = new Point((int) this.getX(), (int) this.getY());
        IPoint endpoint = new Point( e.getX(), e.getY());
        //System.out.println(startpoint+ "\n"+ endpoint);
        conf.setStart(startpoint);
        conf.setEnd(endpoint);
        AG geneticalgorithm = new AG(conf);
        Cromossoma best = geneticalgorithm.run();
        points = best.getPoints(); // Carrega os pontos da melhor solução encontrada
        System.out.println("Obstacles: "+obstacles);
        System.out.println("PONTOS: " + points);
        currentPoint = 0;
        this.execute();
    }


    @Override
    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        scan();
        Dados dados = balasLancadas.get(event.getBullet());
        dados.hit = 1;
        balasLancadas.replace(event.getBullet(), dados);
        System.out.println(dados);
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        super.onHitByBullet(e);
        setAhead(70*moveDirection);
        count = 0;
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);
        turnGunRight(360);
        moveDirection *= -1;
        setAhead(50*moveDirection);

    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        Dados dados = balasLancadas.get(event.getBullet());
        System.out.println(dados);
        scan();
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        super.onRoundEnded(event);
        dataToCSV();
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        super.onBattleEnded(event);
    }

    /**
     * Dirige o robot (AdvancedRobot) para determinadas coordenadas
     *
     * @param robot o meu robot
     * @param x coordenada x do alvo
     * @param y coordenada y do alvo
     * */
    public static void advancedRobotGoTo(AdvancedRobot robot, double x, double y)
    {
        x -= robot.getX();
        y -= robot.getY();

        double angleToTarget = Math.atan2(x, y);
        double targetAngle = robocode.util.Utils.normalRelativeAngle(angleToTarget - Math.toRadians(robot.getHeading()));
        double distance = Math.hypot(x, y);
        double turnAngle = Math.atan(Math.tan(targetAngle));
        //System.out.println(turnAngle);
        robot.setTurnRight(Math.toDegrees(turnAngle));
        if (targetAngle == turnAngle)
            robot.setAhead(distance);
        else
            robot.setBack(distance);

        robot.execute();
    }

    private void drawThickLine(Graphics g, int x1, int y1, int x2, int y2, int thickness, Color c) {

        g.setColor(c);
        int dX = x2 - x1;
        int dY = y2 - y1;

        double lineLength = Math.sqrt(dX * dX + dY * dY);

        double scale = (double) (thickness) / (2 * lineLength);

        double ddx = -scale * (double) dY;
        double ddy = scale * (double) dX;
        ddx += (ddx > 0) ? 0.5 : -0.5;
        ddy += (ddy > 0) ? 0.5 : -0.5;
        int dx = (int) ddx;
        int dy = (int) ddy;

        int xPoints[] = new int[4];
        int yPoints[] = new int[4];

        xPoints[0] = x1 + dx;
        yPoints[0] = y1 + dy;
        xPoints[1] = x1 - dx;
        yPoints[1] = y1 - dy;
        xPoints[2] = x2 - dx;
        yPoints[2] = y2 - dy;
        xPoints[3] = x2 + dx;
        yPoints[3] = y2 + dy;

        g.fillPolygon(xPoints, yPoints, 4);
    }

    public boolean dataToCSV(){
        try {
            FileWriter stats = new FileWriter("D://GeneralStuff//robotFiringData2.csv", true);
            BufferedWriter br = new BufferedWriter(stats);
            Iterator it = balasLancadas.entrySet().iterator();
            while(it.hasNext()){
                Map.Entry pair = (Map.Entry)it.next();
                Dados dados = balasLancadas.get(pair.getKey());
                br.write(dados.toString()+"\n");
            }
            br.close();
            stats.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

}