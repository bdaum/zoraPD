/*
 * This file is part of the ZoRa project: http://www.photozora.org.
 *
 * ZoRa is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * ZoRa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ZoRa; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2009 Berthold Daum  (berthold.daum@bdaum.de)
 */

package com.bdaum.zoom.ui.internal.html;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IUndoManager;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;


public class XMLViewerConfiguration extends SourceViewerConfiguration {

    /**
     * @param sourceViewer
     * @param codeScanner
     * @param contentAssistant
     * @param undoManager
     */
    public XMLViewerConfiguration(SourceViewer sourceViewer,
            ITokenScanner codeScanner,
            IContentAssistProcessor contentAssistant, IUndoManager undoManager) {
        this.sourceViewer = sourceViewer;
        this.codeScanner = codeScanner;
        this.contentAssistant = contentAssistant;
        this.undoManager = undoManager;
    }
    private SourceViewer sourceViewer;
    private ITokenScanner codeScanner;
    private IContentAssistProcessor contentAssistant;
    private IUndoManager undoManager;
    
	@Override
	public IPresentationReconciler getPresentationReconciler(
            ISourceViewer viewer) {
        if (codeScanner == null) {
            return super.getPresentationReconciler(sourceViewer);
        }
        PresentationReconciler reconciler = new PresentationReconciler();
        DefaultDamagerRepairer dr = new DefaultDamagerRepairer(codeScanner);
        reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
        reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
        return reconciler;
    }
    
	@Override
	public IContentAssistant getContentAssistant(ISourceViewer viewer) {
        if (contentAssistant == null) {
            return super.getContentAssistant(viewer);
        }
        ContentAssistant assistant = new ContentAssistant();
        assistant.setContentAssistProcessor(contentAssistant,
                IDocument.DEFAULT_CONTENT_TYPE);
        assistant.enableAutoActivation(true);
        assistant.setAutoActivationDelay(500);
        return assistant;
    }
    
	@Override
	public IUndoManager getUndoManager(ISourceViewer viewer) {
        return undoManager;
    }

}