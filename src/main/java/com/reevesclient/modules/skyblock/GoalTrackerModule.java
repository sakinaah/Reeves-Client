package com.reevesclient.modules.skyblock;

import com.google.gson.*;
import com.reevesclient.core.module.Module;
import com.reevesclient.core.module.ModuleCategory;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Personal goal and note system. Stored locally — never sent anywhere. */
public class GoalTrackerModule extends Module {

    public enum GoalType { COINS, SKILL_LEVEL, ITEM, COLLECTION, CUSTOM }

    public static final class Goal {
        public String   id;
        public String   name;
        public GoalType type;
        public double   current;
        public double   target;
        public String   notes;
        public boolean  completed;

        public Goal(String name, GoalType type, double target) {
            this.id        = UUID.randomUUID().toString();
            this.name      = name;
            this.type      = type;
            this.target    = target;
            this.current   = 0;
            this.notes     = "";
            this.completed = false;
        }

        public double percentComplete() {
            return target > 0 ? Math.min(100.0, (current / target) * 100) : (completed ? 100 : 0);
        }
    }

    private final List<Goal> goals = new ArrayList<>();
    private final Path       saveFile;
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public GoalTrackerModule() {
        super("goal_tracker", "Goal Tracker",
              "Set and track personal SkyBlock goals. Stored locally.",
              ModuleCategory.SKYBLOCK, false);
        saveFile = FabricLoader.getInstance().getConfigDir()
                               .resolve("reeves-client/goals.json");
        load();
    }

    public Goal addGoal(String name, GoalType type, double target) {
        Goal g = new Goal(name, type, target);
        goals.add(g);
        save();
        return g;
    }

    public void removeGoal(String id)      { goals.removeIf(g -> g.id.equals(id)); save(); }
    public void update(Goal goal)          { save(); }
    public List<Goal> getGoals()           { return Collections.unmodifiableList(goals); }
    public List<Goal> getPending()         { return goals.stream().filter(g -> !g.completed).toList(); }
    public List<Goal> getCompleted()       { return goals.stream().filter(g -> g.completed).toList(); }

    private void save() {
        try {
            Files.createDirectories(saveFile.getParent());
            JsonArray arr = new JsonArray();
            for (Goal g : goals) arr.add(GSON.toJsonTree(g));
            try (Writer w = new OutputStreamWriter(Files.newOutputStream(saveFile), StandardCharsets.UTF_8)) {
                GSON.toJson(arr, w);
            }
        } catch (IOException e) {
            com.reevesclient.ReevesClient.LOGGER.warn("Failed to save goals: {}", e.getMessage());
        }
    }

    private void load() {
        if (!Files.exists(saveFile)) return;
        try (Reader r = new InputStreamReader(Files.newInputStream(saveFile), StandardCharsets.UTF_8)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonArray();
            for (JsonElement el : arr) {
                goals.add(GSON.fromJson(el, Goal.class));
            }
        } catch (Exception e) {
            com.reevesclient.ReevesClient.LOGGER.warn("Failed to load goals: {}", e.getMessage());
        }
    }
}
