package ResearchSystem.Config;

import ARLib.utils.RecipePart;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RecipeConfig {

    public static RecipeConfig INSTANCE = loadConfig();

    public static class Recipe {
        public String requiredResearch = "";
        public RecipePart output = new RecipePart("minecraft:air",1);
        public List<String> pattern = new ArrayList<>();
        public Map<String,RecipeInput> keys = new HashMap<>();
    }
    public static class RecipeInput{
        public RecipePart input = new RecipePart("");
        public RecipePart onComplete = new RecipePart("",0);
        public RecipeInput(RecipePart in, RecipePart out){
            this.input = in;this.onComplete = out;
        }
        public RecipeInput(RecipePart in){
            this(in, new RecipePart(""));
        }
    }

    public List<Recipe> recipeList = new ArrayList<>();

    public static String[] shrink(List<String> pattern) {
        int i = Integer.MAX_VALUE;
        int j = 0;
        int k = 0;
        int l = 0;

        for(int i1 = 0; i1 < pattern.size(); ++i1) {
            String s = (String)pattern.get(i1);
            i = Math.min(i, firstNonSpace(s));
            int j1 = lastNonSpace(s);
            j = Math.max(j, j1);
            if (j1 < 0) {
                if (k == i1) {
                    ++k;
                }

                ++l;
            } else {
                l = 0;
            }
        }

        if (pattern.size() == l) {
            return new String[0];
        } else {
            String[] astring = new String[pattern.size() - l - k];

            for(int k1 = 0; k1 < astring.length; ++k1) {
                astring[k1] = ((String)pattern.get(k1 + k)).substring(i, j + 1);
            }

            return astring;
        }
    }

     static int firstNonSpace(String row) {
        int i;
        for(i = 0; i < row.length() && row.charAt(i) == ' '; ++i) {
        }

        return i;
    }

     static int lastNonSpace(String row) {
        int i;
        for(i = row.length() - 1; i >= 0 && row.charAt(i) == ' '; --i) {
        }

        return i;
    }


    public RecipeConfig() {
        Recipe t1 = new Recipe();
        t1.requiredResearch = "example Research 1";
        t1.pattern = List.of("   ","ABA","   ");
        t1.keys.put("A", new RecipeInput(new RecipePart("c:ingots/iron",2), new RecipePart("minecraft:stone")));
        t1.keys.put("B", new RecipeInput(new RecipePart("minecraft:string")));
        t1.output = new RecipePart("minecraft:dirt",10);
        recipeList.add(t1);
    }

    public void SyncConfig(ServerPlayer p) {
        if (p != null) {
            PacketDistributor.sendToPlayer(p, new PacketConfigSync(new Gson().toJson(this)));
        }
    }

    // because i can not call the direct jei method from here (the class may not be found if not installed),
    // i let the plugin insert a runnable here and i execute it on recipe load.
    // this way it should not crash when jei is not found.
    public static Runnable jeiRunnableOnConfigLoad = null;

    public void loadConfig(String configString) {
        RecipeConfig.INSTANCE = new Gson().fromJson(configString, RecipeConfig.class);
        System.out.println("load config:" + configString);
        if(jeiRunnableOnConfigLoad!=null)
            jeiRunnableOnConfigLoad.run();
    }

    public static RecipeConfig loadConfig() {
        String filename = "research_recipe_list.json";
        Path configDir = Paths.get(FMLPaths.CONFIGDIR.get().toString());
        Path filePath = configDir.resolve(filename);
        try {
            // Create the config directory if it doesn't exist
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
                Files.write(filePath, new GsonBuilder().setPrettyPrinting().excludeFieldsWithModifiers(Modifier.PRIVATE).create().toJson(new RecipeConfig()).getBytes(StandardCharsets.UTF_8));
            }
            // Load JSON from the file
            String jsonContent = Files.readString(filePath);
            Gson gson = new Gson();
            RecipeConfig c = gson.fromJson(jsonContent, RecipeConfig.class);
            return c;
        } catch (JsonSyntaxException e) {
            System.err.println("Failed to parse config JSON");
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }




    public static class PacketConfigSync implements CustomPacketPayload {

        public static final Type<PacketConfigSync> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath("research_station", "packet_engineering_config_sync"));


        public PacketConfigSync(String config) {
            this.config = config;
        }

        String config;
        public String getConfig() {
            return config;
        }


        public static final StreamCodec<ByteBuf, PacketConfigSync> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,
                PacketConfigSync::getConfig,
                PacketConfigSync::new
        );

        public static void readClient(final PacketConfigSync data, final IPayloadContext context) {
            String config = data.getConfig();
            RecipeConfig.INSTANCE.loadConfig(config);
        }
        public static void readServer(final PacketConfigSync data, final IPayloadContext context) {
        }

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }

        public static void register(PayloadRegistrar registrar) {
            registrar.playBidirectional(
                    PacketConfigSync.TYPE,
                    PacketConfigSync.STREAM_CODEC,
                    new DirectionalPayloadHandler<>(
                            PacketConfigSync::readClient,
                            PacketConfigSync::readServer
                    )
            );
        }
    }
}


