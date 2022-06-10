package me.dadus33.chatitem.chatmanager.v1.packets.custom.channel;

import static me.dadus33.chatitem.utils.PacketUtils.getPlayerConnection;

import java.util.List;
import java.util.NoSuchElementException;

import org.bukkit.entity.Player;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import me.dadus33.chatitem.ChatItem;
import me.dadus33.chatitem.chatmanager.v1.packets.ChatItemPacket;
import me.dadus33.chatitem.chatmanager.v1.packets.PacketType;
import me.dadus33.chatitem.chatmanager.v1.packets.custom.CustomPacketManager;
import me.dadus33.chatitem.utils.PacketUtils;
import me.dadus33.chatitem.utils.ReflectionUtils;
import me.dadus33.chatitem.utils.Version;

public class INC2Channel extends ChannelAbstract {
	
	private final ChannelInboundHandler boundHandler;
	private ChannelPipeline pipeline;
	
	@SuppressWarnings("unchecked")
	public INC2Channel(CustomPacketManager customPacketManager) {
		super(customPacketManager);
		boundHandler = new ChannelInboundHandler(customPacketManager);
		try {
			Object mcServer = ReflectionUtils.callMethod(PacketUtils.getCraftServer(), Version.getVersion().equals(Version.V1_17) ? "getServer" : "b");
			Object co = ReflectionUtils.getFirstWith(mcServer, PacketUtils.getNmsClass("MinecraftServer", "server."), PacketUtils.getNmsClass("ServerConnection", "server.network."));
			((List<ChannelFuture>) ReflectionUtils.getObject(co, "f")).forEach((channelFuture) -> {
				pipeline = channelFuture.channel().pipeline();
				pipeline.addFirst(boundHandler);
			});
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	protected void stopPipelines() {
		pipeline.remove(boundHandler);
		boundHandler.clean();
	}

	@Override
	public void addChannel(final Player player, String endChannelName) {
		getOrCreateAddChannelExecutor().execute(() -> {
			if(!player.isOnline())
				return;
			try {
				Channel channel = getChannel(player);
				// Managing outgoing packet (to the player)
				channel.pipeline().addAfter(KEY_HANDLER_SERVER, KEY_SERVER + endChannelName, new ChannelHandlerSent(player));
			} catch (NoSuchElementException e) {
				// appear when the player's channel isn't accessible because of reload.
				getPacketManager().getPlugin().getLogger().warning("Please, don't use reload, this can produce some problem. Currently, " + player.getName() + " isn't fully checked because of that. More details: " + e.getMessage() + " (NoSuchElementException)");
			} catch (IllegalArgumentException e) {
				if(e.getMessage().contains("Duplicate handler")) {
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
			} catch (Exception e) {
				ChatItem.getInstance().getLogger().warning("Failed to remove channel for " + player.getName() + ". Reason: " + e.getMessage() + " (" + e.getStackTrace()[0].toString() + ")");
			}
		});
	}

	@Override
	public Channel getChannel(Player p) throws Exception {
		Object playerConnection = ReflectionUtils.getObject(getPlayerConnection(p), Version.getVersion().isNewerOrEquals(Version.V1_19) ? "b" : "a");
		return ReflectionUtils.getFirstWith(playerConnection, playerConnection.getClass(), Channel.class);//(Channel) networkManager.getClass().getDeclaredField("channel").get(networkManager);
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
}
