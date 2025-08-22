package com.github.squi2rel.mcft;

import dev.architectury.injectables.annotations.ExpectPlatform;

import java.nio.file.Path;

public class Platform {
    @ExpectPlatform
    public static Path getConfigPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static Path getClientConfigPath() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static String getVersion() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void register() {
        throw new AssertionError();
    }

    @ExpectPlatform
    public static void registerCommand() {
        throw new AssertionError();
    }
}
