package com.hxl.plugin.scheduledinvokestarter;


import com.hxl.plugin.scheduledinvokestarter.json.JsonMapper;
import com.hxl.plugin.scheduledinvokestarter.model.pack.CommunicationPackage;
import com.hxl.plugin.scheduledinvokestarter.utils.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

public class PluginCommunication implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginCommunication.class);

    public interface MessageCallback {
        void pluginMessage(String msg);
    }

    private final MessageCallback messageCallback;
    private SocketChannel socketChannel;
    private Selector selector;
    private JsonMapper jsonMapper;

    public PluginCommunication(MessageCallback messageCallback) {
        this.messageCallback = messageCallback;
    }

    public void startServer(int port) throws Exception {
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.bind(new InetSocketAddress(port));
        serverSocketChannel.configureBlocking(false);
        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        new Thread(this).start();
    }

    private void handleAccept(SelectionKey key) throws IOException {
        ServerSocketChannel serverChannel = (ServerSocketChannel) key.channel();
        SocketChannel clientChannel = serverChannel.accept();
        clientChannel.configureBlocking(false);
        clientChannel.register(selector, SelectionKey.OP_READ);
    }

    private byte[] getByteAndClose(SelectionKey key) {
        key.cancel();
        if (key.attachment() == null) return null;
        return ((ByteArrayOutputStream) key.attachment()).toByteArray();
    }

    private byte[] handleRead(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        int read = channel.read(buffer);
        if (read <= 0) return getByteAndClose(key);
        if (key.attachment() == null) key.attach(new ByteArrayOutputStream());
        ((Buffer) buffer).flip();
        int remainingBytes = buffer.remaining();
        byte[] data = new byte[remainingBytes];
        System.arraycopy(buffer.array(), buffer.position(), data, 0, remainingBytes);
        ((ByteArrayOutputStream) key.attachment()).write(data);
        return null;
    }

    private void invoke(byte[] data) {
        if (messageCallback != null) messageCallback.pluginMessage(new String(data));
    }

    @Override
    public void run() {
        while (true) {
            try {
                selector.select();
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    iterator.remove();
                    if (key.isAcceptable()) {
                        handleAccept(key);
                    } else if (key.isReadable()) {
                        // 处理读取事件
                        byte[] bytes = handleRead(key);
                        if (bytes != null) invoke(bytes);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void send(CommunicationPackage communicationPackage) {
        String port = System.getProperty("hxl.spring.invoke.port");
        System.out.println(port +"send");
        if (port == null) {
            return;
        }
        try (SocketChannel pluginServer = SocketChannel.open(new InetSocketAddress("localhost", Integer.parseInt(port)))) {
            String json = communicationPackage.toJson();
            if (SystemUtils.isDebug()) System.out.println(json);
            pluginServer.write(Charset.defaultCharset().encode(json));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
