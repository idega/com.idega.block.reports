package com.idega.block.reports.presentation;

import com.idega.block.reports.data.*;
import com.idega.block.reports.business.*;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.PresentationObjectContainer;
import java.sql.SQLException;
import java.util.Vector;
import java.util.Collections;
import java.util.List;
import com.idega.data.EntityFinder;
import com.idega.presentation.Editor;
import com.idega.presentation.Table;
import com.idega.presentation.ui.*;
import com.idega.presentation.text.*;
import com.idega.presentation.Script;
import com.idega.presentation.PresentationObject;
import com.idega.presentation.Image;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.IWBundle;
import com.idega.block.IWBlock;
import com.idega.core.data.ICCategory;
import com.idega.block.presentation.CategoryBlock;
import com.idega.block.category.business.*;
import com.idega.util.text.Edit;
import com.idega.idegaweb.IWMainApplication;


/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:      idega multimedia
 * @author       <a href="mailto:aron@idega.is">aron@idega.is</a>
 * @version 2.0
 */


public class Reporter extends CategoryBlock implements IWBlock,Reports{

  private final String sAction = "rep_reporter_action";
  private String sActPrm = "";
  private int iAction = 0;
  private static final String prefix = "rep_reporter_";
  private static final String PermissionAdd = "add";
  private static final String PermissionPref = "pref";
  private String sLastOrder = "0";
  private boolean sqlEditAdmin = true;
  boolean newobjinst = false;
  boolean isAdmin = false;
  boolean hasEdit = false,hasPref = false,hasAdd = false;

  protected IWResourceBundle iwrb;
  protected IWBundle iwb;
  private IWBundle core ;
  private Image XLS,PDF,TXT;


  public Reporter(){
    super();
  }
  public Reporter(int iCategory){
    this();
    setCategoryId(iCategory);
  }

  public String getCategoryType(){
    return "reports";
  }

  public boolean getMultible(){
    return true;
  }

  public void setReportCategoryId(int iCategory){
    setCategoryId(iCategory);
  }

  public void setSQLEdit(boolean value){
    sqlEditAdmin = value;
  }

  public void registerPermissionKeys(){
    registerPermissionKey(PermissionAdd);
    registerPermissionKey(PermissionPref);
  }

