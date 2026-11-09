package sigr.pruebas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Inicio de sesión con credenciales correctas e incorrectas. */
@RunWith(Parameterized.class)
public class LoginParametrizadoTest extends PruebaBase {
    private final String caso, email, clave, rol;
    private final int estadoEsperado;

    @Parameters(name = "{0}")
    public static Collection<Object[]> datos() {
        return Arrays.asList(new Object[][] {
            {"administrador correcto", "admin@sigr.local",  "Admin12345",  200, "administrador"},
            {"mesero correcto",        "mesero@sigr.local", "Mesero12345", 200, "mesero"},
            {"clave incorrecta",       "admin@sigr.local",  "admin12345",  401, null},
            {"correo inexistente",     "nadie@sigr.local",  "Admin12345",  401, null},
            {"clave vacía",            "admin@sigr.local",  "",            401, null},
            {"mayúsculas en el correo", "ADMIN@sigr.local", "Admin12345",  401, null},
        });
    }

    public LoginParametrizadoTest(String caso, String email, String clave, int estado, String rol) {
        this.caso = caso;
        this.email = email;
        this.clave = clave;
        this.estadoEsperado = estado;
        this.rol = rol;
    }

    @Test
    public void loginSegunLosDatos() {
        Respuesta r = api.post("/auth/login", "{\"email\":\"" + email + "\",\"clave\":\"" + clave + "\"}", null);
        assertEstado(estadoEsperado, r);
        if (estadoEsperado == 200) {
            assertTrue(r.tiene("token"));
            assertEquals(rol, r.texto("usuario.rol"));
        } else {
            assertFalse("Un login fallido no debe entregar token", r.tiene("token"));
        }
    }
}
