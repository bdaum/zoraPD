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
 * (c) 2011 Berthold Daum  
 */
package com.bdaum.zoom.ui.internal.codes;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.runtime.Platform;
import org.eclipse.osgi.util.NLS;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.bdaum.zoom.core.Constants;
import com.bdaum.zoom.core.Core;
import com.bdaum.zoom.core.QueryField;
import com.bdaum.zoom.core.internal.Utilities;
import com.bdaum.zoom.ui.internal.UiActivator;
import com.bdaum.zoom.ui.preferences.PreferenceConstants;
import com.ibm.icu.util.StringTokenizer;

@SuppressWarnings("restriction")
public class CodeParser {

	private static final String DATE_AND_TIME = "DateAndTime"; //$NON-NLS-1$
	private static final String SUBJECT_DETAIL = "SubjectDetail"; //$NON-NLS-1$
	private static final String SUBJECT_MATTER = "SubjectMatter"; //$NON-NLS-1$
	private static final String CODEFOLDER = "codes/XML/"; //$NON-NLS-1$
	private static final String TOPIC = "Topic"; //$NON-NLS-1$
	private static final String FORMALNAME = "FormalName"; //$NON-NLS-1$
	private static final String DESCRIPTION = "Description"; //$NON-NLS-1$
	private static final String VARIANT = "Variant"; //$NON-NLS-1$
	private static final String NAME = "Name"; //$NON-NLS-1$
	private static final String EXPLANATION = "Explanation"; //$NON-NLS-1$
	private static final String TOPICTYPE = "TopicType"; //$NON-NLS-1$
	private static final String SUBJECT = "Subject"; //$NON-NLS-1$
	private static final String SCENE = "Scene"; //$NON-NLS-1$
	private static final String GENRE = "Genre"; //$NON-NLS-1$

	private SAXParser saxParser;
	private String filename;
	private File file;
	private boolean done;
	private String title = ""; //$NON-NLS-1$
	private String msg = ""; //$NON-NLS-1$
	private Date dateAndTime;
	private Map<String, Topic> topicMap;
	private LinkedList<Topic> recentTopics = new LinkedList<Topic>();
	private List<Topic> roots = new ArrayList<Topic>(100);
	private Set<String> customTopics = new HashSet<>();
	private Topic[] hierarchy;
	private String[] topicType;
	private boolean subtopics = false;
	private boolean byName = false;
	private boolean allowCustomCode = false;
	private String id;

	public CodeParser(int type) {
		switch (type) {
		case QueryField.SUBJECTCODES:
			id = PreferenceConstants.V_SUBJECT;
			filename = "topicset.iptc-subjectcode.xml"; //$NON-NLS-1$
			title = Messages.CodeParser_subject_codes;
			msg = Messages.CodeParser_subject_codes_msg + '\n' + Messages.CodeParser_revision + '\n'
					+ Messages.CodeParser_custom_defined;
			topicMap = new HashMap<String, Topic>(2250);
			topicType = new String[] { SUBJECT, SUBJECT_MATTER, SUBJECT_DETAIL };
			hierarchy = new Topic[3];
			allowCustomCode = true;
			break;
		case QueryField.SCENECODES:
			id = PreferenceConstants.V_SCENE;
			filename = "topicset.iptc-scene.xml"; //$NON-NLS-1$
			title = Messages.CodeParser_scene_codes;
			msg = Messages.CodeParser_scene_codes_msg + '\n' + Messages.CodeParser_revision + '\n'
					+ Messages.CodeParser_custom_defined;
			topicMap = new HashMap<String, Topic>(150);
			topicType = new String[] { SCENE };
			hierarchy = new Topic[1];
			allowCustomCode = true;
			break;
		case QueryField.GENRECODES:
			id = PreferenceConstants.V_GENRE;
			filename = "topicset.iptc-genre.xml"; //$NON-NLS-1$
			title = Messages.CodeParser_genre;
			msg = Messages.CodeParser_genre_expl + '\n' + Messages.CodeParser_revision + '\n'
					+ Messages.CodeParser_custom_defined;
			topicMap = new HashMap<String, Topic>(150);
			topicType = new String[] { GENRE };
			hierarchy = new Topic[1];
			allowCustomCode = byName = true;
			break;
		}
		if (id != null) {
			String topics = UiActivator.getDefault().getPreferenceStore().getString(id);
			if (topics != null) {
				StringTokenizer st = new StringTokenizer(topics, "\n"); //$NON-NLS-1$
				while (st.hasMoreTokens())
					customTopics.add(st.nextToken());
			}
			File installFolder = new File(Platform.getInstallLocation().getURL().getPath());
			file = new File(installFolder.getParent(), CODEFOLDER + filename);
			if (!file.exists())
				file = new File(installFolder, CODEFOLDER + filename);
			if (file.exists()) {
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setValidating(false);
				try {
					saxParser = factory.newSAXParser();
				} catch (Exception e) {
					UiActivator.getDefault().logError(Messages.CodeParser_error_creating_code_parser, e);
				}
			} else
				UiActivator.getDefault().logError(NLS.bind(Messages.CodeParser_code_catalog_not_found, filename), null);
			if (saxParser == null) {
				title = Messages.CodeParser_cat_not_found;
				msg = NLS.bind(Messages.CodeParser_code_catalog_not_found, filename);
			}
		}
	}

