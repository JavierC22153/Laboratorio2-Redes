

public class CRC32Emisor {

    private static final int POLY = 0x04C11DB7;
    private static final int POLY_LEN = 32;

   
    public  String generarTramaConCRC(String mensajeBin) {
        long data = Long.parseUnsignedLong(mensajeBin, 2);
        int dataLen = mensajeBin.length();

        
        long dataExtendida = data << POLY_LEN;
        int totalLen = dataLen + POLY_LEN;

        long mask = 1L << (totalLen - 1);

        for (int i = 0; i < dataLen; i++) {
            if ((dataExtendida & mask) != 0) {
                dataExtendida ^= ((long) POLY) << (totalLen - POLY_LEN - 1 - i);
            }
            mask >>= 1;
        }

       
        long crc = dataExtendida & ((1L << POLY_LEN) - 1);

       
        String mensajePadded = mensajeBin;
        String crcBin = String.format("%32s", Long.toBinaryString(crc)).replace(' ', '0');

        return mensajePadded + crcBin;
    }

    

}
