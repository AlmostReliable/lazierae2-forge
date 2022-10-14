# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [3.1.4] - 2022-10-14

### Removed
- compat recipes for MEGACells, they are now part of the main mod

## [3.1.3] - 2022-08-09

### Added
- Mekanism compat recipes
  - crushing Resonating Crystal to Resonating Dust
  - enriching Resonating Seed to Resonating Crystal

### Changed
- JEI recipe pages are now viewable by clicking the progress arrows
  - this bumps the minimum JEI version to 9.7.2.259
  - when using JEI v10, the minimum version is 10.1.4.258

### Fixed
- crash when an auto extract target is removed

## [3.1.2] - 2022-08-09

### Added
- JEI 10 compatibility ([#17])

<!-- Links -->
[#17]: https://github.com/AlmostReliable/lazierae2-forge/issues/17

## [3.1.1] - 2022-07-31

### Added
- error messages for invalid upgrade and multiplier config entries
- Chinese translation ([#23])

### Fixed
- grinder and infuser descriptions in German translation

<!-- Links -->
[#23]: https://github.com/AlmostReliable/lazierae2-forge/pull/23

## [3.1.0] - 2022-07-21

### Added
- new models and textures for all machines
- JEI info panels for the Universal Press and the Singularity
  - brief explanation of how to obtain them
  - can be disabled in the config
- compat recipe for MEGACells ([#21])
- ability to shift-right-click upgrades into processing machines
- German translation

### Changed
- improved texture of the Universal Press
- improved texture of the Resonating Seed
- improved texture of Requester checkboxes
- more clunky descriptions in tooltips are now only visible when holding shift
- reworked processing machine upgrade system
  - when upgraded to half of the maximum upgrade count, machines will be twice as fast and consume twice as much energy
  - each upgrade above will be exponentially faster but has worse energy efficiency
- Requester now requires a channel
  - can be modified in the config
  - default setting is true
- Requester no longer exposes front side for cable connections

### Fixed
- main node creation at wrong time
  - fixes connections when a Security Terminal is set up in the network ([#18])
  - an AE2 API update was required, thus raising the AE2 version to 11.1.4
- block tooltip hiding when no description is provided

<!-- Links -->
[#18]: https://github.com/AlmostReliable/lazierae2-forge/issues/18
[#21]: https://github.com/AlmostReliable/lazierae2-forge/issues/21

## [3.0.2] - 2022-06-05

### Fixed
- ME Requester not exporting crafting results from all slots properly

## [3.0.1] - 2022-06-04

### Fixed
- mod entry point recipe for the Infuser
  - now uses a Resonating Crystal instead of a processor

## [3.0.0] - 2022-06-01

This initial version marks the beginning of the **1.18** port!<br>
The following changes are notable changes from the 1.16 version.

### Added
- the **ME Requester**
  - a new block that can be attached to any ME network
  - will automatically request new crafts if a specific item storage amount falls below a certain threshold
  - can track up to 6 different item storages
- the ability to have **multiple inputs per slot** for recipes
- **native KubeJS integration**
  - you can now easily add new recipes to all Lazier AE2 machines
  - a guide can be found on our [wiki]
- AE2 Inscriber recipes for the Universal Press
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
- the Crystal Energizer in favor of the new **Matter Infuser**
  - the Crystal Energizer had no further use with AE2's new progression
  - the Matter Infuser is now used to craft ingredients required for the mod
- some items that were no longer required for crafting

<!-- Links -->
[wiki]: https://github.com/AlmostReliable/lazierae2-forge/wiki
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/lazierae2-forge/compare/v1.18-3.1.4...HEAD
[3.1.4]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.1.2..v1.18-3.1.4
[3.1.3]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.1.2..v1.18-3.1.3
[3.1.2]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.1.1..v1.18-3.1.2
[3.1.1]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.1.0..v1.18-3.1.1
[3.1.0]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.2-beta..v1.18-3.1.0
[3.0.2]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.1-beta..v1.18-3.0.2-beta
[3.0.1]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.0-beta..v1.18-3.0.1-beta
[3.0.0]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.18-3.0.0-beta
