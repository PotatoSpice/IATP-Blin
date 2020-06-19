package maprunner;

public class Conf {
    static int pop_size = 50; // tamanho da população para cada geração
    static int pop_select = 25; // número de cromossomas selecionados para manter da geração anterior
    static double mutation_rate = 50; // taxa de mutação na geração de novos cromossomas
    static int generation_limit = 10000; // limite total de gerações possíveis
    static int convergence_limit = 25; // gerações em que o melhor fitness se poderá repetir para terminar o algoritmo
    static int cross_limit = 8; // total de cruzamentos por geração
    static int mutation_limit = 8; // total de mutações por geração
    static int random_limit = 1; // total de novos cromossomas aleatórios por geração
    static int map_size = 600;
}
