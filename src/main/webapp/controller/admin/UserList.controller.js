sap.ui.define([
	"com/sos/ui5/battle/controller/PageListController"
], function(PageListController) {
	"use strict";
	return PageListController.extend("com.sos.ui5.battle.controller.admin.UserList", {
		onInit : function() {
			PageListController.prototype.onInit.call(this, "User", "o.name", "inner join fetch o.group");
		}
	});
});