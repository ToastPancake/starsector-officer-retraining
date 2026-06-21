package officerretraining;

import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaSettings.LunaSettingsListener;

public class LunaWrapper implements LunaSettingsListener {
    public static void init() {
        LunaSettings.addSettingsListener(new LunaWrapper());
        applyLunaSettings();
    }

    public static void applyLunaSettings() {
        Integer credits = LunaSettings.getInt("officer_retraining", "creditsCost");
        if (credits != null) {
            Settings.creditsCost = credits;
        }

        Integer sp = LunaSettings.getInt("officer_retraining", "storyPointsCost");
        if (sp != null) {
            Settings.storyPointsCost = sp;
        }
        
        Double levelScaling = LunaSettings.getDouble("officer_retraining", "levelScalingMultiplier");
        if (levelScaling != null) {
            Settings.levelScalingMultiplier = levelScaling.floatValue();
        }
        
        Double stepScaling = LunaSettings.getDouble("officer_retraining", "stepScalingMultiplier");
        if (stepScaling != null) {
            Settings.stepScalingMultiplier = stepScaling.floatValue();
        }
        
        Boolean unlocks = LunaSettings.getBoolean("officer_retraining", "unlocksPersonalityPermanently");
        if (unlocks != null) {
            Settings.unlocksPersonalityPermanently = unlocks;
        }
    }

    @Override
    public void settingsChanged(String modID) {
        applyLunaSettings();
    }
}
