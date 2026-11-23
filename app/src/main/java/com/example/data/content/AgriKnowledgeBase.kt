package com.example.data.content

import com.example.data.model.AgriChallenge
import com.example.data.model.AgriQuizQuestion
import com.example.data.model.BadgeInfo
import com.example.data.model.BudgetPreset
import com.example.data.model.ChallengeChoice
import com.example.data.model.CropGuideTopic
import com.example.data.model.DailyTip
import com.example.data.model.PhilippineRegion
import com.example.data.model.PlantDiagnosisResult
import com.example.data.model.SamplePlantCase
import com.example.data.model.VirtualCropOption

object AgriKnowledgeBase {

    val philippineRegions = listOf(
        PhilippineRegion("Nueva Ecija", "Central Luzon (Rice Granary)", 15.4865, 120.9665, "Palay, Sibuyas, Kamatis"),
        PhilippineRegion("La Trinidad", "Benguet (Cordillera)", 16.4567, 120.5877, "Repolyo, Patatas, Strawberry"),
        PhilippineRegion("Los Baños", "Laguna (CALABARZON)", 14.1667, 121.2433, "Palay, Buko, Kamatis"),
        PhilippineRegion("Iloilo City", "Western Visayas", 10.7202, 122.5621, "Palay, Mais, Tubo"),
        PhilippineRegion("Tacloban", "Eastern Visayas", 11.2543, 125.0000, "Buko, Palay, Kamote"),
        PhilippineRegion("Cagayan de Oro", "Northern Mindanao", 8.4542, 124.6319, "Mais, Saging, Pinakbet Crops"),
        PhilippineRegion("Davao City", "Davao Region", 7.1907, 125.4553, "Saging, Cacao, Durian, Buko"),
        PhilippineRegion("General Santos", "SOCCSKSARGEN", 6.1164, 125.1716, "Mais, Mangga, Gulay")
    )

    val dailyTips = listOf(
        DailyTip(
            id = "tip_1",
            category = "Water & Irrigation",
            title = "Alternate Wetting & Drying (AWD) for Palay",
            tip = "Let paddy water drop up to 15 cm below the soil surface before re-irrigating. This cuts water use by up to 30% without lowering harvest!",
            funFact = "Did you know? IRRI in Los Baños, Laguna helped pioneer AWD, helping Filipino rice farmers save fuel and pump costs every dry season!"
        ),
        DailyTip(
            id = "tip_2",
            category = "Soil Health",
            title = "Compost Your Rice Straw Instead of Burning",
            tip = "Burning rice straw destroys nitrogen and organic carbon. Shredding and composting it with Trichoderma returns potassium and softens heavy clay soil.",
            funFact = "1 ton of composted rice straw returns about 5–7 kg of nitrogen and 15 kg of potassium back to your field!"
        ),
        DailyTip(
            id = "tip_3",
            category = "Pest Control",
            title = "Plant Marigolds Around Vegetable Plots",
            tip = "Planting yellow marigolds (amarillo) along kamatis and talong borders repels root-knot nematodes and attracts helpful ladybugs.",
            funFact = "A single ladybug can eat up to 50 aphids a day — free 24/7 organic pest patrol for your gulayan!"
        ),
        DailyTip(
            id = "tip_4",
            category = "Cost Saving",
            title = "Test Soil Before Buying Full Fertilizer Sacks",
            tip = "Use a simple Soil Test Kit (STK) or Minus-One Element Technique (MOET) before basal application so you only buy nutrients your field actually lacks.",
            funFact = "Farmers who match fertilizer to soil tests often cut fertilizer expenses by 20–30% while keeping yields high!"
        ),
        DailyTip(
            id = "tip_5",
            category = "Climate Resilience",
            title = "Mulch Your Soil to Beat Dry-Season Heat",
            tip = "Cover bare soil around kamatis, sili, and talong with dried grass or rice straw mulch (5 cm thick) to cool roots and keep moisture longer.",
            funFact = "Mulching can lower topsoil temperature by up to 6°C during hot El Niño afternoons!"
        ),
        DailyTip(
            id = "tip_6",
            category = "Crop Rotation",
            title = "Rotate Palay with Munggo (Mungbean)",
            tip = "Planting munggo after rice harvest naturally fixes nitrogen from the air into the soil and gives you a bonus income crop in just 60 days!",
            funFact = "Munggo roots host friendly Rhizobium bacteria that act like tiny natural urea factories underground."
        )
    )

