package sigr.pruebas;

import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

/** Ejecuta la suite e imprime un informe legible: una línea por prueba, resumen por clase y detalle de fallos. */
public class EjecutarPruebas {
    public static void main(String[] args) {
        Map<String, int[]> porClase = new LinkedHashMap<>(); // {ejecutadas, fallidas}
        Map<String, Long> inicio = new LinkedHashMap<>();
        Map<String, Boolean> fallo = new LinkedHashMap<>();

        JUnitCore nucleo = new JUnitCore();
        nucleo.addListener(new RunListener() {
            @Override public void testStarted(Description d) { inicio.put(d.getDisplayName(), System.nanoTime()); }
            @Override public void testFailure(Failure f) { fallo.put(f.getDescription().getDisplayName(), true); }
            @Override public void testFinished(Description d) {
                long ms = (System.nanoTime() - inicio.get(d.getDisplayName())) / 1_000_000;
                boolean f = fallo.containsKey(d.getDisplayName());
                int[] c = porClase.computeIfAbsent(simple(d.getClassName()), k -> new int[2]);
                c[0]++;
                if (f) c[1]++;
                System.out.printf("  %s %-4s %-3d ms  %s.%s%n", f ? "✗" : "✓", f ? "FALLA" : "OK", ms,
                        simple(d.getClassName()), d.getMethodName());
            }
        });

        long t0 = System.nanoTime();
        Result r = nucleo.run(SuiteSigr.class);
        long total = (System.nanoTime() - t0) / 1_000_000;

        System.out.println();
        System.out.println("RESUMEN POR CLASE");
        System.out.printf("  %-34s %10s %10s%n", "Clase", "Ejecutadas", "Fallidas");
        porClase.forEach((k, v) -> System.out.printf("  %-34s %10d %10d%n", k, v[0], v[1]));
        System.out.printf("  %-34s %10d %10d%n", "TOTAL", r.getRunCount(), r.getFailureCount());
        System.out.printf("  Duración total: %.1f s%n", total / 1000.0);

        if (!r.wasSuccessful()) {
            System.out.println();
            System.out.println("DETALLE DE LOS FALLOS");
            for (Failure f : r.getFailures()) {
                System.out.println("  - " + f.getDescription().getMethodName() + " (" + simple(f.getDescription().getClassName()) + ")");
                String msg = String.valueOf(f.getMessage()).replace("\n", " ");
                System.out.println("      " + (msg.length() > 160 ? msg.substring(0, 159) + "…" : msg));
            }
        }
        System.out.println();
        System.out.println(r.wasSuccessful() ? "RESULTADO: TODAS LAS PRUEBAS PASARON" : "RESULTADO: HAY PRUEBAS FALLIDAS");
        System.exit(r.wasSuccessful() ? 0 : 1);
    }

    private static String simple(String clase) { return clase.substring(clase.lastIndexOf('.') + 1); }
}
