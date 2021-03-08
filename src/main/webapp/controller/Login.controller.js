sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"sap/ui/model/resource/ResourceModel",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageBox"
], function (BaseController, ResourceModel, JSONModel, MessageBox) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.Login", {

		onInit: function () {
			BaseController.prototype.onInit.call(this);

			this.setModel(new JSONModel({}));

			// Forcage du modele i18n
			var i18nModel = new ResourceModel({
				bundleName: "com.sos.ui5.battle.i18n.i18n"
			});
			this.getView().setModel(i18nModel, "i18n");
		},

		onAfterRendering: function () {
			this.byId("dialog").open();
		},

		onOpenId: function () {
			// Récupération de l'url du navigateur pour fabriquer
			// l'url de redirection
			var sRedirectUri = encodeURIComponent(location.href.split("/").splice(0, 4).join("/") + "/rest/discord");
			/*eslint sap-no-hardcoded-url: 0*/
			document.location.href =
				"https://discord.com/api/oauth2/authorize?client_id=773858599197343765&redirect_uri="+sRedirectUri+"&response_type=code&scope=identify" ;
		},

		onLogin: function () {
			jQuery.ajax({
				context: this,
				type: "POST",
				url: "rest/security/login",
				data: {
					"user": this.byId("user").getValue(),
					"password": this.byId("pwd").getValue()
				},
				contentType: "application/x-www-form-urlencoded",
				success: function (returnURL) {
					if (returnURL) {
						document.location.href = returnURL;
					} else {
						document.location.href = "./";
					}
				},
				error: function () {
					MessageBox.error(this.getModel("i18n").getResourceBundle().getText("page.Login.indentification_error"));
				}
			});
		}

	});
});