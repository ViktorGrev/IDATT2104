import java.io.*;
import java.net.*;
import java.util.Scanner;

public class UDPKlient {
    public static void main(String[] args) throws IOException {
        // Bufferstørrelse for å motta data
        byte[] mottaData = new byte[1024];

        // Oppretter en DatagramSocket for å sende og motta data
        DatagramSocket klientSocket = new DatagramSocket();

        // Adresse til tjeneren
        InetAddress tjenerAdresse = InetAddress.getByName("localhost"); // eller bruk tjenermaskinens adresse
        final int PORT = 1250;

        Scanner leserFraKommandovindu = new Scanner(System.in);
        System.out.println("Skriv regnestykket (format: operand operator operand, f.eks., 5 + 3):");

        String inputLinje = leserFraKommandovindu.nextLine();
        while (!inputLinje.equals("")) {
            byte[] sendData = inputLinje.getBytes();

            // Sender pakken til tjeneren
            DatagramPacket sendPakke = new DatagramPacket(sendData, sendData.length, tjenerAdresse, PORT);
            klientSocket.send(sendPakke);

            // For å motta svar fra tjeneren
            DatagramPacket mottaPakke = new DatagramPacket(mottaData, mottaData.length);
            klientSocket.receive(mottaPakke);

            String svar = new String(mottaPakke.getData(), 0, mottaPakke.getLength());
            System.out.println("Fra tjeneren: " + svar);

            // Leser neste linje
            inputLinje = leserFraKommandovindu.nextLine();
        }

        // Lukker socketen
        klientSocket.close();
    }
}