    val cropGuides = listOf(
        CropGuideTopic(
            id = "palay",
            title = "Palay (Rice)",
            category = "Staple Crop",
            subtitle = "Smart water-saving & high-yield lowland rice care",
            idealSeason = "Wet (Jun–Nov) & Dry (Dec–May)",
            soilPreference = "Clay loam, pH 5.5–6.5, well-puddled",
            waterNeeds = "Moderate (use Alternate Wetting & Drying)",
            plantingSteps = listOf(
                "Choose certified inbred or hybrid seeds suited to your province (e.g., NSIC Rc222 or Rc160).",
                "Soak seeds 24 hours, incubate 24–36 hours, and transplant 18–21 day old seedlings at 20×20 cm spacing.",
                "Level your paddy well so water depth stays uniform and weeds struggle to grow."
            ),
            careAndSoilTips = listOf(
                "Use the Leaf Color Chart (LCC) every 7 days before adding urea so you never over-apply nitrogen.",
                "Incorporate decomposed rice straw 2–3 weeks before transplanting to boost organic matter.",
                "Drain field 10–14 days before harvest for uniform golden ripening."
            ),
            pestWatch = "Watch for Rice Black Bug, Stem Borer, and Rice Blast during humid nights. Keep bunds clean and encourage dragonflies & spiders.",
            budgetTip = "Using drum seeder or AWD irrigation cuts labor and diesel pumping expenses significantly."
        ),
        CropGuideTopic(
            id = "kamatis",
            title = "Kamatis (Tomato)",
            category = "High-Value Vegetable",
            subtitle = "Heat-tolerant & blight-resistant gulayan strategies",
            idealSeason = "Cool Dry (Oct–Mar) or raised beds in Wet season",
            soilPreference = "Sandy loam to clay loam, well-drained, pH 6.0–6.8",
            waterNeeds = "Consistent moisture at roots; avoid wetting leaves",
            plantingSteps = listOf(
                "Start seeds in seedling trays with coco coir + vermicompost for strong, disease-free roots.",
                "Transplant 21–25 day old seedlings onto raised beds (30 cm high during rainy months) spaced 50×75 cm.",
                "Install bamboo stakes early so fruits stay clean off wet soil."
            ),
            careAndSoilTips = listOf(
                "Apply rice straw or plastic mulch to prevent blossom-end rot and reduce weeding labor.",
                "Water early morning at the base only — wet leaves at dusk invite Early Blight fungus.",
                "Side-dress with organic compost or calcium-rich crushed eggshells during flowering."
            ),
            pestWatch = "Check undersides of leaves for whiteflies, fruitworms, and leaf miners. Use yellow sticky traps and neem spray early.",
            budgetTip = "Making your own seedling mix (garden soil + compost + carbonized rice hull) saves up to 40% on nursery costs."
        ),
        CropGuideTopic(
            id = "talong",
            title = "Talong (Eggplant)",
            category = "High-Value Vegetable",
            subtitle = "Long-harvesting favorite with smart borer control",
            idealSeason = "Year-round with good drainage",
            soilPreference = "Deep fertile loam, pH 5.5–6.8",
            waterNeeds = "Regular watering, sensitive to waterlogging",
            plantingSteps = listOf(
                "Sow in trays and transplant after 4 weeks when plants have 4–5 true leaves.",
                "Space 75 cm between plants and 100 cm between rows for good airflow.",
                "Mix 1–2 handfuls of vermicompost per planting hole before transplanting."
            ),
            careAndSoilTips = listOf(
                "Prune old yellowing bottom leaves regularly to let breeze dry morning dew quickly.",
                "Harvest fruits every 3–4 days once fruiting starts to trigger continuous new flowers.",
                "Keep soil moist with mulch so fruits grow tender and glossy."
            ),
            pestWatch = "Eggplant Fruit and Shoot Borer (EFSB) is #1 enemy: immediately clip and bury/bag wilted shoots and damaged fruits so larvae don't multiply.",
            budgetTip = "Sanitation pruning of damaged shoots reduces pesticide spraying by more than half!"
        ),
        CropGuideTopic(
            id = "mais",
            title = "Mais (Corn)",
            category = "Staple Crop",
            subtitle = "Efficient nitrogen use & Fall Armyworm defense",
            idealSeason = "Wet (May–Aug) & Dry (Nov–Feb)",
            soilPreference = "Deep well-drained loam, pH 5.8–7.0",
            waterNeeds = "Moderate; critical during tasseling and silking",
            plantingSteps = listOf(
                "Plant 1–2 seeds per hill at 70 cm between rows and 25 cm between hills.",
                "Apply basal fertilizer or organic chicken manure mixed into soil at planting.",
                "Hill-up soil around stalks at 25–30 days after planting to anchor roots against wind."
            ),
            careAndSoilTips = listOf(
                "Ensure uninterrupted moisture during tasseling and ear-filling — dry stress at this stage slashes grain weight.",
                "Intercrop with peanut or munggo between corn rows to suppress weeds and add nitrogen."
            ),
            pestWatch = "Scout weekly for Fall Armyworm inside the whorl. Early spot-treatment with botanical extracts or biological Trichogramma wasps prevents spread.",
            budgetTip = "Intercropping corn with legumes spreads income risk and lowers nitrogen fertilizer bills."
        ),
        CropGuideTopic(
            id = "soil_compost",
            title = "Soil Health & Organic Composting",
            category = "Soil Management",
            subtitle = "Fixing acidic, tired, or compacted farm soils",
            idealSeason = "Year-round soil improvement",
            soilPreference = "Target pH 6.0–6.8 with >3% organic matter",
            waterNeeds = "Keep compost pile damp like a wrung-out sponge",
            plantingSteps = listOf(
                "Layer 'browns' (dried leaves, rice straw, sawdust) and 'greens' (kitchen scraps, kakawate/ipil-ipil leaves, manure) in a 3:1 ratio.",
                "Add a thin layer of garden soil or EM1/Trichoderma activator every 30 cm.",
                "Turn pile every 7–10 days; rich dark compost is ready in 4–6 weeks."
            ),
            careAndSoilTips = listOf(
                "If soil is too acidic (common in highland/rainy areas), mix agricultural lime 2–4 weeks before planting.",
                "Never leave plowed soil bare under heavy sun or rain — plant cover crops or apply mulch.",
                "Use Carbonized Rice Hull (CRH / ipa) to loosen sticky clay and hold water in sandy soil."
            ),
            pestWatch = "Healthy living soil feeds stronger plant immune systems and hosts beneficial fungi that crowd out root rot.",
            budgetTip = "Farm-made vermicompost and Fermented Plant Juice (FPJ) replace costly imported soil conditioners."
        ),
        CropGuideTopic(
            id = "ipm_natural",
            title = "Natural Pest Defense (IPM)",
            category = "Pest Control",
            subtitle = "Integrated Pest Management without wasting money",
            idealSeason = "Every cropping cycle",
            soilPreference = "Diverse farm border with flowering herbs",
            waterNeeds = "Spray botanical solutions late afternoon",
            plantingSteps = listOf(
                "Inspect 10 random plants in an 'X' pattern across your field twice a week.",
                "Identify whether insects are pests or friendly predators (spiders, ladybugs, wasps) before spraying.",
                "Set up yellow sticky traps (for whiteflies/aphids) and pheromone lures."
            ),
            careAndSoilTips = listOf(
                "DIY Chili-Garlic-Soap Spray: blend 10 siling labuyo + 1 garlic head + 1L water, strain, add 1 tsp mild dish soap, dilute 1:10 before spraying.",
                "Neem oil spray disrupts chewing & sucking pests without harming bees when applied at dusk."
            ),
            pestWatch = "Never spray broad-spectrum chemicals when pests are below action threshold — killing good bugs causes worse secondary outbreaks!",
            budgetTip = "Weekly scouting prevents emergency full-field chemical purchases."
        )
    )

