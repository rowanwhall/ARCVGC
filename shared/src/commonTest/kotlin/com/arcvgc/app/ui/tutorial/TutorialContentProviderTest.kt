package com.arcvgc.app.ui.tutorial

import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TutorialContentProviderTest {

    @Test
    fun everyConfiguredStepHasStrings() {
        TutorialConfig.steps.forEach { step ->
            val strings = TutorialContentProvider.get(step.id)
            assertNotNull(strings, "Missing TutorialString for step id='${step.id}'")
            assertTrue(strings.title.isNotBlank(), "Empty title for step id='${step.id}'")
            assertTrue(strings.body.isNotBlank(), "Empty body for step id='${step.id}'")
        }
    }

    @Test
    fun unknownIdReturnsNull() {
        assertNull(TutorialContentProvider.get("nonexistent_tutorial_id"))
    }

    @Test
    fun resolveFallsBackToProviderWhenNoOverride() {
        // All actual platforms return null from `tutorialOverride`, so resolve()
        // should return the same as get() for every configured step.
        TutorialConfig.steps.forEach { step ->
            val resolved = TutorialContentProvider.resolve(step.id)
            val direct = TutorialContentProvider.get(step.id)
            assertNotNull(resolved)
            assertNotNull(direct)
            kotlin.test.assertEquals(direct, resolved)
        }
    }

    @Test
    fun configHasAtLeastOneStep() {
        assertTrue(TutorialConfig.steps.isNotEmpty())
    }
}
