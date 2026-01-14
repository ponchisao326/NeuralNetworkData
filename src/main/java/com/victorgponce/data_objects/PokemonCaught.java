package com.victorgponce.data_objects;

public class PokemonCaught {

    private String species;
    private int level;
    private String nature;
    private String ability;
    private boolean shiny;
    private Ivs ivs;
    private String ballUsed;
    private float pokedexCompletion;

    public PokemonCaught(String species, int level, String nature, String ability, boolean shiny, Ivs ivs, String ballUsed, float pokedexCompletion) {
        this.species = species;
        this.level = level;
        this.nature = nature;
        this.ability = ability;
        this.shiny = shiny;
        this.ivs = ivs;
        this.ballUsed = ballUsed;
        this.pokedexCompletion = pokedexCompletion;
    }

    public String getSpecies() {
        return species;
    }

    public int getLevel() {
        return level;
    }

    public String getNature() {
        return nature;
    }

}
