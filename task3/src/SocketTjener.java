import java.io.*;
import java.net.*;

public class SocketTjener {
    public static void main(String[] args) throws IOException {
        final int PORTNR = 1250;

        ServerSocket tjener = new ServerSocket(PORTNR);
        System.out.println("Logg for tjenersiden. Nå venter vi...");

        while (true) {
            Socket forbindelse = tjener.accept();  // venter inntil noen tar kontakt
            Thread klientTraad = new KlientTraad(forbindelse);
            klientTraad.start();
        }
    }
}

class KlientTraad extends Thread {
    private Socket forbindelse;

    public KlientTraad(Socket forbindelse) {
        this.forbindelse = forbindelse;
    }

    public void run() {
        try {
            /* Åpner strømmer for kommunikasjon med klientprogrammet */
            InputStreamReader leseforbindelse = new InputStreamReader(forbindelse.getInputStream());
            BufferedReader leseren = new BufferedReader(leseforbindelse);
            PrintWriter skriveren = new PrintWriter(forbindelse.getOutputStream(), true);

            /* Sender innledning til klienten */
            skriveren.println("Hei, du har kontakt med tjenersiden!");
            skriveren.println("Skriv hva du vil, så skal jeg gjenta det, avslutt med linjeskift.");

            /* Mottar data fra klienten */
            String enLinje = leseren.readLine();  // mottar en linje med tekst
            while (enLinje != null) {  // forbindelsen på klientsiden er lukket
                System.out.println("En klient skrev: " + enLinje);
                String[] parts = enLinje.split("\\s+");
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
                    skriveren.println("Resultat: " + result);  // sender svar til klienten
                } else {
                    skriveren.println("Invalid input format. Please provide two operands separated by an operator (+ or -).");
                }
                enLinje = leseren.readLine();
            }

            /* Lukker forbindelsen */
            leseren.close();
            skriveren.close();
            forbindelse.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
