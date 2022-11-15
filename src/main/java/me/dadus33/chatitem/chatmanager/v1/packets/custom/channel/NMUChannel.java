package me.dadus33.chatitem.chatmanager.v1.packets.custom.channel;

import static me.dadus33.chatitem.utils.PacketUtils.getPlayerConnection;

import java.lang.reflect.Field;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;

import org.bukkit.entity.Player;

import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketContent;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.CustomPacketManager;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;
import net.minecraft.util.io.netty.channel.Channel;
import net.minecraft.util.io.netty.channel.ChannelFuture;
import net.minecraft.util.io.netty.channel.ChannelHandler;
import net.minecraft.util.io.netty.channel.ChannelHandlerContext;
import net.minecraft.util.io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelInitializer;
import net.minecraft.util.io.netty.channel.ChannelOutboundHandlerAdapter;
import net.minecraft.util.io.netty.channel.ChannelPipeline;
import net.minecraft.util.io.netty.channel.ChannelPromise;

public class NMUChannel extends ChannelAbstract {

	private ChannelInboundHandlerAdapter boundHandler;
	private ChannelPipeline pipeline;
	
	public NMUChannel(CustomPacketManager customPacketManager) {
		super(customPacketManager);
		try {
			Object dedicatedSrv = PacketUtils.getCraftServer();
			Object mcServer = dedicatedSrv.getClass().getMethod("getServer").invoke(dedicatedSrv);
			Object co = ReflectionUtils.getFirstWith(mcServer, PacketUtils.getNmsClass("MinecraftServer", "server."), PacketUtils.getNmsClass("ServerConnection", "network."));
			List<ChannelFuture> g = (List<ChannelFuture>) ReflectionUtils.getObject(co, "g", "e");
			g.forEach((channelFuture) -> {
				pipeline = channelFuture.channel().pipeline();
				pipeline.addFirst(boundHandler = new ChannelInboundHandlerAdapter() {
					@Override
					public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
						((Channel) msg).pipeline().addFirst(new ChannelInitializer<Channel>() {
							@Override
							protected void initChannel(Channel channel) {
								try {
									channel.eventLoop().submit(() -> {
										try {
											ChannelHandler interceptor = channel.pipeline().get(KEY_HANDSHAKE);
											// Inject our packet interceptor
											if (interceptor == null) {
												interceptor = new ChannelHandlerHandshakeReceive(channel);
												channel.pipeline().addBefore("packet_handler", KEY_HANDSHAKE, interceptor);
											}
											return interceptor;
										} catch (IllegalArgumentException e) {
											// Try again
											return channel.pipeline().get(KEY_HANDSHAKE);
										}
									});
								} catch (Exception e) {
									getPacketManager().getPlugin().getLogger().log(Level.SEVERE, "Cannot inject incomming channel " + channel, e);
								}
							}
						});
						ctx.fireChannelRead(msg);
					}
				});
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void stopPipelines() {
		pipeline.remove(boundHandler);
	}

	@Override
	public void addChannel(final Player player, String endChannelName) {
		getOrCreateAddChannelExecutor().execute(() -> {
			try {
				Channel channel = getChannel(player);
				// Managing outgoing packet (to the player)
				channel.pipeline().addAfter(KEY_HANDLER_SERVER, KEY_SERVER + endChannelName, new ChannelHandlerSent(player));
			} catch (NoSuchElementException e) {
				// appear when the player's channel isn't accessible because of reload.
				getPacketManager().getPlugin().getLogger().warning("Please, don't use reload, this can produce some problem. Currently, " + player.getName() + " isn't fully checked because of that. More details: " + e.getMessage() + " (NoSuchElementException)");
			} catch (IllegalArgumentException e) {
				if(e.getMessage().contains("Duplicate")) {
					removeChannel(player, endChannelName);
					addChannel(player, endChannelName);
				} else
					getPacketManager().getPlugin().getLogger().severe("Error while loading Packet channel. " + e.getMessage() + ". Please, prefer restart than reload.");
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
	}

	@Override
	public void removeChannel(Player player, String endChannelName) {
		getOrCreateRemoveChannelExecutor().execute(() -> {
			try {
				final Channel channel = getChannel(player);
				if(channel.pipeline().get(KEY_SERVER + endChannelName) != null)
					channel.pipeline().remove(KEY_SERVER + endChannelName);
			} catch (Exception e) {}
		});
	}

	@Override
	public Channel getChannel(Player p) throws Exception {
		Object playerConnection = getPlayerConnection(p);
		Object networkManager = playerConnection.getClass().getField("networkManager").get(playerConnection);
		
		for (Field field : networkManager.getClass().getDeclaredFields())
			if (field.getType().equals(Channel.class)) {
				field.setAccessible(true);
				return (Channel) field.get(networkManager);
			}
		return null;
	}

	private class ChannelHandlerSent extends ChannelOutboundHandlerAdapter {

		private final Player owner;

		public ChannelHandlerSent(Player player) {
			this.owner = player;
		}
		
		@Override
		public void write(ChannelHandlerContext ctx, Object packet, ChannelPromise promise) throws Exception {
			ChatItemPacket nextPacket = getPacketManager().onPacketSent(PacketType.getType(packet.getClass().getSimpleName()), owner, packet);
			if(nextPacket != null && nextPacket.isCancelled())
				return;
			super.write(ctx, nextPacket == null ? packet : nextPacket.getPacket(), promise);
		}
	}

	private class ChannelHandlerHandshakeReceive extends ChannelInboundHandlerAdapter {

		private final Channel channel;
		
		public ChannelHandlerHandshakeReceive(Channel channel) {
			this.channel = channel;
		}
		
		@Override
		public void channelRead(ChannelHandlerContext ctx, Object packet) {
			try {
				PacketType packetType = PacketType.getType(packet.getClass().getSimpleName());
				if(packetType != null && packetType == PacketType.Handshake.IS_SET_PROTOCOL) {
					getPacketManager().protocolVersionPerChannel.put(channel, new PacketContent(packet).getIntegers().readSafely(1, 0));
				}
				super.channelRead(ctx, packet);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
