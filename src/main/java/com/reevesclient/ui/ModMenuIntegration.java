package com.reevesclient.ui;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import com.reevesclient.ui.screens.DashboardScreen;

/** Wires the ModMenu "Configure" button to the Reeves Client dashboard. */
public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> new DashboardScreen(parent);
    }
}
