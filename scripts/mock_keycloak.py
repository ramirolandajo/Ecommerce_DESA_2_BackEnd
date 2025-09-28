#!/usr/bin/env python3
# Simple mock Keycloak token endpoint for local testing
# Usage: python mock_keycloak.py [port]

import sys
from http.server import BaseHTTPRequestHandler, HTTPServer
import json
from datetime import datetime

LOGPATH = 'scripts/log_keycloak.txt'

class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        if self.path.endswith('/token'):
            length = int(self.headers.get('content-length', 0))
            body = self.rfile.read(length).decode('utf-8')
            # Not validating body, just return fake token
            token = 'mock-token-1234567890abcdefghijklmnopqrstuvwxyz'
            resp = {
                'access_token': token,
                'expires_in': 300,
                'token_type': 'Bearer'
            }
            data = json.dumps(resp).encode('utf-8')
            self.send_response(200)
            self.send_header('Content-Type', 'application/json')
            self.send_header('Content-Length', str(len(data)))
            self.end_headers()
            self.wfile.write(data)
            # Log to file
            try:
                with open(LOGPATH, 'a', encoding='utf-8') as f:
                    f.write(f"[{datetime.utcnow().isoformat()}] POST {self.path} headers={dict(self.headers)} body={body}\n")
                    f.write(f"[{datetime.utcnow().isoformat()}] RESPONSE {resp}\n")
            except Exception as e:
                print('Failed to write log:', e)
        else:
            self.send_response(404)
            self.end_headers()

    def log_message(self, format, *args):
        print("[mock_keycloak] %s - - [%s] %s" % (self.client_address[0], self.log_date_time_string(), format%args))

if __name__ == '__main__':
    port = 8085
    if len(sys.argv) > 1:
        port = int(sys.argv[1])
    print(f"Starting mock Keycloak token endpoint at http://localhost:{port}/token")
    server = HTTPServer(('0.0.0.0', port), Handler)
    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print('Stopping mock server')
        server.server_close()
