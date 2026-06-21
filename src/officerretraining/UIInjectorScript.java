package officerretraining;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.ui.UIComponentAPI;
import com.fs.starfarer.api.ui.PositionAPI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import com.fs.starfarer.api.campaign.InteractionDialogAPI;

public class UIInjectorScript implements EveryFrameScript {
    private Object lastCpdRef = null;
    private final Map<Object, CustomPanelAPI> injectedPanels = new HashMap<>();
    private static Class<?> actionListenerInterface = null;

    @Override
    public boolean isDone() {
        return false;
    }

    @Override
    public boolean runWhilePaused() {
        return true;
    }

    private boolean dumped = false;

    private int lastCaptainDialogIndex = -1;
    private boolean f11Pressed = false;

    @Override
    public void advance(float amount) {
        if (!ClassRefs.hasFoundAllClasses()) {
            if (Global.getSector().getCampaignUI() != null && !Global.getSector().getCampaignUI().isShowingDialog()) {
                ClassRefs.findAllClasses();
            }
        }

        boolean f11 = org.lwjgl.input.Keyboard.isKeyDown(org.lwjgl.input.Keyboard.KEY_F11);
        if (f11 && !f11Pressed) {
            f11Pressed = true;
            dumpUITree();
        } else if (!f11) {
            f11Pressed = false;
        }

        Object cpd = findCaptainPickerDialog();
        if (cpd != null) {
            injectCaptainPickerDialog(cpd);
        }
    }

    private void dumpUITree() {
        Global.getLogger(UIInjectorScript.class).info("================ DUMPING UI TREE ================");
        UIPanelAPI core = UtilReflection.getCoreUI();
        if (core != null) {
            Global.getLogger(UIInjectorScript.class).info("CoreUI found: " + core.getClass().getName());
            dumpNode(core, 0);
        } else {
            Global.getLogger(UIInjectorScript.class).info("CoreUI is NULL");
        }
        Global.getLogger(UIInjectorScript.class).info("================================================");
    }

    private void dumpNode(Object comp, int depth) {
        if (comp == null) return;
        StringBuilder sb = new StringBuilder();
        for (int i=0; i<depth; i++) sb.append("  ");
        sb.append(comp.getClass().getName());
        
        if (comp instanceof LabelAPI) {
            sb.append(" [Label: \"").append(((LabelAPI)comp).getText()).append("\"]");
        } else if (comp instanceof UIComponentAPI) {
            PersonAPI p = getPersonFromComponent((UIComponentAPI)comp);
            if (p != null) {
                sb.append(" [Has PersonAPI: ").append(p.getNameString()).append("]");
            }
        }
        Global.getLogger(UIInjectorScript.class).info(sb.toString());

        if (comp.getClass().getName().equals("com.fs.starfarer.coreui.CaptainPickerDialog")) {
            Object listOfficers = UtilReflection.invokeGetter(comp, "getListOfficers");
            if (listOfficers != null) {
                List<?> items = (List<?>) UtilReflection.invokeGetter(listOfficers, "getItems");
                if (items != null) {
                    for (Object child : items) {
                        dumpNode(child, depth + 1);
                    }
                }
            }
        } else if (comp instanceof UIPanelAPI) {
            List<?> children = (List<?>) UtilReflection.invokeGetter(comp, "getChildrenNonCopy");
            if (children != null) {
                for (Object child : children) {
                    dumpNode(child, depth + 1);
                }
            }
        }
    }

    private PersonAPI getPersonFromComponent(Object comp) {
        if (comp == null) return null;
        try {
            Class<?> clazz = comp.getClass();
            while (clazz != null && clazz != Object.class) {
                for (java.lang.reflect.Field f : clazz.getDeclaredFields()) {
                    f.setAccessible(true);
                    Object val = f.get(comp);
                    if (val instanceof com.fs.starfarer.api.characters.OfficerDataAPI) {
                        return ((com.fs.starfarer.api.characters.OfficerDataAPI) val).getPerson();
                    } else if (val instanceof PersonAPI) {
                        return (PersonAPI) val;
                    }
                }
                clazz = clazz.getSuperclass();
            }
        } catch (Exception e) {}
        return null;
    }

