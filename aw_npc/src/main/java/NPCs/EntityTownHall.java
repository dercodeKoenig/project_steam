package NPCs;

import ARLib.utils.DimensionUtils;
import com.electronwill.nightconfig.toml.TomlWriter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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

import static NPCs.EntityTownHall.TownHallOwners.ownerNamesStatic;
import static NPCs.Registry.ENTITY_TOWNHALL;

public class EntityTownHall extends BlockEntity {

    public static HashSet<BlockPos> knownTownHalls = new HashSet<>();

    public static class TownHallOwners {
        //TODO use setchanged and only save on change
        public static HashMap<String, HashMap<BlockPos, Set<String>>> ownerNamesStatic = new HashMap<>();

        public BlockPos pos;
        public Set<String> owners;

        public static HashMap<String, List<TownHallOwners>> getFromStaticMap() {
            HashMap<String, List<TownHallOwners>> map = new HashMap<>();
            for (String s : ownerNamesStatic.keySet()) {
                map.put(s, new ArrayList<>());
                for (BlockPos p : ownerNamesStatic.get(s).keySet()) {
                    TownHallOwners i = new TownHallOwners();
                    i.pos = p;
                    i.owners = ownerNamesStatic.get(s).get(p);
                    map.get(s).add(i);
                }
            }
            return map;
        }

        public static void createStaticMap(HashMap<String, List<TownHallOwners>> map) {
            ownerNamesStatic = new HashMap<>();
            for (Level l : ServerLifecycleHooks.getCurrentServer().getAllLevels()) {
                if (!ownerNamesStatic.containsKey(DimensionUtils.getLevelId(l))) {
                    ownerNamesStatic.put(DimensionUtils.getLevelId(l), new HashMap<>());
                }
                if(map.containsKey(DimensionUtils.getLevelId(l))){
                    for(TownHallOwners i : map.get(DimensionUtils.getLevelId(l))){
                        if(i!=null)
                            ownerNamesStatic.get(DimensionUtils.getLevelId(l)).put(i.pos,i.owners);
                    }
                }
            }
        }

        public static String toJson() {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            String s = gson.toJson(getFromStaticMap());
            return s;
        }

        public static void fromJson(String json) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            Type mapType = new TypeToken<HashMap<String, List<TownHallOwners>>>() {
            }.getType();
            HashMap<String, List<TownHallOwners>> map = gson.fromJson(json, mapType);
            createStaticMap(map);
        }
    }

    Set<String> ownerNames;

    public static void onLevelSave(LevelEvent.Save event) {
        if (event.getLevel().isClientSide()) return;
        Path configDir = Paths.get(FMLPaths.GAMEDIR.get().toString()).resolve(event.getLevel().getServer().getWorldPath(LevelResource.ROOT));
        String filename = "townHallOwners";
        Path filePath = configDir.resolve(filename);

        try {
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }
            Files.writeString(filePath, TownHallOwners.toJson());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void onLevelLoad(LevelEvent.Load event) {
        if (event.getLevel().isClientSide()) return;
        Path configDir = Paths.get(FMLPaths.GAMEDIR.get().toString()).resolve(event.getLevel().getServer().getWorldPath(LevelResource.ROOT));
        String filename = "townHallOwners";
        Path filePath = configDir.resolve(filename);
        if (Files.exists(filePath)) {
            try {
                String s = Files.readString(filePath);
                TownHallOwners.fromJson(s);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JsonSyntaxException j) {
                System.err.println(j);
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
            if (ownerNamesStatic.get(DimensionUtils.getLevelId(level)).get(getBlockPos()) == null) {
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
