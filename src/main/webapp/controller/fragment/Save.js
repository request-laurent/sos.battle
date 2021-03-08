/*jshint esversion: 6 */
sap.ui.define([
	"com/sos/ui5/battle/controller/fragment/BaseFragment",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data",
	"sap/ui/model/Filter"
], function(BaseFragment, JSONModel, Data, Filter) {
	"use strict";
	return BaseFragment.extend("com.sos.ui5.battle.controller.fragment.Save", {
		onInit : function() {
			this.setModel(new JSONModel({
				"name" : null
			}), "save");
			Data.getJSONModel(this.getController(), "rest/battle/templateList", null, "tmp").then(this.filter.bind(this));
		},

		onValidate : function() {
			var name = this.getModel("save").getProperty("/name");
			if (name) {
				this.close(name);
			}
		},

		onReject : function() {
			this.close();
		},

		filter : function(oData) {
			var tab = oData.getProperty("/");

			tab = oData.getProperty("/").filter(function(o) {
				return (o.name === "event_showdown_tutorial_player_nametag");
			});

			this.setModel(new JSONModel(tab), "templates");
		},
		
		handleSuggest : function(oEvent) {
			var sTerm = oEvent.getParameter("suggestValue");
			var aFilters = [];
			if (sTerm) {
				aFilters.push(new Filter("lvl", sap.ui.model.FilterOperator.Contains, sTerm));
			}
			oEvent.getSource().getBinding("suggestionItems").filter(aFilters);
		},


	});
});