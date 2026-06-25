package com.reevesclient.modules.skyblock;

import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;

import java.util.*;

/**
 * Stateless profit-calculation helpers.
 * All calculations are read-only; nothing is sent to the server.
 */
public class ProfitCalculatorModule extends Module {

    public record FlipResult(String item, double cost, double revenue, double profit, double roi) {}
    public record CraftResult(String item, double materialCost, double sellPrice, double profit) {}

    public ProfitCalculatorModule() {
        super("profit_calculator", "Profit Calculator",
              "Calculate farming, crafting, and flipping profits.",
              ModuleCategory.SKYBLOCK, false);
    }

    /** Simple flip margin: revenue = sell-price after 1% tax. */
    public FlipResult calculateFlip(String itemName, double buyCost, double sellPrice) {
        double tax = sellPrice * 0.01;
        double revenue = sellPrice - tax;
        double profit  = revenue - buyCost;
        double roi     = buyCost > 0 ? (profit / buyCost) * 100 : 0;
        return new FlipResult(itemName, buyCost, revenue, profit, roi);
    }

    /** Calculates crafting profit given a list of (materialId, qty, unitCost) tuples. */
    public CraftResult calculateCraft(String outputItem, double outputSellPrice,
                                      List<double[]> materials /* [qty, unitCost] */) {
        double materialCost = materials.stream().mapToDouble(m -> m[0] * m[1]).sum();
        double tax    = outputSellPrice * 0.01;
        double revenue = outputSellPrice - tax;
        double profit  = revenue - materialCost;
        return new CraftResult(outputItem, materialCost, revenue, profit);
    }

    /**
     * Farming profit per hour.
     * @param yieldPerHour   crops harvested per hour
     * @param sellPriceEach  coins per crop (Bazaar sell-order price)
     * @param farmingFortune farming fortune (affects yield multiplier if known)
     */
    public double farmingProfitPerHour(double yieldPerHour, double sellPriceEach, double farmingFortune) {
        double bonus = 1.0 + farmingFortune / 100.0;
        return yieldPerHour * bonus * sellPriceEach;
    }
}
