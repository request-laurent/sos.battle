/*jshint esversion: 6 */
sap.ui.define([
	"com/sos/ui5/battle/controller/fragment/BaseFragment",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data"
], function(BaseFragment, JSONModel, Data) {
	"use strict";
	return BaseFragment.extend("com.sos.ui5.battle.controller.fragment.Skill", {
		onInit : function(obj) {
			this.clear();
			this.survivors();
			if (obj) {
				this.setModel(new JSONModel({
					"id" : null,
					"skills" : [
						obj,
					]
				}), "hero");
				
				//Recherche du hero
				
				var skill = Data.getLine(this.getModel("skills").getProperty("/"), "id", obj);
				
				this.getModel("survivor").setProperty("/id", skill.hero);
				this.heroChange2();
			}
		},

		clear : function() {
			this.setModel(new JSONModel({
				"id" : null,
				"skills" : [
					null,
				]
			}), "hero");

			this.setModel(new JSONModel([]), "skills1");
		},

		onValidate : function() {
			if (!this.getController().hasError(this.getFragment())) {
				this.close(this.getModel("hero").getProperty("/skills/0"));
			} else {
				this.getController().messageError(this.getI18n().getText("common.correct_error"));
			}
		},

		onReject : function() {
			this.close();
		},

		heroChange : function() {
			this.setModel(new JSONModel({
				"id" : null,
				"skills" : [
					null,
				]
			}), "hero");
			
			this.heroChange2();
		},
		
		heroChange2 : function() {
			var hero = Data.getLine(this.getModel("heros").getProperty("/"), "hero", this.getModel("survivor").getProperty("/id"));
			
			if (hero) {
				// Filtrer les skills du héro
				this.skills(hero, 1);
			} else {
				this.clear();
			}
		},

		skills : function(hero, num) {
			var tab = this.getModel("skills").getProperty("/").filter(function(o) {
				return (o.hero == hero.hero) && (o.skillNo == num);
			});
			this.setModel(new JSONModel(tab), "skills" + num);
		},

		survivors : function() {
			var tab = this.getModel("heros").getProperty("/").filter(function(o) {
				return o.rank == "1.1";
			});
			this.setModel(new JSONModel(tab), "survivors");
			this.setModel(new JSONModel({
				"id" : null
			}), "survivor");
		},

		getObj : function(key, model, attr) {
			var line = Data.getLine(this.getModel(model).getProperty("/"), "id", key);

			if (line) {
				return line[attr];
			} else if (attr === "image") {
				return "img/empty.png";
			} else {
				return null;
			}
		}
	});
});