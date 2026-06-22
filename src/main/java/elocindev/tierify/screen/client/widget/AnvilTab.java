package elocindev.tierify.screen.client.widget;

import org.jetbrains.annotations.Nullable;

import elocindev.tierify.network.TieredClientPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public class AnvilTab {

    public AnvilTab(Component title, @Nullable Identifier texture, int preferedPos, Class<?>... screenClasses) {
    }

    public void onClick(Minecraft client) {
        TieredClientPacket.writeC2SScreenPacket((int) client.mouseHandler.xpos(), (int) client.mouseHandler.ypos(), false);
    }

}
