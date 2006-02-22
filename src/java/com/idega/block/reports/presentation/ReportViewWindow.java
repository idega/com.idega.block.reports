package com.idega.block.reports.presentation;

import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.idegaweb.presentation.IWAdminWindow;
import com.idega.presentation.IWContext;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2000-2001 idega.is All Rights Reserved
 * Company:      idega
  *@author <a href="mailto:aron@idega.is">Aron Birkir</a>
 * @version 1.1
 */

public class ReportViewWindow extends IWAdminWindow implements Reports{

  private final static String IW_BUNDLE_IDENTIFIER="com.idega.block.reports";
  protected IWResourceBundle iwrb;
  protected IWBundle iwb;

  public static final  String prmCategoryId = PRM_CATEGORYID;
  public final static String prmReportId = PRM_REPORTID;

  public ReportViewWindow() {
    setWidth(800);
    setHeight(600);
    setResizable( true);
    setMenubar( true);
  }

  public String getBundleIdentifier(){
    return IW_BUNDLE_IDENTIFIER;
  }

  public void main(IWContext iwc) throws Exception{

    iwrb = getResourceBundle(iwc);
    //ReportContentViewer RCV = new ReportContentViewer();

    ReportViewer rv = new ReportViewer();

    rv.setShowLinks(false);
    add(rv);
    String title = iwrb.getLocalizedString("report_viewer","Report Viewer");
    setTitle(title);
    addTitle(title);
    addHeaderObject(rv.getLinks());
  }
}
