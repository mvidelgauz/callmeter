/*
 * Copyright (C) 2009-2010 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of Call Meter 3G.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.callmeter.ui.prefs;

import android.app.ListActivity;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import de.ub0r.android.callmeter.R;
import de.ub0r.android.callmeter.data.DataProvider;
import de.ub0r.android.callmeter.ui.prefs.Preference.BoolPreference;
import de.ub0r.android.callmeter.ui.prefs.Preference.CursorPreference;
import de.ub0r.android.callmeter.ui.prefs.Preference.ListPreference;
import de.ub0r.android.callmeter.ui.prefs.Preference.TextPreference;
import de.ub0r.android.lib.DbUtils;

/**
 * Edit a single Plan.
 * 
 * @author flx
 */
public class RuleEdit extends ListActivity implements OnClickListener,
		OnItemClickListener, OnDismissListener {
	/** {@link PreferenceAdapter}. */
	private PreferenceAdapter adapter = null;

	/** Activity result request id: rule. */
	private static final int REQUEST_RULE = 0;

	/** Extra for {@link Intent}: is child? */
	static final String EXTRA_ISCHILD = "is_child";

	/** Id of edited filed. */
	private long rid = -1;

	/** Data for is child. */
	private boolean isChild = false;

	/** Array holding {@link String}s. */
	private String[] inOutNomatterCalls = null;
	/** Array holding {@link String}s. */
	private String[] inOutNomatterSms = null;
	/** Array holding {@link String}s. */
	private String[] inOutNomatterMms = null;
	/** Array holding {@link String}s. */
	private String[] inOutNomatterData = null;
	/** Array holding {@link String}s. */
	private String[] yesNoNomatter = null;

	/**
	 * Get a {@link String}-Array for ListView.
	 * 
	 * @param base
	 *            base array without no_matter_
	 * @return array with no_matter_
	 */
	private String[] getStrings(final int base) {
		switch (base) {
		case R.array.direction_calls:
			if (this.inOutNomatterCalls == null) {
				final String[] tmp1 = new String[3];
				final String[] tmp2 = this.getResources().getStringArray(base);
				tmp1[0] = tmp2[0];
				tmp1[1] = tmp2[1];
				tmp1[2] = this.getString(R.string.no_matter_);
				this.inOutNomatterCalls = tmp1;
			}
			return this.inOutNomatterCalls;
		case R.array.direction_sms:
			if (this.inOutNomatterSms == null) {
				final String[] tmp1 = new String[3];
				final String[] tmp2 = this.getResources().getStringArray(base);
				tmp1[0] = tmp2[0];
				tmp1[1] = tmp2[1];
				tmp1[2] = this.getString(R.string.no_matter_);
				this.inOutNomatterSms = tmp1;
			}
			return this.inOutNomatterSms;
		case R.array.direction_mms:
			if (this.inOutNomatterMms == null) {
				final String[] tmp1 = new String[3];
				final String[] tmp2 = this.getResources().getStringArray(base);
				tmp1[0] = tmp2[0];
				tmp1[1] = tmp2[1];
				tmp1[2] = this.getString(R.string.no_matter_);
				this.inOutNomatterMms = tmp1;
			}
			return this.inOutNomatterMms;
		case R.array.direction_data:
			if (this.inOutNomatterData == null) {
				final String[] tmp1 = new String[3];
				final String[] tmp2 = this.getResources().getStringArray(base);
				tmp1[0] = tmp2[0];
				tmp1[1] = tmp2[1];
				tmp1[2] = this.getString(R.string.no_matter_);
				this.inOutNomatterData = tmp1;
			}
			return this.inOutNomatterData;
		default:
			if (this.yesNoNomatter == null) {
				final String[] tmp1 = new String[3];
				tmp1[0] = this.getString(R.string.yes);
				tmp1[1] = this.getString(R.string.no);
				tmp1[2] = this.getString(R.string.no_matter_);
				this.yesNoNomatter = tmp1;
			}
			return this.yesNoNomatter;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(this.getString(R.string.settings) + " > "
				+ this.getString(R.string.rules) + " > "
				+ this.getString(R.string.edit_));
		this.setContentView(R.layout.list_ok_cancel);

		this.getListView().setOnItemClickListener(this);
		this.findViewById(R.id.ok).setOnClickListener(this);
		this.findViewById(R.id.cancel).setOnClickListener(this);

		this.fillFields();
		this.fillPlan();
		this.showHideFileds();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onResume() {
		super.onResume();
		this.showHideFileds();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void onActivityResult(final int requestCode,
			final int resultCode, final Intent data) {
		long w = -1L;
		if (resultCode == RESULT_OK) {
			final Uri uri = data.getData();
			switch (requestCode) {
			case REQUEST_RULE:
				if (uri == null) {
					w = -1L;
				} else {
					w = ContentUris.parseId(uri);
				}
				((CursorPreference) this.adapter
						.getPreference(DataProvider.Rules.AND_PLAN))
						.setValue(w);
				break;
			default:
				break;
			}
			this.adapter.notifyDataSetInvalidated();
		}
	}

	/**
	 * Set plan's and what0 value.
	 */
	private void fillPlan() {
		final int t = ((ListPreference) this.adapter
				.getPreference(DataProvider.Rules.WHAT)).getValue();
		String where = null;
		switch (t) {
		case DataProvider.Rules.WHAT_CALL:
			where = DataProvider.Plans.TYPE + " = " + DataProvider.TYPE_CALL
					+ " OR " + DataProvider.Plans.TYPE + " = "
					+ DataProvider.TYPE_MIXED;
			break;
		case DataProvider.Rules.WHAT_DATA:
			where = DataProvider.Plans.TYPE + " = " + DataProvider.TYPE_DATA;
			break;
		case DataProvider.Rules.WHAT_SMS:
		case DataProvider.Rules.WHAT_MMS:
			where = DataProvider.Plans.TYPE + " = " + DataProvider.TYPE_SMS
					+ " OR " + DataProvider.Plans.TYPE + " = "
					+ DataProvider.TYPE_MMS + " OR " + DataProvider.Plans.TYPE
					+ " = " + DataProvider.TYPE_MIXED;
			break;
		default:
			where = DataProvider.Plans.WHERE_REALPLANS;
			break;
		}

		((CursorPreference) this.adapter
				.getPreference(DataProvider.Rules.PLAN_ID)).setCursor(where);
	}

	/**
	 * Get a new {@link PreferenceAdapter} for this {@link ListActivity}.
	 * 
	 * @return {@link PreferenceAdapter}
	 */
	private PreferenceAdapter getAdapter() {
		final PreferenceAdapter ret = new PreferenceAdapter(this);
		ret.add(new TextPreference(this, DataProvider.Rules.NAME, this
				.getString(R.string.rules_new), R.string.name_,
				R.string.name_help, InputType.TYPE_CLASS_TEXT));
		ret.add(new ListPreference(this, DataProvider.Rules.WHAT,
				DataProvider.Rules.WHAT_CALL, R.string.what_,
				R.string.what_help, R.array.rules_type));
		ret.add(new BoolPreference(this, DataProvider.Rules.NOT,
				R.string.negate_, R.string.negate_help, this));
		ret.add(new CursorPreference(this, DataProvider.Rules.PLAN_ID,
				R.string.plan_, R.string.plan_help, -1, -1, -1,
				DataProvider.Plans.CONTENT_URI, DataProvider.Plans.ID,
				DataProvider.Plans.NAME, null, null, null, null));
		ret.add(new ListPreference(this, DataProvider.Rules.DIRECTION,
				DataProvider.Rules.NO_MATTER, R.string.direction_,
				R.string.direction_help, this
						.getStrings(R.array.direction_calls)));
		ret.add(new ListPreference(this, DataProvider.Rules.ROAMED,
				DataProvider.Rules.NO_MATTER, R.string.roamed_,
				R.string.roamed_help, this.getStrings(-1)));
		final DialogInterface.OnClickListener editHours = // .
		new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				RuleEdit.this.startActivity(new Intent(RuleEdit.this, // .
						HourGroups.class));
			}
		};
		ret.add(new CursorPreference(this, DataProvider.Rules.INHOURS_ID,
				R.string.hourgroup_, R.string.hourgroup_help,
				R.string.edit_groups_, -1, -1,
				DataProvider.HoursGroup.CONTENT_URI,
				DataProvider.HoursGroup.ID, DataProvider.HoursGroup.NAME, null,
				editHours, null, null));
		ret.add(new CursorPreference(this, DataProvider.Rules.EXHOURS_ID,
				R.string.exhourgroup_, R.string.exhourgroup_help,
				R.string.edit_groups_, -1, -1,
				DataProvider.HoursGroup.CONTENT_URI,
				DataProvider.HoursGroup.ID, DataProvider.HoursGroup.NAME, null,
				editHours, null, null));
		final DialogInterface.OnClickListener editNumbers = // .
		new DialogInterface.OnClickListener() {
			@Override
			public void onClick(final DialogInterface dialog, final int which) {
				RuleEdit.this.startActivity(new Intent(RuleEdit.this, // .
						NumberGroups.class));
			}
		};
		ret.add(new CursorPreference(this, DataProvider.Rules.INNUMBERS_ID,
				R.string.numbergroup_, R.string.numbergroup_help,
				R.string.edit_groups_, -1, -1,
				DataProvider.NumbersGroup.CONTENT_URI,
				DataProvider.NumbersGroup.ID, DataProvider.NumbersGroup.NAME,
				null, editNumbers, null, null));
		ret.add(new CursorPreference(this, DataProvider.Rules.EXNUMBERS_ID,
				R.string.numbergroup_, R.string.numbergroup_help,
				R.string.edit_groups_, -1, -1,
				DataProvider.NumbersGroup.CONTENT_URI,
				DataProvider.NumbersGroup.ID, DataProvider.NumbersGroup.NAME,
				null, editNumbers, null, null));
		ret.add(new CursorPreference(this, DataProvider.Rules.AND_PLAN,
				R.string.what1_, R.string.what1_help, R.string.edit_selected_,
				R.string.new_, R.string.clear_, DataProvider.Rules.CONTENT_URI,
				DataProvider.Rules.ID, DataProvider.Rules.NAME, DbUtils.sqlAnd(
						DataProvider.Rules.ISCHILD + " = 1",
						DataProvider.Rules.ID + " != "
								+ DataProvider.Rules.AND_PLAN),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						final long sel = ((CursorPreference) // .
						RuleEdit.this.adapter.getPreference(DataProvider.// .
								Rules.AND_PLAN)).getValue();
						if (sel < 0) {
							return;
						}
						final Intent fi = new Intent(Intent.ACTION_VIEW,
								ContentUris.withAppendedId(
										DataProvider.Rules.CONTENT_URI, sel),
								RuleEdit.this, RuleEdit.class);
						fi.putExtra(EXTRA_ISCHILD, true);
						RuleEdit.this.startActivityForResult(fi, REQUEST_RULE);
					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						final Intent fi = new Intent(RuleEdit.this,
								RuleEdit.class);
						fi.putExtra(EXTRA_ISCHILD, true);
						RuleEdit.this.startActivityForResult(fi, REQUEST_RULE);
					}
				}, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						((CursorPreference) RuleEdit.this.adapter
								.getPreference(DataProvider.// .
								Rules.AND_PLAN)).clearValue();
					}
				}));
		return ret;
	}

	/**
	 * Fill the fields with data from the cursor.
	 */
	private void fillFields() {
		this.isChild = this.getIntent().getBooleanExtra(EXTRA_ISCHILD, false);
		final Uri uri = this.getIntent().getData();
		int nid = -1;
		Cursor cursor = null;
		if (uri != null) {
			cursor = this.getContentResolver().query(uri,
					DataProvider.Rules.PROJECTION, null, null, null);
			if (cursor == null || !cursor.moveToFirst()) {
				cursor = null;
				this.rid = -1;
			}
		}
		if (cursor != null) {
			nid = cursor.getInt(DataProvider.Rules.INDEX_ID);
		}
		if (this.rid == -1 || nid != this.rid) {
			this.rid = nid;
			this.adapter = this.getAdapter();
			this.getListView().setAdapter(this.adapter);
		}
		if (cursor != null && !cursor.isClosed()) {
			this.adapter.load(cursor);
			cursor.close();
		}
	}

	/**
	 * Show or hide fields based on data in there.
	 */
	private void showHideFileds() {
		// FIXME
		this.adapter.hide(DataProvider.Rules.WHAT0, true);
		// FIXME
		this.adapter.hide(DataProvider.Rules.NOT, true);
		this.adapter.hide(DataProvider.Rules.PLAN_ID, this.isChild);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.ok:
			final ContentValues cv = this.adapter.save();
			cv.put(DataProvider.Rules.ISCHILD, this.isChild);

			Uri uri = this.getIntent().getData();
			if (uri == null) {
				uri = this.getContentResolver().insert(
						DataProvider.Rules.CONTENT_URI, cv);
			} else {
				this.getContentResolver().update(uri, cv, null, null);
			}
			this.rid = -1;
			final Intent intent = new Intent(this, RuleEdit.class);
			intent.setData(uri);
			this.setResult(RESULT_OK, new Intent(intent));
			this.finish();
			break;
		case R.id.cancel:
			this.rid = -1;
			this.setResult(RESULT_CANCELED);
			this.finish();
			break;
		default:
			break;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Preference p = this.adapter.getItem(position);
		p.showDialog(this);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onDismiss(final DialogInterface dialog) {
		this.showHideFileds();
		this.fillPlan();
		this.adapter.notifyDataSetInvalidated();
	}
}
