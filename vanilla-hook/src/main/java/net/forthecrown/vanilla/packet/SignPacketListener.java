package net.forthecrown.vanilla.packet;

import java.lang.reflect.Field;
import java.util.List;
import net.forthecrown.packet.PacketCall;
import net.forthecrown.packet.PacketHandler;
import net.forthecrown.utils.math.Vectors;
import net.forthecrown.utils.math.WorldVec3i;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.protocol.game.ClientboundLevelChunkPacketData;
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.HangingSignBlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftHangingSign;
import org.bukkit.craftbukkit.v1_20_R2.block.CraftSign;

class SignPacketListener {

  private final ListenersImpl renderer;

  private static final int COMPATIBLE_VERSION = 3578;

  private Field ClientboundLevelChunkPacketData_blockEntitiesData;

  private Field BlockEntityInfo_tag;
  private Field BlockEntityInfo_packedXz;
  private Field BlockEntityInfo_y;
  private Field BlockEntityInfo_type;

  static {
    int dataVersion = Bukkit.getUnsafe().getDataVersion();
    if (dataVersion != COMPATIBLE_VERSION) {
      throw new RuntimeException("Packet listener class is out of date, needs updating!");
    }
  }

  public SignPacketListener(ListenersImpl renderer) {
    this.renderer = renderer;
  }

  @PacketHandler(ignoreCancelled = true)
  public void onChunkLoad(ClientboundLevelChunkWithLightPacket packet, PacketCall call)
      throws ReflectiveOperationException
  {
    var registry = renderer.getSignRenderers();

    if (registry.isEmpty()) {
      return;
    }

    var data = packet.getChunkData();
    var extra = data.getExtraPackets();

    var newCall = new PacketCallImpl(call.getPlayer());
    newCall.setCancelled(call.isCancelled());

    if (!extra.isEmpty()) {
      var it = extra.listIterator();
      while (it.hasNext()) {
        var n = it.next();

        if (n instanceof ClientboundBlockEntityDataPacket entityData) {
          onBlockEntityData(entityData, call);

          if (call.getReplacementPacket() != null) {
            it.set(call.getReplacementPacket());
          }
        }
      }
    }

    List<?> infoList = getBlockEntityInfoList(data);
    if (infoList.isEmpty()) {
      return;
    }

    int chunkMinX = Vectors.toBlock(packet.getX());
    int chunkMinZ = Vectors.toBlock(packet.getZ());

    for (Object o: infoList) {
      BlockEntityInfo info = getInfo(o);

      if (info.type != BlockEntityType.SIGN && info.type != BlockEntityType.HANGING_SIGN) {
        continue;
      }

      int sectionX = info.packedXz >> 4 & 0xF;
      int sectionZ = info.packedXz & 0xF;

      int blockX = sectionX + chunkMinX;
      int blockZ = sectionZ + chunkMinZ;

      BlockPos pos = new BlockPos(blockX, info.y, blockZ);

      var entity = renderSign(pos, info.type, info.tag, newCall);

      if (entity == null) {
        continue;
      }

      info.tag = entity.saveWithoutMetadata();
      entity.sanitizeSentNbt(info.tag);

      setInfo(o, info);
    }
  }

  private void ensureBlockEntityInfoFieldsArePresent(Object info) {
    if (BlockEntityInfo_packedXz != null) {
      return;
    }

    Class c = info.getClass();
    Field[] declared = c.getDeclaredFields();

    BlockEntityInfo_packedXz = declared[0];
    BlockEntityInfo_y = declared[1];
    BlockEntityInfo_type = declared[2];
    BlockEntityInfo_tag = declared[3];

    BlockEntityInfo_packedXz.setAccessible(true);
    BlockEntityInfo_y.setAccessible(true);
    BlockEntityInfo_type.setAccessible(true);
    BlockEntityInfo_tag.setAccessible(true);
  }

  private BlockEntityInfo getInfo(Object o) throws IllegalAccessException {
    ensureBlockEntityInfoFieldsArePresent(o);

    BlockEntityInfo info = new BlockEntityInfo();
    info.packedXz = BlockEntityInfo_packedXz.getInt(o);
    info.y = BlockEntityInfo_y.getInt(o);
    info.type = (BlockEntityType<?>) BlockEntityInfo_type.get(o);
    info.tag = (CompoundTag) BlockEntityInfo_tag.get(o);

    return info;
  }

