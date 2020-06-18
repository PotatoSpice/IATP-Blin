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

public class UpgradedRobot extends AdvancedRobot {

    private class EnemyBot {
        private String name;
        private double bearing, heading, distance, energy, velocity;
        private boolean exists;

        private double targetProb;
        private int firePower;

        EnemyBot() {
            reset();
        }

        public void updateFirePower(double fireProb) {
            this.targetProb = fireProb;

            // # Decisão sobre o disparo
            switch (name) {
                case "sample.Walls":
                    if (fireProb >= 0.5)
                        this.firePower = 10;
                    else if(fireProb>=0.3)
                        if(getDistance() < 150)
                            this.firePower = 5;
                        else if (getDistance() < 270)
                            this.firePower = 3;
                        else
                            this.firePower = 2;
                    else if (fireProb >= 0.1)
                        this.firePower = 2;
                    else
                        this.firePower = -1;
                    break;
                default:
                    if (fireProb >= 0.7)
                        this.firePower = 10;
                    else if(fireProb>=0.5)
                        if(getDistance() < 150)
                            this.firePower = 5;
                        else if (getDistance() < 270)
                            this.firePower = 3;
                        else
                            this.firePower = 2;
                    else if (fireProb >= 0.3)
                        this.firePower = 2;
                    else
                        this.firePower = -1;
            }
        }

        public void update(ScannedRobotEvent event, double targetProb) {
            this.name = event.getName();
            this.bearing = event.getBearing();
            this.heading = event.getHeading();
            this.distance = event.getDistance();
            this.energy = event.getEnergy();
            this.velocity = event.getVelocity();
            this.exists = true;

            updateFirePower(targetProb);
        }

        public void reset() {
            this.name = "";
            this.bearing = 0.0;
            this.distance = 0.0;
            this.energy = 0.0;
            this.velocity = 0.0;
            this.exists = false;

            this.firePower = -1;
            this.targetProb = 0;
        }

        public void stopShooting() {
            this.firePower = -1;
        }

        public String getName() { return this.name; }
        public double getBearing() { return this.bearing; }
        public double getHeading() { return this.heading; }
        public double getDistance() { return this.distance; }
        public double getEnergy() { return this.energy; }
        public double getVelocity() { return this.velocity; }
        public boolean exists() { return this.exists; }

        public int getFirepower() { return this.firePower; }
        public double getTargetProb() { return this.targetProb; }

    }

    EvaluateFire ef; // leaderboard IA
    public static UIConfiguration conf; // robocode Map Config

    private List<Rectangle> obstacles;
    private List<IPoint> points;
    private HashMap<String, Rectangle> inimigos; // associar inimigos a retângulos e permitir a sua gestão
    private EnemyBot targetEnemy = new EnemyBot();

    private double turn;
    private int count;
    private boolean checkSurroundings = true, clearPath = false;
    private byte scanDirection = 1;
    private byte moveDirection = 1; // direção do movimento do robot (positivo - direita; negativo - esquerda)
    private byte currentPoint = -1; // ponto atual para o qual o robot se está a dirigir
    private EasyPredictModelWrapper model;

