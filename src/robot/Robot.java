package robot;

import evolution.AG;
import evolution.Cromossoma;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.RegressionModelPrediction;
import impl.UIConfiguration;
import interf.IPoint;
import impl.Point;
import performance.EvaluateFire;
import robocode.*;
import utils.Utils;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static robocode.util.Utils.normalRelativeAngleDegrees;


public class Robot extends AdvancedRobot {

    EvaluateFire ef;

    private List<Rectangle> obstacles;
    public static UIConfiguration conf;
    private List<IPoint> points;
    private HashMap<String, Rectangle> inimigos; //utilizada par associar inimigos a retângulos e permitir remover retângulos de inimigos já desatualizados
    private double turn;
    private int count;
    private int moveDirection = 1;
    //variável que contém o ponto atual para o qual o robot se está a dirigir
    private int currentPoint = -1;
    private EasyPredictModelWrapper model;

    @Override
    public void run() {
        super.run();
        try {
            model = new EasyPredictModelWrapper(MojoModel.load("C:\\Users\\rodri\\OneDrive\\Documents\\IAStuff\\newModel.zip"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("BLIN MACHINE IS RAD");
        ef = new EvaluateFire("Blin Machine");
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

        RowData data = new RowData();
        data.put("validationName", event.getName());
        data.put("distance", event.getDistance());
        data.put("bearing", event.getBearing());
        data.put("heading", event.getHeading());

        double gunTurnAmt = normalRelativeAngleDegrees(event.getBearing() + (getHeading() - getRadarHeading()));
        turnGunRight(gunTurnAmt);
        Bullet b;
        double re = this.getDistanceRemaining();
        int moving;
        if(re==0){
            moving = 0;
        }else{
            moving = 1;
        }
        data.put("moving", (double)moving);
        data.put("gunHeading", this.getGunHeading());
        data.put("robotHeading", this.getHeading());
        if(event.getDistance()<150)
            data.put("power", 5.0);
        else if (event.getDistance()<270)
            data.put("power", 3.0);
        else
            data.put("power", 2.0);

        double value = 0.0;
        try {
            RegressionModelPrediction prediction = model.predictRegression(data);
            value = prediction.value;
        } catch (PredictException e) {
            e.printStackTrace();
        }

        //System.out.println(value);
        if(value >=0.7) b = this.fireBullet(10);
        else if(value>=0.5)
            if(event.getDistance()<150) b = this.fireBullet(5);
            else if (event.getDistance()<270) b = this.fireBullet(3);
            else b = this.fireBullet(2);
        else if (value>=0.3) b= this.fireBullet(2);
        else b = null;
        System.out.println(value);
      /*  if(b == null)
            System.out.println("Não disparei");
        else {
            System.out.println("Disparei ao " + event.getName());
        } */

        //System.out.println("Enemy spotted: "+event.getName());

        Point2D.Double ponto = getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        ponto.x -= this.getWidth()*2.5 / 2;
        ponto.y -= this.getHeight()*2.5 / 2;

        Rectangle rect = new Rectangle((int)ponto.x, (int)ponto.y, (int)(this.getWidth()*2.5), (int)(this.getHeight()*2.5));
        count = 0;
        /*if (event.getDistance() < 100) {
            if (event.getBearing() > -90 && event.getBearing() <= 90) {
                back(40);
            } else {
                ahead(40);
            }
        }*/

        if (inimigos.containsKey(event.getName())) //se já existe um retângulo deste inimigo
            obstacles.remove(inimigos.get(event.getName()));//remover da lista de retângulos

        obstacles.add(rect);
        inimigos.put(event.getName(), rect);
        ef.addScanned(event);
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
    public static Point2D.Double getEnemyCoordinates(Robot robot, double bearing, double distance){
        double angle = Math.toRadians((robot.getHeading() + bearing) % 360);

        return new Point2D.Double((robot.getX() + Math.sin(angle) * distance), (robot.getY() + Math.cos(angle) * distance));
    }

    @Override
    public void onMouseClicked(MouseEvent e) {
        super.onMouseClicked(e);
        clearAllEvents();
        calculatePath(e);
        scan();
    }

    private void calculatePath(MouseEvent e){
        IPoint startpoint = new Point((int) this.getX(), (int) this.getY());
        IPoint endpoint = new Point( e.getX(), e.getY());
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
        ef.addHit(event);
        scan();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent e) {
        super.onHitByBullet(e);
        setAhead(90*moveDirection);
        count = 0;
        scan();
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);
        turnGunRight(360);
        moveDirection *= -1;
        setAhead(90*moveDirection);

    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        scan();
    }

    @Override
    public void onRoundEnded(RoundEndedEvent event) {
        super.onRoundEnded(event);
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        super.onBattleEnded(event);
        //ef.submit(event.getResults());
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
}
