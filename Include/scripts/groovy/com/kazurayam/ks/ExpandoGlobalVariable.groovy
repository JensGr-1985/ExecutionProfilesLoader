package com.kazurayam.ks

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import com.kazurayam.ks.ExpandoGlobalVariable as EGV
import com.kms.katalon.core.configuration.RunConfiguration

import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import internal.GlobalVariable

@RunWith(JUnit4.class)
public class ExpandoGlobalVariableTest {

	private static Path json
	private static String FILENAME = "MyExecutionProfile.json"

	@BeforeClass
	static void setupClass() {
		Path projectDir = Paths.get(RunConfiguration.getProjectDir())
		Path testOutputDir = projectDir.resolve("build/tmp/testOutput")
		Path pkgDir = testOutputDir.resolve("com.kazurayam.visualtesting")
		Path classDir = pkgDir.resolve(ExpandoGlobalVariableTest.class.getSimpleName())
		if (!Files.exists(classDir)) {
			Files.createDirectories(classDir)
		}
		json = classDir.resolve(FILENAME)
	}

	/**
	 * listGlobalVaraibles() should return a List<String> of GlobalVariable names
	 * defined in the current context.
	 * Here we assume that the "default" Execution Profile is selected where FOO=BAR is defined
	 */
	@Test
	void test_listGlobalVariables() {
		List<String> names = EGV.listGlobalVariables()
		//names.each { String name -> println name }
		assertTrue("expected 1 or more GlobalVaraiable(s) defined, but not found", names.size() > 0)
	}

	/**
	 * We will add a new GlobalVariable "NEW=VALUE" dynamically in the current context.
	 * Here we assume that the "default" Execution Profile is selected.
	 * So we expect to find 2 GlobalVariables: FOO=BAR and NEW=VALUE
	 */
	@Test
	void test_listGlobalVariablesWithAdditive() {
		EGV.addGlobalVariable("NEW", "VALUE")
		println "keySet: " + EGV.additionalProperties.keySet()
		List<String> names = EGV.listGlobalVariables()
		assertTrue("names does not contain FOO", names.contains('FOO'))
		assertTrue("names does not contain NEW", names.contains("NEW"))
		assertTrue(names.size() >= 2)
	}

	@Test
	void test_isGlobalVariablePresent_negative() {
		assertFalse(EGV.isGlobalVariablePresent("THERE_IS_NO_SUCH_VARIABLE"))
	}

	/**
	 * assert that a GlobalVariable.SETTABLE is created on the fly
	 */
	@Test
	void test_addedGlobalVariableShouldImplementSetter() {
		EGV.addGlobalVariable("SETTABLE", "not yet modified")
		GlobalVariable.SETTABLE = "Hello, world"
		assertEquals("Hello, world", GlobalVariable.SETTABLE)
	}

	@Test
	void test_basic_operations() {
		String name = "foo"
		Object value = "value"
		EGV.ensureGlobalVariable(name, value)
		assertTrue("GlobalVariable.${name} is not present", EGV.isGlobalVariablePresent(name))
		Object obj = EGV.getGlobalVariableValue(name)
		assertNotNull("GVH.getGlobalVariableValue('${name}') returned null", obj)
		assertTrue(obj instanceof String)
		assertEquals((String)value, (String)obj)
	}

	@Test
	void test_write_read() {
		// setup
		String gvName = 'CUSTOMLY_CREATED_GLOBALVARIABLE'
		Object value = "The Hill We Climb"
		EGV.ensureGlobalVariable(gvName, value)
		// when:
		Writer writer = new OutputStreamWriter(new FileOutputStream(json.toFile()),"utf-8")
		EGV.write([gvName], writer)
		// then
		assertTrue(json.toFile().length() > 0)

		// OK, next
		Reader reader = new InputStreamReader(new FileInputStream(json.toFile()),"utf-8")
		Map<String, Object> loaded = EGV.read([gvName], reader)
		assertTrue(loaded.containsKey(gvName))
		assertEquals(value, loaded.get(gvName))
		//println "value read from file: name=${gvName}, value=${loaded.get(gvName)}"
	}
}