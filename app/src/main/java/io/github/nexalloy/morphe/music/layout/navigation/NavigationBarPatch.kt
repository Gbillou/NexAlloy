package io.github.nexalloy.morphe.music.layout.navigation

import android.view.View
import android.widget.TextView
import app.morphe.extension.music.patches.NavigationBarPatch
import io.github.nexalloy.morphe.music.misc.settings.PreferenceScreen
import io.github.nexalloy.morphe.shared.misc.settings.preference.PreferenceScreenPreference
import io.github.nexalloy.morphe.shared.misc.settings.preference.PreferenceScreenPreference.Sorting
import io.github.nexalloy.morphe.shared.misc.settings.preference.SwitchPreference
import io.github.nexalloy.patch
import io.github.nexalloy.scopedHook
import org.luckypray.dexkit.wrap.DexMethod

/**
 * Port of Morphe's YT Music "Navigation bar" patch. Replaces the old "Hide upgrade button",
 * which removed pivot item index 4 and broke on 9.42+ (Samples tab gone, 4 items). Buttons
 * are identified by their icon enum (TAB_MUSIC_PREMIUM etc.), not by position.
 *
 * Morphe injects three calls inside [TabLayoutTextFingerprint]; here each becomes a scoped
 * hook on the callee, active only while that method runs.
 */
val NavigationBar = patch(
    name = "Navigation bar",
    description = "Adds options to hide navigation bar, labels and buttons.",
) {
    PreferenceScreen.GENERAL.addPreferences(
        PreferenceScreenPreference(
            key = "morphe_music_navigation_bar_screen",
            sorting = Sorting.UNSORTED,
            preferences = setOf(
                SwitchPreference("morphe_music_hide_navigation_bar_home_button"),
                SwitchPreference("morphe_music_hide_navigation_bar_samples_button"),
                SwitchPreference("morphe_music_hide_navigation_bar_explore_button"),
                SwitchPreference("morphe_music_hide_navigation_bar_library_button"),
                SwitchPreference("morphe_music_hide_navigation_bar_upgrade_button"),
                SwitchPreference("morphe_music_hide_navigation_bar"),
                SwitchPreference("morphe_music_hide_navigation_bar_labels"),
            )
        )
    )

    val text1 = text1Id
    TabLayoutTextFingerprint.hookMethod(
        scopedHook(
            ::navigationEnumLookupMethod.member to {
                after { NavigationBarPatch.setLastAppNavigationEnum(it.result as? Enum<*>) }
            },
            ::tabGetVisibilityMethod.member to {
                before {
                    if (innerDepth == 0) NavigationBarPatch.hideNavigationButton(it.thisObject as View)
                }
            },
            DexMethod("Landroid/view/View;->findViewById(I)Landroid/view/View;").toMember() to {
                after {
                    if (innerDepth == 0 && it.args[0] == text1) {
                        (it.result as? TextView)?.let(NavigationBarPatch::hideNavigationLabel)
                    }
                }
            },
        )
    )
}
