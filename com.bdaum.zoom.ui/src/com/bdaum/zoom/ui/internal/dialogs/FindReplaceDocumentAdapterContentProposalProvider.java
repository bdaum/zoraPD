/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Cagatay Calli <ccalli@gmail.com> - [find/replace] retain caps when replacing - https://bugs.eclipse.org/bugs/show_bug.cgi?id=28949
 *     Berthold Daum - paramaterized for full or restricted set
 *******************************************************************************/
package com.bdaum.zoom.ui.internal.dialogs;

import java.util.ArrayList;

import org.eclipse.jface.fieldassist.ContentProposal;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalProvider;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;

/**
 * Content assist proposal provider for the {@link FindReplaceDocumentAdapter}.
 * <p>
 * Clients can subclass to provide additional proposals in case they are
 * supported by their own find/replace mechanism.
 * </p>
 * <p>
 * <strong>Note:</strong> Clients must not make any assumptions about the
 * returned proposals. This can change from release to release to adapt to
 * changes made in {@link FindReplaceDocumentAdapter}.
 * </p>
 *
 * @since 3.4
 */
public class FindReplaceDocumentAdapterContentProposalProvider implements IContentProposalProvider {

	/**
	 * Proposal computer.
	 */
	private static class ProposalComputer {

		/**
		 * The whole regular expression.
		 */
		private final String fExpression;
		/**
		 * The document offset.
		 */
		private final int fDocumentOffset;
		/**
		 * The high-priority proposals.
		 */
		private final ArrayList<ContentProposal> fPriorityProposals;
		/**
		 * The low-priority proposals.
		 */
		private final ArrayList<ContentProposal> fProposals;
		/**
		 * <code>true</code> iff <code>fExpression</code> ends with an open
		 * escape.
		 */
		private final boolean fIsEscape;
		private boolean full;

		/**
		 * Creates a new Proposal Computer.
		 *
		 * @param contents
		 *            the contents of the subject control
		 * @param position
		 *            the cursor position
		 * @param full
		 */
		public ProposalComputer(String contents, int position, boolean full) {
			fExpression = contents;
			fDocumentOffset = position;
			this.full = full;
			fPriorityProposals = new ArrayList<ContentProposal>();
			fProposals = new ArrayList<ContentProposal>();

			boolean isEscape = false;
			esc: for (int i = position - 1; i >= 0; i--) {
				if (fExpression.charAt(i) == '\\')
					isEscape = !isEscape;
				else
					break esc;
			}
			fIsEscape = isEscape;
		}

