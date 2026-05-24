---

# 🧩 PrimeTiers Overlay (TierTagger)

Minecraft Fabric mod that shows PvP tier rankings in-game.

---

# ⚙️ Requirements (Windows)

## 🟢 1. Install Java (VERY IMPORTANT)

You need **Java 17 or Java 21**

Download:
https://adoptium.net/temurin/releases/

During install:
✔ Check “Add to PATH”

---

## 🟡 2. Verify Java

Open **Command Prompt (CMD)** and run:

```bash
java -version

If installed correctly, you will see something like:

openjdk version "17" or "21"


---

📦 How to Compile Mod to JAR (Windows)

🟢 Step 1 — Open Project Folder

Go to your mod folder, example:

C:\Users\YourName\Desktop\TierTagger\


---

🟢 Step 2 — Open Terminal

Inside the folder:

✔ Shift + Right Click
✔ Click “Open PowerShell window here”

OR

✔ Open CMD manually in that folder


---

⚙️ Step 3 — Run Build Command

IMPORTANT COMMAND:

gradlew clean build


---

❗ If it doesn’t work, use:

.\gradlew clean build


---

⏳ Step 4 — Wait for Build

First build will:

Download Minecraft files

Download dependencies

Compile mod


It may take 1–5 minutes.


---

📁 Step 5 — Get the JAR File

After success, go here:

build/libs/

You will see:

tier-tagger-1.0.0.jar

👉 THIS is your mod file.


---

🎮 Step 6 — Install Mod

Copy jar to:

C:\Users\YourName\AppData\Roaming\.minecraft\mods\

Then launch Minecraft with Fabric.


---

❌ Common Problems

❌ 'java is not recognized'

✔ Java not installed or PATH not set


---

❌ gradlew not found

✔ You are not in the correct folder


---

❌ Build failed

Check:

Java version (17/21 required)

Internet connection

Correct folder opened



---

🚀 Quick Summary

Run this in project folder:

gradlew clean build

If error:

.\gradlew clean build

Then check:

build/libs/

Done.

---
