package ProjectSteamCrafting.MillStone;

import ARLib.multiblockCore.BlockMultiblockMaster;
import ARLib.multiblockCore.EntityMultiblockMaster;
import ARLib.network.PacketBlockEntity;
import ProjectSteam.Core.AbstractMechanicalBlock;
import ProjectSteam.Core.IMechanicalBlockProvider;
import ProjectSteam.ProjectSteam;
import ProjectSteamCrafting.Sieve.EntitySieve;
import ProjectSteamCrafting.WoodMill.EntityWoodMill;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import static ProjectSteam.Registry.WOODEN_AXLE;
import static ProjectSteamCrafting.Registry.ENTITY_MILLSTONE;
import static ProjectSteamCrafting.Registry.MILLSTONE;

public class EntityMillStone extends EntityMultiblockMaster implements IMechanicalBlockProvider {

    AbstractMechanicalBlock myMechanicalBlock = new AbstractMechanicalBlock(0, this) {
        @Override
        public double getMaxStress() {
            return 600;
        }

        @Override
        public double getInertia(Direction direction) {
            return 500;
        }

        @Override
        public double getTorqueResistance(Direction direction) {
            return 30;
        }

        @Override
        public double getTorqueProduced(Direction direction) {
            return 0;
        }

        @Override
        public double getRotationMultiplierToInside(@Nullable Direction direction) {
            return 1;
        }
    };

    public EntityMillStone(BlockPos pos, BlockState blockState) {
        super(ENTITY_MILLSTONE.get(), pos, blockState);
        myMechanicalBlock.resetRotationAfterX = 360 * 4;
    }

    @Override
    public void onStructureComplete() {
        if(level.isClientSide)
            // this is executed before minecraft updates the blockstate on client
            // but resetRotation (to make it sync to the rotation) checks for connected mechanical blocks and it only connects to other mechanical blocks when the multiblock is formed
            // so i update it directly here
            level.setBlock(getBlockPos(),getBlockState().setValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED,true),3);
        myMechanicalBlock.mechanicalOnload();
    }

    public void tick() {
        myMechanicalBlock.mechanicalTick();
        if(!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)){
            if(level.getGameTime() % 51 == 0){
                super.scanStructure();
            }
        }
    }

    public static <T extends BlockEntity> void tick(Level level, BlockPos blockPos, BlockState blockState, T t) {
        ((EntityMillStone) t).tick();
    }

    // "c" is ALWAYS used for the controller/master block.
    public static Object[][][] structure = {
            {{'S', 'A', 'S'}, {'S', 'c', 'S'}, {'S', 'A', 'S'}}
    };
    public static boolean[][][] hideBlocks = {
            {{true, false, true}, {true, true, true}, {true, false, true}}
    };

    public static HashMap<Character, List<Block>> charMapping = new HashMap<>();

    static {
        List<Block> c = new ArrayList<>();
        c.add(MILLSTONE.get());
        charMapping.put('c', c);

        List<Block> A = new ArrayList<>();
        A.add(WOODEN_AXLE.get());
        charMapping.put('A', A);

        List<Block> S = new ArrayList<>();
        S.add(Blocks.STONE_SLAB);
        charMapping.put('S', S);
    }

    public boolean[][][] hideBlocks() {
        return hideBlocks;
    }

    @Override
    public Object[][][] getStructure() {
        return structure;
    }

    @Override
    public HashMap<Character, List<Block>> getCharMapping() {
        return charMapping;
    }

    @Override
    public AbstractMechanicalBlock getMechanicalBlock(Direction direction) {
        if (!getBlockState().getValue(BlockMultiblockMaster.STATE_MULTIBLOCK_FORMED)) return null;
        if (direction.getAxis() == getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING).getAxis())
            return myMechanicalBlock;
        else return null;
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        myMechanicalBlock.mechanicalLoadAdditional(tag, registries);
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        myMechanicalBlock.mechanicalSaveAdditional(tag, registries);
    }

    @Override
    public void readClient(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadClient(tag);
        super.readClient(tag);
    }

    @Override
    public void readServer(CompoundTag tag) {
        myMechanicalBlock.mechanicalReadServer(tag);
        super.readServer(tag);
    }
}
