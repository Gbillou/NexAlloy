package io.github.nexalloy.morphe.music.layout.navigation

import io.github.nexalloy.morphe.AccessFlags
import io.github.nexalloy.morphe.Fingerprint
import io.github.nexalloy.morphe.InstructionLocation.MatchAfterImmediately
import io.github.nexalloy.morphe.InstructionLocation.MatchAfterWithin
import io.github.nexalloy.morphe.Opcode
import io.github.nexalloy.morphe.ResourceType
import io.github.nexalloy.morphe.anyInstruction
import io.github.nexalloy.morphe.checkCast
import io.github.nexalloy.morphe.fieldAccess
import io.github.nexalloy.morphe.findMethodDirect
import io.github.nexalloy.morphe.methodCall
import io.github.nexalloy.morphe.newInstance
import io.github.nexalloy.morphe.opcode
import io.github.nexalloy.morphe.resourceLiteral
import io.github.nexalloy.morphe.resourceMappings
import io.github.nexalloy.morphe.string

val text1Id get() = resourceMappings["id", "text1"]

/** Builds one navigation tab: label, icon enum and visibility. Same filters as Morphe. */
internal object TabLayoutTextFingerprint : Fingerprint(
    accessFlags = listOf(AccessFlags.PUBLIC, AccessFlags.FINAL),
    returnType = "V",
    parameters = listOf("L"),
    filters = listOf(
        anyInstruction(
            string("FEmusic_search"), // 8.49 and lower.
            string("FEsearch"), // 8.50+
            newInstance("Ljava/util/ArrayList;") // 9.21+
        ),

        // Navigation label.
        resourceLiteral(ResourceType.ID, "text1"),
        methodCall(
            smali = "Landroid/view/View;->findViewById(I)Landroid/view/View;",
            location = MatchAfterWithin(5)
        ),
        checkCast(
            type = "Landroid/widget/TextView;",
            location = MatchAfterWithin(5)
        ),

        // Navigation enum: icon type int -> enum.
        anyInstruction(
            opcode(Opcode.SGET_OBJECT),
            opcode(Opcode.IGET_OBJECT)
        ),
        fieldAccess(
            opcode = Opcode.IGET,
            type = "I",
            location = MatchAfterWithin(5)
        ),
        methodCall(
            opcode = Opcode.INVOKE_STATIC,
            returnType = "L",
            parameters = listOf("I"),
            location = MatchAfterWithin(5)
        ),
        opcode(
            opcode = Opcode.MOVE_RESULT_OBJECT,
            location = MatchAfterImmediately()
        ),

        // Navigation button visibility.
        methodCall(name = "getVisibility")
    )
)

/** The static icon-type -> enum lookup called by [TabLayoutTextFingerprint]. */
val navigationEnumLookupMethod = findMethodDirect {
    TabLayoutTextFingerprint.instructionMatches[6].instruction.methodRef!!
}

/** The `getVisibility()` call Morphe injects before: its receiver is the tab view. */
val tabGetVisibilityMethod = findMethodDirect {
    TabLayoutTextFingerprint.instructionMatches.last().instruction.methodRef!!
}
