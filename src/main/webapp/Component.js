sap.ui.define([
	"sap/ui/core/UIComponent",
	"sap/ui/model/json/JSONModel"
], function(UIComponent, JSONModel) {
	"use strict";
	return UIComponent.extend("com.sos.ui5.battle.Component", {
		metadata : {
			manifest : "json"
		},

		init : function() {
			// call the init function of the parent
			UIComponent.prototype.init.apply(this, arguments);

			// Affichage global app
			this.setModel(new JSONModel({
				"sideExpanded" : false
			}), "view");
			
			//On met l'objet user dans le context
			this.setModel(sap.ui.getCore().getModel("user"), "user");
			
			this.getRouter().initialize();
			
			/*global ga:true*/
			// Add router event to notify Google Analytics
			this.getRouter().attachRouteMatched(null, function(oEvent) {
			   //Uniquement les pages parents
			   if (!oEvent.getParameter("config").parent) {
			     ga("set", "page", "/" + oEvent.getParameter("name"));
			     ga("send", "pageview");
			   }
			});
		}

	});
});