package NPCs;

import ARLib.utils.DimensionUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static NPCs.Registry.ENTITY_TOWNHALL;

public class EntityTownHall extends BlockEntity {

    public static HashSet<BlockPos> knownTownHalls = new HashSet<>();

    public static HashMap<String, HashMap<BlockPos, Set<String>>> ownerNamesStatic = new HashMap<>();
    Set<String> ownerNames;


    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel().isClientSide()) return;
        Path configDir = Paths.get(FMLPaths.GAMEDIR.get().toString()).resolve(event.getLevel().getServer().getWorldPath(LevelResource.ROOT));
        String filename = "townHallOwners";
        Path filePath = configDir.resolve(filename);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String s = gson.toJson(ownerNamesStatic);
        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            Files.writeString(filePath, s);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        Path configDir = Paths.get(FMLPaths.GAMEDIR.get().toString()).resolve(event.getLevel().getServer().getWorldPath(LevelResource.ROOT));
        String filename = "townHallOwners";
        Path filePath = configDir.resolve(filename);
        Gson gson = new Gson();

        if (Files.exists(filePath)) {
            try {
                Type mapType = new TypeToken<HashMap<String, HashMap<BlockPos, Set<String>>>>() {
                }.getType();
                String s = Files.readString(filePath);
                ownerNamesStatic = gson.fromJson(s, mapType);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JsonSyntaxException j){
                System.err.println(j);;
            }
        }

        for (Level l : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
            if (!ownerNamesStatic.containsKey(DimensionUtils.getLevelId(l))) {
                ownerNamesStatic.put(DimensionUtils.getLevelId(l), new HashMap<>());
            }
        }
    }

    public EntityTownHall(BlockPos pos, BlockState blockState) {
        super(ENTITY_TOWNHALL.get(), pos, blockState);
    }

    public void useWithoutItem(Player p) {
        if (!level.isClientSide) {
            if (ownerNames.isEmpty()) {
                ownerNames.add(p.getName().getString());
                p.sendSystemMessage(Component.literal("you are now owner of this townhall"));
            } else {
                if (ownerNames.contains(p.getName().getString())) {
                    System.out.println("owner");
                }
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            if(ownerNamesStatic.get(DimensionUtils.getLevelId(level)).get(getBlockPos()) == null) {
                ownerNamesStatic.get(DimensionUtils.getLevelId(level)).put(getBlockPos(), new HashSet<>());
            }
                ownerNames = ownerNamesStatic.get(DimensionUtils.getLevelId(level)).get(getBlockPos());
        }

        knownTownHalls.add(getBlockPos());
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        knownTownHalls.remove(getBlockPos());
    }
}
