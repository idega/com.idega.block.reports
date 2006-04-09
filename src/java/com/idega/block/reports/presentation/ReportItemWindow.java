package com.idega.block.reports.presentation;

import java.sql.SQLException;
import java.util.List;

import com.idega.block.reports.business.ReportEntityHandler;
import com.idega.block.reports.business.ReportFinder;
import com.idega.block.reports.data.ReportItem;
import com.idega.core.component.data.ICObject;
import com.idega.data.IDOLegacyEntity;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.block.presentation.Builderaware;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Table;
import com.idega.presentation.text.Link;
import com.idega.presentation.ui.CheckBox;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.HiddenInput;
import com.idega.presentation.ui.SelectionBox;
import com.idega.presentation.ui.SelectionDoubleBox;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextInput;
import com.idega.presentation.util.Edit;
import com.idega.repository.data.RefactorClassRegistry;

public class ReportItemWindow extends IWAdminWindow {

	protected final static int ACT1 = 1, ACT2 = 2, ACT3 = 3, ACT4 = 4, ACT5 = 5, ACT6 = 6;
	private final String sAction = "report_action";
	private final String prefix = "rpit_";
	public final static String prmCategoryId = "rep_categoryid";
	protected IWResourceBundle iwrb;
	protected IWBundle iwb, core;

	private int iCategoryId = -1;
	private String sActPrm = "0";
	private int iAction = 0;

	public ReportItemWindow() {
		setResizable(true);
		setWidth(300);
		setHeight(300);
	}

