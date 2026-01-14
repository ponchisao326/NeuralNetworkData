-- Time Held Calculation --
SELECT
    c.species,
    c.shiny,
    (r.timestamp - c.timestamp) / 1000 AS segundos_poseido
FROM pokemon_released r
JOIN pokemon_caught c ON r.pokemon_uuid = c.pokemon_uuid;