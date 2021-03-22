package hide.resource;

import java.util.Random;

import hide.core.HideFaction;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class SpawnerBlock extends BlockContainer{

	public SpawnerBlock() {
		super(Material.IRON);
		setHardness(0.2F);
		setUnlocalizedName("spawner_block");
		setRegistryName(HideFaction.MODID,"spawner_block");
	}

	@Override
	public void updateTick(World worldIn, BlockPos pos, IBlockState state, Random rand) {

		System.out.println("ID = "+getRegistryName());
	}

	@Override
	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return null;
	}


}