		/**
		 * Computes applicable proposals for the find field.
		 *
		 * @return the proposals
		 */
		public IContentProposal[] computeFindProposals() {
			// characters
			addBsProposal("\\\\", RegExMessages.getString("displayString_bs_bs"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_bs")); //$NON-NLS-1$
			if (full) {
				addBracketProposal("\\0", 2, RegExMessages.getString("displayString_bs_0"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_0")); //$NON-NLS-1$
				addBracketProposal("\\x", 2, RegExMessages.getString("displayString_bs_x"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_x")); //$NON-NLS-1$
				addBracketProposal("\\u", 2, RegExMessages.getString("displayString_bs_u"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_u")); //$NON-NLS-1$
				addBsProposal("\\t", RegExMessages.getString("displayString_bs_t"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_t")); //$NON-NLS-1$
				addBsProposal("\\R", RegExMessages.getString("displayString_bs_R"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_R")); //$NON-NLS-1$
				addBsProposal("\\n", RegExMessages.getString("displayString_bs_n"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_n")); //$NON-NLS-1$
				addBsProposal("\\r", RegExMessages.getString("displayString_bs_r"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_r")); //$NON-NLS-1$
				addBsProposal("\\f", RegExMessages.getString("displayString_bs_f"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_f")); //$NON-NLS-1$
				addBsProposal("\\a", RegExMessages.getString("displayString_bs_a"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_a")); //$NON-NLS-1$
				addBsProposal("\\e", RegExMessages.getString("displayString_bs_e"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_e")); //$NON-NLS-1$
				addBracketProposal("\\c", 2, RegExMessages.getString("displayString_bs_c"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_c")); //$NON-NLS-1$
			}
			if (!fIsEscape)
				addBracketProposal(".", 1, RegExMessages.getString("displayString_dot"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_dot")); //$NON-NLS-1$
			addBsProposal("\\d", RegExMessages.getString("displayString_bs_d"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_d")); //$NON-NLS-1$
			addBsProposal("\\D", RegExMessages.getString("displayString_bs_D"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_D")); //$NON-NLS-1$
			addBsProposal("\\s", RegExMessages.getString("displayString_bs_s"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_s")); //$NON-NLS-1$
			addBsProposal("\\S", RegExMessages.getString("displayString_bs_S"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_S")); //$NON-NLS-1$
			addBsProposal("\\w", RegExMessages.getString("displayString_bs_w"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_w")); //$NON-NLS-1$
			addBsProposal("\\W", RegExMessages.getString("displayString_bs_W"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_W")); //$NON-NLS-1$

			// back reference
			addBsProposal("\\", RegExMessages.getString("displayString_bs_i"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_i")); //$NON-NLS-1$

			// quoting
			addBsProposal("\\", RegExMessages.getString("displayString_bs"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs")); //$NON-NLS-1$
			addBsProposal("\\Q", RegExMessages.getString("displayString_bs_Q"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_Q")); //$NON-NLS-1$
			addBsProposal("\\E", RegExMessages.getString("displayString_bs_E"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_E")); //$NON-NLS-1$

			// character sets
			if (!fIsEscape) {
				addBracketProposal("[]", 1, RegExMessages.getString("displayString_set"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_set")); //$NON-NLS-1$
				addBracketProposal("[^]", 2, RegExMessages.getString("displayString_setExcl"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_setExcl")); //$NON-NLS-1$
				addBracketProposal("[-]", 1, RegExMessages.getString("displayString_setRange"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_setRange")); //$NON-NLS-1$
				addProposal("&&", RegExMessages.getString("displayString_setInter"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_setInter")); //$NON-NLS-1$
			}
			if (!fIsEscape && fDocumentOffset > 0 && fExpression.charAt(fDocumentOffset - 1) == '\\') {
				addProposal("\\p{}", 3, RegExMessages.getString("displayString_posix"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_posix")); //$NON-NLS-1$
				addProposal("\\P{}", 3, RegExMessages.getString("displayString_posixNot"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_posixNot")); //$NON-NLS-1$
			} else {
				addBracketProposal("\\p{}", 3, RegExMessages.getString("displayString_posix"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_posix")); //$NON-NLS-1$
				addBracketProposal("\\P{}", 3, RegExMessages.getString("displayString_posixNot"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_posixNot")); //$NON-NLS-1$
			}

			// boundary matchers
			if (full) {
				if (fDocumentOffset == 0) {
					addPriorityProposal("^", RegExMessages.getString("displayString_start"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_start")); //$NON-NLS-1$
				} else if (fDocumentOffset == 1 && fExpression.charAt(0) == '^') {
					addBracketProposal("^", 1, RegExMessages.getString("displayString_start"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_start")); //$NON-NLS-1$
				}
				if (fDocumentOffset == fExpression.length()) {
					addProposal("$", RegExMessages.getString("displayString_end"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_end")); //$NON-NLS-1$
				}
			}
			addBsProposal("\\b", RegExMessages.getString("displayString_bs_b"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_b")); //$NON-NLS-1$
			addBsProposal("\\B", RegExMessages.getString("displayString_bs_B"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_B")); //$NON-NLS-1$
			addBsProposal("\\A", RegExMessages.getString("displayString_bs_A"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_A")); //$NON-NLS-1$
			addBsProposal("\\G", RegExMessages.getString("displayString_bs_G"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_G")); //$NON-NLS-1$
			if (full)
				addBsProposal("\\Z", RegExMessages.getString("displayString_bs_Z"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_bs_Z")); //$NON-NLS-1$
			addBsProposal("\\z", RegExMessages.getString("displayString_bs_z"), //$NON-NLS-1$ //$NON-NLS-2$
					RegExMessages.getString("additionalInfo_bs_z")); //$NON-NLS-1$

			if (!fIsEscape) {
				// capturing groups
				addBracketProposal("()", 1, RegExMessages.getString("displayString_group"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_group")); //$NON-NLS-1$

				// flags
				addBracketProposal("(?)", 2, RegExMessages.getString("displayString_flag"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_flag")); //$NON-NLS-1$
				addBracketProposal("(?:)", 3, RegExMessages.getString("displayString_flagExpr"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_flagExpr")); //$NON-NLS-1$

				// non-capturing group
				addBracketProposal("(?:)", 3, RegExMessages.getString("displayString_nonCap"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_nonCap")); //$NON-NLS-1$
				addBracketProposal("(?>)", 3, RegExMessages.getString("displayString_atomicCap"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_atomicCap")); //$NON-NLS-1$

				// look around
				addBracketProposal("(?=)", 3, RegExMessages.getString("displayString_posLookahead"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_posLookahead")); //$NON-NLS-1$
				addBracketProposal("(?!)", 3, RegExMessages.getString("displayString_negLookahead"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_negLookahead")); //$NON-NLS-1$
				addBracketProposal("(?<=)", 4, RegExMessages.getString("displayString_posLookbehind"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_posLookbehind")); //$NON-NLS-1$
				addBracketProposal("(?<!)", 4, RegExMessages.getString("displayString_negLookbehind"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_negLookbehind")); //$NON-NLS-1$

				// greedy quantifiers
				addBracketProposal("?", 1, RegExMessages.getString("displayString_quest"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_quest")); //$NON-NLS-1$
				addBracketProposal("*", 1, RegExMessages.getString("displayString_star"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_star")); //$NON-NLS-1$
				addBracketProposal("+", 1, RegExMessages.getString("displayString_plus"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_plus")); //$NON-NLS-1$
				addBracketProposal("{}", 1, RegExMessages.getString("displayString_exact"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_exact")); //$NON-NLS-1$
				addBracketProposal("{,}", 1, RegExMessages.getString("displayString_least"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_least")); //$NON-NLS-1$
				addBracketProposal("{,}", 1, RegExMessages.getString("displayString_count"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_count")); //$NON-NLS-1$

				// lazy quantifiers
				addBracketProposal("??", 1, RegExMessages.getString("displayString_questLazy"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_questLazy")); //$NON-NLS-1$
				addBracketProposal("*?", 1, RegExMessages.getString("displayString_starLazy"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_starLazy")); //$NON-NLS-1$
				addBracketProposal("+?", 1, RegExMessages.getString("displayString_plusLazy"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_plusLazy")); //$NON-NLS-1$
				addBracketProposal("{}?", 1, RegExMessages.getString("displayString_exactLazy"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_exactLazy")); //$NON-NLS-1$
				addBracketProposal("{,}?", 1, RegExMessages.getString("displayString_leastLazy"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_leastLazy")); //$NON-NLS-1$
				addBracketProposal("{,}?", 1, RegExMessages.getString("displayString_countLazy"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_countLazy")); //$NON-NLS-1$

				// possessive quantifiers
				addBracketProposal("?+", 1, RegExMessages.getString("displayString_questPoss"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_questPoss")); //$NON-NLS-1$
				addBracketProposal("*+", 1, RegExMessages.getString("displayString_starPoss"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_starPoss")); //$NON-NLS-1$
				addBracketProposal("++", 1, RegExMessages.getString("displayString_plusPoss"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_plusPoss")); //$NON-NLS-1$
				addBracketProposal("{}+", 1, RegExMessages.getString("displayString_exactPoss"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_exactPoss")); //$NON-NLS-1$
				addBracketProposal("{,}+", 1, RegExMessages.getString("displayString_leastPoss"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_leastPoss")); //$NON-NLS-1$
				addBracketProposal("{,}+", 1, RegExMessages.getString("displayString_countPoss"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_countPoss")); //$NON-NLS-1$

				// alternative
				addBracketProposal("|", 1, RegExMessages.getString("displayString_alt"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_alt")); //$NON-NLS-1$
			}

			fPriorityProposals.addAll(fProposals);
			return fPriorityProposals.toArray(new IContentProposal[fProposals.size()]);
		}

		/**
		 * Computes applicable proposals for the replace field.
		 *
		 * @return the proposals
		 */
		public IContentProposal[] computeReplaceProposals() {
			if (fDocumentOffset > 0 && '$' == fExpression.charAt(fDocumentOffset - 1)) {
				addProposal("", RegExMessages.getString("displayString_dollar"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_dollar")); //$NON-NLS-1$
			} else {
				if (!fIsEscape)
					addProposal("$", RegExMessages.getString("displayString_dollar"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_dollar")); //$NON-NLS-1$
				addBsProposal("\\", RegExMessages.getString("displayString_replace_cap"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_replace_cap")); //$NON-NLS-1$
				addBsProposal("\\", RegExMessages.getString("displayString_replace_bs"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_replace_bs")); //$NON-NLS-1$
				if (full) {
					addBsProposal("\\R", RegExMessages.getString("displayString_replace_bs_R"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_replace_bs_R")); //$NON-NLS-1$
					addBracketProposal("\\x", 2, RegExMessages.getString("displayString_bs_x"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_x")); //$NON-NLS-1$
					addBracketProposal("\\u", 2, RegExMessages.getString("displayString_bs_u"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_u")); //$NON-NLS-1$
					addBsProposal("\\t", RegExMessages.getString("displayString_bs_t"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_t")); //$NON-NLS-1$
					addBsProposal("\\n", RegExMessages.getString("displayString_replace_bs_n"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_replace_bs_n")); //$NON-NLS-1$
					addBsProposal("\\r", RegExMessages.getString("displayString_replace_bs_r"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_replace_bs_r")); //$NON-NLS-1$
					addBsProposal("\\f", RegExMessages.getString("displayString_bs_f"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_f")); //$NON-NLS-1$
					addBsProposal("\\a", RegExMessages.getString("displayString_bs_a"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_a")); //$NON-NLS-1$
					addBsProposal("\\e", RegExMessages.getString("displayString_bs_e"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_e")); //$NON-NLS-1$
					addBracketProposal("\\c", 2, RegExMessages.getString("displayString_bs_c"), //$NON-NLS-1$ //$NON-NLS-2$
							RegExMessages.getString("additionalInfo_bs_c")); //$NON-NLS-1$
				}
				addBsProposal("\\C", RegExMessages.getString("displayString_replace_bs_C"), //$NON-NLS-1$ //$NON-NLS-2$
						RegExMessages.getString("additionalInfo_replace_bs_C")); //$NON-NLS-1$
			}
			fPriorityProposals.addAll(fProposals);
			return fPriorityProposals.toArray(new IContentProposal[fPriorityProposals.size()]);
		}

		/**
		 * Adds a proposal.
		 *
		 * @param proposal
		 *            the string to be inserted
		 * @param displayString
		 *            the proposal's label
		 * @param additionalInfo
		 *            the additional information
		 */
		private void addProposal(String proposal, String displayString, String additionalInfo) {
			fProposals.add(new ContentProposal(proposal, displayString, additionalInfo));
		}

		/**
		 * Adds a proposal.
		 *
		 * @param proposal
		 *            the string to be inserted
		 * @param cursorPosition
		 *            the cursor position after insertion, relative to the start
		 *            of the proposal
		 * @param displayString
		 *            the proposal's label
		 * @param additionalInfo
		 *            the additional information
		 */
		private void addProposal(String proposal, int cursorPosition, String displayString, String additionalInfo) {
			fProposals.add(new ContentProposal(proposal, displayString, additionalInfo, cursorPosition));
		}

		/**
		 * Adds a proposal to the priority proposals list.
		 *
		 * @param proposal
		 *            the string to be inserted
		 * @param displayString
		 *            the proposal's label
		 * @param additionalInfo
		 *            the additional information
		 */
		private void addPriorityProposal(String proposal, String displayString, String additionalInfo) {
			fPriorityProposals.add(new ContentProposal(proposal, displayString, additionalInfo));
		}

		/**
		 * Adds a proposal. Ensures that existing pre- and postfixes are not
		 * duplicated.
		 *
		 * @param proposal
		 *            the string to be inserted
		 * @param cursorPosition
		 *            the cursor position after insertion, relative to the start
		 *            of the proposal
		 * @param displayString
		 *            the proposal's label
		 * @param additionalInfo
		 *            the additional information
		 */
		private void addBracketProposal(String proposal, int cursorPosition, String displayString,
				String additionalInfo) {
			String prolog = fExpression.substring(0, fDocumentOffset);
			if (!fIsEscape && prolog.endsWith("\\") && proposal.startsWith("\\")) { //$NON-NLS-1$//$NON-NLS-2$
				fProposals.add(new ContentProposal(proposal, displayString, additionalInfo, cursorPosition));
				return;
			}
			for (int i = 1; i <= cursorPosition; i++) {
				String prefix = proposal.substring(0, i);
				if (prolog.endsWith(prefix)) {
					String postfix = proposal.substring(cursorPosition);
					String epilog = fExpression.substring(fDocumentOffset);
					if (epilog.startsWith(postfix)) {
						fPriorityProposals.add(new ContentProposal(proposal.substring(i, cursorPosition), displayString,
								additionalInfo, cursorPosition - i));
					} else {
						fPriorityProposals.add(new ContentProposal(proposal.substring(i), displayString, additionalInfo,
								cursorPosition - i));
					}
					return;
				}
			}
			fProposals.add(new ContentProposal(proposal, displayString, additionalInfo, cursorPosition));
		}

		/**
		 * Adds a proposal that starts with a backslash. Ensures that the
		 * backslash is not repeated if already typed.
		 *
		 * @param proposal
		 *            the string to be inserted
		 * @param displayString
		 *            the proposal's label
		 * @param additionalInfo
		 *            the additional information
		 */
		private void addBsProposal(String proposal, String displayString, String additionalInfo) {
			String prolog = fExpression.substring(0, fDocumentOffset);
			int position = proposal.length();
			// If the string already contains the backslash, do not include in
			// the proposal
			if (prolog.endsWith("\\")) { //$NON-NLS-1$
				position--;
				proposal = proposal.substring(1);
			}

			if (fIsEscape) {
				fPriorityProposals.add(new ContentProposal(proposal, displayString, additionalInfo, position));
			} else {
				addProposal(proposal, position, displayString, additionalInfo);
			}
		}
	}

	/**
	 * <code>true</code> iff the processor is for the find field.
	 * <code>false</code> iff the processor is for the replace field.
	 */
	private final boolean fIsFind;

	private boolean full;

	/**
	 * Creates a new completion proposal provider.
	 *
	 * @param isFind
	 *            <code>true</code> if the provider is used for the 'find' field
	 *            <code>false</code> if the provider is used for the 'replace'
	 *            field
	 */
	public FindReplaceDocumentAdapterContentProposalProvider(boolean isFind, boolean full) {
		fIsFind = isFind;
		this.full = full;
	}

	/*
	 * @see
	 * org.eclipse.jface.fieldassist.IContentProposalProvider#getProposals(java
	 * .lang.String, int)
	 */
	public IContentProposal[] getProposals(String contents, int position) {
		if (fIsFind)
			return new ProposalComputer(contents, position, full).computeFindProposals();
		return new ProposalComputer(contents, position, full).computeReplaceProposals();
	}
}
