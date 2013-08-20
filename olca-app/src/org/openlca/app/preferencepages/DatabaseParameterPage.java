/*******************************************************************************
 * Copyright (c) 2007 - 2010 GreenDeltaTC. All rights reserved. This program and
 * the accompanying materials are made available under the terms of the Mozilla
 * Public License v1.1 which accompanies this distribution, and is available at
 * http://www.openlca.org/uploads/media/MPL-1.1.html
 * 
 * Contributors: GreenDeltaTC - initial API and implementation
 * www.greendeltatc.com tel.: +49 30 4849 6030 mail: gdtc@greendeltatc.com
 ******************************************************************************/
package org.openlca.app.preferencepages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.Section;
import org.openlca.app.Messages;
import org.openlca.app.db.Database;
import org.openlca.app.resources.ImageType;
import org.openlca.app.util.Question;
import org.openlca.app.util.UI;
import org.openlca.core.database.IDatabase;
import org.openlca.core.database.ParameterDao;
import org.openlca.core.model.Parameter;
import org.openlca.core.model.ParameterScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DatabaseParameterPage extends PreferencePage implements
		IWorkbenchPreferencePage {

	private Logger log = LoggerFactory.getLogger(this.getClass());
	private AddParameterAction addParameterAction;
	private IDatabase database;

	private List<Parameter> parameters = new ArrayList<>();

	private RemoveParameterAction removeParameterAction;
	private DatabaseParameterTable parameterTable;

	@Override
	public void init(IWorkbench workbench) {
		log.trace("initialize database parameter page");
		database = Database.get();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		getApplyButton().setVisible(false);
		getDefaultsButton().setVisible(false);
	}

	@Override
	protected Control createContents(final Composite parent) {
		log.trace("create content of database parameter page");
		Composite body = new Composite(parent, SWT.NONE);
		body.setLayout(new GridLayout());
		body.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Section section = new Section(body, ExpandableComposite.NO_TITLE);
		UI.gridData(section, true, true);
		Composite composite = new Composite(section, SWT.NONE);
		UI.gridData(composite, true, true);
		UI.gridLayout(composite, 1);
		section.setClient(composite);

		parameterTable = new DatabaseParameterTable(composite);
		createActions(section);
		if (database != null) {
			loadParameters();
			addParameterAction.setEnabled(true);
			removeParameterAction.setEnabled(true);
			parameterTable.setEnabled(true);
		}
		return parent;
	}

	private void createActions(Section section) {
		addParameterAction = new AddParameterAction();
		removeParameterAction = new RemoveParameterAction();
		final ToolBarManager parametersBar = new ToolBarManager();
		parametersBar.add(addParameterAction);
		parametersBar.add(removeParameterAction);
		section.setTextClient(parametersBar.createControl(section));
		parameterTable.setActions(addParameterAction, removeParameterAction);
		removeParameterAction.setEnabled(false);
		addParameterAction.setEnabled(false);
	}

	private void loadParameters() {
		try {
			ParameterDao dao = new ParameterDao(database);
			List<Parameter> parameters = dao.getGlobalParameters();
			this.parameters.clear();
			for (Parameter parameter : parameters) {
				this.parameters.add(parameter);
			}
			parameterTable.setInput(this.parameters);
		} catch (final Exception e) {
			log.error("Loading database parameters failed", e);
		}
	}

	@Override
	public String getTitle() {
		return Messages.GlobalParametersPreferencePage_Title;
	}

	@Override
	public boolean performOk() {
		if (database == null)
			return true;
		boolean b = Question.ask("Save changes?",
				"Do you want to save the changes?");
		if (!b)
			return true;
		try {
			ParameterDao dao = new ParameterDao(database);
			for (Parameter p : parameters) {
				if (p.getId() > 0L)
					dao.update(p);
				else
					dao.insert(p);
			}
			return true;
		} catch (Exception e) {
			log.error("failed to save database parameters", e);
			return false;
		}

	}

	private class AddParameterAction extends Action {

		public AddParameterAction() {
			setText(NLS.bind(Messages.AddAction_Text, Messages.Parameter));
			setImageDescriptor(ImageType.ADD_ICON.getDescriptor());
			setDisabledImageDescriptor(ImageType.ADD_ICON_DISABLED
					.getDescriptor());
		}

		@Override
		public void run() {
			Parameter parameter = new Parameter();
			parameter.setScope(ParameterScope.GLOBAL);
			String name = "p" + parameters.size();
			parameter.setName(name);
			parameters.add(parameter);
			parameter.setValue(1.0);
			parameterTable.setInput(parameters);
		}
	}

	private class RemoveParameterAction extends Action {

		public RemoveParameterAction() {
			setText(NLS.bind(Messages.RemoveAction_Text, Messages.Parameter));
			setImageDescriptor(ImageType.DELETE_ICON.getDescriptor());
			setDisabledImageDescriptor(ImageType.DELETE_ICON_DISABLED
					.getDescriptor());
		}

		@Override
		public void run() {
			Parameter parameter = parameterTable.getSelected();
			if (parameter == null)
				return;
			boolean b = Question.ask(
					"Delete paramater",
					"Do you really want to delete parameter "
							+ parameter.getName() + " from the database?");
			if (!b)
				return;
			tryDelete(parameter);
		}

		private void tryDelete(Parameter parameter) {
			try {
				if (parameter.getId() > 0L) {
					ParameterDao dao = new ParameterDao(database);
					dao.delete(parameter);
				}
				parameters.remove(parameter);
				parameterTable.setInput(parameters);
			} catch (Exception e) {
				log.error("failed to delete parameter " + parameter, e);
			}
		}
	}

}