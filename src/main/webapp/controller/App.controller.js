sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController"
], function (BaseController) {
	"use strict";

	return BaseController.extend("com.sos.ui5.battle.controller.App", {

		onInit: function () {
			BaseController.prototype.onInit.call(this);

			this.pingServer();
		},

		/**
		 * Convenience method for menu selected item
		 * 
		 * @public
		 * @param {sap.ui.base.Event} oEvent The item select event
		 */
		onItemSelect: function (oEvent) {
			this.getRouter().navTo(oEvent.getParameter("item").getKey());
		},

		/**
		 * Menu humburger maximize / minimize
		 * 
		 * @protected
		 */
		onSideNavButtonPress: function () {
			this.getModel("view").setProperty("/sideExpanded", !this.getModel("view").getProperty("/sideExpanded"));
		},

		/**
		 * Deconnexion de l'utilisateur
		 */
		onExit: function () {
			jQuery.ajax({
				context: this,
				type: "POST",
				url: "rest/security/logout",
				success: function ( /* data */ ) {
					document.location.href = "./";
				}
			});
		},

		/**
		 * On ping le serveur tant que l'application est lancée pour
		 * éviter l'expiration de la session
		 */
		pingServer: function () {
			this.timer = setInterval(function () {
				$.ajax({
					url: "rest/security/ping",
					error: function () {
						document.location.href = "./";
					}
				});
			}.bind(this), 60000);
		}
	});
});