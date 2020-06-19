package robot.enemytracking;

import robocode.Robot;
import robocode.ScannedRobotEvent;

import java.awt.geom.Point2D;

/**
 * Classe utilizada no Robot Final para gestão do 'alvo atual' para o disparo do canhão.
 *
 * Isto é calculado tendo em conta os resultados obtidos pelo modelo ML treinado para o efeito.
 *
 * Esta classe é uma versão melhorada do "EnemyBot" que adiciona métodos para melhorar o targeting system do Robot Final.
 */
public class AdvancedEnemyBot extends EnemyBot {

    private double x, y;

    /**
     * Atualiza o estado do alvo atual. Poderá ser um novo robot ou atualizar a informação do próprio.
     *
     * Calcula as coordenadas X e Y do alvo.
     *
     * @param event informação sobre o robot descoberto
     * @param targetProb resultado do modelo ML
     * @param robot
     */
    public void update(ScannedRobotEvent event, double targetProb, Robot robot) {
        super.update(event, targetProb);

        double absBearingDeg = getBearing() + robot.getHeading();
        if (absBearingDeg < 0) absBearingDeg += 360;

        // 0 deg is North
        x = robot.getX() + Math.sin(Math.toRadians(absBearingDeg)) * event.getDistance();
        y = robot.getY() + Math.cos(Math.toRadians(absBearingDeg)) * event.getDistance();
    }

    /**
     * Retorna a possível coordenada futura do alvo tendo em conta a sua velocidade e a velocidade do disparo.
     *
     * @param when velocidade do disparo contra o alvo
     * @return coordenada resultante
     */
    public double getFutureX(long when) {
        return x + Math.sin(Math.toRadians(getHeading())) * getVelocity() * when;
    }

    /**
     * Retorna a possível coordenada futura do alvo tendo em conta a sua velocidade e a velocidade do disparo.
     *
     * @param when velocidade do disparo contra o alvo
     * @return coordenada resultante
     */
    public double getFutureY(long when) {
        return y + Math.cos(Math.toRadians(getHeading())) * getVelocity() * when;
    }

    /**
     * Calcula o 'absolute bearing' (ângulo) entre os dois pontos recebidos.
     *
     * @param x1 coordenada x do primeiro ponto
     * @param y1 coordenada y do primeiro ponto
     * @param x2 coordenada x do segundo ponto
     * @param y2 coordenada y do segundo ponto
     * @return angulo resultante entre os dois pontos
     */
    public static double absoluteBearing(double x1, double y1, double x2, double y2) {
        double xo = x2-x1;
        double yo = y2-y1;
        double hyp = Point2D.distance(x1, y1, x2, y2);
        double arcSin = Math.toDegrees(Math.asin(xo / hyp));
        double bearing = 0;

        if (xo > 0 && yo > 0) { // both pos: lower-Left
            bearing = arcSin;
        } else if (xo < 0 && yo > 0) { // x neg, y pos: lower-right
            bearing = 360 + arcSin; // arcsin is negative here, actuall 360 - ang
        } else if (xo > 0 && yo < 0) { // x pos, y neg: upper-left
            bearing = 180 - arcSin;
        } else if (xo < 0 && yo < 0) { // both neg: upper-right
            bearing = 180 - arcSin; // arcsin is negative here, actually 180 + ang
        }

        return bearing;
    }

    @Override
    public void reset() {
        super.reset();

        this.x = 0.0;
        this.y = 0.0;
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
