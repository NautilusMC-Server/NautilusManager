package org.nautilusmc.nautilusmanager.crews;

import java.util.Objects;
import java.util.UUID;

public class War {
    private UUID uuid;
    private Crew attacker;
    private Crew defender;

    public War(Crew attacker, Crew defender) {
        uuid = UUID.randomUUID();
        this.attacker = attacker;
        this.defender = defender;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Crew getAttacker() {
        return attacker;
    }

    public void setAttacker(Crew attacker) {
        this.attacker = attacker;
    }

    public Crew getDefender() {
        return defender;
    }

    public void setDefender(Crew defender) {
        this.defender = defender;
    }

    @Override
    public boolean equals(Object that) {
        return this == that
                || that instanceof War war && (Objects.equals(attacker, war.attacker) && Objects.equals(defender, war.defender) || Objects.equals(defender, war.attacker) && Objects.equals(attacker, war.defender));
    }
}
