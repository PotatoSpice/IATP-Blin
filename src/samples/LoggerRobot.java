package samples;

import robocode.*;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

public class LoggerRobot extends AdvancedRobot {

    private class Dados{
        String nome;
        Double distancia;

        public Dados(String nome, Double distancia) {
            this.nome = nome;
            this.distancia = distancia;
        }
    }

    FileWriter fw;

    HashMap<Bullet, Dados> balasNoAr = new HashMap<>();

    @Override
    public void run()
    {
        super.run();

        try {
            fw = new FileWriter("log_robocode.txt");
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(true){
            setAhead(100);
            setTurnLeft(100);
            Random rand = new Random();
            setAllColors(new Color(rand.nextInt(3), rand.nextInt(3), rand.nextInt(3)));
            execute();
        }

    }

    @Override
    public void onScannedRobot(ScannedRobotEvent event) {
        super.onScannedRobot(event);

        try {
            fw.write("Enemy "+event.getName()+" spotted at "+utils.Utils.getEnemyCoordinates(this, event.getBearing(), event.getDistance())+"\n");
            Bullet b = fireBullet(3);

            if (b!=null){
                fw.write("Fire at will!!!\n");
                balasNoAr.put(b, new Dados(event.getName(), event.getDistance()));
            }
            else
                fw.write("Out of ammo...\n");



        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBulletHit(BulletHitEvent event) {
        super.onBulletHit(event);
        Dados d = balasNoAr.get(event.getBullet());
        try
        {
            //testar se acertei em quem era suposto
            if (event.getName().equals(event.getBullet().getVictim()))
                fw.write(d.nome+","+d.distancia+",acertei\n");
            else
                fw.write(d.nome+","+d.distancia+",falhei\n");

        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBulletMissed(BulletMissedEvent event) {
        super.onBulletMissed(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write(d.nome+","+d.distancia+",falhei\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBulletHitBullet(BulletHitBulletEvent event) {
        super.onBulletHitBullet(event);
        Dados d = balasNoAr.get(event.getBullet());
        try {
            fw.write(d.nome+","+d.distancia+",falhei\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
        balasNoAr.remove(event.getBullet());
    }

    @Override
    public void onBattleEnded(BattleEndedEvent event) {
        super.onBattleEnded(event);

        try {
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
