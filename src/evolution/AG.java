package evolution;

import interf.IUIConfiguration;
import performance.Evaluate;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AG {

    private double last_fitness_value = 0;
    private int convergence_counter = 0;
    private int iteration_counter = 0;
    private IUIConfiguration uiconf;
    public AG (IUIConfiguration conf){
        this.uiconf = conf;
    }

    public List<Cromossoma> init() {
        List<Cromossoma> gen1_func = Stream.generate(() -> new Cromossoma(uiconf))
                .limit(Conf.pop_size)
                .collect(Collectors.toList());
        return gen1_func;
    }

    public Cromossoma run(){
        Cromossoma bestSolutionEver;
        List<Double> best_fitness = new ArrayList<>();
        List<Double> avg_fitness = new ArrayList<>();
        int generationCounter = 0;
        List<Cromossoma> pop;
        pop = init();
        Collections.sort(pop);
        bestSolutionEver = pop.get(0);
        best_fitness.add(pop.get(0).getFitness());
        avg_fitness.add(pop.stream().mapToDouble(x -> x.getFitness()).average().getAsDouble());
        Evaluate evaluate =  new Evaluate(Conf.pop_size, 6, "Blin Machine");

        while(iteration_counter < Conf.generation_limit && convergence_counter < Conf.converence_limit) {

            //System.out.println("Iteration: "+iteration_counter);

            //selecionar melhores soluções
            List<Cromossoma> best = pop.stream().limit(Conf.pop_select).collect(Collectors.toList());

            List<Cromossoma> filhos = new ArrayList<>();
            //mutação
            for (int i = 0; i< Conf.mutation_limit; i++)
                filhos.add(getCopyOfRandomSolution(best).mutate());


            for(int i = 0; i< Conf.cross_limit; i++) {
                Cromossoma c1 = getCopyOfRandomSolution(best);
                Cromossoma c2 = getCopyOfRandomSolution(best);

                Cromossoma[] cr = c1.cross(c2);
                filhos.add(cr[0]);
                filhos.add(cr[1]);
            }

            //aleatórias

            filhos.addAll(Stream.generate(() -> new Cromossoma())
                    .limit(Conf.random_limit)
                    .collect(Collectors.toList()));

            //nova geração é resultado de acrescentar os melhores aos filhos por cruzamento e mutação
            pop = new ArrayList<>();
            pop.addAll(best);
            pop.addAll(filhos);

            Collections.sort(pop);
            best_fitness.add(pop.get(0).getFitness());
            avg_fitness.add(pop.stream().mapToDouble(x -> x.getFitness()).average().getAsDouble()); //calcular fitness médio

            iteration_counter++;
            if (last_fitness_value == pop.get(0).getFitness()) {
                convergence_counter++;
            }
            else {
                convergence_counter = 0;
                last_fitness_value = pop.get(0).getFitness();
                bestSolutionEver = pop.get(0);
                evaluate = recordClassification(evaluate, bestSolutionEver, generationCounter);
                //evaluate.addSolution(bestSolutionEver.getPoints(), generationCounter);
            }
            generationCounter++;
        }
        //submitClassification(evaluate);
         evaluate.submit();
        dataToCSV(best_fitness, avg_fitness);
    return bestSolutionEver;
    }

    public Cromossoma getCopyOfRandomSolution(List<Cromossoma> lista) {
        return lista.get(new Random().nextInt(lista.size()));
    }

    public boolean dataToCSV(List<Double> best_fitness, List<Double> avg_fitness){
        try {
            FileWriter stats = new FileWriter("rundata.csv");
            stats.write("best_fitness,avg_fitness\n");
            for (int i=0;i<best_fitness.size();i++)
                stats.write(best_fitness.get(i)+","+avg_fitness.get(i)+"\n");
            stats.close();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Evaluate recordClassification(Evaluate evaluate, Cromossoma bestCromossoma, int counter){
        evaluate.addSolution(bestCromossoma.getPoints(), counter);
        return evaluate;
    }

    public boolean submitClassification(Evaluate eval){
        return eval.submit();
    }
}
