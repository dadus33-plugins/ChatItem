package me.dadus33.chatitem.chatmanager.v1.packets.custom.channel;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.bukkit.Bukkit;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent.ContentModifier;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.CustomPacketManager;

public class ChannelInboundHandler extends ChannelInboundHandlerAdapter {

	private CustomPacketManager packetManager;
	private List<ChannelPipeline> pipelines = new ArrayList<>();
	
	public ChannelInboundHandler(CustomPacketManager packetManager) {
		this.packetManager = packetManager;
	}
	
	public void clean() {
		pipelines.forEach(ChannelPipeline::removeFirst);
		pipelines.clear();
	}
	
	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		ctx.fireChannelRead(msg);
		ChannelPipeline pipe = ((Channel) msg).pipeline();
		pipelines.add(pipe);
		pipe.addFirst(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel channel) {
				try {
					channel.eventLoop().submit(() -> {
						try {
							ChannelHandler interceptor = channel.pipeline().get(ChannelAbstract.KEY_HANDSHAKE);
							// Inject our packet interceptor
							if (interceptor == null) {
								interceptor = new ChannelHandlerHandshakeReceive(channel);
								channel.pipeline().addBefore("packet_handler", ChannelAbstract.KEY_HANDSHAKE, interceptor);
							}
							return interceptor;
						} catch (IllegalArgumentException e) {
							// Try again
							return channel.pipeline().get(ChannelAbstract.KEY_HANDSHAKE);
						}
					});
				} catch (Exception e) {
					packetManager.getPlugin().getLogger().log(Level.SEVERE, "Cannot inject incoming channel " + channel, e);
				}
			}
		});
	}
	
	public class ChannelHandlerHandshakeReceive extends ChannelInboundHandlerAdapter {
		
		private Channel channel;
		
		public ChannelHandlerHandshakeReceive(Channel channel) {
			this.channel = channel;
		}

		@Override
		public void channelRead(ChannelHandlerContext ctx, Object packet) {
			try {
				PacketType packetType = PacketType.getType(packet.getClass().getSimpleName());
				if(packetType != null && packetType == PacketType.Handshake.IS_SET_PROTOCOL) {
					ContentModifier<Integer> ints = new PacketContent(packet).getIntegers();
					int possibleProtocol = ints.readSafely(0, 0);
					if(possibleProtocol == Bukkit.getPort())
						possibleProtocol = ints.readSafely(1, 0);
					packetManager.protocolVersionPerChannel.put(channel, possibleProtocol);
				}
				super.channelRead(ctx, packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
}
