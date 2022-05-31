# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]

### Notable changes from 1.16
- added ME Requester
  - a block that can be attached to ME networks
  - will automatically request new crafts if a specific item storage amount falls below a certain threshold
  - can track up to 6 different item storages
- added native KubeJS integration
- added AE2 Inscriber recipes for the Universal Press
  - you can also duplicate the press now
  - the word Universal is more fitting now
- added the ability to have multiple inputs per slot for recipes
- renamed the Pulse Centrifuge to Pulse Grinder
- renamed the Crystal Growth Chamber to Crystal Growth Core
  - we got questions in the past why the Chamber has no use, although it's just a crafting ingredient
  - might have been a misunderstanding from the old AE2Stuff mod
- removed the Crystal Energizer in favor of the Matter Infuser
  - the Crystal Energizer had no further use with AE2's new progression
  - the Matter Infuser is now used to craft ingredients required for the mod
- removed some items that were no longer required for crafting
- reworked recipe processing for recipes taking less than a tick
  - recipes that are very fast will multiply their output amounts now
  - if more than 64 items are produced per tick, the machine will cap the output to 64 (no items are lost)
- reworked auto extraction logic for large output amounts
- reworked menu syncing
  - should increase client performance
- reworked all recipes
  - acquiring machines is now less grindy but still requires progression in AE2
  - balancing of energy costs and processing times
  - adapted to the new progression of AE2
  - added some compat recipes for other mods to give the machines more use (more to come)
- reworked some textures and models
  - the Fluix Aggregator got a custom model now (more to come)

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/lazierae2-forge/compare/v1.18-3.0.0...HEAD
[3.0.0]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.0
