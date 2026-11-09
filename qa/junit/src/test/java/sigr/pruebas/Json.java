package sigr.pruebas;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Lector de JSON mínimo (el JDK no incluye uno). Devuelve Map, List, String, Double, Boolean o null. */
final class Json {
    private final String s;
    private int i;

    private Json(String s) { this.s = s; }

    static Object leer(String texto) {
        Json j = new Json(texto);
        Object v = j.valor();
        j.espacios();
        if (j.i != texto.length()) throw new IllegalArgumentException("Texto sobrante en la posición " + j.i);
        return v;
    }

    private void espacios() { while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++; }

    private Object valor() {
        espacios();
        char c = s.charAt(i);
        if (c == '{') return objeto();
        if (c == '[') return lista();
        if (c == '"') return cadena();
        if (s.startsWith("true", i)) { i += 4; return Boolean.TRUE; }
        if (s.startsWith("false", i)) { i += 5; return Boolean.FALSE; }
        if (s.startsWith("null", i)) { i += 4; return null; }
        return numero();
    }

    private Map<String, Object> objeto() {
        Map<String, Object> m = new LinkedHashMap<>();
        i++; espacios();
        if (s.charAt(i) == '}') { i++; return m; }
        while (true) {
            espacios();
            String clave = cadena();
            espacios(); esperar(':');
            m.put(clave, valor());
            espacios();
            if (s.charAt(i) == ',') { i++; continue; }
            esperar('}');
            return m;
        }
    }

    private List<Object> lista() {
        List<Object> l = new ArrayList<>();
        i++; espacios();
        if (s.charAt(i) == ']') { i++; return l; }
        while (true) {
            l.add(valor());
            espacios();
            if (s.charAt(i) == ',') { i++; continue; }
            esperar(']');
            return l;
        }
    }

    private String cadena() {
        esperar('"');
        StringBuilder sb = new StringBuilder();
        while (s.charAt(i) != '"') {
            char c = s.charAt(i++);
            if (c != '\\') { sb.append(c); continue; }
            char e = s.charAt(i++);
            switch (e) {
                case 'n': sb.append('\n'); break;
                case 't': sb.append('\t'); break;
                case 'r': sb.append('\r'); break;
                case 'b': sb.append('\b'); break;
                case 'f': sb.append('\f'); break;
                case 'u': sb.append((char) Integer.parseInt(s.substring(i, i + 4), 16)); i += 4; break;
                default: sb.append(e);
            }
        }
        i++;
        return sb.toString();
    }

    private Double numero() {
        int ini = i;
        while (i < s.length() && "+-0123456789.eE".indexOf(s.charAt(i)) >= 0) i++;
        if (ini == i) throw new IllegalArgumentException("Valor JSON no válido en la posición " + i);
        return Double.valueOf(s.substring(ini, i));
    }

    private void esperar(char c) {
        if (s.charAt(i) != c) throw new IllegalArgumentException("Se esperaba '" + c + "' en la posición " + i);
        i++;
    }
}
