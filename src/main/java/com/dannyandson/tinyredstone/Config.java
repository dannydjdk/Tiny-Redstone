package com.dannyandson.tinyredstone;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {
    public static ForgeConfigSpec SERVER_CONFIG;
    public static final String CATEGORY_PERFORMANCE = "performance";
    public static ForgeConfigSpec.BooleanValue TORCH_LIGHT;

    static {

        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();


        SERVER_BUILDER.comment("Performance Settings").push(CATEGORY_PERFORMANCE);

        TORCH_LIGHT = SERVER_BUILDER.comment("Should redstone torches output light to the surrounding area? (default:false)")
                .define("torch_light",false);

        SERVER_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();


    }


}
