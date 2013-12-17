/*
 *  Copyright 1995-2013 Caplin Systems Ltd. Do not edit this file; edits will be lost after upgrades.
 */

if(!window.caplin)
{
	window.caplin = {};
}

caplin._deprecatedClassOrMethodWarningHistory = {};

var br = require('br');

caplin.extend = function(subClass, superClass) {
	// see if the base classes prototype is currently empty
	var isFirstInheritance = true;
	for(var attribute in subClass.prototype) {
		isFirstInheritance = false;
		break;
	}

	if (isFirstInheritance) {
		return br.Core.extend(subClass, superClass);
	}

	return br.Core.inherit(subClass, superClass);
};

/**
 * Extends the class definition of <code>fClass</code> with the interface defined in
 * <code>fInterface</code>.
 *
 * @param {Function} fClass The class that is implementing the interface.
 * @param {Function} fInterface The interface that the class must implement.
 */
caplin.implement = function(fClass, fInterface) {
	for (var attribute in fInterface.prototype) {
		if (fClass.prototype[attribute] === undefined) {
			fClass.prototype[attribute] = fInterface.prototype[attribute];
		}
	}
	br.Core.implement(fClass, fInterface);
};

/**
 * Checks that an object instance implements all the public functions of the class (i.e. a duck-typing check).  NB: does not check the entire prototype chain.
 *
 * @param {Object} oInstance An object instance that we are checking.
 * @param {Function} fClass The class or interface we are checking against.
 *
 * @return {boolean} true if the instance is an instance of the class constructed by the function.
 *
 * @throws Error if fClass is not a function.
 */
caplin.isImplementorOf = br.fulfills;

/**
 * Invoked by API methods that have been deprecated. If the unary parameter <code>deprecated</code>
 * is included within the query string of the application web page any invocations of this
 * method will result in an error message being displayed, making it easier to find out which
 * deprecated methods are being used by the application.
 *
 * Remember that we want to be able to pull uses of this method, including the classname, the method
 * name and the date out of our code base using a regex.
 *
 * @param {String} sClassName The name of the class that has been deprecated or the class that the
 *			deprecated method belongs to.
 * @param {String} sMethodName The name of the method that is deprecated. null if the whole class
 *			or constructor has been deprecated.
 * @param {String} sDate The date that this method was deprecated in YYYY-MM-DD format, e.g. "2012-03-20"
 * @param {String} sMessage A message describing the deprecation.  This should explain how code that uses this method should be rewritten.
 */
caplin.deprecated = function(sClassName, sMethodName, sDate, sMessage)
{
	if(this._isDeprecatedEnabled(window.location.search))
	{
		var sKey = sMethodName == null ? sClassName : sClassName + "#" + sMethodName;
		if(this._deprecatedClassOrMethodWarningHistory[sKey] === undefined)
		{
			var sError = sMethodName == null ? 'The class "' + sClassName + '" includes a deprecation warning.' : 'The "' + sMethodName + '()" method on the class "' + sClassName + '" includes a deprecation warning.';

			if(sMessage) {
				sError += " " + sMessage;
			}

			this._logError(sError);
			this._deprecatedClassOrMethodWarningHistory[sKey] = true;
		}
	}
};

/**
 * Determines whether the <code>deprecated</code> parameter has been included within the specified
 * query string or not.
 * @see #deprecated
 * @private
 */
caplin._isDeprecatedEnabled = function(sQueryString)
{
	return (sQueryString.match(/[?&]deprecated([&=]|$)/) !== null);
};

/**
 * @private
 */
caplin._logError = function(sErrorMessage)
{
	if(window.console && console.error)
	{
		console.error(sErrorMessage);
	}
	else
	{
		window.alert(sErrorMessage);
	}
};

/**
 * Invoked by API methods that are no longer supported.
 *
 * @param {String} sClassName The name of the class that is no longer supported or the class that the
 *			unsupported method belongs to.
 * @param {String} sMethodName The name of the method that is no longer supported (Optional: not needed for
 *			constructors).
 */
caplin.unsupported = function(sClassName, sMethodName)
{
	if(!sMethodName)
	{
		throw new Error("The " + sClassName + " class is no longer supported.");
	}
	else
	{
		throw new Error("The " + sClassName + "." + sMethodName + "() method is no longer supported.");
	}
};

caplin.thirdparty = function(sClassName)
{
	// TODO: remove this method completely as it was only introduced recently -- we're going to use pragma comments once
	// js-bundler comment stripping has been disabled (PCTCUT-???)
};

