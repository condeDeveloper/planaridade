package br.com.conde.planaridade;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Os numeros do README, presos.
 *
 * <p>A varredura de 6 vertices leva mais de um minuto e nao cabe aqui: ela e
 * conferida dentro do proprio comando "fundo" do {@link Medidor}, que roda num
 * job separado do CI e quebra se a medida mudar.
 */
class MedidasTest {

    @Test
    @DisplayName("de todos os 1024 grafos com cinco vertices, exatamente um nao e planar")
    void comCincoVerticesSoOK5Escapa() {
        Varredura.Resumo r = Varredura.varrer(5);
        assertThat(r.grafos()).isEqualTo(1024);
        assertThat(r.planares()).isEqualTo(1023);
        assertThat(r.grafos() - r.planares()).isEqualTo(1);
        assertThat(r.fatiaPlanar()).isCloseTo(99.902, org.assertj.core.data.Offset.offset(0.001));
    }

    @Test
    @DisplayName("o unico nao planar de cinco vertices e o K5, com as dez arestas")
    void oUnicoEhOK5() {
        int naoPlanares = 0;
        int qual = -1;
        for (int m = 0; m < Grafo.quantosGrafos(5); m++) {
            if (!Menores.ehPlanar(Grafo.doNumero(5, m))) {
                naoPlanares++;
                qual = m;
            }
        }
        assertThat(naoPlanares).isEqualTo(1);
        Grafo g = Grafo.doNumero(5, qual);
        assertThat(g.quantasArestas()).isEqualTo(10);
        for (int v = 0; v < 5; v++) {
            assertThat(g.grau(v)).as("vertice %d", v).isEqualTo(4);
        }
    }

    @Test
    @DisplayName("ate quatro vertices, todos os 64 grafos sao planares")
    void ateQuatroTodosPlanares() {
        assertThat(Varredura.varrer(4).planares()).isEqualTo(64);
        assertThat(Varredura.varrer(4).grafos()).isEqualTo(64);
        assertThat(Varredura.varrer(3).planares()).isEqualTo(8);
        assertThat(Varredura.varrer(2).planares()).isEqualTo(2);
    }

    @Test
    @DisplayName("menores x desenho concordam em 771 grafos conexos")
    void juizConcorda() {
        int total = 0;
        for (int n = 2; n <= Medidor.VERTICES_JUIZ; n++) {
            total += Varredura.conferirContraImersao(n);
        }
        assertThat(total).isEqualTo(1 + 4 + 38 + 728);
        assertThat(total).isEqualTo(771);
    }

    @Test
    @DisplayName("a planaridade com cinco vertices morre de uma vez: 100% ate nove arestas, 0% em dez")
    void corteAbrupto() {
        int[][] tabela = Varredura.porArestas(5);
        for (int e = 0; e <= 9; e++) {
            assertThat(tabela[e][1]).as("%d arestas", e).isEqualTo(tabela[e][0]);
        }
        assertThat(tabela[10][0]).isEqualTo(1);
        assertThat(tabela[10][1]).isZero();
    }

    @Test
    @DisplayName("o maior planar de cinco vertices tem nove arestas, que e a cota 3n-6")
    void aCotaEhAlcancada() {
        Varredura.Resumo r = Varredura.varrer(5);
        assertThat(r.maiorArestaPlanar()).isEqualTo(9);
        assertThat(3 * 5 - 6).isEqualTo(9);
    }

    @Test
    @DisplayName("as peneiras resolvem 62,4% dos grafos de cinco vertices sem enumerar nada")
    void trabalhoDasPeneiras() {
        int[] conta = Varredura.trabalhoDasPeneiras(5);
        assertThat(conta[0]).isEqualTo(638);
        assertThat(conta[1]).isEqualTo(1);
        assertThat(conta[2]).isZero();
        assertThat(conta[3]).isEqualTo(385);
        assertThat(conta[0] + conta[1] + conta[2] + conta[3]).isEqualTo(1024);
        assertThat(100.0 * (conta[0] + conta[1]) / 1024)
                .isCloseTo(62.4, org.assertj.core.data.Offset.offset(0.1));
    }

    @Test
    @DisplayName("os dois proibidos tem genero 1 e faces 5 e 3")
    void osProibidos() {
        Imersao k5 = new Imersao(Grafo.completo(5));
        assertThat(k5.genero()).isEqualTo(1);
        assertThat(k5.maiorNumeroDeFaces()).isEqualTo(5);

        Imersao k33 = new Imersao(Grafo.bipartidoCompleto(3, 3));
        assertThat(k33.genero()).isEqualTo(1);
        assertThat(k33.maiorNumeroDeFaces()).isEqualTo(3);
    }
}
