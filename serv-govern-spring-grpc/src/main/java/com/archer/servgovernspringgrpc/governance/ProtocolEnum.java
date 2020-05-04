package com.archer.servgovernspringgrpc.governance;

public enum ProtocolEnum {
    HTTP("http"), HTTPS("https"), gRPC("grpc");

    ProtocolEnum(String protocolName) {
        this.protocolName = protocolName;
    }

    private String protocolName;

    public String getProtocolName() {
        return protocolName;
    }

    public void setProtocolName(String protocolName) {
        this.protocolName = protocolName;
    }
}
