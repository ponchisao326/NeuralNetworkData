# CobbleAnalytics (ServerBrain)

**CobbleAnalytics** is a high-performance, server-side telemetry agent designed for **Cobblemon** (Fabric 1.21.1).

Unlike traditional loggers that output text files for human reading, CobbleAnalytics functions as a **Big Data ingestion pipeline**. It captures rich, contextual "Behavioral Data" in real-time, structuring it for Time-Series analysis and Machine Learning applications (Churn Prediction, Dynamic Difficulty, Economy Balancing).

## 🚀 Core Philosophy

* **Context is King:** We don't just log `Player1 won a battle`. We capture `{Timestamp, Biome, Opponent_Type, MVP_Pokemon, HP_Remaining, Battle_Duration}`.
* **Zero-Lag Policy:** All database I/O is handled by an asynchronous worker thread using `ConcurrentLinkedQueue` buffering. The server main thread never waits for the database.
* **AI-Ready:** Data is stored in a Hybrid SQL/JSON format, allowing for rigorous indexing on standard metrics (Time, UUID) while preserving complex, unstructured game data (IVs, Movesets) in JSON for future Neural Network training.

---

## 🏗 Data Architecture

The system utilizes a centralized **Fact Table** approach rather than fragmenting data across dozens of tables. This simplifies downstream analytics.

### Schema (MariaDB / MySQL)

The schema combines rigid SQL columns for high-performance indexing with a flexible JSON column for event-specific deep data.

```sql
-- Dimension Table: Player static data (Slowly Changing Dimension)
CREATE TABLE dim_players (
    player_uuid VARCHAR(36) PRIMARY KEY,
    player_name VARCHAR(32),
    first_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP,
    total_playtime_seconds INT DEFAULT 0,
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
    category VARCHAR(20),    -- e.g., 'ECONOMY', 'COMBAT', 'EXPLORATION'
    action_type VARCHAR(50), -- e.g., 'POKEMON_CAUGHT', 'GTS_TRADE', 'RAID_FAIL'
    
    -- THE AI BRAIN: Unstructured Contextual Data
    context_data JSON, 
    
    -- Spatial Data for Heatmaps
    world VARCHAR(50),
    biome VARCHAR(100),
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

The mod hooks into Fabric and Cobblemon events to capture the following "Sensors":

### A. Cobblemon Core (`COBBLEMON_CORE`)

*Purpose: Train the AI to understand asset valuation and player collecting habits.*

* **`POKEMON_CAUGHT`**
* **Context Data:**
* `pokemon_uuid`: (UUID)
* `species`: (String) e.g., "charmander"
* `level`: (Int) e.g., 15
* `nature`: (String) e.g., "adamant"
* `ability`: (String) Hidden vs Standard
* `shiny`: (Boolean)
* `ivs`: (Object) `{"hp": 31, "atk": 10, ...}`
* `ball_used`: (String) Tracks resource usage (e.g., Masterball on Pidgey?)
* `pokedex_completion`: (Int) % at moment of capture.
* `timestamp`: (Long) Tracks when the Pokemon is captured




* **`POKEMON_RELEASED`**


* **`EGG_HATCHED`**
* **Context Data:** `cycles_needed`, `parents_species`, `shiny_charm_active`.



### B. Combat Analytics (`COMBAT_ANALYTICS`)

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



### C. Economy Flow (`ECONOMY_FLOW`)

*Purpose: Monitor inflation and market health.*

* **`GTS_TRADE`** (Integration with GTS mods)
* **Context Data:** `item_sold`, `price`, `time_on_market`.


* **`SHOP_TRANSACTION`**
* Tracks NPC interactions and currency sinks.



---

## ⚙️ Technical Stack

* **Language:** Java 21
* **Loader:** Fabric Loader
* **Database:** MariaDB / MySQL 8.0+
* **Driver:** `mysql-connector-j` (9.1.0)
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