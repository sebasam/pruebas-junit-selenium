package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.BeforeClass;
import org.junit.Test;

/** Caja: reporte de ventas del día y cierre único. Prepara un pedido pagado en @BeforeClass. */
public class CajaTest extends PruebaBase {
    private static final String HOY = LocalDate.now(ZoneOffset.UTC).toString();

    @BeforeClass
    public static void registrarUnaVentaPagada() {
        long plato = crearPlato("Postre del día", 13000);
        long pedido = api.post("/pedidos", "{\"mesa\":2,\"items\":[{\"platoId\":" + plato + ",\"cantidad\":2}]}", tokenMesero).entero("id");
        for (String e : new String[] {"en_preparacion", "listo", "entregado", "pagado"}) {
            assertEstado(200, api.patch("/pedidos/" + pedido + "/estado", "{\"estado\":\"" + e + "\"}", tokenMesero));
        }
    }

    @Test
    public void elReporteExigeSesion() {
        assertEstado(401, api.get("/reportes/ventas?fecha=" + HOY, null));
    }

    @Test
    public void elReporteNoLoPuedeVerUnMesero() {
        assertEstado(403, api.get("/reportes/ventas?fecha=" + HOY, tokenMesero));
    }

    @Test
    public void elReporteReflejaLaVentaPagada() {
        Respuesta r = api.get("/reportes/ventas?fecha=" + HOY, tokenAdmin);
        assertEstado(200, r);
        assertEquals(1, r.entero("pedidos"));
        assertEquals(26000, r.entero("total"));
        assertEquals(2, r.entero("platosVendidos.Postre del día"));
    }

    @Test
    public void unDiaSinVentasDaCeros() {
        Respuesta r = api.get("/reportes/ventas?fecha=2020-01-01", tokenAdmin);
        assertEstado(200, r);
        assertEquals(0, r.entero("pedidos"));
        assertEquals(0, r.entero("total"));
    }

    @Test
    public void unaFechaMalEscritaSeRechaza() {
        assertError(400, "AAAA-MM-DD", api.get("/reportes/ventas?fecha=ayer", tokenAdmin));
    }

    @Test
    public void laCajaSoloSePuedeCerrarUnaVezPorFecha() {
        Respuesta primero = api.post("/caja/cierre", "{\"fecha\":\"" + HOY + "\"}", tokenAdmin);
        assertEstado(201, primero);
        assertEquals("Administrador", primero.texto("responsable"));
        assertEquals(26000, primero.entero("total"));
        assertError(409, "ya fue cerrada", api.post("/caja/cierre", "{\"fecha\":\"" + HOY + "\"}", tokenAdmin));
        assertTrue(primero.tiene("cerradoEn"));
    }
}
