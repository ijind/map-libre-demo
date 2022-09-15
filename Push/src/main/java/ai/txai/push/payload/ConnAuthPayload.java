package ai.txai.push.payload;

import ai.txai.common.json.GsonManager;
import ai.txai.push.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Admin
 * @className ConnAuthPayload
 * @date 12/7/21 17:29
 * @description
 */

public class ConnAuthPayload extends AbstractMessagePayload {
    // 用户ID
    private String userId;
    // 客户端连接标识 Android/iOS
    private String endpoint;
    // ConnectAuth Key
    private byte[] authKey;

    // 采集客户端信息
    private ClientInfoPayload clientInfo;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public byte[] getAuthKey() {
        return authKey;
    }

    public void setAuthKey(byte[] authKey) {
        this.authKey = authKey;
    }

    public ClientInfoPayload getClientInfo() {
        return clientInfo;
    }

    public void setClientInfo(ClientInfoPayload clientInfo) {
        this.clientInfo = clientInfo;
    }

    /**
     * 编码
     *
     * @param payload
     * @param buf
     * @return
     */
    @Override
    public void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf) {
        try {
            ConnAuthPayload cap = (ConnAuthPayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, cap.getUserId());
            ByteBufUtil.writeStringForByteLength(buf, cap.getEndpoint());
            buf.writeBytes(cap.getAuthKey());
            if (cap.getClientInfo() != null) {
                String str = GsonManager.GsonString(cap.getClientInfo());
                buf.writeBytes(str.getBytes());
            }
        } catch (Exception e) {
            throw new RuntimeException("ConnAuth pack Exception:" + e.getMessage());
        }
    }

    /**
     * 解码
     *
     * @param buf
     * @return
     */
    @Override
    public AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf) {
        try {
            ConnAuthPayload cap = new ConnAuthPayload();
            String userId = ByteBufUtil.readStringForByteLength(buf);
            String endpoint = ByteBufUtil.readStringForByteLength(buf);
            byte[] ak = new byte[16];
            buf.readBytes(ak);
            cap.setUserId(userId);
            cap.setEndpoint(endpoint);
            cap.setAuthKey(ak);
            if (buf.readableBytes() > 0) {
                byte[] clientInfoBytes = new byte[buf.readableBytes()];
                buf.readBytes(clientInfoBytes);
                cap.setClientInfo(GsonManager.fromJsonObject(new String(clientInfoBytes), ClientInfoPayload.class));
            }
            return cap;
        } catch (Exception e) {
            throw new RuntimeException("ConnAuth unpack exception:" + e.getMessage());
        }

    }
}
