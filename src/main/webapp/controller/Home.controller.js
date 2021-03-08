sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController"
], function(BaseController) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.Home", {

		onInit : function() {
			BaseController.prototype.onInit.call(this);
			this.getRouter().getRoute("Home").attachPatternMatched(this._handleRouteMatched, this);
		},

		_handleRouteMatched : function() {
			this.getRouter().navTo("Battle");
		}

	});
});