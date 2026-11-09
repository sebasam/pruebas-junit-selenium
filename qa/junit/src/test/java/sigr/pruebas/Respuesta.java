package sigr.pruebas;

import java.util.List;
import java.util.Map;

/** Respuesta HTTP ya leída: estado, cuerpo en texto y acceso al JSON por ruta (por ejemplo "usuario.rol"). */
public final class Respuesta {
    public final int estado;
    public final String cuerpo;
    public final long milisegundos;
    private Object json;

    Respuesta(int estado, String cuerpo, long milisegundos) {
        this.estado = estado;
        this.cuerpo = cuerpo;
        this.milisegundos = milisegundos;
    }

    private Object raiz() {
        if (json == null) json = Json.leer(cuerpo);
        return json;
    }

    /** Navega el JSON con una ruta separada por puntos; los números indexan listas. */
    public Object en(String ruta) {
        Object actual = raiz();
        if (ruta.isEmpty()) return actual;
        for (String parte : ruta.split("\\.")) {
            if (actual instanceof Map) actual = ((Map<?, ?>) actual).get(parte);
            else if (actual instanceof List) actual = ((List<?>) actual).get(Integer.parseInt(parte));
            else return null;
        }
        return actual;
    }

    public String texto(String ruta) { Object o = en(ruta); return o == null ? null : o.toString(); }

    public long entero(String ruta) { return ((Double) en(ruta)).longValue(); }

    public boolean tiene(String ruta) { return en(ruta) != null; }

    public List<?> lista(String ruta) { return (List<?>) en(ruta); }

    public String error() { return texto("error"); }

    @Override
    public String toString() { return estado + " " + cuerpo; }
}
