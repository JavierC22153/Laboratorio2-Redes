public class Coder {
    public String coder_funct(String PAYLOAD) {
        StringBuilder binario = new StringBuilder();
        for (char c : PAYLOAD.toCharArray()) {
            String bits = String.format("%8s", Integer.toBinaryString(c))
                                .replace(' ', '0');
            binario.append(bits).append(" ");
        }

        return binario.toString().trim();
    }
}
