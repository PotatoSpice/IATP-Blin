package robot.enemytracking;

import robocode.ScannedRobotEvent;

/**
 * Classe utilizada no Robot Final para gestão do 'alvo atual' para o disparo do canhão.
 *
 * Isto é calculado tendo em conta os resultados obtidos pelo modelo ML treinado para o efeito.
 */
public class EnemyBot {
    private String name;
    private double bearing, heading, distance, energy, velocity;

    private int firePower;
    private boolean cannonFire;
    private double targetProb;

    public EnemyBot() {
        reset();
    }

    /**
     * Atualiza a flag do disparo contra o alvo
     *
     * @param power força da bala do canhão
     */
    public void updateCanonFire(int power) {
        this.firePower = power;
        this.cannonFire = power != -1;
    }

    /**
     * Atualiza o estado do alvo atual. Poderá ser um novo robot ou atualizar a informação do próprio.
     *
     * @param event informação sobre o robot descoberto
     * @param targetProb resultado do modelo ML
     */
    public void update(ScannedRobotEvent event, double targetProb) {
        this.name = event.getName();
        this.bearing = event.getBearing();
        this.heading = event.getHeading();
        this.distance = event.getDistance();
        this.energy = event.getEnergy();
        this.velocity = event.getVelocity();

        this.targetProb = targetProb;
    }

    /**
     * Apaga os dados do alvo atual. No fundo diz que não existe um alvo para o disparo.
     */
    public void reset() {
        this.name = "";
        this.bearing = 0.0;
        this.heading = 0.0;
        this.distance = 0.0;
        this.energy = 0.0;
        this.velocity = 0.0;

        this.cannonFire = false;
        this.targetProb = 0;
    }

    /**
     * Sinaliza a paragem do disparo.
     *
     * É provável que uma única chamada neste método não seja suficiente tendo em conta que o método update será chamado
     * várias vezes.
     */
    public void stopShooting() {
        this.cannonFire = false;
    }

    public String getName() { return this.name; }
    public double getBearing() { return this.bearing; }
    public double getHeading() { return this.heading; }
    public double getDistance() { return this.distance; }
    public double getEnergy() { return this.energy; }
    public double getVelocity() { return this.velocity; }

    public boolean exists() { return !name.equals(""); }
    public double getTargetProb() { return this.targetProb; }

    public int getFirePower() { return this.firePower; }
    public boolean isCanonFire() { return this.cannonFire; }
}