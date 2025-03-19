#!/usr/bin/env python3

import socket
import threading

# Server configuration
HOST = '152.105.66.53'  # Listen on all available network interfaces
PORT = 4300         # Port number

clients = []  # List to store connected clients

def handle_client(client_socket, address):
    """Handles incoming messages from a client and broadcasts them to others."""
    print(f"[+] New connection from {address}")
    
    clients.append(client_socket)
    idBytes = ("Player" + str(len(clients))).encode('utf-8')
    idLength = len(idBytes)
    
    idMessage = (idLength).to_bytes(1, byteorder='big')
    idMessage+=idBytes
    
    client_socket.send(idMessage)
    print(f"ID Message sent to {idBytes.decode('utf-8')}: Length {idLength}, Whole message = {idMessage}")
    try:
        while True:
            message = client_socket.recv(1024)
            if not message:
                break
            # print(f"[Message from {address}]: {message.decode('utf-8')}")
            broadcast(message, client_socket)
    except ConnectionResetError:
        print(f"[-] Connection lost from {address}")
    finally:
        clients.remove(client_socket)
        client_socket.close()

def broadcast(message, sender_socket):
    """Sends a message to all connected clients except the sender."""
    for client in clients:
        if client != sender_socket:
            try:
                client.send(message)
            except Exception:
                clients.remove(client)
                client.close()

def start_server():
    """Starts the TCP server and listens for new connections."""
    server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    server.bind((HOST, PORT))
    server.listen(5)
    print(f"[*] Server listening on {HOST}:{PORT}")
    while True:
        client_socket, address = server.accept()
        client_thread = threading.Thread(target=handle_client, args=(client_socket, address))
        client_thread.start()

if __name__ == "__main__":
    start_server()
