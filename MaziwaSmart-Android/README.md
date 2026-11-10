# MaziwaSmart – Android app (Kotlin + Firebase)

A dairy farm app for smallholder farmers: cow records, feeding records with **dry matter intake (DMI)**,
milk records, expenses, reports and an **AI section** (dummy data for now).

- **Language:** Kotlin
- **UI:** Jetpack Compose (Material 3)
- **Login:** Firebase Authentication (email + password)
- **Database:** Cloud Firestore
- **Min Android version:** Android 8.0 (API 26)

---

## 1. Open the project

1. Unzip the folder.
2. Open **Android Studio** → **File → Open** → choose the `MaziwaSmart-Android` folder.
3. Wait for Gradle to finish syncing (first time can take a few minutes, it downloads libraries).
   If Android Studio suggests upgrading the Gradle plugin, you can click **Upgrade** or ignore it.

> The app will **not build yet** – it needs the `google-services.json` file from Firebase (step 2).

## 2. Connect Firebase

### a) Create the Firebase project
1. Go to <https://console.firebase.google.com> → **Add project** → name it `MaziwaSmart` → Continue.
   (You can turn Google Analytics off.)

### b) Add the Android app
1. On the project page click the **Android** icon (Add app).
2. **Android package name:** `com.maziwasmart.app` ← must be exactly this.
3. App nickname: `MaziwaSmart` (optional). Click **Register app**.
4. Click **Download google-services.json**.
5. Put the file inside the **`app`** folder:
   ```
   MaziwaSmart-Android/
     app/
       google-services.json   <-- here
       build.gradle.kts
   ```
6. Skip the rest of the Firebase wizard (the SDK lines are already in the project).

### c) Turn on Email/Password login
**Build → Authentication → Get started → Sign-in method → Email/Password → Enable → Save.**

### d) Create the Firestore database
1. **Build → Firestore Database → Create database.**
2. Choose a location (e.g. `eur3` or the one closest to Kenya) → **Next**.
3. Choose **Start in production mode** → **Create**.
4. Open the **Rules** tab, delete what is there, paste the contents of `firestore.rules` from this project,
   then click **Publish**. This makes sure each farmer can only see their own data.

### e) Run it
Back in Android Studio: **File → Sync Project with Gradle Files**, then press the green **Run ▶** button
(use an emulator or your phone with USB debugging on).

Create an account on the Register screen – you should then see the new user under
**Authentication → Users** and a document under **Firestore → users**.

---

## 3. How the data is stored (Firestore)

```
users/{userId}                 farmer profile (name, phone, email, farmName, location, herdSize)
users/{userId}/cows/{id}       name, tagId, breed, dateOfBirth, lactationStatus, notes
users/{userId}/feedings/{id}   cowId, date, session, feedType, freshWeightKg,
                               dryMatterPercent, dmiKg, supplement, supplementGrams, timesPerDay
users/{userId}/milk/{id}       cowId, date, session (Morning/Evening), litres
users/{userId}/expenses/{id}   category, description, amount, date
users/{userId}/sales/{id}      litres, pricePerLitre, date
```
Dates are saved as text like `2026-11-06`.

## 4. Dry matter calculation

In **Feeding Records** the farmer enters the **fresh weight**; the app fills in the dry matter %
for the chosen feed and calculates:

```
DMI = fresh weight × dry matter %
e.g. 30 kg Napier × 0.20 = 6.0 kg DMI
```
The `dmiKg` value is saved with every feeding record – this is what the AI model will use.

Dry matter values are in `data/Feeds.kt` (Napier 20%, Hay 88%, Maize silage 30%, Dairy meal 90%).
**Change these to the values from your research source.** The farmer can also edit the % on the screen.

## 5. The AI part (dummy for now)

Bottom bar: **Home · Cows · AI · Advice · Reports**

The **AI** tab opens the AI menu:
- **Milk Yield Prediction** – pick a cow → predicted litres, confidence, actual vs predicted chart
- **Feeding Insights** – DMI vs milk yield chart, supplement comparison
- **Recommendations** – advice cards you can mark as reviewed (also the **Advice** tab)
- **Prediction Accuracy** – shown in Reports

All AI numbers come from `ai/DummyAi.kt` and every AI screen shows a **DEMO** banner.

**To connect the real model later:** create a class that implements `MilkYieldPredictor`
(for example one that sends the cow's DMI + milk history to your model/API and returns the prediction),
then change one line in `DummyAi.kt`:

```kotlin
val predictor: MilkYieldPredictor = DummyPredictor()   // change to your real class
```
The screens don't need to change.

## 6. Where things are in the code

```
app/src/main/java/com/maziwasmart/app/
  MainActivity.kt              starts the app
  data/
    Models.kt                  the data classes saved in Firestore
    Feeds.kt                   feed types, dry matter %, DMI formula
    Dates.kt                   date + number helpers
    AuthRepository.kt          login / register / logout (Firebase Auth)
    FarmRepository.kt          read/write cows, feedings, milk, expenses, sales (Firestore)
  ai/
    DummyAi.kt                 dummy prediction, insights, recommendations
  ui/
    FarmViewModel.kt           holds the farm data for the screens
    navigation/Navigation.kt   all routes + bottom bar
    components/Components.kt   shared header, cards, buttons, fields, charts
    auth/                      Login, Register
    dashboard/                 Dashboard (home)
    cows/                      My Cows, Add Cow, Cow Profile
    records/                   Feeding Records, Milk Records
    aiscreens/                 AI menu, Prediction, Insights, Recommendations
    finance/                   Expenses & Revenue
    reports/                   Reports
    settings/                  Profile & Settings
```

## 7. Common problems

| Problem | Fix |
|---|---|
| `File google-services.json is missing` | Download it from Firebase and put it in the `app` folder (step 2b). |
| `No matching client found for package name` | The package name in Firebase must be `com.maziwasmart.app`. |
| Register fails with "CONFIGURATION_NOT_FOUND" | Turn on Email/Password in Firebase Authentication (step 2c). |
| Data doesn't save / "PERMISSION_DENIED" | Create the Firestore database and publish `firestore.rules` (step 2d). |
| Emulator has no internet | Cold-boot the emulator or test on a real phone. |
