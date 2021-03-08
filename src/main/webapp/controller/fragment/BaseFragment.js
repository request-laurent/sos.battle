sap.ui.define([
	"sap/ui/base/EventProvider",
	"sap/ui/core/Fragment"
], function (EventProvider, Fragment) {
	"use strict";

	var oFragments = {};

	return EventProvider.extend("com.sos.ui5.battle.controller.fragment.BaseFragment", {
		oController: null,

		/**
		 * Affiche le dialogue
		 * 
		 * @param {object} oController initial settings for the new control
		 * @param {function} [fnCallBack] la fonction de retour
		 * @param {object} oParam parametre pour la fonction oInit
		 */
		open: function (oController, fnCallBack, oParam) {
			this.oController = oController;
			this.fnCallBack = fnCallBack;

			var oFragment = this.getFragment();
			oFragment.fnCallBack = fnCallBack;
			this.onInit(oParam);
			oFragment.open();
		},

		/**
		 * A surcharger, pour l'initialisation du fragment
		 */
		onInit: function ( /*oParam*/ ) {},

		/**
		 * Récupération du fragment
		 *  @return {Fragment} Le fragment
		 */
		getFragment: function () {
			var sName = this.oController.getMetadata().getName() + "-" + this.getMetadata().getName();

			if (!oFragments[sName]) {
				var oFragment = new sap.ui.xmlfragment(sName, this.getMetadata().getName().replace("controller", "view"), this);
				this.oController.getView().addDependent(oFragment);
				oFragments[sName] = oFragment;
			}
			return oFragments[sName];
		},

		/**
		 * Ferme la boite de dialogue
		 * 
		 * @param {object} [obj] l'objet de retour
		 */
		close: function (obj) {
			var oFragment = this.getFragment();
			var fnCallBack = oFragment.fnCallBack;
			oFragment.close();
			if (fnCallBack) {
				fnCallBack(obj);
			}
		},

		/**
		 * Récupère un model, le modèle est positionné sur le controleur parent
		 * 
		 * @param {string} sName Le nom du modèle
		 * @return {sap.ui.model.Model} le modele
		 */
		getModel: function (sName) {
			return this.oController.getModel(sName);
		},

		/**
		 * Affecte le modèle au controller parent
		 * 
		 * @param {sap.ui.model.Model} oModel Le modèle
		 * @param {string} sName Le nom du modèle
		 */
		setModel: function (oModel, sName) {
			this.oController.setModel(oModel, sName);
		},

		/**
		 * Récupère un objet par son som sur le fragment
		 * 
		 * @param {string} sName Le nom de l'objet
		 * @return {sap.ui.core.Element} l'objet
		 */
		byId: function (sName) {
			return Fragment.byId(this.oController.getMetadata().getName() + "-" + this.getMetadata().getName(), sName);
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
			return this.oController.getI18n();
		},
		
		/**
		 * Retourne le controller
		 * 
		 * @return {object} renvoie le constroller
		 */
		getController: function() {
			return this.oController; 
		}
	});
});