package ru.beykerykt.minecraft.lightapi.bukkit.internal.utils;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import ru.beykerykt.minecraft.lightapi.bukkit.api.extension.IBukkitExtension;
import ru.beykerykt.minecraft.lightapi.bukkit.internal.handler.IBukkitHandler;
import ru.beykerykt.minecraft.lightapi.common.api.LightAPI;
import ru.beykerykt.minecraft.lightapi.common.api.LightType;
import ru.beykerykt.minecraft.lightapi.common.api.ResultCode;
import ru.beykerykt.minecraft.lightapi.common.api.chunks.ChunkData;
import ru.beykerykt.minecraft.lightapi.common.api.service.ICallback;
import ru.beykerykt.minecraft.lightapi.common.api.strategy.EditStrategy;
import ru.beykerykt.minecraft.lightapi.common.api.strategy.SendStrategy;
import ru.beykerykt.minecraft.lightapi.common.internal.ILightAPI;
import ru.beykerykt.minecraft.lightapi.common.internal.service.RequestFlag;

public class UsageTest {

    private LightAPI mLightAPI;

    private void onLoad() {
        ILightAPI impl = null;
        LightAPI.prepare(impl);
    }

    private void onEnable() {
        try {
            LightAPI.initialization();
        } catch (Exception ex) {
            ex.printStackTrace();
            // disable plugin
        }
    }

    /* Sync call method */
    private void setLightLevel(String world, int blockX, int blockY, int blockZ, int lightLevel) {
        int lightTypeFlags = LightType.BLOCK_LIGHTING;
        int oldBlockLight = mLightAPI.getLightLevel(world, blockX, blockY, blockZ, lightTypeFlags);
        EditStrategy editStrategy = EditStrategy.DEFERRED;
        SendStrategy sendStrategy = SendStrategy.DEFERRED;
        mLightAPI.setLightLevel(world, blockX, blockY, blockZ, lightLevel, lightTypeFlags, editStrategy, sendStrategy
                , new ICallback() {
                    @Override
                    public void onResult(int requestFlag, int resultCode) {
                        switch (requestFlag) {
                            case RequestFlag.EDIT:
                                if (resultCode == ResultCode.SUCCESS) {
                                    System.out.println("Light level was edited.");
                                } else {
                                    System.out.println("request (" + requestFlag + "): result code " + resultCode);
                                }
                                break;
                            case RequestFlag.RECALCULATE:
                                if (resultCode == ResultCode.SUCCESS) {
                                    int newBlockLight = mLightAPI.getLightLevel(world, blockX, blockY, blockZ,
                                            lightTypeFlags);
                                    if (oldBlockLight != newBlockLight) {
                                        System.out.println("Light level was recalculated.");
                                    } else {
                                        System.out.println("Light level was not recalculated.");
                                    }
                                } else {
                                    System.out.println("request (" + requestFlag + "): result code " + resultCode);
                                }
                                break;
                            case RequestFlag.SEND:
                                System.out.println("request (" + requestFlag + "): result code " + resultCode);
                                break;
                        }
                    }
                });
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String msg = event.getMessage();
        Location loc = player.getLocation();
        int flags = LightType.BLOCK_LIGHTING;
        EditStrategy editStrategy = EditStrategy.DEFERRED;
        SendStrategy sendStrategy = SendStrategy.DEFERRED;
        if (msg.equals("create")) {
            mLightAPI.setLightLevel(loc.getWorld().getName(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 15,
                    flags, editStrategy, sendStrategy, null);
        } else if (msg.equals("createhandler")) {
            IBukkitExtension extension = (IBukkitExtension) mLightAPI.getExtension();
            IBukkitHandler handler = extension.getHandler();
            handler.setRawLightLevel(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), 15,
                    flags);
            handler.recalculateLighting(loc.getWorld(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(),
                    15);
            for (ChunkData chunkData : handler.collectChunkSections(loc.getWorld(), loc.getBlockX(),
                    loc.getBlockY(), loc.getBlockZ(), 15, LightType.BLOCK_LIGHTING)) {
                handler.sendChunk(chunkData);
            }
        } else if (msg.equals("delete")) {

        }
    }
}
