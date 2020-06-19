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

    private double targetProb;
    private int firePower;

    public EnemyBot() {
        reset();
    }

    /**
     * Partindo do resultado do modelo ML, verifica se o Robot deve disparar e com quanto dano.
     *
     * @param fireProb resultado do modelo ML
     */
    public void updateFirePower(double fireProb /*, int currNumEnemies*/) {
        this.targetProb = fireProb;

        // # tendo em conta o benchmarking realizado para este robot e querendo obter melhores classificações,
        //      é construido aqui uma "emenda" para a lógica do disparo. ## DEPRECATED
        //if (currNumEnemies == 1)
        //    if (fireProb >= 0.5)
        //        this.firePower = 10;
        //    else if (fireProb >= 0.3)
        //        if (getDistance() < 150)
        //            this.firePower = 5;
        //        else if (getDistance() < 270)
        //            this.firePower = 3;
        //        else
        //            this.firePower = 2;
        //    else if (fireProb >= 0.1)
        //        this.firePower = 2;
        //    else
        //        this.firePower = -1;
        //else
            // # decisão sobre o disparo
            if (fireProb >= 0.7)
                this.firePower = 10;
            else if (fireProb >= 0.5)
                if (getDistance() < 150)
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

        updateFirePower(targetProb);
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

        this.firePower = -1;
        this.targetProb = 0;
    }

    /**
     * Sinaliza a paragem do disparo.
     *
     * É provável que uma única chamada neste método não seja suficiente tendo em conta que o método update será chamado
     * várias vezes.
     */
    public void stopShooting() {
        this.firePower = -1;
    }

    public String getName() { return this.name; }
    public double getBearing() { return this.bearing; }
    public double getHeading() { return this.heading; }
    public double getDistance() { return this.distance; }
    public double getEnergy() { return this.energy; }
    public double getVelocity() { return this.velocity; }
    public boolean exists() { return !name.equals(""); }

    public int getFirepower() { return this.firePower; }
    public double getTargetProb() { return this.targetProb; }

}