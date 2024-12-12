package ProjectSteam.Blocks.mechanics.Axle;

import ARLib.network.INetworkTagReceiver;
import ProjectSteam.core.AbstractMechanicalBlock;
import ProjectSteam.core.IMechanicalBlockProvider;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.VertexBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;

import static ProjectSteam.Blocks.mechanics.Axle.BlockWoodenAxle.ROTATION_AXIS;
import static ProjectSteam.Registry.ENTITY_AXLE;
import static ProjectSteam.Registry.ENTITY_AXLE_FLYWHEEL;
import static ProjectSteam.Static.WOODEN_SOUNDS;

public class EntityWoodenAxleFlyWheel extends EntityWoodenAxle{

    public EntityWoodenAxleFlyWheel(BlockPos pos, BlockState blockState) {
        super(ENTITY_AXLE_FLYWHEEL.get(), pos, blockState);
        myMass = 20;
    }
}