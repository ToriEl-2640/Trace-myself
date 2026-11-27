package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.data.model.CatalogOpportunity
import com.example.data.model.DatasetAnalysis
import com.example.data.model.FieldEvidence
import com.example.data.model.LibraryItem
import com.example.data.model.MetricDataPoint
import com.example.data.model.ProblemInvestigation
import com.example.data.model.ResearchTopic
import com.example.data.model.TopicCategory
import com.example.data.model.TrackedOpportunity
import com.example.data.model.UserProfile
import com.example.util.OperationType
import com.example.util.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TraceRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    // --- User Profile ---
    fun observeUserProfile(): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) snapshot.toObject(UserProfile::class.java) else null
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.READ, path)
                    }
                    throw error
                }
        )
    }

    suspend fun saveUserProfile(displayName: String, email: String, interestTrack: String, researchLevel: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid)
        val snapshot = docRef.get().await()
        if (!snapshot.exists()) {
            val payload = mapOf(
                "userId" to uid,
                "displayName" to displayName,
                "email" to email,
                "interestTrack" to interestTrack,
                "researchLevel" to researchLevel,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
        } else {
            val updatePayload = mapOf(
                "displayName" to displayName,
                "email" to email,
                "interestTrack" to interestTrack,
                "researchLevel" to researchLevel,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.update(updatePayload).await()
        }
    }

    // --- Everyday Problems Lab ---
    fun observeProblems(): Flow<List<ProblemInvestigation>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/problems"
        emitAll(
            db.collection("users").document(uid).collection("problems")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(ProblemInvestigation::class.java)
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getProblemById(problemId: String): ProblemInvestigation? {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("problems").document(problemId)
        val snapshot = docRef.get().await()
        return snapshot.toObject(ProblemInvestigation::class.java)
    }

    suspend fun createProblem(
        title: String,
        sector: String,
        location: String,
        problemStatement: String,
        tags: List<String> = emptyList()
    ): String {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("problems").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "sector" to sector,
            "location" to location,
            "status" to "investigating",
            "problemStatement" to problemStatement,
            "existingResearch" to "",
            "unaddressedGaps" to "",
            "evidenceNotes" to "",
            "proposedSolutions" to "",
            "opportunitySummary" to "",
            "evidenceCount" to 0L,
            "tags" to tags,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            return docRef.id
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateProblemStep(
        problemId: String,
        status: String? = null,
        problemStatement: String? = null,
        existingResearch: String? = null,
        unaddressedGaps: String? = null,
        evidenceNotes: String? = null,
        proposedSolutions: String? = null,
        opportunitySummary: String? = null
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("problems").document(problemId)
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to FieldValue.serverTimestamp()
        )
        status?.let { updates["status"] = it }
        problemStatement?.let { updates["problemStatement"] = it }
        existingResearch?.let { updates["existingResearch"] = it }
        unaddressedGaps?.let { updates["unaddressedGaps"] = it }
        evidenceNotes?.let { updates["evidenceNotes"] = it }
        proposedSolutions?.let { updates["proposedSolutions"] = it }
        opportunitySummary?.let { updates["opportunitySummary"] = it }

        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    suspend fun deleteProblem(problemId: String) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("problems").document(problemId)
        try {
            docRef.delete().await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            throw e
        }
    }

    // --- Evidence ---
    fun observeEvidence(problemId: String): Flow<List<FieldEvidence>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/problems/$problemId/evidence"
        emitAll(
            db.collection("users").document(uid).collection("problems").document(problemId).collection("evidence")
                .snapshots()
                .map { it.toObjects(FieldEvidence::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addEvidence(
        problemId: String,
        title: String,
        evidenceType: String,
        content: String,
        dataValue: String,
        locationTag: String
    ) {
        val uid = requireUserId()
        val problemDocRef = db.collection("users").document(uid).collection("problems").document(problemId)
        val docRef = problemDocRef.collection("evidence").document()
        val payload = mapOf(
            "userId" to uid,
            "problemId" to problemId,
            "title" to title,
            "evidenceType" to evidenceType,
            "content" to content,
            "dataValue" to dataValue,
            "locationTag" to locationTag,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
            problemDocRef.update(
                mapOf(
                    "evidenceCount" to FieldValue.increment(1),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    // --- Research Library ---
    fun observeLibrary(): Flow<List<LibraryItem>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/library"
        emitAll(
            db.collection("users").document(uid).collection("library")
                .snapshots()
                .map { it.toObjects(LibraryItem::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addLibraryItem(
        title: String,
        source: String,
        authors: String = "",
        publicationYear: String = "2024",
        domain: String = "General",
        summary: String = "",
        keyFindings: String = "",
        simplifiedExplanation: String = "",
        url: String = "",
        notes: String = ""
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("library").document()
        val payload = mapOf(
            "userId" to uid,
            "title" to title,
            "source" to source,
            "authors" to authors,
            "publicationYear" to publicationYear,
            "domain" to domain,
            "summary" to summary,
            "keyFindings" to keyFindings,
            "simplifiedExplanation" to simplifiedExplanation,
            "url" to url,
            "isBookmarked" to true,
            "notes" to notes,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun toggleBookmark(itemId: String, isBookmarked: Boolean) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("library").document(itemId)
        try {
            docRef.update(
                mapOf(
                    "isBookmarked" to isBookmarked,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Opportunities ---
    fun observeTrackedOpportunities(): Flow<List<TrackedOpportunity>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/tracked_opportunities"
        emitAll(
            db.collection("users").document(uid).collection("tracked_opportunities")
                .snapshots()
                .map { it.toObjects(TrackedOpportunity::class.java) }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun trackOpportunity(
        catalogOpp: CatalogOpportunity,
        stage: String = "interested",
        notes: String = ""
    ) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tracked_opportunities").document(catalogOpp.id)
        val payload = mapOf(
            "userId" to uid,
            "opportunityId" to catalogOpp.id,
            "title" to catalogOpp.title,
            "organization" to catalogOpp.organization,
            "fundingAmount" to catalogOpp.fundingAmount,
            "deadline" to catalogOpp.deadline,
            "category" to catalogOpp.category,
            "stage" to stage,
            "notes" to notes,
            "createdAt" to FieldValue.serverTimestamp()
        )
        try {
            docRef.set(payload).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            throw e
        }
    }

    suspend fun updateTrackedStage(oppDocId: String, newStage: String, notes: String? = null) {
        val uid = requireUserId()
        val docRef = db.collection("users").document(uid).collection("tracked_opportunities").document(oppDocId)
        val updates = mutableMapOf<String, Any>(
            "stage" to newStage,
            "updatedAt" to FieldValue.serverTimestamp()
        )
        notes?.let { updates["notes"] = it }
        try {
            docRef.update(updates).await()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            throw e
        }
    }

    // --- Seed Starter Investigations for First-Time Users ---
    suspend fun seedInitialDataIfEmpty() {
        val uid = requireUserId()
        val problemsColl = db.collection("users").document(uid).collection("problems")
        val existingProblems = problemsColl.limit(1).get().await()
        if (existingProblems.isEmpty) {
            // Seed 1: Post-harvest tomato losses in Nigeria
            val p1 = problemsColl.document()
            p1.set(
                mapOf(
                    "userId" to uid,
                    "title" to "Post-Harvest Tomato Losses in Northern Nigeria",
                    "sector" to "Agriculture & Food",
                    "location" to "Kano & Kaduna, Nigeria",
                    "status" to "evidence_collected",
                    "problemStatement" to "Smallholder tomato farmers experience 40-50% post-harvest spoilage during the 700km transit to southern urban markets like Lagos. The root causes are wooden crate compression, non-refrigerated transit trucks, and lack of farm-gate precooling.",
                    "existingResearch" to "CGIAR and FAO studies (2022) confirm that inadequate ventilation in raffia baskets and lack of local solar cold storage account for $1.8B annual agricultural value destruction in Nigeria. Existing solutions focus on capital-heavy cold chain warehouses, inaccessible to smallholders.",
                    "unaddressedGaps" to "1. Low-cost passive evaporative cooling chambers ('Zero Emission Coolers') using local clay and sand are rarely paired with digital off-taker logistics.\n2. No micro-leasing models exist for plastic collapsible returnable crates.\n3. Market price transparency is fragmented between Northern farm hubs and Southern buyers.",
                    "evidenceNotes" to "Field survey of 18 farmers in Kano: average transit loss is 38 crates per 100 crates shipped. Farmers sell at distress prices of N4,000/crate during peak glut instead of N18,000 off-season.",
                    "proposedSolutions" to "1. Village-level decentralized evaporative cooling hubs powered by solar water pumps.\n2. Tokenized returnable crate pooling to replace wooden raffia baskets.\n3. Mobile USSD booking system linking refrigerated backhaul trucks returning empty from the North.",
                    "opportunitySummary" to "High commercial potential for agro-logistics startup with blended grant funding. Estimated addressable market: N42B in post-harvest loss recovery.",
                    "evidenceCount" to 2L,
                    "tags" to listOf("Agriculture", "Cold Chain", "Nigeria", "Food Security"),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed Evidence for P1
            p1.collection("evidence").document().set(
                mapOf(
                    "userId" to uid,
                    "problemId" to p1.id,
                    "title" to "Dawanau Market Crate Spoilage Count",
                    "evidenceType" to "metric",
                    "content" to "Counted 42 smashed and rotten crates out of 110 unloaded from Bedford truck. Ambient temperature was 36°C.",
                    "dataValue" to "38.1% loss rate",
                    "locationTag" to "Dawanau Market, Kano",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            // Seed 2: Affordable packaging costs for small food businesses
            val p2 = problemsColl.document()
            p2.set(
                mapOf(
                    "userId" to uid,
                    "title" to "High Packaging Costs for Micro Food Producers",
                    "sector" to "Packaging & Materials",
                    "location" to "Lagos & Ibadan, Nigeria",
                    "status" to "investigating",
                    "problemStatement" to "Micro food processors (packaged spices, plantain chips, dried garri) spend up to 35% of total product cost on imported laminate pouches and plastic seals, eroding margins amid currency devaluation.",
                    "existingResearch" to "UNIDO data indicates over 80% of flexible barrier packaging in West Africa is imported as virgin multi-layer polymer film. Local recycling infrastructure cannot process multi-layer composites.",
                    "unaddressedGaps" to "Lack of accessible bio-composite films produced from cassava starch and plantain pseudo-stem fiber that meet moisture-barrier standards for dry goods shelf life.",
                    "evidenceNotes" to "Interviews with 12 small confectionery producers: packaging unit costs tripled from N35 to N110 per pouch over 18 months.",
                    "proposedSolutions" to "Cassava-starch and agricultural byproduct compostable pouch extrusion cooperative.",
                    "opportunitySummary" to "Import substitution opportunity with circular economy carbon credits.",
                    "evidenceCount" to 1L,
                    "tags" to listOf("Manufacturing", "Circular Economy", "SME"),
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }

        // Seed Library if empty
        val libraryColl = db.collection("users").document(uid).collection("library")
        val existingLibrary = libraryColl.limit(1).get().await()
        if (existingLibrary.isEmpty) {
            libraryColl.document().set(
                mapOf(
                    "userId" to uid,
                    "title" to "Solar-Powered Evaporative Cooling for Sub-Saharan Smallholders",
                    "source" to "CGIAR Research Program on Climate Change & Food Security",
                    "authors" to "Adeyemi, O., & Mensah, K.",
                    "publicationYear" to "2023",
                    "domain" to "Agriculture & Energy",
                    "summary" to "Empirical evaluation of zero-energy cool chambers (ZECC) combined with solar fans in Northern Nigeria, extending vegetable shelf-life from 2 days to 14 days.",
                    "keyFindings" to "Maintained internal chamber temperature 10-12°C lower than ambient, reducing tomato decay rate by 72% at $420 total installation cost.",
                    "simplifiedExplanation" to "By building a double-walled brick chamber filled with wet sand and a tiny solar fan, farmers keep tomatoes fresh for two weeks without an expensive diesel generator.",
                    "url" to "https://cgiar.org/research/evaporative-cooling-nigeria",
                    "isBookmarked" to true,
                    "notes" to "Directly applicable to our Everyday Problems Lab tomato case study.",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()

            libraryColl.document().set(
                mapOf(
                    "userId" to uid,
                    "title" to "Decentralized Physical Infrastructure Networks (DePIN) for Rural Cold Chains",
                    "source" to "MIT Cryptoeconomics Lab & Cambridge Alternative Finance",
                    "authors" to "Zhang, L., & Ibrahim, M.",
                    "publicationYear" to "2024",
                    "domain" to "Blockchain & IoT",
                    "summary" to "Designing tokenized sensor incentives for monitoring temperature and humidity across distributed refrigerated transit in developing markets.",
                    "keyFindings" to "Cryptographic proof-of-cooling allows micro-insurers to automatically pay indemnity claims upon transit temperature breach.",
                    "simplifiedExplanation" to "Small temperature sensors record coldness onto a secure digital ledger. If goods get too warm on the road, insurance pays out instantly without months of paperwork.",
                    "url" to "https://mit.edu/depin-rural-logistics",
                    "isBookmarked" to true,
                    "notes" to "Great cross-over of blockchain, IoT, and logistics research.",
                    "createdAt" to FieldValue.serverTimestamp()
                )
            ).await()
        }
    }

    // --- Static Curated Catalogs for Explorer & Opportunities ---
    fun getCuratedTopics(): List<ResearchTopic> = listOf(
        ResearchTopic(
            id = "top_1",
            title = "Decentralized Cold-Chain Microgrids for Rural Markets",
            category = TopicCategory.COMMUNITY_CHALLENGE,
            sector = "Agriculture & Renewable Energy",
            region = "West & East Africa",
            urgencyLevel = "High Urgency",
            overview = "Over 45% of fresh horticultural produce in emerging markets is lost between farm gate and retail centers due to broken cold chains.",
            keyQuestions = listOf(
                "How can pay-as-you-chill solar hubs achieve break-even unit economics in off-grid trading centers?",
                "What phase-change materials (PCM) can sustain 4°C cooling during 12-hour transport outages?",
                "Can local farmer cooperatives manage shared cold lockers reliably?"
            ),
            relevanceToIndustry = "Transforms smallholder net farmer revenues by 35% and opens direct B2B supply lines to modern supermarkets.",
            relatedDisciplines = listOf("Agro-economics", "Thermal Engineering", "Microgrid Design"),
            potentialImpact = "Reduces national food waste by 1.2M tons annually and stabilizes seasonal price inflation."
        ),
        ResearchTopic(
            id = "top_2",
            title = "Cassava & Banana Fiber Bioplastics as Single-Use Plastic Alternatives",
            category = TopicCategory.EMERGING_INDUSTRY,
            sector = "Circular Economy & Materials Science",
            region = "Global South / Sub-Saharan Africa",
            urgencyLevel = "High Urgency",
            overview = "Banning single-use plastics without cost-competitive local substitutes hurts micro-retailers. Agricultural waste fibers offer an abundant feed-stock.",
            keyQuestions = listOf(
                "What plasticizer ratios allow starch-fiber films to achieve tensile strength comparable to LDPE?",
                "How do local composting timelines vary under tropical soil and humidity conditions?",
                "What is the cost per unit compared to petroleum-derived polyethylene?"
            ),
            relevanceToIndustry = "Creates closed-loop agricultural value chains and avoids import tariffs on petroleum polymers.",
            relatedDisciplines = listOf("Polymer Chemistry", "Agricultural Byproduct Engineering", "Industrial Design"),
            potentialImpact = "Replaces 200,000 tons of landfill-bound non-biodegradable packaging."
        ),
        ResearchTopic(
            id = "top_3",
            title = "DePIN & Tokenized Carbon Offsets for Smallholder Agroforestry",
            category = TopicCategory.FINANCE_BLOCKCHAIN,
            sector = "Climate Finance & Blockchain",
            region = "Global Emerging Markets",
            urgencyLevel = "Emerging",
            overview = "Smallholder tree-planting initiatives are historically locked out of voluntary carbon markets due to prohibitive satellite and ground MRV (Measurement, Reporting, Verification) costs.",
            keyQuestions = listOf(
                "Can smartphone LiDAR and drone photogrammetry deliver sub-$2 tree biomass verification?",
                "How to design automated smart contract escrow payouts directly to mobile money wallets?",
                "What tokenomics prevent front-running by predatory intermediary carbon brokers?"
            ),
            relevanceToIndustry = "Directly channels billions in global voluntary carbon credit liquidity to rural households.",
            relatedDisciplines = listOf("Tokenomics", "Remote Sensing", "Forestry", "Decentralized Finance"),
            potentialImpact = "Increases smallholder household annual cash revenue by $300-$700 via verifiable carbon credits."
        ),
        ResearchTopic(
            id = "top_4",
            title = "Zero-Rated Mesh Networks for Open Educational Content",
            category = TopicCategory.COMMUNITY_CHALLENGE,
            sector = "Education & Connectivity",
            region = "Semi-Urban & Rural Institutions",
            urgencyLevel = "Medium",
            overview = "Tertiary and secondary students face steep mobile data costs ($2-$4/GB) that restrict access to open-source textbooks and academic journals.",
            keyQuestions = listOf(
                "What caching topology enables 500+ students to sync digital textbooks over Wi-Fi Direct or low-cost campus relays?",
                "How can offline-first digital credentials verify course completion without persistent internet?",
                "What micropayment structures can sustain solar-powered campus cache nodes?"
            ),
            relevanceToIndustry = "Lowers student textbook overhead by 80% while enabling localized educational content creation.",
            relatedDisciplines = listOf("Distributed Systems", "EdTech", "Public Policy"),
            potentialImpact = "Democratizes higher education access for over 4 million underserved students."
        ),
        ResearchTopic(
            id = "top_5",
            title = "Informal E-Waste Recycler Health & Urban Copper Recovery",
            category = TopicCategory.UNANSWERED_QUESTION,
            sector = "Urban Waste & Public Health",
            region = "West Africa (Agbogbloshie, Alaba)",
            urgencyLevel = "High Urgency",
            overview = "Open burning of computer cables and circuit boards yields high-grade copper but emits neurotoxic dioxins affecting thousands of informal workers.",
            keyQuestions = listOf(
                "What low-cost mechanical cable strippers can match the throughput speed of open cable burning?",
                "Can hydrometallurgical leaching be safely miniaturized for cooperative scrap workshops?",
                "What traceability protocols allow ethical electronics manufacturers to source urban recycled copper?"
            ),
            relevanceToIndustry = "Validates ethical circular metal sourcing for green electronics manufacturers worldwide.",
            relatedDisciplines = listOf("Metallurgical Engineering", "Environmental Toxicology", "Labor Economics"),
            potentialImpact = "Eliminates toxic lead and dioxin exposure for 35,000 workers while boosting copper yield by 28%."
        )
    )

    fun getCatalogOpportunities(): List<CatalogOpportunity> = listOf(
        CatalogOpportunity(
            id = "opp_gates_agtech",
            title = "Grand Challenges: Innovative Post-Harvest & Cold Chain Solutions",
            organization = "Bill & Melinda Gates Foundation",
            fundingAmount = "$100,000 - $500,000",
            deadline = "November 30, 2026",
            category = "Grant",
            focusArea = "Agricultural Productivity & Food Loss Reduction",
            eligibleRegions = "Sub-Saharan Africa, South Asia",
            description = "Seeking grassroots researchers and early-stage innovators testing low-cost, decentralized storage and transportation methods that preserve smallholder crop value.",
            applicationUrl = "https://gcgh.grandchallenges.org"
        ),
        CatalogOpportunity(
            id = "opp_innovate_africa",
            title = "African Youth Climate & Circular Economy Fellowship",
            organization = "Innovate Africa & African Development Bank",
            fundingAmount = "$25,000 Seed Grant + Mentorship",
            deadline = "December 15, 2026",
            category = "Fellowship",
            focusArea = "Biomaterials, Plastic Alternatives, Waste Valorization",
            eligibleRegions = "All African Countries",
            description = "A 9-month incubation and prototyping fellowship for independent researchers turning municipal and agricultural waste into commercial products.",
            applicationUrl = "https://innovateafrica.fund/fellowship"
        ),
        CatalogOpportunity(
            id = "opp_undp_ocean_waste",
            title = "UNDP Ocean & Urban Plastic Innovation Challenge",
            organization = "United Nations Development Programme",
            fundingAmount = "Up to $40,000",
            deadline = "January 20, 2027",
            category = "Innovation Challenge",
            focusArea = "Drainage Clog Prevention & Sustainable Packaging",
            eligibleRegions = "Developing Coastal & Inland Market Cities",
            description = "Supports applied research that prevents plastic waste from entering drainage systems and waterways through localized collection and recycling.",
            applicationUrl = "https://undp.org/challenges/plastic-innovation"
        ),
        CatalogOpportunity(
            id = "opp_web3_climate",
            title = "DePIN & Public Goods Research Residency",
            organization = "Ethereum Climate Platform & Filecoin Foundation",
            fundingAmount = "$50,000 + Compute Credits",
            deadline = "February 10, 2027",
            category = "Research Residency",
            focusArea = "Decentralized Physical Infrastructure, Sensor Networks, Transparent Supply Chains",
            eligibleRegions = "Global Open Call",
            description = "Funding independent software engineers, researchers, and field data scientists building open hardware and smart contract telemetry for real-world environmental problems.",
            applicationUrl = "https://climatecollective.org/grants"
        ),
        CatalogOpportunity(
            id = "opp_unesco_open_sci",
            title = "UNESCO Open Science Early-Career Grant",
            organization = "UNESCO & International Science Council",
            fundingAmount = "€15,000",
            deadline = "March 1, 2027",
            category = "Grant",
            focusArea = "Citizen Science & Community Problem Documentation",
            eligibleRegions = "Low and Middle-Income Countries",
            description = "Supports non-traditional researchers documenting local ecological, public health, and agricultural problems using accessible scientific methodologies.",
            applicationUrl = "https://unesco.org/open-science-awards"
        )
    )

    fun getAnalysisDatasets(): List<DatasetAnalysis> = listOf(
        DatasetAnalysis(
            title = "Tomato Post-Harvest Spoilage by Transit Phase",
            subtitle = "Northern Nigeria to Southern Hubs (Kano to Lagos Corridor)",
            description = "Data collected across 48 truck shipments measuring crate loss percentage under ambient open-air transport vs. ventilated pre-cooled transport.",
            insightSummary = "Crate compression at the base of overloaded trucks causes 52% of total physical damage, followed by heat build-up during traffic delays.",
            dataPoints = listOf(
                MetricDataPoint("Farm Gate Loading", 4.2f, 2.0f, "Handling", "% loss"),
                MetricDataPoint("Highway Transit (Open Bed)", 18.5f, 5.1f, "Vibration", "% loss"),
                MetricDataPoint("Lagos Wholesale Bottleneck", 14.8f, 3.2f, "Heat/Delay", "% loss"),
                MetricDataPoint("Retail Stall Decay (48h)", 11.0f, 2.8f, "Shelf-life", "% loss")
            ),
            keyTakeaway = "Implementing rigid stackable plastic crates and shade stops cuts transit losses from 48.5% down to 13.1%."
        ),
        DatasetAnalysis(
            title = "SME Food Packaging Cost Ratio vs. Net Margin",
            subtitle = "Micro-Food Processors in Lagos State (Sample: N=60)",
            description = "Breakdown of cost components across small packaged goods producers (plantain chips, chin chin, cassava flour, spice blends).",
            insightSummary = "Packaging costs have risen from 12% of total Cost of Goods Sold (COGS) in 2022 to 34% in 2026 due to polymer import tariffs.",
            dataPoints = listOf(
                MetricDataPoint("Raw Food Ingredients", 42f, 0f, "COGS", "%"),
                MetricDataPoint("Packaging Materials", 34f, 0f, "COGS", "%"),
                MetricDataPoint("Energy & Fuel", 14f, 0f, "COGS", "%"),
                MetricDataPoint("Labor & Distribution", 10f, 0f, "COGS", "%")
            ),
            keyTakeaway = "Local cassava-starch biofilm could reduce packaging cost share from 34% to 16%, tripling producer take-home margin."
        ),
        DatasetAnalysis(
            title = "Urban Drain Clogging Composition by Material",
            subtitle = "Market Canal Audits across 5 Major Markets",
            description = "Analysis of solid municipal waste density causing monsoon flash flooding in dense market corridors.",
            insightSummary = "Thin single-use polythene bags ('pure water' sachets and shopping bags) constitute 63% of clog volume.",
            dataPoints = listOf(
                MetricDataPoint("Water Sachets (LDPE)", 41f, 0f, "Plastic", "% volume"),
                MetricDataPoint("Styrofoam Takeaway Packs", 22f, 0f, "Plastic", "% volume"),
                MetricDataPoint("PET Bottles", 16f, 0f, "Plastic", "% volume"),
                MetricDataPoint("Organic Market Waste", 12f, 0f, "Organic", "% volume"),
                MetricDataPoint("Other Textiles & Silt", 9f, 0f, "Mixed", "% volume")
            ),
            keyTakeaway = "Targeted mechanical collection of water sachets at source points yields the highest drainage flood-mitigation ROI."
        )
    )
}
