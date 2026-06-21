# Starsector Officer Retraining Mod

This mod allows you to retrain your officers' combat personalities (e.g., changing an officer from Steady to Aggressive) directly from the Captain Picker dialog.

## Credits
**This mod is heavily inspired by & derived from [Officer Extension](https://fractalsoftworks.com/forum/index.php?topic=25658.0). All credit for the UI injection, reflection-based class loading, and UI tree polling optimization code belongs to them.**

## Configuration Options
You can configure the mod using [LunaLib](https://fractalsoftworks.com/forum/index.php?topic=25658) or by manually editing `data/config/officer_retraining_settings.json`.

The following options are available:

* **Base Credit Cost** (`creditsCost`) - *Default: 10000*
  The base amount of credits it costs to retrain an officer's personality.

* **Base Story Point Cost** (`storyPointsCost`) - *Default: 1*
  The base amount of story points it costs to retrain an officer's personality.

* **Level Scaling Multiplier** (`levelScalingMultiplier`) - *Default: 0.0*
  Increases the retraining cost based on the officer's current level. The cost formula adds `Base Cost * Multiplier * Officer Level` to the total cost.

* **Distance Scaling Multiplier** (`stepScalingMultiplier`) - *Default: 0.0*
  Increases the retraining cost based on how drastically you are changing the personality. Personalities fall on an axis: `Timid -> Cautious -> Steady -> Aggressive -> Reckless`. 
  For example, changing from Steady to Aggressive is 1 step. Changing from Cautious to Reckless is 3 steps. The formula adds `Base Cost * Multiplier * Number of Steps` to the total cost.

* **Permanent Unlocks** (`unlocksPersonalityPermanently`) - *Default: true*
  If enabled, paying to retrain an officer to a specific personality permanently "unlocks" that personality for that specific officer. You can freely switch back to any previously unlocked personality on that officer for free!

## Compatibility
This mod is compatible with [Officer Extension](https://fractalsoftworks.com/forum/index.php?topic=25658.0). It is also safe to add or remove from an existing save.

## GenAI Disclosure
This mod was created with the help of generative AI for coding assistance.
