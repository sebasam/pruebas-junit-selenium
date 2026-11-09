package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import org.junit.Test;

/** Errores generales, tiempos de respuesta y contrato HTML de los formularios. */
public class RobustezYFormulariosTest extends PruebaBase {

    @Test
    public void saludDevuelveEstadoYVersion() {
        Respuesta r = api.get("/salud", null);
        assertEstado(200, r);
        assertEquals("ok", r.texto("estado"));
        assertTrue(r.texto("version").matches("\\d+\\.\\d+\\.\\d+"));
        assertTrue("Responde en menos de 500 ms (tardó " + r.milisegundos + ")", r.milisegundos < 500);
    }

    @Test
    public void unaRutaInexistenteDa404() {
        assertError(404, "Ruta no encontrada", api.get("/no-existe", null));
    }

    @Test
    public void unJsonMalFormadoDa400() throws Exception {
        HttpResponse<String> r = HttpClient.newHttpClient().send(
                HttpRequest.newBuilder(URI.create(servidor.url + "/auth/login"))
                        .header("Content-Type", "application/json")
                        .POST(BodyPublishers.ofString("{\"email\": ")).build(),
                BodyHandlers.ofString());
        assertEquals(400, r.statusCode());
        assertTrue(r.body().contains("JSON no valido"));
    }

    @Test
    public void elFormularioDeLoginTieneSusCampos() {
        Respuesta r = api.get("/ui/login.html", null);
        assertEstado(200, r);
        for (String id : new String[] {"id=\"email\"", "id=\"clave\"", "type=\"password\"", "id=\"enviar\"", "id=\"mensaje\""}) {
            assertTrue("Falta " + id + " en el formulario de login", r.cuerpo.contains(id));
        }
    }

    @Test
    public void elFormularioDeReservaTieneSusCampos() {
        Respuesta r = api.get("/ui/reserva.html", null);
        assertEstado(200, r);
        for (String id : new String[] {"id=\"cliente\"", "id=\"fecha\"", "id=\"hora\"", "id=\"personas\"", "id=\"mesa\"", "id=\"enviar\""}) {
            assertTrue("Falta " + id + " en el formulario de reserva", r.cuerpo.contains(id));
        }
    }

    @Test
    public void noSePuedenLeerArchivosFueraDeLaCarpetaPublica() {
        assertEstado(404, api.get("/ui/..%2f..%2fpackage.json", null));
        assertEstado(404, api.get("/ui/%2e%2e/src/server.js", null));
        assertEstado(404, api.get("/ui/no-existe.html", null));
    }
}
