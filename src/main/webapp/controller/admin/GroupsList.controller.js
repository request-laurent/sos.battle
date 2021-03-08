sap.ui.define([
	"com/sos/ui5/battle/controller/PageListController"
], function(PageListController) {
	"use strict";
	return PageListController.extend("com.sos.ui5.battle.controller.admin.GroupsList", {
		onInit : function() {
			PageListController.prototype.onInit.call(this, "Groups", "o.name");
		}
	});
});