	public boolean canParse() {
		return saxParser != null;
	}

	public boolean hasSubtopics() {
		return subtopics;
	}

	public Topic[] loadCodes() {
		doLoad();
		return roots.toArray(new Topic[roots.size()]);
	}

	private void doLoad() {
		if (!done) {
			try {
				if (saxParser != null)
					saxParser.parse(file, getHandler());
			} catch (SAXException e) {
				UiActivator.getDefault().logError(NLS.bind(Messages.CodeParser_sax_parsing_exceptiion, file), e);
			} catch (IOException e) {
				UiActivator.getDefault().logError(NLS.bind(Messages.CodeParser_io_exception, file), e);
			}
			done = true;
		}
	}

	protected DefaultHandler getHandler() {
		DefaultHandler handler = new DefaultHandler() {
			private boolean topc = false;
			private boolean name = false;
			private boolean explanation = false;
			private Topic topic;
			private final StringBuilder text = new StringBuilder(128);
			
			@Override
			public void startElement(String namespaceURI, String localName, String qName, Attributes atts)
					throws SAXException {
				if (TOPIC.equals(qName)) {
					topc = true;
					topic = new Topic();
				} else if (topc) {
					if (DESCRIPTION.equals(qName)) {
						String variant = atts.getValue(VARIANT);
						if (NAME.equals(variant))
							name = true;
						else if (EXPLANATION.equals(variant))
							explanation = true;
					} else if (TOPICTYPE.equals(qName)) {
						String fname = atts.getValue(FORMALNAME);
						for (int i = 0; i < topicType.length; i++)
							if (topicType[i].equals(fname)) {
								hierarchy[i] = topic;
								if (i == 0)
									roots.add(topic);
								else {
									hierarchy[i - 1].addSubTopic(topic);
									subtopics = true;
								}
							}
					}
				}
				text.setLength(0);
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				text.append(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String qName) throws SAXException {
				if (topc) {
					if (TOPIC.equals(qName)) {
						topicMap.put(topic.getCode(), topic);
						topc = false;
					} else if (FORMALNAME.equals(qName))
						topic.setCode(text.toString());
					else if (DESCRIPTION.equals(qName))
						if (name) {
							topic.setName(text.toString());
							name = false;
						} else if (explanation) {
							topic.setDescription(text.toString());
							explanation = false;
						}
				} else if (DATE_AND_TIME.equals(qName))
					try {
						dateAndTime = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ").parse(text.toString()); //$NON-NLS-1$
					} catch (ParseException e) {
						// do nothing
					}
			}
		};
		return handler;
	}

	/**
	 * @return the title
	 */
	public String getTitle() {
		doLoad();
		return title;
	}

	/**
	 * @return the msg
	 */
	public String getMessage() {
		doLoad();
		return NLS.bind(msg, dateAndTime == null ? Messages.CodeParser_unknown : Constants.DFDT.format(dateAndTime));
	}

	public Topic findTopic(String code) {
		doLoad();
		Topic topic = topicMap.get(code);
		if (topic != null)
			addRecentTopic(topic);
		return topic;
	}

	private void addRecentTopic(Topic topic) {
		Iterator<Topic> iterator = recentTopics.iterator();
		while (iterator.hasNext())
			if (iterator.next().equals(topic)) {
				iterator.remove();
				break;
			}
		recentTopics.add(0, topic);
	}

	public Topic[] getRecentTopics() {
		return recentTopics.toArray(new Topic[recentTopics.size()]);
	}

	public boolean isByName() {
		return byName;
	}

	public boolean hasRecentTopics() {
		return !recentTopics.isEmpty();
	}

	public boolean isAllowCustomCode() {
		return allowCustomCode;
	}

	public void addCustomTopic(String t) {
		customTopics.add(t);
		addRecentTopic(new Topic(t, t, Messages.CodeParser_user_defined));
		UiActivator.getDefault().getPreferenceStore().putValue(id, Core.toStringList(getCustomTopics(), "\n")); //$NON-NLS-1$
	}

	public String[] getCustomTopics() {
		String[] topics = customTopics.toArray(new String[customTopics.size()]);
		Arrays.sort(topics, Utilities.KEYWORDCOMPARATOR);
		return topics;
	}

}
