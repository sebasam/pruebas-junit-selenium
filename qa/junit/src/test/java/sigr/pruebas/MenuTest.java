package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Menú digital: lectura pública, permisos por rol y validaciones del CRUD. */
public class MenuTest extends PruebaBase {

    /** Cuerpo JSON con un nombre de categoría distinto en cada llamada, para que las pruebas no choquen entre sí. */
    private static String nombreUnico() { return "{\"nombre\":\"Sin permiso " + System.nanoTime() + "\"}"; }

    @Test
    public void elMenuEsPublicoYTraeCategoriasConSusPlatos() {
        Respuesta r = api.get("/menu", null);
        assertEstado(200, r);
        assertFalse("Debería haber categorías iniciales", r.lista("").isEmpty());
        assertTrue("Cada categoría trae su lista de platos", r.en("0.platos") instanceof java.util.List);
    }

    @Test
    public void crearCategoriaSinSesionDa401() {
        assertEstado(401, api.post("/menu/categorias", nombreUnico(), null));
    }

    @Test
    public void crearCategoriaComoMeseroDa403() {
        assertError(403, "permiso", api.post("/menu/categorias", nombreUnico(), tokenMesero));
    }

    @Test
    public void categoriaRepetidaSeRechazaSinImportarMayusculas() {
        String nombre = "Sopas" + System.nanoTime();
        assertEstado(201, api.post("/menu/categorias", "{\"nombre\":\"" + nombre + "\"}", tokenAdmin));
        assertError(409, "ya existe", api.post("/menu/categorias", "{\"nombre\":\"" + nombre.toUpperCase() + "\"}", tokenAdmin));
    }

    @Test
    public void platoValidoQuedaDisponibleConElPrecioIndicado() {
        long id = crearPlato("Tres leches", 12000);
        Respuesta menu = api.get("/menu", null);
        assertTrue("El plato nuevo debe aparecer en el menú", menu.cuerpo.contains("\"id\":" + id));
    }

    @Test
    public void platoConPrecioNegativoOCeroSeRechaza() {
        long cat = api.post("/menu/categorias", "{\"nombre\":\"Cat" + System.nanoTime() + "\"}", tokenAdmin).entero("id");
        assertError(400, "mayor que cero", api.post("/menu/platos",
                "{\"nombre\":\"Error\",\"precio\":-5,\"categoriaId\":" + cat + "}", tokenAdmin));
        assertError(400, "mayor que cero", api.post("/menu/platos",
                "{\"nombre\":\"Gratis\",\"precio\":0,\"categoriaId\":" + cat + "}", tokenAdmin));
    }

    @Test
    public void platoConCategoriaInexistenteDa404() {
        assertError(404, "categoria no existe", api.post("/menu/platos",
                "{\"nombre\":\"Huérfano\",\"precio\":5000,\"categoriaId\":99999}", tokenAdmin));
    }

    @Test
    public void actualizarPrecioConservaElNombre() {
        long id = crearPlato("Flan", 9000);
        Respuesta r = api.put("/menu/platos/" + id, "{\"precio\":9500}", tokenAdmin);
        assertEstado(200, r);
        assertEquals(9500, r.entero("precio"));
        assertEquals("Flan", r.texto("nombre"));
    }

    @Test
    public void eliminarPlatoDa204YYaNoExiste() {
        long id = crearPlato("Temporal", 1000);
        assertEstado(204, api.delete("/menu/platos/" + id, tokenAdmin));
        assertError(404, "no existe", api.put("/menu/platos/" + id, "{\"precio\":1}", tokenAdmin));
    }
}
