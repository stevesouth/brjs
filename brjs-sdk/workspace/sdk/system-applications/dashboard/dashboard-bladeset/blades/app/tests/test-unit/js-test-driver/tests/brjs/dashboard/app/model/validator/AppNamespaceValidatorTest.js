AppNamespaceValidatorTest = TestCase("AppNamespaceValidatorTest");

AppNamespaceValidatorTest.prototype.setUp = function()
{
	this.m_oValidator = new brjs.dashboard.app.model.dialog.validator.AppNamespaceValidator();
};

AppNamespaceValidatorTest.prototype.isValid = function(sNamespace)
{
	var oValidationResult = new br.presenter.validator.ValidationResult();
	this.m_oValidator.validate(sNamespace, {}, oValidationResult);
	
	return oValidationResult.isValid();
};

AppNamespaceValidatorTest.prototype.testNamespacesCanOnlyBeLowerCaseAlphanumsWhereFirstCharacterCantBeNumeric = function()
{
	assertFalse("1a", this.isValid("A"));
	assertFalse("1b", this.isValid("aBc"));
	assertFalse("1c", this.isValid("1a"));
	
	assertTrue("2a", this.isValid("a"));
	assertTrue("2b", this.isValid("abc"));
	assertTrue("2c", this.isValid("a1"));
};

AppNamespaceValidatorTest.prototype.testCaplinAndbrjsAreReservedNames = function()
{
	assertFalse("1a", this.isValid("caplin"));
	assertFalse("1b", this.isValid("brjs"));
	
	assertTrue("2a", this.isValid("caplinz"));
	assertTrue("2b", this.isValid("brjsyz"));
};

AppNamespaceValidatorTest.prototype.testThatEmptyStringIsAValidNamespace = function()
{
	assertTrue("1a", this.isValid(""));
};

AppNamespaceValidatorTest.prototype.testStringWithSpacesIsInvalid = function()
{
	assertFalse("1a", this.isValid("bla bla"));
};

