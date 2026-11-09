package sigr.pruebas;

import static org.junit.Assert.assertFalse;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Una misma prueba ejecutada con una tabla de datos: validaciones del registro de usuarios. */
@RunWith(Parameterized.class)
public class RegistroParametrizadoTest extends PruebaBase {
    private final String caso, nombre, email, clave, mensaje;
    private final int estadoEsperado;

    @Parameters(name = "{0}")
    public static Collection<Object[]> datos() {
        return Arrays.asList(new Object[][] {
            // caso,                       nombre,        correo,               clave,        estado, mensaje esperado
            {"datos válidos",              "Marta Díaz",  "marta<ts>@prueba.com", "Clave12345", 201, null},
            {"nombre con tildes y eñe",    "Íñigo Muñoz", "inigo<ts>@prueba.com", "Clave12345", 201, null},
            {"clave en el límite (8)",     "Límite Ocho", "ocho<ts>@prueba.com",  "12345678",   201, null},
            {"sin nombre",                 null,          "sn<ts>@prueba.com",    "Clave12345", 400, "obligatorios"},
            {"sin correo",                 "Sin Correo",  null,                   "Clave12345", 400, "obligatorios"},
            {"sin clave",                  "Sin Clave",   "sc<ts>@prueba.com",    null,         400, "obligatorios"},
            {"correo sin arroba",          "Mal Correo",  "correo.prueba.com",    "Clave12345", 400, "correo no es valido"},
            {"correo sin dominio",         "Mal Dominio", "ana@sigr",             "Clave12345", 400, "correo no es valido"},
            {"clave de 7 caracteres",      "Clave Corta", "cc<ts>@prueba.com",    "1234567",    400, "al menos 8"},
            {"correo ya registrado",       "Duplicado",   "admin@sigr.local",     "Clave12345", 409, "ya esta registrado"},
        });
    }

    public RegistroParametrizadoTest(String caso, String nombre, String email, String clave, int estado, String mensaje) {
        this.caso = caso;
        this.nombre = nombre;
        this.email = email == null ? null : email.replace("<ts>", String.valueOf(System.nanoTime()));
        this.clave = clave;
        this.estadoEsperado = estado;
        this.mensaje = mensaje;
    }

    private static String campo(String clave, String valor) {
        return valor == null ? "" : "\"" + clave + "\":\"" + valor + "\",";
    }

    @Test
    public void registroSegunLosDatos() {
        String cuerpo = "{" + campo("nombre", nombre) + campo("email", email) + campo("clave", clave);
        cuerpo = cuerpo.endsWith(",") ? cuerpo.substring(0, cuerpo.length() - 1) + "}" : cuerpo + "}";
        Respuesta r = api.post("/auth/registro", cuerpo, null);
        if (mensaje == null) {
            assertEstado(estadoEsperado, r);
            assertFalse("No debe exponer el hash: " + r, r.tiene("hash"));
        } else {
            assertError(estadoEsperado, mensaje, r);
        }
    }
}
