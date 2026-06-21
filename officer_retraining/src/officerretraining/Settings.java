package officerretraining;

import com.fs.starfarer.api.Global;
import org.json.JSONObject;

public class Settings {
    public static int creditsCost = 10000;
    public static int storyPointsCost = 0;
    public static float levelScalingMultiplier = 0.0f;
    public static float stepScalingMultiplier = 0.0f;
    public static boolean unlocksPersonalityPermanently = false;

    public static void loadSettings() {
        try {
            JSONObject settings = Global.getSettings().loadJSON("data/config/officer_retraining_settings.json");
            creditsCost = settings.optInt("creditsCost", 10000);
            storyPointsCost = settings.optInt("storyPointsCost", 0);
            levelScalingMultiplier = (float) settings.optDouble("levelScalingMultiplier", 0.0);
            stepScalingMultiplier = (float) settings.optDouble("stepScalingMultiplier", 0.0);
            unlocksPersonalityPermanently = settings.optBoolean("unlocksPersonalityPermanently", false);
        } catch (Exception e) {
            Global.getLogger(Settings.class).error("Failed to load officer_retraining_settings.json", e);
        }
    }
}
