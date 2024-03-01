const express = require('express');
const fs = require('fs');
const { exec } = require('child_process');
const app = express();
const port = 3000;

app.use(express.json());

app.get('/', (req, res) => {
    res.sendFile(__dirname + '/index.html');
});

const path = require('path');

app.post('/compile', (req, res) => {
    const code = req.body.code;
    fs.writeFileSync(path.join(__dirname, 'source.cpp'), code);

    const currentDir = path.resolve(__dirname);

    console.log("Noe melding her: " + currentDir);

    const dockerCommand = `docker run --rm -v "${currentDir}:/src" venv_ubuntu_1 bash -c "g++ /src/source.cpp -o /src/output && /src/output"`;

    exec(dockerCommand, (error, stdout, stderr) => {
        if (error) {
            console.error(`exec error: ${error}`);
            return res.send(`Error: ${stderr}`);
        }
        res.send(stdout);
    });
});

app.listen(port, () => {
    console.log(`Server kjører på http://localhost:${port}`);
});
