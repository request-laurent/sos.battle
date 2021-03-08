sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"com/sos/ui5/battle/util/Data",
	"sap/m/MessageBox"
], function (BaseController, Data, MessageBox) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.PageListController", {
		onInit: function (sClassName, sOrderBy, sFetch) {
			BaseController.prototype.onInit.call(this);
			this.sClassName = sClassName;
			this.sOrderBy = sOrderBy;
			this.sFetch = sFetch;

			this.getRouter().getRoute(this.sClassName + "List").attachPatternMatched(this._handleRouteMatched, this);
		},

		_handleRouteMatched: function ( /* oEvent */ ) {
			this._loadData();
		},

		_loadData: function ( /* oEvent */ ) {
			var oParam = {
				"className": this.sClassName
			};
			if (this.sOrderBy) {
				oParam.orderBy = this.sOrderBy;
			}
			if (this.sFetch) {
				oParam.fetch = this.sFetch;
			}
			Data.select(this, oParam);
		},

		/**
		 * Selection de la ligne
		 * 
		 * @param {sap.ui.base.Event} oEvent - Evenement sur la ligne
		 */
		onSelect: function (oEvent) {
			var id = oEvent.getParameters().rowContext === undefined ? oEvent.getSource().getBindingContext().getProperty("id") : oEvent.getParameters()
				.rowContext.getObject("id");

			this.getRouter().navTo(this.sClassName + "Detail", {
				"id": id
			});
		},

		/**
		 * Suppression de la ligne
		 * 
		 * @param {sap.ui.base.Event} oEvent - Evenement sur la ligne
		 */
		onDelete: function (oEvent) {
			var id = oEvent.getSource().getBindingContext().getProperty("id");

			MessageBox.warning(this.getI18n().getText("common.delete_confirm", [
				oEvent.getSource().getBindingContext().getProperty("name")
			]), {
				actions: [
					sap.m.MessageBox.Action.OK,
					sap.m.MessageBox.Action.CANCEL
				],
				onClose: function (sAction) {
					if (sAction === "OK") {
						Data.remove(this.sClassName, id).then(function () {
							this.messageToast(this.getI18n().getText("common.delete_success"));
							this._loadData();
						}.bind(this));
					}
				}.bind(this)
			});
		},

		onAdd: function ( /* oEvent */ ) {
			this.getRouter().navTo(this.sClassName + "Detail", {
				"id": "create"
			});
		},

	});
});