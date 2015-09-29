/*
 * Copyright (C) 2015 Mikhail Sapozhnikov
 *
 * This file is part of scriba-android.
 *
 * scriba-android is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * scriba-android is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with scriba-android. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.scribacrm.scriba;

import org.scribacrm.libscriba.*;
import android.app.Activity;
import android.util.Log;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

public class SummaryFragment extends Fragment
                             implements AdapterView.OnItemSelectedListener,
                                        SummaryTask.Listener {

    private StringResource _defaultRes = null;
    private ArrayAdapter<StringResource> _periodAdapter = null;
    private SummaryTask _summaryTask = null;

    @Override
    public void onAttach(Activity activity) {
        _defaultRes = new StringResource(activity, R.string.period_month);
        super.onAttach(activity);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.project_report, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onStart() {
        populatePeriodSpinner();
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    // AdapterView.OnItemSelectedLIstener implementation
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (_periodAdapter != null) {
            StringResource res = _periodAdapter.getItem(position);
            onPeriodChange(res);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // reset to default
        onPeriodChange(_defaultRes);
    }

    // SummaryTask.Listener implementation
    @Override
    public void onSummaryReady(ProjectSummary summary) {
        TextView txt = (TextView)getActivity().findViewById(R.id.total_sales_text);
        String totalSalesStr = summary.rubSales + " " +
            getActivity().getResources().getString(R.string.currency_rub) + "\n" +
            summary.usdSales + " " +
            getActivity().getResources().getString(R.string.currency_usd) + "\n" +
            summary.eurSales + " " +
            getActivity().getResources().getString(R.string.currency_eur);
        txt.setText(totalSalesStr);

        txt = (TextView)getActivity().findViewById(R.id.proj_in_progress_text);
        txt.setText(new Long(summary.inProgress).toString());

        txt = (TextView)getActivity().findViewById(R.id.proj_started_text);
        txt.setText(new Long(summary.started).toString());
    }

    private void populatePeriodSpinner() {
        if (_periodAdapter == null) {
            _periodAdapter = new ArrayAdapter<StringResource>(getActivity(),
                android.R.layout.simple_spinner_item);
            _periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        }

        _periodAdapter.add(_defaultRes);
        _periodAdapter.add(new StringResource(getActivity(), R.string.period_week));
        _periodAdapter.add(new StringResource(getActivity(), R.string.period_year));

        Spinner spinner = (Spinner)getActivity().findViewById(R.id.report_period_spinner);
        spinner.setAdapter(_periodAdapter);
        spinner.setOnItemSelectedListener(this);
        // set selection to default value
        int pos = _periodAdapter.getPosition(_defaultRes);
        spinner.setSelection(pos);
        onPeriodChange(_defaultRes);
    }

    private void onPeriodChange(StringResource newPeriod) {
        Log.d("[Scriba]", "SummaryFragment.onPeriodChange, newPeriod is " + newPeriod.toString());
        _summaryTask = new SummaryTask(getActivity(), this);
        _summaryTask.execute(newPeriod);
    }
}
