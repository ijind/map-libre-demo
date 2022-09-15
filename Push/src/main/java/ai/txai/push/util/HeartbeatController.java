package ai.txai.push.util;

import com.blankj.utilcode.util.CloseUtils;
import com.blankj.utilcode.util.ThreadUtils;

import java.util.concurrent.TimeUnit;

import ai.txai.push.common.PayloadType;
import ai.txai.push.payload.HeartBeatPayload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;

/**
 * Time: 18/04/2022
 * Author Hay
 */
public class HeartbeatController {

    private static class Holder {
        private static final HeartbeatController instance = new HeartbeatController();
    }

    private ChannelHandlerContext context;

    private HeartbeatController() {

    }

    public static HeartbeatController getInstance() {
        return Holder.instance;
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (context != null && context.executor() != null) {
                context.executor().submit(() -> sendHeartbeatPacket(context));
                ThreadUtils.runOnUiThreadDelayed(runnable, 10000);
            }
        }
    };

    public void startHeart(ChannelHandlerContext context) {
        this.context = context;
        ThreadUtils.getMainHandler().removeCallbacks(runnable);
        ThreadUtils.runOnUiThreadDelayed(runnable, 10000);
    }

    public void stopHeart() {
        ThreadUtils.getMainHandler().removeCallbacks(runnable);
        context = null;
    }

    private static void sendHeartbeatPacket(ChannelHandlerContext ctx) {
        if (ctx == null || ctx.isRemoved() || ctx.executor().isShutdown()) {
            return;
        }
        HeartBeatPayload payload = new HeartBeatPayload();
        payload.setZero((byte) 0);
        ByteBuf buf = ctx.channel().alloc().buffer();
        if (ctx.channel().isActive()) {
            buf.writeInt(0);
            buf.writeByte(1);
            buf.writeShort(PayloadType.HeartBeat.getCode());
            payload.pack(ctx, payload, buf);
            int pktLen = buf.writerIndex() - 4;
            buf.setInt(0, pktLen);
            ctx.channel().writeAndFlush(buf);
        } else {
            buf.release();
        }
    }
}
