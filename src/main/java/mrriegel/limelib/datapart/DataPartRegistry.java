package mrriegel.limelib.datapart;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.network.DataPartSyncMessage;
import mrriegel.limelib.network.PacketHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class DataPartRegistry implements INBTSerializable<NBTTagCompound> {

	private Map<BlockPos, DataPart> partMap = Maps.newHashMap();
	public World world;

	public static DataPartRegistry get(World world) {
		if (world == null)
			return null;
		DataPartRegistry reg = world.hasCapability(CapabilityDataPart.DATAPART, null) ? world.getCapability(CapabilityDataPart.DATAPART, null) : null;
		if (reg == null)
			return null;
		else {
			if (reg.world == null)
				reg.world = world;
			for (DataPart part : reg.getParts())
				if (part.world == null)
					part.world = world;
			return reg;
		}
	}

	public DataPart getDataPart(BlockPos pos) {
		return partMap.get(pos);
	}

	public boolean addDataPart(BlockPos pos, DataPart part, boolean force) {
		part.pos = pos;
		part.world = world;
		if (partMap.get(pos) != null) {
			if (force) {
				partMap.put(pos, part);
				part.onAdded();
				sync(pos);
				return true;
			}
			return false;
		} else {
			partMap.put(pos, part);
			part.onAdded();
			sync(pos);
			return true;
		}
	}

	public void removeDataPart(BlockPos pos) {
		if (partMap.containsKey(pos)) {
			partMap.get(pos).onRemoved();
			partMap.remove(pos);
			sync(pos);
		}
	}

	public void clearWorld() {
		partMap.clear();
	}

	public Collection<DataPart> getParts() {
		return Collections.unmodifiableCollection(partMap.values());
	}

	public void sync(BlockPos pos) {
		if (world != null && !world.isRemote)
			PacketHandler.sendToAllAround(new DataPartSyncMessage(getDataPart(pos), pos), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 18));
	}

	@Override
	public NBTTagCompound serializeNBT() {
		List<NBTTagCompound> nbts = Lists.newArrayList();
		for (DataPart entry : partMap.values())
			nbts.add(entry.writeDataToNBT(new NBTTagCompound()));
		return NBTHelper.setTagList(new NBTTagCompound(), "nbts", nbts);
	}

	@Override
	public void deserializeNBT(NBTTagCompound nbt) {
		clearWorld();
		List<NBTTagCompound> nbts = NBTHelper.getTagList((NBTTagCompound) nbt, "nbts");
		for (NBTTagCompound n : nbts) {
			createPart(n);
		}
	}

	public void createPart(NBTTagCompound n) {
		try {
			Class<?> clazz = Class.forName(n.getString("class"));
			if (clazz != null && DataPart.class.isAssignableFrom(clazz)) {
				DataPart part = (DataPart) ConstructorUtils.invokeConstructor(clazz);
				if (part != null) {
					part.readDataFromNBT(n);
					partMap.put(part.pos, part);
					part.onAdded();
					sync(part.pos);
				}
			}
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
	}

}