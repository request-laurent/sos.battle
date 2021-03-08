sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController"
], function(BaseController) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.NotFound", {
		onInit : function() {
			BaseController.prototype.onInit.call(this);
		}
	});
});