#!/usr/bin/env python3

import socket
import threading
import struct

# Server configuration
HOST = '192.168.0.64'  # Listen on all available network interfaces
PORT = 4300         # Port number

clients = []  # List to store connected clients

def handle_client(client_socket, address):
    """Handles incoming messages from a client and broadcasts them to others."""
    print(f"[+] New connection from {address}")
    
    clients.append(client_socket)
    idBytes = ("Player" + str(len(clients))).encode('utf-8')
    idLength = len(idBytes)
    
     # Send the playerid
    idMessage = struct.pack('>H', 1 + idLength + 4 + 4 + 1)  # total length
    idMessage += struct.pack('B', idLength)  # sender id length
    idMessage += idBytes                     # sender id
    idMessage += struct.pack('>f', 0.0)      # x
    idMessage += struct.pack('>f', 0.0)      # y
    idMessage += struct.pack('B', 1)         # currently 1 for MOVE but i might want to change it to its own identification message type
    
    
    client_socket.send(idMessage)
    print(f"ID Message sent to {idBytes.decode('utf-8')}: Length {idLength}, Whole message = {idMessage}")
    try:
        while True:
            #read the length (2 bytes)
            length_bytes = recvall(client_socket, 2)
            if not length_bytes:
                break
            total_length = struct.unpack('>H', length_bytes)[0]

            #read the rest
            message = recvall(client_socket, total_length)
            if not message:
                break
            # print(f"[Message from {address}]: {message.decode('utf-8')}")
            broadcast(length_bytes + message, client_socket)
    except ConnectionResetError:
        print(f"[-] Connection lost from {address}")
    finally:
        clients.remove(client_socket)
        client_socket.close()

def recvall(client_socket, n):
    """Receive n bytes from socket or return None on error."""
    data = bytearray()
    while len(data) < n:
        packet = client_socket.recv(n - len(data))
        if not packet:
            return None
        data.extend(packet)
    return data

def broadcast(message, sender_socket):
    """Sends a message to all connected clients except the sender."""
    print(f"[Broadcasting message of {len(message)} bytes]")
    for client in clients:
        if client != sender_socket:
            try:
                client.sendall(message)
            except Exception as e:
                print(f"[-] Error sending to a client: {e}")
                client.close()
                clients.remove(client)

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
