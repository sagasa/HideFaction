package hide.core.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

public class PacketSimpleCmd implements IMessage, IMessageHandler<PacketSimpleCmd, IMessage> {

	private static List<SimpleCmd> cmdList = new ArrayList<>();

	public static class SimpleCmd {
		private static byte count = 0;
		private final byte id;
		private Consumer<EntityPlayer> func;

		private SimpleCmd(Consumer<EntityPlayer> func) {
			this.func = func;
			id = count;
			cmdList.add(this);
			count++;
		}
	}

	/**サーバーで受信時のみPlayerが渡される*/
	public static SimpleCmd register(Consumer<EntityPlayer> func) {
		return new SimpleCmd(func);

	}

	public PacketSimpleCmd() {
	}

	private SimpleCmd value;

	public PacketSimpleCmd(SimpleCmd cmd) {
		value = cmd;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		value = cmdList.get(buf.readByte());
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(value.id);
	}

	@Override
	public IMessage onMessage(PacketSimpleCmd msg, MessageContext ctx) {
		System.out.println("Reseve " + msg.value);
		if (msg.value.func != null)
			if (ctx.side == Side.SERVER)
				msg.value.func.accept(ctx.getServerHandler().player);
			else
				msg.value.func.accept(null);

		return null;
	}

}
