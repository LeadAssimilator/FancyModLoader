/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.loading.moddiscovery.locators;

import net.neoforged.neoforgespi.ILaunchContext;
import net.neoforged.neoforgespi.locating.IDiscoveryPipeline;
import net.neoforged.neoforgespi.locating.IModFileCandidateLocator;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Any file that remains will just be added to the library set.
 */
public class ClasspathLibrariesLocator implements IModFileCandidateLocator {
    @Override
    public void findCandidates(ILaunchContext context, IDiscoveryPipeline pipeline) {
        // When the application class-loader is reachable, do not run
        var loader = getClass().getClassLoader();
        do {
            if (loader == ClassLoader.getSystemClassLoader()) {
                return;
            }
            loader = loader.getParent();
        } while (loader != null);

        for (var classPathItem : System.getProperty("java.class.path").split(File.pathSeparator)) {
            Path path = Paths.get(classPathItem);
            if (Files.isRegularFile(path) && !context.isLocated(path)) {
                pipeline.addLibrary(path);
            }
        }
    }

    @Override
    public int getPriority() {
        return LOWEST_SYSTEM_PRIORITY;
    }

    @Override
    public String toString() {
        return "classpath libraries locator";
    }
}
