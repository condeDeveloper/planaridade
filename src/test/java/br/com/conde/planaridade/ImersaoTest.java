package br.com.conde.planaridade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** O juiz que desenha o grafo, conferido contra casos que se sabe de cor. */
class ImersaoTest {

    @Test
    @DisplayName("o triangulo tem duas faces: dentro e fora")
    void triangulo() {
        Grafo g = new Grafo(3).ligar(0, 1).ligar(1, 2).ligar(2, 0);
        Imersao im = new Imersao(g);
        assertThat(im.maiorNumeroDeFaces()).isEqualTo(2);
        assertThat(3 - 3 + 2).isEqualTo(2);
        assertThat(im.ehPlanar()).isTrue();
        assertThat(im.genero()).isZero();
    }

    @Test
    @DisplayName("uma arvore tem uma face so: o lado de fora")
    void arvore() {
        Grafo caminho = new Grafo(4).ligar(0, 1).ligar(1, 2).ligar(2, 3);
        Imersao im = new Imersao(caminho);
        assertThat(im.maiorNumeroDeFaces()).isEqualTo(1);
        assertThat(im.ehPlanar()).isTrue();
    }

    @Test
    @DisplayName("o K4 cabe no plano com quatro faces")
    void k4() {
        Imersao im = new Imersao(Grafo.completo(4));
        assertThat(im.maiorNumeroDeFaces()).isEqualTo(4);
        assertThat(im.ehPlanar()).isTrue();
        assertThat(im.genero()).isZero();
    }

    @Test
    @DisplayName("o K5 nao cabe: o melhor desenho da 5 faces e genero 1")
    void k5NaoCabe() {
        Imersao im = new Imersao(Grafo.completo(5));
        assertThat(im.maiorNumeroDeFaces()).isEqualTo(5);
        assertThat(5 - 10 + 5).isZero();
        assertThat(im.ehPlanar()).isFalse();
        assertThat(im.genero()).isEqualTo(1);
    }

    @Test
    @DisplayName("o K3,3 nao cabe: tres faces e genero 1")
    void k33NaoCabe() {
        Imersao im = new Imersao(Grafo.bipartidoCompleto(3, 3));
        assertThat(im.maiorNumeroDeFaces()).isEqualTo(3);
        assertThat(im.ehPlanar()).isFalse();
        assertThat(im.genero()).isEqualTo(1);
    }

    @Test
    @DisplayName("a imersao recusa grafo desconexo em vez de inventar numero")
    void recusaDesconexo() {
        Grafo solto = new Grafo(4).ligar(0, 1).ligar(2, 3);
        assertThatThrownBy(() -> new Imersao(solto)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("a contagem de combinacoes e o produto de (grau - 1)!")
    void combinacoes() {
        // triangulo: todo grau 2, nenhuma escolha
        assertThat(new Imersao(new Grafo(3).ligar(0, 1).ligar(1, 2).ligar(2, 0)).combinacoes())
                .isEqualTo(1);
        // K4: quatro vertices de grau 3, 2! cada
        assertThat(new Imersao(Grafo.completo(4)).combinacoes()).isEqualTo(16);
        // K5: cinco vertices de grau 4, 3! cada
        assertThat(new Imersao(Grafo.completo(5)).combinacoes()).isEqualTo(7776);
    }

    @Test
    @DisplayName("em todo grafo conexo pequeno, Euler fecha com o genero medido")
    void eulerFechaSempre() {
        for (int n = 2; n <= 5; n++) {
            for (int m = 0; m < Grafo.quantosGrafos(n); m++) {
                Grafo g = Grafo.doNumero(n, m);
                if (!g.conexo() || g.quantasArestas() == 0) {
                    continue;
                }
                Imersao im = new Imersao(g);
                if (im.combinacoes() > Imersao.TETO_DE_COMBINACOES) {
                    continue;
                }
                int v = g.vertices();
                int e = g.quantasArestas();
                int f = im.maiorNumeroDeFaces();
                assertThat(v - e + f)
                        .as("n = %d, grafo %d", n, m)
                        .isEqualTo(2 - 2 * im.genero());
            }
        }
    }
}
