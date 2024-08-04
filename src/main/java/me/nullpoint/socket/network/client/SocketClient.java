package me.nullpoint.socket.network.client;

import me.nullpoint.mod.modules.impl.client.IRC;
import me.nullpoint.mod.modules.impl.client.Notify;
import me.nullpoint.mod.modules.impl.client.OnlyNPIRC;
import me.nullpoint.socket.network.packet.Packet;
import me.nullpoint.socket.enums.ConnectionState;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.logging.log4j.LogManager;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author DreamDev
 * @since 4/7/2024
 */
public class SocketClient {
    private final String host;
    private final int port;
    private Channel channel;
    private ExecutorService executorService;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public SocketClient() {
        this("localhost", 11452);
    }

    public SocketClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public void start() {
        start(this.host, this.port);
    }

    public void start(String host, int port) {
        if (isConnected() || isConnecting()) {
            LogManager.getLogger().warn("Client is already connected or connecting.");
            return;
        }
        if (executorService.isShutdown() || executorService.isTerminated()) {
            executorService = Executors.newFixedThreadPool(2);
        }
        setConnectionState(ConnectionState.CONNECTING);
        executorService.submit(() -> {
            EventLoopGroup group = new NioEventLoopGroup();
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(group)
                        .channel(NioSocketChannel.class)
                        .handler(new ClientInitializer())
                        .option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);

                while (true) {
                    try {
                        ChannelFuture future = bootstrap.connect(host, port).sync();
                        this.channel = future.channel();
                        setConnectionState(ConnectionState.CONNECTED);

                        channel.closeFuture().sync();
                        break;
                    } catch (InterruptedException e) {
                        // 你帮忙改下
                        LogManager.getLogger().error( "Interrupted during connection. Exiting.");
                        break;
                    } catch (Exception e) {
                        LogManager.getLogger().error("Connection failed. Retrying in 5 seconds...");
                        Thread.sleep(5000);
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LogManager.getLogger().error("Thread was interrupted, Failed to complete operation.");
            } finally {
                setConnectionState(ConnectionState.DISCONNECTED);
                group.shutdownGracefully();
            }
        });
    }

    public void send(Packet packet) {
        if (channel != null && channel.isActive()) {
            channel.writeAndFlush(packet).addListener(future -> {
                if (!future.isSuccess()) {
                    LogManager.getLogger().error("Failed to send packet: " + future.cause().getMessage());
                }
            });
        } else {
            if (IRC.INSTANCE.isOn()) {
                sendNotify("Channel is not active. Cannot send packet.");
            }else {
                sendNotify("Please open IRC module and try again.");
            }
        }
    }

    public void disconnect() {
        if (channel != null && channel.isOpen()) {
            channel.close().addListener(future -> {
                if (future.isSuccess()) {
                    LogManager.getLogger().info("Disconnected successfully.");
                } else {
                    LogManager.getLogger().warn("Failed to disconnect.");
                }
                setConnectionState(ConnectionState.DISCONNECTED);
            });
            this.shutdown();
        } else {
            LogManager.getLogger().info("Channel is already closed or not initialized.");
        }
    }

    public void shutdown() {
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                LogManager.getLogger().error("Executor did not terminate in the allotted time.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public boolean isConnected() {
        return connectionState == ConnectionState.CONNECTED && this.channel.isActive();
    }

    public boolean isConnecting() {
        return connectionState == ConnectionState.CONNECTING;
    }

    public Channel getChannel() {
        return channel;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public ConnectionState getConnectionState() {
        return connectionState;
    }

    private void setConnectionState(ConnectionState newState) {
        this.connectionState = newState;
        LogManager.getLogger().info("Connection state changed to: " + newState);
    }
    public void sendNotify(String string){
        Notify.notifyList.add(new Notify.Notifys(string));
    }

}
