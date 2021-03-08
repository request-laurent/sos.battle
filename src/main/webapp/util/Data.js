sap.ui.define([
	"sap/ui/model/json/JSONModel",
	"sap/m/MessageBox"
], function (JSONModel, MessageBox) {
	"use strict";

	return {
		/**
		 * Charger un modele JSON avec un service rest
		 * 
		 * @param {object} oController Le controleur
		 * @param {string} sUrl l'url du service
		 * @param {string} sMethod POST ou GET
		 * @param {object} oParameters l'url du service
		 * @param {string} [sModeleName] le nom du modele
		 * 
		 * @return {Promise} promise
		 */
		setJSONModel: function (oController, sUrl, sMethod, oParameters, sModeleName) {
			oController.setBusy(true);
			return new Promise(function (resolve, reject) {
				jQuery.ajax({
					context: this,
					type: sMethod,
					url: sUrl,
					data: oParameters,
					contentType: "application/json; charset=utf-8",
					success: function (data) {
						oController.setBusy(false);
						if (oController.getModel(sModeleName)) {
							oController.getModel(sModeleName).setData(data);
							resolve(oController.getModel(sModeleName));
						} else {
							var oData = new JSONModel(data);
							oController.setModel(oData, sModeleName);
							resolve(oData);
						}
					},
					error: function (xhr, status, error) {
						oController.setBusy(false);
						this.errorMessage(xhr, status, error);
						reject(error);
					}
				});
			}.bind(this));
		},

		/**
		 * Charger un modele JSON avec un service rest de type POST
		 * 
		 * @param {object} oController Le controleur
		 * @param {string} sUrl l'url du service
		 * @param {object} oParameters l'url du service
		 * @param {string} [sModeleName] le nom du modele
		 * 
		 * @return {Promise} promise
		 */
		postJSONModel: function (oController, sUrl, oParameters, sModeleName) {
			return this.setJSONModel(oController, sUrl, "POST", JSON.stringify(oParameters), sModeleName);
		},

		/**
		 * Charger un modele JSON avec un service rest de type GET
		 * 
		 * @param {object} oController Le controleur
		 * @param {string} sUrl l'url du service
		 * @param {object} oParameters l'url du service
		 * @param {string} [sModeleName] le nom du modele
		 * 
		 * @return {Promise} promise
		 */
		getJSONModel: function (oController, sUrl, oParameters, sModeleName) {
			return this.setJSONModel(oController, sUrl, "GET", oParameters, sModeleName);
		},

		/**
		 * Récupérer des objets
		 * 
		 * @param {object} oController Le controleur
		 * @param {object} oParameters l'url du service
		 * @param {string} [sModeleName] le nom du modele
		 * 
		 * @return {Promise} promise
		 */
		select: function (oController, oParameters, sModeleName) {
			return this.postJSONModel(oController, "rest/data/select", oParameters, sModeleName);
		},

		/**
		 * Récupérer une liste d'objets
		 * 
		 * @param {object} oController Le controleur
		 * @param {object} oParameters {"Terminal": {"className" : "Terminal",
		 *          "fetch": "left join fetch o.zz", "orderBy" : "o.code"}, ...}
		 * 
		 * @return {Promise} promise
		 */
		selects: function (oController, oParameters) {
			oController.setBusy(true);
			return new Promise(function (resolve, reject) {
				jQuery.ajax({
					context: this,
					type: "POST",
					url: "rest/data/selects",
					data: JSON.stringify(oParameters),
					contentType: "application/json; charset=utf-8",
					success: function (data) {
						oController.setBusy(false);
						for (var sModeleName in data) {
							if (oController.getModel(sModeleName)) {
								oController.getModel(sModeleName).setData(data[sModeleName]);
							} else {
								var oData = new JSONModel(data[sModeleName]);
								oController.setModel(oData, sModeleName);
							}
						}
						resolve(data);
					},
					error: function (xhr, status, error) {
						oController.setBusy(false);
						this.errorMessage(xhr, status, error);
						reject(error);
					}
				});
			}.bind(this));
		},

		/**
		 * Récupérer un objet
		 * 
		 * @param {object} oController Le controleur
		 * @param {object} oParameters l'url du service
		 * @param {string} [sModeleName] le nom du modele
		 * 
		 * @return {Promise} promise
		 */
		find: function (oController, oParameters, sModeleName) {
			return this.postJSONModel(oController, "rest/data/find", oParameters, sModeleName);
		},

		/**
		 * Enregistrer un objet (création ou modification
		 * 
		 * @param {string} sClassName Le nom de la classe
		 * @param {object} oData L'objet à enregistrer
		 * @return {Promise} promise
		 */
		save: function (sClassName, oData) {
			return new Promise(function (resolve, reject) {
				jQuery.ajax({
					context: this,
					type: "POST",
					url: "rest/data/save?className=" + sClassName,
					data: JSON.stringify(oData),
					contentType: "application/json; charset=utf-8",
					success: function (data) {
						resolve(data);
					},
					error: function (xhr, status, error) {
						this.errorMessage(xhr, status, error);
						reject(error);
					}
				});
			}.bind(this));
		},

		/**
		 * Suprrime un objet
		 * 
		 * @param {string} sClassName Le nom de la classe
		 * @param {object} oKey La clé de l'objet
		 * @return {Promise} promise
		 */
		remove: function (sClassName, oKey) {
			return new Promise(function (resolve, reject) {
				jQuery.ajax({
					context: this,
					type: "DELETE",
					url: "rest/data/delete" + "?" + jQuery.param({
						"className": sClassName,
						"key": oKey
					}),
					contentType: "application/json; charset=utf-8",
					success: function (data) {
						resolve(data);
					},
					error: function (xhr, status, error) {
						this.errorMessage(xhr, status, error);
						reject(error);
					}
				});
			}.bind(this));
		},

		/**
		 * Permet de forcer à null tous les attributs vide, c'est important pour la
		 * serialisation serveur
		 * 
		 * @param {object} oModel Le model
		 * @param {String} [sPath] Le chemin, si vide, tout le model
		 */
		removeEmptyAttributs: function (oModel, sPath) {
			if (sPath) {
				// On force à null
				if (oModel.getProperty(sPath) !== null && typeof oModel.getProperty(sPath) === "object" && (jQuery.isEmptyObject(oModel.getProperty(
						sPath)) || oModel.getProperty(sPath + "/id") === "")) {
					oModel.setProperty(sPath, null);
				}
			} else {
				for (var sAttr in oModel.getData()) {
					this.removeEmptyAttributs(oModel, "/" + sAttr);
				}
			}
		},

		/**
		 * Permet de forcer les objects vides à la place de null
		 * 
		 * @param {object} oModel Le model
		 * @param {array} aPath tableau des chemins
		 */
		addEmptyObjects: function (oModel, aPath) {
			aPath.forEach(function (sPath) {
				// On ajoute un objet vide ) la place du null
				if (oModel.getProperty(sPath) === null) {
					oModel.setProperty(sPath, {});
				}
			});
		},

		/**
		 * Permet de chercher un objet dans un model et de l'afecter sur un autre
		 * objet Utilisé pour les relation 1/N avec les combo
		 * 
		 * @param {object} oModel Le model
		 * @param {string} sPath tableau des chemins
		 * @param {object} oList Le model list avec les valeurs
		 * @param {string} oProperty la proriete qui fait le lien
		 * @param {long} [oVal] la valeur à chercher
		 */
		setObject: function (oModel, sPath, oList, oProperty, oVal) {
			if (!oProperty) {
				oProperty = "id";
			}

			// Si on a pas la valeur, on la cherche dans l'objet source
			if (!oVal) {
				oVal = oModel.getProperty(sPath + "/" + oProperty);
			}

			var o = null;
			oList.getProperty("/").forEach(function (oItem) {
				if (oItem[oProperty] === oVal) {
					o = oItem;
				}
			});

			oModel.setProperty(sPath, o);
		},

		/**
		 * Récupére un object dans une liste par sa propriété
		 * 
		 * @param {array} aList La list avec les valeurs
		 * @param {string} sProperty la proriete qui fait le lien
		 * @param {object} [oVal] la valeur à chercher
		 * 
		 * @return {object} l'objet trouvé ou null
		 */
		getLine: function (aList, sProperty, oVal) {
			var o = null;
			aList.forEach(function (oItem) {
				if (oItem[sProperty] == oVal) {
					o = oItem;
				}
			});
			return o;
		},

		/**
		 * Appel ajax neutre
		 * 
		 * @param {object} oController Le controleur
		 * @param {string} sUrl l'url du service
		 * @param {string} sMethod POST ou GET
		 * @param {object} oParameters l'url du service
		 * 
		 * @return {Promise} promise
		 */
		call: function (oController, sUrl, sMethod, oParameters) {
			oController.setBusy(true);

			var _oParameters = (oParameters && sMethod === "POST") ? JSON.stringify(oParameters) : oParameters;
			return new Promise(function (resolve, reject) {
				jQuery.ajax({
					context: this,
					type: sMethod,
					url: sUrl,
					data: _oParameters,
					contentType: "application/json; charset=utf-8",
					success: function (oData) {
						oController.setBusy(false);
						resolve(oData);
					},
					error: function (xhr, status, error) {
						oController.setBusy(false);
						this.errorMessage(xhr, status, error);
						reject(error);
					}
				});
			}.bind(this));
		},

		errorMessage: function (xhr, status, error) {
			var sMessage = error;
			var sError = null;
			if (xhr.responseText) {
				try {
					var oError = JSON.parse(xhr.responseText);
					sError = oError.details;
					sMessage = oError.message;
				} catch (e) {
					sError = xhr.responseText;
				}
			}
			MessageBox.error(sMessage, {
				"details": sError
			});
		}
	};
});