  protected void control(IWContext iwc){
    XLS = iwb.getImage("/shared/xls.gif");
    PDF = iwb.getImage("/shared/pdf.gif");
    TXT = iwb.getImage("/shared/txt.gif");
    Table T = new Table();
    T.setWidth("100%");
    T.setCellpadding(0);
    T.setCellspacing(0);

    if(isAdmin){
      T.add(getAdminPart(getCategoryId(),false,newobjinst,iwc),1,1);
    }

    Form form = new Form();

    if(iwc.getParameter(sAction) != null){
      sActPrm = iwc.getParameter(sAction);
      try{
        iAction = Integer.parseInt(sActPrm);
        switch(iAction){
          case ACT1:  break;
          case ACT2:  break;
          case ACT3: doChange(iwc); break;
          case ACT4: doDelete(iwc); break;
        }
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }
    T.add(doMain(iwc),1,2);
		form.add(T);
    add(form);
  }

  private PresentationObject getAdminPart(int iCategoryId,boolean enableDelete,boolean newObjInst,IWContext iwc){
    Table T = new Table(3,1);
    T.setCellpadding(2);
    T.setCellspacing(2);
    IWBundle core = iwc.getApplication().getBundle(IW_CORE_BUNDLE_IDENTIFIER);
    if(iCategoryId > 0){
      Link ne = new Link(core.getImage("/shared/create.gif","create"));
      ne.setWindowToOpen(ReportViewWindow.class);
      ne.addParameter(PRM_CATEGORYID,iCategoryId);
      ne.addParameter(ReportViewer.getMenuStartClassParameter(ReportSQLEditor.class));
      //ne.addParameter(ReportSQLEditorWindow.prmReportId,"-1");
      if(hasEdit ||  hasPref || hasAdd ){
        T.add(ne,1,1);
        T.add(T.getTransparentCell(iwc),1,1);
      }

      Link text = new Link(core.getImage("/shared/text.gif","text"));
      text.setWindowToOpen(ReportItemWindow.class);
      text.addParameter(ReportItemWindow.prmCategoryId,iCategoryId);
      //text.addParameter(ReportItemWindow.prmItems,"true");
      if(hasEdit || hasPref){
        T.add(text,1,1);
        T.add(T.getTransparentCell(iwc),1,1);
      }

      Link category = getCategoryLink();
      category.setImage(core.getImage("/shared/detach.gif","detach"));
      if(hasEdit || hasPref)
        T.add(category,1,1);

      if ( hasEdit && enableDelete ) {
        T.add(T.getTransparentCell(iwc),1,1);
        Link delete = new Link(core.getImage("/shared/delete.gif"));
        delete.setWindowToOpen(ReportEditorWindow.class);
        delete.addParameter(ReportEditorWindow.prmDelete,iCategoryId);
        T.add(delete,3,1);
      }
    }
    if(newObjInst){
      Link newLink = new Link(core.getImage("/shared/create.gif"));
      newLink.setWindowToOpen(ReportEditorWindow.class);
      if(newObjInst)
        newLink.addParameter(ReportEditorWindow.prmObjInstId,getICObjectInstanceID());

      T.add(newLink,2,1);
    }
    T.setWidth("100%");
    return T;
  }

  private PresentationObject doMain(IWContext iwc){
    PresentationObject obj = (getCategoryReports(getCategoryId()));
    return obj;
  }

  private PresentationObject getCategoryReports(int iCategoryId){
    ICCategory RC = ReportFinder.getCategory(iCategoryId) ;
    String name = RC!=null?RC.getName():"";
    DataTable T = new DataTable();
      T.setTitlesHorizontal(true);
      T.addTitle(name+iwrb.getLocalizedString("reports","Reports"));
      T.setWidth("100%");

      int a = 1;

      T.add(Edit.formatText(iwrb.getLocalizedString("name","Name")),2,a);
      T.add(Edit.formatText(iwrb.getLocalizedString("info","Info")),3,a);
      T.add(Edit.formatText(iwrb.getLocalizedString("custom","Custom")),4,a);
      T.add(Edit.formatText(iwrb.getLocalizedString("default","Default")),5,a);
      String prm = prefix+"chk";

      if(hasEdit || hasPref){
        T.add(Edit.formatText(iwrb.getLocalizedString("delete","Delete")),6,a);
      }

      List L = ReportEntityHandler.listOfReports(iCategoryId);
      int row = 2;
      if(L!=null){

        int len = L.size();

        for (int i = 0; i < len; i++) {
          Report R = (Report) L.get(i);
          List repinfos = ReportFinder.listOfRelatedReportInfo(R);
          int col = 1;

         // if(sqlEditAdmin){
           // T.add(getAdminLink(R.getID(),iCategoryId),col,row);
         // }
          if(hasEdit || hasPref)
          T.add(getLink(R.getID(),iCategoryId),col,row);
          col++;
          T.add(Edit.formatText(R.getName()),col++,row);
          T.add(Edit.formatText(R.getInfo()),col++,row);
          T.add(getPrintTable(repinfos,R.getID()),col++,row);
          Table def = new Table(3,1);
          def.setCellspacing(2);
          def.add(getXLSLink(XLS,R.getID()),1,1);
          def.add(getTXTLink(TXT,R.getID()),2,1);
          def.add(getPDFLink(PDF,R.getID()),3,1);
          T.add(def,col,row);

          col++;
          if(hasEdit || hasPref )
            T.add(getCheckBox(prm+i,R.getID()),col,row);

            col++;

          row++;
        }
        T.getContentTable().setColumnAlignment(5,"right");
        T.getContentTable().setColumnAlignment(6,"right");
        T.add(new HiddenInput(this.sAction,String.valueOf(this.ACT4)));
        if(hasEdit|| hasPref){
          SubmitButton deleteButtton = new SubmitButton(core.getImage("/shared/delete.gif"));//new Image("/reports/pics/delete.gif"));
          T.add(new HiddenInput(this.sAction,String.valueOf(this.ACT4)));
          T.addButton(deleteButtton);
          HiddenInput countHidden = new HiddenInput(prefix+"count",String.valueOf(len));
          T.add(countHidden);
        }
      }

    return T;
  }

  private Table getPrintTable(List repinfos,int reportId){
    Table T = new Table();
    if(repinfos!=null){
      java.util.Iterator iter = repinfos.iterator();
      ReportInfo info;
      int col = 1;
      while(iter.hasNext()){
        info = (ReportInfo) iter.next();
        T.add(getPrintLink(PDF,reportId,info.getID()),col,1);
        T.add(Edit.formatText(info.getDescription()),col,1);
        T.setColumnAlignment(col,"center");
        col++;
      }
    }
    return T;
  }

  private CheckBox getCheckBox(String name,int id){
    CheckBox chk = new CheckBox(name,String.valueOf(id));
    Edit.setStyle(chk);
    return chk;
  }

  private Link getLink(int id,int catid){
    Link L = new Link(core.getImage("/shared/view.gif"));// Image("/reports/pics/view.gif"));
    L.setWindowToOpen(ReportViewWindow.class);
    L.addParameter(PRM_REPORTID,id);
    L.addParameter(PRM_CATEGORYID,catid);
    L.addParameter(ReportViewer.getMenuStartClassParameter(ReportContentViewer.class));
    return L;
  }
  private Link getAdminLink(int id,int catid){
    Link L = new Link(core.getImage("/shared/edit.gif"));//new Image("/reports/pics/edit.gif"));
    //L.setWindowToOpen(ReportSQLEditorWindow.class);
    //L.addParameter(ReportSQLEditorWindow.prmReportId,id);
    //L.addParameter(ReportSQLEditorWindow.prmCategoryId,catid);
    return L;
  }

  public static Link getPrintLink(Image image,int iReportId,int iReportInfoId){
    Link L = new Link( image );
    L.setWindow(getFileWindow());
    L.addParameter(PRM_REPORTID,iReportId);
    L.addParameter(ReportWriter.prmReportId,iReportId);
    L.addParameter(ReportWriter.prmReportInfoId,iReportInfoId);
    L.addParameter(ReportWriter.PRM_WRITABLE_CLASS,IWMainApplication.getEncryptedClassName(ReportWriter.class));
    return L;
  }

  public static Link getTXTLink(Image image,int iReportId){
    Link L = new Link( image );
    L.setWindow(getFileWindow());
    L.addParameter(PRM_REPORTID,iReportId);
    L.addParameter(ReportWriter.prmReportId,iReportId);
    L.addParameter(ReportWriter.prmPrintType,ReportWriter.TXT);
    L.addParameter(ReportWriter.PRM_WRITABLE_CLASS,IWMainApplication.getEncryptedClassName(ReportWriter.class));
    return L;
  }

 public static Link getPDFLink(Image image,int iReportId){
    Link L = new Link( image );
    L.setWindow(getFileWindow());
    L.addParameter(PRM_REPORTID,iReportId);
    L.addParameter(ReportWriter.prmReportId,iReportId);
    L.addParameter(ReportWriter.prmPrintType,ReportWriter.PDF);
    L.addParameter(ReportWriter.PRM_WRITABLE_CLASS,IWMainApplication.getEncryptedClassName(ReportWriter.class));
    return L;
  }

  public static Link getXLSLink(Image image,int iReportId){
    Link L = new Link( image );
    L.setWindow(getFileWindow());
    L.addParameter(PRM_REPORTID,iReportId);
    L.addParameter(ReportWriter.prmReportId,iReportId);
    L.addParameter(ReportWriter.prmPrintType,ReportWriter.XLS);
    L.addParameter(ReportWriter.PRM_WRITABLE_CLASS,IWMainApplication.getEncryptedClassName(ReportWriter.class));
    return L;
  }

  public static Window getFileWindow(){
    Window w = new Window("Reports","/servlet/MediaServlet");
    w.setResizable(true);
    w.setMenubar(true);
    w.setHeight(400);
    w.setWidth(500);
    return w;
  }
  protected void doChange(IWContext iwc) throws SQLException{

  }
  protected void doUpdate(IWContext iwc) throws SQLException{

  }

  public boolean deleteBlock(int iObjectInstanceId){
    return ReportBusiness.deleteBlock(iObjectInstanceId);
  }

  protected void doDelete(IWContext iwc) throws SQLException{
    String prm = prefix+"chk";
    int count = Integer.parseInt(iwc.getParameter(prefix+"count"));
    for (int i = 0; i < count; i++) {
      if(iwc.getParameter(prm+i)!=null){
        String sId = iwc.getParameter(prm+i);
        try{
          int id = Integer.parseInt(sId);
          ReportBusiness.deleteReport(id);
        }
        catch(NumberFormatException ex){}
      }
    }
  }

  public void main(IWContext iwc){
    isAdmin = iwc.hasEditPermission(this);
    hasEdit = iwc.hasEditPermission(this);
    hasPref = iwc.hasPermission(PermissionPref,this);
    hasAdd = iwc.hasPermission(PermissionAdd,this);
    iwrb = getResourceBundle(iwc);
    iwb = getBundle(iwc);
    core = iwc.getApplication().getBundle(IW_CORE_BUNDLE_IDENTIFIER);
    control(iwc);
  }

  public synchronized Object clone() {
    Reporter obj = null;
    try {
      obj = (Reporter)super.clone();
    }
    catch(Exception ex) {
      ex.printStackTrace(System.err);
    }
    return obj;
  }

  public String getBundleIdentifier(){
    return REPORTS_BUNDLE_IDENTIFIER;
  }


}
