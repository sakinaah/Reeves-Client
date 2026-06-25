# Acknowledgements

Reeves Client is an original, MIT-licensed project. Some features are **inspired by**
the behaviour of well-known open-source Hypixel/SkyBlock community mods. Where a feature
was modelled on another project, it was implemented **clean-room** — from the observable
in-game behaviour and public documentation only, **without copying or adapting their
source code** — so that Reeves Client remains MIT-licensed and does not create a
derivative work of differently-licensed projects.

Functionality (what a feature does) is not copyrightable; specific source code is. We
implement the former and avoid the latter. We gratefully credit the projects that
pioneered or popularised these features:

| Project | License | Influence on Reeves Client |
|---|---|---|
| [JustEnoughItems (JEI)](https://github.com/mezz/JustEnoughItems) | MIT | Item search / recipe-viewer UX |
| [Skytils](https://github.com/Skytils/SkytilsMod) | AGPL-3.0 | Dungeon tracking & SkyBlock QoL concepts |
| [SkyHanni](https://github.com/hannibal002/SkyHanni) | LGPL | Garden/farming, minions, progression concepts |

## Important licensing notes

- **No code** from Skytils (AGPL-3.0) or SkyHanni (LGPL) is included in Reeves Client.
  Those are copyleft licenses incompatible with this project's MIT license; copying their
  code would require relicensing Reeves Client. We therefore reimplement only behaviour.
- JEI is MIT-licensed; any closer adaptation of JEI would retain its copyright notice and
  attribution as MIT requires.
- If you contribute code, do **not** paste source from copyleft-licensed mods. Describe the
  behaviour and implement it fresh.

Hypixel API data is used under the Hypixel Public API terms. All features are informational
and client-side; Reeves Client performs no automation or gameplay actions.
