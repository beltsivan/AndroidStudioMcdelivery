I am having errors in android studio kotlin version iguana but the app is still running but it does have errors saying unresolved reference and at the top it says some kotlin runtime libraries has an unsupported binary format. and when I clicked downgrade all kotlin runtime libraries it will prompt to this: Automatic library version update for Maven and Gradle projects is currently unsupported. Please update your build scripts manually



when I open build.gradle.kts it says Cannot access script base class 'org.gradle.kotlin.dsl.KotlinBuildScript'. Check your module classpath for missing or conflicting dependencies

files contains error:
these files are in com.example.mcdeliveryapp
Adapter.kt
BagActivity
BagAdapter
CartManager
CouponsActivity
ImageUtils.kt
LoginActivity
MainActivity
MenuActivity
MenuSectionAdapter.kt
OrderDetailsActivity
OrdersActivity
ProfileActivity
SignUpActivity

under the app file:
build.gradle.kts

under the AndroidStudioMcdeliverry [Mcdelivery App]
build.gradle.kts

some errors in those files are saying "Unresolved reference:"