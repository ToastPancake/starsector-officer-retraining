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
import java.util.List;

public class RetrainDialogDelegate {
    private final PersonAPI officer;
    private final Object confirmDialog;
    private static Class<?> actionListenerInterface = null;
    private String selectedPersonality = null;

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
        final int creditsCost = Settings.creditsCost + (int) (Settings.creditsCost * Settings.levelScalingMultiplier * level);
        final int spCost = Settings.storyPointsCost + (int) (Settings.storyPointsCost * Settings.levelScalingMultiplier * level);

        info.addPara("Retraining will change the officer's combat personality. Select a new personality to preview costs.", 0f);
        info.addSpacer(15f);

        final LabelAPI costLabelCredits = info.addPara("Credits: -", 0f);
        final LabelAPI costLabelSp = info.addPara("Story Points: -", 0f);
        
        info.addSpacer(20f);

        String[] personalities = {"timid", "cautious", "steady", "aggressive", "reckless"};
        String[] displayNames = {"Timid", "Cautious", "Steady", "Aggressive", "Reckless"};

        final boolean canAfford = Global.getSector().getPlayerFleet().getCargo().getCredits().get() >= creditsCost && 
                                  Global.getSector().getPlayerStats().getStoryPoints() >= spCost;

        float innerWidth = width - 20f;
        
        float btnWidth = 110f;
        float btnHeight = 30f;
        float padding = 5f;
        float totalWidth = (btnWidth * personalities.length) + (padding * (personalities.length - 1));
        float startX = (innerWidth - totalWidth) / 2f;

        CustomPanelAPI rowPanel = Global.getSettings().createCustom(innerWidth, 60f, null);
        
        final List<ButtonAPI> checkboxes = new ArrayList<>();
        final ButtonAPI[] confirmBtnRef = new ButtonAPI[1];

        for (int i = 0; i < personalities.length; i++) {
            final String pId = personalities[i];
            String pName = displayNames[i];

            boolean isCurrent = officer.getPersonalityAPI().getId().equals(pId);
            
            TooltipMakerAPI cell = rowPanel.createUIElement(btnWidth, 60f, false);
            final ButtonAPI btn = cell.addAreaCheckbox(pName, pId, Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), btnWidth, btnHeight, 0f);
            checkboxes.add(btn);
            
            if (isCurrent) {
                btn.setEnabled(false);
                cell.addPara("Current", Misc.getGrayColor(), 0f).setAlignment(com.fs.starfarer.api.ui.Alignment.MID);
            } else if (!canAfford) {
                btn.setEnabled(false);
            } else {
                attachListener(btn, new Runnable() {
                    @Override
                    public void run() {
                        for (ButtonAPI cb : checkboxes) {
                            if (cb != btn) cb.setChecked(false);
                        }
                        btn.setChecked(true);
                        selectedPersonality = pId;
                        
                        costLabelCredits.setText("Credits: " + (creditsCost > 0 ? Misc.getDGSCredits(creditsCost) : "Free"));
                        if (creditsCost > 0) costLabelCredits.setHighlightColors(Misc.getHighlightColor());
                        if (creditsCost > 0) costLabelCredits.setHighlight(Misc.getDGSCredits(creditsCost));

                        costLabelSp.setText("Story Points: " + (spCost > 0 ? String.valueOf(spCost) : "Free"));
                        if (spCost > 0) costLabelSp.setHighlightColors(Misc.getStoryOptionColor());
                        if (spCost > 0) costLabelSp.setHighlight(String.valueOf(spCost));

                        if (confirmBtnRef[0] != null) {
                            confirmBtnRef[0].setEnabled(true);
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
                if (selectedPersonality != null && canAfford) {
                    Global.getSector().getPlayerFleet().getCargo().getCredits().subtract(creditsCost);
                    if (spCost > 0) {
                        Global.getSector().getPlayerStats().spendStoryPoints(spCost, false, (com.fs.starfarer.api.campaign.TextPanelAPI) null, false, "Officer personality retrained");
                    }
                    officer.setPersonality(selectedPersonality);
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
