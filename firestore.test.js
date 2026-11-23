const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_farmer_01";
const BOB_UID = "bob_farmer_02";

const [emulatorHost, emulatorPortStr] = (
  process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085"
).split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write any user profile or subcollections", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  const now = new Date(Date.now() - 1000);
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("budgets").get()
  );
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      xp: 0,
      coins: 0,
      streakDays: 1,
      preferredRegion: "Nueva Ecija",
      completedChallenges: [],
      earnedBadges: [],
      studiedGuides: [],
      createdAt: now,
      updatedAt: now,
    })
  );
});

test("UserProfile: owner can create and partially update profile; cross-user and ghost fields fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Mang Juan",
      xp: 100,
      coins: 50,
      streakDays: 1,
      preferredRegion: "Nueva Ecija",
      completedChallenges: ["quiz_1"],
      earnedBadges: ["seedling"],
      studiedGuides: ["palay"],
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update succeeds
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      xp: 125,
      coins: 75,
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Reject immutable createdAt mutation & ghost field
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      createdAt: new Date(Date.now() - 500),
      updatedAt: new Date(Date.now() - 500),
    })
  );
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).update({
      isAdmin: true,
      updatedAt: new Date(Date.now() - 500),
    })
  );

  // Bob cannot read or write Alice's profile
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).update({
      xp: 9999,
      updatedAt: new Date(Date.now() - 500),
    })
  );
});

test("Budgets: owner can create, list, update, delete; rejects @DocumentId id field & cross-user", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("budgets")
      .doc("budget_1")
      .set({
        userId: ALICE_UID,
        cropName: "Palay (Rice)",
        farmAreaLabel: "1 Hectare",
        seedsCost: 3500,
        fertilizerCost: 8000,
        pestControlCost: 2500,
        laborCost: 12000,
        otherCost: 2000,
        expectedYieldKg: 4500,
        pricePerKg: 21,
        totalExpense: 28000,
        grossIncome: 94500,
        netProfit: 66500,
        createdAt: created,
        updatedAt: created,
      })
  );

  // List subcollection succeeds for owner
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("budgets").get()
  );

  // Reject extra `id` field
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("budgets")
      .doc("budget_2")
      .set({
        id: "budget_2",
        userId: ALICE_UID,
        cropName: "Kamatis",
        farmAreaLabel: "0.5 Ha",
        seedsCost: 1000,
        fertilizerCost: 2000,
        pestControlCost: 1000,
        laborCost: 3000,
        otherCost: 500,
        expectedYieldKg: 2000,
        pricePerKg: 35,
        totalExpense: 7500,
        grossIncome: 70000,
        netProfit: 62500,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Cross-user list/read/delete denied
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("budgets").get()
  );
  await assertFails(
    bobDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("budgets")
      .doc("budget_1")
      .delete()
  );
});

test("FarmPlots & PlantScans: owner operations succeed; invalid severity/bounds fail", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("farmPlots")
      .doc("plot_0")
      .set({
        userId: ALICE_UID,
        plotIndex: 0,
        cropId: "palay",
        cropName: "Palay (NSIC Rc222)",
        growthStage: 1,
        waterLevel: 80,
        soilHealth: 85,
        pestProtected: true,
        harvestCount: 0,
        createdAt: created,
        updatedAt: created,
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("farmPlots")
      .doc("plot_0")
      .update({
        growthStage: 2,
        waterLevel: 95,
        updatedAt: new Date(Date.now() - 1000),
      })
  );

  await assertSucceeds(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("plantScans")
      .doc("scan_1")
      .set({
        userId: ALICE_UID,
        cropName: "Kamatis",
        conditionName: "Early Blight",
        severity: "Moderate",
        summary: "Dark concentric spots on lower leaves.",
        organicSolutions: ["Remove affected leaves", "Apply neem oil spray"],
        preventionTips: ["Avoid overhead watering"],
        demoMode: false,
        createdAt: created,
        updatedAt: created,
      })
  );

  // Invalid severity rejected
  await assertFails(
    aliceDb
      .collection("users")
      .doc(ALICE_UID)
      .collection("plantScans")
      .doc("scan_2")
      .set({
        userId: ALICE_UID,
        cropName: "Kamatis",
        conditionName: "Unknown",
        severity: "CRITICAL_INVALID",
        summary: "Invalid enum",
        organicSolutions: [],
        preventionTips: [],
        demoMode: false,
        createdAt: created,
        updatedAt: created,
      })
  );
});
