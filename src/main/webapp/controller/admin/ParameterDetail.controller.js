sap.ui.define([
	"com/sos/ui5/battle/controller/PageDetailController"
], function(PageDetailController) {
	"use strict";
	return PageDetailController.extend("com.sos.ui5.battle.controller.sos.ParameterDetail", {
		onInit : function() {
			PageDetailController.prototype.onInit.call(this, "Parameter");
		}
	});
});