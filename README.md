# planaridade

Desenhe um grafo numa folha de papel sem que duas arestas se cruzem. Para muitos grafos dá.
Para o **K5** — cinco pontos, todos ligados a todos — não dá, e isso não é falta de
habilidade: é impossível, e a prova cabe em três linhas da fórmula de Euler.

Este repositório varre **todos** os grafos de até 6 vértices e decide cada um. E decide de
duas maneiras que não se falam: uma pergunta se o grafo contém um dos dois padrões
proibidos; a outra simplesmente **tenta desenhar** e conta as faces do desenho.

```bash
mvn -B -ntp verify                              # 22 testes
mvn -B -ntp exec:java -Dexec.args="varredura"   # todos os grafos até 5 vértices
mvn -B -ntp exec:java -Dexec.args="proibidos"   # K5 e K3,3, e a minimalidade deles
mvn -B -ntp exec:java -Dexec.args="peneiras"    # quem decide sem enumerar nada
mvn -B -ntp exec:java -Dexec.args="juiz"        # menores x desenho
mvn -B -ntp exec:java -Dexec.args="arestas"     # onde a planaridade morre
mvn -B -ntp exec:java -Dexec.args="fundo"       # 6 vértices (job próprio)
```

Java 21, sem dependência fora de JUnit e AssertJ.

## Os dois caminhos

**Por menores proibidos.** O teorema de Wagner diz que um grafo é planar se, e somente se,
não contém K5 nem K3,3 como menor — isto é, não dá para chegar a um deles apagando
vértices, apagando arestas e contraindo arestas. Procurar isso é procurar cinco (ou seis)
grupos de vértices, disjuntos, conexos por dentro e ligados entre si.

**Por desenho.** Um desenho sem cruzamentos fica inteiramente descrito pela ordem cíclica
em que as arestas saem de cada vértice. Dessa descrição saem as faces: entre por uma
aresta, chegue do outro lado, saia pela próxima da rotação, repita até fechar. Com V, E e
F na mão, Euler decide: cabe no plano quando alguma escolha de rotações dá **V − E + F =
2**.

O segundo não sabe o que é K5. É por isso que ele serve de juiz.

```
menores x desenho:
   2 vértices:     1 grafo conexo, igual
   3 vértices:     4 grafos conexos, iguais
   4 vértices:    38 grafos conexos, iguais
   5 vértices:   728 grafos conexos, iguais
                 ---
                 771 conferidos, zero divergências
```

## Todos os grafos, um por um

```
n   grafos   planares   fatia       maior E planar   3n−6
1   1        1          100,00%     0                0
2   2        2          100,00%     1                0
3   8        8          100,00%     3                3
4   64       64         100,00%     6                6
5   1024     1023        99,90%     9                9
6   32768    32071       97,87%     12               12
```

A linha de 5 vértices é a minha favorita do acervo inteiro. De **1024 grafos, exatamente
um** não é planar, e esse um é o K5. Até quatro vértices não existe grafo não planar
nenhum, não importa como se ligue o que for.

A linha de 6 vértices sai da varredura de fundo, que leva 80 segundos e roda num job
separado — e que confere as próprias medidas antes de terminar, para o número aqui não
ficar solto.

## Onde a planaridade morre

```
arestas   grafos   planares   fatia
0         1        1          100,0%
...
8         45       45         100,0%
9         10       10         100,0%
10        1        0            0,0%
```

Com cinco vértices, a planaridade não vai se degradando conforme o grafo engorda. Ela vai
**100% até nove arestas e desaba para zero na décima**. A décima aresta é a que fecha o
K5, e não existe outro jeito de ter dez arestas com cinco vértices.

## Quem decide sem enumerar nada

Antes de procurar menor nenhum, duas cotas baratas resolvem a maioria. Num desenho planar
cada face tem pelo menos 3 lados e cada aresta encosta em 2 faces, então `2E ≥ 3F`; com
Euler sai `E ≤ 3V − 6`. Se o grafo não tem triângulo, cada face tem pelo menos 4 lados e a
cota aperta para `E ≤ 2V − 4`.

```
planar de cara (poucos vértices ou arestas)   638    62,30%
passou de 3n−6                                  1     0,10%
passou de 2n−4 (bipartido)                      0     0,00%
precisou enumerar menores                     385    37,60%
```

O `1` da segunda linha é o K5.

## Os dois proibidos

```
K5     V=5  E=10  planar=não  gênero=1  faces no melhor desenho=5
K3,3   V=6  E=9   planar=não  gênero=1  faces no melhor desenho=3
```

Nos dois, `V − E + F = 0`, que é a fórmula de Euler para o toro: eles não cabem na folha,
mas cabem numa rosquinha. E os dois são **mínimos**: tirar qualquer uma das 10 arestas do
K5 o torna planar, e o mesmo vale para as 9 do K3,3. Nenhum pedaço deles é proibido
sozinho.

## O que as medidas me corrigiram

**Achei que a cota `3V − 6` pegasse o K3,3.** Não pega: o K3,3 tem 6 vértices e 9 arestas,
e `3·6 − 6 = 12`. Ele passa folgado pela peneira famosa. Quem o pega é a cota do
bipartido, `2V − 4 = 8`, que só vale porque ele não tem triângulo nenhum. Eu tinha
escrito o teste de planaridade com uma peneira só e ele declarava o K3,3 planar — o juiz
do desenho acusou na primeira rodada.

**Esperei uma descida suave.** Imaginei a fatia de planares caindo devagar conforme as
arestas aumentam. Com cinco vértices ela fica em 100% até nove arestas e vira zero na
décima. Não há transição: há um penhasco.

**Esperei mais não planares com 5 vértices.** Com 1024 grafos na mesa, "um" me pareceu
baixo demais e fui conferir achando que a varredura estava com defeito. Está certa: para
ser não planar com cinco vértices é preciso ser o K5 inteiro, e só existe um jeito de
montar o K5.

**O juiz precisou de teto.** A enumeração de rotações custa o produto de `(grau − 1)!`.
No K6 isso passa de quatro milhões de desenhos, e a conferência pararia o CI. Hoje o
`Imersao` recusa o que não cabe no teto em vez de travar, e a varredura pula esses casos
declarando quantos conferiu — não "todos".

## Quatro achados

1. **De todos os 1024 grafos com cinco vértices, exatamente um não é planar.** Até quatro
   vértices, nenhum: não existe maneira de ligar quatro pontos que não caiba na folha.
2. As duas decisões independentes — padrão proibido e desenho com contagem de faces —
   concordaram em **771 grafos conexos**, sem uma divergência.
3. A planaridade com cinco vértices **não degrada, desaba**: 100% até nove arestas, 0% na
   décima.
4. A cota famosa `E ≤ 3V − 6` **não detecta o K3,3**, que passa com 9 arestas contra um
   limite de 12; quem o pega é a cota `E ≤ 2V − 4`, válida só para grafos sem triângulo.