    private LabelAPI findPersonalityLabel(UIComponentAPI comp, String personalityName) {
        if (comp instanceof LabelAPI) {
            String text = ((LabelAPI)comp).getText();
            if (text != null && text.toLowerCase().contains(personalityName.toLowerCase())) {
                return (LabelAPI) comp;
            }
        }
        if (comp instanceof UIPanelAPI) {
            List<?> children = (List<?>) UtilReflection.invokeGetter(comp, "getChildrenNonCopy");
            if (children != null) {
                for (Object child : children) {
                    if (child instanceof UIComponentAPI) {
                        LabelAPI res = findPersonalityLabel((UIComponentAPI)child, personalityName);
                        if (res != null) return res;
                    }
                }
            }
        }
        return null;
    }

    private Object findCaptainPickerDialog() {
        UIPanelAPI core = UtilReflection.getCoreUI();
        if (core == null) return null;
        List<?> items = (List<?>) UtilReflection.invokeGetter(core, "getChildrenNonCopy");
        if (items != null && !items.isEmpty()) {
            if (lastCaptainDialogIndex >= 0 && 
                lastCaptainDialogIndex < items.size() && 
                items.get(lastCaptainDialogIndex).getClass().getName().equals("com.fs.starfarer.coreui.CaptainPickerDialog")) {
                return items.get(lastCaptainDialogIndex);
            }
            for (int i = 0; i < items.size(); i++) {
                Object component = items.get(i);
                if (component.getClass().getName().equals("com.fs.starfarer.coreui.CaptainPickerDialog")) {
                    lastCaptainDialogIndex = i;
                    return component;
                }
            }
        }
        return null;
    }

