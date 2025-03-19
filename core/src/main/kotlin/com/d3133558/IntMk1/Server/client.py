#!/usr/bin/env python3

import socket
import threading

# Server configuration
HOST = '152.105.66.53'  # Change to the actual server IP if needed
PORT = 4300

def receive_messages(client_socket):
    """Receives messages from the server and prints them."""
    while True:
        try:
            message = client_socket.recv(1024).decode('utf-8')
            if not message:
                break
            print(message) # Assuming text message
        except Exception:
            print("[-] Connection closed.")
            break

def start_client():
    """Starts the TCP client and connects to the server."""
    client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    client.connect((HOST, PORT))
    receive_thread = threading.Thread(target=receive_messages, args=(client,))
    receive_thread.start()
    try:
        while True:
            message = input()
            if message.lower() == 'exit':
                break
            client.send(message.encode('utf-8'))
    except KeyboardInterrupt:
        print("[-] Client disconnected.")
    finally:
        client.close()

if __name__ == "__main__":
    start_client()
