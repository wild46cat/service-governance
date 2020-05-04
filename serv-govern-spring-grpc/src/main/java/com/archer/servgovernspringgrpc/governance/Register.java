package com.archer.servgovernspringgrpc.governance;

public interface Register {
    /**
     * 服务注册
     */
    public void register();

    /**
     * 服务重新注册
     */
    public void reRegister();


    /**
     * 服务注销
     */
    public void unRegister();

    /**
     * 服务续约(续约1次)
     */
    public void keepAlive();

}
