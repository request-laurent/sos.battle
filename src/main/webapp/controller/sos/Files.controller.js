sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data"
], function(BaseController, JSONModel, Data) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.sos.Files", {
		onInit : function() {
			BaseController.prototype.onInit.call(this);

			this.getRouter().getRoute("Files").attachPatternMatched(this._handleRouteMatched, this);
		},

		_handleRouteMatched : function() {
			Data.getJSONModel(this, "rest/files/config", null).then(function() {
				this.getModel().setProperty("/config_version2", this.getModel().getProperty("/config_version")*1000);
			}.bind(this));
		},

		onJson : function() {
			sap.m.URLHelper.redirect("rest/files/json?"+jQuery.param({
				"config_version" : this.getModel().getProperty("/config_version"),
			}), true);
		},

		onBundle : function() {
			sap.m.URLHelper.redirect("rest/files/bundle?"+jQuery.param({
				"bundle_version_r" : this.getModel().getProperty("/bundle_version_r"),
			}), true);
		},

		onLang : function() {
			sap.m.URLHelper.redirect("rest/files/lang?"+jQuery.param({
				"game_version" : this.getModel().getProperty("/game_version"),
			}), true);
		},
	});
});