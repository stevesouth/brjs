var Errors = require('br/Errors');

/**
 * @name br.services.BundlePathService
 * @constructor
 * @class
 * @interface
 * This class provides access to the bundle path which is automatically generated by BladeRunnerJS
 */
function BundlePathService() {};

/**
 * Returns the version for the App
 */
BundlePathService.prototype.getBundlePath = function(bundlePath) {
	throw new Errors.UnimplementedInterfaceError("BundlePathService.getBundlePath(bundlePath) has not been implemented.");
};

module.exports = BundlePathService;
