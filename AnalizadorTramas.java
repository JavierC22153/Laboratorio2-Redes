public class AnalizadorTramas {

    private static int n = 0;
    private static int m = 0;

    public static int calcularN(String tramaRecibida) {
        if (tramaRecibida == null || tramaRecibida.isEmpty()) {
            return 0;
        }
        return tramaRecibida.length();
    }

    public static int calcularM(String tramaRecibida) {
        n = calcularN(tramaRecibida);
        if (n == 0) return 0;

        int r = 1;
        while (Math.pow(2, r) < n + 1) {
            r++;
        }

        m = n - r;
        return m;
    }

    public  void analizarTrama(String tramaRecibida) {
        m = calcularM(tramaRecibida); // n ya se calcula dentro de calcularM
        System.out.println("n = " + n + ", m = " + m);
    }

    // Getters si los necesitas
    public  int getN() {
        return n;
    }

    public  int getM() {
        return m;
    }
}
