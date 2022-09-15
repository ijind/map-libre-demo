package ai.txai.push.handler;


import ai.txai.common.log.LOG;
import ai.txai.push.BlueGoPushClient;
import ai.txai.push.common.CommonStatusCodeEnum;
import ai.txai.push.common.PayloadType;
import ai.txai.push.listener.PushInitializedListener;
import ai.txai.push.listener.PushNotifyListener;
import ai.txai.push.payload.AckPayload;
import ai.txai.push.payload.ConnAuthRespPayload;
import ai.txai.push.payload.PushNotifyPayload;
import ai.txai.push.util.HeartbeatController;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * @Description BizProcessorHandler
 * @Author Jiaming.gong
 * @Date 08/01/2022
 */
public class BizProcessorHandler extends ChannelInboundHandlerAdapter {

    private BlueGoPushClient blueGoPushClient;
    private PushInitializedListener pushInitializedListener;
    private PushNotifyListener pushNotifyListener;
    private ChannelHandlerContext channelHandlerContext;

    public BizProcessorHandler(BlueGoPushClient blueGoPushClient) {
        this.blueGoPushClient = blueGoPushClient;
        this.pushInitializedListener = blueGoPushClient.getPushInitializedListener();
        this.pushNotifyListener = blueGoPushClient.getPushNotifyListener();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        log.info("server: {} is active", ctx.channel().remoteAddress());
        channelHandlerContext = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        try {
            int pktLen = byteBuf.readInt();
            byte ver = byteBuf.readByte();
            short payloadTypeCode = byteBuf.readShort();
            if (payloadTypeCode == PayloadType.ConnAuthResp.getCode()) {
                ConnAuthRespPayload carp = (ConnAuthRespPayload) new ConnAuthRespPayload().unpack(ctx, byteBuf);
                if (carp.getStatus() == CommonStatusCodeEnum.RPC_OK) {
                    pushInitializedListener.onInitialized(200, "Authentication of BlueGoPush connection success");
                    blueGoPushClient.setCurrentState(BlueGoPushClient.STATE_CONNECTED);
                    HeartbeatController.getInstance().startHeart(ctx);

                } else {
//                    log.warn("Authentication of BlueGoPush connection failed:{}, reason:{}", carp.getStatus().getCode(), carp.getMessage());
                    pushInitializedListener.onInitialized(403, String.format("Authentication of BlueGoPush connection failed: %s",carp.getMessage()));
                    blueGoPushClient.setCurrentState(BlueGoPushClient.STATE_CONNECT_FAILED);
                    blueGoPushClient.shutdownAllEventLoop();
                }
            }
            else if (payloadTypeCode == PayloadType.PushNotify.getCode()) {
                LOG.i("PushNotifyPayload", "onReceived----");
                PushNotifyPayload pushNotifyPayload = (PushNotifyPayload)new PushNotifyPayload().unpack(ctx, byteBuf);
                pushNotifyListener.onReceived(pushNotifyPayload);
                if (pushNotifyPayload.getNeedAck() == (byte) 1) {
                    sendAck(ctx, pushNotifyPayload.getMessageId());
                }
            }
            else {
//                log.warn("Received Unknown Payload");
            }

        } catch (Exception e) {
//            log.warn("Connection of BlueGoPush failed: " + e);
        } finally {
            byteBuf.skipBytes(byteBuf.readableBytes());
            ReferenceCountUtil.release(byteBuf);
        }
    }

    private void sendAck(ChannelHandlerContext ctx, String messageId) {
        AckPayload payload = new AckPayload();
        payload.setMessageId(messageId);
        payload.setAckTime(System.currentTimeMillis());
        ByteBuf buf = ctx.channel().alloc().buffer();
        if (ctx.channel().isActive()) {
            buf.writeInt(0);
            buf.writeByte(1);
            buf.writeShort(PayloadType.MessageACK.getCode());
            payload.pack(ctx, payload, buf);
            int pktLen = buf.writerIndex() - 4;
            buf.setInt(0, pktLen);
            ctx.channel().writeAndFlush(buf);
        } else {
            buf.release();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//        log.info("server: {} exceptionCaught: {}" , ctx.channel().remoteAddress(), cause.getLocalizedMessage());
        this.blueGoPushClient.reconnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
//        log.info("server: {} is inactive", ctx.channel().remoteAddress());
        this.blueGoPushClient.reconnect();
    }
}