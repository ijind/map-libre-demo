package ai.txai.push.payload;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author Admin
 * @className HeartBeatPayload
 * @date 11/30/21 15:37
 * @description
 */
public class HeartBeatPayload extends AbstractMessagePayload {
    private byte zero;

    public byte getZero() {
        return zero;
    }

    public void setZero(byte zero) {
        this.zero = zero;
    }

    /**
     * 编码
     *
     * @param ctx     ChannelHandlerContext
     * @param payload AbstractMessagePayload
     * @param buf     ByteBuf
     */
    @Override
    public void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf) {
        try {
            HeartBeatPayload hbp = (HeartBeatPayload) payload;
            buf.writeByte(0);
        } catch (Exception e) {
            throw new RuntimeException("HeartBeatPayload pack exception:" + e.getMessage());
        }
    }

    /**
     * 解码
     *
     * @param ctx ChannelHandlerContext
     * @param buf ByteBuf
     * @return AbstractMessagePayload
     */
    @Override
    public AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf) {
        return null;
    }
}
