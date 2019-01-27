/*
 * This file is part of resonator.
 *
 * resonator is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * resonator is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with resonator.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.alexhyisen.resonator.core;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

public class HttpService {
    private final EventLoopGroup eventGroup = new NioEventLoopGroup();
    private final Consumer<Map<String, String>> dataHandler;

    public HttpService(Consumer<Map<String, String>> dataHandler) {
        this.dataHandler = dataHandler;
    }

    public ChannelFuture start(InetSocketAddress address) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(eventGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline()
                                .addLast(new HttpServerCodec())
                                .addLast(new ChunkedWriteHandler())
                                .addLast(new HttpObjectAggregator(65536))
                                .addLast(new HttpRequestHandler(dataHandler));
                    }
                });
        return bootstrap.bind(address);
    }

    public Future<?> stop() {
        return eventGroup.shutdownGracefully();
    }

    public static void main(String[] args) {
        Instant timestamp = Instant.now();
        var port = 8080;
        System.out.println("port = " + port);
        ChannelFuture future = new HttpService(map -> map.forEach((k, v) -> System.out.println(k + " = " + v)))
                .start(new InetSocketAddress(port));
        future.syncUninterruptibly();
        System.out.println("launched in " + Duration.between(timestamp, Instant.now()).toMillis() + " ms");
    }
}
