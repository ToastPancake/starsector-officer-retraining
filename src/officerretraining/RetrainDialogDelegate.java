package officerretraining;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.ui.ButtonAPI;
import com.fs.starfarer.api.ui.CustomPanelAPI;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.ui.UIPanelAPI;
import com.fs.starfarer.api.util.Misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RetrainDialogDelegate {
    private final PersonAPI officer;
    private final Object confirmDialog;
    private static Class<?> actionListenerInterface = null;
    private String selectedPersonality = null;
    
    private static final List<String> PERSONALITY_ORDER = Arrays.asList("timid", "cautious", "steady", "aggressive", "reckless");

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
        float width = 600f;
        float height = 300f;

        CustomPanelAPI panel = Global.getSettings().createCustom(width, height, null);
        TooltipMakerAPI info = panel.createUIElement(width, height, true);

        info.addSectionHeading("Retrain Personality", com.fs.starfarer.api.ui.Alignment.MID, 0f);
        info.addSpacer(10f);

        int level = officer.getStats().getLevel();
        final int baseCreditsCost = Settings.creditsCost + (int) (Settings.creditsCost * Settings.levelScalingMultiplier * level);
        final int baseSpCost = Settings.storyPointsCost + (int) (Settings.storyPointsCost * Settings.levelScalingMultiplier * level);
        
        final int[] finalCreditsCost = new int[1];
        final int[] finalSpCost = new int[1];

        info.addPara("Retraining will change the officer's combat personality. Select a new personality to preview costs.", 0f);
        info.addSpacer(15f);

        final LabelAPI costLabelCredits = info.addPara("Credits: -", 0f);
        final LabelAPI costLabelSp = info.addPara("Story Points: -", 0f);
        
        info.addSpacer(20f);

        String[] personalities = {"timid", "cautious", "steady", "aggressive", "reckless"};
        String[] displayNames = {"Timid", "Cautious", "Steady", "Aggressive", "Reckless"};

        float innerWidth = width - 20f;
        
        float btnWidth = 110f;
        float btnHeight = 30f;
        float padding = 5f;
        float totalWidth = (btnWidth * personalities.length) + (padding * (personalities.length - 1));
        float startX = (innerWidth - totalWidth) / 2f;

        CustomPanelAPI rowPanel = Global.getSettings().createCustom(innerWidth, 60f, null);
        
        final List<ButtonAPI> checkboxes = new ArrayList<>();
        final ButtonAPI[] confirmBtnRef = new ButtonAPI[1];
        final String currentPersonality = officer.getPersonalityAPI().getId();
        
        if (Settings.unlocksPersonalityPermanently) {
            officer.getMemoryWithoutUpdate().set("$officer_retraining_unlocked_" + currentPersonality, true);
        }

        for (int i = 0; i < personalities.length; i++) {
            final String pId = personalities[i];
            String pName = displayNames[i];

            boolean isCurrent = currentPersonality.equals(pId);
            
            TooltipMakerAPI cell = rowPanel.createUIElement(btnWidth, 60f, false);
            final ButtonAPI btn = cell.addAreaCheckbox(pName, pId, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), btnWidth, btnHeight, 0f);
            checkboxes.add(btn);
            
            if (isCurrent) {
                btn.setEnabled(false);
                cell.addPara("Current", Misc.getGrayColor(), 0f).setAlignment(com.fs.starfarer.api.ui.Alignment.MID);
            } else {
                attachListener(btn, new Runnable() {
                    @Override
                    public void run() {
                        for (ButtonAPI cb : checkboxes) {
                            if (cb != btn) cb.setChecked(false);
                        }
                        btn.setChecked(true);
                        selectedPersonality = pId;
                        
                        int steps = 0;
                        if (PERSONALITY_ORDER.contains(currentPersonality) && PERSONALITY_ORDER.contains(pId)) {
                            steps = Math.abs(PERSONALITY_ORDER.indexOf(pId) - PERSONALITY_ORDER.indexOf(currentPersonality));
                        }
                        
                        finalCreditsCost[0] = baseCreditsCost + (int) (Settings.creditsCost * Settings.stepScalingMultiplier * steps);
                        finalSpCost[0] = baseSpCost + (int) (Settings.storyPointsCost * Settings.stepScalingMultiplier * steps);
                        
                        if (Settings.unlocksPersonalityPermanently && officer.getMemoryWithoutUpdate().getBoolean("$officer_retraining_unlocked_" + pId)) {
                            finalCreditsCost[0] = 0;
                            finalSpCost[0] = 0;
                        }
                        
                        boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= finalCreditsCost[0] && 
                                            Global.getSector().getPlayerStats().getStoryPoints() >= finalSpCost[0];
                        
                        String creditsText = finalCreditsCost[0] > 0 ? Misc.getDGSCredits(finalCreditsCost[0]) : "Free";
                        costLabelCredits.setText("Credits: " + creditsText);
                        costLabelCredits.setHighlightColors(Misc.getHighlightColor());
                        costLabelCredits.setHighlight(creditsText);

                        String spText = finalSpCost[0] > 0 ? String.valueOf(finalSpCost[0]) : "Free";
                        costLabelSp.setText("Story Points: " + spText);
                        costLabelSp.setHighlightColors(Misc.getStoryOptionColor());
                        costLabelSp.setHighlight(spText);

                        if (confirmBtnRef[0] != null) {
                            confirmBtnRef[0].setEnabled(canAfford);
                        }
                    }
                });
            }
            rowPanel.addUIElement(cell).inTL(startX + (i * (btnWidth + padding)), 0f);
        }

        info.addCustom(rowPanel, 0f);
        
        info.addSpacer(20f);
        
        CustomPanelAPI buttonRow = Global.getSettings().createCustom(innerWidth, 30f, null);
        
        TooltipMakerAPI confirmCell = buttonRow.createUIElement(100f, 30f, false);
        ButtonAPI confirmBtn = confirmCell.addButton("Confirm", "confirm", 100f, 30f, 0f);
        confirmBtn.setEnabled(false);
        confirmBtnRef[0] = confirmBtn;
        attachListener(confirmBtn, new Runnable() {
            @Override
            public void run() {
                if (selectedPersonality != null) {
                    Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(finalCreditsCost[0]);
                    if (finalSpCost[0] > 0) {
                        Global.getSector().getPlayerStats().spendStoryPoints(finalSpCost[0], false, (com.fs.starfarer.api.campaign.TextPanelAPI) null, false, "Officer personality retrained");
                    }
                    officer.setPersonality(selectedPersonality);
                    if (Settings.unlocksPersonalityPermanently) {
                        officer.getMemoryWithoutUpdate().set("$officer_retraining_unlocked_" + selectedPersonality, true);
                    }
                    dismissDialog();
                }
            }
        });
        buttonRow.addUIElement(confirmCell).inTL((innerWidth / 2f) - 105f, 0f);

        TooltipMakerAPI cancelCell = buttonRow.createUIElement(100f, 30f, false);
        ButtonAPI cancelBtn = cancelCell.addButton("Cancel", "cancel", 100f, 30f, 0f);
        attachListener(cancelBtn, new Runnable() {
            @Override
            public void run() {
                dismissDialog();
            } 
        });
        buttonRow.addUIElement(cancelCell).inTL((innerWidth / 2f) + 5f, 0f);
        
        info.addCustom(buttonRow, 0f);

        panel.addUIElement(info).inTL(0, 0);
        innerPanel.addComponent(panel).inTL(0, 0);
    }
}
