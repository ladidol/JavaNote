package org.cuit.epoch.rpc.provider;

import org.cuit.epoch.rpc.api.IRpcService;

public class RpCServiceImpl implements IRpcService {
    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int mult(int a, int b) {
        return a * b;
    }

    public int div(int a, int b) {
        if (0 == b) return 0;
        return a / b;
    }
}
