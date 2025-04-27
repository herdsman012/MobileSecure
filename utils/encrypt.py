from Crypto.Cipher import AES
from Crypto.Random import get_random_bytes
import zipfile
import os


def encrypt_data(data, key):
    cipher = AES.new(key, AES.MODE_CBC)
    data_padded = data + (AES.block_size - len(data) % AES.block_size) * b'\0'
    ciphertext = cipher.encrypt(data_padded)
    return cipher.iv + ciphertext  # Prepend IV for use during decryption


def encrypt_zip(zip_file_path, encrypted_file_path, key):
    # Read the ZIP file content
    with open(zip_file_path, 'rb') as zip_file:
        zip_data = zip_file.read()

    # Encrypt the ZIP file content
    encrypted_data = encrypt_data(zip_data, key)

    # Write the encrypted data to the output file
    with open(encrypted_file_path, 'wb') as encrypted_file:
        encrypted_file.write(encrypted_data)


if __name__ == '__main__':
    # Your key must be 16, 24, or 32 bytes for AES
    key = get_random_bytes(32)  # 32 bytes = AES-256
    zip_file_path = '../app/src/main/assets/data.zip'  # Path to the ZIP file to encrypt
    encrypted_file_path = '../app/src/main/assets/data.enc'  # Path to save the encrypted file

    print(zip_file_path)
    print(key.hex())

    # Encrypt the ZIP file
    encrypt_zip(zip_file_path, encrypted_file_path, key)
    print(f"Encrypted file saved to: {encrypted_file_path}")
