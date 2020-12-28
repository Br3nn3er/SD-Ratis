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

import org.apache.ratis.proto.RaftProtos;
import org.apache.ratis.protocol.Message;
import org.apache.ratis.statemachine.TransactionContext;
import org.apache.ratis.statemachine.impl.BaseStateMachine;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


public class MaquinaDeEstados extends BaseStateMachine {
    private final Map<String, String> key2values = new ConcurrentHashMap<>();

    @Override
    public CompletableFuture<Message> query(Message request) {
        final String[] opKey = request.getContent().toString(Charset.defaultCharset()).split(":");
        final String result = opKey[0] + ":" + key2values.get(opKey[1]);
        LOG.debug("{}: {} = {}", opKey[0], opKey[1], result);
        return CompletableFuture.completedFuture(Message.valueOf(result));
    }


    @Override
    public CompletableFuture<Message> applyTransaction(TransactionContext trx) {
        final RaftProtos.LogEntryProto entry = trx.getLogEntry();
        final String[] opKeyValue = entry.getStateMachineLogEntry().getLogData().toString(Charset.defaultCharset()).split(":");
        final String result;
        final String[] data;
        final String[] dataMap;
        int version = 0;
        final RaftProtos.RaftPeerRole role = trx.getServerRole();
        if (opKeyValue[0].equals("delete")) {
            result = opKeyValue[0] + ":" + key2values.remove(opKeyValue[1]);
            LOG.info("{}:{} {} {}", role, getId(), opKeyValue[0], opKeyValue[1]);
        } else if (opKeyValue[0].equals("replace") && key2values.get(opKeyValue[1]) != null) {
            data = opKeyValue[2].split(",");
            dataMap = key2values.get(opKeyValue[1]).split(",");
             if (data[1].equals(dataMap[1])) {
                version = Integer.parseInt(dataMap[1].replaceAll("\\s+",""));
                 version++;
                 data[1] = (" "+version);
                 opKeyValue[2] = String.join(",", data);
                 result = opKeyValue[0] + ":" + key2values.replace(opKeyValue[1], opKeyValue[2]);
                LOG.info("{}:{} {} {}={}", role, getId(), opKeyValue[0], opKeyValue[1], opKeyValue[2]);
            }
             else {
                 result = opKeyValue[0] + ":null";
             }
        } else if (opKeyValue[0].equals("add")) {
            result = opKeyValue[0] + ":" + key2values.putIfAbsent(opKeyValue[1], opKeyValue[2]);
            LOG.info("{}:{} {} {}={}", role, getId(), opKeyValue[0], opKeyValue[1], opKeyValue[2]);
        }
        else {
            result = opKeyValue[0] + ":null";
        }
        final CompletableFuture<Message> f = CompletableFuture.completedFuture(Message.valueOf(result));

        if (LOG.isTraceEnabled()) {
            LOG.trace("{}: key/values={}", getId(), key2values);
        }
        return f;
    }
}