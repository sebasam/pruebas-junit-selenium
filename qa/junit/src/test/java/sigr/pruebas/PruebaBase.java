package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.Timeout;

/**
 * Base de todas las clases de prueba. Cada clase arranca su propio SIGR con base limpia, de modo
 * que ninguna depende de los datos que dejó otra.
 */
public abstract class PruebaBase {
    protected static ServidorSigr servidor;
    protected static ClienteApi api;
    protected static String tokenAdmin;
    protected static String tokenMesero;

    /** Ninguna prueba debería tardar más de 10 s; si se cuelga, falla en lugar de bloquear la suite. */
    @Rule public Timeout limite = Timeout.seconds(10);

    @BeforeClass
    public static void iniciarServidor() throws Exception {
        servidor = ServidorSigr.iniciar();
        api = new ClienteApi(servidor.url);
        tokenAdmin = api.iniciarSesion("admin@sigr.local", "Admin12345");
        tokenMesero = api.iniciarSesion("mesero@sigr.local", "Mesero12345");
    }

    @AfterClass
    public static void detenerServidor() throws Exception {
        if (servidor != null) servidor.close();
    }

    // ---------- aserciones reutilizables ----------

    /** Verifica el código HTTP e incluye la respuesta completa en el mensaje si no coincide. */
    protected static void assertEstado(int esperado, Respuesta r) {
        assertEquals("Código HTTP (respuesta: " + r + ")", esperado, r.estado);
    }

    /** Verifica el código HTTP y que el mensaje de error contenga el fragmento indicado. */
    protected static void assertError(int esperado, String fragmento, Respuesta r) {
        assertEstado(esperado, r);
        assertNotNull("La respuesta debería traer un mensaje de error: " + r, r.error());
        org.junit.Assert.assertTrue("El error '" + r.error() + "' debería contener '" + fragmento + "'",
                r.error().contains(fragmento));
    }

    /** Crea una categoría y un plato como administrador y devuelve el id del plato. */
    protected static long crearPlato(String nombre, double precio) {
        String sufijo = String.valueOf(System.nanoTime());
        Respuesta c = api.post("/menu/categorias", "{\"nombre\":\"Cat " + sufijo + "\"}", tokenAdmin);
        assertEstado(201, c);
        Respuesta p = api.post("/menu/platos",
                "{\"nombre\":\"" + nombre + "\",\"precio\":" + precio + ",\"categoriaId\":" + c.entero("id") + "}", tokenAdmin);
        assertEstado(201, p);
        return p.entero("id");
    }
}
