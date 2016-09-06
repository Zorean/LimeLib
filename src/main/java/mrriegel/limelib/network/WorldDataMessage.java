package mrriegel.limelib.network;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.lang3.reflect.ConstructorUtils;

import io.netty.util.internal.MpscLinkedQueueNode;
import mrriegel.limelib.helper.NBTHelper;
import mrriegel.limelib.util.CommonWorldData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldSavedData;
import net.minecraft.world.storage.MapStorage;
import net.minecraftforge.fml.relauncher.Side;

public class WorldDataMessage extends AbstractMessage<WorldDataMessage> {

	public WorldDataMessage() {
		super();
	}

	public WorldDataMessage(NBTTagCompound nbt) {
		super(nbt);
	}

	@Override
	public void handleMessage(EntityPlayer player, NBTTagCompound nbt, Side side) {
		MapStorage storage = NBTHelper.getBoolean(nbt, "global") ? player.worldObj.getMapStorage() : player.worldObj.getPerWorldStorage();
		Class<WorldSavedData> clazz = null;
		try {
			clazz = (Class<WorldSavedData>) Class.forName(NBTHelper.getString(nbt, "className"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if (clazz == null)
			return;
		CommonWorldData d = (CommonWorldData) storage.getOrLoadData(clazz, NBTHelper.getString(nbt, "mapName"));
		if (d == null) {
			try {
				d = (CommonWorldData) ConstructorUtils.invokeConstructor(clazz, NBTHelper.getString(nbt, "mapName"));
			} catch (Exception e) {
				e.printStackTrace();
			}
			storage.setData(NBTHelper.getString(nbt, "mapName"), d);
		}
		d.deserializeNBT(nbt);
		d.markDirty();
	}

}
