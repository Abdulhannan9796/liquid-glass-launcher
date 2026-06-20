# Liquid Glass Launcher — Get a Built APK (No Install Needed)

This project now builds itself in the cloud using GitHub Actions — you
don't need Android Studio, the Android SDK, or anything installed on
your laptop. You just need a free GitHub account.

---

## Step 1 — Create a GitHub account (skip if you have one)

Go to **https://github.com/signup** and make a free account.

## Step 2 — Create a new repository

1. Go to **https://github.com/new**
2. Name it `liquid-glass-launcher` (or anything you like)
3. Set it to **Public** or **Private**, doesn't matter
4. **Do not** check "Add a README" — leave it empty
5. Click **Create repository**

## Step 3 — Upload this entire project

1. Unzip `LiquidGlassLauncher-source.zip` on your laptop.
2. On your new repo's empty GitHub page, click **"uploading an existing
   file"** (the blue link in the middle of the page).
3. Open the unzipped `LiquidGlassLauncher` folder in File Explorer,
   select **everything inside it** (all files and folders — `app`,
   `.github`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`,
   `.gitignore`), and **drag the whole selection** into the GitHub upload
   box in your browser.
   - GitHub preserves folder structure when you drag a multi-file/folder
     selection like this, including the hidden `.github` folder.
   - If `.github` doesn't visibly show up in your file explorer (some
     systems hide dot-folders), enable "show hidden files" first, or
     just drag the visible files/folders and let me know — I can give
     you the workflow file's content to paste in manually instead.
4. Scroll down, click **"Commit changes"**.

## Step 4 — Watch it build

1. Click the **"Actions"** tab at the top of your repo.
2. You'll see a workflow run called **"Build APK"** already running
   (it starts automatically on upload). Click it.
3. Wait 2–5 minutes for the green checkmark.

## Step 5 — Download the APK

1. Still on that workflow run page, scroll to the bottom to
   **"Artifacts"**.
2. Click **`liquid-glass-launcher-debug-apk`** to download a zip
   containing `app-debug.apk`.
3. Unzip it — that `.apk` file is the real, installable app.

## Step 6 — Install it on your phone

1. Transfer `app-debug.apk` to your phone (USB cable, or upload it
   somewhere like Google Drive and download it on the phone).
2. Tap the file on your phone to install. Android will ask permission
   to "install unknown apps" the first time — allow it for whichever
   app you used to open the file.
3. Press the **Home button**. Android will ask which launcher to use —
   pick **Liquid Glass Launcher**. Choose "Always" to make it permanent,
   or "Just once" to try it first.
4. To switch back later: **Settings → Apps → Default apps → Home app**.

---

## What's included vs. not

**Included:** glass-style dock, search bar, app drawer, folders
(long-press an app → Add to Folder), a Today View page hosting real
Android widgets, and squircle (iOS-style) icon shaping on every app icon.
Your phone's navigation bar, buttons, and gestures are untouched — this
app never asks the system to change them.

**Not included:** true freeform icon placement across multiple
swipeable home pages (a much bigger drag-and-drop feature). Right now
there's one app-drawer page with search + folders rather than several
customizable home pages.

## If the build fails (red X instead of green check)

Click the failed run → click the red step → copy the error text and
send it to me. I wrote and balance-checked every file by hand since I
can't run a real compiler in my own environment, so a small typo making
it through is possible — tell me the exact error and I'll patch the
specific file.

## Prefer Android Studio instead?

This same project also opens directly in Android Studio if you install
it later (**File → Open** → select the unzipped folder). The first time
you open it, Android Studio may prompt about the Gradle wrapper being
missing — just accept its offer to generate one using its bundled
Gradle, then **Build → Build APK(s)** as normal.
