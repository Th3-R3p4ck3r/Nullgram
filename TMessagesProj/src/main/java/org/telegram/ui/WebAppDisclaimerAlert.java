/*
 * Copyright (C) 2019-2024 qwq233 <qwq233@qwq2333.top>
 * https://github.com/qwq233/Nullgram
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this software.
 *  If not, see
 * <https://www.gnu.org/licenses/>
 */

package org.telegram.ui;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.util.Consumer;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Cells.CheckBoxCell;
import org.telegram.ui.Components.LayoutHelper;

public class WebAppDisclaimerAlert {


    private CheckBoxCell cell;
    private CheckBoxCell cell2;
    private AlertDialog alert;
    private TextView positiveButton;

    public static void show(Context context, Consumer<Boolean> consumer, TLRPC.User withSendMessage, Runnable dismissed) {
        WebAppDisclaimerAlert alert = new WebAppDisclaimerAlert();

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle(LocaleController.getString("TermsOfUse", R.string.TermsOfUse));

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        TextView textView = new TextView(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            textView.setLetterSpacing(0.025f);
        }
        textView.setTextColor(Theme.getColor(Theme.key_dialogTextBlack));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        linearLayout.addView(textView, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, 0, 24, 0, 24, 0));

        alert.cell = new CheckBoxCell(context, 1, null);
        alert.cell.getTextView().getLayoutParams().width = LayoutHelper.MATCH_PARENT;
        alert.cell.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        linearLayout.addView(alert.cell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT, 8, 0, 8, 0));

//        if (withSendMessage != null) {
//            alert.cell2 = new CheckBoxCell(context, 1, null);
//            alert.cell2.getTextView().setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
//            linearLayout.addView(alert.cell2, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT, 8, -8, 8, 0));
//            alert.cell2.setText(AndroidUtilities.replaceTags(LocaleController.formatString("OpenUrlOption2", R.string.OpenUrlOption2, UserObject.getUserName(withSendMessage))), "", true, false);
//            alert.cell2.setOnClickListener(v -> {
//                alert.cell2.setChecked(!alert.cell2.isChecked(), true);
//            });
//        }

        final boolean[] dismissing = new boolean[1];
        textView.setText(AndroidUtilities.replaceTags(LocaleController.getString("BotWebAppDisclaimerSubtitle", R.string.BotWebAppDisclaimerSubtitle)));
        alert.cell.setText(AndroidUtilities.replaceSingleTag(LocaleController.getString("BotWebAppDisclaimerCheck", R.string.BotWebAppDisclaimerCheck), () -> {
            Browser.openUrl(context, LocaleController.getString("WebAppDisclaimerUrl", R.string.WebAppDisclaimerUrl));
        }), "", false, false);
        alertDialog.setView(linearLayout);
        alertDialog.setPositiveButton(LocaleController.getString("Continue", R.string.Continue), (dialog, which) -> {
            consumer.accept(true);
            dismissing[0] = true;
            dialog.dismiss();
        });
        alertDialog.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), (dialog, which) -> {
            dialog.dismiss();
        });
        alert.alert = alertDialog.create();
        alert.alert.show();
        alert.positiveButton = (TextView) alert.alert.getButton(DialogInterface.BUTTON_POSITIVE);
        alert.positiveButton.setEnabled(false);
        alert.positiveButton.setAlpha(0.5f);
        alert.cell.setOnClickListener(v -> {
            alert.cell.setChecked(!alert.cell.isChecked(), true);
            alert.positiveButton.setEnabled(alert.cell.isChecked());
            alert.positiveButton.animate().alpha(alert.cell.isChecked() ? 1f : 0.5f).start();
        });
        alert.cell.setBackground(Theme.createSelectorDrawable(Theme.getColor(Theme.key_listSelector), Theme.RIPPLE_MASK_ROUNDRECT_6DP));
        alert.alert.setOnDismissListener(d -> {
            if (!dismissing[0]) {
                dismissing[0] = true;
                if (dismissed != null) {
                    dismissed.run();
                }
            }
        });
    }
}