  private void setInfo(Object info, BlockEntityInfo wrapper) throws IllegalAccessException {
    ensureBlockEntityInfoFieldsArePresent(info);

    BlockEntityInfo_packedXz.setInt(info, wrapper.packedXz);
    BlockEntityInfo_y.setInt(info, wrapper.y);
    BlockEntityInfo_type.set(info, wrapper.type);
    BlockEntityInfo_tag.set(info, wrapper.tag);
  }

  private List<?> getBlockEntityInfoList(ClientboundLevelChunkPacketData data)
      throws IllegalAccessException
  {
    if (ClientboundLevelChunkPacketData_blockEntitiesData == null) {
      Class c = data.getClass();
      Field[] declared = c.getDeclaredFields();

      for (var f: declared) {
        if (!List.class.isAssignableFrom(f.getType()) || f.getName().equals("extraPackets")) {
          continue;
        }

        ClientboundLevelChunkPacketData_blockEntitiesData = f;
        f.setAccessible(true);
      }

      if (ClientboundLevelChunkPacketData_blockEntitiesData == null) {
        throw new RuntimeException("Couldn't find blockEntitiesData field");
      }
    }

    return (List<?>) ClientboundLevelChunkPacketData_blockEntitiesData.get(data);
  }

  @PacketHandler(ignoreCancelled = true)
  public void onBlockEntityData(ClientboundBlockEntityDataPacket packet, PacketCall call) {
    if (renderer.getSignRenderers().isEmpty()) {
      return;
    }

    if (packet.getType() != BlockEntityType.SIGN
        && packet.getType() != BlockEntityType.HANGING_SIGN
    ) {
      return;
    }

    var entity = renderSign(packet.getPos(), packet.getType(), packet.getTag(), call);

    if (entity == null) {
      return;
    }

    ClientboundBlockEntityDataPacket replacement = ClientboundBlockEntityDataPacket.create(entity);
    call.setReplacementPacket(replacement);
  }

  private BlockEntity renderSign(
      BlockPos pos,
      BlockEntityType<?> type,
      CompoundTag tag,
      PacketCall call
  ) {
    if (tag == null) {
      return null;
    }

    SignBlockEntity entity;
    SnapshotGetter sign;

    if (type == BlockEntityType.HANGING_SIGN) {
      var hanging = new HangingSignBlockEntity(pos, null);
      hanging.load(tag);
      sign = new ExposingHangingSign(null, hanging);
      entity = hanging;
    } else {
      entity = new SignBlockEntity(pos, null);
      entity.load(tag);
      sign = new ExposingCraftSign<>(null, entity);
    }

    WorldVec3i wrapperPos = new WorldVec3i(call.getWorld(), pos.getX(), pos.getY(), pos.getZ());

    if (!renderer.renderSign(sign, wrapperPos, call.getPlayer())) {
      return null;
    }

    sign.applyTo(entity);
    return sign.getSnapshot();
  }

  private static interface SnapshotGetter<T extends SignBlockEntity> extends Sign {

    T getSnapshot();

    void applyTo(T entity);
  }

  private static class ExposingHangingSign
      extends CraftHangingSign
      implements SnapshotGetter<HangingSignBlockEntity>
  {

    public ExposingHangingSign(World world, HangingSignBlockEntity tileEntity) {
      super(world, tileEntity);
    }

    @Override
    public HangingSignBlockEntity getSnapshot() {
      return super.getSnapshot();
    }
  }

  private static class ExposingCraftSign<T extends SignBlockEntity>
      extends CraftSign<T>
      implements SnapshotGetter<T>
  {

    public ExposingCraftSign(World world, T tileEntity) {
      super(world, tileEntity);
    }

    @Override
    public T getSnapshot() {
      return super.getSnapshot();
    }
  }

  private class BlockEntityInfo {
    int packedXz;
    int y;
    BlockEntityType<?> type;
    CompoundTag tag;
  }
}
