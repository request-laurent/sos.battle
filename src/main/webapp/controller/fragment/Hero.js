/*jshint esversion: 6 */
sap.ui.define([
	"com/sos/ui5/battle/controller/fragment/BaseFragment",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/util/Data"
], function(BaseFragment, JSONModel, Data) {
	"use strict";
	return BaseFragment.extend("com.sos.ui5.battle.controller.fragment.Hero", {
		onInit : function(obj) {
			this.clear();
			this.survivors();
			if (obj) {
				this.setModel(new JSONModel(obj), "hero");
				var hero = Data.getLine(this.getModel("heros").getProperty("/"), "id", obj.id);
				this.getModel("survivor").setProperty("/id", hero.hero);
				this.heroChange2();
			}
		},

		clear : function() {
			this.setModel(new JSONModel({
				"id" : null,
				"skills" : [
					null,
					null,
					null
				],
				"equipment" : [
					null,
					null,
					null
				]
			}), "hero");

			this.setModel(new JSONModel([]), "skills1");
			this.setModel(new JSONModel([]), "skills2");
			this.setModel(new JSONModel([]), "skills3");
			this.setModel(new JSONModel([]), "equipment1");
			this.setModel(new JSONModel([]), "equipment2");
			this.setModel(new JSONModel([]), "equipment3");

		},

		onValidate : function() {
			if (!this.getController().hasError(this.getFragment())) {
			  this.close(this.getModel("hero").getProperty("/"));
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
					null,
					null
				],
				"equipment" : [
					null,
					null,
					null
				]
			}), "hero");
			
			this.heroChange2();
		},
		
		heroChange2 : function() {
			var hero = Data.getLine(this.getModel("heros").getProperty("/"), "hero", this.getModel("survivor").getProperty("/id"));
			
			if (hero) {
				// Filtrer les rank
				this.ranks(hero);

				// Filtrer les skills du héro
				this.skills(hero, 1);
				this.skills(hero, 2);
				this.skills(hero, 3);

				// Filtrer les équipements
				this.equipment(hero, "goggle", 1);
				this.equipment(hero, "body", 2);
				this.equipment(hero, "leg", 3);
			} else {
				this.clear();
			}
		},

		ranks : function(hero) {
			var tab = this.getModel("heros").getProperty("/").filter(function(o) {
				return (o.hero == hero.hero);
			});
			this.setModel(new JSONModel(tab), "ranks");
		},

		skills : function(hero, num) {
			var tab = this.getModel("skills").getProperty("/").filter(function(o) {
				return (o.hero == hero.hero) && (o.skillNo == num);
			});
			this.setModel(new JSONModel(tab), "skills" + num);
		},

		equipment : function(hero, part, num) {
			var tab = this.getModel("equipment").getProperty("/").filter(function(o) {
				return (o.profession == hero.profession) && (o.part == part);
			});
			this.setModel(new JSONModel(tab), "equipment" + num);
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