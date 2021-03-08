sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"com/sos/ui5/battle/util/Data",
	"sap/ui/model/json/JSONModel"
], function (BaseController, Data, JSONModel) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.admin.StatisticErreur", {
		onInit: function () {
			BaseController.prototype.onInit.call(this);
			this.getRouter().getRoute("StatisticErreur").attachPatternMatched(this._handleRouteMatched, this);

			this.setModel(new JSONModel({}), "stack");
			this.getModel("stack").setProperty("/", "");
		},

		_handleRouteMatched: function (oEvent) {
			var oTable = this.byId("table");
			Data.setJSONModel(this, "rest/statistics/stack", "GET", {
				"code": decodeURIComponent(oEvent.getParameter("arguments").code)
			}).then(function (oData) {
				if (oData.getProperty("/").length > 0) {
					//this.getModel("stack").setProperty("/", oData.getProperty("/")[0].stack);
					oTable.setSelectedIndex(0);
				}
			});
		},

		/**
		 * Selection de la ligne
		 * 
		 * @param {sap.ui.base.Event} oEvent - Evenement sur la ligne
		 */
		onSelect: function (oEvent) {
			this.getModel("stack").setProperty("/", "<pre>" + oEvent.getParameters().rowContext.getObject("stack") + "</pre>");
		}
	});
});