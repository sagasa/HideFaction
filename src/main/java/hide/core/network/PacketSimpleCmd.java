package hide.core.network;

import org.apache.commons.lang3.ArrayUtils;

import hide.region.gui.RegionEditGUI;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class PacketSimpleCmd implements IMessage, IMessageHandler<PacketSimpleCmd, IMessage> {

	public enum Cmd {
		S, OpenRegionGUI;
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
	public IMessage onMessage(PacketSimpleCmd msg, MessageContext ctx) {
		System.out.println("Reseve " + msg.value);
		if (msg.value == Cmd.OpenRegionGUI) {
			openRegionGUI();
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	private void openRegionGUI() {
		Minecraft.getMinecraft().displayGuiScreen(new RegionEditGUI());
	}

}
