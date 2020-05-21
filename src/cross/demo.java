package cross;

import java.util.ArrayList;
import java.util.Collections;

public class demo {
    public static void main(String args[]){

        ArrayList<Cromossoma> cromossomaList = new ArrayList<>();

        for(int ix=0; ix<=10000; ix++){
            Cromossoma test = new Cromossoma();
            cromossomaList.add(test);
        }

        Collections.sort(cromossomaList);

        //Imprimir as 3 melhores soluções
        /*
       cromossomaList.get(0).map();
       System.out.println(cromossomaList.get(0).toString());
       cromossomaList.get(0).getPoints();
       */
        Cromossoma[] testCross =cromossomaList.get(0).cross1(cromossomaList.get(1));

        System.out.println("pai->"+cromossomaList.get(0));
        cromossomaList.get(0).getPoints();
        System.out.println("mae->"+cromossomaList.get(1));
        cromossomaList.get(1).getPoints();

        System.out.println("filho->"+testCross[1]);
        testCross[1].getPoints();
        testCross[1].map();



    }
}
