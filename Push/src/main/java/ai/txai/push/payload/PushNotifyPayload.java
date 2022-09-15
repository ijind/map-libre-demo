package ai.txai.push.payload;

import com.google.gson.Gson;

import java.io.ByteArrayInputStream;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import ai.txai.common.json.GsonManager;
import ai.txai.push.common.NotifyClassifyEnum;
import ai.txai.push.util.ByteBufUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

/**
 * 通知数据结构
 *
 * @author <a href="{+}jiaming.gong@ctechm.com+"></a>
 * @version 1.0.0 2022/3/2
 */
public class PushNotifyPayload extends AbstractMessagePayload {
    // 压缩机
    private static int needCompressSize = 1024;

    private byte needAck;
    private String messageId;
    private NotifyClassifyEnum notifyClassify;
    private String toUserId;
    private Object messageBody;
    private long serverTime;

    public static int getNeedCompressSize() {
        return needCompressSize;
    }

    public static void setNeedCompressSize(int needCompressSize) {
        PushNotifyPayload.needCompressSize = needCompressSize;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public NotifyClassifyEnum getNotifyClassify() {
        return notifyClassify;
    }

    public void setNotifyClassify(NotifyClassifyEnum notifyClassify) {
        this.notifyClassify = notifyClassify;
    }

    public String getToUserId() {
        return toUserId;
    }

    public void setToUserId(String toUserId) {
        this.toUserId = toUserId;
    }

    public Object getMessageBody() {
        return messageBody;
    }

    public void setMessageBody(Object messageBody) {
        this.messageBody = messageBody;
    }

    public long getServerTime() {
        return serverTime;
    }

    public void setServerTime(long serverTime) {
        this.serverTime = serverTime;
    }

    public byte getNeedAck() {
        return needAck;
    }

    public void setNeedAck(byte needAck) {
        this.needAck = needAck;
    }

    public <T> T getMessageBody(Class<T> type) {
        Gson gson = GsonManager.getGson();
        String s = gson.toJson(messageBody);
        return gson.fromJson(s, type);
    }

    public static class MessageBody implements Serializable {

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
            PushNotifyPayload pmp = (PushNotifyPayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, pmp.getMessageId());
            buf.writeByte(pmp.getNotifyClassify().getCode());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getToUserId());
            buf.writeLong(pmp.getServerTime());
            byte messageFlag = 2;
            byte[] finallyBody = GsonManager.getGson().toJson(pmp.getMessageBody()).getBytes(StandardCharsets.UTF_8);
            if (finallyBody.length > PushNotifyPayload.needCompressSize) {
                messageFlag = 3;
                // 压缩
//                finallyBody = ZipUtil.gzip(finallyBody);

            }
            buf.writeByte(messageFlag);
            buf.writeBytes(finallyBody);
        } catch (Exception e) {
            throw new RuntimeException("PushMessage pack exception: " + e.getMessage());
        }
    }


    /**
     * 手动编码
     *
     * @param payload
     * @param buf
     * @param encMsgKey
     */
    public void encode(AbstractMessagePayload payload, ByteBuf buf, byte[] encMsgKey, byte[] iv) {
        try {
            PushNotifyPayload pmp = (PushNotifyPayload) payload;
            ByteBufUtil.writeStringForByteLength(buf, pmp.getMessageId());
            buf.writeByte(pmp.getNotifyClassify().getCode());
            ByteBufUtil.writeStringForByteLength(buf, pmp.getToUserId());
            buf.writeLong(pmp.getServerTime());
            byte messageFlag = 2;
            byte[] finallyBody = GsonManager.getGson().toJson(pmp.getMessageBody()).getBytes(StandardCharsets.UTF_8);
            if (finallyBody.length > PushNotifyPayload.needCompressSize) {
                messageFlag = 3;
                // 压缩
//                finallyBody = ZipUtil.gzip(finallyBody);
            }
            buf.writeByte(messageFlag);
            buf.writeBytes(finallyBody);
        } catch (Exception e) {
            throw e;
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
            PushNotifyPayload pmp = new PushNotifyPayload();
            byte needAck = buf.readByte();
            pmp.setNeedAck(needAck);
            String messageId = ByteBufUtil.readStringForByteLength(buf);
            byte classify = buf.readByte();
            String userId = ByteBufUtil.readStringForByteLength(buf);
            long serverTime = buf.readLong();
            byte flag = buf.readByte();
            byte[] body = new byte[buf.readableBytes()];
            buf.readBytes(body);
            if (flag == 3) {
                // 解压缩
                body = ByteBufUtil.unGzip(new ByteArrayInputStream(body), body.length);
            }

            pmp.setMessageId(messageId);
            pmp.setNotifyClassify(NotifyClassifyEnum.fromCode(classify));
            pmp.setToUserId(userId);
            pmp.setServerTime(serverTime);
            String json = new String(body, StandardCharsets.UTF_8);
            pmp.setMessageBody(GsonManager.parseJson(json));
            return pmp;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("PushNotify unpack exception:" + e.getMessage());
        }

    }
}
