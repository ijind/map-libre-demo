package ai.txai.push.entity;

/**
 * @Description PushGateAddress
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
public class PushGateAddress {
    private String ip;
    private int port;

    public PushGateAddress(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}