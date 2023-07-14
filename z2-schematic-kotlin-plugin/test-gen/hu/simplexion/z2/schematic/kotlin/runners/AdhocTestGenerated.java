

package hu.simplexion.z2.schematic.kotlin.runners;

import com.intellij.testFramework.TestDataPath;
import org.jetbrains.kotlin.test.TargetBackend;
import org.jetbrains.kotlin.test.TestMetadata;
import org.jetbrains.kotlin.test.util.KtTestUtil;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.regex.Pattern;

/** This class is generated by {@link hu.simplexion.z2.schematic.kotlin.GenerateTestsKt}. DO NOT MODIFY MANUALLY */
@SuppressWarnings("all")
@TestMetadata("testData/adhoc")
@TestDataPath("$PROJECT_ROOT")
public class AdhocTestGenerated extends AbstractAdhocTest {
    @Test
    @TestMetadata("adhoc.kt")
    public void testAdhoc() throws Exception {
        runTest("testData/adhoc/adhoc.kt");
    }

    @Test
    public void testAllFilesPresentInAdhoc() throws Exception {
        KtTestUtil.assertAllTestsPresentByMetadataWithExcluded(this.getClass(), new File("testData/adhoc"), Pattern.compile("^(.+)\\.kt$"), null, TargetBackend.JVM_IR, true);
    }
}
