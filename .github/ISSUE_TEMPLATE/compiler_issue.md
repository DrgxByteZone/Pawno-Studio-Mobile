---
name: ⚙️ Compiler or Gamemode Build Issue
about: Report a compilation error, hang, or include resolution issue with a specific gamemode
title: "[COMPILER] "
labels: ["compiler", "triage"]
assignees: ""
---

**Gamemode / Script Overview**
- Gamemode Name / Framework: [e.g. Atlantic, YSI, open.mp, Scavenge and Survive]
- Approximate Lines of Code: [e.g. 15,000 / 100,000+]
- Selected Compiler: [Auto / Pawn 3.2 Legacy / Pawn 3.10.7 / Pawn 3.10.11]

**Problem Description**
Does it fail with an error code (e.g. Error 100, Error 17), crash, or hang?

**Exact Error Output (Raw Log)**
```
Paste the error log from the Diagnostics Screen here.
```

**Include Folders & Structure**
Explain how your include files are structured:
- [ ] Root `pawno/include`
- [ ] Subfolders (e.g. `YSI_Data/`, `YSI_Internal/`)
- [ ] Case sensitivity differences (e.g. `A_SAMP.INC` vs `a_samp.inc`)

**Device & Storage Details**
- Device: [e.g. Redmi Note 12]
- Android OS: [e.g. Android 14]
- Storage Path: [e.g. Internal Storage `/storage/emulated/0/Download/...`]