    private void injectCaptainPickerDialog(Object cpd) {
        Object listOfficers = UtilReflection.invokeGetter(cpd, "getListOfficers");
        if (listOfficers != null) {
            List<?> items = (List<?>) UtilReflection.invokeGetter(listOfficers, "getItems");
            if (items != null) {
                for (Object item : items) {
                    if (item instanceof UIComponentAPI) {
                        UIComponentAPI uiComp = (UIComponentAPI) item;
                        PersonAPI person = getPersonFromComponent(uiComp);
                        if (person != null && !person.isPlayer() && !person.isAICore()) {
                            LabelAPI statusLabel = findPersonalityLabel(uiComp, person.getPersonalityAPI().getDisplayName());
                            if (statusLabel != null) {
                                boolean needsInjection = true;
                                if (injectedPanels.containsKey(uiComp)) {
                                    CustomPanelAPI panel = injectedPanels.get(uiComp);
                                    if (uiComp instanceof UIPanelAPI) {
                                        List<?> children = (List<?>) UtilReflection.invokeGetter(uiComp, "getChildrenNonCopy");
                                        if (children != null && children.contains(panel)) {
                                            needsInjection = false;
                                        }
                                    } else {
                                        needsInjection = false; 
                                    }
                                }
                                
                                if (needsInjection) {
                                    inject(uiComp, person, statusLabel);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void showRetrainDialog(final PersonAPI person) {
        try {
            ClassRefs.findAllClasses();
            if (!ClassRefs.hasFoundAllClasses()) {
                Global.getLogger(UIInjectorScript.class).error("Failed to find ClassRefs for ConfirmDialog hack! " +
                    "confirmDialogClass=" + (ClassRefs.confirmDialogClass != null) +
                    ", dialogDismissedInterface=" + (ClassRefs.dialogDismissedInterface != null) +
                    ", uiPanelClass=" + (ClassRefs.uiPanelClass != null));
                return;
            }

            java.lang.reflect.InvocationHandler handler = new java.lang.reflect.InvocationHandler() {
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    return null;
                }
            };
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                UIInjectorScript.class.getClassLoader(),
                new Class<?>[]{ClassRefs.dialogDismissedInterface},
                handler
            );

            java.lang.reflect.Constructor<?> cons = ClassRefs.confirmDialogClass.getConstructor(
                Float.TYPE, Float.TYPE, ClassRefs.uiPanelClass, ClassRefs.dialogDismissedInterface, String.class, String[].class
            );

            Object screenPanel = UtilReflection.invokeGetter(Global.getSector().getCampaignUI(), "getScreenPanel");
            if (screenPanel == null) {
                screenPanel = ClassRefs.getField(Global.getSector().getCampaignUI(), "screenPanel");
            }

            final Object confirmDialog = cons.newInstance(
                600f, 300f, screenPanel, proxy, "", new String[]{} 
            );

            java.lang.reflect.Method show = confirmDialog.getClass().getMethod("show", Float.TYPE, Float.TYPE);
            show.invoke(confirmDialog, 0.25f, 0.25f);

            UIPanelAPI innerPanel = (UIPanelAPI) UtilReflection.invokeGetter(confirmDialog, "getInnerPanel");
            if (innerPanel != null) {
                RetrainDialogDelegate delegate = new RetrainDialogDelegate(person, confirmDialog);
                delegate.createCustomDialog(innerPanel);
            } else {
                Global.getLogger(UIInjectorScript.class).error("getInnerPanel returned null!");
            }

        } catch (Exception e) {
            Global.getLogger(UIInjectorScript.class).error("Error showing ConfirmDialog hack", e);
        }
    }

    private static void initActionListener(Object buttonObj) {
        if (actionListenerInterface == null) {
            for (java.lang.reflect.Method m : buttonObj.getClass().getMethods()) {
                if (m.getName().equals("setListener") && m.getParameterTypes().length == 1) {
                    actionListenerInterface = m.getParameterTypes()[0];
                    Global.getLogger(UIInjectorScript.class).info("FOUND actionListenerInterface via button: " + actionListenerInterface.getName());
                    return;
                }
            }
            Global.getLogger(UIInjectorScript.class).info("FAILED to find actionListenerInterface on Button!");
        }
    }

    private void setButtonListener(ButtonAPI button, final PersonAPI person) {
        initActionListener(button);
        if (actionListenerInterface == null) return;
        
        try {
            java.lang.reflect.InvocationHandler handler = new java.lang.reflect.InvocationHandler() {
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("actionPerformed")) {
                        showRetrainDialog(person);
                    }
                    return null;
                }
            };
            
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                UIInjectorScript.class.getClassLoader(),
                new Class<?>[]{actionListenerInterface},
                handler
            );
            
            java.lang.reflect.Method setListener = button.getClass().getMethod("setListener", actionListenerInterface);
            setListener.invoke(button, proxy);
        } catch (Exception e) {
            Global.getLogger(UIInjectorScript.class).error("Error setting listener", e);
        }
    }

    private void inject(UIComponentAPI elem, PersonAPI person, LabelAPI statusLabel) {
        try {
            CustomPanelAPI dummyPanel = Global.getSettings().createCustom(70f, 20f, null);
            TooltipMakerAPI tooltipMaker = dummyPanel.createUIElement(70f, 20f, false);
            
            ButtonAPI button = tooltipMaker.addButton("Retrain", "retrain", Global.getSettings().getBasePlayerColor(), Global.getSettings().getDarkPlayerColor(), com.fs.starfarer.api.ui.Alignment.MID, com.fs.starfarer.api.ui.CutStyle.ALL, 70f, 20f, 0f);
            setButtonListener(button, person);
            dummyPanel.addUIElement(tooltipMaker).inTL(0f, 0f);
            
            boolean added = false;
            if (elem instanceof UIPanelAPI) {
                ((UIPanelAPI)elem).addComponent(dummyPanel);
                added = true;
            } else {
                for (java.lang.reflect.Method m : elem.getClass().getMethods()) {
                    if ((m.getName().equals("addComponent") || m.getName().equals("addUIElement")) && m.getParameterTypes().length == 1) {
                        m.invoke(elem, dummyPanel);
                        added = true;
                        break;
                    }
                }
            }
            
            if (!added) {
                return;
            }
            
            // Anchor it to the Top Right, to the left of the salary and below the level up button
            dummyPanel.getPosition().inTR(160f, 35f);
            
            injectedPanels.put(elem, dummyPanel);
        } catch (Exception e) {
            Global.getLogger(UIInjectorScript.class).error("Error injecting UI", e);
        }
    }
}
