package net.kapitencraft.scripted.io.network;

import net.kapitencraft.scripted.io.network.C2S.SendBlocklySourcePacket;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public class ScriptedMessages {

    @SubscribeEvent
    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(SendBlocklySourcePacket.TYPE, SendBlocklySourcePacket.STREAM_CODEC, SendBlocklySourcePacket::handle);
    }
}
