package sigr.pruebas;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;

/** Cliente HTTP de pruebas: un método por verbo, con token opcional. Solo usa el JDK. */
public final class ClienteApi {
    private final String base;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public ClienteApi(String base) { this.base = base; }

    public Respuesta get(String ruta, String token) { return enviar("GET", ruta, null, token); }
    public Respuesta post(String ruta, String json, String token) { return enviar("POST", ruta, json, token); }
    public Respuesta put(String ruta, String json, String token) { return enviar("PUT", ruta, json, token); }
    public Respuesta patch(String ruta, String json, String token) { return enviar("PATCH", ruta, json, token); }
    public Respuesta delete(String ruta, String token) { return enviar("DELETE", ruta, null, token); }

    private Respuesta enviar(String metodo, String ruta, String json, String token) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(base + ruta)).timeout(Duration.ofSeconds(5));
        if (json != null) b.header("Content-Type", "application/json");
        if (token != null) b.header("Authorization", "Bearer " + token);
        b.method(metodo, json == null ? BodyPublishers.noBody() : BodyPublishers.ofString(json));
        long ini = System.nanoTime();
        try {
            HttpResponse<String> r = http.send(b.build(), BodyHandlers.ofString());
            return new Respuesta(r.statusCode(), r.body(), (System.nanoTime() - ini) / 1_000_000);
        } catch (Exception e) {
            throw new IllegalStateException("Fallo de comunicación con " + metodo + " " + ruta + ": " + e, e);
        }
    }

    /** Inicia sesión y devuelve el token. Falla si las credenciales no sirven. */
    public String iniciarSesion(String email, String clave) {
        Respuesta r = post("/auth/login", "{\"email\":\"" + email + "\",\"clave\":\"" + clave + "\"}", null);
        if (r.estado != 200) throw new IllegalStateException("No se pudo iniciar sesión: " + r);
        return r.texto("token");
    }
}
