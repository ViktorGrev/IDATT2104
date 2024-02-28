
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Tomas Holt, inspired by http://www.baeldung.com/java-nio2-async-socket-channel
 */
public class AsyncSocketServerCompletionHandler {

    private AsynchronousServerSocketChannel serverChannel;

    public void init() throws Exception {
        System.out.println("Lets accept clients.");
        serverChannel = AsynchronousServerSocketChannel.open();
        serverChannel.bind(new InetSocketAddress("127.0.0.1", 4555));

        System.out.println("Making client handler ");
        serverChannel.accept(
                null, new CompletionHandler<AsynchronousSocketChannel, Object>() {

            @Override
            public void completed(
                    AsynchronousSocketChannel result, Object attachment) {
                
                if (serverChannel.isOpen()) {
                    serverChannel.accept(null, this);//accept new clients connecting
                }

                AsynchronousSocketChannel clientChannel = result;

                if ((clientChannel != null) && (clientChannel.isOpen())) {
                    
                    ReadWriteHandler handler = new ReadWriteHandler(clientChannel);// added clientChannel - has to be local in instance
                    ByteBuffer buffer = ByteBuffer.allocate(32);

                    Map<String, Object> readInfo = new HashMap<>();
                    readInfo.put("action", "read");
                    readInfo.put("buffer", buffer);

                    clientChannel.read(buffer, readInfo, handler);//handler(ReadWriteHandler) is used for communication with client
                    System.out.println("Done \"main \" read");
                }
            }

            @Override
            public void failed(Throwable exc, Object attachment) {
                // process error
            }
        });
        System.in.read();//keep server running
    }

    class ReadWriteHandler implements CompletionHandler<Integer, Map<String, Object>> {

        private AsynchronousSocketChannel clientChannel;

        /* Tomas, keep clientChannel local, ie. for this client */
        public ReadWriteHandler(AsynchronousSocketChannel clientChannel) {
            this.clientChannel = clientChannel;
            System.out.println("Channel = " + clientChannel.toString());
        }

        @Override
        public void completed(
            Integer result, Map<String, Object> attachment) {
            System.out.println("Start ReadWriteHandeler.completed()");

            Map<String, Object> actionInfo = attachment;
            String action = (String) actionInfo.get("action");
            
            System.out.println("The action is " + action);

            //check if client has closed socket channel
            if (result == -1) {
                System.out.println("Client closed connection, bye.");
                return;//end, ie. do not register a new callback/listener
            }
            
            
            if ("read".equals(action)) {
                ByteBuffer buffer = (ByteBuffer) actionInfo.get("buffer");
                buffer.flip();
                actionInfo.put("action", "write");

                System.out.println("Let's write to client");

                clientChannel.write(buffer, actionInfo, this);
                buffer.clear();
                System.out.println("Registered new callback/listener for clientChannel.write()");

            } else if ("write".equals(action)) {
                ByteBuffer buffer = ByteBuffer.allocate(32);

                actionInfo.put("action", "read");
                actionInfo.put("buffer", buffer);

                clientChannel.read(buffer, actionInfo, this);
                System.out.println("Registered new callback/listener for clientChannel.read()");
            }

        }

        @Override
        public void failed(Throwable exc, Map<String, Object> attachment) {
            // 
        }
    }

    public static void main(String args[]) throws Exception {
        System.out.println("Start server");
        AsyncSocketServerCompletionHandler server = new AsyncSocketServerCompletionHandler();
        server.init();
    }
}
