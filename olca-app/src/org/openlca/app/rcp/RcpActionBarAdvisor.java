package org.openlca.app.rcp;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.ICoolBarManager;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarContributionItem;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.ActionFactory.IWorkbenchAction;
import org.eclipse.ui.actions.ContributionItemFactory;
import org.eclipse.ui.application.ActionBarAdvisor;
import org.eclipse.ui.application.IActionBarConfigurer;
import org.openlca.app.Config;
import org.openlca.app.Messages;
import org.openlca.app.devtools.js.JavaScriptEditor;
import org.openlca.app.devtools.python.PythonEditor;
import org.openlca.app.devtools.sql.SqlEditor;
import org.openlca.app.editors.StartPage;
import org.openlca.app.rcp.browser.MozillaConfigView;
import org.openlca.app.rcp.plugins.PluginManagerDialog;
import org.openlca.app.util.Actions;
import org.openlca.app.util.Desktop;
import org.openlca.app.util.UI;

public class RcpActionBarAdvisor extends ActionBarAdvisor {

	private IWorkbenchAction aboutAction;
	private IWorkbenchAction closeAction;
	private IWorkbenchAction closeAllAction;
	private IWorkbenchAction exitAction;
	private IWorkbenchAction exportAction;

	private IWorkbenchAction importAction;
	private IWorkbenchAction newWindowAction;
	private IWorkbenchAction preferencesAction;
	private IWorkbenchAction saveAction;
	private IWorkbenchAction saveAllAction;
	private IWorkbenchAction saveAsAction;
	private IContributionItem showViews;

	public RcpActionBarAdvisor(IActionBarConfigurer configurer) {
		super(configurer);
	}

	@Override
	protected void fillCoolBar(ICoolBarManager coolBar) {
		IToolBarManager toolbar = new ToolBarManager(SWT.FLAT | SWT.LEFT);
		coolBar.add(new ToolBarContributionItem(toolbar, "main"));
		toolbar.add(new HomeAction());
		toolbar.add(saveAction);
		toolbar.add(saveAsAction);
		toolbar.add(saveAllAction);
	}

	@Override
	protected void fillMenuBar(IMenuManager menuBar) {
		super.fillMenuBar(menuBar);
		fillFileMenu(menuBar);
		fillWindowMenu(menuBar);
		fillHelpMenu(menuBar);
	}

	private void fillHelpMenu(IMenuManager menuBar) {
		MenuManager helpMenu = new MenuManager(Messages.Help,
				IWorkbenchActionConstants.M_HELP);
		HelpAction helpAction = new HelpAction();
		helpMenu.add(helpAction);
		helpMenu.add(new Separator());
		// Plugin manager can be shown if version check is implemented correctly
		// helpMenu.add(new PluginAction());
		helpMenu.add(aboutAction);
		menuBar.add(helpMenu);
	}

	private void fillFileMenu(IMenuManager menuBar) {
		MenuManager fileMenu = new MenuManager(Messages.File,
				IWorkbenchActionConstants.M_FILE);
		fileMenu.add(saveAction);
		fileMenu.add(saveAsAction);
		fileMenu.add(saveAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(closeAction);
		fileMenu.add(closeAllAction);
		fileMenu.add(new Separator());
		fileMenu.add(preferencesAction);
		fileMenu.add(new Separator());
		fileMenu.add(importAction);
		fileMenu.add(exportAction);
		fileMenu.add(new Separator());
		fileMenu.add(exitAction);
		menuBar.add(fileMenu);
	}

	private void fillWindowMenu(IMenuManager menuBar) {
		MenuManager windowMenu = new MenuManager(Messages.Window,
				IWorkbenchActionConstants.M_WINDOW);
		windowMenu.add(newWindowAction);
		MenuManager viewMenu = new MenuManager(Messages.Showviews);
		viewMenu.add(showViews);
		windowMenu.add(viewMenu);
		createDeveloperMenu(windowMenu);
		windowMenu.add(new FormulaConsoleAction());
		if (MozillaConfigView.canShow()) {
			windowMenu.add(Actions.create(Messages.BrowserConfiguration,
					ImageType.FIREFOX_ICON.getDescriptor(),
					MozillaConfigView::open));
		}
		menuBar.add(windowMenu);
	}

	private void createDeveloperMenu(MenuManager windowMenu) {
		windowMenu.add(new Separator());
		MenuManager devMenu = new MenuManager(Messages.DeveloperTools);
		windowMenu.add(devMenu);
		devMenu.add(Actions.create("SQL",
				ImageType.SQL_ICON.getDescriptor(),
				SqlEditor::open));
		devMenu.add(Actions.create("JavaScript",
				ImageType.JAVASCRIPT_ICON.getDescriptor(),
				JavaScriptEditor::open));
		devMenu.add(Actions.create("Python",
				ImageType.PYTHON_ICON.getDescriptor(),
				PythonEditor::open));
		windowMenu.add(new Separator());
	}

	@Override
	protected void makeActions(final IWorkbenchWindow window) {
		saveAction = ActionFactory.SAVE.create(window);
		saveAsAction = ActionFactory.SAVE_AS.create(window);
		saveAllAction = ActionFactory.SAVE_ALL.create(window);
		closeAction = ActionFactory.CLOSE.create(window);
		closeAllAction = ActionFactory.CLOSE_ALL.create(window);
		preferencesAction = ActionFactory.PREFERENCES.create(window);
		importAction = ActionFactory.IMPORT.create(window);
		exportAction = ActionFactory.EXPORT.create(window);
		exitAction = ActionFactory.QUIT.create(window);
		newWindowAction = ActionFactory.OPEN_NEW_WINDOW.create(window);
		showViews = ContributionItemFactory.VIEWS_SHORTLIST.create(window);
		aboutAction = ActionFactory.ABOUT.create(window);
	}

	private class HelpAction extends Action {
		public HelpAction() {
			setText(Messages.OnlineHelp);
			setToolTipText(Messages.OnlineHelp);
			setImageDescriptor(ImageType.HELP_ICON.getDescriptor());
		}

		@Override
		public void run() {
			Desktop.browse(Config.HELP_URL);
		}
	}

	private class PluginAction extends Action {
		public PluginAction() {
			setText("Plugins");
			setImageDescriptor(ImageType.LOGO_16_32.getDescriptor());
		}

		@Override
		public void run() {
			PluginManagerDialog dialog = new PluginManagerDialog(UI.shell());
			dialog.create();
			dialog.getShell().setSize(500, 600);
			dialog.open();
		}
	}

	private class HomeAction extends Action {
		public HomeAction() {
			setImageDescriptor(ImageType.HOME_ICON.getDescriptor());
			setText(Messages.Home);
		}

		@Override
		public void run() {
			StartPage.open();
		}
	}

}
