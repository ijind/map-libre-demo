package ai.txai.push.payload;

import ai.txai.push.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * Time: 19/04/2022
 * Author Hay
 */
public class AckPayload extends AbstractMessagePayload{
    private String messageId;
    private long ackTime;

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public long getAckTime() {
        return ackTime;
    }

    public void setAckTime(long ackTime) {
        this.ackTime = ackTime;
    }

    @Override
    public void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf) {
        try {
            AckPayload ackP = (AckPayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, ackP.getMessageId());
            buf.writeLong(ackTime);
        } catch (Exception e) {
            throw new RuntimeException("HeartBeatPayload pack exception:" + e.getMessage());
        }
    }

    @Override
    public AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf) {

        return null;
    }
}
