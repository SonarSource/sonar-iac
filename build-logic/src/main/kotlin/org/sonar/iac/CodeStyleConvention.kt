package org.sonar.iac

import com.diffplug.gradle.spotless.SpotlessExtension

interface CodeStyleConvention {
    var spotless: (SpotlessExtension.() -> Unit)?
    fun spotless(action: SpotlessExtension.() -> Unit) {
        spotless = action
    }
}
