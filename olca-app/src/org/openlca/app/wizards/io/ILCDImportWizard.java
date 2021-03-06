package org.openlca.app.wizards.io;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IImportWizard;
import org.eclipse.ui.IWorkbench;
import org.openlca.app.App;
import org.openlca.app.Messages;
import org.openlca.app.db.Cache;
import org.openlca.app.db.Database;
import org.openlca.app.navigation.Navigator;
import org.openlca.app.rcp.ImageType;
import org.openlca.io.ilcd.ILCDImport;

/**
 * Import wizard for the import of a set of ILCD files.
 */
public class ILCDImportWizard extends Wizard implements IImportWizard {

	private FileImportPage importPage;

	public ILCDImportWizard() {
		setNeedsProgressMonitor(true);
	}

	@Override
	public void addPages() {
		importPage = new FileImportPage(new String[] { "zip" }, false);
		addPage(importPage);
	}

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		setWindowTitle(Messages.ImportILCD);
		setDefaultPageImageDescriptor(ImageType.IMPORT_ZIP_WIZARD
				.getDescriptor());
	}

	@Override
	public boolean performFinish() {
		final File zip = getZip();
		if (zip == null)
			return false;
		try {
			doRun(zip);
			return true;
		} catch (final Exception e) {
			return false;
		} finally {
			Navigator.refresh();
			Cache.evictAll();
		}
	}

	private File getZip() {
		File[] files = importPage.getFiles();
		if (files.length > 0)
			return files[0];
		return null;
	}

	private void doRun(final File zip) throws Exception {
		getContainer().run(true, true, new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask(Messages.Import, IProgressMonitor.UNKNOWN);
				ImportHandler handler = new ImportHandler(monitor);
				ILCDImport iImport = new ILCDImport(zip, Database.get());
				if (App.runsInDevMode())
					iImport.setImportFlows(true);
				handler.run(iImport);
			}
		});
	}

}
