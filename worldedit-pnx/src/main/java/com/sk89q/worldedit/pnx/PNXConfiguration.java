/*
 * WorldEdit, a Minecraft world manipulation toolkit
 * Copyright (C) sk89q <http://www.sk89q.com>
 * Copyright (C) WorldEdit team and contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sk89q.worldedit.pnx;

import com.sk89q.util.yaml.YAMLProcessor;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.util.YAMLConfiguration;
import com.sk89q.worldedit.util.report.Unreported;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.nio.file.Path;

/**
 * YAMLConfiguration but with setting for no op permissions and plugin root data folder.
 */
public class PNXConfiguration extends YAMLConfiguration {

    @Unreported
    private final PNXWorldEditPlugin plugin;
    public boolean noOpPermissions = false;
    public boolean commandBlockSupport = false;
    public boolean unsupportedVersionEditing = false;

    public PNXConfiguration(YAMLProcessor config, PNXWorldEditPlugin plugin) {
        super(config, LogManager.getLogger(plugin.getDescription().getMain()));
        this.plugin = plugin;
    }

    @Override
    public void load() {
        super.load();
        noOpPermissions = config.getBoolean("no-op-permissions", false);
        commandBlockSupport = config.getBoolean("command-block-support", false);
        unsupportedVersionEditing = "I accept that I will receive no support with this flag enabled.".equals(
                config.getString("allow-editing-on-unsupported-versions", "false"));
        if (unsupportedVersionEditing) {
            WorldEdit.logger.warn("Editing without a Bukkit adapter has been enabled. You will not receive support "
                    + "for any issues that arise as a result.");
        }
        migrateLegacyFolders();
    }

    private void migrateLegacyFolders() {
        migrate(scriptsDir, "craftscripts");
        migrate(saveDir, "schematics");
        migrate("drawings", "draw.js images");
    }

    private void migrate(String file, String name) {
        File fromDir = new File(".", file);
        File toDir = new File(getWorkingDirectoryPath().toFile(), file);
        if (fromDir.exists() & !toDir.exists()) {
            if (fromDir.renameTo(toDir)) {
                plugin.getLogger().info("Migrated " + name + " folder '" + file
                        + "' from server root to plugin data folder.");
            } else {
                plugin.getLogger().warning("Error while migrating " + name + " folder!");
            }
        }
    }

    @Override
    public Path getWorkingDirectoryPath() {
        return plugin.getDataFolder().toPath();
    }

}