    @Override
    public void run() {
        super.run();
        try {
            model = new EasyPredictModelWrapper(MojoModel.load("IA_ML_models/nfolds6.zip"));

            setBodyColor(Color.red);
            setGunColor(Color.yellow);
            setRadarColor(Color.white);
            setBulletColor(Color.red);
            setScanColor(Color.white);

            System.out.println("BLIN MACHINE IS RAD");
            ef = new EvaluateFire("Blin Machine");

            obstacles = new ArrayList<>();
            inimigos = new HashMap<>();
            conf = new UIConfiguration((int) getBattleFieldWidth(), (int) getBattleFieldHeight() , obstacles);
            
            setAdjustGunForRobotTurn(true); // separar o movimento do canhao
            setAdjustRadarForRobotTurn(true); // separar o movimento do radar
            while(true) {
                // # colocar o robot em movimento no MouseClickEvent
                if (currentPoint >= 0) {
                    targetEnemy.stopShooting();
                    IPoint ponto = points.get(currentPoint);
                    //se já está no ponto ou lá perto...
                    if (Utils.getDistance(this, ponto.getX(), ponto.getY()) < 2){
                        currentPoint++;
                        //se chegou ao fim do caminho
                        if (currentPoint >= points.size()) {
                            currentPoint = -1;
                            checkSurroundings = true;
                            points.clear();
                        }
                    }
                    advancedRobotGoTo(this, ponto.getX(), ponto.getY());

                } // # robot está parado, faz scan do mapa uma vez
                else {
                    if(checkSurroundings) {
                        setTurnRadarRight(360);
                        // setTurnGunRight(360);
                        checkSurroundings = false;
                    }
                }
                
                // # disparar contra o inimigo
                if (targetEnemy.exists()) {
                    // rodar o canhão
                    /*double addedAngle = 0.0;
                    if (targetEnemy.getVelocity() > 0 && targetEnemy.getDistance() > 50) {
                        addedAngle = targetEnemy.getVelocity();
                        // enemy is going right
                        if (targetEnemy.getHeading() > 0 && targetEnemy.getHeading() < 180) {
                            // enemy is above us
                            if (targetEnemy.getBearing() > -90 && targetEnemy.getBearing() < 90)
                                addedAngle *= 1; // turn gun right a little bit
                            // enemy is below us
                            else
                                addedAngle *= -1; // turn gun left a little bit
                        } // enemy is going left
                        else {
                            // enemy is above us
                            if (targetEnemy.getBearing() > -90 && targetEnemy.getBearing() < 90)
                                addedAngle *= -1; // turn gun left a little bit
                                // enemy is below us
                            else
                                addedAngle *= 1; // turn gun right a little bit
                        }
                    } */
                    double gunTurnAmt =
                            normalRelativeAngleDegrees(/*addedAngle*/ + targetEnemy.getBearing() + (getHeading() - getGunHeading()));
                    setTurnGunRight(gunTurnAmt);

                    // disparar!
                    if (targetEnemy.firePower != -1) {
                        setFireBullet(targetEnemy.firePower);
                        // System.out.println("# BULLET: Disparei ao " + targetEnemy.getName());
                    } else {
                        // System.out.println("# BULLET: Não disparei");
                    }
                }
                
                // # Executar os comandos planeados
                execute();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        // # guardar o resultado do modelo ML sobre a viabilidade do disparo ao inimigo
        Double value = evaluateBulletFire(event);
        // System.out.println("ModelPrediction: " + value + ", for " + event.getName());

        // # atualizar posição e disparo sobre o inimigo quando
        //      ainda não existe nenhum OU o valor do modelo ML é maior que o atual OU scannedRobot é o inimigo atual
        if (!targetEnemy.exists || value > targetEnemy.getTargetProb() || event.getName().equals(targetEnemy.getName())) {
            String temp = targetEnemy.getName();
            targetEnemy.update(event, value);
            if (!temp.equals(targetEnemy.getName()))
                System.out.println("# NewTarget: " + targetEnemy.getName());
        }

        // System.out.println("# Enemy spotted: " + event.getName());
        // # adicionar ou atualizar a identificação do inimigo encontrado
        Point2D.Double ponto = getEnemyCoordinates(this, event.getBearing(), event.getDistance());
        ponto.x -= this.getWidth()*2.5 / 2;
        ponto.y -= this.getHeight()*2.5 / 2;
        Rectangle rect = new Rectangle((int)ponto.x, (int)ponto.y, (int)(this.getWidth()*2.5), (int)(this.getHeight()*2.5));
        if (inimigos.containsKey(event.getName())) // se já existe um retângulo deste inimigo
            obstacles.remove(inimigos.get(event.getName())); // remover da lista de retângulos
        obstacles.add(rect);
        inimigos.put(event.getName(), rect);
        // System.out.println("Enemies at:");
        // obstacles.forEach(x -> System.out.println(x));

        // # atualizar direção do radar
        scanDirection *= -1; // changes value from 1 to -1
        setTurnRadarRight(360 * scanDirection);

        // # sinalizar scan ao mapa se o robot estiver parado
        checkSurroundings = true;
        ef.addScanned(event); // leaderboard IA

//        // # apontar canhão para o inimigo encontrado
//        double gunTurnAmt = normalRelativeAngleDegrees(event.getBearing() + (getHeading() - getRadarHeading()));
//        turnGunRight(gunTurnAmt);
//
//        Double value = evaluateBulletFire(event);
//
//        // # decisão sobre o disparo
//        Bullet b = null;
//        if (value >= 0.3) {
//            if (value >= 0.7)
//                b = setFireBullet(10);
//            else if(value>=0.5)
//                if(event.getDistance() < 150)
//                    b = setFireBullet(5);
//                else if (event.getDistance() < 270)
//                    b = setFireBullet(3);
//                else
//                    b = setFireBullet(2);
//            else if (value >= 0.3)
//                b = setFireBullet(2);
//        } else {
//            b = null;
//        }
//        // System.out.println("ModelPrediction: " + value + ", for " + event.getName());
//        if (b != null)
//            System.out.println("# BULLET: Disparei ao " + event.getName());
//        else
//            System.out.println("# BULLET: Não disparei");
    }

    /**
     * Verifica se dispara contra um inimigo utilizando o modelo Machine Learning
     *
     * @param event informação sobre um inimigo encontrado
     */
    private double evaluateBulletFire(ScannedRobotEvent event) {
        RowData data = new RowData();
        data.put("validationName", event.getName());
        data.put("distance", event.getDistance());
        data.put("bearing", event.getBearing());
        data.put("heading", event.getHeading());

        // # verificar se o robot está parado ou a mover-se
        double re = this.getDistanceRemaining();
        int moving;
        if(re==0)
            moving = 0;
        else
            moving = 1;

        data.put("moving", (double)moving);
        data.put("gunHeading", this.getGunHeading());
        data.put("robotHeading", this.getHeading());

        if(event.getDistance()<150)
            data.put("power", 5.0);
        else if (event.getDistance()<270)
            data.put("power", 3.0);
        else
            data.put("power", 2.0);

        // # utilizar modelo Machine Learning para a tomada de decisão do disparo
        double value = 0.0;
        try {
            RegressionModelPrediction prediction = model.predictRegression(data);
            value = prediction.value;
        } catch (PredictException e) {
            e.printStackTrace();
        }
        //System.out.println(value);
        return value;
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

    @Override
    public void onMouseClicked(MouseEvent event) {
        super.onMouseClicked(event);

        // clearAllEvents();
        // System.out.println("- MOUSE CLICKED -");
        calculatePath(event);
    }

    /**
     * Fazendo uso de um Algoritmo Genético, descobre um caminho até ao ponto escolhido, evitando todos os obstáculos.
     *
     * @param e informação sobre o mouseClickEvent
     */
    private void calculatePath(MouseEvent e){
        IPoint startpoint = new Point((int) this.getX(), (int) this.getY());
        IPoint endpoint = new Point( e.getX(), e.getY());
        conf.setStart(startpoint);
        conf.setEnd(endpoint);
        // # execução do algoritmo
        AG geneticalgorithm = new AG(conf);
        Cromossoma best = geneticalgorithm.run();
        // # carregar os pontos da melhor solução encontrada
        points = best.getPoints();
        // System.out.println("Obstacles: " + obstacles);
        // System.out.println("PONTOS: " + points);
        currentPoint = 0;
        this.execute();
    }

    @Override
    public void onRobotDeath(RobotDeathEvent event) {
        super.onRobotDeath(event);

        Rectangle rect = inimigos.get(event.getName());
        obstacles.remove(rect);
        inimigos.remove(event.getName());
        if (event.getName().equals(targetEnemy.getName()))
            targetEnemy.reset();
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);

        ef.addHit(event);
        // scan();
    }

    @Override
    public void onHitByBullet(HitByBulletEvent event) {
        super.onHitByBullet(event);

        // # reagir ao dano
        // setAhead(70 * moveDirection);
        // checkSurroundings = true;
        // scan();
    }

    @Override
    public void onHitWall(HitWallEvent event) {
        super.onHitWall(event);

        // # mudar de direção
        // turnGunRight(360);
        moveDirection *= -1;
        setAhead(90 * moveDirection);
    }

    @Override
    public void onHitRobot(HitRobotEvent event) {
        super.onHitRobot(event);

        // If he's in front of us, set back up a bit.
        if (event.getBearing() > -90 && event.getBearing() < 90) {
            setBack(100);
        } // else he's in back of us, so set ahead a bit.
        else {
            setAhead(100);
        }
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);

        // scan();
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
     * Devolve as coordenadas de um alvo
     *
     * @param upgradedRobot o meu robot
     * @param bearing ângulo para o alvo, em graus
     * @param distance distância ao alvo
     * @return coordenadas do alvo
     * */
    public static Point2D.Double getEnemyCoordinates(UpgradedRobot upgradedRobot, double bearing, double distance){
        double angle = Math.toRadians((upgradedRobot.getHeading() + bearing) % 360);

        return new Point2D.Double((upgradedRobot.getX() + Math.sin(angle) * distance), (upgradedRobot.getY() + Math.cos(angle) * distance));
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
