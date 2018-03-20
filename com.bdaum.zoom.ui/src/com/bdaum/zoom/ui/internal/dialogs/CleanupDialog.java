package com.bdaum.zoom.ui.internal.dialogs;

import java.util.List;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import com.bdaum.aoModeling.runtime.IIdentifiableObject;
import com.bdaum.zoom.cat.model.artworkOrObjectShown.ArtworkOrObject;
import com.bdaum.zoom.cat.model.creatorsContact.Contact;
import com.bdaum.zoom.css.ZColumnLabelProvider;
import com.bdaum.zoom.ui.dialogs.ZTitleAreaDialog;
import com.bdaum.zoom.ui.internal.ZViewerComparator;

public class CleanupDialog extends ZTitleAreaDialog {

	private List<IIdentifiableObject> objects;

	public CleanupDialog(Shell parentShell, List<IIdentifiableObject> objects) {
		super(parentShell);
		this.objects = objects;
	}

	@Override
	public void create() {
		super.create();
		IIdentifiableObject ob = objects.get(0);
		String type;
		if (ob instanceof ArtworkOrObject)
			type = Messages.CleanupDialog_artworks;
		else if (ob instanceof Contact)
			type = Messages.CleanupDialog_contacts;
		else
			type = Messages.CleanupDialog_locations;
		setTitle(NLS.bind(Messages.CleanupDialog_unused_items, type));
		setMessage(NLS.bind(Messages.CleanupDialog_unused_msg, type));
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite composite = new Composite(area, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(1, false));
		TableViewer viewer = new TableViewer(composite, SWT.V_SCROLL | SWT.BORDER);
		viewer.getControl().setLayoutData(new GridData(500, 300));
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new ZColumnLabelProvider() {
			@Override
			public String getText(Object element) {
				String structText = StructComponent.getStructText(element, null);
				return (structText != null) ? structText : element.toString();
			}
		});
		viewer.setComparator(ZViewerComparator.INSTANCE);
		viewer.setInput(objects);
		return area;
	}

}
