# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]
- /

## [3.0.1] - 2022-06-04

### Fixed
- mod entry point recipe for the Infuser
  - now uses a Resonating Crystal instead of a processor


## [3.0.0] - 2022-06-01

This initial version marks the beginning of the **1.18** port!<br>
The following changes are notable changes from the 1.16 version.

### Added
- added the **ME Requester**
  - a new block that can be attached to any ME network
  - will automatically request new crafts if a specific item storage amount falls below a certain threshold
  - can track up to 6 different item storages
- added the ability to have **multiple inputs per slot** for recipes
- added **native KubeJS integration**
  - you can now easily add new recipes to all Lazier AE2 machines
  - a guide can be found on our [wiki]
- added AE2 Inscriber recipes for the Universal Press
  - you can also duplicate the press now
  - the word Universal is more fitting now

### Changed
- renamed the Pulse Centrifuge to **Pulse Grinder**
  - the name is more fitting considering its purpose
- renamed the Crystal Growth Chamber to **Crystal Growth Core**
  - we got questions in the past why the Chamber has no use, although it's just a crafting ingredient
  - might have been a misunderstanding from the old AE2Stuff mod
- reworked recipe processing for recipes taking less than a tick
  - recipes that take less than a tick will properly multiply their output
  - if more than 64 items are produced per tick, the machine will slow down
- reworked auto extraction logic for large output amounts
- reworked menu syncing
  - increases client performance
  - decreases server network traffic
- **reworked and rebalanced** all recipes
  - acquiring machines is now less grindy but still requires progression in AE2
  - balancing of energy costs and processing times
  - adapted to the new progression of AE2
  - added some compat recipes for other mods to give the machines more use (more to come)
- reworked some **textures and models**
  - the Fluix Aggregator got a custom model now (more to come)

### Removed
- removed the Crystal Energizer in favor of the new **Matter Infuser**
  - the Crystal Energizer had no further use with AE2's new progression
  - the Matter Infuser is now used to craft ingredients required for the mod
- removed some items that were no longer required for crafting

<!-- Links -->
[wiki]: https://github.com/AlmostReliable/lazierae2-forge/wiki
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/lazierae2-forge/compare/v1.18-3.0.1-beta...HEAD
[3.0.1]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.0-beta..v1.18-3.0.1-beta
[3.0.0]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.0-beta
