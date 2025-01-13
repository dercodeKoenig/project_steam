package ARLib.gui.modules;


import ARLib.gui.IGuiHandler;
import ARLib.gui.modules.GuiModuleBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;

public class guiModuleTextInput extends GuiModuleBase {
    public     boolean isSelected = false;
    public int w, h;
    public String text = "";

    public guiModuleTextInput(int id, IGuiHandler guiHandler, int x, int y, int w, int h) {
        super(id, guiHandler, x, y);
        this.w = w;
        this.h= h;
    }

    public void client_onMouseCLick(double x, double y, int button) {
        if(client_isMouseOver(x,y,onGuiX,onGuiY,w,h)){
            isSelected = true;
        }else{
            isSelected = false;
        }
    }

    public void server_writeDataToSyncToClient(CompoundTag tag) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            CompoundTag myTag = new CompoundTag();
            myTag.putString("text", text);
            tag.put(this.getMyTagKey(), myTag);
        }
        super.server_writeDataToSyncToClient(tag);
    }

    public void server_readNetworkData(CompoundTag tag) {
        if (tag.contains(this.getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(this.getMyTagKey());
            if(myTag.contains("text")){
                text = myTag.getString("text");
            }
        }
        super.server_readNetworkData(tag);
    }

    public void client_handleDataSyncedToClient(CompoundTag tag) {
        if (tag.contains(this.getMyTagKey())) {
            CompoundTag myTag = tag.getCompound(this.getMyTagKey());
            if(myTag.contains("text")){
                text = myTag.getString("text");
            }
        }
        super.client_handleDataSyncedToClient(tag);
    }

    @Override
    public void client_onKeyClick(int keyCode, int scanCode, int modifiers) {
        if(!isSelected) return;

        // Check if the keyCode is for printable characters
        if (isPrintableCharacter(keyCode)) {
            // Determine if Shift is pressed for uppercase letters
            boolean shiftPressed = (modifiers & GLFW.GLFW_MOD_SHIFT) != 0;
            char character = getCharacterFromKey(keyCode, shiftPressed);

            if (character != '\0') { // '\0' indicates no valid character
                text += character; // Add the character to the text string
            }
        } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            // Handle backspace for deleting characters
            if (!text.isEmpty()) {
                text = text.substring(0, text.length() - 1);
            }
        }

        CompoundTag info = new CompoundTag();
        CompoundTag myTag = new CompoundTag();
        myTag.putString("text", text);
        info.put(this.getMyTagKey(), myTag);
        guiHandler.sendToServer(info);
    }

    // Helper method to determine if a key code corresponds to a printable character
    private boolean isPrintableCharacter(int keyCode) {
        return (keyCode >= GLFW.GLFW_KEY_SPACE && keyCode <= GLFW.GLFW_KEY_GRAVE_ACCENT) ||
                (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) ||
                (keyCode >= GLFW.GLFW_KEY_0 && keyCode <= GLFW.GLFW_KEY_9);
    }

    // Helper method to map a key code to a character, handling shift for uppercase
    private char getCharacterFromKey(int keyCode, boolean shiftPressed) {
        // Handle letters
        if (keyCode >= GLFW.GLFW_KEY_A && keyCode <= GLFW.GLFW_KEY_Z) {
            char base = shiftPressed ? 'A' : 'a';
            return (char) (base + (keyCode - GLFW.GLFW_KEY_A));
        }

        // Handle numbers and symbols
        switch (keyCode) {
            case GLFW.GLFW_KEY_0: return shiftPressed ? ')' : '0';
            case GLFW.GLFW_KEY_1: return shiftPressed ? '!' : '1';
            case GLFW.GLFW_KEY_2: return shiftPressed ? '@' : '2';
            case GLFW.GLFW_KEY_3: return shiftPressed ? '#' : '3';
            case GLFW.GLFW_KEY_4: return shiftPressed ? '$' : '4';
            case GLFW.GLFW_KEY_5: return shiftPressed ? '%' : '5';
            case GLFW.GLFW_KEY_6: return shiftPressed ? '^' : '6';
            case GLFW.GLFW_KEY_7: return shiftPressed ? '&' : '7';
            case GLFW.GLFW_KEY_8: return shiftPressed ? '*' : '8';
            case GLFW.GLFW_KEY_9: return shiftPressed ? '(' : '9';
            case GLFW.GLFW_KEY_SPACE: return ' '; // Space key
            case GLFW.GLFW_KEY_MINUS: return shiftPressed ? '_' : '-';
            case GLFW.GLFW_KEY_EQUAL: return shiftPressed ? '+' : '=';
            case GLFW.GLFW_KEY_LEFT_BRACKET: return shiftPressed ? '{' : '[';
            case GLFW.GLFW_KEY_RIGHT_BRACKET: return shiftPressed ? '}' : ']';
            case GLFW.GLFW_KEY_SEMICOLON: return shiftPressed ? ':' : ';';
            case GLFW.GLFW_KEY_APOSTROPHE: return shiftPressed ? '"' : '\'';
            case GLFW.GLFW_KEY_COMMA: return shiftPressed ? '<' : ',';
            case GLFW.GLFW_KEY_PERIOD: return shiftPressed ? '>' : '.';
            case GLFW.GLFW_KEY_SLASH: return shiftPressed ? '?' : '/';
            case GLFW.GLFW_KEY_BACKSLASH: return shiftPressed ? '|' : '\\';
            case GLFW.GLFW_KEY_GRAVE_ACCENT: return shiftPressed ? '~' : '`';
            default: return '\0'; // No valid character for this key
        }
    }


    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (this.isEnabled) {
            guiGraphics.fill(onGuiX-1,onGuiY-1,onGuiX+w+1,onGuiY+h+1,isSelected ? 0xffffffff:0xff000000);
            guiGraphics.fill(onGuiX,onGuiY,onGuiX+w,onGuiY+h,0xff000000);
            guiGraphics.drawString(Minecraft.getInstance().font,text,onGuiX+1,onGuiY+h/2-Minecraft.getInstance().font.lineHeight / 2,0xffffffff,false);
        }
    }
}
