package com.victorgponce.model;

public record Ivs(int PS_IV, int ATTACK_IV, int DEFENCE_IV, int SP_ATTACK_IV, int SP_DEFENSE_IV, int SPEED_IV) {
    public int getTotalIvs() {
        return PS_IV + ATTACK_IV + DEFENCE_IV + SP_ATTACK_IV + SP_DEFENSE_IV + SPEED_IV;
    }
}
