package br.com.conde.planaridade;

/** Varre TODOS os grafos de n vertices e separa quem cabe no plano. */
public final class Varredura {

    public record Resumo(
            int vertices,
            int grafos,
            int conexos,
            int planares,
            int conexosPlanares,
            int maiorArestaPlanar,
            int naCota) {

        public double fatiaPlanar() {
            return grafos == 0 ? 0 : 100.0 * planares / grafos;
        }

        public double fatiaPlanarEntreConexos() {
            return conexos == 0 ? 0 : 100.0 * conexosPlanares / conexos;
        }
    }

    private Varredura() {
    }

    public static Resumo varrer(int n) {
        int total = Grafo.quantosGrafos(n);
        int conexos = 0;
        int planares = 0;
        int conexosPlanares = 0;
        int maiorArestaPlanar = 0;
        int naCota = 0;
        int cota = 3 * n - 6;

        for (int m = 0; m < total; m++) {
            Grafo g = Grafo.doNumero(n, m);
            boolean planar = Menores.ehPlanar(g);
            boolean conexo = g.conexo();
            if (conexo) {
                conexos++;
            }
            if (planar) {
                planares++;
                if (conexo) {
                    conexosPlanares++;
                }
                int e = g.quantasArestas();
                maiorArestaPlanar = Math.max(maiorArestaPlanar, e);
                if (n >= 3 && e == cota) {
                    naCota++;
                }
            }
        }
        return new Resumo(n, total, conexos, planares, conexosPlanares, maiorArestaPlanar, naCota);
    }

    /**
     * Confere a decisao por menores contra a imersao que desenha o grafo, em
     * todo grafo conexo de n vertices cujas rotacoes caibam no teto.
     *
     * @return quantos grafos foram conferidos
     */
    public static int conferirContraImersao(int n) {
        int total = Grafo.quantosGrafos(n);
        int conferidos = 0;
        for (int m = 0; m < total; m++) {
            Grafo g = Grafo.doNumero(n, m);
            if (!g.conexo() || g.quantasArestas() == 0) {
                continue;
            }
            Imersao im = new Imersao(g);
            if (im.combinacoes() > Imersao.TETO_DE_COMBINACOES) {
                continue;
            }
            boolean porMenores = Menores.ehPlanar(g);
            boolean porDesenho = im.ehPlanar();
            if (porMenores != porDesenho) {
                throw new IllegalStateException(
                        "discordaram no grafo " + m + " de " + n + " vertices: menores diz "
                                + porMenores + ", desenho diz " + porDesenho);
            }
            conferidos++;
        }
        return conferidos;
    }

    /** Quantos grafos cada peneira resolve sem enumerar menor nenhum. */
    public static int[] trabalhoDasPeneiras(int n) {
        int[] conta = new int[Menores.Peneira.values().length];
        int total = Grafo.quantosGrafos(n);
        for (int m = 0; m < total; m++) {
            conta[Menores.peneirar(Grafo.doNumero(n, m)).ordinal()]++;
        }
        return conta;
    }

    /** Planares por numero de arestas, para ver onde a planaridade morre. */
    public static int[][] porArestas(int n) {
        int maximo = n * (n - 1) / 2;
        int[][] tabela = new int[maximo + 1][2];
        int total = Grafo.quantosGrafos(n);
        for (int m = 0; m < total; m++) {
            Grafo g = Grafo.doNumero(n, m);
            int e = g.quantasArestas();
            tabela[e][0]++;
            if (Menores.ehPlanar(g)) {
                tabela[e][1]++;
            }
        }
        return tabela;
    }
}
