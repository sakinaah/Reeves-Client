package com.reevesclient.modules.utility;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

/**
 * Provides a browseable list of screenshots stored in .minecraft/screenshots/.
 * Does not take screenshots on its own — the player still uses F2.
 */
public class ScreenshotManagerModule extends Module {

    private final Path screenshotDir;
    private List<File> screenshots = new ArrayList<>();

    public ScreenshotManagerModule() {
        super("screenshot_manager", "Screenshot Manager",
              "Browse and manage your screenshots from inside the game.",
              ModuleCategory.UTILITY, false);
        screenshotDir = FabricLoader.getInstance().getGameDir().resolve("screenshots");
    }

    /** Refreshes the in-memory list from disk. Call before displaying the UI. */
    public void refresh() {
        File dir = screenshotDir.toFile();
        if (!dir.exists() || !dir.isDirectory()) {
            screenshots = new ArrayList<>();
            return;
        }
        File[] files = dir.listFiles(f -> f.isFile() &&
                (f.getName().endsWith(".png") || f.getName().endsWith(".jpg")));
        screenshots = files == null ? new ArrayList<>() : Arrays.asList(files);
        screenshots.sort(Comparator.comparingLong(File::lastModified).reversed());
    }

    public List<File> getScreenshots()           { return Collections.unmodifiableList(screenshots); }
    public Path       getScreenshotDir()         { return screenshotDir; }
    public int        count()                    { return screenshots.size(); }

    public void delete(File file) {
        if (file.delete()) screenshots.remove(file);
    }

    public void openInExplorer(File file) {
        try {
            java.awt.Desktop.getDesktop().browse(file.getParentFile().toURI());
        } catch (Exception ignored) {}
    }
}
