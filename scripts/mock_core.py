#!/usr/bin/env python3
# Simple mock Core API endpoint for local testing
# Accepts POST /events and prints headers + body

from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import sys
from datetime import datetime

LOGPATH = 'scripts/log_core.txt'

class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path.endswith('/events'):
            length = int(self.headers.get('content-length', 0))
            body = self.rfile.read(length).decode('utf-8')
            print('[mock_core] Received POST /events')
            print('[mock_core] Headers:')
            for k,v in self.headers.items():
                print(f'  {k}: {v}')
            print('[mock_core] Body:')
            print(body)
            # Log to file
            try:
                with open(LOGPATH, 'a', encoding='utf-8') as f:
                    f.write(f"[{datetime.utcnow().isoformat()}] POST {self.path}\n")
                    f.write(f"[{datetime.utcnow().isoformat()}] HEADERS: {dict(self.headers)}\n")
                    f.write(f"[{datetime.utcnow().isoformat()}] BODY: {body}\n")
            except Exception as e:
                print('Failed to write log:', e)
            resp = ''
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', '0')
            self.end_headers()
            return
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        # write minimal logs to stdout
        print("[mock_core] %s - - [%s] %s" % (self.client_address[0], self.log_date_time_string(), format%args))

if __name__ == '__main__':
    port = 8086
    if len(sys.argv) > 1:
        port = int(sys.argv[1])
    print(f"Starting mock Core endpoint at http://localhost:{port}/events")
    server = HTTPServer(('0.0.0.0', port), Handler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print('Stopping mock server')
        server.server_close()
