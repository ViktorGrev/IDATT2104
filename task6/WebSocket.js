const net = require('net');
const crypto = require('crypto');

// Create an HTTP server using net.createServer()
const httpServer = net.createServer((connection) => {
    connection.on('data', () => {
        let content = `<!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <title>WebSocket Test</title>
        </head>
        <body>
            <h2>Send a message to the server via WebSocket</h2>
            <textarea id="txt" cols="40" rows="5"></textarea>
            <button id="send-btn" onclick="sendMessage()">Send Message</button>
            <div id="messages">Messages: <br></div>
            <canvas id="drawboard" width="500" height="500" style="border:1px solid #000;"></canvas>
            <script>
                var ws = new WebSocket('ws://localhost:3001');
                ws.onopen = () => console.log("Connection is open");
                ws.onmessage = event => {
                    let msg = event.data;
                    if (msg.startsWith('draw|')) {
                        let [, fromX, fromY, toX, toY] = msg.split('|').map(Number);
                        drawLine(fromX, fromY, toX, toY);
                    } else {
                        document.getElementById("messages").innerHTML += msg + "<br>";
                    }
                };
                function sendMessage() {
                    var message = document.getElementById("txt").value;
                    ws.send(message);
                    document.getElementById("txt").value = ''; // Clear the text area after sending
                    document.getElementById("messages").innerHTML += message + "<br>";
                }
        
                var canvas = document.getElementById('drawboard');
                var context = canvas.getContext('2d');
                var isDrawing = false;
                var lastX = 0;
                var lastY = 0;
                
                canvas.addEventListener('mousedown', function(e) {
                    isDrawing = true;
                    [lastX, lastY] = [e.offsetX, e.offsetY];
                });
        
                canvas.addEventListener('mousemove', function(e) {
                    if (!isDrawing) return;
                    let cmd = \`draw|\${lastX}|\${lastY}|\${e.offsetX}|\${e.offsetY}\`;
                    ws.send(cmd); // Send drawing command to the server
                    drawLine(lastX, lastY, e.offsetX, e.offsetY); // Draw locally
                    [lastX, lastY] = [e.offsetX, e.offsetY];
                });
        
                function drawLine(fromX, fromY, toX, toY) {
                    context.beginPath();
                    context.moveTo(fromX, fromY);
                    context.lineTo(toX, toY);
                    context.stroke();
                }
        
                canvas.addEventListener('mouseup', () => isDrawing = false);
                canvas.addEventListener('mouseleave', () => isDrawing = false);
        
                context.strokeStyle = '#000';
                context.lineWidth = 2;
            </script>
        </body>
        </html>
    `;
        connection.write('HTTP/1.1 200 OK\r\nContent-Length: ' + content.length + '\r\n\r\n' + content);
    });
});

httpServer.listen(3000, () => {
    console.log('HTTP server listening on port 3000');
});

// Create a WebSocket server
const wsServer = net.createServer();
let clients = []; // Array to keep track of all connected clients

wsServer.on('connection', (connection) => {
    console.log('Client connected');
    clients.push(connection); // Add the new connection to the list of clients

    connection.on('data', (data) => {
        if (data[0] === 0x8) {
            console.log("Close message received from client");
            connection.end();
            return;
        }

        let isHandshake = data.toString().indexOf("Sec-WebSocket-Key") !== -1;

        if (isHandshake) {
            // Perform WebSocket handshake
            let key;
            const arr = data.toString().split("\n");
            arr.forEach((line) => {
                if (line.includes("Sec-WebSocket-Key")) {
                    key = line.split(" ")[1].trim();
                }
            });

            var shasum = crypto.createHash('sha1');
            combined = key + "258EAFA5-E914-47DA-95CA-C5AB0DC85B11";
            shasum.update(combined);
            encoded = shasum.digest('base64');

            connection.write("HTTP/1.1 101 Switching Protocols\r\nUpgrade: websocket\r\nConnection: Upgrade\r\nSec-WebSocket-Accept: " + encoded + "\r\n\r\n");
            return;
        }

        // Decode WebSocket frame
        let bytes = Buffer.from(data);
        let length = bytes[1] & 127;
        let maskStart = 2;
        let dataStart = maskStart + 4;
        let msg = '';
        var myBuffer = [];

        for (let i = dataStart; i < dataStart + length; i++) {
            let byte = bytes[i] ^ bytes[maskStart + ((i - dataStart) % 4)];
            myBuffer.push(byte);
            msg += String.fromCharCode(byte);
        }
        console.log("Message: " + msg);

        // Broadcast the message to all clients except the sender
        clients.forEach((client) => {
            if (client !== connection && client.writable) {
                client.write(Buffer.from([0x81, length, ...myBuffer]));
            }
        });
    });

    connection.on('end', () => {
        console.log('Client disconnected');
        clients = clients.filter((client) => client !== connection);
    });
});

wsServer.on('error', (error) => {
    console.error('Error: ', error);
});

wsServer.listen(3001, () => {
    console.log('WebSocket server listening on port 3001');
});
