/*
 * Copyright (C) 2014 Mikhail Sapozhnikov
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
import android.app.DialogFragment;
import android.os.Bundle;
import android.app.FragmentManager;
import android.app.Dialog;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.content.DialogInterface;

// this class manages dialog used to filter projects by state in project list
public class ProjStateFilterDialog extends DialogFragment
                                   implements DialogInterface.OnClickListener {

    public interface DismissListener {
        public void onDismiss();
    }

    private byte _projectState = Project.State.INITIAL;
    private ProjectStateMapper _projStateMapper = null;
    private DismissListener _listener = null;

    public ProjStateFilterDialog(byte state, DismissListener listener) {
        _projectState = state;
        _listener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        _projStateMapper = new ProjectStateMapper(getActivity());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String[] states = _projStateMapper.getStrings();
        int checked = 0;
        for (int i = 0; i < states.length; i++) {
            if (_projStateMapper.isEqual(_projectState, states[i])) {
                checked = i;
                break;
            }
        }
        builder.setSingleChoiceItems(states, checked, this);
        builder.setTitle(R.string.project_state_spinner_prompt);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        String[] states = _projStateMapper.getStrings();
        String checked = states[which];
        _projectState = _projStateMapper.getCode(checked);
        dialog.dismiss();
        if (_listener != null) {
            _listener.onDismiss();
        }
    }

    public byte getProjectState() {
        return _projectState;
    }
}
