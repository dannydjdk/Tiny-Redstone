package com.dannyandson.tinyredstone;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Config {
    public static ForgeConfigSpec SERVER_CONFIG;
    public static ForgeConfigSpec CLIENT_CONFIG;

    public static final String CATEGORY_FEATURE = "feature";
    public static final String CATEGORY_PERFORMANCE = "performance";
    public static ForgeConfigSpec.BooleanValue TORCH_LIGHT;
    public static ForgeConfigSpec.IntValue DISPLAY_MODE;
    public static ForgeConfigSpec.BooleanValue JSON_BLUEPRINT;

    static {

        ForgeConfigSpec.Builder SERVER_BUILDER = new ForgeConfigSpec.Builder();

        SERVER_BUILDER.comment("Feature Settings").push(CATEGORY_FEATURE);

        JSON_BLUEPRINT = SERVER_BUILDER.comment("Should it be possible to export or import the blueprint as json? (default:true)")
                .define("json_blueprint",true);

        SERVER_BUILDER.pop();

        SERVER_BUILDER.comment("Performance Settings").push(CATEGORY_PERFORMANCE);

        TORCH_LIGHT = SERVER_BUILDER.comment("Should redstone torches output light to the surrounding area? (default:false)")
                .define("torch_light",false);

        SERVER_BUILDER.pop();

        SERVER_CONFIG = SERVER_BUILDER.build();

        ForgeConfigSpec.Builder CLIENT_BUILDER = new ForgeConfigSpec.Builder();

        CLIENT_BUILDER.comment("Performance Settings").push(CATEGORY_PERFORMANCE);

        DISPLAY_MODE = CLIENT_BUILDER.comment("When should the information be displayed in theoneprobe? 0 = always, 1 = only in extended or debug, 2 = when you have a wrench in your hand")
                .defineInRange("display_mode", 0, 0, 2);

        CLIENT_BUILDER.pop();

        CLIENT_CONFIG = CLIENT_BUILDER.build();
    }


}