    val agriChallenges = listOf(
        AgriChallenge(
            id = "challenge_typhoon",
            title = "Typhoon Signal #2 Approaching",
            regionContext = "Nueva Ecija • Palay near milking stage",
            scenario = "PAGASA warns of heavy rains and 75 km/h winds in 36 hours. Your rice paddies are already full of water and bunds have small crab holes.",
            choices = listOf(
                ChallengeChoice(
                    id = "c1",
                    label = "Clear drainage canals, patch bund holes, and lower paddy water slightly",
                    outcome = "Smart move! Lowering water prevents overflow flooding and keeps rice stalks from lodging completely.",
                    xpReward = 40,
                    coinDelta = 60,
                    isRecommended = true
                ),
                ChallengeChoice(
                    id = "c2",
                    label = "Apply extra urea fertilizer right before the rain for strength",
                    outcome = "Ouch! Heavy rain washes expensive urea straight out of the field and weakens stalks, increasing lodging.",
                    xpReward = 15,
                    coinDelta = -20,
                    isRecommended = false
                ),
                ChallengeChoice(
                    id = "c3",
                    label = "Do nothing and wait for the storm to pass",
                    outcome = "Crab holes widened during the downpour, breaching your bund and washing away part of your crop.",
                    xpReward = 10,
                    coinDelta = -30,
                    isRecommended = false
                )
            )
        ),
        AgriChallenge(
            id = "challenge_fertilizer",
            title = "High Fertilizer Prices & Yellow Leaves",
            regionContext = "Laguna • 30-day-old Palay plot",
            scenario = "Fertilizer prices jumped 25% this month. Some rice leaves look slightly pale green. How do you manage costs wisely?",
            choices = listOf(
                ChallengeChoice(
                    id = "c1",
                    label = "Check Leaf Color Chart (LCC) first & combine reduced urea with compost/CRH",
                    outcome = "Great financial & soil choice! You saved ₱2,400/ha on unnecessary urea while improving long-term soil fertility.",
                    xpReward = 45,
                    coinDelta = 75,
                    isRecommended = true
                ),
                ChallengeChoice(
                    id = "c2",
                    label = "Borrow money to double the usual urea dose just to be safe",
                    outcome = "Too much nitrogen attracted brown planthoppers and increased your debt burden!",
                    xpReward = 10,
                    coinDelta = -35,
                    isRecommended = false
                ),
                ChallengeChoice(
                    id = "c3",
                    label = "Skip all nutrients for the rest of the season",
                    outcome = "Yield dropped noticeably at harvest because nitrogen deficiency wasn't corrected gently.",
                    xpReward = 15,
                    coinDelta = -10,
                    isRecommended = false
                )
            )
        ),
        AgriChallenge(
            id = "challenge_aphids",
            title = "Aphid & Whitefly Alert on Kamatis",
            regionContext = "Benguet / Batangas • Flowering Tomatoes",
            scenario = "You notice sticky honeydew and curling tips on 5 tomato plants at the edge of your plot. What is your first move?",
            choices = listOf(
                ChallengeChoice(
                    id = "c1",
                    label = "Prune heavily curled leaves, set yellow sticky traps, and spray neem/garlic mix at dusk",
                    outcome = "Bullseye! Early spot treatment stopped the colony while keeping bees and ladybugs safe for pollination!",
                    xpReward = 40,
                    coinDelta = 65,
                    isRecommended = true
                ),
                ChallengeChoice(
                    id = "c2",
                    label = "Spray strong broad-spectrum insecticide at noon under hot sun",
                    outcome = "Midday spraying scorched tomato blossoms and killed pollinating bees — reducing fruit set!",
                    xpReward = 10,
                    coinDelta = -25,
                    isRecommended = false
                )
            )
        ),
        AgriChallenge(
            id = "challenge_soil_acid",
            title = "Stunted Roots in Acidic Rainy Soil",
            regionContext = "Bukidnon • Vegetable Plot (pH 4.9)",
            scenario = "Your vegetables grow slowly even after adding fertilizer. A quick soil test shows acidic pH 4.9.",
            choices = listOf(
                ChallengeChoice(
                    id = "c1",
                    label = "Apply agricultural lime + rich vermicompost 3 weeks before next planting",
                    outcome = "Excellent! Raising pH toward 6.2 unlocks trapped soil phosphorus so plants finally absorb nutrients!",
                    xpReward = 45,
                    coinDelta = 70,
                    isRecommended = true
                ),
                ChallengeChoice(
                    id = "c2",
                    label = "Keep pouring more chemical NPK every week",
                    outcome = "In very acidic soil, chemical phosphorus locks up fast — wasting money without fixing root stress.",
                    xpReward = 10,
                    coinDelta = -30,
                    isRecommended = false
                )
            )
        )
    )

