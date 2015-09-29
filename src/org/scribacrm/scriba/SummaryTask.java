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
import android.content.Context;
import android.os.AsyncTask;
import java.util.Date;
import java.util.Calendar;

public class SummaryTask extends AsyncTask<StringResource, Void, ProjectSummary> {

    public interface Listener {
        public void onSummaryReady(ProjectSummary summary);
    }

    private ProjectSummary _summary = new ProjectSummary();
    private Context _context = null;
    private Listener _listener = null;

    public SummaryTask(Context context, Listener listener) {
        _context = context;
        _listener = listener;
    }

    @Override
    protected ProjectSummary doInBackground(StringResource... params) {
        ScribaDBManager.useDB(_context);

        // calculate minimal mod time for sales and number of started projects
        // calculation
        Date curDate = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(curDate);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        // adjust date according to the request
        switch (params[0].resid()) {
        case R.string.period_week:
            cal.set(Calendar.WEEK_OF_MONTH, cal.get(Calendar.WEEK_OF_MONTH));
            cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            break;
        case R.string.period_month:
            cal.set(Calendar.DAY_OF_MONTH, 1);
            break;
        case R.string.period_year:
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            break;
        }

        calcSales(cal.getTimeInMillis() / 1000);
        calcProjectsInProgress();
        calcStarted(cal.getTimeInMillis() / 1000);

        ScribaDBManager.releaseDB();
        return _summary;
    }

    @Override
    protected void onPostExecute(ProjectSummary summary) {
        if (_listener != null) {
            _listener.onSummaryReady(summary);
        }
    }

    private void calcSales(long minModTime) {
        DataDescriptor[] projs = ScribaDB.getProjectsByStateTime(Project.State.CONTRACT_SIGNED,
            0, Project.TimeComp.IGNORE, minModTime, Project.TimeComp.AFTER);
        if (projs == null) {
            return;
        }
        projs = ScribaDB.fetchAll(projs);

        for (DataDescriptor proj : projs) {
            Project project = ScribaDB.getProject(proj.id);
            switch (project.currency) {
            case Project.Currency.RUB:
                _summary.rubSales += project.cost;
                break;
            case Project.Currency.USD:
                _summary.usdSales += project.cost;
                break;
            case Project.Currency.EUR:
                _summary.eurSales += project.cost;
                break;
            }
        }
    }

    private void calcProjectsInProgress() {
        DataDescriptor[] projs = ScribaDB.getProjectsByState(Project.State.INITIAL);
        if (projs != null) {
            projs = ScribaDB.fetchAll(projs);
            for (DataDescriptor proj : projs) {
                _summary.inProgress++;
            }
        }
        projs = ScribaDB.getProjectsByState(Project.State.CLIENT_INFORMED);
        if (projs != null) {
            projs = ScribaDB.fetchAll(projs);
            for (DataDescriptor proj : projs) {
                _summary.inProgress++;
            }
        }
        projs = ScribaDB.getProjectsByState(Project.State.CLIENT_RESPONSE);
        if (projs != null) {
            projs = ScribaDB.fetchAll(projs);
            for (DataDescriptor proj : projs) {
                _summary.inProgress++;
            }
        }
        projs = ScribaDB.getProjectsByState(Project.State.OFFER);
        if (projs != null) {
            projs = ScribaDB.fetchAll(projs);
            for (DataDescriptor proj : projs) {
                _summary.inProgress++;
            }
        }
        projs = ScribaDB.getProjectsByState(Project.State.CONTRACT_SIGNED);
        if (projs != null) {
            projs = ScribaDB.fetchAll(projs);
            for (DataDescriptor proj : projs) {
                _summary.inProgress++;
            }
        }
        projs = ScribaDB.getProjectsByState(Project.State.EXECUTION);
        if (projs != null) {
            projs = ScribaDB.fetchAll(projs);
            for (DataDescriptor proj : projs) {
                _summary.inProgress++;
            }
        }
    }

    private void calcStarted(long startTime) {
        DataDescriptor[] projs = ScribaDB.getProjectsByTime(startTime,
            Project.TimeComp.AFTER, 0, Project.TimeComp.IGNORE);
        if (projs == null) {
            return;
        }
        projs = ScribaDB.fetchAll(projs);
        for (DataDescriptor proj : projs) {
            _summary.started++;
        }
    }
}
