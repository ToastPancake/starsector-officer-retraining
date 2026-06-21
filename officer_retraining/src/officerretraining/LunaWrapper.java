package officerretraining;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class LunaWrapper implements LunaSettingsListener {
    public static void init() {
        LunaSettings.addSettingsListener(new LunaWrapper());
        applyLunaSettings();
    }

    public static void applyLunaSettings() {
        try {
            Integer credits = LunaSettings.getInt("officer_retraining", "creditsCost");
            com.fs.starfarer.api.Global.getLogger(LunaWrapper.class).info("LunaLib creditsCost: " + credits);
            if (credits != null) {
                Settings.creditsCost = credits;
            }

            Integer sp = LunaSettings.getInt("officer_retraining", "storyPointsCost");
            com.fs.starfarer.api.Global.getLogger(LunaWrapper.class).info("LunaLib storyPointsCost: " + sp);
            if (sp != null) {
                Settings.storyPointsCost = sp;
            }
            
            Double levelScaling = LunaSettings.getDouble("officer_retraining", "levelScalingMultiplier");
            com.fs.starfarer.api.Global.getLogger(LunaWrapper.class).info("LunaLib levelScalingMultiplier: " + levelScaling);
            if (levelScaling != null) {
                Settings.levelScalingMultiplier = levelScaling.floatValue();
            }
            
            Double stepScaling = LunaSettings.getDouble("officer_retraining", "stepScalingMultiplier");
            com.fs.starfarer.api.Global.getLogger(LunaWrapper.class).info("LunaLib stepScalingMultiplier: " + stepScaling);
            if (stepScaling != null) {
                Settings.stepScalingMultiplier = stepScaling.floatValue();
            }
            
            Boolean unlocks = LunaSettings.getBoolean("officer_retraining", "unlocksPersonalityPermanently");
            com.fs.starfarer.api.Global.getLogger(LunaWrapper.class).info("LunaLib unlocksPersonalityPermanently: " + unlocks);
            if (unlocks != null) {
                Settings.unlocksPersonalityPermanently = unlocks;
            }
        } catch (Throwable t) {
            com.fs.starfarer.api.Global.getLogger(LunaWrapper.class).error("Error applying LunaLib settings", t);
        }
    }

    @Override
    public void settingsChanged(String modID) {
        applyLunaSettings();
    }
}
