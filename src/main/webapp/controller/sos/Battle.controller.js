sap.ui.define([
	"com/sos/ui5/battle/controller/BaseController",
	"sap/m/MessageBox",
	"sap/ui/model/json/JSONModel",
	"com/sos/ui5/battle/controller/sos/Percent",
	"com/sos/ui5/battle/util/Data",
	"com/sos/ui5/battle/controller/fragment/Hero",
	"com/sos/ui5/battle/controller/fragment/Skill",
	"com/sos/ui5/battle/controller/fragment/Save",
	"com/sos/ui5/battle/controller/fragment/Load",
	"sap/m/Dialog",
	"sap/m/DialogType",
	"sap/m/Input",
	"sap/m/Button",
	"sap/m/ButtonType",
	"sap/ui/core/Core"
], function(BaseController, MessageBox, JSONModel, Percent, Data, Hero, Skill, Save, Load, Dialog, DialogType, Input, Button, ButtonType, Core) {
	"use strict";
	return BaseController.extend("com.sos.ui5.battle.controller.sos.Battle", {
		cpt : 0,
		sos : null,
		onInit : function() {
			BaseController.prototype.onInit.call(this);
			this.getRouter().getRoute("Battle").attachPatternMatched(this._handleRouteMatched, this);
			this.sos = this.getOwnerComponent().getModel("sos").getResourceBundle();
			Data.getJSONModel(this, "rest/battle/units", null, "units").then(this.trad.bind(this));
			Data.getJSONModel(this, "rest/battle/heros", null, "heros").then(this.trad.bind(this));
			Data.getJSONModel(this, "rest/battle/skills", null, "skills").then(this.trad.bind(this));
			Data.getJSONModel(this, "rest/battle/equipment", null, "equipment").then(this.trad.bind(this));
			Data.getJSONModel(this, "rest/battle/benefits", null, "benefits").then(this.trad.bind(this));
			Data.getJSONModel(this, "rest/battle/buffs", null, "buffs").then(this.trad.bind(this));
			this.setModel(new JSONModel({}), "result");
			this.setModel(new JSONModel({
				"report" : false,
				"key" : "information",
				"attacker" : {
					"heros" : 0
				},
				"attacked" : {
					"heros" : 0
				}

			}), "view");

			this.byId("vizAttacker").setVizProperties({
				"title" : {
					"text" : this.sos.getText("id_attacker")
				}
			});
			this.byId("vizDefender").setVizProperties({
				"title" : {
					"text" : this.sos.getText("id_defender")
				}
			});
		},

		trad : function(oData) {
			var tab = oData.getProperty("/");

			tab.forEach(function(o) {
				if (o.name && (!o.name.startsWith("prop_") ||  this.sos.hasText(o.name))) {
					o.name = this.sos.getText(o.name);
				}
				if (o.rankName && this.sos.hasText(o.rankName)) {
					o.rankName = this.sos.getText(o.rankName);
				}
			}.bind(this));
			this.cpt++;
			if (this.cpt === 4) {
				Data.getJSONModel(this, "rest/battle/simulation").then(this.calcSelected.bind(this));
			}
		},

		_handleRouteMatched : function() {
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
		},

		i18n : function(text) {
			return this.getOwnerComponent().getModel("sos").getResourceBundle().getText(text);
		},

		onAdd : function(oEvent, path) {
			var tab = this.getModel().getProperty(path);
			var comp = oEvent.getSource().getParent().getParent();

			tab.push({});
			this.getModel().setProperty(path, tab);
			comp.setVisibleRowCount(tab.length);
		},

		onAddHeroes : function(oEvent, path) {
			var tab = this.getModel().getProperty(path);
			var comp = oEvent.getSource().getParent().getParent();
			var localPath = null;
			var obj = null;
			if (oEvent.getSource().getBindingContext()) {
				localPath = oEvent.getSource().getBindingContext().getPath();
				obj = oEvent.getSource().getBindingContext().getProperty();
			}

			new Hero().open(this, function(oReturn) {
				if (oReturn) {
					if (obj) {
						this.getModel().setProperty(localPath, oReturn);
					} else {
						tab.push(oReturn);
						this.getModel().setProperty(path, tab);
						comp.setVisibleRowCount(this.getModel().getProperty(path).length);
					}
				}
			}.bind(this), obj);
		},

		onAddSkills : function(oEvent, path) {
			var tab = this.getModel().getProperty(path);
			var comp = oEvent.getSource().getParent().getParent();
			if (!tab) {
				tab = [];
			}
			var localPath = null;
			var obj = null;
			if (oEvent.getSource().getBindingContext()) {
				localPath = oEvent.getSource().getBindingContext().getPath();
				obj = oEvent.getSource().getBindingContext().getProperty();
			}

			new Skill().open(this, function(oReturn) {
				if (oReturn) {
					if (obj) {
						this.getModel().setProperty(localPath, oReturn);
					} else {
						tab.push(oReturn);
						this.getModel().setProperty(path, tab);
						comp.setVisibleRowCount(this.getModel().getProperty(path).length);
					}
				}
			}.bind(this), obj);
		},

		onDelete : function(oEvent, path) {
			var tab = this.getModel().getProperty(path);
			var index = tab.indexOf(oEvent.getSource().getBindingContext().getProperty(""));
			tab.splice(index, 1);
			this.getModel().setProperty(path, tab);
			oEvent.getSource().getParent().getParent().getParent().setVisibleRowCount(tab.length);
			this.calcSelected();
		},

		onBattle : function() {
			if (this.hasError()) {
				this.messageError(this.getI18n().getText("common.correct_error"));
				return;
			}
			Data.postJSONModel(this, "rest/battle/battle", this.getModel().getProperty("/"), "result").then(function() {
				this.getModel("view").setProperty("/report", true);
				this.getModel("view").setProperty("/key", "report");
				this.messageToast(this.getI18n().getText("common.success"));
			}.bind(this));
		},

		onOptimize : function() {
			if (this.hasError()) {
				this.messageError(this.getI18n().getText("common.correct_error"));
				return;
			}
			Data.postJSONModel(this, "rest/battle/optimize", this.getModel().getProperty("/"), "result").then(function(oData) {
				this.getModel().setProperty("/attacker", oData.getProperty("/"));
				this.calcSelected();
				this.messageToast(this.getI18n().getText("common.success"));
				this.onBattle();
			}.bind(this));
		},

		onSave : function(path) {
			new Save().open(this, function(oReturn) {
				if (oReturn) {
					Data.call(this, "rest/battle/saveFighter?" + jQuery.param({
						"name" : oReturn
					}), "POST", this.getModel().getProperty(path)).then(function() {
						this.messageToast(this.getI18n().getText("common.success"));
					}.bind(this));
				}
			}.bind(this));

		},

		onLoad : function(path) {
			new Load().open(this, function(oReturn) {
				if (oReturn) {
					Data.call(this, "rest/battle/template?", "GET", {
						"id" : oReturn.id,
						"type" : oReturn.type
					}).then(function(oData) {
						this.getModel().setProperty(path, oData);
						this.calcSelected();
						this.messageToast(this.getI18n().getText("common.success"));
					}.bind(this));
				}
			}.bind(this));
		},

		rowSelectionChange : function(oContext) {
			this.updateSelected(oContext.getSource());
		},

		updateSelected : function(grid) {
			if (this.calc !== true) {
				var aIndices = grid.getSelectedIndices();
				if (grid.getContextByIndex(0)) {
					var path = grid.getContextByIndex(0).getPath().substring(0, 16);

					// On force tout à false
					this.getModel().getProperty(path).forEach(function(o, i) {
						this.getModel().setProperty(path + "/" + i + "/selected", false);
					}.bind(this));

					// On positionne à true
					var nb = 0;
					aIndices.forEach(function(i) {
						this.getModel().setProperty(path + "/" + i + "/selected", true);
						nb++;
					}.bind(this));
					this.getModel("view").setProperty(path, nb);
				}
			}
		},

		calcSelected : function() {
			this.calcSelected2(this.byId("grid1"), "/attacker/heroes");
			this.calcSelected2(this.byId("grid2"), "/attacked/heroes");
		},

		calcSelected2 : function(grid, path) {
			this.calc = true;
			var nb = 0;

			grid.clearSelection();
			this.getModel().getProperty(path).forEach(function(o, i) {
				if (o.selected) {
					grid.addSelectionInterval(i, i);
					nb++;
				}
			}.bind(this));
			this.getModel("view").setProperty(path, nb);
			this.calc = false;
		},

		onExport : function() {
			jQuery("<form>", {
				"method" : "POST",
				"action" : "rest/battle/export",
				"target" : "blank"
			}).append(jQuery("<input>", {
				"name" : "json",
				"value" : JSON.stringify(this.getModel().getProperty("/")),
				"type" : "hidden"
			})).appendTo('body').submit();
		},
		
		onImport : function() {
			if (!this.oSubmitDialog) {
				var that = this;
				this.oSubmitDialog = new Dialog({
					type : DialogType.Message,
					title : "Confirm",
					content : [
						new Input("submissionNote", {
							width : "100%",
							placeholder : "battle id"
						})
					],
					beginButton : new Button({
						type : ButtonType.Emphasized,
						text : "Submit",
						press : function() {
							Data.getJSONModel(this, "rest/battle/read", {
								"id" : Core.byId("submissionNote").getValue()
							}).then(that.calcSelected.bind(this));
							this.oSubmitDialog.close();
						}.bind(this)
					}),
					endButton : new Button({
						text : "Cancel",
						press : function() {
							this.oSubmitDialog.close();
						}.bind(this)
					})
				});
			}

			this.oSubmitDialog.open();
		}
	});
});