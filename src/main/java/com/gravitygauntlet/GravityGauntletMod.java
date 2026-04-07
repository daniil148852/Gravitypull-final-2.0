package com.gravitygauntlet;

import com.gravitygauntlet.item.GravityGauntletItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GravityGauntletMod implements ModInitializer {
	public static final String MOD_ID = "gravitygauntlet";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	
	public static final Item GRAVITY_GAUNTLET = new GravityGauntletItem(new FabricItemSettings().maxCount(1));

	@Override
	public void onInitialize() {
		Registry.register(Registries.ITEM, new Identifier(MOD_ID, "gravity_gauntlet"), GRAVITY_GAUNTLET);
		
		ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(content -> {
			content.add(GRAVITY_GAUNTLET);
		});
		
		LOGGER.info("Gravity Gauntlet initialized!");
	}
}
