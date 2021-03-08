sap.ui.define([
	"sap/m/MessageBox"
], function(MessageBox) {
	"use strict";

	var _errorDialogOpen = false;

	return {
		/**
		 * Fonction d'enregistrement des errerus du service Odata Pour afficher les
		 * erreurs
		 * 
		 * @param {sap.ui.model.Model}
		 *          oODataModel le modele à gérer
		 * @param {sap.ui.model.Model}
		 *          oI18n le modele de traduction
		 */
		register : function(oODataModel, oI18n) {
			// Gestion des erreurs sur la récupération des metadatas
			oODataModel.attachEvent("metadataFailed", function(oEvent) {
				var mTexts = {
					title : oI18n.getResourceBundle().getText("errorHandler.metadataFailed.title"),
					message : oI18n.getResourceBundle().getText("errorHandler.metadataFailed.message")
				};
				this._showServiceError(mTexts, oEvent.getParameters(), false);
			}.bind(this));

			// Gestion des erreurs sur les appels
			oODataModel.attachEvent("requestFailed", function(oEvent) {
				var oParams = oEvent.getParameters();
				// On ne considère pas les 404 / not found comme une erreur
				if ((oParams.response.statusCode === "404" || oParams.response.statusCode === 404) && oParams.response.responseText.indexOf("Cannot POST") !== 0) {
					return;
				}

				var oParsedMessage = JSON.parse(oParams.response.responseText);
				var mTexts = {
					title : oI18n.getResourceBundle().getText("errorHandler.requestFailed.title"),
					message : oParsedMessage.error.message.value
				};
				this._showServiceError(mTexts, oParams, true);
			}.bind(this));
		},
		_showServiceError : function(mErrorTexts, oParams, bShowDetail) {
			if (_errorDialogOpen) {
				return;
			}
			_errorDialogOpen = true;
			var mMessageBoxParams = {
				title : mErrorTexts.title,
				actions : [
					MessageBox.Action.CLOSE
				],
				onClose : function() {
					_errorDialogOpen = false;
				}
			};
			if (bShowDetail) {
				mMessageBoxParams.details = oParams.response;
			}
			MessageBox.error(mErrorTexts.message, mMessageBoxParams);
		}
	};
});