caplin.namespace = function(sPackage)
{
	var pPackageElements = sPackage.split(".");
	var oPackageObject = window;

	for(var n = 0, nLength = pPackageElements.length; n < nLength; n++)
	{
		var sNextPackageElement = pPackageElements[n];

		if (sNextPackageElement == "")
		{
			if(n == nLength - 1)
			{
				continue;
			}
			else
			{
				throw new Error("invalid package argument: " + sPackage);
			}
		}

		if(!oPackageObject[sNextPackageElement])
		{
			oPackageObject[sNextPackageElement] = {};
		}

		oPackageObject = oPackageObject[sNextPackageElement];
	}
};

caplin.include = function(sClassName, bBefore)
{
	caplin.deprecated("caplin", "include", "2012-08-22", "The bundler makes this method unnecessary." );
};

caplin.forceload = function(sClassName)
{
	caplin.deprecated("caplin", "forceload", "2012-08-22", "The bundler makes this method unnecessary." );
};

caplin.singleton = function()
{
	caplin.unsupported("caplin", "singleton");
};

caplin.notifyAfterClassLoad = function()
{
	caplin.unsupported("caplin", "notifyAfterClassLoad");
};

caplin.onLoad = function()
{
	caplin.unsupported("caplin", "onLoad");
};

caplin.addClassPath = function(sClassName, sPath)
{
	caplin.unsupported("caplin", "addClassPath");
};

caplin.loadScript = function(sScriptPath, bSynchronous, sClassName)
{
	caplin.unsupported("caplin", "loadScript");
};

caplin.getClass = function(sClassName)
{
	caplin.deprecated("caplin", "getClass", "2012-08-22", "Use caplin.core.ClassUtility.getClass instead");

	// TODO: implement this method
	return caplin.core.ClassUtility.getClass(sClassName);
};

caplin.getPackage = function(sPackageName)
{
	caplin.deprecated("caplin", "getPackage", "2012-08-22", "Use caplin.core.ClassUtility.getPackage instead");
	return caplin.core.ClassUtility.getPackage(sPackageName);
};

caplin.getObject = function(sObjectName)
{
	caplin.deprecated("caplin", "getObject", "2012-08-22", "Use caplin.core.ClassUtility.getPackage instead");
	return caplin.core.ClassUtility.getPackage(sObjectName);
};

caplin.resolve = function(sFullName, pGlobals)
{
	caplin.unsupported("caplin", "resolve");
};

caplin.getFileContents = function(sFileUrl)
{
	caplin.unsupported("caplin", "getFileContents");
};

caplin.getResourceContents = function(sPackageName, sResourceName)
{
	caplin.unsupported("caplin", "getResourceContents");
};

caplin.getResourceUrl = function(sPackageName, sResourceName)
{
	caplin.unsupported("caplin", "getResourceUrl");
};

caplin.getModuleResourceUrl = function(sPackage, sResource)
{
	caplin.unsupported("caplin", "getModuleResourceUrl");
};

caplin.getPublicResourceUrl = function(sResource)
{
	caplin.unsupported("caplin", "getPublicResourceUrl");
};

caplin.isFunctionOnIEWindowOpener = function (fFunction)
{
	caplin.unsupported("caplin", "isFunctionOnIEWindowOpener");
};

/**
 * Attaches properties to a class's prototype.
 *
 * @private
 */
caplin.defineProperties = function(classConstructor, descriptorObject)
{
	Object.defineProperties(classConstructor.prototype, caplin.createDescriptor(descriptorObject));
};

/**
 * Creates a descriptor object that can be used by the Object.defineProperties/defineProperty/create
 * methods. These methods are used to create classes and/or to do inheritance in JS.
 *
 * @private
 */
caplin.createDescriptor = function(descriptorObject)
{
	var result = {};

	for (var descriptor in descriptorObject)
	{
		result[descriptor] = {
			value: descriptorObject[descriptor],
			enumerable: true
		};
	}

	return result;
};

/**
 * Returns a constructor if the class has already been loaded, else returns a proxy constructor
 * which when called with keyword 'new' will create the required class and pass it back.
 *
 * Will not work for static class methods, can only be used for object construction.
 *
 * @private
 */
caplin.require = function(requiredClass)
{
	try
	{
		return caplin.core.ClassUtility.getClass(requiredClass);
	}
	catch(e)
	{
		return this._getConstructorProxy(requiredClass);
	}
};

/**
 * @private
 */
caplin._getConstructorProxy = function(requiredClass)
{
	return ProxyConstructor;

	function ProxyConstructor() {
		var Constructor = caplin.core.ClassUtility.getClass(requiredClass);

		function Intermediary(args) {
			return Constructor.apply(this, args);
		}
		Intermediary.prototype = Constructor.prototype;

		return new Intermediary(arguments);
	}
};
