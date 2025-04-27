b = bytes.fromhex('79651acfa6733fc23a3fd36f3637beeba4b15667dd55f303f74bb9150fe1fd73')

# Convert bytes to bytearray
ba = bytearray(b)

# XOR each byte
for i in range(len(ba)):
    ba[i] ^= 0xAA

# Convert back to hex string
print(ba.hex())
