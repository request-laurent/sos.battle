sap.ui.define([
	"sap/ui/core/mvc/Controller",
	"sap/ui/core/routing/History",
	"sap/ui/model/json/JSONModel",
	"sap/m/MessagePopover",
	"sap/m/MessagePopoverItem",
	"sap/m/MessageToast",
	"sap/m/MessageBox"
], function(Controller, History, JSONModel, MessagePopover, MessagePopoverItem, MessageToast, MessageBox) {
	"use strict";

	return Controller.extend("com.sos.ui5.battle.controller.BaseController", {

		/**
		 * Initialisation des controllers
		 */
		onInit : function() {
			// Densité des composants en fonction du support de touch ou pas
			// this.getView().addStyleClass(sap.ui.Device.support.touch ?
			// "sapUiSizeCozy" : "sapUiSizeCompact");

			// Activer l'affichage des erreurs
			sap.ui.getCore().getMessageManager().registerObject(this.getView(), true);
		},

		/**
		 * Convenience method for accessing the router.
		 * 
		 * @public
		 * @returns {sap.ui.core.routing.Router} the router for this component
		 */
		getRouter : function() {
			return sap.ui.core.UIComponent.getRouterFor(this);
		},

		/**
		 * Convenience method for getting the view model by name.
		 * 
		 * @public
		 * @param {string} sName - the model name
		 * @returns {sap.ui.model.Model} the model instance
		 */
		getModel : function(sName) {
			return this.getView().getModel(sName) || (this.getOwnerComponent() && this.getOwnerComponent().getModel(sName));
		},

		/**
		 * Convenience method for setting the view model.
		 * 
		 * @public
		 * @param {sap.ui.model.Model} oModel the model instance
		 * @param {string} [sName] the model name
		 * @returns {sap.ui.mvc.View} the view instance
		 */
		setModel : function(oModel, sName) {
			oModel.setSizeLimit(10000);
			return this.getView().setModel(oModel, sName);
		},
		/**
		 * Convenience method for handle back navigation.
		 * 
		 * @public
		 * @param {?} event l'événement déclancheur, facultatif
		 */
		onNavBack : function(/* oEvent */) {
			var oHistory = History.getInstance();
			var sPreviousHash = oHistory.getPreviousHash();
			if (sPreviousHash !== undefined) {
				window.history.go(-1);
			} else {
				this.getRouter().navTo("Home", {}, true /* no history */);
			}
		},
		/**
		 * Méthode utilitaire: Retourne le ResourceBundle contenant les traductions
		 * de l'application
		 * 
		 * @protected
		 * @return {jQuery.sap.util.ResourceBundle} renvoie le resourceBundle de
		 *         i18n
		 */
		getI18n : function() {
			return this.getOwnerComponent().getModel("i18n").getResourceBundle();
		},
		/**
		 * Bind the view with the path
		 * 
		 * @public
		 * @param {string} sPath odata Path of object
		 * @param {map} oparameters bindings parameters
		 */
		bindView : function(sPath, oparameters) {
			var oView = this.getView();

			// Attente
			oView.setBusy(true);
			oView.unbindElement();

			// On bind l'objet dans la vue
			oView.bindElement({
				path : sPath,
				parameters : oparameters,
				events : {
					change : function() {
						oView.setBusy(false);
					}
				}
			});
		},
		/**
		 * Récupère l'objet associé au context
		 * 
		 * @public
		 * @returns {sap.ui.model.Model} L'objet du context
		 */
		getBindObject : function() {
			return this.getView().getModel().getProperty(this.getView().getElementBinding().sPath);
		},
		/**
		 * Récupération du Message model, il y n'existe pas, il est créé.
		 * 
		 * @return {sap.ui.model.Model} renvoie le model message
		 */
		_getMessageModel : function() {
			if (!this.getModel("message")) {
				this.setModel(new JSONModel({
					count : 0,
					messages : [],
					_refs : {}
				}), "message");
				this.getModel("message").setSizeLimit(1000);
			}
			return this.getModel("message");
		},
		/**
		 * Evénement pour l'ouverture de la boite de message
		 * 
		 * @param {sap.ui.base.Event} oEvent message le message à ajouter
		 * 
		 */
		onMessage : function(oEvent) {
			this.showMessage(oEvent.getSource());
		},
		/**
		 * Affiche la liste des messages
		 */
		showMessage : function() {
			if (!this.oDialogMessage) {
				var oMessageTemplate = new MessagePopoverItem({
					type : "{type}",
					title : "{title}",
					description : "{description}",
					subtitle : "{subtitle}",
					counter : "{counter}"
				});

				this.oDialogMessage = new MessagePopover({
					items : {
						path : "/messages",
						template : oMessageTemplate
					}
				});
				this.oDialogMessage.setModel(this._getMessageModel());
			}
			this.oDialogMessage.openBy(this.getView().byId("errorMessages"));
		},
		/**
		 * Suppression de tous les messages
		 */
		clearMessage : function() {
			var data = this._getMessageModel().getData();
			data.messages = [];
			data.count = 0;
			this._getMessageModel().setData(data);
		},
		/**
		 * Ajouter un message
		 * 
		 * @param {sap.m.MessageItem} oMessage ajoute un message à la liste
		 */
		addMessage : function(oMessage) {
			var data = this._getMessageModel().getData();
			data.messages.push(oMessage);
			data.count += 1;
			this._getMessageModel().setData(data);
		},
		/**
		 * Ajout un message de type erreur
		 * 
		 * @param {string} sTitle le titre
		 * @param {string} [sSubTitle] Le sous titre (facultatif)
		 * @param {string} [sDescription] la descrition (facultatif)
		 * @param {integer} [iCounter] le nombre d'erreur (facultatif)
		 * 
		 */
		addError : function(sTitle, sSubTitle, sDescription, iCounter) {
			this.addMessage({
				type : "Error",
				title : sTitle,
				subTitel : sSubTitle,
				description : sDescription,
				counter : iCounter
			});
			this.showMessage();
		},
		/**
		 * Affiche un message temporaire (message Toast
		 * 
		 * @param {string} sMessage le message à afficher
		 */
		messageToast : function(sMessage) {
			MessageToast.show(sMessage);
		},
		/**
		 * Affiche une boite de dialogue pour une erreur
		 * 
		 * @param {string} sMessage le message à afficher
		 */
		messageError : function(sMessage) {
			MessageBox.error(sMessage);
		},

		/**
		 * Vérifie si il y a des erreurs sur les composants de la vue
		 * 
		 * @param {Control} [oControl] le controle à vérifier si vide, c'est la vue
		 * @return {boolean} a des erreurs ?
		 */
		hasError : function(oControl) {
			if (!oControl) {
				return this.hasError(this.getView());
			} else {
				if (typeof oControl.getContent === "function") {
					if (this._hasErrorChild(oControl.getContent())) {
						return true;
					}
				}
				if (typeof oControl.getItems === "function") {
					if (this._hasErrorChild(oControl.getItems())) {
						return true;
					}
				}
				if (typeof oControl.getRows === "function") {
					if (this._hasErrorChild(oControl.getRows())) {
						return true;
					}
				}
				if (typeof oControl.getCells === "function") {
					if (this._hasErrorChild(oControl.getCells())) {
						return true;
					}
				}

				if (typeof oControl.getValueState === "function") {
					return oControl.getValueState() === "Error";
				}

				return false;
			}
		},
		
		_hasErrorChild : function(tChilds) {
			if (tChilds) {
				for ( var oChild in tChilds) {
					var bError = this.hasError(tChilds[oChild]);
					if (bError) {
						return true;
					}
				}
			}
			return false;
		},

		_busyCpt : 0,

		/**
		 * Affiche ou masque la page d'attente
		 * Gére l'enpilement des appels, c'est le premier qui affiche et le dernier qui masque
		 * 
		 * @param {boolean} [bBusy] affiche ou masque la page d'attente
		 */
		setBusy : function(bBusy) {
			if (bBusy) {
				//On affiche
				if (this._busyCpt === 0) {
					this.getView().setBusy(true);
				}
				this._busyCpt = this._busyCpt + 1;
			} else {
				//On n'affiche pas
				this._busyCpt = this._busyCpt - 1;
				
				if (this._busyCpt === 0) {
					this.getView().setBusy(false);
				}
			}
		}
	});
});