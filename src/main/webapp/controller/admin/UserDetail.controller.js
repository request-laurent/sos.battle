sap.ui.define([
	"com/sos/ui5/battle/controller/PageDetailController",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data",
	"sap/m/MessageBox"
], function(PageDetailController, JSONModel, Data, MessageBox) {
	"use strict";
	return PageDetailController.extend("com.sos.ui5.battle.controller.admin.UserDetail", {
		onInit : function() {
			PageDetailController.prototype.onInit.call(this, "User", "left join fetch o.group");
		},

		onRouteMatch : function() {
			// Lecture des Groupes
			Data.selects(this, {
				"group" : {
					"className" : "Groups",
					"orderBy" : "o.name"
				}
			});
		},

		onLoad : function(oData) {
			Data.addEmptyObjects(oData, [
				"/group"
			]);
		},

		newObject : function() {
			return new JSONModel({
				"group" : {}
			});
		},
		
		save : function() {
			jQuery.ajax({
				context : this,
				type : "POST",
				url : "rest/security/saveUser",
				data : JSON.stringify(this.getModel().getProperty("/")),
				contentType : "application/json; charset=utf-8",
				success : function() {
					this.messageToast(this.getI18n().getText("common.save_success"));
					this.onNavBack();
				},
				error : function(xhr, status, error) {
					MessageBox.error(error);
				}
			});
		}

	});
});