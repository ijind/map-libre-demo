//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package ai.txai.push.util;

import java.io.IOException;
import java.io.OutputStream;

public class FastByteArrayOutputStream extends OutputStream {
    private final FastByteBuffer buffer;

    public FastByteArrayOutputStream() {
        this(1024);
    }

    public FastByteArrayOutputStream(int size) {
        this.buffer = new FastByteBuffer(size);
    }

    public void write(byte[] b, int off, int len) {
        this.buffer.append(b, off, len);
    }

    public void write(int b) {
        this.buffer.append((byte)b);
    }

    public int size() {
        return this.buffer.size();
    }

    public void close() {
    }

    public void reset() {
        this.buffer.reset();
    }

    public void writeTo(OutputStream out) throws IOException {
        int index = this.buffer.index();

        for (int i = 0; i < index; ++i) {
            byte[] buf = this.buffer.array(i);
            out.write(buf);
        }

        out.write(this.buffer.array(index), 0, this.buffer.offset());
    }

    public byte[] toByteArray() {
        return this.buffer.toArray();
    }
}
