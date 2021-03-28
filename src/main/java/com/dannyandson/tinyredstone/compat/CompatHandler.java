package com.dannyandson.tinyredstone.compat;

import com.dannyandson.tinyredstone.compat.theoneprobe.PanelProvider;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.ModList;

public class CompatHandler {
    public static void register()  {
        if(ModList.get().isLoaded("theoneprobe")) {
            InterModComms.sendTo("theoneprobe", "getTheOneProbe", PanelProvider::new);
        }
    }
}
