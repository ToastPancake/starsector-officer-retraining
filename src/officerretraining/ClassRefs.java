package officerretraining;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignUIAPI;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.List;

public class ClassRefs {
    public static Class<?> confirmDialogClass;
    public static Class<?> dialogDismissedInterface;
    public static Class<?> uiPanelClass;
    private static boolean foundAllClasses;

    public static void findConfirmDialogClass() {
        CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
        try {
            boolean isPaused = Global.getSector().isPaused();
            boolean showed = campaignUI.showConfirmDialog("", "", "", null, null);
            Global.getLogger(ClassRefs.class).info("findConfirmDialogClass: showed=" + showed);
            if (confirmDialogClass == null && showed) {
                Object screenPanel = UtilReflection.invokeGetter(campaignUI, "getScreenPanel");
                if (screenPanel == null) {
                    screenPanel = getField(campaignUI, "screenPanel");
                }
                Global.getLogger(ClassRefs.class).info("findConfirmDialogClass: screenPanel=" + screenPanel);
                
                List<?> children = (List<?>) UtilReflection.invokeGetter(screenPanel, "getChildrenNonCopy");
                Object panel = children.get(children.size() - 1);
                confirmDialogClass = panel.getClass();
                Global.getLogger(ClassRefs.class).info("findConfirmDialogClass: confirmDialogClass=" + confirmDialogClass);
                Method dismiss = confirmDialogClass.getMethod("dismiss", Integer.TYPE);
                dismiss.invoke(panel, 0);
                Global.getSector().setPaused(isPaused);
            }
        } catch (Exception e) {
            Global.getLogger(ClassRefs.class).error("findConfirmDialogClass exception", e);
            e.printStackTrace();
        }
    }

    public static void findUIPanelClass() {
        CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
        try {
            Field field = campaignUI.getClass().getDeclaredField("screenPanel");
            uiPanelClass = field.getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Class<?> findInterfaceByMethod(Class<?>[] interfaces, String methodName) {
        for (Class<?> cls : interfaces) {
            Method[] methods = cls.getDeclaredMethods();
            if (methods.length == 1 && methods[0].getName().equals(methodName)) {
                return cls;
            }
        }
        return null;
    }

    public static void findDialogDismissedInterface(Object witness) {
        dialogDismissedInterface = findInterfaceByMethod(witness.getClass().getInterfaces(), "dialogDismissed");
    }

    public static void findAllClasses() {
        if (foundAllClasses) return;
        
        CampaignUIAPI campaignUI = Global.getSector().getCampaignUI();
        if (confirmDialogClass == null) {
            findConfirmDialogClass();
        }
        if (dialogDismissedInterface == null) {
            findDialogDismissedInterface(campaignUI);
        }
        if (uiPanelClass == null) {
            findUIPanelClass();
        }
        
        if (confirmDialogClass != null && dialogDismissedInterface != null && uiPanelClass != null) {
            foundAllClasses = true;
        }
    }

    public static boolean hasFoundAllClasses() {
        return foundAllClasses;
    }

    public static Object getField(Object o2, String fieldName) {
        try {
            Class<?> clazz = o2.getClass();
            while (clazz != null && clazz != Object.class) {
                for (Field field : clazz.getDeclaredFields()) {
                    if (field.getName().equals(fieldName)) {
                        field.setAccessible(true);
                        return field.get(o2);
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
