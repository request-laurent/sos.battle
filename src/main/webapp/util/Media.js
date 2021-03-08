sap.ui.define([
	"com/sos/ui5/battle/util/Data",
	"sap/ui/model/json/JSONModel",
	"sap/m/UploadCollectionParameter"
], function(Data, JSONModel, UploadCollectionParameter) {
	"use strict";

	return {
		/**
		 * Permet de récupérer les medias d'un objet (tout sauf le contenu) et le
		 * positionne dans un model avec le nom de la property
		 * 
		 * @param {object} oController Le controller
		 * @param {string} className Le nom de la classe de l'objet
		 * @param {string} property La propriete à gérer
		 * @param {lond} id l'id de l'objet
		 */
		findMedia : function(oController, className, property, id) {
			Data.getJSONModel(oController, "rest/media/find", {
				"class" : className,
				"property" : property,
				"id" : id
			}, property);
		},

		/**
		 * Synchronise les medias avec l'objet pour les relations 1/n
		 * 
		 * @param {object} oController Le controller
		 * @param {string} property Le nom de la propriete
		 * @param {string} model le nom du model de l'objet
		 */
		sync1 : function(oController, property, model) {
			if (oController.getModel(property)) {
				var tMedias = oController.getModel(property).getProperty("/");
				if (tMedias.length === 0) {
					oController.getModel(model).setProperty("/" + property, null);
				} else {
					oController.getModel(model).setProperty("/" + property, tMedias[0]);
				}
			}
		},
		
		/**
		 * Synchronise les medias avec l'objet pour les relations n/n
		 * 
		 * @param {object} oController Le controller
		 * @param {string} property Le nom de la propriete
		 * @param {string} model le nom du model de l'objet
		 */
		syncN : function(oController, property, model) {
			if (oController.getModel(property)) {
				var tMedias = oController.getModel(property).getProperty("/");
				oController.getModel(model).setProperty("/" + property, tMedias);
			}
		},

		/**
		 * Annulation de l'upload d'un fichier au cancel d'une page
		 * 
		 * @param {object} oController Le controller
		 * @param {long} lId L'id du media
		 * @param {string} sClass La classe de l'objet
		 * @param {string} sProperty La propriété
		 * @param {oMedia} oMedia Le media à vérifier
		 */
		cancel : function(oController, lId, sClass, sProperty, oMedia) {
			if (lId) {
				// Si l'objet existe déjà, on revient à son état précédent
				Data.call(oController, "rest/media/cancel?" + jQuery.param({
					"id" : lId,
					"class" : sClass,
					"property" : sProperty
				}), "POST", oMedia);
			} else if (Array.isArray(oMedia)) {
				oMedia.forEach(function (media) {
					this._delete(oController, media);	
				}.bind(this));
			} else {
				this._delete(oController, oMedia);
			}
		},
		
		_delete : function(oController, oMedia) {
			Data.call(oController, "rest/media/delete?" + jQuery.param({
				"id" : oMedia.id
			}), "DELETE", oMedia);
		},

		/**
		 * Lecture des medias 1/n et création des objects corespondants
		 * 
		 * @param {object} oController Le controller
		 * @param {object} oMedia Le media
		 * @param {string} sProperty la properiété
		 */
		readMedias : function(oController, oMedia, sProperty) {
			if (oMedia) {
				var aMedias;
				if (Array.isArray(oMedia)) {
					aMedias = oMedia;
				} else {
					aMedias = [oMedia];
				}
				oController.setModel(new JSONModel(aMedias), sProperty);
			} else {
				oController.setModel(new JSONModel([]), sProperty);
			}
		},

		/**
		 * Attache les événements à un composant UploadCollection
		 * 
		 * @param {UploadCollection} oUploadCollection Le composant
		 */
		attach : function(oUploadCollection) {
			oUploadCollection.attachFileDeleted(function(oEvent) {
				// On supprime l'elément
				var iDoc = oEvent.getParameter("documentId");
				var oData = oEvent.getSource().getBinding("items").getModel().getProperty("/");

				jQuery.each(oData, function(index) {
					if (oData[index] && oData[index].id === iDoc) {
						oData.splice(index, 1);
					}
				});

				oEvent.getSource().getBinding("items").getModel().setData(oData);
				oEvent.getSource().getBinding("items").getModel().refresh(true);
			});

			oUploadCollection.attachChange(function(oEvent) {
				// Obligé de faire cela pour avoir un suivi du fichier ...
				oEvent.getSource().addHeaderParameter(new UploadCollectionParameter({
					name : "x-csrf-token",
					value : "securityTokenFromModel"
				}));
			});

			oUploadCollection.attachBeforeUploadStarts(function(oEvent) {
				// On ajout dans les headers le nom du fichier pour le récupérer côté
				// serveur
				oEvent.getParameters().addHeaderParameter(new UploadCollectionParameter({
					name : "fileName",
					value : oEvent.getParameter("fileName")
				}));
			});

			oUploadCollection.attachUploadComplete(function(oEvent) {
				var oData = oEvent.getSource().getBinding("items").getModel().getProperty("/");
				oData.unshift(JSON.parse(oEvent.getParameter("files")[0].responseRaw));
				oEvent.getSource().getBinding("items").getModel().setData(oData);
				oEvent.getSource().getBinding("items").getModel().refresh(true);
				oEvent.getSource()._aFileUploadersForPendingUpload = [];
			});
		}
	};
});