package ai.txai.push;

import android.text.TextUtils;

import ai.txai.common.json.GsonManager;
import ai.txai.common.log.LOG;
import ai.txai.database.user.User;
import ai.txai.push.common.PayloadType;
import ai.txai.push.entity.PushGateAddress;
import ai.txai.push.entity.UserInfo;
import ai.txai.push.handler.BizProcessorHandler;
import ai.txai.push.listener.PushInitializedListener;
import ai.txai.push.listener.PushNotifyListener;
import ai.txai.push.listener.PushStateListener;
import ai.txai.push.payload.ConnAuthPayload;
import ai.txai.push.payload.PushNotifyPayload;
import ai.txai.push.util.HeartbeatController;
import ai.txai.push.util.KeyManager;
import ai.txai.push.util.PushUtils;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BlueGoPushClient {
    public static final String TAG = BlueGoPushClient.class.getSimpleName();
    public static final int[] RECONNECT_GAT = {5, 10, 15, 30, 60};
    public static final int STATE_IDLE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_AUTHING = 2;
    public static final int STATE_CONNECTED = 3;
    public static final int STATE_CONNECT_FAILED = 4;
    public static final int STATE_RECONNECTING = 5;
    public static final int STATE_SHUTDOWN = 6;

    private volatile int oldState = STATE_IDLE;
    private volatile int currentState = STATE_IDLE;

    public static BlueGoPushClient sClient;

    // userInfo
    private UserInfo userInfo;

    // pushGateAddress
    private PushGateAddress pushGateAddress;

    // Netty config
    private NioEventLoopGroup workerGroup;
    private Bootstrap bootstrap;
    private ChannelFuture pushChannelFuture;
    private BizProcessorHandler bizProcessorHandler;

    // listener
    private PushInitializedListener pushInitializedListener = new PushInitializedListener() {
        @Override
        public void onInitialized(int code, String message) {

            for (PushInitializedListener listener : pushInitializedListeners) {
                listener.onInitialized(code, message);
            }
        }
    };
    private PushNotifyListener pushNotifyListener = new PushNotifyListener() {
        @Override
        public void onReceived(PushNotifyPayload pushNotifyPayload) {
            for (PushNotifyListener listener : pushNotifyListeners) {
                listener.onReceived(pushNotifyPayload);
            }
        }
    };

    // listener
    private Set<PushInitializedListener> pushInitializedListeners = new HashSet<>();
    private Set<PushNotifyListener> pushNotifyListeners = new HashSet<>();
    private Set<PushStateListener> pushStateListeners = new HashSet<>();

    public PushInitializedListener getPushInitializedListener() {
        return pushInitializedListener;
    }

    public PushNotifyListener getPushNotifyListener() {
        return pushNotifyListener;
    }

    // reconnect
    private AtomicInteger _reconnectCnt = new AtomicInteger(0);
    private AtomicBoolean _isReconnecting = new AtomicBoolean(false);
    private SingleThreadEventLoop _reconnectLoop = new DefaultEventLoop();

    public void registerPushInitializedListener(PushInitializedListener pushInitializedListener) {
        this.pushInitializedListeners.add(pushInitializedListener);
    }

    public void unregisterPushInitializedListener(PushInitializedListener pushInitializedListener) {
        this.pushInitializedListeners.remove(pushInitializedListener);
    }

    public void registerPushNotifyListener(PushNotifyListener pushNotifyListener) {
        this.pushNotifyListeners.add(pushNotifyListener);
    }

    public void unregisterPushNotifyListener(PushNotifyListener pushNotifyListener) {
        this.pushNotifyListeners.remove(pushNotifyListener);
    }

    public void registerPushStateListener(PushStateListener listener) {
        pushStateListeners.add(listener);
        listener.onStateChanged(oldState, currentState);
    }

    public void unregisterPushStateListener(PushStateListener listener) {
        pushStateListeners.remove(listener);
    }

    public BlueGoPushClient(UserInfo userInfo, PushGateAddress pushGateAddress) {
        Objects.requireNonNull(userInfo, "UserInfo is required");
        this.userInfo = userInfo;
        this.pushGateAddress = pushGateAddress;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void buildNewConnect() {
        this.connect();
    }

    public void connect() {
        if (currentState == STATE_CONNECTED || currentState == STATE_SHUTDOWN) {
            return;
        }
        setCurrentState(STATE_CONNECTING);
        String pushGateAddressString = String.format("%s:%s", pushGateAddress.getIp(), pushGateAddress.getPort());
        workerGroup = new NioEventLoopGroup();
        bootstrap = new Bootstrap();
        bizProcessorHandler = new BizProcessorHandler(this);
        bootstrap.group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 4, 0, 0));
                        pipeline.addLast(bizProcessorHandler);
                    }
                });
        try {
            pushChannelFuture = bootstrap.connect(pushGateAddress.getIp(), pushGateAddress.getPort()).sync();
            pushChannelFuture.addListener((ChannelFutureListener) future -> {
                if (_isReconnecting.get()) {
                    _isReconnecting.set(false);
                }
                if (future.isSuccess()) {
                    pushInitializedListener.onInitialized(200, String.format("Connection of BlueGoPush: %s success", pushGateAddressString));
                    setCurrentState(STATE_AUTHING);
                    // send auth
                    sendAuthConnPacket();
                } else {
                    pushInitializedListener.onInitialized(503, String.format("Connection of BlueGoPush: %s failed", pushGateAddressString));
                    setCurrentState(STATE_CONNECT_FAILED);
                    // reconnect when connect failed
                    reconnect();
                }
            });
            // pushChannelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            setCurrentState(STATE_CONNECT_FAILED);
            pushInitializedListener.onInitialized(503, String.format("Connection of BlueGoPush: %s failed", pushGateAddressString));
            if (_isReconnecting.get()) {
                _isReconnecting.set(false);
            }
            reconnect();
        }
    }

    public void reconnect() {
        if (_isReconnecting.get()) return;
        setCurrentState(STATE_RECONNECTING);
        _isReconnecting.set(true);
        int i = _reconnectCnt.get();
        int seconds = i < RECONNECT_GAT.length ? RECONNECT_GAT[i] : 60;

        _reconnectLoop.schedule(() -> {
            connect();
        }, seconds, TimeUnit.SECONDS);
    }

    private boolean isWriteAble() {
        if (pushChannelFuture == null || pushChannelFuture.channel() == null || !pushChannelFuture.channel().isActive()) {
            reconnect();
        }
        return true;
    }

    public void shutdownAllEventLoop() {
        if (_reconnectLoop != null) {
            _reconnectLoop.shutdownGracefully(2, 5, TimeUnit.SECONDS);
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(1, 1, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        setCurrentState(STATE_SHUTDOWN);
        HeartbeatController.getInstance().stopHeart();
        if (pushChannelFuture != null) {
            pushChannelFuture.channel().close().syncUninterruptibly();
        }
        shutdownAllEventLoop();
    }

    private void sendAuthConnPacket() {
        ByteBuf buf = this.pushChannelFuture.channel().alloc().buffer();
        if (this.pushChannelFuture.channel().isActive()) {
            ConnAuthPayload connAuthPayload = new ConnAuthPayload();
            connAuthPayload.setUserId(userInfo.getUserId());
            connAuthPayload.setEndpoint(userInfo.getEndpoint());
            byte[] keys = userInfo.getAuthKey().getBytes(StandardCharsets.UTF_8);
            connAuthPayload.setAuthKey(KeyManager.getAuthKey(keys));

            connAuthPayload.setClientInfo(PushUtils.defaultClientInfo());

            buf.writeInt(0);
            buf.writeByte(1);
            buf.writeShort(PayloadType.ConnAuth.getCode());
            connAuthPayload.pack(null, connAuthPayload, buf);
            int pktLen = buf.writerIndex() - 4;
            buf.setInt(0, pktLen);
            this.pushChannelFuture.channel().writeAndFlush(buf);
        } else {
            buf.release();
        }
    }

    public void setCurrentState(int currentState) {
        LOG.d(TAG, "currentState change from %s to %s", this.currentState, currentState);
        this.oldState = currentState;
        this.currentState = currentState;
        for (PushStateListener listener : pushStateListeners) {
            listener.onStateChanged(this.oldState, this.currentState);
        }
    }

    public int getCurrentState() {
        return currentState;
    }

    public static void startPush(UserInfo userInfo, String ip, int port) {
        PushGateAddress pushGateAddress = new PushGateAddress(ip, port);
        sClient = new BlueGoPushClient(userInfo, pushGateAddress);
        sClient.registerPushInitializedListener(new PushInitializedListener() {
            @Override
            public void onInitialized(int code, String message) {
                LOG.i("PushTest", "PushInitializedListener received code: %s, message: %s", code, message);
            }
        });
        sClient.registerPushNotifyListener(new PushNotifyListener() {
            @Override
            public void onReceived(PushNotifyPayload pushNotifyPayload) {
                LOG.i("PushTest", "PushNotifyListener received: %s", GsonManager.GsonString(pushNotifyPayload));
            }
        });
        sClient.buildNewConnect();
    }

    public static void stopPush(User user) {
        if (sClient != null) {
            UserInfo userInfo = sClient.getUserInfo();
            if (userInfo == null || user == null || TextUtils.equals(userInfo.getUserId(), user.getUid())) {
                sClient.shutdown();
                sClient = null;
            }
        }
    }

    public static BlueGoPushClient currentClient() {
        return sClient;
    }
}