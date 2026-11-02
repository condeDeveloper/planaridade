package br.com.conde.planaridade;

import java.util.ArrayList;
import java.util.List;

/**
 * O juiz: em vez de perguntar se existe um menor proibido, tenta DESENHAR o
 * grafo e conta as faces do desenho.
 *
 * <p>Um desenho sem cruzamentos fica completamente descrito pela ordem
 * ciclica em que as arestas saem de cada vertice. Essa colecao de ordens tem
 * nome, mapa combinatorio, e dela se extraem as faces: ande por uma aresta,
 * chegue do outro lado, pegue a proxima aresta no sentido da rotacao, repita.
 * O passeio fecha, e cada passeio fechado e uma face.
 *
 * <p>Com V, E e F na mao, a formula de Euler decide: um grafo conexo cabe no
 * plano se, e somente se, alguma escolha de rotacoes der
 * <b>V - E + F = 2</b>. Qualquer outra escolha da um numero menor de faces, e
 * o desenho precisaria de alcas para nao se cruzar.
 *
 * <p>Isto nao sabe o que e K5 nem K3,3. E so por isso que serve de juiz para o
 * {@link Menores}.
 */
public final class Imersao {

    /** Teto de combinacoes de rotacao: acima disso a enumeracao e recusada. */
    public static final long TETO_DE_COMBINACOES = 4_000_000L;

    private final Grafo g;
    private final List<int[]> arestas;
    private final int[][] dartsDe;

    public Imersao(Grafo g) {
        if (!g.conexo()) {
            throw new IllegalArgumentException("a imersao so roda em grafo conexo");
        }
        this.g = g;
        this.arestas = g.arestas();
        int n = g.vertices();
        List<List<Integer>> saem = new ArrayList<>();
        for (int v = 0; v < n; v++) {
            saem.add(new ArrayList<>());
        }
        for (int i = 0; i < arestas.size(); i++) {
            int[] e = arestas.get(i);
            saem.get(e[0]).add(2 * i);
            saem.get(e[1]).add(2 * i + 1);
        }
        this.dartsDe = new int[n][];
        for (int v = 0; v < n; v++) {
            dartsDe[v] = saem.get(v).stream().mapToInt(Integer::intValue).toArray();
        }
    }

    private int destino(int dart) {
        int[] e = arestas.get(dart / 2);
        return dart % 2 == 0 ? e[1] : e[0];
    }

    /** Quantas combinacoes de rotacao existem: o produto de (grau - 1)!. */
    public long combinacoes() {
        long total = 1;
        for (int v = 0; v < g.vertices(); v++) {
            int grau = dartsDe[v].length;
            for (int k = 2; k < grau; k++) {
                total *= k;
                if (total > TETO_DE_COMBINACOES) {
                    return Long.MAX_VALUE;
                }
            }
        }
        return total;
    }

    /**
     * O maior numero de faces que algum desenho consegue. Pela formula de
     * Euler, isso equivale ao menor genero de superficie que aguenta o grafo.
     */
    public int maiorNumeroDeFaces() {
        if (combinacoes() > TETO_DE_COMBINACOES) {
            throw new IllegalStateException("rotacoes demais para enumerar: " + combinacoes());
        }
        int n = g.vertices();
        int[][] rotacao = new int[n][];
        for (int v = 0; v < n; v++) {
            rotacao[v] = dartsDe[v].clone();
        }
        return melhor(rotacao, 0);
    }

    private int melhor(int[][] rotacao, int v) {
        if (v == g.vertices()) {
            return contarFaces(rotacao);
        }
        int grau = rotacao[v].length;
        if (grau <= 2) {
            return melhor(rotacao, v + 1);
        }
        // o primeiro dart fica fixo: girar a ordem toda nao muda o desenho
        return permutar(rotacao, v, 1, 0);
    }

    private int permutar(int[][] rotacao, int v, int i, int melhorAteAgora) {
        int grau = rotacao[v].length;
        if (i == grau) {
            return Math.max(melhorAteAgora, melhor(rotacao, v + 1));
        }
        int melhor = melhorAteAgora;
        for (int j = i; j < grau; j++) {
            trocar(rotacao[v], i, j);
            melhor = permutar(rotacao, v, i + 1, melhor);
            trocar(rotacao[v], i, j);
        }
        return melhor;
    }

    private static void trocar(int[] v, int a, int b) {
        int t = v[a];
        v[a] = v[b];
        v[b] = t;
    }

    /** Anda pelas faces do mapa e conta quantas sao. */
    int contarFaces(int[][] rotacao) {
        int totalDarts = 2 * arestas.size();
        int[] proximo = new int[totalDarts];
        for (int v = 0; v < g.vertices(); v++) {
            int[] r = rotacao[v];
            for (int i = 0; i < r.length; i++) {
                // chegando por r[i] invertido, sai-se pelo proximo da rotacao
                proximo[r[i] ^ 1] = r[(i + 1) % r.length];
            }
        }
        boolean[] visto = new boolean[totalDarts];
        int faces = 0;
        for (int d = 0; d < totalDarts; d++) {
            if (visto[d]) {
                continue;
            }
            faces++;
            int atual = d;
            do {
                visto[atual] = true;
                atual = proximo[atual];
            } while (atual != d);
        }
        return faces;
    }

    /** Planar quando o melhor desenho fecha a formula de Euler em 2. */
    public boolean ehPlanar() {
        int v = g.vertices();
        int e = arestas.size();
        if (e == 0) {
            return true;
        }
        return v - e + maiorNumeroDeFaces() == 2;
    }

    /** O genero da superficie mais simples que aguenta o grafo. Zero e o plano. */
    public int genero() {
        int v = g.vertices();
        int e = arestas.size();
        if (e == 0) {
            return 0;
        }
        return (2 - (v - e + maiorNumeroDeFaces())) / 2;
    }
}
