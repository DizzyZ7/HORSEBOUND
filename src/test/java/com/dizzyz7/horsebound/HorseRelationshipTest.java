// HORSEBOUND — Created by Dimash Janibekov (DizZyZ7), © 2026. All rights reserved.
package com.dizzyz7.horsebound;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HorseRelationshipTest {
    @Test
    void curiousHorseBuildsTrustFasterThanStubbornHorse() {
        HorseRelationship curious = HorseRelationship.wild();
        HorseRelationship stubborn = HorseRelationship.wild();

        curious.feed(HorsePersonality.CURIOUS);
        stubborn.feed(HorsePersonality.STUBBORN);

        assertTrue(curious.trust() > stubborn.trust());
        assertTrue(curious.bond() > stubborn.bond());
    }

    @Test
    void shyHorseAccumulatesMoreFearFromSameThreat() {
        HorseRelationship shy = new HorseRelationship(40f, 10f, 0f);
        HorseRelationship brave = new HorseRelationship(40f, 10f, 0f);

        shy.observeThreat(10f, HorsePersonality.SHY);
        brave.observeThreat(10f, HorsePersonality.BRAVE);

        assertTrue(shy.fear() > brave.fear());
        assertTrue(shy.trust() < brave.trust());
    }

    @Test
    void pettingBuildsBondAndCalmsFear() {
        HorseRelationship relationship = new HorseRelationship(55f, 20f, 40f);

        relationship.pet(HorsePersonality.CALM);

        assertTrue(relationship.trust() > 55f);
        assertTrue(relationship.bond() > 20f);
        assertTrue(relationship.fear() < 40f);
    }

    @Test
    void valuesStayWithinZeroToOneHundred() {
        HorseRelationship relationship = new HorseRelationship(500f, -20f, 500f);

        assertEquals(100f, relationship.trust());
        assertEquals(0f, relationship.bond());
        assertEquals(100f, relationship.fear());

        relationship.calm(1000f);
        assertEquals(0f, relationship.fear());
    }
}
