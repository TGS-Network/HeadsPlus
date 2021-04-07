package io.github.thatsmusic99.headsplus.config;

import io.github.thatsmusic99.configurationmaster.CMFile;
import io.github.thatsmusic99.headsplus.HeadsPlus;
import io.github.thatsmusic99.headsplus.crafting.RecipeEnums;
import org.bukkit.DyeColor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ConfigCrafting extends CMFile {

	public ConfigCrafting() {
		super(HeadsPlus.getInstance(), "crafting");
    }

	@Override
	public void loadDefaults() {
		addDefault("base-item.material", "PLAYER_HEAD");
		addDefault("base-item.data", 0);
		addDefault("base-item.price", 10.0);
		addDefault("base-item.display-name", "{type} Head");
		addDefault("base-item.lore", new ArrayList<>(Arrays.asList("&7Price &8» &c{price}", "&7Type &8» &c{type}")));
		addDefault("base-item.use-base-item", true);
		for (RecipeEnums key : RecipeEnums.values()) {
			if (key == RecipeEnums.SHEEP) {
				for (DyeColor d : DyeColor.values()) {
					if (d.name().equalsIgnoreCase("LIGHT_GRAY")) { // stupid move ngl
						addDefault(key.str + "." + d.name() + ".head", "HP#silver_sheep");
					} else {
						addDefault(key.str + "." + d.name() + ".head", "HP#" + d.name().toLowerCase() + "_sheep");
					}
					addDefault(key.str + "." + d.name() + ".ingredients", new ArrayList<>(Collections.singletonList(d.name() + "_WOOL")));
					addDefault(key.str + "." + d.name() + ".price", "{default}");
					addDefault(key.str + "." + d.name() + ".display-name", "{default}");
					addDefault(key.str + "." + d.name() + ".display-type", HeadsPlus.capitalize(key.name().toLowerCase().replaceAll("_", " ")));
					addDefault(key.str + "." + d.name() + ".lore", "{default}");
					addDefault(key.str + "." + d.name() + ".shaped", false);
					addDefault(key.str + "." + d.name() + ".sellhead-id", key.name());
				}
				continue;
			} else {
				addDefault(key.str + ".ingredients", new ArrayList<>(Collections.singletonList(key.mat)));
				switch (key) {
					case RABBIT:
						addDefault(key.str + ".head", "HP#brown_" + key.name().toLowerCase());
						break;
					case MUSHROOM_COW:
						addDefault(key.str + ".head", "HP#red_mooshroom");
						break;
					case VILLAGER:
						addDefault(key.str + ".head", "HP#villager_plains");
						break;
					default:
						addDefault(key.str + ".head", "HP#" + key.name().toLowerCase());
				}

			}
			addDefault(key.str + ".price", "{default}");
			addDefault(key.str + ".display-name", "{default}");
			addDefault(key.str + ".display-type", HeadsPlus.capitalize(key.name().toLowerCase().replaceAll("_", " ")));
			addDefault(key.str + ".lore", "{default}");
			addDefault(key.str + ".shaped", false);
			addDefault(key.str + ".sellhead-id", key.name());

		}
	}

    public List<String> getLore(String key) {
		List<String> lore = new ArrayList<>();
		try {
			if (get(key + ".lore").equals("{default}")) {
				for (String str : getStringList("base-item.lore")) {
					lore.add(HeadsPlusMessagesManager.get().formatMsg(str, null)
							.replaceAll("\\{type}", getString(key + ".display-type"))
							.replaceAll("\\{price}", String.valueOf(getPrice(key))));
				}
			} else {
				for (String str : getStringList(key + ".lore")) {
					lore.add(HeadsPlusMessagesManager.get().formatMsg(str, null));
				}
			}
		} catch (NullPointerException ex) {
			for (String str : getStringList(key + ".lore")) {
				lore.add(HeadsPlusMessagesManager.get().formatMsg(str, null));
			}
		}

		return lore;
	}

	public double getPrice(String key) {
		return getDouble(key + ".price", getDouble("base-item.price"));
	}

	public String getDisplayName(String key) {
		return HeadsPlusMessagesManager.get().formatMsg(getString(key + ".display-name", getString("base-item.display-name")), null)
				.replaceAll("\\{type}", getString(key + ".display-type", getString("base-item.display-type")));
	}

}
