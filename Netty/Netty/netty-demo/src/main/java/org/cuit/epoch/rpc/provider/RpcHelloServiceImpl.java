package org.cuit.epoch.rpc.provider;

import org.cuit.epoch.rpc.api.IRpcHelloService;

public class RpcHelloServiceImpl implements IRpcHelloService {
    public String hello(String name) {
        return "Hello " + name + "!";
    }
}
