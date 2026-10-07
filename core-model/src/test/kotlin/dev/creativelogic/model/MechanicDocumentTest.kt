package dev.creativelogic.model

import org.junit.Assert.assertEquals
import org.junit.Test

class MechanicDocumentTest {
    private val document = MechanicDocumentV1(
        mechanicMetadata = MechanicMetadataV1("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee", "Vault", 100, 200),
        notes = "Quoted \"notes\"\n→ rebuild manually",
    )

    @Test fun explicitContractRoundTripsMetadataAndNotes() {
        assertEquals(document, MechanicDocumentCodec.decode(MechanicDocumentCodec.encode(document)))
    }

    @Test(expected = IllegalArgumentException::class)
    fun futureVersionRequiresAnExplicitMigration() {
        MechanicDocumentCodec.decode(MechanicDocumentCodec.encode(document).replace("\"formatVersion\":1", "\"formatVersion\":2"))
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidIdCannotBeStored() {
        MechanicDocumentCodec.encode(document.copy(mechanicMetadata = document.mechanicMetadata.copy(id = "not-an-id")))
    }

    @Test(expected = IllegalArgumentException::class)
    fun unsupportedGraphContentCannotBeSilentlyDiscarded() {
        val source = MechanicDocumentCodec.encode(document).replace("\"devices\":[]", "\"devices\":[{\"type\":\"trigger\"}]")
        MechanicDocumentCodec.decode(source)
    }
}
