package hide.core.network;

import org.apache.commons.lang3.ArrayUtils;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSimpleCmd implements IMessage, IMessageHandler<PacketSimpleCmd, IMessage> {

	public enum Cmd {
		S;
	}

	public PacketSimpleCmd() {
	}

	private Cmd value;

	public PacketSimpleCmd(Cmd cmd) {
		value = cmd;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		value = Cmd.values()[buf.readByte()];
	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeByte(ArrayUtils.indexOf(Cmd.values(), value));
	}

	@Override
	public IMessage onMessage(PacketSimpleCmd message, MessageContext ctx) {
		System.out.println("Reseve "+message.value);
		return null;
	}

}
