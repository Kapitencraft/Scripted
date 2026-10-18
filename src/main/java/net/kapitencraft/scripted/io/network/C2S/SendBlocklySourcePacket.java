package net.kapitencraft.scripted.io.network.C2S;

import net.kapitencraft.kap_lib.core.helpers.IOHelper;
import net.kapitencraft.scripted.Scripted;
import net.kapitencraft.scripted.edit.graphical.widgets.stmt.StmtCodeWidget;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.io.File;
import java.io.StreamCorruptedException;
import java.util.List;

public record SendBlocklySourcePacket(List<StmtCodeWidget> methods) implements CustomPacketPayload {
    public static final StreamCodec<RegistryFriendlyByteBuf, SendBlocklySourcePacket> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(StmtCodeWidget.CODEC)
            .apply(ByteBufCodecs.list())
            .map(SendBlocklySourcePacket::new, SendBlocklySourcePacket::methods);
    public static final Type<SendBlocklySourcePacket> TYPE = new Type<>(Scripted.res("send_blockly_source"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public void handle(IPayloadContext iPayloadContext) {
        ServerPlayer player = (ServerPlayer) iPayloadContext.player();
        File file = player.serverLevel().getServer().getWorldPath(Scripted.SOURCE_DIR).toFile();
        final String name = "BlocklyTest";
        File target = new File(new File(file, player.getGameProfile().getName()), name + ".bscr");
        IOHelper.saveFile(target, StmtCodeWidget.CODEC.listOf(), this.methods);
        Scripted.LOGGER.debug("saving blocky source named {} at {}", name, target.getPath());
    }
}
