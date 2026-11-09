package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;

/** Pedidos: cálculo del total y orden obligatorio de los estados. */
public class PedidosTest extends PruebaBase {
    private static long platoId;

    @BeforeClass
    public static void crearPlatoDePrueba() {
        platoId = crearPlato("Tres leches", 13000);
    }

    private Respuesta crearPedido(int cantidad) {
        return api.post("/pedidos", "{\"mesa\":3,\"items\":[{\"platoId\":" + platoId + ",\"cantidad\":" + cantidad + "}]}", tokenMesero);
    }

    private Respuesta cambiarEstado(long pedido, String estado) {
        return api.patch("/pedidos/" + pedido + "/estado", "{\"estado\":\"" + estado + "\"}", tokenMesero);
    }

    @Test
    public void elTotalEsPrecioPorCantidad() {
        Respuesta r = crearPedido(2);
        assertEstado(201, r);
        assertEquals(26000, r.entero("total"));
        assertEquals("recibido", r.texto("estado"));
    }

    @Test
    public void elTotalSumaVariosPlatos() {
        long otro = crearPlato("Flan", 9000);
        Respuesta r = api.post("/pedidos", "{\"mesa\":4,\"items\":[{\"platoId\":" + platoId + ",\"cantidad\":1},"
                + "{\"platoId\":" + otro + ",\"cantidad\":3}]}", tokenMesero);
        assertEstado(201, r);
        assertEquals(13000 + 3 * 9000, r.entero("total"));
    }

    @Test
    public void sinSesionNoSePuedeCrearPedido() {
        assertEstado(401, api.post("/pedidos", "{\"mesa\":3,\"items\":[]}", null));
    }

    @Test
    public void pedidoSinPlatosSeRechaza() {
        assertError(400, "al menos un plato", api.post("/pedidos", "{\"mesa\":3,\"items\":[]}", tokenMesero));
    }

    @Test
    public void cantidadCeroONegativaSeRechaza() {
        assertError(400, "entero positivo", crearPedido(0));
        assertError(400, "entero positivo", crearPedido(-1));
    }

    @Test
    public void platoInexistenteDa404() {
        assertEstado(404, api.post("/pedidos", "{\"mesa\":3,\"items\":[{\"platoId\":99999,\"cantidad\":1}]}", tokenMesero));
    }

    @Test
    public void elPedidoRecorreTodosLosEstadosEnOrden() {
        long id = crearPedido(1).entero("id");
        Respuesta r = null;
        for (String estado : new String[] {"en_preparacion", "listo", "entregado", "pagado"}) {
            r = cambiarEstado(id, estado);
            assertEstado(200, r);
            assertEquals(estado, r.texto("estado"));
        }
        assertTrue("El pedido pagado registra la hora de pago", r.tiene("pagadoEn"));
        assertError(409, "No se puede pasar", cambiarEstado(id, "pagado"));
    }

    @Test
    public void noSePuedeSaltarUnEstado() {
        long id = crearPedido(1).entero("id");
        assertError(409, "No se puede pasar", cambiarEstado(id, "listo"));
    }

    @Test
    public void unEstadoInexistenteSeRechaza() {
        long id = crearPedido(1).entero("id");
        assertError(400, "Estado no valido", cambiarEstado(id, "volando"));
    }

    @Test
    public void elListadoSePuedeFiltrarPorEstado() {
        long id = crearPedido(1).entero("id");
        cambiarEstado(id, "en_preparacion");
        Respuesta r = api.get("/pedidos?estado=en_preparacion", tokenMesero);
        assertEstado(200, r);
        assertTrue(r.cuerpo.contains("\"id\":" + id));
        assertTrue("El filtro no debe traer pedidos de otro estado", !r.cuerpo.contains("\"estado\":\"recibido\""));
    }
}
