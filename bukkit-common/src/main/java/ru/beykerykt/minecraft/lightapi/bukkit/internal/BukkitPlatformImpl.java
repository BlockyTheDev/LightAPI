/**
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2021 Vladimir Mikhailov <beykerykt@gmail.com>
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package ru.beykerykt.minecraft.lightapi.bukkit.internal;

import org.bukkit.ChatColor;
import ru.beykerykt.minecraft.lightapi.bukkit.BukkitPlugin;
import ru.beykerykt.minecraft.lightapi.bukkit.ConfigurationPath;
import ru.beykerykt.minecraft.lightapi.bukkit.api.extension.IBukkitExtension;
import ru.beykerykt.minecraft.lightapi.bukkit.internal.handler.IHandler;
import ru.beykerykt.minecraft.lightapi.bukkit.internal.service.BukkitBackgroundService;
import ru.beykerykt.minecraft.lightapi.common.api.ResultCode;
import ru.beykerykt.minecraft.lightapi.common.api.extension.IExtension;
import ru.beykerykt.minecraft.lightapi.common.internal.IComponentFactory;
import ru.beykerykt.minecraft.lightapi.common.internal.InternalCode;
import ru.beykerykt.minecraft.lightapi.common.internal.PlatformType;
import ru.beykerykt.minecraft.lightapi.common.internal.chunks.observer.IChunkObserver;
import ru.beykerykt.minecraft.lightapi.common.internal.engine.ILightEngine;
import ru.beykerykt.minecraft.lightapi.common.internal.service.IBackgroundService;

import java.util.UUID;

public class BukkitPlatformImpl implements IBukkitPlatformImpl, IBukkitExtension {

    private boolean DEBUG = false;

    private BukkitPlugin mPlugin;
    private BukkitComponentFactory mFactory;
    private boolean isInit = false;

    // will be receive from LightAPI class
    private IChunkObserver mChunkObserver;
    private ILightEngine mLightEngine;
    private BukkitBackgroundService mBukkitBackgroundService;
    private IExtension mExtension;
    private UUID mUUID;

    public BukkitPlatformImpl(BukkitPlugin plugin) {
        this.mPlugin = plugin;
    }

    @Override
    public int prepare() {
        // debug mode
        this.DEBUG = mPlugin.getConfig().getBoolean(ConfigurationPath.GENERAL_DEBUG);

        // create component factory
        this.mFactory = new BukkitComponentFactory(this);
        return ResultCode.SUCCESS;
    }

    @Override
    public int initialization() {
        try {
            this.mFactory.init();
        } catch (Exception e) {
            e.printStackTrace();
            return ResultCode.FAILED;
        }
        isInit = true;
        return ResultCode.SUCCESS;
    }

    @Override
    public void shutdown() {
        this.mFactory.shutdown();
        isInit = false;
    }

    @Override
    public boolean isInitialized() {
        return isInit;
    }

    @Override
    public void log(String msg) {
        mPlugin.getServer().getConsoleSender().sendMessage(ChatColor.AQUA + "<LightAPI>: " + ChatColor.WHITE + msg);
    }

    @Override
    public PlatformType getPlatformType() {
        return getHandler().getPlatformType();
    }

    @Override
    public IComponentFactory getFactory() {
        return mFactory;
    }

    @Override
    public ILightEngine getLightEngine() {
        return mLightEngine;
    }

    @Override
    public IChunkObserver getChunkObserver() {
        return mChunkObserver;
    }

    @Override
    public IBackgroundService getBackgroundService() {
        return mBukkitBackgroundService;
    }

    @Override
    public IExtension getExtension() {
        return mExtension;
    }

    @Override
    public boolean isWorldAvailable(String worldName) {
        return getPlugin().getServer().getWorld(worldName) != null;
    }

    /* @hide */
    public int sendCmdLocked(int cmdId, Object... args) {
        int resultCode = ResultCode.SUCCESS;
        // handle internal codes
        switch (cmdId) {
            case InternalCode.UPDATE_BACKGROUND_SERVICE:
                mBukkitBackgroundService = (BukkitBackgroundService) args[0];
                break;
            case InternalCode.UPDATE_LIGHT_ENGINE:
                mLightEngine = (ILightEngine) args[0];
                break;
            case InternalCode.UPDATE_CHUNK_OBSERVER:
                mChunkObserver = (IChunkObserver) args[0];
                break;
            case InternalCode.UPDATE_EXTENSION:
                mExtension = (IExtension) args[0];
                break;
            case InternalCode.UPDATE_UUID:
                mUUID = (UUID) args[0];
                break;
            default:
                resultCode = getHandler().sendCmd(cmdId, args);
                break;
        }
        return resultCode;
    }

    @Override
    public int sendCmd(int cmdId, Object... args) {
        return sendCmdLocked(cmdId, args);
    }

    @Override
    public void onImplementationChanged() {
        // do something
    }

    @Override
    public UUID getUUID() {
        return mUUID;
    }

    @Override
    public void debug(String msg) {
        if (DEBUG) {
            log("[DEBUG] " + msg);
        }
    }

    @Override
    public void error(String msg) {
        log("[ERROR] " + msg);
    }

    @Override
    public BukkitPlugin getPlugin() {
        return mPlugin;
    }

    @Override
    public IHandler getHandler() {
        return mFactory.getHandler();
    }

    @Override
    public boolean isMainThread() {
        return getHandler().isMainThread();
    }
}
