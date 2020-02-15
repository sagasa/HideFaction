package hide.region.network;

import io.netty.buffer.ByteBuf;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketRegionEdit implements IMessage, IMessageHandler<PacketRegionEdit, IMessage> {

	@Override
	public void fromBytes(ByteBuf buf) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public void toBytes(ByteBuf buf) {
		// TODO 自動生成されたメソッド・スタブ

	}

	@Override
	public IMessage onMessage(PacketRegionEdit message, MessageContext ctx) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}
}
