# IATP-Blin

<p>
Tendo em conta o enunciado do projeto,
<blockquote>
Considere a plataforma Robocode, em que tanques de guerra simulados combatem entre si de forma autónoma num campo de batalha bi-dimensional.
<br/>
O desenvolvimento de um robot autónomo que exiba um bom desempenho geral nesta plataforma implica a resolução de diversos problemas de complexidade moderada, incluindo a movimentação no campo de batalha, a gestão de energia, ou a lógica de disparo.
<br/><br/>
Neste trabalho prático serão abordados dois problemas principais:
<ol>
  <li>O problema da movimentação, utilizando Algoritmos Genéticos</li>
  <li>O problema do disparo, utilizando Machine Learning</li>
</ol>
<br/>
O objetivo principal deste trabalho é assim o desenvolvimento de um robot para a framework Robocode que se movimente de acordo com um algoritmo genético e dispare de acordo com um modelo de Machine Learning treinado previamente. 
</blockquote>
</p>

### TECNOLOGIA

- [Robocode](https://robocode.sourceforge.io/)

- [IntelliJ](https://www.jetbrains.com/idea/) - plataforma de desenvolvimento
- [Java](https://www.java.com/) - LP utilizada para construção dos Robots e Algoritmo Genético
- [H2O](https://www.h2o.ai/) - plataforma para treino dos modelos Machine Learning

## SETUP

- Instalar a plataforma [Robocode](https://robowiki.net/wiki/Robocode/Getting_Started)
- Adicionar os Robots desenvolvidos a um Mapa

*Note: o modelo Machine Learning só foi treinado para jogar contra alguns dos sample.Robots presentes no Robocode*

## REPOSITORY

O repositório contém as soluções para os Robots desenvolvidos tal como a utilização do Algoritmo Genético em mapas pre-definidos:

### maprunner
- Solução do Algoritmo Genético para execução em Mapas pre-definidos

### robot
- Contém o source code dos Robots desenvolvidos para o Robocode

### evolution
- Solução do Algoritmo Genético para execução no Robot

### samples
- Exemplos de algumas implementações.
