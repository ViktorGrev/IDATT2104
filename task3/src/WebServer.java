import java.io.*;
import java.net.*;

public class WebServer {
    public static void main(String[] args) {
        final int PORT = 80;

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Web server running on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected.");

                // Opprett en strøm for å lese fra klienten
                BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                // Opprett en strøm for å skrive til klienten
                PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());

                // Les HTTP-headeren fra klienten
                StringBuilder headerLines = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null && !line.isEmpty()) {
                    headerLines.append("<LI>").append(line).append("</LI>");
                }

                // Skriv HTTP-responsen til klienten
                writer.println("HTTP/1.0 200 OK");
                writer.println("Content-Type: text/html; charset=utf-8");
                writer.println();
                writer.println("<HTML><BODY>");
                writer.println("<H1> Hilsen. Du har koblet deg opp til min enkle web-tjener awdawdawawd</H1>");
                writer.println("Header fra klient er:");
                writer.println("<UL>");
                writer.println(headerLines);
                writer.println("</UL>");
                writer.println("</BODY></HTML>");
                writer.flush();

                // Lukk forbindelsen
                reader.close();
                writer.close();
                clientSocket.close();
                System.out.println("Connection closed.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