    val quizQuestions = listOf(
        AgriQuizQuestion(
            id = "q1",
            topic = "Water Management",
            question = "How does Alternate Wetting and Drying (AWD) help Filipino rice farmers?",
            options = listOf(
                "It saves up to 30% irrigation water without reducing harvest",
                "It requires flooding the field 20 cm deep every day",
                "It replaces the need for sunlight"
            ),
            correctIndex = 0,
            explanation = "AWD lets water safely drop up to 15 cm below the soil surface before re-irrigating, cutting pump fuel costs while strengthening rice roots.",
            xpReward = 25
        ),
        AgriQuizQuestion(
            id = "q2",
            topic = "Soil & Rotation",
            question = "Why is planting munggo (mungbean) after rice harvest a smart farming practice?",
            options = listOf(
                "It removes all water from the province",
                "It naturally adds nitrogen to soil and gives an extra crop",
                "It attracts stem borers away from neighbors"
            ),
            correctIndex = 1,
            explanation = "Legumes like munggo partner with Rhizobium bacteria to fix atmospheric nitrogen into the soil while maturing in ~60 days.",
            xpReward = 25
        ),
        AgriQuizQuestion(
            id = "q3",
            topic = "Plant Disease",
            question = "What is the best time of day to water tomatoes to prevent fungal leaf diseases like Early Blight?",
            options = listOf(
                "Late at night over all the leaves",
                "Early morning directly at the base/roots",
                "During high noon over the flowers"
            ),
            correctIndex = 1,
            explanation = "Morning base watering keeps leaves dry and lets any splashes evaporate quickly in morning sun, discouraging fungal spores.",
            xpReward = 25
        ),
        AgriQuizQuestion(
            id = "q4",
            topic = "Pest Defense",
            question = "Which bright flower planted around vegetable beds helps repel root nematodes and attract ladybugs?",
            options = listOf(
                "Marigold (Amarillo)",
                "Cogon grass",
                "Water hyacinth"
            ),
            correctIndex = 0,
            explanation = "Marigold roots release natural compounds that suppress nematodes while its blooms feed beneficial predatory insects.",
            xpReward = 25
        ),
        AgriQuizQuestion(
            id = "q5",
            topic = "Farm Economics",
            question = "What does 'Break-Even Price per kg' tell a farmer?",
            options = listOf(
                "The maximum price allowed in the market",
                "The minimum selling price per kg needed to cover all expenses",
                "The weight of one sack of fertilizer"
            ),
            correctIndex = 1,
            explanation = "Break-even price = Total Expenses ÷ Total Yield (kg). Selling above this number means your farm earns a profit!",
            xpReward = 25
        )
    )

