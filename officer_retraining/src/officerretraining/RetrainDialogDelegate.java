package officerretraining;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

public class RetrainDialogDelegate {
    private final PersonAPI officer;
    private final Object confirmDialog;
    private static Class<?> actionListenerInterface = null;

    public RetrainDialogDelegate(PersonAPI officer, Object confirmDialog) {
        this.officer = officer;
        this.confirmDialog = confirmDialog;
    }

    private static void initActionListener(Object buttonObj) {
        if (actionListenerInterface == null) {
            for (java.lang.reflect.Method m : buttonObj.getClass().getMethods()) {
                if (m.getName().equals("setListener") && m.getParameterTypes().length == 1) {
                    actionListenerInterface = m.getParameterTypes()[0];
                    return;
                }
            }
        }
    }

    private void dismissDialog() {
        try {
            java.lang.reflect.Method dismiss = confirmDialog.getClass().getMethod("dismiss", Integer.TYPE);
            dismiss.invoke(confirmDialog, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void attachListener(ButtonAPI button, final Runnable action) {
        initActionListener(button);
        if (actionListenerInterface == null) return;
        try {
            java.lang.reflect.InvocationHandler handler = new java.lang.reflect.InvocationHandler() {
                @Override
                public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                    if (method.getName().equals("actionPerformed")) {
                        action.run();
                        dismissDialog();
                    }
                    return null;
                }
            };
            Object proxy = java.lang.reflect.Proxy.newProxyInstance(
                RetrainDialogDelegate.class.getClassLoader(),
                new Class<?>[]{actionListenerInterface},
                handler
            );
            java.lang.reflect.Method setListener = button.getClass().getMethod("setListener", actionListenerInterface);
            setListener.invoke(button, proxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createCustomDialog(UIPanelAPI innerPanel) {
        float width = 400f;
        float height = 350f;

        CustomPanelAPI panel = Global.getSettings().createCustom(width, height, null);
        TooltipMakerAPI info = panel.createUIElement(width, height, true);

        info.addSectionHeading("Retrain Personality", com.fs.starfarer.api.ui.Alignment.MID, 0f);
        info.addSpacer(10f);

        int level = officer.getStats().getLevel();
        int creditsCost = Settings.creditsCost + (int) (Settings.creditsCost * Settings.levelScalingMultiplier * level);
        int spCost = Settings.storyPointsCost + (int) (Settings.storyPointsCost * Settings.levelScalingMultiplier * level);

        info.addPara("Retraining will change the officer's combat personality. This action will cost:", 0f);
        info.addSpacer(5f);
        if (creditsCost > 0) {
            info.addPara("Credits: " + Misc.getDGSCredits(creditsCost), 0f, Misc.getHighlightColor(), Misc.getDGSCredits(creditsCost));
        } else {
            info.addPara("Credits: Free", 0f, Misc.getHighlightColor(), "Free");
        }
        
        if (spCost > 0) {
            info.addPara("Story Points: " + spCost, 0f, Misc.getStoryOptionColor(), String.valueOf(spCost));
        } else {
            info.addPara("Story Points: Free", 0f, Misc.getStoryOptionColor(), "Free");
        }
        
        info.addSpacer(10f);

        String[] personalities = {"timid", "cautious", "steady", "aggressive", "reckless"};
        String[] displayNames = {"Timid", "Cautious", "Steady", "Aggressive", "Reckless"};

        final boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= creditsCost && 
                                  Global.getSector().getPlayerStats().getStoryPoints() >= spCost;

        for (int i = 0; i < personalities.length; i++) {
            final String pId = personalities[i];
            String pName = displayNames[i];

            boolean isCurrent = officer.getPersonalityAPI().getId().equals(pId);
            ButtonAPI btn = info.addButton(pName, pId, width - 20f, 30f, 5f);
            
            if (isCurrent) {
                btn.setEnabled(false);
                info.addPara("Already has this personality.", Misc.getGrayColor(), 0f);
            } else if (!canAfford) {
                btn.setEnabled(false);
            } else {
                final int finalCreditsCost = creditsCost;
                final int finalSpCost = spCost;
                attachListener(btn, new Runnable() {
                    @Override
                    public void run() {
                        Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(finalCreditsCost);
                        if (finalSpCost > 0) {
                            Global.getSector().getPlayerStats().spendStoryPoints(finalSpCost, false, (com.fs.starfarer.api.campaign.TextPanelAPI) null, false, "Officer personality retrained");
                        }
                        officer.setPersonality(pId);
                    }
                });
            }
        }

        info.addSpacer(20f);
        ButtonAPI cancelBtn = info.addButton("Cancel", "cancel", width - 20f, 30f, 5f);
        attachListener(cancelBtn, new Runnable() {
            @Override
            public void run() {} // Just dismisses
        });

        panel.addUIElement(info).inTL(0, 0);
        innerPanel.addComponent(panel).inTL(0, 0);
    }
}
