package br.com.conde.planaridade;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MenoresTest {

    @Test
    @DisplayName("todo grafo de ate quatro vertices cabe no plano")
    void ateQuatroSempreCabe() {
        for (int n = 1; n <= 4; n++) {
            for (int m = 0; m < Grafo.quantosGrafos(n); m++) {
                assertThat(Menores.ehPlanar(Grafo.doNumero(n, m)))
                        .as("n = %d, grafo %d", n, m)
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("os dois proibidos sao reconhecidos, e um nao contem o outro")
    void osDoisProibidos() {
        Grafo k5 = Grafo.completo(5);
        Grafo k33 = Grafo.bipartidoCompleto(3, 3);

        assertThat(Menores.temMenorK5(k5)).isTrue();
        assertThat(Menores.temMenorK33(k33)).isTrue();
        assertThat(Menores.ehPlanar(k5)).isFalse();
        assertThat(Menores.ehPlanar(k33)).isFalse();

        // o K3,3 tem 9 arestas e nao tem K5 dentro
        assertThat(Menores.temMenorK5(k33)).isFalse();
    }

    @Test
    @DisplayName("sao minimos: tirar qualquer aresta faz os dois caberem no plano")
    void minimalidade() {
        for (Grafo proibido : new Grafo[] {Grafo.completo(5), Grafo.bipartidoCompleto(3, 3)}) {
            for (int[] e : proibido.arestas()) {
                Grafo menor = proibido.copia().desligar(e[0], e[1]);
                assertThat(Menores.ehPlanar(menor))
                        .as("tirando %d-%d", e[0], e[1])
                        .isTrue();
            }
        }
    }

    @Test
    @DisplayName("a peneira de 3n-6 pega o K5 e nao pega mais ninguem com 5 vertices")
    void peneiraDasArestas() {
        int[] conta = Varredura.trabalhoDasPeneiras(5);
        assertThat(conta[Menores.Peneira.DEMAIS_PARA_O_PLANO.ordinal()]).isEqualTo(1);
        assertThat(conta[Menores.Peneira.PLANAR_DE_CARA.ordinal()]).isEqualTo(638);
        assertThat(conta[Menores.Peneira.DEMAIS_PARA_BIPARTIDO.ordinal()]).isZero();
        assertThat(conta[Menores.Peneira.PRECISA_ENUMERAR.ordinal()]).isEqualTo(385);
    }

    @Test
    @DisplayName("o K3,3 nao e pego pela cota 3n-6, so pela cota do bipartido")
    void k33PassaPelaCotaGeral() {
        Grafo k33 = Grafo.bipartidoCompleto(3, 3);
        assertThat(k33.quantasArestas()).isEqualTo(9);
        assertThat(3 * k33.vertices() - 6).isEqualTo(12);
        assertThat(k33.quantasArestas()).isLessThanOrEqualTo(3 * k33.vertices() - 6);

        assertThat(k33.bipartido()).isTrue();
        assertThat(2 * k33.vertices() - 4).isEqualTo(8);
        assertThat(Menores.peneirar(k33)).isEqualTo(Menores.Peneira.DEMAIS_PARA_BIPARTIDO);
    }

    @Test
    @DisplayName("bipartido e reconhecido: ciclo par sim, ciclo impar nao")
    void bipartido() {
        assertThat(new Grafo(4).ligar(0, 1).ligar(1, 2).ligar(2, 3).ligar(3, 0).bipartido()).isTrue();
        assertThat(new Grafo(3).ligar(0, 1).ligar(1, 2).ligar(2, 0).bipartido()).isFalse();
        assertThat(Grafo.bipartidoCompleto(3, 3).bipartido()).isTrue();
        assertThat(Grafo.completo(5).bipartido()).isFalse();
    }
}