    val badges = listOf(
        BadgeInfo("seedling", "Sprouting Farmer", "Joined AgriQuest AI & started your smart farming journey.", "Unlocked at start"),
        BadgeInfo("plant_doctor", "Plant Protector", "Diagnosed a crop issue with AI Plant Doctor.", "Run 1 AI Plant Doctor scan"),
        BadgeInfo("wise_budgeter", "Wise Budgeter (₱)", "Calculated and saved a farm budget in Philippine Pesos.", "Save 1 Farm Budget"),
        BadgeInfo("soil_scientist", "Soil & Crop Scholar", "Studied 2 or more Smart Farming Guide topics.", "Mark 2 Guide topics studied"),
        BadgeInfo("quest_champion", "AgriQuest Strategist", "Completed 2 farming challenges or quizzes.", "Complete 2 Challenges/Quizzes"),
        BadgeInfo("harvest_master", "Virtual Harvester", "Grew and harvested your first crop in the Virtual Farm.", "Harvest 1 Virtual Farm plot")
    )

    val virtualCrops = listOf(
        VirtualCropOption("palay", "Palay (Rice)", 30, 85, 35, "Thrives with steady water & balanced nitrogen."),
        VirtualCropOption("kamatis", "Kamatis (Tomato)", 35, 100, 40, "Loves sunlit soil, mulch, and pest protection."),
        VirtualCropOption("talong", "Talong (Eggplant)", 25, 80, 30, "Needs well-drained soil and clean pruned leaves."),
        VirtualCropOption("saging", "Saging (Banana)", 45, 130, 50, "Needs rich compost and moisture retention."),
        VirtualCropOption("sibuyas", "Sibuyas (Onion)", 40, 115, 45, "Prefers cool dry weather and loose soil.")
    )

