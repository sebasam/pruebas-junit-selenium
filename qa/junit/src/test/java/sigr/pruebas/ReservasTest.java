package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/** Reservas: disponibilidad y la regla de que cada reserva ocupa la mesa dos horas. */
public class ReservasTest extends PruebaBase {

    private static String cuerpo(String cliente, int mesa, String fecha, String hora, int personas) {
        return "{\"cliente\":\"" + cliente + "\",\"mesa\":" + mesa + ",\"fecha\":\"" + fecha + "\",\"hora\":\"" + hora
                + "\",\"personas\":" + personas + "}";
    }

    @Test
    public void alInicioLasDiezMesasEstanLibres() {
        Respuesta r = api.get("/reservas/disponibilidad?fecha=2030-01-10&hora=20:00", null);
        assertEstado(200, r);
        assertEquals(10, r.lista("mesasLibres").size());
    }

    @Test
    public void reservaValidaOcupaLaMesa() {
        String f = "2030-02-10";
        Respuesta r = api.post("/reservas", cuerpo("Laura Gómez", 5, f, "20:00", 4), null);
        assertEstado(201, r);
        assertEquals("activa", r.texto("estado"));
        Respuesta d = api.get("/reservas/disponibilidad?fecha=" + f + "&hora=20:00", null);
        assertEquals(9, d.lista("mesasLibres").size());
        assertFalse("La mesa 5 ya no debe figurar como libre", d.lista("mesasLibres").contains(5.0));
    }

    @Test
    public void laMismaMesaYHoraSeRechaza() {
        String f = "2030-03-10";
        assertEstado(201, api.post("/reservas", cuerpo("Primero", 6, f, "20:00", 2), null));
        assertError(409, "ya esta reservada", api.post("/reservas", cuerpo("Segundo", 6, f, "20:00", 2), null));
    }

    @Test
    public void unaReservaUnaHoraDespuesSeSolapa() {
        String f = "2030-04-10";
        assertEstado(201, api.post("/reservas", cuerpo("Turno 1", 7, f, "20:00", 2), null));
        assertEstado(409, api.post("/reservas", cuerpo("Turno 2", 7, f, "21:00", 2), null));
    }

    @Test
    public void unaReservaJustoCuandoTerminanLasDosHorasSeAcepta() {
        String f = "2030-05-10";
        assertEstado(201, api.post("/reservas", cuerpo("Turno 1", 8, f, "20:00", 2), null));
        assertEstado(201, api.post("/reservas", cuerpo("Turno 2", 8, f, "22:00", 2), null));
    }

    @Test
    public void otraMesaEnElMismoHorarioSeAcepta() {
        String f = "2030-06-10";
        assertEstado(201, api.post("/reservas", cuerpo("A", 1, f, "20:00", 2), null));
        assertEstado(201, api.post("/reservas", cuerpo("B", 2, f, "20:00", 2), null));
    }

    @Test
    public void cancelarLiberaLaMesa() {
        String f = "2030-07-10";
        long id = api.post("/reservas", cuerpo("Cancelará", 9, f, "20:00", 2), null).entero("id");
        Respuesta c = api.delete("/reservas/" + id, null);
        assertEstado(200, c);
        assertEquals("cancelada", c.texto("estado"));
        assertEstado(201, api.post("/reservas", cuerpo("Nuevo", 9, f, "20:00", 2), null));
    }

    @Test
    public void cancelarUnaReservaInexistenteDa404() {
        assertEstado(404, api.delete("/reservas/99999", null));
    }

    @Test
    public void fechaYHoraConFormatoIncorrectoSeRechazan() {
        assertError(400, "AAAA-MM-DD", api.post("/reservas", cuerpo("Ana", 1, "24/12/2030", "20:00", 2), null));
        assertError(400, "HH:MM", api.post("/reservas", cuerpo("Ana", 1, "2030-12-24", "8pm", 2), null));
        assertError(400, "HH:MM", api.get("/reservas/disponibilidad?fecha=2030-12-24&hora=25:99", null));
    }

    @Test
    public void mesaFueraDeRangoSeRechaza() {
        assertError(400, "mesa debe estar entre", api.post("/reservas", cuerpo("Ana", 11, "2030-12-24", "20:00", 2), null));
        assertError(400, "mesa debe estar entre", api.post("/reservas", cuerpo("Ana", 0, "2030-12-24", "20:00", 2), null));
    }

    @Test
    public void elNombreDelClienteEsObligatorio() {
        assertError(400, "nombre del cliente", api.post("/reservas", cuerpo("", 1, "2030-12-24", "20:00", 2), null));
    }
}
