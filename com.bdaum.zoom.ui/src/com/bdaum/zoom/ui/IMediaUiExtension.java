package com.bdaum.zoom.ui;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.action.IMenuManager;

import com.bdaum.zoom.ui.internal.views.ImageView;

public interface IMediaUiExtension {

	void setMediaSupportId(String ref);

	void addMediaContributions(IMenuManager manager, String anchor, IAdaptable info);

	void registerMediaContributions(ImageView imageView);

	void updateMediaContributions(ImageView imageView, int count, int localCount, AssetSelection assetSelection);

}
