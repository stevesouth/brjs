package org.bladerunnerjs.spec.observer;

import org.bladerunnerjs.plugin.plugins.jsdoc.JsDocObserver;
import org.bladerunnerjs.testing.specutility.engine.SpecTest;
import org.junit.Test;

public class JsDocModelObserverTest extends SpecTest {
	
	@Test 
	public void placeholdersAreCreatedWhenANewAppIsCreated() throws Exception {
		given(testSdkDirectory).containsFileWithContents("apps/app1/src/MyClass.js", "// my class")
			.and(brjs).hasModelObserverPlugins(new JsDocObserver())
			.and(testSdkDirectory).containsFileWithContents("sdk/jsdoc-toolkit-resources/jsdoc-placeholders/index.html", "PLACEHOLDER");
		when(brjs).hasBeenCreated();
		then(brjs).hasDir("generated/app/app1/jsdoc")
			.and(brjs.app("app1").storageDir("jsdoc")).containsFileWithContents("index.html", "PLACEHOLDER");
	}
	
}
