sap.ui.define([
	"com/sos/ui5/battle/controller/PageListController"
], function(PageListController) {
	"use strict";
	return PageListController.extend("com.sos.ui5.battle.controller.admin.ParameterList", {
		onInit : function() {
			PageListController.prototype.onInit.call(this, "Parameter");
		},

		onRefresh : function(/* oEvent */) {
			jQuery.ajax({
				context : this,
				type : "GET",
				url : "rest/parameter/refresh",
				success : function(/* data */) {
					this._loadData();
				},
				error : function(xhr, status, error) {
					this.messageError(error);
				}
			});
		}
	});
});