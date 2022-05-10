# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog],
and this project adheres to [Semantic Versioning].

## [Unreleased]
- /

## [2.0.4] - 2022-05-10

### Changed
- recipes do no longer accept duplicate inputs
  - this is a breaking change if your modpack has custom recipes with duplicate inputs
  - the change ensures easier automation with feeding mechanisms like hoppers and prevents slot overflow
- recipes now validate themselves before they are added to the game

### Fixed
- fix speculation core 1 recipe using an item stack instead of a tag
- fix rare shift clicking items to input slots dupe bug
- fix machines not dropping inputs and upgrades when broken in creative mode
- fix progress arrow overlapping when inserting upgrades while processing

## [2.0.3] - 2022-04-25

### Fixed
- insertion/extraction not correctly respecting the side configuration ([#7])
- recipe output amount not being correctly serialized ([#8])

<!-- Links -->
[#7]: https://github.com/AlmostReliable/lazierae2-forge/issues/7
[#8]: https://github.com/AlmostReliable/lazierae2-forge/issues/8

## [2.0.2] - 2022-04-21

### Changed
- if, through upgrades in the machine, a recipe requires less than a tick to process, the minimum required time is adjusted to 1 tick
  - this will be changed in the upcoming 1.18 version and calculate it exactly
  - this means all machines have a maximum output of 20 items per second

### Fixed
- fix a rare occasion of an arithmetic exception from a division by zero

## [2.0.1] - 2022-04-19

### Fixed
- auto extract being able to insert items to output and upgrade slots
- shift clicking logic being able to dupe items

## [2.0.0] - 2022-04-16
- rewrite

### Warning
This version is not compatible with any earlier version.<br>
All worlds where 1.x.x versions were used will have the block removed.

### Notable Changes
- a whole new code base
- machines with 3 input slots can now also process fewer inputs (so some input slots can be empty)
- the CraftTweaker integration has been improved and now features a recipe builder
- the GUI has been reworked and cleaned up
- data syncing has been improved
- performance has been improved drastically
- recipe caching system for the processing which improves the performance even further

<!-- Links -->
[keep a changelog]: https://keepachangelog.com/en/1.0.0/
[semantic versioning]: https://semver.org/spec/v2.0.0.html

<!-- Versions -->
[unreleased]: https://github.com/AlmostReliable/lazierae2-forge/compare/v1.16-2.0.4...HEAD
[2.0.4]: https://github.com/AlmostReliable/energymeter-forge/releases/tag/v1.16-2.0.3-beta..v1.16-2.0.4
[2.0.3]: https://github.com/AlmostReliable/energymeter-forge/releases/tag/v1.16-2.0.2-beta..v1.16-2.0.3
[2.0.2]: https://github.com/AlmostReliable/energymeter-forge/releases/tag/v1.16-2.0.1-beta..v1.16-2.0.2-beta
[2.0.1]: https://github.com/AlmostReliable/energymeter-forge/releases/tag/v1.16-2.0.0-beta..v1.16-2.0.1-beta
[2.0.0]: https://github.com/AlmostReliable/lazierae2-forge/releases/tag/v1.16-2.0.0-beta
