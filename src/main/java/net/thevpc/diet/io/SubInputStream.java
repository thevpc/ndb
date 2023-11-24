/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.diet.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author vpc
 */
class SubInputStream extends InputStream {

    private StoreInputStream is;

    public SubInputStream(StoreInputStream is) {
        this.is = is;
    }

    long unread = -1;
    byte[] buffer = new byte[1024];
    int offset = -1;
    int len = -1;
    boolean completed = false;

    @Override
    public int read(byte[] b, int off, int len0) throws IOException {
        if (completed) {
            return 0;
        }
        if (len <= 0 || offset >= len) {
            prepareNewBloc();
            if (unread <= 0) {
                completed = true;
                return 0;
            }
        }
        if (len0 <= 0) {
            return 0;
        } else {
            int ll = Math.min(len0, len);
            System.arraycopy(buffer, offset, b, off, ll);
            offset += ll;
            unread -= ll;
            return ll;
        }
    }

    @Override
    public int read() throws IOException {
        if (completed) {
            return -1;
        }
        if (len <= 0 || offset >= len) {
            prepareNewBloc();
            if (unread <= 0) {
                completed = true;
                return -1;
            }
        }
        byte x = buffer[offset];
        offset++;
        unread--;
        return x & 0xff;
    }

    public void prepareNewBloc() throws IOException {
        if (unread <= 0) {
            long count = is.readNonNullableLong();
            if (count < 0) {
                unread = 0;
                len = 0;
                offset = 0;
            } else {
                unread = count;
                int max = unread < buffer.length ? ((int) unread) : buffer.length;
                len = is.read(buffer, 0, max);
                offset = 0;
            }
        } else {
            int max = unread < buffer.length ? ((int) unread) : buffer.length;
            len = is.read(buffer, 0, max);
            offset = 0;
        }
    }

}
