package sigr.pruebas;

import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/** Autenticación: inicio de sesión, registro y control de roles. */
public class AutenticacionTest extends PruebaBase {

    @Test
    public void loginDeAdministradorDevuelveTokenYRol() {
        Respuesta r = api.post("/auth/login", "{\"email\":\"admin@sigr.local\",\"clave\":\"Admin12345\"}", null);
        assertEstado(200, r);
        assertTrue("El token debería tener al menos 20 caracteres", r.texto("token").length() >= 20);
        assertEquals("administrador", r.texto("usuario.rol"));
    }

    @Test
    public void loginConClaveIncorrectaNoEntregaToken() {
        Respuesta r = api.post("/auth/login", "{\"email\":\"admin@sigr.local\",\"clave\":\"incorrecta1\"}", null);
        assertError(401, "Credenciales", r);
        assertFalse("No debe devolver token", r.tiene("token"));
    }

    @Test
    public void registroCreaClienteSinExponerClaveNiHash() {
        String email = "cliente" + System.nanoTime() + "@prueba.com";
        Respuesta r = api.post("/auth/registro",
                "{\"nombre\":\"Cliente Prueba\",\"email\":\"" + email + "\",\"clave\":\"Clave12345\"}", null);
        assertEstado(201, r);
        assertEquals("cliente", r.texto("rol"));
        assertThat(r.cuerpo, not(containsString("hash")));
        assertThat(r.cuerpo, not(containsString("Clave12345")));
    }

    @Test
    public void registroNoPermiteAutoasignarseElRolDeAdministrador() {
        String email = "intruso" + System.nanoTime() + "@prueba.com";
        Respuesta r = api.post("/auth/registro",
                "{\"nombre\":\"Intruso\",\"email\":\"" + email + "\",\"clave\":\"Clave12345\",\"rol\":\"administrador\"}", null);
        assertEstado(201, r);
        assertEquals("El rol solicitado debe ignorarse", "cliente", r.texto("rol"));
    }

    @Test
    public void sesionDeClienteNoPuedeUsarFuncionesDeAdministrador() {
        String email = "otro" + System.nanoTime() + "@prueba.com";
        api.post("/auth/registro", "{\"nombre\":\"Otro\",\"email\":\"" + email + "\",\"clave\":\"Clave12345\"}", null);
        String token = api.iniciarSesion(email, "Clave12345");
        assertError(403, "permiso", api.get("/reportes/ventas?fecha=2026-01-01", token));
    }

    @Test
    public void tokenInventadoEsTratadoComoSinSesion() {
        assertEstado(401, api.get("/reportes/ventas?fecha=2026-01-01", "token-que-no-existe"));
    }
}
