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

import org.apache.ratis.client.RaftClient;
import org.apache.ratis.conf.Parameters;
import org.apache.ratis.conf.RaftProperties;
import org.apache.ratis.grpc.GrpcFactory;
import org.apache.ratis.protocol.*;
import org.apache.ratis.thirdparty.com.google.protobuf.ByteString;
import org.apache.ratis.grpc.GrpcConfigKeys;
import org.apache.ratis.protocol.RaftGroup;
import org.apache.ratis.protocol.RaftGroupId;
import org.apache.ratis.protocol.RaftPeer;
import org.apache.ratis.protocol.RaftPeerId;
import org.apache.ratis.server.RaftServer;
import org.apache.ratis.server.RaftServerConfigKeys;
import org.apache.ratis.util.LifeCycle;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Collectors;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Map;
import java.util.*;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class HelloWorldServer {

    static HashMap<Long, Tripla> banco = new HashMap<Long, Tripla>();

    private static final Logger logger = Logger.getLogger(HelloWorldServer.class.getName());

    private Server server;
    private static String raftGroupId = "raft_group____um"; // 16 caracteres.
    private static Map<String, InetSocketAddress> id2addr = new HashMap<>();
    private static List<RaftPeer> addresses;
    private static RaftPeerId myId;
    private static RaftProperties properties = new RaftProperties();
    private static RaftProperties raftProperties = new RaftProperties();
    private static RaftClient client;

    public HelloWorldServer() {
        id2addr.put("p1", new InetSocketAddress("127.0.0.1", 3000));
        id2addr.put("p2", new InetSocketAddress("127.0.0.1", 3500));
        id2addr.put("p3", new InetSocketAddress("127.0.0.1", 4000));
        addresses = id2addr.entrySet()
                .stream()
                .map(e -> new RaftPeer(RaftPeerId.valueOf(e.getKey()), e.getValue()))
                .collect(Collectors.toList());
        properties.setInt(GrpcConfigKeys.OutputStream.RETRY_TIMES_KEY, Integer.MAX_VALUE);
    }

    private void start(int port) throws IOException {
        /* The port on which the server should run */
        server = ServerBuilder.forPort(port)
                .addService(new CreaterImpl())
                .addService(new GreeterImpl())
                .addService(new ReaderImpl())
                .addService(new AtualizarImpl())
                .addService(new DeletarImpl())
                .build()
                .start();
        logger.info("Server started, listening on " + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                // Use stderr here since the logger may have been reset by its JVM shutdown hook.
                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                try {
                    HelloWorldServer.this.stop();
                } catch (InterruptedException e) {
                    e.printStackTrace(System.err);
                }
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() throws InterruptedException {
        if (server != null) {
            server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
        }
    }

    /**
     * Await termination on the main thread since the grpc library uses daemon threads.
     */
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    /**
     * Main launches the server from the command line.
     */
    public static void main(String[] args) throws IOException, InterruptedException {
        final HelloWorldServer server = new HelloWorldServer();
        server.start(Integer.parseInt(args[0]));
        //server.blockUntilShutdown();

        myId = RaftPeerId.valueOf(args[1]);
        if (addresses.stream().noneMatch(p -> p.getId().equals(myId))) {
            System.out.println("Identificador " + args[1] + " é inválido.");
            System.exit(1);
        }

        GrpcConfigKeys.Server.setPort(properties, id2addr.get(args[1]).getPort());
        RaftServerConfigKeys.setStorageDir(properties, Collections.singletonList(new File("/tmp/" + myId)));


        //Join the group of processes.
        final RaftGroup raftGroup = RaftGroup.valueOf(RaftGroupId.valueOf(ByteString.copyFromUtf8(raftGroupId)), addresses);
        RaftServer raftServer = RaftServer.newBuilder()
                .setServerId(myId)
                .setStateMachine(new MaquinaDeEstados()).setProperties(properties)
                .setGroup(raftGroup)
                .build();
        raftServer.start();


        //cliente
        client = RaftClient.newBuilder()
                .setProperties(raftProperties)
                .setRaftGroup(raftGroup)
                .setClientRpc(new GrpcFactory(new Parameters())
                        .newRaftClientRpc(ClientId.randomId(), raftProperties))
                .build();
        client.close();
        while (raftServer.getLifeCycleState() != LifeCycle.State.CLOSED) {
            TimeUnit.SECONDS.sleep(1);
        }
    }

    static class GreeterImpl extends GreeterGrpc.GreeterImplBase {

        @Override
        public void sayHello(HelloRequest req, StreamObserver<HelloReply> responseObserver) {
            HelloReply reply = HelloReply.newBuilder().setMessage("Hello " + req.getName()).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    static class CreaterImpl extends CreaterGrpc.CreaterImplBase {

        @Override
        public void sayCreate(CreateRequest req, StreamObserver<CreateReply> responseObserver) {


            RaftClientReply getValue;
            String response;
            long chave = req.getChave();
            String [] dados = {req.getDados(),"1",String.valueOf(req.getTimeStamp())};
            try {
                getValue = client.send(Message.valueOf("add:" + chave + ":" + Arrays.toString(dados)));
                response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
                CreateReply sucesso = CreateReply.newBuilder().setMessage(response).build();
                responseObserver.onNext(sucesso);
                responseObserver.onCompleted();
                client.close();
            } catch (IOException e) {
            }
        }
    }

    static class ReaderImpl extends ReaderGrpc.ReaderImplBase {

        @Override
        public void sayRead(ReadRequest req, StreamObserver<ReadReply> responseObserver) {


            RaftClientReply getValue2;
            String response2;
            long chave = req.getChave();
            try {
                getValue2 = client.sendReadOnly(Message.valueOf("get:" + chave));
                response2 = getValue2.getMessage().getContent().toString(Charset.defaultCharset());
                ReadReply sucesso = ReadReply.newBuilder().setMessage(response2).build();
                responseObserver.onNext(sucesso);
                responseObserver.onCompleted();
                client.close();
            } catch (IOException e) {
            }

        }

    }

    static class AtualizarImpl extends AtualizarGrpc.AtualizarImplBase {

        @Override
        public void sayAtualiza(AtualizaRequest req, StreamObserver<AtualizaReply> responseObserver) {
            RaftClientReply getValue;
            String response;
            long chave = req.getChave();
            String [] dados = {req.getDados(), String.valueOf(req.getVersao()), String.valueOf(req.getTimeStamp())};
            try {
                getValue = client.send(Message.valueOf("replace:" + chave + ":" + Arrays.toString(dados)));
                response = getValue.getMessage().getContent().toString(Charset.defaultCharset());
                AtualizaReply sucesso = AtualizaReply.newBuilder().setMessage(response).build();
                responseObserver.onNext(sucesso);
                responseObserver.onCompleted();
                client.close();
            } catch (IOException e) {
            }

        }

    }

    static class DeletarImpl extends DeletarGrpc.DeletarImplBase {

        @Override
        public void sayDeleta(DeletaRequest req, StreamObserver<DeletaReply> responseObserver) {
            RaftClientReply getValue2;
            String response2;
            long chave = req.getChave();
            try {
                getValue2 = client.send(Message.valueOf("delete:" + chave));
                response2 = getValue2.getMessage().getContent().toString(Charset.defaultCharset());
                DeletaReply sucesso = DeletaReply.newBuilder().setMessage(response2).build();
                responseObserver.onNext(sucesso);
                responseObserver.onCompleted();
                client.close();
            } catch (IOException e) {
            }
        }
    }
}