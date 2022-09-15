package ai.txai.push.payload;

import ai.txai.push.common.CommonStatusCodeEnum;
import ai.txai.push.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Admin
 * @className ConnAuthRespPayload
 * @date 12/8/21 10:43
 * @description
 */
public class ConnAuthRespPayload  extends AbstractMessagePayload{

    private CommonStatusCodeEnum status;
    private String message;
    private long serverTime;

    public CommonStatusCodeEnum getStatus() {
        return status;
    }

    public void setStatus(CommonStatusCodeEnum status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public void setResponseStatus(CommonStatusCodeEnum status){
        this.status = status;
        this.message = status.getMessage();
        this.serverTime = System.currentTimeMillis();
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
            ConnAuthRespPayload carp = (ConnAuthRespPayload) payload;
            buf.writeInt(carp.getStatus().getCode());
            ByteBufUtil.writeStringForIntLength(buf, carp.getMessage());
            buf.writeLong(carp.getServerTime());
        } catch (Exception e) {
            throw new RuntimeException("ConnAuthRespPayload pack exception:" + e.getMessage());
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
            ConnAuthRespPayload carp = new ConnAuthRespPayload();
            int code = buf.readInt();
            String message = ByteBufUtil.readStringForIntLength(buf);
            long serverTime = buf.readLong();
            carp.setStatus(CommonStatusCodeEnum.fromCode(code));
            carp.setMessage(message);
            carp.setServerTime(serverTime);
            return carp;
        } catch (Exception e) {
            throw new RuntimeException("ConnAuthRespPayload unpack exception:" + e.getMessage());
        }
    }
}
