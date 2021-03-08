sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data"
], function (BaseController, JSONModel, Data) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.gestion.PageDetailController", {
		onInit: function (sClassName, sFetch) {
			BaseController.prototype.onInit.call(this);
			this.sClassName = sClassName;
			this.sFetch = sFetch;

			this.getRouter().getRoute(this.sClassName + "Detail").attachPatternMatched(this._handleRouteMatched, this);
		},

		_handleRouteMatched: function (oEvent) {
			var id = oEvent.getParameter("arguments").id;

			if (id === "create") {
				this.setModel(this.newObject());
				this.onRouteMatch();
			} else {
				this._load(oEvent.getParameter("arguments").id);
			}
		},
		
		_load : function (id) {
			var oParam = {
				"className": this.sClassName,
				"key": id
			};
			if (this.sFetch) {
				oParam.fetch = this.sFetch;
			}
			Data.find(this, oParam).then(function (oData) {
				this.onLoad(oData);
			}.bind(this)).then(function () {
				this.onRouteMatch();
			}.bind(this));
		},

		/**
		 * A surcharger pour la création d'un nouvel objet
		 * @return {sap.ui.model.json.JSONModel} le nouvel objet
		 */

		newObject: function () {
			return new JSONModel({});
		},

		/**
		 * A surcharger pour le chargement
		 */
		onRouteMatch: function () {},

		/**
		 * Méthode à surcharger pour faire des actions après le chargement de données
		 * 
		 * @param {sap.ui.model.json.JSONModel} oData L'objet Json chargé
		 * @return {sap.ui.model.json.JSONModel} les données
		 */
		onLoad: function (oData) {
			return oData;
		},

		onValidate: function ( /* oEvent */ ) {
			if (this.hasError()) {
				this.messageError(this.getI18n().getText("common.correct_error"));
				return;
			}

			// On force à null pour le mapping
			Data.removeEmptyAttributs(this.getModel());

			this.save();
		},

		save: function () {
			Data.save(this.sClassName, this.getModel().getProperty("/")).then(function () {
				this.messageToast(this.getI18n().getText("common.save_success"));
				this.onNavBack();
			}.bind(this));
		},

		onReject: function ( /* oEvent */ ) {
			this.onNavBack();
		},
		
		onRefresh: function ( /* oEvent */ ) {
			this._load(this.getModel().getProperty("/id"));
		}

	});
});