package vitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class demo {
    public static void main(String args[]){

        ArrayList<Cromossoma> cromossomaList = new ArrayList<>();

        for(int ix=0; ix<=100000; ix++){
            Cromossoma test = new Cromossoma();
            cromossomaList.add(test);
        }

        Collections.sort(cromossomaList);

        //Imprimir as 3 melhores soluções
       cromossomaList.get(0).map();
       System.out.println(cromossomaList.get(0).toString());
       cromossomaList.get(0).getPoints();

    }
}