	protected void control(IWContext iwc) {

		System.err.println("ReportEditorWindow parameters:");
		java.util.Enumeration E = iwc.getParameterNames();
		while (E.hasMoreElements()) {
			String name = (String) E.nextElement();
			System.err.println(name + " " + iwc.getParameter(name));
		}
		System.err.println();
		if (true) {
			try {
				Form F = new Form();
				Table T = new Table();

				if (iwc.isParameterSet(prmCategoryId)) {
					this.iCategoryId = Integer.parseInt(iwc.getParameter(prmCategoryId));
				}

				System.err.println("CategoryId :" + this.iCategoryId);

				if (iwc.getParameter("risave") != null) {
					doUpdate(iwc);
				}
				else if (iwc.getParameter("ea_apply") != null || iwc.getParameter("ea_ok") != null) {
					doUpdateEntityForm(iwc);
				}
				T.add(getLinkTable(this.iCategoryId), 1, 2);

				if (iwc.getParameter(this.sAction) != null) {
					this.sActPrm = iwc.getParameter(this.sAction);
					try {
						this.iAction = Integer.parseInt(this.sActPrm);
						switch (this.iAction) {
							case ACT1:
								T.add(doEntityAdd(iwc, this.iCategoryId), 1, 3);
								break;
							case ACT2:
								T.add(doView(iwc, this.iCategoryId), 1, 3);
								break;
							case ACT3:
								T.add(doChange(iwc, this.iCategoryId), 1, 3);
								break;
						}
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
				else {
					T.add(doView(iwc, this.iCategoryId), 1, 3);
				}
				F.add(T);
				add(F);
			}
			catch (Exception S) {
				S.printStackTrace();
			}
		}
		else {
			add("access denied");
		}
	}

	protected PresentationObject getLinkTable(int iCategoryId) {
		Table LinkTable = new Table(3, 1);
		int last = 3;
		LinkTable.setWidth("100%");
		LinkTable.setCellpadding(2);
		LinkTable.setCellspacing(1);
		LinkTable.setWidth(last, "100%");
		Link Link1 = new Link(this.core.getImage("/shared/create.gif"));
		Link1.addParameter(this.sAction, ACT3);
		Link1.addParameter(prmCategoryId, iCategoryId);
		Link Link2 = new Link(this.core.getImage("/shared/create.gif"));
		Link2.addParameter(this.sAction, ACT2);
		Link2.addParameter(prmCategoryId, iCategoryId);
		Link Link3 = new Link(this.core.getImage("/shared/aid.gif"));
		Link3.addParameter(this.sAction, ACT1);
		Link3.addParameter(prmCategoryId, iCategoryId);

		LinkTable.add(Link1, 1, 1);
		LinkTable.add(Link2, 2, 1);
		LinkTable.add(Link3, 3, 1);

		return LinkTable;
	}

	private PresentationObject doView(IWContext iwc, int iCategoryId) {
		List L = ReportFinder.listOfReportItems(iCategoryId);
		Table T = new Table();
		T.add(formatText("Name"), 1, 1);
		T.add(formatText("Entity"), 2, 1);
		T.add(formatText("Display order"), 3, 1);
		if (L != null) {
			int count = L.size();
			for (int i = 0; i < count; i++) {
				int a = i + 2;
				int b = 1;
				T.add(Edit.formatText(i + 1), b++, a);
				ReportItem RI = (ReportItem) L.get(i);
				Link link = new Link(RI.getName());
				link.addParameter(this.sAction, ACT3);
				link.addParameter("repitemid", RI.getID());
				link.addParameter(prmCategoryId, iCategoryId);
				T.add(link, b++, a);
				T.add(formatText(RI.getEntity()), b++, a);
				T.add(formatText(String.valueOf(RI.getDisplayOrder())), b++, a);
			}
			T.setWidth("100%");
			// T.setHorizontalZebraColored(LightColor,WhiteColor);
			// T.setRowColor(1,MiddleColor);
		}
		return T;
	}

	protected PresentationObject doChange(IWContext iwc, int iCategoryId) throws SQLException {
		String sRepItemId = iwc.getParameter("repitemid");
		Table Frame = new Table(2, 1);
		Frame.setRowVerticalAlignment(1, "top");
		Table T = new Table();
		T.setCellpadding(2);
		T.setCellspacing(1);
		// T.setHorizontalZebraColored(LightColor,WhiteColor);
		// T.setRowColor(1,MiddleColor);
		int a = 1;
		T.add(formatText("Property"), 1, a);
		T.add(formatText("Value"), 2, a++);
		T.add(formatText("Name"), 1, a++);
		T.add(formatText("Field"), 1, a++);
		T.add(formatText("Maintable"), 1, a++);
		T.add(formatText("Joins"), 1, a++);
		T.add(formatText("Join Tables"), 1, a++);
		T.add(formatText("Condition Type"), 1, a++);
		T.add(formatText("Condition Data"), 1, a++);
		T.add(formatText("Condition Operator"), 1, a++);
		T.add(formatText("Entity Class"), 1, a++);
		T.add(formatText("Information"), 1, a++);
		T.add(formatText("Display order"), 1, a++);
		T.add(formatText("Is Function"), 1, a++);

		TextInput name, field, table, joins, jointables, conddata, condop, entity, info, displayorder;
		CheckBox isFunction;
		DropdownMenu condtype;

		name = new TextInput(this.prefix + "name");
		field = new TextInput(this.prefix + "field");
		table = new TextInput(this.prefix + "table");
		joins = new TextInput(this.prefix + "joins");
		jointables = new TextInput(this.prefix + "jointables");
		condtype = ReportObjectHandler.drpTypes(this.prefix + "condtype", "");
		conddata = new TextInput(this.prefix + "conddata");
		condop = new TextInput(this.prefix + "condop");
		entity = new TextInput(this.prefix + "entity");
		info = new TextInput(this.prefix + "info");
		displayorder = new TextInput(this.prefix + "disorder");
		isFunction = new CheckBox(this.prefix + "function");

		if (sRepItemId != null) {
			int repItemId = Integer.parseInt(sRepItemId);
			if (repItemId > 0) {
				try {
					ReportItem ri = ((com.idega.block.reports.data.ReportItemHome) com.idega.data.IDOLookup.getHomeLegacy(ReportItem.class)).findByPrimaryKeyLegacy(repItemId);
					name.setContent(ri.getName());
					field.setContent(ri.getField());
					table.setContent(ri.getMainTable());
					joins.setContent(ri.getJoin());
					jointables.setContent(ri.getJoinTables());
					condtype.setSelectedElement(ri.getConditionType());
					conddata.setContent(ri.getConditionData());
					condop.setContent(ri.getConditionOperator());
					entity.setContent(ri.getEntity());
					info.setContent(ri.getInfo());
					isFunction.setChecked(ri.getIsFunction());
					displayorder.setContent(String.valueOf(ri.getDisplayOrder()));
					T.add(new HiddenInput("repitemid", String.valueOf(ri.getID())));
				}
				catch (SQLException ex) {

				}
			}
		}

		int tlen = 50;
		name.setSize(tlen);
		field.setSize(tlen);
		table.setSize(tlen);
		joins.setSize(tlen);
		jointables.setSize(tlen);
		conddata.setSize(tlen);
		condop.setSize(tlen);
		entity.setSize(tlen);
		info.setSize(tlen);
		displayorder.setSize(4);

		setStyle(name);
		setStyle(field);
		setStyle(table);
		setStyle(joins);
		setStyle(jointables);
		setStyle(condtype);
		setStyle(conddata);
		setStyle(condop);
		setStyle(entity);
		setStyle(info);
		setStyle(isFunction);
		setStyle(displayorder);

		int col = 2;
		int row = 2;
		T.add(name, col, row++);
		T.add(field, col, row++);
		T.add(table, col, row++);
		T.add(joins, col, row++);
		T.add(jointables, col, row++);
		T.add(condtype, col, row++);
		T.add(conddata, col, row++);
		T.add(condop, col, row++);
		T.add(entity, col, row++);
		T.add(info, col, row++);
		T.add(displayorder, col, row++);
		T.add(isFunction, col, row++);

		Frame.add(T);
		Frame.add(new SubmitButton("risave", "Save"));
		Frame.add(new HiddenInput(this.sAction, String.valueOf(ReportItemWindow.ACT4)));
		Frame.add(new HiddenInput(prmCategoryId, String.valueOf(iCategoryId)));

		return (Frame);
	}

	private PresentationObject doEntityAdd(IWContext iwc, int iCategoryId) {

		String sEntId = iwc.getParameter("ent_drp");
		String sDataClassName = "";
		if (iwc.isParameterSet("ent_drp")) {
			sDataClassName = iwc.getParameter("ent_drp");
		}
		Table T = new Table();
		T.add(new HiddenInput(this.sAction, String.valueOf(ACT1)));
		T.add(new HiddenInput(prmCategoryId, String.valueOf(iCategoryId)));
		DropdownMenu drp = getDataEntityDrop("ent_drp", sEntId);
		setStyle(drp);
		drp.setToSubmit();
		T.add(drp, 1, 1);
		if (!"".equals(sDataClassName)) {
			T.add(getEntityForm(sDataClassName, iCategoryId), 1, 2);
			T.add(new SubmitButton("ea_cancel", "Cancel"), 1, 3);
			T.add(new SubmitButton("ea_apply", "Apply"), 1, 3);
			T.add(new SubmitButton("ea_ok", "Ok"), 1, 3);
		}
		return T;
	}

	private PresentationObject getEntityForm(String dataClassName, int iCategoryId) {
		try {
			IDOLegacyEntity ent = (IDOLegacyEntity) RefactorClassRegistry.forName(dataClassName).newInstance();
			Table T = new Table();

			T.add(formatText("Display"), 1, 1);
			T.add(formatText("Field"), 2, 1);
			T.add(formatText("Relation"), 3, 1);
			T.add(new HiddenInput("re_dataclass", dataClassName));
			T.add(new HiddenInput(prmCategoryId, String.valueOf(iCategoryId)));
			SelectionDoubleBox box = new SelectionDoubleBox("box", "Fields", "Order");
			SelectionBox box1 = box.getLeftBox();
			box1.keepStatusOnAction();
			SelectionBox box2 = box.getRightBox();
			box1.keepStatusOnAction();
			box2.addUpAndDownMovers();
			for (int i = 0; i < ent.getVisibleColumnNames().length; i++) {
				box1.addMenuElement(i, ent.getLongName(ent.getVisibleColumnNames()[i]));
			}
			box1.setHeight(20);
			box2.setHeight(20);
			box2.selectAllOnSubmit();
			T.mergeCells(1, 2, 3, 2);
			T.add(box, 1, 2);
			return T;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return new Table();
		}
	}

	protected void doUpdateEntityForm(IWContext iwc) throws SQLException {
		System.err.println("doUpdateEntityForm");
		try {
			String dataClassName = iwc.getParameter("re_dataclass");
			System.err.println(dataClassName);
			if (dataClassName != null) {
				IDOLegacyEntity ent = (IDOLegacyEntity) RefactorClassRegistry.forName(dataClassName).newInstance();
				String[] s = iwc.getParameterValues("box");
				int len = s.length;
				String[] columns = ent.getVisibleColumnNames();
				for (int i = 0; i < len; i++) {
					System.err.println("id from selection box " + s[i]);

					int nr = Integer.parseInt(s[i]);
					ReportEntityHandler.saveReportItem(this.iCategoryId, ent.getLongName(columns[i]), columns[nr], ent.getEntityName(), "", "", "I", "", "like", ent.getClass().getName(), "", false);
				}
			}
		}
		catch (Exception ex) {
			ex.printStackTrace();

		}
	}

	protected void doUpdate(IWContext iwc) throws SQLException {
		String entityId = iwc.getParameter("repitemid");
		int itemId = -1;
		if (entityId != null) {
			itemId = Integer.parseInt(entityId);
		}

		int id = this.iCategoryId;
		String name, field, table, joins, jointables, condtype, conddata, condop, entity, info;
		boolean function;

		name = iwc.getParameter(this.prefix + "name");
		field = iwc.getParameter(this.prefix + "field");
		table = iwc.getParameter(this.prefix + "table");
		joins = iwc.getParameter(this.prefix + "joins");
		jointables = iwc.getParameter(this.prefix + "jointables");
		condtype = iwc.getParameter(this.prefix + "condtype");
		conddata = iwc.getParameter(this.prefix + "conddata");
		condop = iwc.getParameter(this.prefix + "condop");
		entity = iwc.getParameter(this.prefix + "entity");
		info = iwc.getParameter(this.prefix + "info");
		function = iwc.getParameter(this.prefix + "function") != null;
		if (id != 0) {
			if (itemId > 0) {
				ReportEntityHandler.updateReportItem(itemId, id, name, field, table, joins, jointables, condtype, conddata, condop, entity, info, function);
			}
			else {
				ReportEntityHandler.saveReportItem(id, name, field, table, joins, jointables, condtype, conddata, condop, entity, info, function);
			}
		}
	}

	private DropdownMenu getDataEntityDrop(String name, String selected) {
		List L = ReportFinder.listOfDataClasses();
		DropdownMenu drp = new DropdownMenu(name);
		drp.addMenuElementFirst("-", "-1");
		java.util.Iterator I = L.iterator();
		while (I.hasNext()) {
			ICObject obj = (ICObject) I.next();
			drp.addMenuElement(obj.getClassName(), obj.getName());
		}

		drp.setSelectedElement(selected);
		return drp;
	}

	public void main(IWContext iwc) {
		this.core = iwc.getIWMainApplication().getBundle(Builderaware.IW_CORE_BUNDLE_IDENTIFIER);
		control(iwc);
	}

}
