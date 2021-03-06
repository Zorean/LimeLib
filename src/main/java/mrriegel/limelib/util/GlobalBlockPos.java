package mrriegel.limelib.util;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class GlobalBlockPos {
	private BlockPos pos;
	private int dimension;

	public GlobalBlockPos(BlockPos pos, int dimension) {
		this.pos = pos;
		this.dimension = dimension;
	}

	public GlobalBlockPos(BlockPos pos, World world) {
		this(pos, world.provider.getDimension());
	}

	public GlobalBlockPos(NBTTagCompound nbt) {
		readFromNBT(nbt);
	}

	private GlobalBlockPos() {
	}

	public static GlobalBlockPos fromTile(TileEntity tile) {
		if (tile == null)
			return null;
		return new GlobalBlockPos(tile.getPos(), tile.getWorld());
	}

	@Override
	public String toString() {
		return "GlobalBlockPos [pos=" + pos + ", dimension=" + dimension + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + dimension;
		result = prime * result + ((pos == null) ? 0 : pos.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		GlobalBlockPos other = (GlobalBlockPos) obj;
		if (dimension != other.dimension)
			return false;
		if (pos == null) {
			if (other.pos != null)
				return false;
		} else if (!pos.equals(other.pos))
			return false;
		return true;
	}

	public BlockPos getPos() {
		return pos;
	}

	public void setPos(BlockPos pos) {
		this.pos = pos;
	}

	public int getDimension() {
		return dimension;
	}

	public void setDimension(int dimension) {
		this.dimension = dimension;
	}

	public World getWorld() {
		return DimensionManager.getWorld(dimension);
	}

	public TileEntity getTile() {
		if (getWorld() == null)
			return null;
		return getWorld().getTileEntity(getPos());
	}

	public IBlockState getBlockState() {
		return getWorld().getBlockState(getPos());
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound.hasKey("Gpos"))
			pos = BlockPos.fromLong(compound.getLong("Gpos"));
		else
			pos = null;
		dimension = compound.getInteger("Gdim");
	}

	public void writeToNBT(NBTTagCompound compound) {
		if (pos != null)
			compound.setLong("Gpos", pos.toLong());
		compound.setInteger("Gdim", dimension);
	}

	public static GlobalBlockPos loadGlobalPosFromNBT(NBTTagCompound nbt) {
		GlobalBlockPos pos = new GlobalBlockPos();
		pos.readFromNBT(nbt);
		return pos.getPos() != null ? pos : null;
	}

}
