# SD_trab
Segundo projeto de sistemas distribuidos


# Requisitos

Clonar o projeto de exemplo fornecido no site do gRPC, pelo comando:
git clone -b v1.33.0 https://github.com/grpc/grpc-java

Navega até grpc-java/examples/src/main/java/io/grpc/examples/helloworld

Copie e cole os arquivos .java do projeto para essa pasta

Agora vá até a pasta grpc-java/examples/src/main/proto

Copie e cole o arquivo .proto para essa pasta

Vá ate a pasta grpc-java/examples

Copie e cole os arquivos pom.xml e build.gradle na pasta atual

Abra o terminal e execute o comando: ./gradlew installDist

Execute o servidor com ./build/install/examples/bin/hello-world-server $param1 $param2

-> Sendo param1: a porta do grpc

-> Sendo param2: o id do servidor Ratis

Obs: nesse projeto usamos p1,p2 e p3 como id do Ratis

Execute o cliente com ./build/install/examples/bin/hello-world-client $param1

-> Sendo param1: a porta do servidor grpc


Obs: No menu do cliente a opção '5 - Cliente Teste', é um teste de estresse.

### Integrantes:

Brenner de Souza Broges                  11421BCC013

Pedro Henrique Fernandes de Oliveira     11521BCC015


