package ai.txai.push.util;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

import ai.txai.common.json.GsonManager;
import io.netty.buffer.ByteBuf;

public class ByteBufUtil {
    /**
     * 从byteBuf 读长度为byte的字符串
     **/
    public static String readStringForByteLength(ByteBuf byteBuf) {
        byte strLen = byteBuf.readByte();
        return readString(byteBuf, strLen);
    }

    /**
     * 从byteBuf 读长度为short的字符串
     *
     * @param byteBuf
     * @return
     */
    public static String readStringForShortLength(ByteBuf byteBuf) {
        short strLen = byteBuf.readShort();
        return readString(byteBuf, strLen);
    }

    /**
     * 从byteBuf 读长度为int的字符串
     *
     * @param byteBuf
     * @return
     */
    public static String readStringForIntLength(ByteBuf byteBuf) {
        int strLen = byteBuf.readInt();
        return readString(byteBuf, strLen);
    }


    /**
     * 读一个对象，因为对象都是用JSONString编码的
     *
     * @param byteBuf
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T readObjForIntLength(ByteBuf byteBuf, Class<T> clazz) {
        int strLen = byteBuf.readInt();
        if (strLen == 0) return null;
        String jsonString = readString(byteBuf, strLen);
        Gson gson = GsonManager.getGson();
        return gson.fromJson(gson.toJson(jsonString), clazz);
    }

    private static String readString(ByteBuf buf, int strLen) {
        if (strLen == 0) return "";
        byte[] str = new byte[strLen];
        buf.readBytes(str);
        return new String(str, StandardCharsets.UTF_8);
    }


    private static void writeString(ByteBuf buf, String str) {
        if (str.getBytes().length > 0) {
            buf.writeBytes(str.getBytes());
        }
    }

    public static int writeStringForByteLength(ByteBuf byteBuf, String str) {
        byteBuf.writeByte(str.getBytes().length);
        writeString(byteBuf, str);
        return str.getBytes().length + 1;
    }

    public static int writeStringForShortLength(ByteBuf byteBuf, String str) {
        byteBuf.writeShort(str.getBytes().length);
        writeString(byteBuf, str);
        return str.getBytes().length + 2;
    }

    public static int writeStringForIntLength(ByteBuf byteBuf, String str) {
        byteBuf.writeInt(str.getBytes().length);
        writeString(byteBuf, str);
        return str.getBytes().length + 4;
    }

    public static int writeObjectForIntLength(ByteBuf byteBuf, Object obj) {
        String jsonString = GsonManager.getGson().toJson(obj);
        return writeStringForIntLength(byteBuf, jsonString);
    }


    public static byte[] unGzip(InputStream in, int length)  {
        GZIPInputStream gzi = null;

        FastByteArrayOutputStream bos = null;
        try {
            gzi = in instanceof GZIPInputStream ? (GZIPInputStream)in : new GZIPInputStream(in);
            bos = new FastByteArrayOutputStream(length);
            copy(gzi, bos, 8192);
        } catch (IOException e) {
            return null;
        } finally {
            try {
                gzi.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return bos.toByteArray();
    }


    public static long copy(InputStream in, OutputStream out, int bufferSize) {
        if (bufferSize <= 0) {
            bufferSize = 8192;
        }

        byte[] buffer = new byte[bufferSize];

        long size = 0L;

        try {
            int readSize;
            while((readSize = in.read(buffer)) != -1) {
                out.write(buffer, 0, readSize);
                size += (long)readSize;
            }

            out.flush();
        } catch (IOException ignored) {

        }
        return size;
    }
}
