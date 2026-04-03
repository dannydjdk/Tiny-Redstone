package com.dannyandson.tinyredstone.compat.jade;

import com.dannyandson.tinyredstone.blocks.PanelBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(JadePanelComponentProvider.INSTANCE, PanelBlock.class);
        registration.registerBlockIcon(JadePanelComponentProvider.INSTANCE, PanelBlock.class);
    }
}