sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"com/sos/ui5/battle/util/Data"
], function(BaseController, Data) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.admin.Statistic", {
		onInit : function() {
			BaseController.prototype.onInit.call(this);

			this.getRouter().getRoute("Statistic").attachPatternMatched(this._handleRouteMatched, this);
		},

		_handleRouteMatched : function(/* oEvent */) {
			this._loadData();
		},

		_loadData : function(/* oEvent */) {
			Data.setJSONModel(this, "rest/statistics/all", "GET").then(function(oData) {
				oData.getProperty("/").forEach(function(o) {
					o.temps = o.dureeTotal / o.nombre;
				});
				oData.refresh();
			});
		},

		/**
		 * Selection de la ligne
		 * 
		 * @param {sap.ui.base.Event} oEvent - Evenement sur la ligne
		 */
		onSelect : function(oEvent) {
			var code = oEvent.getParameters().rowContext === undefined ? oEvent.getSource().getBindingContext().getProperty("code") : oEvent.getParameters().rowContext.getObject("code");

			this.getRouter().navTo("StatisticErreur", {
				"code" : encodeURIComponent(code)
			});
		},

		onRefresh : function(/* oEvent */) {
			this._loadData();
		},
		
		onReset : function(/* oEvent */) {
			Data.call(this, "rest/statistics/clear", "GET", {}).then(function(/*oData*/) {
				this._loadData();
			}.bind(this));
		}
	});
});