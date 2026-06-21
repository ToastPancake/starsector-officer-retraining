package officerretraining;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import com.fs.starfarer.api.campaign.CoreUIAPI;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UtilReflection {
    public static UIPanelAPI getCoreUI() {
        try {
            CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
            InteractionDialogAPI dialog = campaignUI.getCurrentInteractionDialog();

            if (dialog != null) {
                Object core = invokeGetter(dialog, "getCoreUI");
                if (core instanceof UIPanelAPI) return (UIPanelAPI) core;
                
                if (dialog.getPlugin() != null) {
                    core = invokeGetter(dialog.getPlugin(), "getCoreUI");
                    if (core instanceof UIPanelAPI) return (UIPanelAPI) core;
                    
                    core = invokeGetter(dialog.getPlugin(), "getCore");
                    if (core instanceof UIPanelAPI) return (UIPanelAPI) core;
                }
            }

            if (campaignUI != null) {
                Object core = invokeGetter(campaignUI, "getCore");
                if (core instanceof UIPanelAPI) return (UIPanelAPI) core;

                core = getField(campaignUI, "core");
                if (core instanceof UIPanelAPI) return (UIPanelAPI) core;
            }
        } catch (Exception e) {
            Global.getLogger(UtilReflection.class).error("Error getting CoreUI", e);
        }
        return null;
    }

    public static Object getField(Object o, String fieldName) {
        if (o == null) return null;
        Class<?> clazz = o.getClass();
        while (clazz != null && clazz != Object.class) {
            try {
                java.lang.reflect.Field f = clazz.getDeclaredField(fieldName);
                f.setAccessible(true);
                return f.get(o);
            } catch (Exception e) {}
            clazz = clazz.getSuperclass();
        }
        return null;
    }

    public static Object invokeGetter(Object o, String methodName, Object... args) {
        if (o == null) return null;
        try {
            Class<?>[] argClasses = new Class<?>[args.length];
            for (int i = 0; i < args.length; i++) {
                argClasses[i] = args[i].getClass();
            }
            Class<?> clazz = o.getClass();
            while (clazz != null && clazz != Object.class) {
                try {
                    java.lang.reflect.Method method = clazz.getDeclaredMethod(methodName, argClasses);
                    method.setAccessible(true);
                    return method.invoke(o, args);
                } catch (Exception e) {}
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            Global.getLogger(UtilReflection.class).error("Error invoking " + methodName, e);
        }
        return null;
    }
}
