# CobbleAnalytics (ServerBrain)

**CobbleAnalytics** is a high-performance, server-side telemetry agent designed for **Cobblemon** (Fabric 1.21.1 / Yarn Mappings).

Unlike traditional loggers that output text files for human reading, CobbleAnalytics functions as a **Big Data ingestion pipeline**. It captures rich, contextual "Behavioral Data" in real-time, structuring it for Time-Series analysis and Machine Learning applications (Churn Prediction, Dynamic Difficulty, Economy Balancing).

## 🚀 Core Philosophy

* **Context is King:** We don't just log `Player1 caught a Pokemon`. We capture `{Timestamp, Biome_Key, IV_Distribution, Dex_Completion_%, Ball_Used}`.
* **Zero-Lag Policy:** NO database I/O occurs on the main server thread.
* **Flow:** `Game Event` -> `Extract Data` -> `Immutable Record (DTO)` -> `ConcurrentLinkedQueue` -> `Async Writer Thread`.


* **AI-Ready:** Data is stored in a Hybrid SQL/JSON format. We use **Java Records** with internal `Gson` serialization to store complex, unstructured game data (IVs, Movesets) in JSON, while indexing standard metrics (Time, UUID) in SQL.

---

## 🏗 Data Architecture

The system utilizes a centralized **Fact Table** approach.

### Schema (MariaDB / MySQL)

The schema combines rigid SQL columns for high-performance indexing with a flexible JSON column for event-specific deep data.

```sql
-- Dimension Table: Player static data (Slowly Changing Dimension)
CREATE TABLE dim_players (
    player_uuid VARCHAR(36) PRIMARY KEY,
    player_name VARCHAR(32),
    first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    -- Game Demographics
    favorite_starter VARCHAR(50), 
    team_affinity VARCHAR(50) -- FTB Teams integration
);

-- Fact Table: The "Big Data" Stream (Time Series)
CREATE TABLE fact_game_events (
    event_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3), -- Millisecond precision
    player_uuid VARCHAR(36),
    
    -- High-Level Categorization for SQL Indexing
    category VARCHAR(20),    -- e.g., 'LIFECYCLE', 'COMBAT', 'ECONOMY'
    action_type VARCHAR(50), -- e.g., 'POKEMON_CAUGHT', 'BATTLE_END'
    
    -- THE AI BRAIN: Unstructured Contextual Data
    context_data JSON, 
    
    -- Spatial Data for Heatmaps
    world VARCHAR(50),
    biome VARCHAR(100), -- Stored as registry key (e.g., "minecraft:plains")
    pos_x INT,
    pos_y INT,
    pos_z INT,
    
    INDEX idx_time (timestamp),
    INDEX idx_player (player_uuid),
    INDEX idx_category (category)
);

```

---

## 📡 Data Dictionary (Sensors)

The mod hooks into Fabric and Cobblemon events.

### A. Pokemon Lifecycle (`POKEMON_LIFECYCLE`) [IMPLEMENTED]

*Purpose: Train the AI to understand asset valuation, breeding habits, and player retention.*

* **`POKEMON_CAUGHT`**
* **Trigger:** `PokemonCapturedEvent`
* **Context Data (JSON):**
* `species`: (String) e.g., "charmander"
* `level`: (Int)
* `shiny`: (Boolean)
* `ivs`: (Object) `{"hp": 31, "atk": 10, ...}`
* `ball_used`: (String) Tracks resource usage vs asset value.
* `dex_completion`: (Float) National Dex % at the exact moment of capture.
* `location`: (Object) XYZ coordinates.




* **`POKEMON_RELEASED`**
* **Trigger:** `ReleasePokemonEvent`
* **Context Data (JSON):**
* `species`: (String)
* `shiny`: (Boolean)
* `capture_timestamp`: (Long) Used by SQL/Dashboard to calculate "Time Held" (Retention).
* `biome`: (String) Extracted via `world.getBiome(pos).getKey()` for precise mapping.




* **`POKEMON_HATCHED`**
* **Trigger:** `PokemonHatchedEvent`
* **Logic Note:** Cobblemon strips parent NBT on egg creation. We cannot track lineage directly.
* **Strategy:** **"Genetic Result Analysis"**. We infer intent by analyzing the output.
* **Context Data (JSON):**
* `species`: (String)
* `iv_sum`: (Int) Total IV stats (High IVs + Specific Biome = Intentional Breeding).
* `shiny`: (Boolean)
* `location`: (Object) Used to detect "Hatching loops/paths".





### B. Combat Analytics (`COMBAT_ANALYTICS`) [PLANNED]

*Purpose: Balance PvE/Raids and auto-adjust difficulty curves.*

* **`BATTLE_END`**
* **Context Data:**
* `opponent_type`: "WILD", "TRAINER_NPC", "PLAYER", "RAID_BOSS"
* `result`: "WIN", "LOSS", "FLEE"
* `duration_ticks`: (Int) Length of engagement.
* `mvp_pokemon`: The party member dealing the most damage.
* `fainted_count`: Number of party deaths (measure of struggle).




* **`RAID_INTERACTION`**
* **Context Data:** `raid_tier`, `boss_species`, `participants_count`, `damage_dealt`.



### C. Economy & Player (`ECONOMY_FLOW` / `PLAYER_SESSION`) [PLANNED]

*Purpose: Monitor inflation, market health, and "Rage Quits".*

* **`GTS_TRADE`** (Integration with GTS mods)
* **Context Data:** `item_sold`, `price`, `time_on_market`.


* **`SESSION_END`**
* **Context Data:** `duration`, `quit_reason` (Detects if quit happened immediately after a Battle Loss).



---

## ⚙️ Technical Stack

* **Language:** Java 21
* **Loader:** Fabric Loader
* **Mappings:** Yarn
* **Database:** MariaDB / MySQL 8.0+
* **Serialization:** Java Records + Static Gson
* **Connection Pooling:** HikariCP (Optimized for high-concurrency inserts)

## 🔧 Configuration

1. Drop the jar into `/mods`.
2. Configure database credentials in `config/cobbleanalytics.json`:

```json
{
  "database": {
    "host": "127.0.0.1",
    "port": 3306,
    "schema": "minecraft_analytics",
    "user": "admin",
    "password": "secure_password",
    "batch_size": 50,
    "flush_interval_seconds": 10
  }
}

```