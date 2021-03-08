sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageBox"
], function(BaseController, JSONModel, MessageBox) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.admin.GroupsDetail", {
		onInit : function() {
			BaseController.prototype.onInit.call(this);
			
			this.getRouter().getRoute("GroupsDetail").attachPatternMatched(this._handleRouteMatched, this);
			sap.ui.getCore().getMessageManager().registerObject(this.getView(), true);
		},
		
		_handleRouteMatched : function(oEvent) {
			var id = oEvent.getParameter("arguments").id;

			if (id === "create") {
				id = null;
			}
			
			jQuery.ajax({
				context : this,
				type : "GET",
				url : "rest/security/findGroup",
				data : {
					"id" : id
				},
				contentType : "application/json; charset=utf-8",
				success : function(data) {
					this.setModel(new JSONModel(data));

					var oTable = this.byId("table");

					for (var i = 0; i < data.roles.length; i++) {
						if (data.roles[i].selected) {
							oTable.addSelectionInterval(i, i);
						}
					}
				},
				error : function(xhr, status, error) {
					MessageBox.error(error);
				}
			});

		},

		onValidate : function(/* oEvent */) {
			if (this.hasError()) {
				this.messageError(this.getI18n().getText("common.correct_error"));
				return;
			}

			var oData = this.getModel().getProperty("/");
			oData.roles.forEach(function(role) {
				role.selected = false;
			});

			this.byId("table").getSelectedIndices().forEach(function(i) {
				oData.roles[i].selected = true;
			});

			jQuery.ajax({
				context : this,
				type : "POST",
				url : "rest/security/saveGroup",
				data : JSON.stringify(oData),
				contentType : "application/json; charset=utf-8",
				success : function() {
					this.onNavBack();
				},
				error : function(xhr, status, error) {
					MessageBox.error(error);
				}
			});
		},

		onReject : function(/* oEvent */) {
			this.onNavBack();
		}
	});
});