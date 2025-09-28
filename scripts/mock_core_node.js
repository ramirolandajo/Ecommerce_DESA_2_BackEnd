const http = require('http');
const fs = require('fs');
const path = require('path');

const LOGPATH = '/scripts/log_core.txt';

const server = http.createServer((req, res) => {
  let body = '';
  req.on('data', chunk => { body += chunk.toString(); });
  req.on('end', () => {
    const entry = `[${new Date().toISOString()}] ${req.method} ${req.url}\nHEADERS: ${JSON.stringify(req.headers)}\nBODY: ${body}\n\n`;
    try {
      fs.appendFileSync(LOGPATH, entry, { encoding: 'utf8' });
    } catch (e) {
      console.error('Failed to write log:', e);
    }
    console.log(entry);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end('{}');
  });
});

const PORT = process.env.PORT || 8086;
server.listen(PORT, '0.0.0.0', () => {
  console.log(`mock-core node server listening on ${PORT}`);
});

