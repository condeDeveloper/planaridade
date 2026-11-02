package br.com.conde.planaridade;

/**
 * Roda as tabelas do README.
 *
 * <pre>
 * mvn -B -ntp exec:java -Dexec.args="varredura"
 * mvn -B -ntp exec:java -Dexec.args="proibidos"
 * mvn -B -ntp exec:java -Dexec.args="peneiras"
 * mvn -B -ntp exec:java -Dexec.args="juiz"
 * mvn -B -ntp exec:java -Dexec.args="arestas"
 * mvn -B -ntp exec:java -Dexec.args="fundo"     (seis vertices, job proprio)
 * </pre>
 */
public final class Medidor {

    public static final int VERTICES = 5;
    public static final int VERTICES_JUIZ = 5;
    public static final int VERTICES_FUNDO = 6;

    public static void main(String[] args) {
        String comando = args.length > 0 ? args[0] : "tudo";
        switch (comando) {
            case "varredura" -> varredura();
            case "proibidos" -> proibidos();
            case "peneiras" -> peneiras();
            case "juiz" -> juiz();
            case "arestas" -> arestas();
            case "fundo" -> fundo();
            case "tudo" -> {
                varredura();
                proibidos();
                peneiras();
                juiz();
                arestas();
            }
            default -> {
                System.out.println("comando desconhecido: " + comando);
                System.exit(2);
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("-".repeat(texto.length()));
    }

    private static void varredura() {
        titulo("Todos os grafos de 1 a " + VERTICES + " vertices");
        System.out.printf("%-4s %-10s %-10s %-12s %-10s %s%n",
                "n", "grafos", "planares", "fatia", "max E", "3n-6");
        for (int n = 1; n <= VERTICES; n++) {
            Varredura.Resumo r = Varredura.varrer(n);
            System.out.printf("%-4d %-10d %-10d %-12s %-10d %d%n",
                    n, r.grafos(), r.planares(),
                    String.format("%.2f%%", r.fatiaPlanar()),
                    r.maiorArestaPlanar(), Math.max(0, 3 * n - 6));
        }
        System.out.println();
        System.out.println("\"max E\" = o maior numero de arestas que algum planar alcancou.");
    }

    private static void proibidos() {
        titulo("Os dois grafos proibidos, e o que acontece ao tirar uma aresta");
        Grafo k5 = Grafo.completo(5);
        Grafo k33 = Grafo.bipartidoCompleto(3, 3);

        mostrar("K5   ", k5);
        mostrar("K3,3 ", k33);

        System.out.println();
        System.out.println("Tirando UMA aresta qualquer:");
        System.out.printf("   K5   vira planar em %d das %d arestas%n",
                planarAoTirarUma(k5), k5.quantasArestas());
        System.out.printf("   K3,3 vira planar em %d das %d arestas%n",
                planarAoTirarUma(k33), k33.quantasArestas());
        System.out.println("   (sao minimos: nenhum pedaco deles e proibido sozinho)");
    }

    private static void mostrar(String nome, Grafo g) {
        Imersao im = new Imersao(g);
        System.out.printf("%s V=%d  E=%d  planar=%-6s genero=%d  faces no melhor desenho=%d%n",
                nome, g.vertices(), g.quantasArestas(), Menores.ehPlanar(g),
                im.genero(), im.maiorNumeroDeFaces());
    }

    private static int planarAoTirarUma(Grafo g) {
        int quantas = 0;
        for (int[] e : g.arestas()) {
            Grafo menor = g.copia().desligar(e[0], e[1]);
            if (Menores.ehPlanar(menor)) {
                quantas++;
            }
        }
        return quantas;
    }

    private static void peneiras() {
        titulo("Quem decide a planaridade, em " + VERTICES + " vertices");
        int[] conta = Varredura.trabalhoDasPeneiras(VERTICES);
        String[] nomes = {
            "planar de cara", "passou de 3n-6", "passou de 2n-4 (bipartido)", "precisou enumerar"
        };
        int total = 0;
        for (int c : conta) {
            total += c;
        }
        for (int i = 0; i < conta.length; i++) {
            System.out.printf("%-30s %-8d %.2f%%%n", nomes[i], conta[i], 100.0 * conta[i] / total);
        }
        System.out.printf("%ntotal: %d grafos%n", total);
    }

    private static void juiz() {
        titulo("Menores x desenho, ate " + VERTICES_JUIZ + " vertices");
        int total = 0;
        for (int n = 2; n <= VERTICES_JUIZ; n++) {
            int quantos = Varredura.conferirContraImersao(n);
            total += quantos;
            System.out.printf("   %d vertices: %d grafos conexos conferidos, todos iguais%n", n, quantos);
        }
        System.out.printf("%ntotal conferido contra o desenho: %d%n", total);
    }

    private static void arestas() {
        titulo("Planares por numero de arestas, em " + VERTICES + " vertices");
        int[][] tabela = Varredura.porArestas(VERTICES);
        System.out.printf("%-10s %-10s %-10s %s%n", "arestas", "grafos", "planares", "fatia");
        for (int e = 0; e < tabela.length; e++) {
            if (tabela[e][0] == 0) {
                continue;
            }
            System.out.printf("%-10d %-10d %-10d %.1f%%%n",
                    e, tabela[e][0], tabela[e][1], 100.0 * tabela[e][1] / tabela[e][0]);
        }
    }

    private static void fundo() {
        titulo("Varredura com " + VERTICES_FUNDO + " vertices");
        long comeco = System.nanoTime();
        Varredura.Resumo r = Varredura.varrer(VERTICES_FUNDO);
        System.out.printf("grafos          : %d%n", r.grafos());
        System.out.printf("conexos         : %d%n", r.conexos());
        System.out.printf("planares        : %d (%.3f%%)%n", r.planares(), r.fatiaPlanar());
        System.out.printf("conexos planares: %d (%.3f%% dos conexos)%n",
                r.conexosPlanares(), r.fatiaPlanarEntreConexos());
        System.out.printf("maior E planar  : %d   (a cota 3n-6 vale %d)%n",
                r.maiorArestaPlanar(), 3 * VERTICES_FUNDO - 6);
        System.out.printf("planares na cota: %d%n", r.naCota());
        System.out.printf("%ntempo: %.1f s%n", (System.nanoTime() - comeco) / 1e9);

        // Esta varredura leva mais de um minuto e por isso nao cabe na bateria,
        // que roda na matriz inteira de sistemas. Para o numero do README nao
        // ficar solto, a conferencia acontece aqui mesmo: se a medida mudar, o
        // job de fundo quebra, exatamente como um teste quebraria.
        confere("grafos", r.grafos(), 32_768);
        confere("conexos", r.conexos(), 26_704);
        confere("planares", r.planares(), 32_071);
        confere("conexos planares", r.conexosPlanares(), 26_013);
        confere("maior E planar", r.maiorArestaPlanar(), 12);
        confere("planares na cota", r.naCota(), 195);
        System.out.println();
        System.out.println("as seis medidas do README bateram");
    }

    private static void confere(String nome, int medido, int esperado) {
        if (medido != esperado) {
            System.out.printf("MEDIDA MUDOU: %s era %d e veio %d%n", nome, esperado, medido);
            System.exit(1);
        }
    }
}
