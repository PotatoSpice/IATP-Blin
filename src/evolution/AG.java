package evolution;

import interf.IUIConfiguration;
import performance.Evaluate;

import java.awt.*;
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

    /**
     * Constructor com inicialização do mapa Robocode
     *
     * @param conf configurações do mapa
     */
    public AG (IUIConfiguration conf){
        Cromossoma.conf = conf;
    }

    /**
     * Construir uma população inicial de cromossomas para execução do algoritmo.
     *
     * @return lista com a população de cromossomas
     */
    public List<Cromossoma> init() {
        List<Cromossoma> gen1_func = Stream.generate(() -> new Cromossoma(true))
                .limit(Conf.pop_size)
                .collect(Collectors.toList());
        return gen1_func;
    }

    public Cromossoma run(){
        Cromossoma bestSolutionEver;
        List<Double> best_fitness = new ArrayList<>();
        List<Double> avg_fitness = new ArrayList<>();
        List<Cromossoma> pop;
        // inicializar a primeira população
        pop = init();
        // ordenar população por fitness ascendente (menor valor na primeira posição)
        Collections.sort(pop);
        // atualizar dados resultantes da execução
        bestSolutionEver = pop.get(0);
        best_fitness.add(pop.get(0).getFitness());
        avg_fitness.add(pop.stream().mapToDouble(x -> x.getFitness()).average().getAsDouble());

        while(iteration_counter < Conf.generation_limit && convergence_counter < Conf.convergence_limit) {
            //System.out.println("Iteration: "+iteration_counter);

            // selecionar melhores soluções
            List<Cromossoma> best = pop.stream().limit(Conf.pop_select).collect(Collectors.toList());

            List<Cromossoma> filhos = new ArrayList<>();
            // adicionar novos cromossomas por mutação
            for (int i = 0; i< Conf.mutation_limit; i++)
                filhos.add(getCopyOfRandomSolution(best).mutate());
            // adicionar novos cromossomas por cruzamento
            for(int i = 0; i< Conf.cross_limit; i++) {
                Cromossoma c1 = getCopyOfRandomSolution(best);
                Cromossoma c2 = getCopyOfRandomSolution(best);

                Cromossoma[] cr = c1.cross(c2);
                filhos.add(cr[0]);
                filhos.add(cr[1]);
            }
            // adicionar novos cromossomas aleatórios
            filhos.addAll(Stream.generate(() -> new Cromossoma(true))
                    .limit(Conf.random_limit)
                    .collect(Collectors.toList()));

            // nova geração é resultado de acrescentar os melhores aos filhos por cruzamento, mutação e aleatorios
            pop = new ArrayList<>();
            pop.addAll(best);
            pop.addAll(filhos);

            // ordenar população por fitness ascendente (menor valor na primeira posição)
            Collections.sort(pop);
            // atualizar dados resultantes da execução
            best_fitness.add(pop.get(0).getFitness());
            avg_fitness.add(pop.stream().mapToDouble(x -> x.getFitness()).average().getAsDouble()); //calcular fitness médio

            iteration_counter++;
            if (last_fitness_value == pop.get(0).getFitness()) {
                convergence_counter++;
            } // um novo melhor fitness foi encontrado!
            else {
                convergence_counter = 0;
                last_fitness_value = pop.get(0).getFitness();
                bestSolutionEver = pop.get(0);
                // evaluate.addSolution(bestSolutionEver.getPoints(), generationCounter);
            }
        }
        return bestSolutionEver;
    }

    /**
     * Retorna um elemento aleatório de uma lista.
     *
     * @param lista lista de elementos
     * @return elemento encontrado
     */
    public Cromossoma getCopyOfRandomSolution(List<Cromossoma> lista) {
        return lista.get(new Random().nextInt(lista.size()));
    }

    /**
     * Guarda os dados gerados pela execução algoritmo genético
     *
     * @param best_fitness lista com os melhores valores de fitness por geração
     * @param avg_fitness lista com os valores médios de fitness por geração
     * @return true no sucesso da operação, false no contrário
     */
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
}
