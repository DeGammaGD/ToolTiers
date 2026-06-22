package elocindev.tierify.screen.client.widget;

import org.jetbrains.annotations.Nullable;

import elocindev.tierify.network.TieredClientPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

@Environment(EnvType.CLIENT)
public class ReforgeTab {
    public ReforgeTab(Component title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
    }

    public void onClick(Minecraft client) {
        TieredClientPacket.writeC2SScreenPacket((int) client.mouseHandler.xpos(), (int) client.mouseHandler.ypos(), true);
    }

}
