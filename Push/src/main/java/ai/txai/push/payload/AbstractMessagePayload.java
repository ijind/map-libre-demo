package ai.txai.push.payload;

import ai.txai.common.json.GsonManager;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.Serializable;

/**
 * @author Admin
 * @className AbstractPayload
 * @date 11/29/21 12:11
 * @description
 */
public abstract class AbstractMessagePayload implements Serializable {
    /**
     * 编码
     *
     * @param ctx     ChannelHandlerContext
     * @param payload AbstractMessagePayload
     * @param buf     ByteBuf
     */
    public abstract void pack(ChannelHandlerContext ctx, AbstractMessagePayload payload, ByteBuf buf);

    /**
     * 解码
     *
     * @param ctx ChannelHandlerContext
     * @param buf ByteBuf
     * @return AbstractMessagePayload
     */
    public abstract AbstractMessagePayload unpack(ChannelHandlerContext ctx, ByteBuf buf);

    public String string() {
        return GsonManager.getGson().toJson(this);
    }

    public String toJsonString() {
        return string();
    }

    @Override
    public String toString() {
        return this.string();
    }
}
