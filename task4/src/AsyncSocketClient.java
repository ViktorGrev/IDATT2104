
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousSocketChannel;
import java.util.concurrent.Future;

/**
 *
 * @author Tomas Holt, inspired by http://www.baeldung.com/java-nio2-async-socket-channel
 */

public class AsyncSocketClient {
    
    private AsynchronousSocketChannel client;
    
    public void init() throws Exception{
        client = AsynchronousSocketChannel.open();
        InetSocketAddress hostAddress = new InetSocketAddress("localhost", 4555);
        Future<Void> future = client.connect(hostAddress);
        
        future.get();//wait for connection
    }

    public String sendMessage(String message) throws Exception {
        byte[] byteMsg = new String(message).getBytes();
        ByteBuffer buffer = ByteBuffer.wrap(byteMsg);
        Future<Integer> writeResult = client.write(buffer);

        // do some computation
        writeResult.get();//wait for result
        buffer.flip();
        Future<Integer> readResult = client.read(buffer);

        // do some computation
        readResult.get();//wait for result
        String echo = new String(buffer.array()).trim();
        buffer.clear();
        return echo;
    }
    
    public void cleanUp() throws IOException{
        client.shutdownInput();
        client.shutdownOutput();
        client.close();
    }

    public static void main(String args[]) throws Exception {
        System.out.println("*Start client");
        AsyncSocketClient client = new AsyncSocketClient();
        client.init();
        String serverString = client.sendMessage("TEST");
        System.out.println("Message for server " + serverString);
        //serverString = client.sendMessage("TEST 2**");
        //System.out.println("Message 2 from server " + serverString);
        client.cleanUp();
    }

}
