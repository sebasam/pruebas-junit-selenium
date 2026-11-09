package sigr.pruebas;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/** Agrupa todas las clases de prueba para ejecutarlas con una sola orden. */
@RunWith(Suite.class)
@SuiteClasses({
    AutenticacionTest.class, LoginParametrizadoTest.class, RegistroParametrizadoTest.class,
    MenuTest.class, PedidosTest.class, ReservasTest.class, ReservasLimitesTest.class,
    CajaTest.class, RobustezYFormulariosTest.class
})
public class SuiteSigr { }