    val budgetPresets = listOf(
        BudgetPreset(
            label = "Palay (1 Hectare)",
            cropName = "Palay (Rice)",
            farmAreaLabel = "1.0 Hectare",
            seedsCost = 3600.0,
            fertilizerCost = 9500.0,
            pestControlCost = 2800.0,
            laborCost = 14500.0,
            otherCost = 3200.0,
            expectedYieldKg = 4800.0,
            pricePerKg = 21.0
        ),
        BudgetPreset(
            label = "Talong (0.5 Hectare)",
            cropName = "Talong (Eggplant)",
            farmAreaLabel = "0.5 Hectare",
            seedsCost = 2200.0,
            fertilizerCost = 6500.0,
            pestControlCost = 3500.0,
            laborCost = 11000.0,
            otherCost = 2500.0,
            expectedYieldKg = 3200.0,
            pricePerKg = 32.0
        ),
        BudgetPreset(
            label = "Kamatis (0.25 Hectare)",
            cropName = "Kamatis (Tomato)",
            farmAreaLabel = "0.25 Hectare",
            seedsCost = 1800.0,
            fertilizerCost = 4800.0,
            pestControlCost = 2400.0,
            laborCost = 7500.0,
            otherCost = 2000.0,
            expectedYieldKg = 2600.0,
            pricePerKg = 38.0
        ),
        BudgetPreset(
            label = "Mais (1 Hectare)",
            cropName = "Mais (Yellow Corn)",
            farmAreaLabel = "1.0 Hectare",
            seedsCost = 4500.0,
            fertilizerCost = 8800.0,
            pestControlCost = 2200.0,
            laborCost = 9500.0,
            otherCost = 2500.0,
            expectedYieldKg = 4200.0,
            pricePerKg = 17.5
        )
    )

    val samplePlantCases = listOf(
        SamplePlantCase(
            id = "sample_rice_blast",
            label = "Palay Leaf Spots",
            cropName = "Palay (Rice)",
            symptomPreview = "Diamond-shaped gray-green spots with brown borders on rice leaves",
            demoDiagnosis = PlantDiagnosisResult(
                cropName = "Palay (Rice)",
                conditionName = "Rice Blast (Pyricularia oryzae)",
                severity = "Moderate",
                summary = "Diamond or spindle-shaped lesions with grayish centers and dark brown margins are classic early signs of Rice Blast, especially during cool humid mornings.",
                organicSolutions = listOf(
                    "Avoid excessive nitrogen/urea topdressing while spots are active.",
                    "Maintain shallow uniform water levels rather than letting paddy soil dry completely during blast outbreaks.",
                    "Spray beneficial Trichoderma or compost tea early morning to outcompete leaf fungi."
                ),
                preventionTips = listOf(
                    "Use blast-resistant NSIC rice varieties on your next cropping.",
                    "Keep 20×20 cm plant spacing for airflow and avoid heavy shade.",
                    "Remove infected rice straw or compost it thoroughly instead of leaving it near seedbeds."
                ),
                isDemoMode = true
            )
        ),
        SamplePlantCase(
            id = "sample_tomato_blight",
            label = "Kamatis Yellow Spots",
            cropName = "Kamatis (Tomato)",
            symptomPreview = "Dark brown concentric rings on older lower tomato leaves with yellow halos",
            demoDiagnosis = PlantDiagnosisResult(
                cropName = "Kamatis (Tomato)",
                conditionName = "Early Blight (Alternaria solani)",
                severity = "Moderate",
                summary = "Target-like dark rings starting on older lower leaves with yellowing edges point to Tomato Early Blight, common after rain splashes.",
                organicSolutions = listOf(
                    "Prune off the affected lower leaves and dispose of them away from the garden.",
                    "Apply rice straw mulch under plants so rain doesn't splash fungal spores from soil onto leaves.",
                    "Spray diluted baking soda + mild soap or neem oil solution in late afternoon."
                ),
                preventionTips = listOf(
                    "Water only at the base of roots in the morning so foliage stays dry.",
                    "Rotate tomatoes with corn, legumes, or leafy greens instead of eggplant/peppers.",
                    "Stake plants early for strong sunlight and air circulation."
                ),
                isDemoMode = true
            )
        ),
        SamplePlantCase(
            id = "sample_talong_aphids",
            label = "Talong Curled Leaves",
            cropName = "Talong (Eggplant)",
            symptomPreview = "Curled young shoots, sticky leaf residue, and tiny green insects underneath",
            demoDiagnosis = PlantDiagnosisResult(
                cropName = "Talong (Eggplant)",
                conditionName = "Aphid & Whitefly Infestation",
                severity = "Low",
                summary = "Curled new shoots with sticky honeydew indicate sap-sucking aphids or whiteflies hiding on the underside of young leaves.",
                organicSolutions = listOf(
                    "Wash undersides of leaves with a firm stream of water or spray homemade chili-garlic-soap mix.",
                    "Hang bright yellow sticky traps near the canopy to catch winged adults.",
                    "Encourage ladybugs by planting marigolds along bed edges."
                ),
                preventionTips = listOf(
                    "Inspect young shoots twice a week so small colonies are caught early.",
                    "Avoid over-fertilizing with nitrogen, which produces soft juicy shoots that attract aphids.",
                    "Keep weeds trimmed around plot borders."
                ),
                isDemoMode = true
            )
        )
    )
}
