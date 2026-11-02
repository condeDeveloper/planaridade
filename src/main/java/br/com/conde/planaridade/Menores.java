package br.com.conde.planaridade;

/**
 * Decide planaridade pelo teorema de Wagner: um grafo e planar se, e somente
 * se, nao tem K5 nem K3,3 como MENOR.
 *
 * <p>Menor e o que sobra depois de apagar vertices, apagar arestas e contrair
 * arestas. Procurar um menor K5 aqui e procurar cinco grupos de vertices,
 * dois a dois sem interseccao, cada grupo conexo por dentro, e cada par de
 * grupos ligado por pelo menos uma aresta. Cada grupo e um vertice do K5
 * depois de contrair tudo que esta dentro dele.
 *
 * <p>A enumeracao atribui cada vertice a um dos grupos ou a nenhum. E
 * exponencial no numero de vertices, e por isso vem depois de duas peneiras
 * baratas que resolvem a esmagadora maioria dos casos.
 */
public final class Menores {

    private Menores() {
    }

    /** Resultado da peneira barata, antes de qualquer enumeracao. */
    public enum Peneira {
        /** Poucos vertices ou poucas arestas: planar sem discussao. */
        PLANAR_DE_CARA,
        /** Passou de 3V - 6 arestas: nao cabe no plano, por Euler. */
        DEMAIS_PARA_O_PLANO,
        /** Bipartido e passou de 2V - 4: nao cabe, por Euler sem triangulo. */
        DEMAIS_PARA_BIPARTIDO,
        /** A peneira nao decide: precisa enumerar menores. */
        PRECISA_ENUMERAR
    }

    /**
     * As duas cotas de Euler, que decidem quase tudo sem enumerar nada.
     *
     * <p>Numa imersao planar de um grafo conexo com ciclo, cada face tem pelo
     * menos 3 lados e cada aresta encosta em 2 faces, entao 2E >= 3F. Juntando
     * com V - E + F = 2 sai E <= 3V - 6. Se o grafo nao tem triangulo, cada
     * face tem pelo menos 4 lados e a cota aperta para E <= 2V - 4.
     */
    public static Peneira peneirar(Grafo g) {
        int v = g.vertices();
        int e = g.quantasArestas();
        if (v <= 4) {
            return Peneira.PLANAR_DE_CARA;
        }
        if (e <= v) {
            // no maximo um ciclo por componente: sempre cabe no plano
            return Peneira.PLANAR_DE_CARA;
        }
        if (e > 3 * v - 6) {
            return Peneira.DEMAIS_PARA_O_PLANO;
        }
        if (g.bipartido() && e > 2 * v - 4) {
            return Peneira.DEMAIS_PARA_BIPARTIDO;
        }
        return Peneira.PRECISA_ENUMERAR;
    }

    public static boolean ehPlanar(Grafo g) {
        Peneira p = peneirar(g);
        return switch (p) {
            case PLANAR_DE_CARA -> true;
            case DEMAIS_PARA_O_PLANO, DEMAIS_PARA_BIPARTIDO -> false;
            case PRECISA_ENUMERAR -> !temMenorK5(g) && !temMenorK33(g);
        };
    }

    /** Cinco grupos conexos, dois a dois ligados. */
    public static boolean temMenorK5(Grafo g) {
        return procurar(g, 5, null);
    }

    /**
     * Seis grupos conexos: os tres primeiros de um lado, os tres ultimos do
     * outro, e todo par de lados opostos ligado. Dentro do mesmo lado nao se
     * exige nada.
     */
    public static boolean temMenorK33(Grafo g) {
        boolean[][] precisa = new boolean[6][6];
        for (int i = 0; i < 3; i++) {
            for (int j = 3; j < 6; j++) {
                precisa[i][j] = true;
                precisa[j][i] = true;
            }
        }
        return procurar(g, 6, precisa);
    }

    /**
     * @param grupos quantos grupos montar
     * @param precisa quais pares de grupos precisam estar ligados; null = todos
     */
    private static boolean procurar(Grafo g, int grupos, boolean[][] precisa) {
        int n = g.vertices();
        if (n < grupos) {
            return false;
        }
        int[] onde = new int[n];
        java.util.Arrays.fill(onde, -1);
        return atribuir(g, onde, 0, grupos, precisa);
    }

    private static boolean atribuir(Grafo g, int[] onde, int v, int grupos, boolean[][] precisa) {
        if (v == onde.length) {
            return valido(g, onde, grupos, precisa);
        }
        // o vertice v pode ficar de fora...
        onde[v] = -1;
        if (atribuir(g, onde, v + 1, grupos, precisa)) {
            return true;
        }
        // ...ou entrar em algum grupo. Para nao repetir a mesma particao com os
        // grupos trocados de nome, o vertice so entra num grupo novo se todos
        // os grupos de numero menor ja tiverem alguem.
        int maiorUsado = -1;
        for (int i = 0; i < v; i++) {
            maiorUsado = Math.max(maiorUsado, onde[i]);
        }
        int teto = precisa == null ? Math.min(maiorUsado + 1, grupos - 1) : grupos - 1;
        for (int grupo = 0; grupo <= teto; grupo++) {
            onde[v] = grupo;
            if (atribuir(g, onde, v + 1, grupos, precisa)) {
                return true;
            }
        }
        onde[v] = -1;
        return false;
    }

    private static boolean valido(Grafo g, int[] onde, int grupos, boolean[][] precisa) {
        int[] tamanho = new int[grupos];
        for (int x : onde) {
            if (x >= 0) {
                tamanho[x]++;
            }
        }
        for (int t : tamanho) {
            if (t == 0) {
                return false;
            }
        }
        for (int grupo = 0; grupo < grupos; grupo++) {
            if (!grupoConexo(g, onde, grupo)) {
                return false;
            }
        }
        for (int a = 0; a < grupos; a++) {
            for (int b = a + 1; b < grupos; b++) {
                if (precisa != null && !precisa[a][b]) {
                    continue;
                }
                if (!ligados(g, onde, a, b)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean grupoConexo(Grafo g, int[] onde, int grupo) {
        int n = onde.length;
        int inicio = -1;
        int quantos = 0;
        for (int v = 0; v < n; v++) {
            if (onde[v] == grupo) {
                quantos++;
                if (inicio < 0) {
                    inicio = v;
                }
            }
        }
        if (quantos <= 1) {
            return quantos == 1;
        }
        boolean[] visto = new boolean[n];
        int[] pilha = new int[n];
        int topo = 0;
        pilha[topo++] = inicio;
        visto[inicio] = true;
        int contados = 1;
        while (topo > 0) {
            int v = pilha[--topo];
            for (int u = 0; u < n; u++) {
                if (onde[u] == grupo && !visto[u] && g.temAresta(v, u)) {
                    visto[u] = true;
                    contados++;
                    pilha[topo++] = u;
                }
            }
        }
        return contados == quantos;
    }

    private static boolean ligados(Grafo g, int[] onde, int a, int b) {
        for (int u = 0; u < onde.length; u++) {
            if (onde[u] != a) {
                continue;
            }
            for (int w = 0; w < onde.length; w++) {
                if (onde[w] == b && g.temAresta(u, w)) {
                    return true;
                }
            }
        }
        return false;
    }
}
