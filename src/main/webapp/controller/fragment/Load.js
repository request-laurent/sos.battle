/*jshint esversion: 6 */
sap.ui.define([
	"com/sos/ui5/battle/controller/fragment/BaseFragment",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data"
], function(BaseFragment, JSONModel, Data) {
	"use strict";
	return BaseFragment.extend("com.sos.ui5.battle.controller.fragment.Load", {
		onInit : function() {
			this.setModel(new JSONModel({
				"id" : null
			}), "save");
			Data.getJSONModel(this.getController(), "rest/battle/templateList", null, "tmp").then(this.trad.bind(this));
		},

		onValidate : function() {
			var id = this.getModel("save").getProperty("/id");
			if (id) {
				this.close(Data.getLine(this.getModel("templates").getProperty("/"), "id", id));
			}
		},

		onReject : function() {
			this.close();
		},

		trad : function(oData) {
			var tab = oData.getProperty("/");
			var sos = this.getController().getOwnerComponent().getModel("sos").getResourceBundle();

			tab.forEach(function(o) {
				if (sos.hasText(o.name)) {
					o.name = (o.type === "human" ? "*" : "") + sos.getText(o.name);
				}
			}.bind(this));
			this.setModel(new JSONModel(tab), "templates");
		},

	});
});