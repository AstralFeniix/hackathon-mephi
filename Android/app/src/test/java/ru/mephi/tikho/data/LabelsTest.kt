package ru.mephi.tikho.data

import org.junit.Assert.assertEquals
import org.junit.Test
import ru.mephi.tikho.model.speakersLabel
import ru.mephi.tikho.model.utterancesLabel

class LabelsTest {

    @Test
    fun speakerLabelsUseRussianPluralRules() {
        assertEquals("1 говорящий", speakersLabel(1))
        assertEquals("3 говорящих", speakersLabel(3))
        assertEquals("11 говорящих", speakersLabel(11))
        assertEquals("21 говорящий", speakersLabel(21))
    }

    @Test
    fun utteranceLabelsUseRussianPluralRules() {
        assertEquals("1 реплика", utterancesLabel(1))
        assertEquals("3 реплики", utterancesLabel(3))
        assertEquals("6 реплик", utterancesLabel(6))
        assertEquals("11 реплик", utterancesLabel(11))
        assertEquals("21 реплика", utterancesLabel(21))
    }
}
