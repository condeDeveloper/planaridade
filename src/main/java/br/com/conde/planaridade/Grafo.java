package br.com.conde.planaridade;

import java.util.ArrayList;
import java.util.List;

/**
 * Grafo nao dirigido simples, guardado em matriz de adjacencia. Cada numero de
 * 0 a 2^C(n,2) - 1 corresponde a um grafo: e assim que a varredura passa por
 * TODOS os grafos de n vertices sem esquecer nenhum.
 */
public final class Grafo {

    private final int vertices;
    private final boolean[][] liga;

    public Grafo(int vertices) {
        if (vertices < 1) {
            throw new IllegalArgumentException("precisa de pelo menos um vertice");
        }
        this.vertices = vertices;
        this.liga = new boolean[vertices][vertices];
    }

    public int vertices() {
        return vertices;
    }

    public Grafo ligar(int a, int b) {
        if (a == b) {
            throw new IllegalArgumentException("laco nao entra: " + a);
        }
        liga[a][b] = true;
        liga[b][a] = true;
        return this;
    }

    public Grafo desligar(int a, int b) {
        liga[a][b] = false;
        liga[b][a] = false;
        return this;
    }

    public boolean temAresta(int a, int b) {
        return a != b && liga[a][b];
    }

    public int grau(int v) {
        int g = 0;
        for (int u = 0; u < vertices; u++) {
            if (temAresta(v, u)) {
                g++;
            }
        }
        return g;
    }

    public List<Integer> vizinhos(int v) {
        List<Integer> lista = new ArrayList<>();
        for (int u = 0; u < vertices; u++) {
            if (temAresta(v, u)) {
                lista.add(u);
            }
        }
        return lista;
    }

    public List<int[]> arestas() {
        List<int[]> lista = new ArrayList<>();
        for (int a = 0; a < vertices; a++) {
            for (int b = a + 1; b < vertices; b++) {
                if (liga[a][b]) {
                    lista.add(new int[] {a, b});
                }
            }
        }
        return lista;
    }

    public int quantasArestas() {
        return arestas().size();
    }

    public boolean conexo() {
        if (vertices == 1) {
            return true;
        }
        boolean[] visto = new boolean[vertices];
        int[] pilha = new int[vertices];
        int topo = 0;
        pilha[topo++] = 0;
        visto[0] = true;
        int contados = 1;
        while (topo > 0) {
            int v = pilha[--topo];
            for (int u = 0; u < vertices; u++) {
                if (temAresta(v, u) && !visto[u]) {
                    visto[u] = true;
                    contados++;
                    pilha[topo++] = u;
                }
            }
        }
        return contados == vertices;
    }

    /** Sem ciclo impar: condicao para o limite mais apertado de Euler valer. */
    public boolean bipartido() {
        int[] cor = new int[vertices];
        java.util.Arrays.fill(cor, -1);
        for (int inicio = 0; inicio < vertices; inicio++) {
            if (cor[inicio] != -1) {
                continue;
            }
            cor[inicio] = 0;
            List<Integer> fila = new ArrayList<>();
            fila.add(inicio);
            for (int i = 0; i < fila.size(); i++) {
                int v = fila.get(i);
                for (int u : vizinhos(v)) {
                    if (cor[u] == -1) {
                        cor[u] = 1 - cor[v];
                        fila.add(u);
                    } else if (cor[u] == cor[v]) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public Grafo copia() {
        Grafo g = new Grafo(vertices);
        for (int a = 0; a < vertices; a++) {
            System.arraycopy(liga[a], 0, g.liga[a], 0, vertices);
        }
        return g;
    }

    public static Grafo doNumero(int n, int mascara) {
        Grafo g = new Grafo(n);
        int bit = 0;
        for (int a = 0; a < n; a++) {
            for (int b = a + 1; b < n; b++) {
                if ((mascara & (1 << bit)) != 0) {
                    g.ligar(a, b);
                }
                bit++;
            }
        }
        return g;
    }

    public static int quantosGrafos(int n) {
        return 1 << (n * (n - 1) / 2);
    }

    /** O grafo completo com n vertices. */
    public static Grafo completo(int n) {
        Grafo g = new Grafo(n);
        for (int a = 0; a < n; a++) {
            for (int b = a + 1; b < n; b++) {
                g.ligar(a, b);
            }
        }
        return g;
    }

    /** O bipartido completo com a + b vertices. */
    public static Grafo bipartidoCompleto(int a, int b) {
        Grafo g = new Grafo(a + b);
        for (int i = 0; i < a; i++) {
            for (int j = 0; j < b; j++) {
                g.ligar(i, a + j);
            }
        }
        return g;
    }
}
