package sigr.pruebas;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/** Análisis de valores límite del número de personas (válido: de 1 a 12). */
@RunWith(Parameterized.class)
public class ReservasLimitesTest extends PruebaBase {
    private final int personas, estadoEsperado, mesa;

    @Parameters(name = "{0} personas -> {1}")
    public static Collection<Object[]> datos() {
        return Arrays.asList(new Object[][] {
            // personas, estado esperado, mesa (cada caso usa una distinta para ser independiente)
            {-1, 400, 1}, {0, 400, 2}, {1, 201, 3}, {2, 201, 4}, {11, 201, 5}, {12, 201, 6}, {13, 400, 7}, {100, 400, 8},
        });
    }

    public ReservasLimitesTest(int personas, int estado, int mesa) {
        this.personas = personas;
        this.estadoEsperado = estado;
        this.mesa = mesa;
    }

    @Test
    public void limiteDePersonas() {
        String cuerpo = "{\"cliente\":\"Grupo\",\"mesa\":" + mesa + ",\"fecha\":\"2031-01-15\",\"hora\":\"20:00\",\"personas\":" + personas + "}";
        Respuesta r = api.post("/reservas", cuerpo, null);
        if (estadoEsperado == 400) assertError(400, "entre 1 y 12", r);
        else assertEstado(201, r);
    }
}
