/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.helloworld;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;
import java.util.Date;
import java.lang.*;

/**
 * A simple client that requests a greeting from the {@link HelloWorldServer}.
 */
public class HelloWorldClient {
    private static final Logger logger = Logger.getLogger(HelloWorldClient.class.getName());

    private final GreeterGrpc.GreeterBlockingStub blockingStub;
    private final CreaterGrpc.CreaterBlockingStub blockingStub2;
    private final ReaderGrpc.ReaderBlockingStub blockingStub3;
    private final AtualizarGrpc.AtualizarBlockingStub blockingStub4;
    private final DeletarGrpc.DeletarBlockingStub blockingStub5;

    /**
     * Construct client for accessing HelloWorld server using the existing channel.
     */
    public HelloWorldClient(Channel channel) {
        // 'channel' here is a Channel, not a ManagedChannel, so it is not this code's responsibility to
        // shut it down.

        // Passing Channels to code makes code easier to test and makes it easier to reuse Channels.
        blockingStub = GreeterGrpc.newBlockingStub(channel);
        blockingStub2 = CreaterGrpc.newBlockingStub(channel);
        blockingStub3 = ReaderGrpc.newBlockingStub(channel);
        blockingStub4 = AtualizarGrpc.newBlockingStub(channel);
        blockingStub5 = DeletarGrpc.newBlockingStub(channel);
    }

    /**
     * Say hello to server.
     */
    public void greet(String name) {
        logger.info("Will try to greet " + name + " ...");
        HelloRequest request = HelloRequest.newBuilder().setName(name).build();
        HelloReply response;
        try {
            response = blockingStub.sayHello(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }

    /**
     * Greet server. If provided, the first element of {@code args} is the name to use in the
     * greeting. The second argument is the target server.
     */

    public void create(long k, long ts, String d) {
        //logger.info("Will try to greet " + name + " ...");
        CreateRequest request = CreateRequest.newBuilder().setChave(k).setTimeStamp(ts).setDados(d).build();
        CreateReply response;
        try {
            response = blockingStub2.sayCreate(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }

    public void read(long k) {
        //logger.info("Will try to greet " + name + " ...");
        ReadRequest request = ReadRequest.newBuilder().setChave(k).build();
        ReadReply response;
        try {
            response = blockingStub3.sayRead(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }

    public void atualiza(long k, long versao, long ts, String d) {
        //logger.info("Will try to greet " + name + " ...");
        AtualizaRequest request = AtualizaRequest.newBuilder().setChave(k).setVersao(versao).setTimeStamp(ts).setDados(d).build();
        AtualizaReply response;
        try {
            response = blockingStub4.sayAtualiza(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }

    public void deleta(long k) {
        //logger.info("Will try to greet " + name + " ...");
        DeletaRequest request = DeletaRequest.newBuilder().setChave(k).build();
        DeletaReply response;
        try {
            response = blockingStub5.sayDeleta(request);
        } catch (StatusRuntimeException e) {
            logger.log(Level.WARNING, "RPC failed: {0}", e.getStatus());
            return;
        }
        logger.info("Greeting: " + response.getMessage());
    }

    public static void main(String[] args) throws Exception {
        //teste
        int opcao;
        long chave;
        long versao;
        long timestamp;
        String dados;
        Random rand = new Random();

        String info;
        Scanner scanner = new Scanner(System.in);
        Date date = new Date();


        String user = "world";
        // Access a service running on the local machine on port 50051
        String target = "localhost:" + args[0];
        // Allow passing in the user and target strings as command line arguments
        if (args.length > 0) {
            if ("--help".equals(args[0])) {
                System.err.println("Usage: [name [target]]");
                System.err.println("");
                System.err.println("  name    The name you wish to be greeted by. Defaults to " + user);
                System.err.println("  target  The server to connect to. Defaults to " + target);
                System.exit(1);
            }
        }

        // Create a communication channel to the server, known as a Channel. Channels are thread-safe
        // and reusable. It is common to create channels at the beginning of your application and reuse
        // them until the application shuts down.
        ManagedChannel channel = ManagedChannelBuilder.forTarget(target)
                // Channels are secure by default (via SSL/TLS). For the example we disable TLS to avoid
                // needing certificates.
                .usePlaintext()
                .build();

        do {
            System.out.println("Digite a opcao desejada:");
            System.out.println("1 - Criar chave/valor");
            System.out.println("2 - Ler");
            System.out.println("3 - Atualizar");
            System.out.println("4 - Excluir");
            System.out.println("5 - Cliente Teste");
            System.out.println("0 - Sair");

            opcao = scanner.nextInt();

            switch (opcao) {
                case 0:
                    break;
                case 1:
                    date = new Date();
                    timestamp = date.getTime();
                    System.out.println("Informe a chave:");
                    chave = scanner.nextLong();
                    scanner.nextLine();
                    System.out.println("Informe os dados:");
                    info = scanner.nextLine();
                    //dados = info.getBytes("UTF8"); //Charset.forName("UTF-8")
                    try {
                        HelloWorldClient client = new HelloWorldClient(channel);
                        client.create(chave, timestamp, info);
                    } finally {
                        //channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);
                    }
                    break;
                case 2:
                    System.out.println("Informe a chave:");
                    chave = scanner.nextLong();
                    try {
                        HelloWorldClient client = new HelloWorldClient(channel);
                        client.read(chave);
                    } finally {
                        //channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);}
                    }
                    break;
                case 3:
                    date = new Date();
                    timestamp = date.getTime();
                    System.out.println("Informe a chave:");
                    chave = scanner.nextLong();
                    scanner.nextLine();
                    System.out.println("Informe a versao:");
                    versao = scanner.nextLong();
//                    versao = 1;
                    scanner.nextLine();
                    System.out.println("Informe os dados:");
                    info = scanner.nextLine();
                    try {
                        HelloWorldClient client = new HelloWorldClient(channel);
                        client.atualiza(chave, versao, timestamp, info);
                    } finally {
                        //channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);}
                    }
                    break;
                case 4:
                    System.out.println("Informe a chave:");
                    chave = scanner.nextLong();
                    scanner.nextLine();
                    try {
                        HelloWorldClient client = new HelloWorldClient(channel);
                        client.deleta(chave);
                    } finally {
                        //channel.shutdownNow().awaitTermination(5, TimeUnit.SECONDS);}
                    }
                    break;
                case 5:
                    try{
                        HelloWorldClient client = new HelloWorldClient(channel);
                        for (int i= 0; i<1000; i++) {
                            date = new Date();
                            long key = ThreadLocalRandom.current().nextLong(100000);
                            timestamp = date.getTime();
                            client.create(key,timestamp,"info"+key);
                            client.atualiza(key,1,timestamp,"at"+key);
                            client.read(key);
                        }
                    }finally {}

                    break;

                default:
                    System.out.println("Opcao invalida!");
            }
        } while (opcao != 0);
    }
}