import java.io.*;
import java.net.*;

public class UDPTjener {
    public static void main(String[] args) throws IOException {
        final int PORT = 1250;
        byte[] mottaData = new byte[1024];
        byte[] sendData = new byte[1024];

        // Oppretter en DatagramSocket for å lytte på porten
        DatagramSocket tjenerSocket = new DatagramSocket(PORT);
        System.out.println("Tjeneren kjører og venter på data...");

        while (true) {
            DatagramPacket mottaPakke = new DatagramPacket(mottaData, mottaData.length);
            tjenerSocket.receive(mottaPakke);

            // Henter ut meldingen fra pakken
            String melding = new String(mottaPakke.getData(), 0, mottaPakke.getLength());
            System.out.println("Mottatt: " + melding);

            // Behandler mottatt melding for å beregne resultatet
            String resultat = behandleMelding(melding);

            sendData = resultat.getBytes();

            // Sender resultatet tilbake til klienten
            InetAddress klientAdresse = mottaPakke.getAddress();
            int klientPort = mottaPakke.getPort();
            DatagramPacket sendPakke = new DatagramPacket(sendData, sendData.length, klientAdresse, klientPort);
            tjenerSocket.send(sendPakke);
        }

    }

    private static String behandleMelding(String melding) {
        String[] parts = melding.split("\\s+");
        if (parts.length == 3) {
            int operand1 = Integer.parseInt(parts[0]);
            int operand2 = Integer.parseInt(parts[2]);
            int result;
            if (parts[1].equals("+")) {
                result = operand1 + operand2;
            } else if (parts[1].equals("-")) {
                result = operand1 - operand2;
            } else {
                result = 0; // Handle unsupported operator
            }
            return "Resultat: " + result; // Dette er bare et eksempel
            }
        return "Invalid input format. Please provide two operands separated by an operator (+ or -).";
    }
}
