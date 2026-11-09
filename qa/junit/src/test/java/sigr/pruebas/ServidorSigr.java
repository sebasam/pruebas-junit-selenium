package sigr.pruebas;

import java.io.File;
import java.net.ServerSocket;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/** Levanta el SIGR (Node.js) con una base de datos limpia y en un puerto libre, y lo apaga al terminar. */
public final class ServidorSigr implements AutoCloseable {
    private final Process proceso;
    private final Path carpeta;
    public final String url;

    private ServidorSigr(Process proceso, Path carpeta, String url) {
        this.proceso = proceso;
        this.carpeta = carpeta;
        this.url = url;
    }

    public static ServidorSigr iniciar() throws Exception {
        File raiz = new File(System.getProperty("sigr.raiz", "../..")).getCanonicalFile();
        Path tmp = Files.createTempDirectory("sigr-junit");
        int puerto;
        try (ServerSocket s = new ServerSocket(0)) { puerto = s.getLocalPort(); }
        ProcessBuilder semilla = new ProcessBuilder("node", "src/seed.js").directory(raiz);
        semilla.environment().put("SIGR_DB", tmp.resolve("db.json").toString());
        semilla.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD);
        if (semilla.start().waitFor() != 0) throw new IllegalStateException("No se pudo crear la base inicial");
        ProcessBuilder pb = new ProcessBuilder("node", "src/server.js").directory(raiz);
        pb.environment().put("SIGR_DB", tmp.resolve("db.json").toString());
        pb.environment().put("PORT", String.valueOf(puerto));
        pb.redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD);
        Process p = pb.start();
        String url = "http://localhost:" + puerto;
        esperarServicio(url, p);
        return new ServidorSigr(p, tmp, url);
    }

    private static void esperarServicio(String url, Process p) throws Exception {
        HttpClient http = HttpClient.newHttpClient();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url + "/salud")).build();
        for (int i = 0; i < 80; i++) {
            if (!p.isAlive()) throw new IllegalStateException("El SIGR terminó al arrancar");
            try {
                if (http.send(req, BodyHandlers.ofString()).statusCode() == 200) return;
            } catch (Exception ignorada) { /* aún no escucha */ }
            Thread.sleep(100);
        }
        p.destroyForcibly();
        throw new IllegalStateException("El SIGR no respondió en 8 segundos");
    }

    @Override
    public void close() throws Exception {
        proceso.destroy();
        proceso.waitFor();
        try (Stream<Path> s = Files.walk(carpeta)) { s.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete()); }
    }
}
