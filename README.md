# Starsector Officer Retraining Mod

This mod allows you to retrain your officers' combat personalities (e.g., changing an officer from Steady to Aggressive) directly from the Captain Picker dialog! A new "Retrain" button is seamlessly injected into the officer selection screen, allowing you to fine-tune your fleet's tactical behavior on the fly.

## Special Thanks & Credits
**A massive thank you to the creator of the [Officer Extension](https://fractalsoftworks.com/forum/index.php?topic=25658.0) mod.**
This mod was heavily inspired by the phenomenal work done in Officer Extension. Furthermore, the core UI injection mechanics, reflection-based class loading techniques, and UI tree polling optimizations used in this mod are heavily based on the code framework established by Officer Extension. Without their pioneering work in hacking Starsector's closed UI system, this mod would not have been possible. All credit for the underlying UI injection architecture goes to them!

## Configuration Options
You can configure the mod using [LunaLib](https://github.com/Lukas22041/LunaLib) (highly recommended, as changes apply instantly in-game) or by manually editing `data/config/officer_retraining_settings.json`.

The following options are available:

* **Base Credit Cost** (`creditsCost`) - *Default: 10000*
  The base amount of credits it costs to retrain an officer's personality.

* **Base Story Point Cost** (`storyPointsCost`) - *Default: 0*
  The base amount of story points it costs to retrain an officer's personality.

* **Level Scaling Multiplier** (`levelScalingMultiplier`) - *Default: 0.0*
  Increases the retraining cost based on the officer's current level. The cost formula adds `Base Cost * Multiplier * Officer Level` to the total cost.

* **Distance Scaling Multiplier** (`stepScalingMultiplier`) - *Default: 0.0*
  Increases the retraining cost based on how drastically you are changing the personality. Personalities fall on an axis: `Timid -> Cautious -> Steady -> Aggressive -> Reckless`. 
  For example, changing from Steady to Aggressive is 1 step. Changing from Cautious to Reckless is 3 steps. The formula adds `Base Cost * Multiplier * Number of Steps` to the total cost.

* **Permanent Unlocks** (`unlocksPersonalityPermanently`) - *Default: false*
  If enabled, paying to retrain an officer to a specific personality permanently "unlocks" that personality for that specific officer. You can freely switch back to any previously unlocked personality on that officer for free!

## Save Compatibility
This mod is **100% safe to add or remove from an existing save** at any time.
- **Adding**: The mod initializes dynamically on game load and does not require a fresh campaign.
- **Removing**: The only data saved to your campaign are harmless memory flags on individual officers to track their unlocked personalities. Uninstalling the mod will safely leave these flags dormant without causing any save corruption or crashes.

## Mod Compatibility
This mod is designed to be highly compatible. It has been tested and works perfectly alongside **Officer Extension** (thanks again for the framework!) and safely persists state into the save file without breaking vanilla behavior.
