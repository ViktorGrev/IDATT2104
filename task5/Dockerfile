# Bruk et offisielt Ubuntu base image
FROM ubuntu:latest

# Installer g++ for å kompilere C++ kode
RUN apt-get update && apt-get install -y g++

# Sett arbeidskatalogen i containeren
WORKDIR /src

# Kommandoen som kjøres når containeren starter. 
# Denne kommandoen gjør ingenting siden vi vil kjøre kommandoer dynamisk med exec
CMD ["bash"]
