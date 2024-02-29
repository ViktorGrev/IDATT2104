const express = require('express');
const fs = require('fs');
const { exec } = require('child_process');
const app = express();
const port = 3000;

// Middleware for å parse JSON body
app.use(express.json());

// Server 'index.html' på rot-URL
app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});

const path = require('path');

// API-endepunkt for å motta og kjøre C++ kode
app.post('/compile', (req, res) => {
    const code = req.body.code;
    // Lagrer mottatt kode til en fil
    fs.writeFileSync(path.join(__dirname, 'source.cpp'), code);

    // Bygger en absolutt sti for Docker volum-montering
    const currentDir = path.resolve(__dirname);

    console.log("Noe melding her: " + currentDir);

    // Kjører Docker-kommando for å kompilere og kjøre C++ koden
    //const dockerCommand = `docker run --rm -v "${currentDir}:/src" venv_ubuntu_1 g++ /src/source.cpp -o /src/output && /src/output`;
    const dockerCommand = `docker run --rm -v "${currentDir}:/src" venv_ubuntu_1 bash -c "g++ /src/source.cpp -o /src/output && /src/output"`;

    exec(dockerCommand, (error, stdout, stderr) => {
        if (error) {
            console.error(`exec error: ${error}`);
            return res.send(`Error: ${stderr}`);
        }
        res.send(stdout);
    });
});

// Starter serveren
app.listen(port, () => {
    console.log(`Server kjører på http://localhost:${port}`);
});
